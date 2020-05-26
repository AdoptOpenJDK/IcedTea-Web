package net.adoptopenjdk.icedteaweb.proxy.pac;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;


public class PacUtilsTest {

    @Test
    public void getProxiesFromPacResultTest() {
        //given
        final String pacResult = "PROXY proxy.example.com:8080";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assert.assertNotNull(proxiesFromPacResult);
        Assert.assertEquals(1, proxiesFromPacResult.size());
        Assert.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxiesFromPacResult.get(0));
    }

    @Test
    public void getProxiesFromPacResultTest2() {
        //given
        final String pacResult = "DIRECT";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assert.assertNotNull(proxiesFromPacResult);
        Assert.assertEquals(1, proxiesFromPacResult.size());
        Assert.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(0));
    }

    @Test
    public void getProxiesFromPacResultTest3() {
        //given
        final String pacResult = "PROXY proxy.example.com:8080; DIRECT";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assert.assertNotNull(proxiesFromPacResult);
        Assert.assertEquals(2, proxiesFromPacResult.size());
        Assert.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxiesFromPacResult.get(0));
        Assert.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(1));

    }

    @Test
    public void getProxiesFromPacResultTest4() {
        //given
        final String pacResult = null;

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assert.assertNotNull(proxiesFromPacResult);
        Assert.assertEquals(1, proxiesFromPacResult.size());
        Assert.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(0));

    }

    @Test
    public void getProxiesFromPacResultTest5() {
        //given
        final String pacResult = "INVALID";

        //than
        try {
            PacUtils.getProxiesFromPacResult(pacResult);
            Assert.fail();
        }
        catch (IllegalArgumentException e) {}
    }
}
