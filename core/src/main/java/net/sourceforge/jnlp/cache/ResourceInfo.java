package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

/**
 * Information about a resource.
 */
interface ResourceInfo {
    /**
     * @return URL as specified in the JNLP file
     */
    URL getLocation();

    /**
     * @return version-id contained in the version-string of the JNLP file
     */
    VersionId getVersion();

    /**
     * @return the size of the resource in bytes
     */
    long getSize();

    /**
     * @return the last time this resource was modified (in Java millis)
     */
    long getLastModified();

    /**
     * @return the last time the resource was downloaded (in Java millis)
     */
    long getDownloadedAt();

    static ResourceInfo createInfoFromRemote(URL location, VersionId version, CloseableConnection connection) {
        return new ResourceInfoImpl(
                location,
                version,
                connection.getContentLength(),
                connection.getLastModified(),
                System.currentTimeMillis()
        );
    }
}

class ResourceInfoImpl implements ResourceInfo {

    private final URL location;
    private final VersionId version;
    private long size;
    private long lastModified;
    private long downloadedAt;

    ResourceInfoImpl(URL location, VersionId version, long size, long lastModified, long downloadedAt) {
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
