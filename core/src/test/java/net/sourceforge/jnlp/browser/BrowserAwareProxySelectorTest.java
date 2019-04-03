/* BrowserAwareProxySelectorTest.java
   Copyright (C) 2013 Red Hat, Inc.

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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPProxySelector;

import org.junit.Before;
import org.junit.Test;

public class BrowserAwareProxySelectorTest {

    static class TestBrowserAwareProxySelector extends BrowserAwareProxySelector {

        private final Map<String, String> browserPrefs;

        public TestBrowserAwareProxySelector(DeploymentConfiguration config, Map<String, String> browserPrefs) {
            super(config);
            this.browserPrefs = browserPrefs;
        }

        @Override
        protected Map<String, String> parseBrowserPreferences() throws IOException {
            return browserPrefs;
        }
    }

    private static final String PROXY_HOST = "foo";
    private static final int PROXY_PORT = 42;
    private static final InetSocketAddress PROXY_ADDRESS = new InetSocketAddress(PROXY_HOST, PROXY_PORT);

    private DeploymentConfiguration config;
    private Map<String, String> browserPrefs;

    @Before
    public void setUp() {
        config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_BROWSER));

        browserPrefs = new HashMap<String, String>();
    }

    @Test
    public void testNoBrowserProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "0" /* none */);

        List<Proxy> result = getProxy(config, browserPrefs, new URI("https://example.org"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testBrowserManualSameProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.share_proxy_settings", "true");
        browserPrefs.put("network.proxy.http", PROXY_HOST);
        browserPrefs.put("network.proxy.http_port", String.valueOf(PROXY_PORT));

        List<Proxy> result;

        result = getProxy(config, browserPrefs, new URI("https://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, PROXY_ADDRESS), result.get(0));

        result = getProxy(config, browserPrefs, new URI("socket://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, PROXY_ADDRESS), result.get(0));

    }

    @Test
    public void testBrowserManualHttpsProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.ssl", PROXY_HOST);
        browserPrefs.put("network.proxy.ssl_port", String.valueOf(PROXY_PORT));

        List<Proxy> result = getProxy(config, browserPrefs, new URI("https://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, PROXY_ADDRESS), result.get(0));
    }

    @Test
    public void testBrowserManualHttpProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.http", PROXY_HOST);
        browserPrefs.put("network.proxy.http_port", String.valueOf(PROXY_PORT));

        List<Proxy> result = getProxy(config, browserPrefs, new URI("http://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, PROXY_ADDRESS), result.get(0));
    }

    @Test
    public void testBrowserManualFtpProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.ftp", PROXY_HOST);
        browserPrefs.put("network.proxy.ftp_port", String.valueOf(PROXY_PORT));

        List<Proxy> result = getProxy(config, browserPrefs, new URI("ftp://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, PROXY_ADDRESS), result.get(0));
    }

    @Test
    public void testBrowserManualSocksProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.socks", PROXY_HOST);
        browserPrefs.put("network.proxy.socks_port", String.valueOf(PROXY_PORT));

        List<Proxy> result = getProxy(config, browserPrefs, new URI("socket://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, PROXY_ADDRESS), result.get(0));
    }

    @Test
    public void testBrowserManualHttpProxyFallsBackToSocksProxy() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);
        browserPrefs.put("network.proxy.socks", PROXY_HOST);
        browserPrefs.put("network.proxy.socks_port", String.valueOf(PROXY_PORT));

        List<Proxy> result = getProxy(config, browserPrefs, new URI("http://example.org"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, PROXY_ADDRESS), result.get(0));
    }

    @Test
    public void testBrowserManualProxyUnknownProtocol() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "1" /* = manual */);

        List<Proxy> result = getProxy(config, browserPrefs, new URI("gopher://example.org"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testBrowserAutoProxyUnimplemented() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "4" /* = auto */);

        List<Proxy> result = getProxy(config, browserPrefs, new URI("http://example.org"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testBrowserSystemProxyUnimplemented() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "5" /* = system */);

        List<Proxy> result = getProxy(config, browserPrefs, new URI("http://example.org"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testBrowserPacProxyUnimplemented() throws URISyntaxException {
        browserPrefs.put("network.proxy.type", "2" /* = pac */);

        List<Proxy> result = getProxy(config, browserPrefs, new URI("http://example.org"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    private static List<Proxy> getProxy(DeploymentConfiguration config, Map<String, String> browserPrefs, URI uri) {
        BrowserAwareProxySelector selector = new TestBrowserAwareProxySelector(config, browserPrefs);
        selector.initialize();

        return selector.getFromBrowser(uri);
    }
}
