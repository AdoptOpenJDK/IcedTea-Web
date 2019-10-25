package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;

import java.net.URL;
import java.util.List;

/**
 * ...
 */
class ExactVersionedResourceInitializer extends VersionedResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ExactVersionedResourceInitializer.class);

    private final VersionId versionId;

    ExactVersionedResourceInitializer(Resource resource) {
        super(resource);
        versionId = VersionId.fromString(resource.getRequestVersion().toString());
    }

    @Override
    public InitializationResult init() {

        if (!Cache.isCached(resource.getLocation(), versionId)) {
            return findDownloadUrl();
        } else {
            return initFromCache(versionId);
        }
    }

    private InitializationResult findDownloadUrl() {
        final List<URL> candidateUrls = getUrlCandidates();
        LOG.debug("Candidate URLs for {}: {}", resource, candidateUrls);
        return getBestUrlByPingingWithHeadRequest(candidateUrls)
                .map(requestResult -> {
                        LOG.debug("Found best URL for {}: {}", resource, requestResult);
                        return new InitializationResult(requestResult);
                })
                .orElseGet(() -> {
                    LOG.debug("Failed to determine best URL for {} will try all of {}", resource, candidateUrls);
                    return new InitializationResult(candidateUrls);
                });
    }
}
