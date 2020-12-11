package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DownloadOptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.adoptopenjdk.icedteaweb.resources.initializer.ResourceUrlCreator.getUrl;
import static net.adoptopenjdk.icedteaweb.resources.initializer.ResourceUrlCreator.getVersionedUrl;

/**
 * Initializer for resources with an exact version.
 */
class ExactVersionedResourceInitializer extends BaseResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ExactVersionedResourceInitializer.class);

    private final VersionId versionId;

    ExactVersionedResourceInitializer(Resource resource) {
        super(resource);
        versionId = VersionString.fromString(resource.getRequestVersion().toString()).getExactVersion();
    }

    @Override
    public InitializationResult init() {
        if (!Cache.isCached(resource.getLocation(), versionId)) {
            if (resource.forceUpdateRequested()) {
                invalidateExistingEntryInCache(versionId);
            } else {
                return findDownloadUrl();
            }
        }
        return initFromCache(versionId);
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

    private List<URL> getUrlCandidates() {
        final VersionId cachedVersion = getExistingVersionFromCache();
        final List<URL> candidates = new ArrayList<>();

        final DownloadOptions downloadOptions = getDownloadOptions();
        final boolean usePack = downloadOptions.useExplicitPack();
        final boolean useVersion = downloadOptions.useExplicitVersion();

        final URL packAndVersionUrl = getUrl(resource, usePack, useVersion);
        if (packAndVersionUrl != null) {
            candidates.add(packAndVersionUrl);
        }

        final URL versionUrl = getUrl(resource, false, useVersion);
        if (versionUrl != null) {
            candidates.add(versionUrl);
        }

        final URL packUrl = getUrl(resource, usePack, false);
        final URL versionedPackUrl = getVersionedUrl(packUrl, resource.getRequestVersion(), cachedVersion);
        if (versionedPackUrl != null) {
            candidates.add(versionedPackUrl);
        }

        final URL versionedUrl = getVersionedUrl(resource.getLocation(), resource.getRequestVersion(), cachedVersion);
        if (versionedUrl != null) {
            candidates.add(versionedUrl);
        }

        if (candidates.isEmpty()) {
            LOG.error("Failed to find a candidate URL for {}", resource);
        }

        return ResourceUrlCreator.prependHttps(candidates);
    }

    private VersionId getExistingVersionFromCache() {
        return Cache.getAllVersionsInCache(resource.getLocation()).stream()
                .filter(Objects::nonNull)
                .filter(cachedVersion -> cachedVersion.compareTo(versionId) < 0)
                .max(VersionId::compareTo)
                .orElse(null);
    }
}
