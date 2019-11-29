/* BrowserAwareProxySelector.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/
package net.sourceforge.jnlp.proxy.browser;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.JNLPProxySelector;
import net.sourceforge.jnlp.proxy.pac.PacEvaluator;
import net.sourceforge.jnlp.proxy.pac.PacEvaluatorFactory;
import net.sourceforge.jnlp.proxy.pac.PacUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.sourceforge.jnlp.proxy.ProxyConstants.SOCKET_SCHEMA;
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

/**
 * A ProxySelector which can read proxy settings from a browser's
 * configuration and use that.
 *
 * @see JNLPProxySelector
 */
public class BrowserAwareProxySelector extends JNLPProxySelector {

    private final static Logger LOG = LoggerFactory.getLogger(BrowserAwareProxySelector.class);

    private BrowserProxyType browserProxyType = BrowserProxyType.BROWSER_PROXY_TYPE_NONE;
    private URL browserAutoConfigUrl;
    /** Whether the http proxy should be used for http, https, ftp and socket protocols */
    private Boolean browserUseSameProxy;
    private String browserHttpProxyHost;
    private int browserHttpProxyPort;
    private String browserHttpsProxyHost;
    private int browserHttpsProxyPort;
    private String browserFtpProxyHost;
    private int browserFtpProxyPort;
    private String browserSocks4ProxyHost;
    private int browserSocks4ProxyPort;

    private PacEvaluator browserProxyAutoConfig = null;

    /**
     * Create a new instance of this class, reading configuration from the browser
     */
    public BrowserAwareProxySelector(DeploymentConfiguration config) {
        super(config);

        try {
            initFromBrowserConfig();
        } catch (IOException e) {
            LOG.error("Unable to use Firefox''s proxy settings. Using \"DIRECT\" as proxy type.", e);
            browserProxyType = BrowserProxyType.BROWSER_PROXY_TYPE_NONE;
        }
    }

    /**
     * Initialize configuration by reading preferences from the browser (firefox)
     */
    private void initFromBrowserConfig() throws IOException {

        Map<String, String> prefs = parseBrowserPreferences();

        String type = prefs.get(PROXY_TYPE_PROPERTY_NAME);
        if (type != null) {
            browserProxyType = BrowserProxyType.getForConfigValue(Integer.valueOf(type));
        } else {
            browserProxyType = BrowserProxyType.BROWSER_PROXY_TYPE_AUTO;
        }

        try {
            String url = prefs.get(AUTO_CONFIG_URL_PROPERTY_NAME);
            if (url != null) {
                browserAutoConfigUrl = new URL(url);
            }
        } catch (MalformedURLException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }

        if (browserProxyType == BrowserProxyType.BROWSER_PROXY_TYPE_PAC) {
            if (browserAutoConfigUrl != null) {
                browserProxyAutoConfig = PacEvaluatorFactory.getPacEvaluator(browserAutoConfigUrl);
            }
        }

        browserUseSameProxy = Boolean.valueOf(prefs.get(SHARE_SETTINGS_PROPERTY_NAME));

        browserHttpProxyHost = prefs.get(HTTP_PROPERTY_NAME);
        browserHttpProxyPort = stringToPort(prefs.get(HTTP_PORT_PROPERTY_NAME));
        browserHttpsProxyHost = prefs.get(SSL_PROPERTY_NAME);
        browserHttpsProxyPort = stringToPort(prefs.get(SSL_PORT_PROPERTY_NAME));
        browserFtpProxyHost = prefs.get(FTP_PROPERTY_NAME);
        browserFtpProxyPort = stringToPort(prefs.get(FTP_PORT_PROPERTY_NAME));
        browserSocks4ProxyHost = prefs.get(SOCKS_PROPERTY_NAME);
        browserSocks4ProxyPort = stringToPort(prefs.get(SOCKS_PORT_PROPERTY_NAME));
    }

    public Map<String, String> parseBrowserPreferences() throws IOException {
        File preferencesFile = FirefoxPreferencesFinder.find();
        FirefoxPreferencesParser parser = new FirefoxPreferencesParser(preferencesFile);
        parser.parse();
        return parser.getPreferences();
    }

    /**
     * Returns port inside a string. Unlike {@link Integer#valueOf(String)},
     * it will not throw exceptions.
     *
     * @param string the string containing the integer to parse
     * @return the port inside the string, or Integer.MIN_VALUE
     */
    private int stringToPort(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException nfe) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * <p>
     * The main entry point for {@link BrowserAwareProxySelector}. Based on
     * the browser settings, determines proxy information for a given URI.
     * </p>
     * <p>
     * The appropriate proxy may be determined by reading static information
     * from the browser's preferences file, or it may be computed dynamically,
     * by, for example, running javascript code.
     * </p>
     */
    @Override
    public List<Proxy> getFromBrowser(URI uri) {
        List<Proxy> proxies = new ArrayList<Proxy>();

        String optionDescription = null;

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
                // firefox will do a whole lot of stuff to automagically
                // figure out the right settings. gconf, WPAD, and ENV are used.
                // https://bugzilla.mozilla.org/show_bug.cgi?id=66057#c32
                // TODO this is probably not easy/quick to do. using libproxy might be
                // the simpler workaround
                if (optionDescription == null) {
                    optionDescription = "Automatic";
                }
            case BROWSER_PROXY_TYPE_SYSTEM:
                // means use $http_proxy, $ftp_proxy etc.
                // TODO implement env vars if possible
                if (optionDescription == null) {
                    optionDescription = "System";
                }
            default:
                if (optionDescription == null) {
                    optionDescription = "Unknown";
                }
                LOG.debug("Browser proxy option \"{}\" ({}) not supported yet.", browserProxyType, optionDescription);
                proxies.add(Proxy.NO_PROXY);
        }

        LOG.info("Browser selected proxies: {}", proxies);

        return proxies;
    }

    /**
     * Get an appropriate proxy for a given URI using a PAC specified in the
     * browser.
     */
    private List<Proxy> getFromBrowserPAC(URI uri) {
        if (browserAutoConfigUrl == null || uri.getScheme().equals(SOCKET_SCHEMA)) {
            return Arrays.asList(new Proxy[] { Proxy.NO_PROXY });
        }

        List<Proxy> proxies = new ArrayList<Proxy>();

        try {
            String proxiesString = browserProxyAutoConfig.getProxies(uri.toURL());
            proxies.addAll(PacUtils.getProxiesFromPacResult(proxiesString));
        } catch (MalformedURLException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

    /**
     * Get an appropriate proxy for the given URI using static information from
     * the browser's preferences file.
     */
    private List<Proxy> getFromBrowserConfiguration(URI uri) {
        return getFromArguments(uri, browserUseSameProxy, true,
                browserHttpsProxyHost, browserHttpsProxyPort,
                browserHttpProxyHost, browserHttpProxyPort,
                browserFtpProxyHost, browserFtpProxyPort,
                browserSocks4ProxyHost, browserSocks4ProxyPort);
    }

}
