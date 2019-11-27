package net.sourceforge.jnlp.proxy.util.config;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.proxy.util.ProxyUtlis;

import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.sourceforge.jnlp.proxy.util.ProxyConstants.FTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.HTTPS_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.HTTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.SOCKET_SCHEMA;

public interface ProxyConfiguration {

    String getHttpHost();

    int getHttpPort();

    default Optional<SocketAddress> getHttpAddress() {
        return ProxyUtlis.getAddress(getHttpHost(), getHttpPort());
    }

    String getHttpsHost();

    int getHttpsPort();

    default Optional<SocketAddress> getHttpsAddress() {
        return ProxyUtlis.getAddress(getHttpsHost(), getHttpsPort());
    }

    String getFtpHost();

    int getFtpPort();

    default Optional<SocketAddress> getFtpAddress() {
        return ProxyUtlis.getAddress(getFtpHost(), getFtpPort());
    }

    String getSocksHost();

    int getSocksPort();

    default Optional<SocketAddress> getSocksAddress() {
        return ProxyUtlis.getAddress(getSocksHost(), getSocksPort());
    }

    List<String> getBypassList();

    boolean isBypassLocal();

    boolean isUseHttpForHttpsAndFtp();

    boolean isUseHttpForSocks();

    default List<Proxy> createProxiesForUri(final URI uri) {
        Assert.requireNonNull(uri, "uri");

        final List<Proxy> proxies = new ArrayList<>();
        final String scheme = uri.getScheme();

        if (isUseHttpForHttpsAndFtp()) {
            getHttpAddress().ifPresent(httpAddress -> {
                if ((scheme.equals(HTTPS_SCHEMA) || scheme.equals(HTTP_SCHEMA) || scheme.equals(FTP_SCHEMA))) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, httpAddress);
                    proxies.add(proxy);
                }
                if (scheme.equals(SOCKET_SCHEMA) && isUseHttpForSocks()) {
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, httpAddress);
                    proxies.add(proxy);
                } else {
                    getSocksAddress().ifPresent(socksAddress -> proxies.add(new Proxy(Proxy.Type.SOCKS, socksAddress)));
                }
            });
        } else if (scheme.equals(HTTP_SCHEMA)) {
            getHttpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(HTTPS_SCHEMA)) {
            getHttpsAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(FTP_SCHEMA)) {
            getFtpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        }

        if (proxies.isEmpty()) {
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

}
