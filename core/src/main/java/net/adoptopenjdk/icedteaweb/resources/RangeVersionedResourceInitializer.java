package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.cache.Resource;

/**
 * ...
 */
class RangeVersionedResourceInitializer extends VersionedResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(RangeVersionedResourceInitializer.class);

    RangeVersionedResourceInitializer(Resource resource) {
        super(resource);
    }

    @Override
    public InitializationResult init() {
        throw new RuntimeException("not implemented yet!");
    }


}
