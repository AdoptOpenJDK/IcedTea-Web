package net.sourceforge.jnlp.util;

import junit.framework.TestCase;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UrlKeyTest extends TestCase {

    public void testEqualsHttp() throws Exception {
        URL url1 = new URL("http://www.example.com:80/index.html?a=b#3");
        List<URL> urlList = Arrays.asList(
                new URL("http://www.example.com/index.html?a=b#3"),
                new URL("http://www.Example.Com/index.html?a=b#3"),
                new URL("Http://www.example.com/index.html?a=b#3"),
                new URL("http://www.example.com:80/index.html?a=b#3"));

        for (final URL url : urlList) {
            UrlKey key1 = new UrlKey(url1);
            UrlKey key2 = new UrlKey(url);

            assertEquals(key1.hashCode(), key2.hashCode());
            assertEquals(key1, key2);
            assertEquals(key2, key1);
        }
    }

    public void testNotEqualsHttp() throws Exception {
        URL url1 = new URL("http://www.example.com:80/index.html?a=b#3");
        List<URL> urlList = Arrays.asList(
                new URL("http://www.example.org:80/index.html?a=b#3"),
                new URL("http://www.example.com:80/index2.html?a=b#3"),
                new URL("http://www.example.com:80/index.html#3"),
                new URL("https://www.example.com:80/index.html?a=b#3"),
                new URL("http://www.example.com:80/index.html?a=c#3"),
                new URL("http://www.example.com:80/index.html?a=b#4"),
                new URL("http://www.example.com:80/index.html?a=b"),
                new URL("http://www.example.com:80/#3"),
                new URL("http://:80/index.html?a=b#3")
        );

        for (final URL url : urlList) {
            UrlKey key1 = new UrlKey(url1);
            UrlKey key2 = new UrlKey(url);

            assertNotEquals(key1.hashCode(), key2.hashCode());
            assertNotEquals(key1, key2);
            assertNotEquals(key2, key1);
        }
    }

    public void testNotEqualsDifferentPorts() throws Exception {
        URL url1 = new URL("http://www.example.com:80/index.html?a=b#3");
        URL url = new URL("http://www.example.com:81/index.html?a=b#3");

        UrlKey key1 = new UrlKey(url1);
        UrlKey key2 = new UrlKey(url);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key2);
        assertNotEquals(key2, key1);
    }

    private void assertNotEquals(Object o1, Object o2) {
        assertFalse(Objects.equals(o1, o2));
    }

    public void testEqualsHttps() throws Exception {
        URL url1 = new URL("https://www.example.com:443/index.html");
        URL url2 = new URL("https://www.example.com/index.html");

        UrlKey key1 = new UrlKey(url1);
        UrlKey key2 = new UrlKey(url2);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(key1, key2);
        assertEquals(key2, key1);
    }

    public void testEqualsFtp() throws Exception {
        URL url1 = new URL("ftp://www.example.com:21/index.html");
        URL url2 = new URL("ftp://www.example.com/index.html");

        UrlKey key1 = new UrlKey(url1);
        UrlKey key2 = new UrlKey(url2);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(key1, key2);
        assertEquals(key2, key1);
    }

}

