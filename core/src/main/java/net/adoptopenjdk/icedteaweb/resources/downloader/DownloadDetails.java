package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.InputStream;
import java.net.URL;

/**
 * ...
 */
class DownloadDetails {
    final URL downloadFrom;
    final InputStream inputStream;
    final String contentEncoding;
    final String version;
    final long lastModified;

    DownloadDetails(URL downloadFrom, InputStream inputStream, String contentEncoding, String version, long lastModified) {
        this.downloadFrom = downloadFrom;
        this.inputStream = inputStream;
        this.contentEncoding = contentEncoding;
        this.version = version;
        this.lastModified = lastModified;
    }
}
