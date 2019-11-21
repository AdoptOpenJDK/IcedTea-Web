package net.sourceforge.jnlp.proxy.pac;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.ProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoConfigUrlProxyProvider implements ProxyProvider {

    public final static String NAME = "AutoConfigUrlProxyProvider";

    private final PacEvaluator pacEvaluator;

    private final DeploymentConfiguration config;

    public AutoConfigUrlProxyProvider(final DeploymentConfiguration config) {
        this.config = Assert.requireNonNull(config, "config");
        final String autoConfigUrlProperty = config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL);
        if (autoConfigUrlProperty != null) {
            final URL autoConfigUrl = new URL(autoConfigUrlProperty);
            pacEvaluator = PacEvaluatorFactory.getPacEvaluator(autoConfigUrl);
        } else {
            pacEvaluator = null;
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception{
        if (pacEvaluator != null) {
            final String proxiesString = pacEvaluator.getProxies(uri.toURL());
            final List<Proxy> proxies = new ArrayList<>();
            proxies.addAll(PacUtils.getProxiesFromPacResult(proxiesString));
            return Collections.unmodifiableList(proxies);
        } else {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

}
