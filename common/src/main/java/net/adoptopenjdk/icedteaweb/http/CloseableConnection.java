package net.adoptopenjdk.icedteaweb.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

/**
 * {@link Closeable} wrapper around a {@link URLConnection}.
 *
 * Only the needed methods of URLConnection are exposed.
 *
 * Additionally {@link #getResponseCode()} from {@link HttpURLConnection} is available.
 */
public class CloseableConnection implements Closeable {
    private final URLConnection delegate;

    CloseableConnection(final URLConnection delegate) {
        this.delegate = delegate;
    }

    /**
     * does nothing.
     * Subclasses may change this behavior
     */
    @Override
    public void close() {
        // do nothing
    }

    /**
     * always returns {@link HttpURLConnection#HTTP_OK}.
     * Subclasses may change this behavior
     */
    public int getResponseCode() throws IOException {
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * delegates to {@link URLConnection#getURL()}
     */
    public URL getURL() {
        return delegate.getURL();
    }

    /**
     * delegates to {@link URLConnection#getInputStream()}
     */
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    /**
     * delegates to {@link URLConnection#getPermission()}
     */
    public Permission getPermission() throws IOException {
        return delegate.getPermission();
    }

    /**
     * delegates to {@link URLConnection#getHeaderFields()}
     */
    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    /**
     * delegates to {@link URLConnection#getHeaderField(String)}
     */
    public String getHeaderField(final String name) {
        return delegate.getHeaderField(name);
    }

    /**
     * @return HTTP header field "Location" as URL
     * @throws MalformedURLException if the location string does not represent a valid URL
     */
    public URL getLocationHeaderFieldUrl() throws MalformedURLException {
        final String location = getHeaderField("Location");
        return isBlank(location) ? null : new URL(location);
    }

    /**
     * delegates to {@link URLConnection#getLastModified()}
     */
    public long getLastModified() {
        return delegate.getLastModified();
    }

    /**
     * delegates to {@link URLConnection#getContentLengthLong()}
     */
    public long getContentLength() {
        return delegate.getContentLengthLong();
    }

    public void setConnectTimeout(int timeout) {
        delegate.setConnectTimeout(timeout);
    }
}
