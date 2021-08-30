package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.controlpanel.CacheFileInfo;

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

class CacheFileInfoImpl implements CacheFileInfo {

    private final CacheEntry infoFile;

    private final LeastRecentlyUsedCacheEntry entry;

    public CacheFileInfoImpl(final CacheEntry infoFile, final LeastRecentlyUsedCacheEntry entry) {
        this.infoFile = Assert.requireNonNull(infoFile, "infoFile");
        this.entry = Assert.requireNonNull(entry, "entry");
    }

    @Override
    public String getInfoFile() {
        return infoFile.toString();
    }

    @Override
    public File getParentFile() {
        return infoFile.getCacheFile().getParentFile();
    }

    @Override
    public String getProtocol() {
        return entry.getProtocol();
    }

    @Override
    public String getDomain() {
        return entry.getDomain();
    }

    @Override
    public long getSize() {
        return infoFile.getSize();
    }

    @Override
    public Date getLastModified() {
        return new Date(infoFile.getLastModified());
    }

    @Override
    public String getJnlpPath() {
        return infoFile.getJnlpPath();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Consumer<Object> appender = v -> sb.append(Optional.ofNullable(v).orElse("??")).append(" ;  ");
        appender.accept(getInfoFile());
        appender.accept(getParentFile());
        appender.accept(getProtocol());
        appender.accept(getDomain());
        appender.accept(getSize());
        appender.accept(getLastModified());
        appender.accept(getJnlpPath());

        return sb.toString();
    }
}
