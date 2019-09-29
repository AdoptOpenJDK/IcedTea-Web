package net.sourceforge.jnlp.cache.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ID for locating resources in the cache either by domain or jnlp-path.
 */
public abstract class CacheId {

    //last century array of objects instead of some nice class inherited from previous century
    protected final List<Object[]> files = new ArrayList<>();

    abstract void populate();

    public abstract String getType();

    protected final String id;

    CacheId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public List<Object[]> getFiles() {
        return files;
    }

    public String getId() {
        return id;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CacheId) {
            CacheId c = (CacheId) obj;
            if (c.id == null && this.id == null) {
                return true;
            }
            if (c.id == null) {
                return false;
            }
            return c.id.equals(this.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    static class CacheJnlpId extends CacheId {

        CacheJnlpId(String id) {
            super(id);
        }

        @Override
        public void populate() {
            ArrayList<Object[]> all = CacheDirectory.generateData();
            for (Object[] object : all) {
                if (id.equals(object[6])) {
                    this.files.add(object);
                }
            }
        }

        @Override
        public String getType() {
            return "JNLP-PATH";
        }

        @Override
        //hashcode in super is ok
        public boolean equals(Object obj) {
            if (obj instanceof CacheJnlpId) {
                return super.equals(obj);
            } else {
                return false;
            }
        }

    }

    static class CacheDomainId extends CacheId {

        CacheDomainId(String id) {
            super(id);
        }

        @Override
        public void populate() {
            ArrayList<Object[]> all = CacheDirectory.generateData();
            for (Object[] object : all) {
                if (id.equals(object[3].toString())) {
                    this.files.add(object);
                }
            }
        }

        @Override
        public String getType() {
            return "DOMAIN";
        }

        @Override
        //hashcode in super is ok
        public boolean equals(Object obj) {
            if (obj instanceof CacheDomainId) {
                return super.equals(obj);
            } else {
                return false;
            }
        }

    }
}
