package net.sourceforge.jnlp.proxy.config;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.ProxyProvider;
import net.sourceforge.jnlp.proxy.util.ProxyUtlis;

import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;

public class DeploymentConfigBasedProxyProvider implements ProxyProvider {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentConfigBasedProxyProvider.class);

    public final static String NAME = "DeploymentConfigBasedProxyProvider";

    private final DeploymentConfiguration config;

    public DeploymentConfigBasedProxyProvider(final DeploymentConfiguration config) {
        this.config = Assert.requireNonNull(config, "config");
    }

    @Override
    public List<Proxy> select(final URI uri) {
        Assert.requireNonNull(uri, "uri");

        final ProxyConfiguration configuration = getConfiguration();
        Assert.requireNonNull(configuration, "configuration");
        return configuration.createProxiesForUri(uri);
    }

    private ProxyConfiguration getConfiguration() {
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

    private static int getPort(final DeploymentConfiguration config, String key) {
        return ProxyUtlis.toPort(config.getProperty(key));
    }

    private static String getHost(final DeploymentConfiguration config, String key) {
        final String proxyHost = config.getProperty(key);
        if (proxyHost != null) {
            return proxyHost.trim();
        }
        return proxyHost;
    }

}
