package net.adoptopenjdk.icedteaweb.proxy.linux;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.config.ConfigBasedProvider;
import net.adoptopenjdk.icedteaweb.proxy.config.ProxyConfigurationImpl;
import net.adoptopenjdk.icedteaweb.proxy.direct.DirectProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.pac.PacBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.pac.PacProxyCache;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static net.adoptopenjdk.icedteaweb.proxy.linux.GnomeProxyConfigReader.readGnomeProxyConfig;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static net.adoptopenjdk.icedteaweb.proxy.linux.SystemPropertiesProxyConfigReader.readSystemPropertiesProxyConfig;

public class LinuxProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxProxyProvider.class);

    private static final LinuxProxySettings NO_PROXY_CONFIG;

    static {
        NO_PROXY_CONFIG = new LinuxProxySettings();
        NO_PROXY_CONFIG.setMode(NO_PROXY);
    }

    private final ProxyProvider internalProvider;


    public LinuxProxyProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws IOException {

        final LinuxProxySettings proxySettings = readGnomeProxyConfig(ioExecutor)
                .orElseGet(() -> readSystemPropertiesProxyConfig()
                        .orElse(NO_PROXY_CONFIG)
                );

        final LinuxProxyMode mode = proxySettings.getMode();
        switch (mode) {
            case NO_PROXY:
                internalProvider = DirectProxyProvider.getInstance();
                break;
            case PAC:
                internalProvider = new PacBasedProxyProvider(proxySettings.getAutoConfigUrl(), PacProxyCache.createFor(config));
                break;
            case MANUAL:
                if (proxySettings.isAuthenticationEnabled()) {
                    LOG.warn("Linux system proxy property 'httpUser' is not supported.");
                }
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
                proxyConfiguration.setBypassLocal(proxySettings.isLocalhostExcluded());
                internalProvider = new ConfigBasedProvider(proxyConfiguration);
            default:
                throw new IllegalArgumentException("unknown linux proxy mode: " + mode);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }

    enum LinuxProxyMode {
        NO_PROXY, PAC, MANUAL
    }
}
