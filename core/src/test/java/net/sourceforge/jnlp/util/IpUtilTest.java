package net.sourceforge.jnlp.util;

import inet.ipaddr.HostNameException;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IpUtilTest {

    @Test
    public void testLocalhost() {
        assertTrue(IpUtil.isLocalhostOrLoopback("localhost"));
        assertTrue(IpUtil.isLocalhostOrLoopback((String) null));
        assertTrue(IpUtil.isLocalhostOrLoopback(""));

        // IPV4
        assertTrue(IpUtil.isLocalhostOrLoopback("127.0.0.1")); // loopback standard address
        assertTrue(IpUtil.isLocalhostOrLoopback("127.0.0.0/8")); // loopback address block

        assertFalse(IpUtil.isLocalhostOrLoopback("0.0.0.0")); // unspecified address

        // IPV6
        assertTrue(IpUtil.isLocalhostOrLoopback("::1")); // loopback standard address
        assertTrue(IpUtil.isLocalhostOrLoopback("0:0:0:0:0:0:0:1")); // loopback standard address

        assertFalse(IpUtil.isLocalhostOrLoopback("0:0:0:0:0:0:0:0")); // unspecified address
    }

    @Test
    public void testLocalhostUrls() throws MalformedURLException {
        // IP4
        assertTrue(IpUtil.isLocalhostOrLoopback(new URL("http://127.0.0.1/")));
        assertTrue(IpUtil.isLocalhostOrLoopback(new URL("https://127.34.123.43/")));

        assertFalse(IpUtil.isLocalhostOrLoopback(new URL("https://0.0.0.0/"))); // unspecified address
        assertFalse(IpUtil.isLocalhostOrLoopback(new URL("https://192.168.0.123/"))); // regular

        // IP6
        assertTrue(IpUtil.isLocalhostOrLoopback(new URL("http://[::1]/")));
        assertTrue(IpUtil.isLocalhostOrLoopback(new URL("https://[::1]/")));
    }

    @Test
    public void testLocalhostUris() throws URISyntaxException {
        // IP4
        assertTrue(IpUtil.isLocalhostOrLoopback(new URI("https://127.0.0.1"))); // loopback address lower bound
        assertTrue(IpUtil.isLocalhostOrLoopback(new URI("https://127.255.255.254"))); // loopback address upper bound

        assertFalse(IpUtil.isLocalhostOrLoopback(new URI("https://0.0.0.0/8"))); // unspecified address

        // IP6
        assertTrue(IpUtil.isLocalhostOrLoopback(new URI("http://[::1]/")));
        assertTrue(IpUtil.isLocalhostOrLoopback(new URI("https://[::1]/")));
    }

    @Test
    public void testLocalFile() throws MalformedURLException {
        URL url = new URL("file:/C:/OpenWebStart/../OWSFiles/AccessibleScrollDemo.jnlp");
        assertTrue(IpUtil.isLocalhostOrLoopback(url));
    }
}
