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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Proxy selector implementation for plugin network functions.
 * 
 * This class fetches proxy information from the web browser and 
 * uses that information in the context of all network connection 
 * (plugin specific and applet connections) as applicable
 * 
 */

public class PluginProxySelector extends ProxySelector {

    private TimedHashMap<String, Proxy> proxyCache = new TimedHashMap<String, Proxy>(); 


    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // If the connection fails, there is little we can do here. Just print the exception
        ioe.printStackTrace();
    }

    /**
     * Selects the appropriate proxy (or DIRECT connection method) for the given URI
     * 
     * @param uri The URI being accessed
     * @return A list of Proxy objects that are usable for this URI
     */
    @Override
    public List<Proxy> select(URI uri) {

        List<Proxy> proxyList = new ArrayList<Proxy>();

        // check cache first
        Proxy cachedProxy = checkCache(uri);
        if (cachedProxy != null) {
            proxyList.add(cachedProxy);
            return proxyList;
        }

        // Nothing usable in cache. Fetch info from browser
        Proxy proxy = Proxy.NO_PROXY;
        Object o = PluginAppletViewer.requestPluginProxyInfo(uri);

        // If the browser returned anything, try to parse it. If anything in the try block fails, the fallback is direct connection
        try {
            if (o != null) {
                PluginDebug.debug("Proxy URI = " + o);
                URI proxyURI = (URI) o;
                
                // If origin uri is http/ftp, we're good. If origin uri is not that, the proxy _must_ be socks, else we fallback to direct
                if (uri.getScheme().startsWith("http") || uri.getScheme().equals("ftp") || proxyURI.getScheme().startsWith("socks")) {

                    Proxy.Type type = proxyURI.getScheme().equals("http") ? Proxy.Type.HTTP : Proxy.Type.SOCKS; 
                    InetSocketAddress socketAddr = new InetSocketAddress(proxyURI.getHost(), proxyURI.getPort());

                    proxy = new Proxy(type, socketAddr);

                    String uriKey = uri.getScheme() + "://" + uri.getHost();
                    proxyCache.put(uriKey, proxy);
                } else {
                    PluginDebug.debug("Proxy " + proxyURI + " cannot be used for " + uri + ". Falling back to DIRECT");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        proxyList.add(proxy);

        PluginDebug.debug("Proxy for " + uri.toString() + " is " + proxy);

        return proxyList;
    }

    /** 
     * Checks to see if proxy information is already cached. 
     * 
     * @param uri The URI to check
     * @return The cached Proxy. null if there is no suitable cached proxy. 
     */
    private Proxy checkCache(URI uri) {
        
        String uriKey = uri.getScheme() + "://" + uri.getHost();
        if (proxyCache.get(uriKey) != null) {
            return proxyCache.get(uriKey);
        }

        return null;
    }
    
    /**
     * Simple utility class that extends HashMap by adding an expiry to the entries.
     * 
     * This map stores entries, and returns them only if the entries were last accessed within time t=10 seconds
     *
     * @param <K> The key type
     * @param <V> The Object type
     */

    private class TimedHashMap<K,V> extends HashMap<K,V> {

        HashMap<K, Long> timeStamps = new HashMap<K, Long>();
        Long expiry = 10000L;
        
        /**
         * Store the item in the map and associate a timestamp with it
         * 
         * @param key The key
         * @param value The value to store
         */
        public V put(K key, V value) {
            timeStamps.put(key, new Date().getTime());
            return super.put(key, value);
        }

        /**
         * Return cached item if it has not already expired.
         * 
         * Before returning, this method also resets the "last accessed" 
         * time for this entry, so it is good for another 10 seconds
         * 
         * @param key The key
         */
        public V get(Object key) {

            Long now = new Date().getTime();

            if (super.containsKey(key)) {
                Long age = now - timeStamps.get(key);

                // Item exists. If it has not expired, renew its access time and return it 
                if (age <= expiry) {
                    PluginDebug.debug("Returning proxy " + super.get(key) + " from cache for " + key);
                    timeStamps.put((K) key, (new Date()).getTime());
                    return super.get(key);
                } else {
                    PluginDebug.debug("Proxy cache for " + key + " has expired (age=" + age/1000.0 + " seconds)");
                }
            } 

            return null;
        }
    }
    
}
