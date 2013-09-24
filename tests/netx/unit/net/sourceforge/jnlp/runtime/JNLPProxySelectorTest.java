/* JNLPProxySelectorTest.java
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

package net.sourceforge.jnlp.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.jnlp.config.DeploymentConfiguration;

import org.junit.Ignore;
import org.junit.Test;

public class JNLPProxySelectorTest {

    private static final Proxy BROWSER_PROXY = new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved("foo", 0xF00));

    class TestProxySelector extends JNLPProxySelector {
        public TestProxySelector(DeploymentConfiguration config) {
            super(config);
        }

        @Override
        protected List<Proxy> getFromBrowser(URI uri) {
            return Arrays.asList(BROWSER_PROXY);
        }
    }

    @Test
    public void testNoProxy() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_NONE));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testProxyBypassLocal() throws URISyntaxException, UnknownHostException {
        final String LOCALHOST = InetAddress.getLocalHost().getHostName();

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_BYPASS_LOCAL, String.valueOf(true));

        List<Proxy> result;
        JNLPProxySelector selector = new TestProxySelector(config);

        result = selector.select(new URI("http://127.0.0.1/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = selector.select(new URI("http://" + LOCALHOST + "/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = selector.select(new URI("socket://127.0.0.1/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = selector.select(new URI("socket://" + LOCALHOST + "/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    // TODO implement this
    @Ignore("Implement this")
    @Test
    public void testLocalProxyBypassListIsIgnoredForNonLocal() {
        fail();
    }

    @Test
    public void testProxyBypassList() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_BYPASS_LIST, "example.org");

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result;

        result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = selector.select(new URI("socket://example.org/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testManualHttpProxy() throws URISyntaxException {
        String HTTP_HOST = "example.org";
        int HTTP_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTP_HOST, HTTP_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTP_PORT, String.valueOf(HTTP_PORT));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress(HTTP_HOST, HTTP_PORT)), result.get(0));
    }

    @Test
    public void testManualHttpsProxy() throws URISyntaxException {
        String HTTPS_HOST = "example.org";
        int HTTPS_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTPS_HOST, HTTPS_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTPS_PORT, String.valueOf(HTTPS_PORT));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("https://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress(HTTPS_HOST, HTTPS_PORT)), result.get(0));
    }

    @Test
    public void testManualFtpProxy() throws URISyntaxException {
        String FTP_HOST = "example.org";
        int FTP_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_FTP_HOST, FTP_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_FTP_PORT, String.valueOf(FTP_PORT));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("ftp://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress(FTP_HOST, FTP_PORT)), result.get(0));
    }

    @Test
    public void testManualSocksProxy() throws URISyntaxException {
        String SOCKS_HOST = "example.org";
        int SOCKS_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_SOCKS4_HOST, SOCKS_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_SOCKS4_PORT, String.valueOf(SOCKS_PORT));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("socket://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, new InetSocketAddress(SOCKS_HOST, SOCKS_PORT)), result.get(0));
    }

    @Test
    public void testHttpFallsBackToManualSocksProxy() throws URISyntaxException {
        String SOCKS_HOST = "example.org";
        int SOCKS_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_SOCKS4_HOST, SOCKS_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_SOCKS4_PORT, String.valueOf(SOCKS_PORT));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, new InetSocketAddress(SOCKS_HOST, SOCKS_PORT)), result.get(0));
    }

    @Test
    public void testManualUnknownProtocolProxy() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("gopher://example.org/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    @Test
    public void testManualSameProxy() throws URISyntaxException {
        final String HTTP_HOST = "example.org";
        final int HTTP_PORT = 42;

        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_MANUAL));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTP_HOST, HTTP_HOST);
        config.setProperty(DeploymentConfiguration.KEY_PROXY_HTTP_PORT, String.valueOf(HTTP_PORT));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_SAME, String.valueOf(true));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result;

        result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress(HTTP_HOST, HTTP_PORT)), result.get(0));
    }

    @Test
    public void testBrowserProxy() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_BROWSER));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertSame(BROWSER_PROXY, result.get(0));
    }

    @Test
    public void testMissingProxyAutoConfigUrl() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_AUTO));

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    // TODO
    @Ignore("Need to find a way to inject a custom proxy autoconfig file first")
    @Test
    public void testProxyAutoConfig() throws URISyntaxException {
        DeploymentConfiguration config = new DeploymentConfiguration();
        config.setProperty(DeploymentConfiguration.KEY_PROXY_TYPE, String.valueOf(JNLPProxySelector.PROXY_TYPE_AUTO));
        config.setProperty(DeploymentConfiguration.KEY_PROXY_AUTO_CONFIG_URL, "foobar");

        JNLPProxySelector selector = new TestProxySelector(config);
        List<Proxy> result = selector.select(new URI("http://example.org/"));

        assertEquals(1, result.size());
    }

    // TODO this JNLPProxySelect#getProxiesFromPacResult should be moved into a different class
    // TODO this test should be split into different methods
    @Test
    public void testConvertingProxyAutoConfigResultToProxyObject() {
        List<Proxy> result;

        result = JNLPProxySelector.getProxiesFromPacResult("foo bar baz; what is this; dunno");
        assertEquals(0, result.size());

        result = JNLPProxySelector.getProxiesFromPacResult("DIRECT");
        assertEquals(1, result.size());
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = JNLPProxySelector.getProxiesFromPacResult("PROXY foo:42");
        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("foo", 42)), result.get(0));

        result = JNLPProxySelector.getProxiesFromPacResult("PROXY foo:bar");
        assertEquals(0, result.size());

        result = JNLPProxySelector.getProxiesFromPacResult("PROXY foo");
        assertEquals(0, result.size());

        result = JNLPProxySelector.getProxiesFromPacResult("SOCKS foo:42");
        assertEquals(1, result.size());
        assertEquals(new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved("foo", 42)), result.get(0));

        result = JNLPProxySelector.getProxiesFromPacResult("SOCKS foo:bar");
        assertEquals(0, result.size());

        result = JNLPProxySelector.getProxiesFromPacResult("SOCKS foo");
        assertEquals(0, result.size());

    }
}
