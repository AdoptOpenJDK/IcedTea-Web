package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

/**
 * Information about a resource.
 */
interface ResourceInfo {
    /**
     * @return URL as specified in the JNLP file
     */
    URL getLocation();

    /**
     * @return version-id contained in the version-string of the JNLP file
     */
    VersionId getVersion();

    /**
     * @return the size of the resource in bytes
     */
    long getSize();

    /**
     * @return the last time this resource was modified (in Java millis)
     */
    long getLastModified();

    /**
     * @return the last time the resource was downloaded (in Java millis)
     */
    long getDownloadedAt();
}
