package net.sourceforge.jnlp.proxy.browser;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.proxy.ProxyProvider;
import net.sourceforge.jnlp.proxy.config.ProxyConfigurationImpl;
import net.sourceforge.jnlp.proxy.pac.PacEvaluator;
import net.sourceforge.jnlp.proxy.pac.PacEvaluatorFactory;
import net.sourceforge.jnlp.proxy.pac.PacUtils;
import net.sourceforge.jnlp.proxy.util.ProxyUtlis;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.AUTO_CONFIG_URL_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.FTP_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.FTP_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.HTTP_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.HTTP_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.PROXY_TYPE_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.SHARE_SETTINGS_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.SOCKS_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.SOCKS_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.SSL_PORT_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.browser.FirefoxConstants.SSL_PROPERTY_NAME;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.SOCKET_SCHEMA;

public class FirefoxProxyProvider implements ProxyProvider {

    public final static String NAME = "FirefoxProxyProvider";

    private final URL browserAutoConfigUrl;

    private final BrowserProxyType browserProxyType;

    private final ProxyConfigurationImpl proxyConfiguration;

    public FirefoxProxyProvider() throws Exception {
        final File preferencesFile = FirefoxPreferencesFinder.find();
        final FirefoxPreferencesParser parser = new FirefoxPreferencesParser(preferencesFile);
        parser.parse();
        final Map<String, String> prefs = parser.getPreferences();

        final String type = prefs.get(PROXY_TYPE_PROPERTY_NAME);
        if (type != null) {
            browserProxyType = BrowserProxyType.getForConfigValue(Integer.valueOf(type));
        } else {
            browserProxyType = BrowserProxyType.BROWSER_PROXY_TYPE_AUTO;
        }

        final String url = prefs.get(AUTO_CONFIG_URL_PROPERTY_NAME);
        if (url != null) {
            browserAutoConfigUrl = new URL(url);
        } else {
            browserAutoConfigUrl = null;
        }

        proxyConfiguration = new ProxyConfigurationImpl();
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
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        List<Proxy> proxies = new ArrayList<>();

        switch (browserProxyType) {
            case BROWSER_PROXY_TYPE_PAC:
                proxies.addAll(getFromBrowserPAC(uri));
                break;
            case BROWSER_PROXY_TYPE_MANUAL:
                proxies.addAll(getFromBrowserConfiguration(uri));
                break;
            case BROWSER_PROXY_TYPE_NONE:
                proxies.add(Proxy.NO_PROXY);
                break;
            case BROWSER_PROXY_TYPE_AUTO:
                throw new IllegalStateException("Not supported");
            case BROWSER_PROXY_TYPE_SYSTEM:
                throw new IllegalStateException("Not supported");
            default:
                proxies.add(Proxy.NO_PROXY);
        }
        return Collections.unmodifiableList(proxies);
    }

    private List<Proxy> getFromBrowserConfiguration(URI uri) {
       return proxyConfiguration.createProxiesForUri(uri);
    }

    private List<Proxy> getFromBrowserPAC(final URI uri) throws MalformedURLException {
        Assert.requireNonNull(uri, "uri");
        if (browserAutoConfigUrl == null || uri.getScheme().equals(SOCKET_SCHEMA)) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        final List<Proxy> proxies = new ArrayList<>();
        final PacEvaluator pacEvaluator = PacEvaluatorFactory.getPacEvaluator(browserAutoConfigUrl);
        final String proxiesString = pacEvaluator.getProxies(uri.toURL());
        proxies.addAll(PacUtils.getProxiesFromPacResult(proxiesString));
        return proxies;
    }

}
