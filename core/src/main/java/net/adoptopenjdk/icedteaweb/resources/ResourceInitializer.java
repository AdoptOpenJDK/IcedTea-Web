package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.util.EnumSet;

import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;

public interface ResourceInitializer {

    static ResourceInitializer of(final Resource resource) {
        if (JNLPRuntime.isOfflineForced()) {
            return new OfflineResourceInitializer(resource);
        }

        final VersionString requestVersion = resource.getRequestVersion();
        if (requestVersion != null) {
            return new VersionedResourceInitializer(resource);
        } else {
            return new UnversionedResourceInitializer(resource);
        }
    }

    InitializationResult init();
}
