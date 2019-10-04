package net.sourceforge.jnlp.cache.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

class ResourceInfoBean implements ResourceInfo {

    private final URL resourceHref;
    private final VersionId version;
    private final long size;
    private final long lastModified;
    private final long downloadedAt;

    ResourceInfoBean(URL resourceHref, VersionId version, long size, long lastModified, long downloadedAt) {
        this.resourceHref = resourceHref;
        this.version = version;
        this.size = size;
        this.lastModified = lastModified;
        this.downloadedAt = downloadedAt;
    }

    @Override
    public URL getResourceHref() {
        return resourceHref;
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
