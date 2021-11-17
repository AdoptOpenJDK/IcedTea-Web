package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.ResourceInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.resources.initializer.ResourceUrlCreator.getUrl;

/**
 * Initializer for unversioned resources.
 */
class UnversionedResourceInitializer extends BaseResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(UnversionedResourceInitializer.class);

    UnversionedResourceInitializer(Resource resource) {
        super(resource);
    }

    @Override
    public InitializationResult init() {
        boolean isCached = Cache.isCached(resource.getLocation(), null);

        if (!isCached) {
            return findDownloadUrl();
        } else if (needsUpdateCheck()) {
            return checkForUpdate();
        } else {
            return initFromCache();
        }
    }

    private InitializationResult findDownloadUrl() {
        final List<URL> candidateUrls = getUrlCandidates();
        LOG.debug("Candidate URLs for {}: {}", resource, candidateUrls);
        return getBestUrlByPingingWithHeadRequest(candidateUrls)
                .map(requestResult -> {
                    LOG.debug("Found best URL for {}: {}", resource, requestResult);
                    return initFromHeadResult(requestResult);
                })
                .orElseGet(() -> {
                    LOG.debug("Failed to determine best URL for {} will try all of {}", resource, candidateUrls);
                    return new InitializationResult(candidateUrls);
                });
    }

    private InitializationResult checkForUpdate() {
        final List<URL> candidateUrls = getUrlCandidates();
        LOG.debug("Candidate URLs for {}: {}", resource, candidateUrls);
        return getBestUrlByPingingWithHeadRequest(candidateUrls)
                .map(requestResult -> {
                    if (needsUpdate(requestResult)) {
                        LOG.debug("Found best URL for {}: {}", resource, requestResult);
                        invalidateExistingEntryInCache(null);
                        return initFromHeadResult(requestResult);
                    } else {
                        return initFromCache();
                    }
                })
                .orElseGet(() -> {
                    LOG.debug("Failed to determine best URL for {} will try all of {}", resource, candidateUrls);
                    return new InitializationResult(candidateUrls);
                });
    }

    private List<URL> getUrlCandidates() {
        final List<URL> candidates = new ArrayList<>();

        final boolean usePack = getDownloadOptions().useExplicitPack();
        final URL packUrl = getUrl(resource, usePack, false);
        if (packUrl != null) {
            candidates.add(packUrl);
        }

        candidates.add(resource.getLocation());

        return ResourceUrlCreator.prependHttps(candidates);
    }

    private boolean needsUpdateCheck() {
        final ResourceInfo info = Cache.getInfo(resource.getLocation(), null);

        final boolean result = resource.forceUpdateRequested()
                || info == null
                || resource.getUpdatePolicy().shouldUpdate(info);
        LOG.debug("needsUpdateCheck: {} -> {}", resource.getLocation(), result);
        return result;
    }

    private boolean needsUpdate(final UrlRequestResult requestResult) {
        final boolean result = resource.forceUpdateRequested()
                || ! Cache.isUpToDate(resource.getLocation(), null, requestResult.getLastModified());
        LOG.debug("needsUpdate: {} -> {}", resource.getLocation(), result);
        return result;
    }

    private InitializationResult initFromCache() {
        return initFromCache(null);
    }

}
