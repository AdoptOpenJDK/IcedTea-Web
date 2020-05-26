package net.adoptopenjdk.icedteaweb.proxy.direct;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.proxy.ProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class DirectProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DirectProxyProvider.class);
    private static final DirectProxyProvider INSTANCE = new DirectProxyProvider();

    private DirectProxyProvider() {
    }

    @Override
    public List<Proxy> select(final URI uri) {
        LOG.debug("Using NO_PROXY");
        return Collections.singletonList(Proxy.NO_PROXY);
    }

    public static DirectProxyProvider getInstance() {
        return INSTANCE;
    }
}
