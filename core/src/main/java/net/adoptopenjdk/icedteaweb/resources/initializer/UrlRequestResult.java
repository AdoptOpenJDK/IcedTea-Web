package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

/**
 * This class stores some result details such as response code, HTTP header field "Location", last modified
 * and content length of a url request.
 */
class UrlRequestResult {

    private final URL url;
    private final int responseCode;
    private final URL location; // HTTP header field "Location" (used for redirection)
    private final VersionId version;
    private final long lastModified;
    private final long contentLength;

    UrlRequestResult(URL url, int responseCode, URL location, VersionId version, long lastModified, long contentLength) {
        if (isRedirectResponseCode(responseCode) && location == null) {
            throw new IllegalStateException("Redirect response code found but location URL is null.");
        }

        this.url = url;
        this.responseCode = responseCode;
        this.location = location;
        this.version = version;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
    }

    URL getUrl() {
        return url;
    }

    URL getLocation() {
        return location;
    }

    VersionId getVersion() {
        return version;
    }

    int getResponseCode() {
        return responseCode;
    }

    long getContentLength() {
        return contentLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return whether this {@link UrlRequestResult} represents a valid redirect with a location
     * and a redirect result code (one of 301-303 or 307-308)
     */
    boolean isRedirect() {
        return isRedirectResponseCode(responseCode);
    }

    /**
     * @return whether this {@link UrlRequestResult} contains a redirect result code (one of 301-303 or 307-308)
     */
    private static boolean isRedirectResponseCode(final int responseCode) {
        return (responseCode == 301
                || responseCode == 302
                || responseCode == 303 /*?*/
                || responseCode == 307
                || responseCode == 308);
    }

    /**
     * @return whether the return code is a success
     */
    public boolean isSuccess() {
        return String.valueOf(responseCode).startsWith("2");
    }

    @Override
    public String toString() {
        return ""
                + "url:" + url + "; "
                + "responseCode:" + responseCode + "; "
                + "location: " + location + "; "
                + "version: " + version + "; "
                + "lastModified: " + lastModified + "; "
                + "contentLength: " + contentLength + "; ";
    }
}
