package net.sourceforge.jnlp.proxy.firefox;

import net.sourceforge.jnlp.proxy.ProxyProvider;
import net.sourceforge.jnlp.proxy.direct.DirectProxyProvider;
import net.sourceforge.jnlp.proxy.util.ProxyUtlis;
import net.sourceforge.jnlp.proxy.util.config.ProxyConfigurationImpl;
import net.sourceforge.jnlp.proxy.util.config.SimpleConfigBasedProvider;
import net.sourceforge.jnlp.proxy.util.pac.PacEvaluator;
import net.sourceforge.jnlp.proxy.util.pac.SimplePacBasedProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.AUTO_CONFIG_URL_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.FTP_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.FTP_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.HTTP_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.HTTP_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.PROXY_TYPE_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.SHARE_SETTINGS_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.SOCKS_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.SOCKS_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.SSL_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.firefox.FirefoxConstants.SSL_PROPERTY_NAME;

public class FirefoxProxyProvider implements ProxyProvider {

    public final static String NAME = "FirefoxProxyProvider";

    private final ProxyProvider internalProvider;

    public FirefoxProxyProvider() throws Exception {
        final File preferencesFile = FirefoxPreferencesFinder.find();
        final Map<String, String> prefs = FirefoxPreferencesParser.parse(preferencesFile);
        final String type = prefs.get(PROXY_TYPE_PROPERTY_NAME);
        if (type != null) {
            final FirefoxProxyType browserProxyType = FirefoxProxyType.getForConfigValue(Integer.valueOf(type));
            if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_PAC) {
                internalProvider = createForPac(prefs);
            } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_MANUAL) {
                internalProvider = createForManualConfig(prefs);
            } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_NONE) {
                internalProvider = DirectProxyProvider.getInstance();
            } else {
                throw new IllegalStateException("Firefox Proxy Type '" + browserProxyType +"' is not supported");
            }
        } else {
            //TODO: is this an error or can there be no specification in firefox settings? - Against BROWSER_PROXY_TYPE_NONE
            internalProvider = DirectProxyProvider.getInstance();
        }
    }

    private ProxyProvider createForManualConfig(final Map<String, String> prefs) {
        final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
        proxyConfiguration.setUseHttpForHttpsAndFtp(Boolean.valueOf(prefs.get(SHARE_SETTINGS_PROPERTY_NAME)));
        proxyConfiguration.setUseHttpForSocks(true);
        proxyConfiguration.setHttpHost(prefs.get(HTTP_PROPERTY_NAME));
        proxyConfiguration.setHttpPort(ProxyUtlis.toPort(prefs.get(HTTP_PORT_PROPERTY_NAME)));
        proxyConfiguration.setHttpsHost(prefs.get(SSL_PROPERTY_NAME));
        proxyConfiguration.setHttpsPort(ProxyUtlis.toPort(prefs.get(SSL_PORT_PROPERTY_NAME)));
        proxyConfiguration.setFtpHost(prefs.get(FTP_PROPERTY_NAME));
        proxyConfiguration.setFtpPort(ProxyUtlis.toPort(prefs.get(FTP_PORT_PROPERTY_NAME)));
        proxyConfiguration.setSocksHost(prefs.get(SOCKS_PROPERTY_NAME));
        proxyConfiguration.setSocksPort(ProxyUtlis.toPort(prefs.get(SOCKS_PORT_PROPERTY_NAME)));
        return new SimpleConfigBasedProvider(proxyConfiguration);
    }

    private ProxyProvider createForPac(final Map<String, String> prefs) throws MalformedURLException {
        final String url = prefs.get(AUTO_CONFIG_URL_PROPERTY_NAME);
        final URL autoConfigUrl = new URL(prefs.get(AUTO_CONFIG_URL_PROPERTY_NAME));
        final PacEvaluator evaluator = new PacEvaluator(autoConfigUrl);
        return new SimplePacBasedProvider(evaluator);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }

}
