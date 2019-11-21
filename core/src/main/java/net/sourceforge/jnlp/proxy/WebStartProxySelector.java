package net.sourceforge.jnlp.proxy;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.browser.FirefoxProxyProvider;
import net.sourceforge.jnlp.proxy.config.DeploymentConfigBasedProxyProvider;
import net.sourceforge.jnlp.proxy.direct.DirectProxyProvider;
import net.sourceforge.jnlp.proxy.pac.AutoConfigUrlProxyProvider;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WebStartProxySelector extends ProxySelector {

    private final ProxyProvider proxyProvider;

    public WebStartProxySelector(final DeploymentConfiguration config) throws Exception {
        Assert.requireNonNull(config, "config");

        proxyProvider = getProvider(config, config.getProperty("proxySelector.name"));
    }

    private ProxyProvider getProvider(final DeploymentConfiguration config, final String providerName) throws Exception {
        if (Objects.equals(providerName, DeploymentConfigBasedProxyProvider.NAME)) {
            return new DeploymentConfigBasedProxyProvider(config);
        } else if (Objects.equals(providerName, AutoConfigUrlProxyProvider.NAME)) {
            return new AutoConfigUrlProxyProvider(config);
        } else if (Objects.equals(providerName, FirefoxProxyProvider.NAME)) {
            return new FirefoxProxyProvider();
        }
        return new DirectProxyProvider();
    }

    @Override
    public List<Proxy> select(final URI uri) {
        try {
            return proxyProvider.select(uri);
        } catch (final Exception e) {
            //TODO
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        //TODO
    }
}
