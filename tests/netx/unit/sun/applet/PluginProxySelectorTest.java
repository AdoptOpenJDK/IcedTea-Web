/* PluginProxySelectorTest
   Copyright (C) 2013  Red Hat

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPProxySelector;

import org.junit.Before;
import org.junit.Test;

public class PluginProxySelectorTest {

    private static class TestSelector extends PluginProxySelector {

        private URI browserResponse = null;
        private int remoteCallCount = 0;

        public TestSelector(DeploymentConfiguration config) {
            super(config);
        }

        @Override
        protected Object getProxyFromRemoteCallToBrowser(String uri) {
            remoteCallCount++;
            return browserResponse;
        }

        public void setBrowserResponse(URI response) {
            browserResponse = response;
        }

        public int getRemoteCallCount() {
            return remoteCallCount;
        }
    }

    private String PROXY_HOST = "localhost";
    private int PROXY_PORT = 42;

    private DeploymentConfiguration config;
    private TestSelector proxySelector;

    @Before
    public void setUp() {
        config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_BROWSER));

        proxySelector = new TestSelector(config);
    }

    @Test
    public void testNullResponseFromBrowserMeansNoProxy() throws URISyntaxException {
        List<Proxy> result = proxySelector.select(new URI("http://example.org"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testUnrecognizedURIMeansNoProxy() throws URISyntaxException {
        TestSelector proxySelector = new TestSelector(config);

        proxySelector.setBrowserResponse(new URI("http://" + PROXY_HOST + ":" + PROXY_PORT));

        List<Proxy> result = proxySelector.select(new URI("foo://example.org"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testHttpResponseFromBrowser() throws URISyntaxException {
        proxySelector.setBrowserResponse(new URI("http://" + PROXY_HOST + ":" + PROXY_PORT));

        List<Proxy> result = proxySelector.select(new URI("http://example.org"));

        Proxy expectedProxy = new Proxy(Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProxy, result.get(0));
    }

    @Test
    public void testHttpsResponseFromBrowser() throws URISyntaxException {
        proxySelector.setBrowserResponse(new URI("https://" + PROXY_HOST + ":" + PROXY_PORT));

        List<Proxy> result = proxySelector.select(new URI("https://example.org"));

        // FIXME if a browser returns a https URI, that does not mean socks
        Proxy expectedProxy = new Proxy(Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProxy, result.get(0));
    }

    @Test
    public void testFtpResponseFromBrowser() throws URISyntaxException {
        proxySelector.setBrowserResponse(new URI("ftp://" + PROXY_HOST + ":" + PROXY_PORT));

        List<Proxy> result = proxySelector.select(new URI("ftp://example.org"));

        // FIXME if a browser returns a ftp URI, that doesn't mean socks
        Proxy expectedProxy = new Proxy(Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProxy, result.get(0));
    }

    @Test
    public void testSocketResponseFromBrowser() throws URISyntaxException {
        TestSelector proxySelector = new TestSelector(config);

        // TODO does firefox actually return a "socks" URI? or a "socket" uri?
        proxySelector.setBrowserResponse(new URI("socks://" + PROXY_HOST + ":" + PROXY_PORT));

        List<Proxy> result = proxySelector.select(new URI("socket://example.org"));

        Proxy expectedProxy = new Proxy(Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProxy, result.get(0));
    }

    @Test
    public void testCacheIsUsedOnRepeatedCalls() throws URISyntaxException {
        proxySelector.setBrowserResponse(new URI("http://" + PROXY_HOST + ":" + PROXY_PORT));

        proxySelector.select(new URI("http://example.org"));
        proxySelector.select(new URI("http://example.org"));

        assertEquals(1, proxySelector.getRemoteCallCount());
    }

    @Test
    public void testCacheIsNotUsedOnDifferentCalls() throws URISyntaxException {
        proxySelector.setBrowserResponse(new URI("http://" + PROXY_HOST + ":" + PROXY_PORT));

        proxySelector.select(new URI("http://foo.example.org"));
        proxySelector.select(new URI("http://bar.example.org"));

        assertEquals(2, proxySelector.getRemoteCallCount());
    }


    @Test
    public void testConvertUriSchemeForProxyQuery() throws Exception {
        URI[] testUris = {
                new URI("http", "foo.com", "/bar", null),
                new URI("https", "foo.com", "/bar", null),
                new URI("ftp", "foo.com", "/app/res/pub/channel.jar?i=1234", null),
                new URI("socket", "foo.co.uk", "/bar/pub/ale.jar", null),
        };

        for (URI uri : testUris) {
            URI result = new URI(PluginProxySelector.convertUriSchemeForProxyQuery(uri));
            assertQueryForBrowserProxyUsesHttpFallback(uri, result);
            String hierarchicalPath = result.getAuthority() + result.getPath();
            assertQueryForBrowserProxyContainsNoDoubleSlashes(hierarchicalPath);
            assertQueryForBrowserProxyDoesNotChangeQuery(uri, result);
        }
    }

    // Test that only HTTP is used as fallback scheme if a protocol other than HTTP(S) or FTP is specified
    public void assertQueryForBrowserProxyUsesHttpFallback(URI expected, URI result) {
        if (expected.getScheme().equals("ftp") || expected.getScheme().startsWith("http")) {
            assertEquals(expected.getScheme(), result.getScheme());
        } else {
            assertEquals(result.getScheme(), "http");
        }
    }

    // Test that absolute resource paths do not result in double-slashes within the URI
    public void assertQueryForBrowserProxyContainsNoDoubleSlashes(String uri) {
        assertFalse(uri.contains("//"));
    }

    // Test that the query string of the URI is not changed
    public void assertQueryForBrowserProxyDoesNotChangeQuery(URI expected, URI result) {
        assertEquals(expected.getQuery(), result.getQuery());
    }

}
