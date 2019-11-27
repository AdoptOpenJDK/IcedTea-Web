package net.sourceforge.jnlp.proxy.config;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.util.pac.AbstractPacBasedProvider;
import net.sourceforge.jnlp.proxy.util.pac.PacEvaluator;

import java.net.MalformedURLException;
import java.net.URL;

public class ConfigBasedAutoConfigUrlProxyProvider extends AbstractPacBasedProvider {

    public final static String NAME = "ConfigBasedAutoConfigUrlProxyProvider";

    private final PacEvaluator pacEvaluator;

    public ConfigBasedAutoConfigUrlProxyProvider(final DeploymentConfiguration config) throws Exception {
        pacEvaluator = new PacEvaluator(getAutoConfigUrl(config));
    }

    @Override
    protected PacEvaluator getPacEvaluator() {
        return pacEvaluator;
    }

    private static URL getAutoConfigUrl(final DeploymentConfiguration config) throws MalformedURLException {
        Assert.requireNonNull(config, "config");
        final String autoConfigUrlProperty = config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL);
        return new URL(autoConfigUrlProperty);
    }
}
