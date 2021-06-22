package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

/**
 * Information about a resource.
 */
public interface ResourceInfo {
    /**
     * @return the cache key of the resource
     */
    CacheKey getCacheKey();

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
