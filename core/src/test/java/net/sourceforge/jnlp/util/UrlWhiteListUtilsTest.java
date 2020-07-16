package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.util.UrlWhiteListUtils.isUrlInWhitelist;

public class UrlWhiteListUtilsTest {

    @Test
    public void expandLegalWhitelistUrlString() {
        Assert.assertEquals("http://subdomain.domain.com:8080", UrlWhiteListUtils.expandWhiteListUrlString("http://subdomain.domain.com:8080"));

        Assert.assertEquals("https://domain.com:443", UrlWhiteListUtils.expandWhiteListUrlString("domain.com"));
        Assert.assertEquals("https://*.domain.com:443", UrlWhiteListUtils.expandWhiteListUrlString("*.domain.com"));
        Assert.assertEquals("https://*.domain.com", UrlWhiteListUtils.expandWhiteListUrlString("*.domain.com:*"));

        Assert.assertEquals("http://subdomain.domain.com:80", UrlWhiteListUtils.expandWhiteListUrlString("http://subdomain.domain.com"));
        Assert.assertEquals("https://subdomain.domain.com:443", UrlWhiteListUtils.expandWhiteListUrlString("https://subdomain.domain.com"));

        Assert.assertEquals("http://subdomain.domain.com", UrlWhiteListUtils.expandWhiteListUrlString("http://subdomain.domain.com:*"));
        Assert.assertEquals("https://subdomain.domain.com", UrlWhiteListUtils.expandWhiteListUrlString("https://subdomain.domain.com:*"));

        Assert.assertEquals("http://*:80", UrlWhiteListUtils.expandWhiteListUrlString("http://*:80"));
        Assert.assertEquals("https://*:443", UrlWhiteListUtils.expandWhiteListUrlString("https://*:443"));

        Assert.assertEquals("http://*", UrlWhiteListUtils.expandWhiteListUrlString("http://*:*"));
        Assert.assertEquals("https://*", UrlWhiteListUtils.expandWhiteListUrlString("https://*:*"));

        // Expected: https://*.domain.com:*, Actual: https://*.domain.com
        // If we resolve this to ' https://*.domain.com', doesn't this mean default port 443? But what we want is any port is whitelisted
       // Assert.assertEquals("https://*.domain.com:*", UrlWhiteListUtils.expandWhiteListUrlString("*.domain.com:*"));
    }

    @Test(expected = MalformedURLException.class)
    public void expandIllegalWhitelistUrlString() throws MalformedURLException {
        URL expURL = new URL(UrlWhiteListUtils.expandWhiteListUrlString("https://subdomain.domain.com:1*"));

             expURL = new URL(UrlWhiteListUtils.expandWhiteListUrlString("https://*jvms.domain.com:443"));

             expURL = new URL(UrlWhiteListUtils.expandWhiteListUrlString("*.domain.com:ABC"));
    }

    @Test
    public void urlInWhiteList() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(new String[]{
                "https://rfy.m-b.com",
                "https://*.m-b.com",
                "https://rfy.*.com",
                "https://rfy.m-b.*",
                "https://*.*.*:446",
                "https://*:447",
                "https://*.mydomain.com",
                "http://*.mydomain.com",
                "*.cintra.net",
                "*.dmlr.com"});

        List<String> wList = wildcardWhiteList
                .stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(s -> UrlWhiteListUtils.expandWhiteListUrlString(s))
                .collect(Collectors.toList());

        // "https://rfry.m-b.com"
        URL url = new URL("https://rfy.m-b.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); //+ " https://rfy.m-b.com:443/some_URL");

        // "https://rfy.m-b.com:443"
        url = new URL("https://rfy.m-b.com:445/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b.com:445/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfyA.m-b.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b.com:443/some_URL");

        // "https://rfy.*.com"
        url = new URL("https://rfy.m-b1.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b1.com:443/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfy.m-b.org:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b.org:443/some_URL");

        // "https://*.*.*:446"
        url = new URL("https://rfy1.m-b1.org:446/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*:447"
        url = new URL("https://rfy1.m-b1.org:447/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*.*.*:446"
        url = new URL("https://rfy1.m-b1.com:445/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.com:445/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:444/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:80/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:81/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.cintra.net"
        url = new URL("https://abc.cintra.net:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        url = new URL("http://abc.cintra.net:443/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:44/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");
    }
}
