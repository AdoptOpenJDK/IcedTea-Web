package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URL;
import java.util.Locale;
import java.util.Objects;

public class UrlKey {
 private String protocol;
 private String host;
 private String file;
 private int port;
 private String ref;

    public UrlKey(final URL url) {
        this.protocol = url.getProtocol() != null ? url.getProtocol().toLowerCase(Locale.ENGLISH) : null;
        this.host = url.getHost();
        this.port = url.getPort();
        this.file = url.getFile();
        this.ref = url.getRef();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof UrlKey)) {
            return false;
        }
        UrlKey other = ((UrlKey) obj);
        return  Objects.equals(protocol, other.protocol) &&
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
        return  (port == defaultPort || port == -1) && (other == defaultPort || other == -1);
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
