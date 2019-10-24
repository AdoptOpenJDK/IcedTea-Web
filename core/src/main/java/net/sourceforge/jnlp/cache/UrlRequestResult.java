package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;

/**
 * This class stores some result details such as response code, HTTP header field "Location", last modified
 * and content length of a url request.
 */
public class UrlRequestResult {
    private final int responseCode;
    private final URL location; // HTTP header field "Location" as URL (redirection or newly created resource)
    private final VersionId version;
    private final long lastModified;
    private final long contentLength;

    public UrlRequestResult(int responseCode, URL location, VersionId version, long lastModified, long contentLength) {
        if (isRedirectResponseCode(responseCode) && location == null) {
            throw new IllegalStateException("Redirect response code found but location URL is null.");
        }

        this.responseCode = responseCode;
        this.location = location;
        this.version = version;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
    }

    /**
     * Create a new {@link UrlRequestResult} based on this with the given redirect url.
     *
     * @param location the location
     * @return a new {@link UrlRequestResult} based on this with the given redirect url
     */
    public UrlRequestResult withLocation(final URL location) {
        return new UrlRequestResult(responseCode, location, version, lastModified, contentLength);
    }

    public URL getLocation() {
        return location;
    }

    public VersionId getVersion() {
        return version;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return whether this {@link UrlRequestResult} represents a valid redirect with a location
     * and a redirect result code (one of 301-303 or 307-308)
     */
    public boolean isRedirect() {
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
                + "location: " + (location == null ? "null" : location.toExternalForm()) + "; "
                + "responseCode:" + responseCode + "; "
                + "lastModified: " + lastModified + "; "
                + "contentLength: " + contentLength + "; ";
    }
}
