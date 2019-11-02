package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.DownloadOptions;

import java.net.URL;

/**
 * Factory to allow creating {@link Resource Resources} outside of its package for testing purposes.
 */
public class ResourceFactory {
    public static Resource createResource(final URL location, final VersionString requestVersion, final DownloadOptions downloadOptions, final UpdatePolicy updatePolicy) {
        return Resource.createResource(location, requestVersion, downloadOptions, updatePolicy);
    }
}
