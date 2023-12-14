package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URL;
import java.util.Locale;
import java.util.Objects;

public class UrlKey {
    private final URL url;
    private final String protocol;
    private final String host;
    private final String file;
    private final int port;
    private final String ref;

    public UrlKey(final URL url) {
        this.url = url;
        this.protocol = url.getProtocol() != null ? url.getProtocol().toLowerCase(Locale.ENGLISH) : null;
        this.host = url.getHost();
        this.port = url.getPort();
        this.file = url.getFile();
        this.ref = url.getRef();
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof UrlKey)) {
            return false;
        }
        UrlKey other = ((UrlKey) obj);
        return Objects.equals(protocol, other.protocol) &&
                Objects.equals(host, other.host) &&
                samePort(port, other.port) &&
                Objects.equals(file, other.file) &&
                Objects.equals(ref, other.ref);
    }

    private boolean samePort(int port, int other) {
        if (port == other) {
            return true;
        }
        final int defaultPort = getDefaultPort();
        return (port == defaultPort || port == -1) && (other == defaultPort || other == -1);
    }

    private int getDefaultPort() {
        if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        }
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("ftp".equalsIgnoreCase(protocol)) {
            return 21;
        }
        return -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, host, file, ref);
    }
}
