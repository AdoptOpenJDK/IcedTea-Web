package net.adoptopenjdk.icedteaweb.proxy.mac;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.config.ConfigBasedProvider;
import net.adoptopenjdk.icedteaweb.proxy.config.ProxyConfigurationImpl;
import net.adoptopenjdk.icedteaweb.proxy.pac.PacBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.pac.PacProxyCache;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MacProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MacProxyProvider.class);

    private static final Set<String> LOCALHOST_INDICATORS = new HashSet<>(Arrays.asList("localhost", "*.local"));

    private final ProxyProvider internalProvider;

    public MacProxyProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {

        final MacProxySettings proxySettings = ScutilUtil.executeScutil(ioExecutor);

        if (proxySettings.isAutoDiscoveryEnabled()) {
            LOG.warn("Mac system proxy property 'autoDiscovery' is not supported.");
        }
        if (proxySettings.isExcludeSimpleHostnames()) {
            LOG.warn("Mac system proxy property 'excludeSimpleHostnames' is not supported.");
        }
        if (proxySettings.isFtpPassive()) {
            LOG.warn("Mac system proxy property 'ftpPassive' is not supported.");
        }
        if (proxySettings.getHttpUser() != null) {
            LOG.warn("Mac system proxy property 'httpUser' is not supported.");
        }
        if (proxySettings.getHttpsUser() != null) {
            LOG.warn("Mac system proxy property 'httpsUser' is not supported.");
        }
        if (proxySettings.getFtpUser() != null) {
            LOG.warn("Mac system proxy property 'ftpUser' is not supported.");
        }
        if (proxySettings.getSocksUser() != null) {
            LOG.warn("Mac system proxy property 'socksUser' is not supported.");
        }

        if (proxySettings.isAutoConfigEnabled()) {
            internalProvider = new PacBasedProxyProvider(proxySettings.getAutoConfigUrl(), PacProxyCache.createFor(config));
        } else {
            final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
            if (proxySettings.isHttpEnabled()) {
                proxyConfiguration.setHttpHost(proxySettings.getHttpHost());
                proxyConfiguration.setHttpPort(proxySettings.getHttpPort());
            }
            if (proxySettings.isHttpsEnabled()) {
                proxyConfiguration.setHttpsHost(proxySettings.getHttpsHost());
                proxyConfiguration.setHttpsPort(proxySettings.getHttpsPort());
            }
            if (proxySettings.isFtpEnabled()) {
                proxyConfiguration.setFtpHost(proxySettings.getFtpHost());
                proxyConfiguration.setFtpPort(proxySettings.getFtpPort());
            }
            if (proxySettings.isSocksEnabled()) {
                proxyConfiguration.setSocksHost(proxySettings.getSocksHost());
                proxyConfiguration.setSocksPort(proxySettings.getSocksPort());
            }
            proxySettings.getExceptionList().forEach(proxyConfiguration::addToBypassList);
            proxyConfiguration.setBypassLocal(bypassLocalhost(proxySettings));
            internalProvider = new ConfigBasedProvider(proxyConfiguration);
        }
    }

    private boolean bypassLocalhost(MacProxySettings proxySettings) {
        return proxySettings.getExceptionList().stream().anyMatch(LOCALHOST_INDICATORS::contains);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
