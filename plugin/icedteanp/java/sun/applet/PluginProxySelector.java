/* PluginProxySelector -- proxy selector for all connections from applets and the plugin
   Copyright (C) 2009  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */

package sun.applet;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.sun.jndi.toolkit.url.UrlUtil;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPProxySelector;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.TimedHashMap;

/**
 * Proxy selector implementation for plugin network functions.
 *
 * This class fetches proxy information from the web browser and
 * uses that information in the context of all network connection
 * (plugin specific and applet connections) as applicable
 *
 */

public class PluginProxySelector extends JNLPProxySelector {

    private TimedHashMap<String, Proxy> proxyCache = new TimedHashMap<String, Proxy>();

    public PluginProxySelector(DeploymentConfiguration config) {
        super(config);
    }

    /**
     * Selects the appropriate proxy (or DIRECT connection method) for the given URI
     *
     * @param uri The URI being accessed
     * @return A list of Proxy objects that are usable for this URI
     */
    @Override
    protected List<Proxy> getFromBrowser(URI uri) {

        List<Proxy> proxyList = new ArrayList<Proxy>();

        // check cache first
        Proxy cachedProxy = checkCache(uri);
        if (cachedProxy != null) {
            proxyList.add(cachedProxy);
            return proxyList;
        }

        // Nothing usable in cache. Fetch info from browser

        String requestURI;
        try {
            requestURI = convertUriSchemeForProxyQuery(uri);
        } catch (Exception e) {
            PluginDebug.debug("Cannot construct URL from ", uri.toString(), " ... falling back to DIRECT proxy");
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
            proxyList.add(Proxy.NO_PROXY);
            return proxyList;
        }

        Proxy proxy = Proxy.NO_PROXY;
        Object o = getProxyFromRemoteCallToBrowser(requestURI);

        // If the browser returned anything, try to parse it. If anything in the try block fails, the fallback is direct connection
        try {
            if (o != null) {
                PluginDebug.debug("Proxy URI = ", o);
                URI proxyURI = (URI) o;

                // If origin uri is http/ftp, we're good. If origin uri is not that, the proxy _must_ be socks, else we fallback to direct
                if (uri.getScheme().startsWith("http") || uri.getScheme().equals("ftp") || proxyURI.getScheme().startsWith("socks")) {

                    Proxy.Type type = proxyURI.getScheme().equals("http") ? Proxy.Type.HTTP : Proxy.Type.SOCKS;
                    InetSocketAddress socketAddr = new InetSocketAddress(proxyURI.getHost(), proxyURI.getPort());

                    proxy = new Proxy(type, socketAddr);

                    String uriKey = computeKey(uri);
                    proxyCache.put(uriKey, proxy);
                } else {
                    PluginDebug.debug("Proxy ", proxyURI, " cannot be used for ", uri, ". Falling back to DIRECT");
                }
            }
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
        }

        proxyList.add(proxy);

        PluginDebug.debug("Proxy for ", uri.toString(), " is ", proxy);

        return proxyList;
    }

    /** For tests to override */
    protected Object getProxyFromRemoteCallToBrowser(String uri) {
        return PluginAppletViewer.requestPluginProxyInfo(uri);
    }

    /**
     * Checks to see if proxy information is already cached.
     *
     * @param uri The URI to check
     * @return The cached Proxy. null if there is no suitable cached proxy.
     */
    private Proxy checkCache(URI uri) {
        String uriKey = computeKey(uri);
        if (proxyCache.get(uriKey) != null) {
            return proxyCache.get(uriKey);
        }

        return null;
    }

    /** Compute a key to use for the proxy cache */
    private String computeKey(URI uri) {
        return uri.getScheme() + "://" + uri.getHost();
    }

    public static String convertUriSchemeForProxyQuery(URI uri) throws URISyntaxException, UnsupportedEncodingException {
        // there is no easy way to get SOCKS proxy info. So, we tell mozilla that we want proxy for
        // an HTTP uri in case of non http/ftp protocols. If we get back a SOCKS proxy, we can
        // use that, if we get back an http proxy, we fallback to DIRECT connect

        String scheme = uri.getScheme();
        if (!scheme.startsWith("http") && !scheme.equals("ftp")) {
            scheme = "http";
        }

        URI result = new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), uri.getQuery(), uri.getFragment());
        return UrlUtil.encode(result.toString(), "UTF-8");
    }
}
