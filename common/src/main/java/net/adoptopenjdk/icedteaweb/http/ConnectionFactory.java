package net.adoptopenjdk.icedteaweb.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;
import static net.adoptopenjdk.icedteaweb.http.HttpMethod.GET;

/**
 * Factory for opening connections to URLs.
 */
public class ConnectionFactory {

    private ConnectionFactory() {
        // do not instantiate.
    }

    /**
     * Opens a connection to an URL.
     *
     * @param url the url to which to open a connection.
     * @return the established connection.
     * @throws IOException if an I/O exception occurs.
     */
    public static CloseableConnection openConnection(final URL url) throws IOException {
        return openConnection(url, GET, emptyMap());
    }

    /**
     * Opens a connection to a URL.
     *
     * If the URL has HTTP or HTTPS as its protocol than {@link HttpURLConnection} is opened
     * and the {@code requestMethod} and {@code requestProperties} are set onto the connection.
     * Otherwise the {@code requestMethod} and {@code requestProperties} are ignored.
     *
     * @param url the url to which to open a connection. Must have protocol HTTP or HTTPS
     * @param requestMethod the HTTP method to use for the connection.
     * @param requestProperties properties to set on the connection.
     * @return the established connection.
     * @throws IOException if an I/O exception occurs.
     */
    public static CloseableConnection openConnection(
            final URL url,
            final HttpMethod requestMethod,
            final Map<String, String> requestProperties
    ) throws IOException
    {
        requireNonNull(url, "url");
        requireNonNull(requestMethod, "requestMethod");
        requireNonNull(requestProperties, "requestProperties");

        final URLConnection connection = url.openConnection();

        if (connection instanceof HttpURLConnection) {
            return createHttpConnection((HttpURLConnection) connection, requestMethod, requestProperties);
        }

        return new CloseableConnection(connection);
    }

    /**
     * Opens a connection to a URL.
     *
     * If the URL has not HTTP or HTTPS as its protocol a {@link IllegalArgumentException} is thrown.
     * Otherwise the {@code requestMethod} and {@code requestProperties} are set onto the connection.
     *
     * @param url the url to which to open a connection. Must have protocol HTTP or HTTPS
     * @param requestMethod the HTTP method to use for the connection.
     * @param requestProperties properties to set on the connection.
     * @return the established connection.
     * @throws IOException if an I/O exception occurs.
     */
    public static CloseableHttpConnection openHttpConnection(
            final URL url,
            final HttpMethod requestMethod,
            final Map<String, String> requestProperties
    ) throws IOException
    {
        requireNonNull(url, "url");
        requireNonNull(requestMethod, "requestMethod");
        requireNonNull(requestProperties, "requestProperties");

        final URLConnection connection = url.openConnection();

        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("onle HTTP and HTTPS urls are supported: " + url.toExternalForm());
        }
        return createHttpConnection((HttpURLConnection) connection, requestMethod, requestProperties);
    }

    /**
     * Create a HTTP or HTTPS connection
     * The {@code requestMethod} and {@code requestProperties} are set onto the connection.
     *
     * @param httpConnection the connection to wrap.
     * @param requestMethod the HTTP method to use for the connection.
     * @param requestProperties properties to set on the connection.
     * @return the established connection.
     * @throws IOException if an I/O exception occurs.
     */
    private static CloseableHttpConnection createHttpConnection(
            final HttpURLConnection httpConnection,
            final HttpMethod requestMethod,
            final Map<String, String> requestProperties
    ) throws IOException
    {

        httpConnection.setRequestMethod(requestMethod.name());

        for (final Map.Entry<String, String> property : requestProperties.entrySet()) {
            httpConnection.addRequestProperty(property.getKey(), property.getValue());
        }

        return new CloseableHttpConnection(httpConnection);
    }
}
