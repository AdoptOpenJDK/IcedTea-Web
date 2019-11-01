package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.cache.ResourceUrlCreator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionString.ANY_VERSION;
import static net.sourceforge.jnlp.cache.ResourceUrlCreator.getUrl;
import static net.sourceforge.jnlp.cache.ResourceUrlCreator.getVersionedUrl;

/**
 * Initializer for resources with a version range.
 */
class RangeVersionedResourceInitializer extends BaseResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(RangeVersionedResourceInitializer.class);

    private boolean isCached;
    private VersionId cachedVersion;

    RangeVersionedResourceInitializer(Resource resource) {
        super(resource);
    }

    @Override
    public InitializationResult init() {
        isCached = Cache.isAnyCached(resource.getLocation(), resource.getRequestVersion());
        cachedVersion = Cache.getBestMatchingVersionInCache(resource.getLocation(), resource.getRequestVersion());

        final List<URL> candidateUrls = getUrlCandidates();
        LOG.debug("Candidate URLs for {}: {}", resource, candidateUrls);

        return getBestUrlByPingingWithHeadRequest(candidateUrls)
                .map(requestResult -> {
                    final VersionId remoteVersion = requestResult.getVersion();
                    if (isCached && remoteVersion != null && cachedVersion != null) {
                        if (cachedVersion.equals(remoteVersion) && resource.forceUpdateRequested()) {
                            invalidateExistingEntryInCache(cachedVersion);
                        } else if (cachedVersion.compareTo(remoteVersion) >= 0) {
                            return initFromCache(cachedVersion);
                        }
                    }

                    LOG.debug("Found best URL for {}: {}", resource, requestResult);
                    return initFromHeadResult(requestResult);
                })
                .orElseGet(() -> {
                    LOG.debug("Failed to determine best URL for {} will try all of {}", resource, candidateUrls);
                    return new InitializationResult(candidateUrls);
                });
    }

    private List<URL> getUrlCandidates() {
        final VersionId anyCachedVersion = cachedVersion != null ? cachedVersion : Cache.getBestMatchingVersionInCache(resource.getLocation(), ANY_VERSION);
        final List<URL> candidates = new ArrayList<>();

        final boolean usePack = getDownloadOptions().useExplicitPack();
        final URL packUrl = getUrl(resource, usePack, false);
        final URL versionedPackUrl = getVersionedUrl(packUrl, resource.getRequestVersion(), anyCachedVersion);
        if (versionedPackUrl != null) {
            candidates.add(versionedPackUrl);
        }

        final URL versionedUrl = getVersionedUrl(resource.getLocation(), resource.getRequestVersion(), anyCachedVersion);
        if (versionedUrl != null) {
            candidates.add(versionedUrl);
        }

        if (candidates.isEmpty()) {
            LOG.error("Failed to find a candidate URL for {}", resource);
        }

        return ResourceUrlCreator.prependHttps(candidates);
    }
}
