package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

public interface ResourceInitializer {

    static ResourceInitializer of(final Resource resource) {
        if (JNLPRuntime.isOfflineForced()) {
            return new OfflineResourceInitializer(resource);
        }

        final VersionString requestVersion = resource.getRequestVersion();
        if (requestVersion == null) {
            return new UnversionedResourceInitializer(resource);
        }

        if (requestVersion.isExactVersion()) {
            return new ExactVersionedResourceInitializer(resource);
        } else {
            return new RangeVersionedResourceInitializer(resource);
        }
    }

    InitializationResult init();
}
