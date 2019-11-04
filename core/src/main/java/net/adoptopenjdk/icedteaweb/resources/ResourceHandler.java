package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.ERROR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

class ResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);
    private static final Executor localExecutor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * 2,
            10L, TimeUnit.SECONDS, new SynchronousQueue<>());

    private static final String LOCALHOST = "localhost";
    private static final String IPV4_LOOPBACK_PREFIX = "127.";

    private final Resource resource;

    ResourceHandler(Resource resource) {
        this.resource = Assert.requireNonNull(resource, "resource");
    }

    Future<Resource> putIntoCache() {
        validateWithWhitelist();
        final CompletableFuture<Resource> result = new CompletableFuture<>();

        // the thread which is processing this resource will set its future onto the resource all other
        // threads will return this future and ensure a resource is only processed by a single thread
        synchronized (resource) {
            final Future<Resource> futureResource = resource.getFutureForDownloaded();
            if (futureResource != null) {
                return futureResource;
            }
            resource.startProcessing(result);
        }

        if (resource.isComplete()) {
            result.complete(resource);
        } else if (isNotCacheable()) {
            result.complete(initNoneCacheableResources());
        } else {
            localExecutor.execute(() -> {
                try {
                    result.complete(download());
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
        }

        return result;
    }

    private boolean isNotCacheable() {
        return !CacheUtil.isCacheable(resource.getLocation());
    }

    private Resource initNoneCacheableResources() {
        resource.setStatus(DOWNLOADED);
        if (resource.getLocation().getProtocol().equals("file")) {
            final File file = new File(resource.getLocation().getPath());
            resource.setSize(file.length());
            resource.setLocalFile(file);
            resource.setTransferred(file.length());
        }
        return resource;
    }

    private Resource download() {
        int triesLeft = 2;
        while (true) {
            try {
                return downloadResource();
            } catch (Exception e) {
                if (--triesLeft <= 0) {
                    resource.setStatus(ERROR);
                    throw e;
                }
            }
        }
    }

    private Resource downloadResource() {
        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }

    private void validateWithWhitelist() {
        final URL url = resource.getLocation();
        Assert.requireNonNull(url, "url");

        if (url.getHost().equals(LOCALHOST) || url.getHost().startsWith(IPV4_LOOPBACK_PREFIX)) {
            return; // local server need not be in whitelist
        }

        final String urlString = url.getProtocol() + "://" + url.getHost() + ((url.getPort() != -1) ? ":" + url.getPort() : "");

        final List<String> whitelist = JNLPRuntime.getConfiguration().getPropertyAsList(KEY_SECURITY_SERVER_WHITELIST, ',');
        if (!whitelist.isEmpty() && !whitelist.contains(urlString)) {
            BasicExceptionDialog.show(new SecurityException(Translator.R("SWPInvalidURL") + ": " + resource.getLocation()));
            LOG.error("Resource URL not In Whitelist: {}", resource.getLocation());
            JNLPRuntime.exit(-1);
        }
    }
}
