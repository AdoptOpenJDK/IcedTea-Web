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
package net.sourceforge.jnlp.browser;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sourceforge.jnlp.runtime.JNLPProxySelector;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.PacEvaluator;
import net.sourceforge.jnlp.runtime.PacEvaluatorFactory;

/**
 * A ProxySelector which can read proxy settings from a browser's
 * configuration and use that.
 *
 * @see JNLPProxySelector
 */
public class BrowserAwareProxySelector extends JNLPProxySelector {

    /* firefox's constants */
    public static final int BROWSER_PROXY_TYPE_NONE = 0;
    public static final int BROWSER_PROXY_TYPE_MANUAL = 1;
    public static final int BROWSER_PROXY_TYPE_PAC = 2;
    public static final int BROWSER_PROXY_TYPE_NONE2 = 3;
    /** use gconf, WPAD and then env (and possibly others)*/
    public static final int BROWSER_PROXY_TYPE_AUTO = 4;
    /** use env variables */
    public static final int BROWSER_PROXY_TYPE_SYSTEM = 5;

    private int browserProxyType = BROWSER_PROXY_TYPE_NONE;
    private URL browserAutoConfigUrl;
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
     * Create a new instance of this class, reading configuration fropm the browser
     */
    public BrowserAwareProxySelector() {
        super();
        try {
            initFromBrowserConfig();
        } catch (IOException e) {
            if (JNLPRuntime.isDebug()) {
                e.printStackTrace();
            }
            System.err.println(R("RProxyFirefoxNotFound"));
            browserProxyType = PROXY_TYPE_NONE;
        }
    }

    /**
     * Initialize configuration by reading preferences from the browser (firefox)
     */
    private void initFromBrowserConfig() throws IOException {

        File preferencesFile = FirefoxPreferencesFinder.find();

        FirefoxPreferencesParser parser = new FirefoxPreferencesParser(preferencesFile);
        parser.parse();
        Map<String, String> prefs = parser.getPreferences();

        String type = prefs.get("network.proxy.type");
        if (type != null) {
            browserProxyType = Integer.valueOf(type);
        } else {
            browserProxyType = BROWSER_PROXY_TYPE_AUTO;
        }

        try {
            String url = prefs.get("network.proxy.autoconfig_url");
            if (url != null) {
                browserAutoConfigUrl = new URL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (browserProxyType == BROWSER_PROXY_TYPE_PAC) {
            if (browserAutoConfigUrl != null) {
                browserProxyAutoConfig = PacEvaluatorFactory.getPacEvaluator(browserAutoConfigUrl);
            }
        }

        browserUseSameProxy = Boolean.valueOf(prefs.get("network.proxy.share_proxy_settings"));

        browserHttpProxyHost = prefs.get("network.proxy.http");
        browserHttpProxyPort = stringToPort(prefs.get("network.proxy.http_port"));
        browserHttpsProxyHost = prefs.get("network.proxy.ssl");
        browserHttpsProxyPort = stringToPort(prefs.get("network.proxy.ssl_port"));
        browserFtpProxyHost = prefs.get("network.proxy.ftp");
        browserFtpProxyPort = stringToPort(prefs.get("network.proxy.ftp_port"));
        browserSocks4ProxyHost = prefs.get("networking.proxy.socks");
        browserSocks4ProxyPort = stringToPort(prefs.get("network.proxy.socks_port"));
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
     * The main entry point for {@link BrowserAwareProxySelector}. Based on
     * the browser settings, determines proxy information for a given URI.
     * <p>
     * The appropriate proxy may be determined by reading static information
     * from the browser's preferences file, or it may be computed dynamically,
     * by, for example, running javascript code.
     */
    @Override
    protected List<Proxy> getFromBrowser(URI uri) {
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
                if (JNLPRuntime.isDebug()) {
                    System.err.println(R("RProxyFirefoxOptionNotImplemented", browserProxyType, optionDescription));
                }
                proxies.add(Proxy.NO_PROXY);
        }

        if (JNLPRuntime.isDebug()) {
            System.out.println("Browser selected proxies: " + proxies.toString());
        }

        return proxies;
    }

    /**
     * Get an appropriate proxy for a given URI using a PAC specified in the
     * browser.
     */
    private List<Proxy> getFromBrowserPAC(URI uri) {
        if (browserAutoConfigUrl == null || uri.getScheme().equals("socket")) {
            return Arrays.asList(new Proxy[] { Proxy.NO_PROXY });
        }

        List<Proxy> proxies = new ArrayList<Proxy>();

        try {
            String proxiesString = browserProxyAutoConfig.getProxies(uri.toURL());
            proxies.addAll(getProxiesFromPacResult(proxiesString));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

    /**
     * Get an appropriate proxy for the given URI using static information from
     * the browser's preferences file.
     */
    private List<Proxy> getFromBrowserConfiguration(URI uri) {
        List<Proxy> proxies = new ArrayList<Proxy>();

        String scheme = uri.getScheme();

        if (browserUseSameProxy) {
            SocketAddress sa = new InetSocketAddress(browserHttpProxyHost, browserHttpProxyPort);
            Proxy proxy;
            if (scheme.equals("socket")) {
                proxy = new Proxy(Type.SOCKS, sa);
            } else {
                proxy = new Proxy(Type.HTTP, sa);
            }
            proxies.add(proxy);
        } else if (scheme.equals("http")) {
            SocketAddress sa = new InetSocketAddress(browserHttpProxyHost, browserHttpProxyPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        } else if (scheme.equals("https")) {
            SocketAddress sa = new InetSocketAddress(browserHttpsProxyHost, browserHttpsProxyPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        } else if (scheme.equals("ftp")) {
            SocketAddress sa = new InetSocketAddress(browserFtpProxyHost, browserFtpProxyPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        } else if (scheme.equals("socket")) {
            SocketAddress sa = new InetSocketAddress(browserSocks4ProxyHost, browserSocks4ProxyPort);
            proxies.add(new Proxy(Type.SOCKS, sa));
        } else {
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

}
