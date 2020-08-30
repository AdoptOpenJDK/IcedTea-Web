package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.net.URL;

/**
 * ...
 */
class DownloadDetails {
    final URL downloadFrom;
    final NotifyingInputStream inputStream;
    final String contentType;
    final String contentEncoding;
    final String version;
    final long lastModified;
    final long totalSize;

    DownloadDetails(URL downloadFrom, NotifyingInputStream inputStream, String contentType, String contentEncoding, String version, long lastModified, long totalSize) {
        this.downloadFrom = downloadFrom;
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.version = version;
        this.lastModified = lastModified;
        this.totalSize = totalSize;
    }
}
