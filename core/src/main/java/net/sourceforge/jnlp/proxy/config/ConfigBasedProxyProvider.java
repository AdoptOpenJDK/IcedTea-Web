package net.sourceforge.jnlp.proxy.config;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.util.config.AbstractConfigBasedProvider;
import net.sourceforge.jnlp.proxy.util.config.ProxyConfiguration;
import net.sourceforge.jnlp.proxy.util.config.ProxyConfigurationImpl;
import net.sourceforge.jnlp.proxy.util.ProxyUtlis;

import java.util.StringTokenizer;

public class ConfigBasedProxyProvider extends AbstractConfigBasedProvider {

    public final static String NAME = "ConfigBasedProxyProvider";

    private final ProxyConfiguration proxyConfiguration;

    public ConfigBasedProxyProvider(final DeploymentConfiguration config) {
        this.proxyConfiguration = createConfiguration(config);
    }

    @Override
    protected ProxyConfiguration getConfig() {
        return proxyConfiguration;
    }

    private static ProxyConfiguration createConfiguration(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
        proxyConfiguration.setBypassLocal(Boolean.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL)));
        proxyConfiguration.setUseHttpForHttpsAndFtp(Boolean.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_SAME)));
        proxyConfiguration.setHttpHost(getHost(config, ConfigurationConstants.KEY_PROXY_HTTP_HOST));
        proxyConfiguration.setHttpPort(getPort(config, ConfigurationConstants.KEY_PROXY_HTTP_PORT));
        proxyConfiguration.setHttpsHost(getHost(config, ConfigurationConstants.KEY_PROXY_HTTPS_HOST));
        proxyConfiguration.setHttpsPort(getPort(config, ConfigurationConstants.KEY_PROXY_HTTPS_PORT));
        proxyConfiguration.setFtpHost(getHost(config, ConfigurationConstants.KEY_PROXY_FTP_HOST));
        proxyConfiguration.setFtpPort(getPort(config, ConfigurationConstants.KEY_PROXY_FTP_PORT));
        proxyConfiguration.setSocksHost(getHost(config, ConfigurationConstants.KEY_PROXY_SOCKS4_HOST));
        proxyConfiguration.setSocksPort(getPort(config, ConfigurationConstants.KEY_PROXY_SOCKS4_PORT));
        final String proxyBypass = config.getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LIST);
        if (proxyBypass != null) {
            final StringTokenizer tokenizer = new StringTokenizer(proxyBypass, ",");
            while (tokenizer.hasMoreTokens()) {
                final String host = tokenizer.nextToken();
                if (host != null && host.trim().length() != 0) {
                    proxyConfiguration.addToBypassList(host);
                }
            }
        }
        return proxyConfiguration;
    }

    private static int getPort(final DeploymentConfiguration config, final String key) {
        return ProxyUtlis.toPort(config.getProperty(key));
    }

    private static String getHost(final DeploymentConfiguration config, final String key) {
        final String proxyHost = config.getProperty(key);
        if (proxyHost != null) {
            return proxyHost.trim();
        }
        return proxyHost;
    }

}
