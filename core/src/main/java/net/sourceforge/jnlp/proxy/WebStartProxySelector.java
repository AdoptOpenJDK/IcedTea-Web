package net.sourceforge.jnlp.proxy;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.config.ConfigBasedAutoConfigUrlProxyProvider;
import net.sourceforge.jnlp.proxy.direct.DirectProxyProvider;
import net.sourceforge.jnlp.proxy.firefox.FirefoxProxyProvider;
import net.sourceforge.jnlp.proxy.ie.WindowsProxyProvider;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebStartProxySelector extends ProxySelector {

    private final ProxyProvider proxyProvider;

    private final AtomicBoolean useDirectAfterError;

    public WebStartProxySelector(final DeploymentConfiguration config) throws Exception {
        this.proxyProvider = createProvider(config);
        this.useDirectAfterError = new AtomicBoolean(false);
    }

    private ProxyProvider createProvider(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        try {
            final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
            final int proxyTypeConfigValue = Integer.valueOf(proxyTypeString);
            final ProxyProviderTypes providerType = ProxyProviderTypes.getForConfigValue(proxyTypeConfigValue);

            providerType.checkSupported();

            if (providerType == ProxyProviderTypes.NONE) {
                return DirectProxyProvider.getInstance();
            }
            if (providerType == ProxyProviderTypes.MANUAL_HOSTS) {
                return new ConfigBasedAutoConfigUrlProxyProvider(config);
            }
            if (providerType == ProxyProviderTypes.MANUAL_PAC_URL) {
                return new ConfigBasedAutoConfigUrlProxyProvider(config);
            }
            if (providerType == ProxyProviderTypes.FIREFOX) {
                return new FirefoxProxyProvider();
            }
            if (providerType == ProxyProviderTypes.WINDOWS) {
                return new WindowsProxyProvider();
            }
            throw new IllegalStateException("Proxy can not be defined");
        } catch (final Exception e) {
            //TODO: DIALOG: Proxy can not be specified. Do you want to continue with default proxy?
            // NO: System.exit
            //YES:
            return DirectProxyProvider.getInstance();
        }
    }

    @Override
    public List<Proxy> select(final URI uri) {
        if(useDirectAfterError.get()) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        try {
            return proxyProvider.select(uri);
        } catch (final Exception e) {
            //TODO: DIALOG: Error in proxy handling for url. Do you want to continue with default proxy?
            // NO: System.exit
            //YES:
            // useDirectAfterError.set(true)
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        //TODO: DIALOG: Looks like gthe configured proxy is not reachable. Do you want to continue with default proxy?
        // NO: System.exit
        //YES:
        // useDirectAfterError.set(true)
    }
}
