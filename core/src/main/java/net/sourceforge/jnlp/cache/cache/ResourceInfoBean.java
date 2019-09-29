package net.sourceforge.jnlp.cache.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

class ResourceInfoBean implements ResourceInfo {

    private final URL location;
    private final VersionId version;
    private long size;
    private long lastModified;
    private long downloadedAt;

    ResourceInfoBean(URL location, VersionId version, long size, long lastModified, long downloadedAt) {
        this.location = location;
        this.version = version;
        this.size = size;
        this.lastModified = lastModified;
        this.downloadedAt = downloadedAt;
    }

    @Override
    public URL getLocation() {
        return location;
    }

    @Override
    public VersionId getVersion() {
        return version;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getDownloadedAt() {
        return downloadedAt;
    }
}
