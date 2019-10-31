package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.InetSecurity511Panel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.cache.CachedDaemonThreadPoolProvider;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.cache.ResourceUrlCreator;
import net.sourceforge.jnlp.cache.UrlRequestResult;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;

/**
 * Base class with commonly used methods.
 */
abstract class BaseResourceInitializer implements ResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(BaseResourceInitializer.class);

    private static final Executor remoteExecutor = CachedDaemonThreadPoolProvider.getThreadPool();

    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String PACK_200_OR_GZIP = "pack200-gzip, gzip";

    protected final Resource resource;

    BaseResourceInitializer(Resource resource) {
        this.resource = resource;
    }

    InitializationResult initFromCache(VersionId version) {
        final File cachedFile = Cache.getCacheFile(this.resource.getLocation(), version);

        synchronized (resource) {
            resource.setSize(cachedFile.length());
            resource.setLocalFile(cachedFile);
            resource.setDownloadVersion(version);
            resource.setTransferred(cachedFile.length());
            resource.changeStatus(null, EnumSet.of(DOWNLOADED));
            resource.notifyAll();
        }

        LOG.debug("Use cached version of resource {}", resource);

        return new InitializationResult();
    }

    InitializationResult initFromHeadResult(UrlRequestResult requestResult) {
        synchronized (resource) {
            resource.setSize(requestResult.getContentLength());
        }

        return new InitializationResult(requestResult);
    }

    DownloadOptions getDownloadOptions() {
        DownloadOptions options = resource.getDownloadOptions();
        if (options == null) {
            return DownloadOptions.NONE;
        }
        return options;
    }

    Optional<UrlRequestResult> getBestUrlByPingingWithHeadRequest(List<URL> urls) {
        return urls.stream()
                .map(this::testSingleUrl)
                .map(this::futureToOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(UrlRequestResult::isSuccess)
                .findFirst()
                ;
    }

    private Future<UrlRequestResult> testSingleUrl(URL url) {
        final CompletableFuture<UrlRequestResult> result = new CompletableFuture<>();
        remoteExecutor.execute(() -> {
            try {
                result.complete(testUrl(url));
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private UrlRequestResult testUrl(URL url) {
        final HttpMethod requestMethod = HttpMethod.HEAD;
        try {
            final Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put(ACCEPT_ENCODING, PACK_200_OR_GZIP);

            final UrlRequestResult response = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(url, requestProperties, requestMethod);
            if (response.getResponseCode() == 511 && !InetSecurity511Panel.isSkip()) {
                boolean result511 = SecurityDialogs.show511Dialogue(resource);
                if (!result511) {
                    throw new RuntimeException("Terminated on users request after encountering 'http 511 authentication'.");
                }
                //try again
                return testUrl(url);
            }
            if (response.isRedirect()) {
                LOG.debug("Resource {} got redirect {} code for {} request for {} adding {} to list of possible urls", resource.toString(), response.getResponseCode(), requestMethod, url.toExternalForm(), response.getLocation().toExternalForm());
                if (!JNLPRuntime.isAllowRedirect()) {
                    throw new RuntimeException("The resource " + url.toExternalForm() + " is being redirected (" + response.getResponseCode() + ") to " + response.getLocation().toExternalForm() + ". This is disabled by default. If you wont to allow it, run javaws with -allowredirect parameter.");
                }
                return testUrl(response.getLocation());
            }

            if (!response.isSuccess()) {
                LOG.debug("For {} the server returned {} code for {} request for {}", resource.toString(), response.getResponseCode(), requestMethod, url.toExternalForm());
            }

            LOG.debug("Best url for {} is {} by {}", resource.toString(), url.toString(), requestMethod);
            if (response.getLocation() == null) {
                return response.withLocation(url);
            }
            return response;
        } catch (IOException e) {
            LOG.debug("While processing {}  by {} for resource {} got {}", url, requestMethod, resource, e.getMessage());
        }

        return null;
    }

    private <R> Optional<R> futureToOptional(Future<R> future) {
        try {
            return Optional.ofNullable(future.get());
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }
}
