package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.InetSecurity511Panel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.CachedDaemonThreadPoolProvider;
import net.adoptopenjdk.icedteaweb.resources.PrioritizedParallelExecutor;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.ACCEPT_ENCODING_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.PACK_200_OR_GZIP;

/**
 * Base class with commonly used methods.
 */
abstract class BaseResourceInitializer implements ResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(BaseResourceInitializer.class);

    private static final ExecutorService remoteExecutor = CachedDaemonThreadPoolProvider.getThreadPool();

    private static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    protected final Resource resource;

    BaseResourceInitializer(Resource resource) {
        this.resource = resource;
    }

    InitializationResult initFromCache(VersionId version) {
        final File cachedFile = Cache.getCacheFile(this.resource.getLocation(), version);

        resource.setStatus(DOWNLOADED);
        resource.setSize(cachedFile.length());
        resource.setLocalFile(cachedFile);
        resource.setTransferred(cachedFile.length());

        LOG.debug("Use cached version of resource {}", resource);

        return new InitializationResult();
    }

    InitializationResult initFromHeadResult(UrlRequestResult requestResult) {
        resource.setSize(requestResult.getContentLength());
        return new InitializationResult(requestResult.getUrl());
    }

    void invalidateExistingEntryInCache(VersionId version) {
        final URL location = resource.getLocation();
        LOG.debug("Invalidating resource in cache: {} / {}", location, version);
        Cache.replaceExistingCacheFile(location, version);
    }

    DownloadOptions getDownloadOptions() {
        DownloadOptions options = resource.getDownloadOptions();
        if (options == null) {
            return DownloadOptions.NONE;
        }
        return options;
    }

    Optional<UrlRequestResult> getBestUrlByPingingWithHeadRequest(List<URL> urls) {
        final List<Callable<UrlRequestResult>> callables = urls.stream()
                .map(url -> (Callable<UrlRequestResult>) () -> testUrl(url))
                .collect(Collectors.toList());

        final PrioritizedParallelExecutor executor = new PrioritizedParallelExecutor(remoteExecutor);
        final Future<UrlRequestResult> future = executor.getSuccessfulResultWithHighestPriority(callables);

        try {
            return Optional.ofNullable(future.get());
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("failed to determine best URL: {}",  e.getMessage());
            return Optional.empty();
        }
    }

    private UrlRequestResult testUrl(URL url) throws IOException {
        final HttpMethod requestMethod = HttpMethod.HEAD;
        try {
            final Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put(ACCEPT_ENCODING_HEADER, PACK_200_OR_GZIP);

            final UrlRequestResult response = UrlProber.getUrlResponseCodeWithRedirectionResult(url, requestProperties, requestMethod);
            if (response.getResponseCode() == NETWORK_AUTHENTICATION_REQUIRED && !InetSecurity511Panel.isSkip()) {
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
                throw new RuntimeException("Server returned " + response.getResponseCode() + " for " + url);
            }

            LOG.debug("Best url for {} is {} by {}", resource.toString(), url.toString(), requestMethod);
            return response;
        } catch (IOException e) {
            LOG.debug("While processing {}  by {} for resource {} got {}", url, requestMethod, resource, e.getMessage());
            throw e;
        }
    }
}
