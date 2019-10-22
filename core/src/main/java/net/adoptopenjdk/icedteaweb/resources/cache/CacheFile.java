package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.File;
import java.util.Date;

public class CacheFile {

    private final CacheEntry infoFile;

    private final LeastRecentlyUsedCacheEntry entry;

    public CacheFile(final CacheEntry infoFile, final LeastRecentlyUsedCacheEntry entry) {
        this.infoFile = Assert.requireNonNull(infoFile, "infoFile");
        this.entry = Assert.requireNonNull(entry, "entry");
    }

    public CacheEntry getInfoFile() {
        return infoFile;
    }

    public File getParentFile() {
        return infoFile.getCacheFile().getParentFile();
    }

    public String getProtocol() {
        return entry.getProtocol();
    }

    public String getDomain() {
        return entry.getDomain();
    }

    public long getSize() {
        return infoFile.getSize();
    }

    public Date getLastModified() {
        return new Date(infoFile.getLastModified());
    }

    public String getJnlpPath() {
        return infoFile.getJnlpPath();
    }

    public LeastRecentlyUsedCacheEntry getEntry() {
        return entry;
    }
}
