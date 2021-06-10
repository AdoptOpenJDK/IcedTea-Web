package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.ERROR;
import static net.sourceforge.jnlp.cache.CacheUtil.isNonCacheable;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;
import static net.sourceforge.jnlp.util.UrlUtils.decodeUrlQuietly;

class ResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);

    static Future<Resource> putIntoCache(final Resource resource, final Executor downloadExecutor) {
        final CompletableFuture<Resource> result = new CompletableFuture<>();
        if (resource.isComplete()) {
            LOG.debug("Resource is already completed: {} ", resource.getSimpleName());
            result.complete(resource);
        } else if (isNonCacheable(resource.getLocation())) {
            LOG.debug("Resource is not cacheable: {}", resource.getSimpleName());
            result.complete(initNoneCacheableResources(resource));
        } else {
            downloadExecutor.execute(() -> {
                try {
                    result.complete(download(resource));
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
        }
        return result;
    }

    private static Resource initNoneCacheableResources(final Resource resource) {
        resource.setStatus(DOWNLOADED);
        if (resource.getLocation().getProtocol().equals(FILE_PROTOCOL)) {
            final File file = new File(decodeUrlQuietly(resource.getLocation()).getPath());
            resource.setSize(file.length());
            resource.setLocalFile(file);
            resource.setTransferred(file.length());
        }
        return resource;
    }

    private static Resource download(final Resource resource) {
        LOG.debug("Starting download of: {}", resource.getSimpleName());
        int numErrors = 0;
        while (true) {
            try {
                return downloadResource(resource);
            } catch (Exception e) {
                numErrors += 1;
                if (numErrors >= 3) {
                    LOG.debug("Exception while downloading '{}'", resource.getSimpleName(), e);
                    resource.setStatus(ERROR);
                    throw e;
                }
            }
        }
    }

    private static Resource downloadResource(final Resource resource) {
        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }

}
