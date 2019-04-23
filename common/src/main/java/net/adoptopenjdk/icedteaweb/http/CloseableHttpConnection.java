package net.adoptopenjdk.icedteaweb.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * {@link Closeable} wrapper around a {@link HttpURLConnection}.
 *
 * Only the needed methods of HttpURLConnection are exposed.
 *
 * Closing the connection will trigger a disconnect on the underlying HttpUrlConnection
 */
public class CloseableHttpConnection extends CloseableConnection {
    private final HttpURLConnection delegate;

    CloseableHttpConnection(final HttpURLConnection delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    /**
     * triggers {@link HttpURLConnection#disconnect()}.
     */
    @Override
    public void close() {
        delegate.disconnect();
    }

    /**
     * delegates to {@link HttpURLConnection#getResponseCode()}
     */
    public int getResponseCode() throws IOException {
        return delegate.getResponseCode();
    }
}
