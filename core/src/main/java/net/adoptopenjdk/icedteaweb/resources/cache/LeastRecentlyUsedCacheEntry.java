package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.util.Objects;

/**
 * ...
 */
class LeastRecentlyUsedCacheEntry implements Comparable<LeastRecentlyUsedCacheEntry> {
    private final String id;
    private final long lastAccessed;
    private final boolean markedForDeletion;

    private final URL resourceHref;
    private final VersionId version;

    LeastRecentlyUsedCacheEntry(String id, long lastAccessed, URL resourceHref, VersionId version) {
        this.id = id;
        this.lastAccessed = lastAccessed;
        this.markedForDeletion = false;
        this.resourceHref = resourceHref;
        this.version = version;
    }

    LeastRecentlyUsedCacheEntry(String id, URL resourceHref, VersionId version) {
        this.id = id;
        this.lastAccessed = 0;
        this.markedForDeletion = true;
        this.resourceHref = resourceHref;
        this.version = version;
    }

    String getId() {
        return id;
    }

    long getLastAccessed() {
        return lastAccessed;
    }

    URL getResourceHref() {
        return resourceHref;
    }

    VersionId getVersion() {
        return version;
    }

    String getProtocol() {
        return resourceHref.getProtocol();
    }

    String getDomain() {
        return resourceHref.getHost();
    }

    boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    boolean matches(URL resource, VersionString versionString) {
        if (this.resourceHref.equals(resource)) {
            if (versionString == null && version == null) {
                return true;
            }
            if (versionString != null && version != null) {
                return versionString.contains(version);
            }
        }
        return false;
    }

    @Override
    public int compareTo(LeastRecentlyUsedCacheEntry o) {
        return Long.compare(this.lastAccessed, o.lastAccessed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeastRecentlyUsedCacheEntry entry = (LeastRecentlyUsedCacheEntry) o;
        return resourceHref.equals(entry.resourceHref) &&
                Objects.equals(version, entry.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceHref, version);
    }
}
