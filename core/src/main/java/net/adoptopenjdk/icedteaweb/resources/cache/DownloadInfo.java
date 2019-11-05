package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

public class DownloadInfo {

    private final URL resourceHref;
    private final VersionId version;
    private final long lastModified;
    private final long downloadedAt;

    public DownloadInfo(URL resourceHref, VersionId version, long lastModified) {
        this.resourceHref = resourceHref;
        this.version = version;
        this.lastModified = lastModified;
        this.downloadedAt = System.currentTimeMillis();
    }

    URL getResourceHref() {
        return resourceHref;
    }

    VersionId getVersion() {
        return version;
    }

    long getLastModified() {
        return lastModified;
    }

    long getDownloadedAt() {
        return downloadedAt;
    }
}
