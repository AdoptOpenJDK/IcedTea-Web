package net.adoptopenjdk.icedteaweb.proxy.pac;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class PacBasedProxyProviderTest {

    @Test
    public void selectTest() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://anyserver:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assert.assertNotNull(proxies);
        Assert.assertEquals(2, proxies.size());
        Assert.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxies.get(0));
        Assert.assertEquals(Proxy.NO_PROXY, proxies.get(1));
    }

    @Test
    public void selectTest2() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://myserver:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assert.assertNotNull(proxies);
        Assert.assertEquals(1, proxies.size());
        Assert.assertEquals(Proxy.NO_PROXY, proxies.get(0));
    }

    @Test
    public void selectTest3() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://noproxy:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assert.assertNotNull(proxies);
        Assert.assertEquals(1, proxies.size());
        Assert.assertEquals(Proxy.NO_PROXY, proxies.get(0));
    }
}
