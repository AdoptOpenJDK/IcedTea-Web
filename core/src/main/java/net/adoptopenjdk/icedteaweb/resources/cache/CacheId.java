package net.adoptopenjdk.icedteaweb.resources.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ID for locating resources in the cache either by domain or jnlp-path.
 */
public class CacheId {

    static CacheId domainId(String id) {
        return new CacheId(id, "DOMAIN");
    }

    static CacheId jnlpPathId(String id) {
        return new CacheId(id, "JNLP-PATH");
    }

    //last century array of objects instead of some nice class inherited from previous century
    private final List<CacheFile> files = new ArrayList<>();
    private final String id;
    private final String type;

    private CacheId(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public List<CacheFile> getFiles() {
        return files;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheId cacheId = (CacheId) o;
        return Objects.equals(id, cacheId.id) &&
                type.equals(cacheId.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
