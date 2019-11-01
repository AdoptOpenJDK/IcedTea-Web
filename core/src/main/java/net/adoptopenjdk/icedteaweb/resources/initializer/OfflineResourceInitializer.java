package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;

import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;

/**
 * Class to initialize resources in case the application runs in offline mode.
 */
class OfflineResourceInitializer extends BaseResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(OfflineResourceInitializer.class);

    OfflineResourceInitializer(Resource resource) {
        super(resource);
    }

    @Override
    public InitializationResult init() {
        if (Cache.isAnyCached(resource.getLocation(), resource.getRequestVersion())) {
            final VersionId version = Cache.getBestMatchingVersionInCache(resource.getLocation(), resource.getRequestVersion());
            return initFromCache(version);
        } else {
            synchronized (resource) {
                resource.setStatus(ERROR);
                resource.notifyAll();
                LOG.warn("Resource '{}' not found in cache. Continuing but you may experience errors", resource.getLocation());
            }
            return new InitializationResult();
        }

    }
}
