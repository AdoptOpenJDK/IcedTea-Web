package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

public class DownloadInfo {

    private final CacheKey key;

    private final long lastModified;
    private final long downloadedAt;

    public DownloadInfo(URL resourceHref, VersionId version, long lastModified) {
        this.key = new CacheKey(resourceHref, version);

        this.lastModified = lastModified;
        this.downloadedAt = System.currentTimeMillis();
    }

    CacheKey getCacheKey() {
        return key;
    }

    long getLastModified() {
        return lastModified;
    }

    long getDownloadedAt() {
        return downloadedAt;
    }
}
