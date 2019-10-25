package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

public interface ResourceInitializer {

    static ResourceInitializer of(final Resource resource) {
        if (JNLPRuntime.isOfflineForced()) {
            return new OfflineResourceInitializer(resource);
        }

        final VersionString requestVersion = resource.getRequestVersion();
        if (requestVersion != null) {
            if (requestVersion.isExactVersion()) {
                return new ExactVersionedResourceInitializer(resource);
            } else {
                return new RangeVersionedResourceInitializer(resource);
            }
        } else {
            return new UnversionedResourceInitializer(resource);
        }
    }

    InitializationResult init();
}
