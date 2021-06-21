package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.client.controlpanel.CacheFileInfo;
import net.adoptopenjdk.icedteaweb.client.controlpanel.CacheIdInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ID for locating resources in the cache either by domain or jnlp-path.
 */
class CacheIdInfoImpl implements CacheIdInfo {

    static CacheIdInfoImpl domainId(String id) {
        return new CacheIdInfoImpl(id, CacheIdType.DOMAIN);
    }

    static CacheIdInfoImpl jnlpPathId(String id) {
        return new CacheIdInfoImpl(id, CacheIdType.JNLP_PATH);
    }

    //last century array of objects instead of some nice class inherited from previous century
    private final List<CacheFileInfo> files = new ArrayList<>();
    private final String id;
    private final CacheIdType type;

    private CacheIdInfoImpl(String id, CacheIdType type) {
        this.id = id;
        this.type = type;
    }

    void addFileInfo(CacheFileInfo file) {
        files.add(file);
    }

    @Override
    public List<CacheFileInfo> getFileInfos() {
        return files;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CacheIdType getType() {
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
        CacheIdInfoImpl cacheId = (CacheIdInfoImpl) o;
        return Objects.equals(id, cacheId.id) && type == cacheId.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
