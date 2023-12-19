package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.sourceforge.jnlp.util.UrlKey;

import java.net.URL;
import java.util.Objects;

import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;

class CacheKey {

    private final URL location;
    private final UrlKey urlKey;
    private final VersionId version;

    public CacheKey(final URL location, final VersionId version) {
        this.location = requireNonNull(location, "location");
        this.urlKey = new UrlKey(location);
        this.version = version;
    }

    public URL getLocation() {
        return location;
    }

    public VersionId getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "CacheKey{" +
                "location=" + location +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(urlKey, cacheKey.urlKey) && Objects.equals(version, cacheKey.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlKey, version);
    }

    public boolean matches(URL resource) {
        return resource != null && urlKey.equals(new UrlKey(resource));
    }
}
