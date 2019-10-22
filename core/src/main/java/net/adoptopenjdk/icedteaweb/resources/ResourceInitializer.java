package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.cache.Resource;

public interface ResourceInitializer {

    static ResourceInitializer of(final Resource resource) {
        final VersionString requestVersion = resource.getRequestVersion();
        if (requestVersion != null) {
            return new VersionedResourceInitializer(resource);
        } else {
            return new UnversionedResourceInitializer(resource);
        }
    }

    InitializationResult init();
}
