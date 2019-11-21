package net.sourceforge.jnlp.proxy;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.pac.AutoConfigUrlProxyProvider;
import net.sourceforge.jnlp.proxy.config.DeploymentConfigBasedProxyProvider;
import net.sourceforge.jnlp.proxy.direct.DirectProxyProvider;
import net.sourceforge.jnlp.proxy.browser.FirefoxProxyProvider;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class WebStartProxySelector extends ProxySelector {

    private final ProxyProvider proxyProvider;

    public WebStartProxySelector(final DeploymentConfiguration config) throws Exception {
        Assert.requireNonNull(config, "config");
        proxyProvider = getProvider(config, config.getProperty("proxySelector.name"));
    }

    private ProxyProvider getProvider(final DeploymentConfiguration config, final String providerName) throws Exception {
        if(providerName == "ConfigBasedProxy") {
            return new DeploymentConfigBasedProxyProvider(config);
        } else if(providerName == "PacBasedProxy") {
            return new AutoConfigUrlProxyProvider(config);
        } else if(providerName == "FirefoyBasedProxy") {
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
