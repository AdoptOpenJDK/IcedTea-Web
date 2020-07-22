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
    public void validateWhitelistUrlString() {
        Assert.assertEquals("http://subdomain.domain.com:8888", UrlWhiteListUtils.validateWhiteListUrlString("http://subdomain.domain.com:8888").getWhiteListEntry());
        Assert.assertEquals("https://subdomain.domain.com:9999", UrlWhiteListUtils.validateWhiteListUrlString("https://subdomain.domain.com:9999").getWhiteListEntry());

        Assert.assertEquals("https://domain.com:443", UrlWhiteListUtils.validateWhiteListUrlString("domain.com").getWhiteListEntry());
        Assert.assertEquals("https://*.domain.com:443", UrlWhiteListUtils.validateWhiteListUrlString("*.domain.com").getWhiteListEntry());
        Assert.assertEquals("https://*.domain.com", UrlWhiteListUtils.validateWhiteListUrlString("*.domain.com:*").getWhiteListEntry());

        Assert.assertEquals("http://subdomain.domain.com:80", UrlWhiteListUtils.validateWhiteListUrlString("http://subdomain.domain.com").getWhiteListEntry());
        Assert.assertEquals("https://subdomain.domain.com:443", UrlWhiteListUtils.validateWhiteListUrlString("https://subdomain.domain.com").getWhiteListEntry());

        Assert.assertEquals("http://subdomain.domain.com", UrlWhiteListUtils.validateWhiteListUrlString("http://subdomain.domain.com:*").getWhiteListEntry());
        Assert.assertEquals("https://subdomain.domain.com", UrlWhiteListUtils.validateWhiteListUrlString("https://subdomain.domain.com:*").getWhiteListEntry());

        Assert.assertEquals("http://*:80", UrlWhiteListUtils.validateWhiteListUrlString("http://*:80").getWhiteListEntry());
        Assert.assertEquals("https://*:443", UrlWhiteListUtils.validateWhiteListUrlString("https://*:443").getWhiteListEntry());

        Assert.assertEquals("http://*", UrlWhiteListUtils.validateWhiteListUrlString("http://*:*").getWhiteListEntry());
        Assert.assertEquals("https://*", UrlWhiteListUtils.validateWhiteListUrlString("https://*:*").getWhiteListEntry());
    }

    @Test
    public void validateIllegalWhitelistUrlString() throws MalformedURLException {
        Assert.assertFalse(UrlWhiteListUtils.validateWhiteListUrlString("https://subdomain.domain.com:1*").getErrorMessage().isEmpty());
        Assert.assertFalse(UrlWhiteListUtils.validateWhiteListUrlString("*.domain.com:ABC").getErrorMessage().isEmpty());
        Assert.assertTrue(UrlWhiteListUtils.validateWhiteListUrlString("//*.domain.com:123").getErrorMessage().isEmpty());
        Assert.assertFalse(UrlWhiteListUtils.validateWhiteListUrlString("https://*jvms.domain.com:443").getErrorMessage().isEmpty());
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
                .map(s -> UrlWhiteListUtils.validateWhiteListUrlString(s).getWhiteListEntry())
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

    @Test
    public void demoUrlInWhiteList() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(new String[]{
                "docs.oracle.com:*",
                "*.oracle.org",
                "docs.*.net",
        });

        List<String> wList = wildcardWhiteList
                .stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(s -> UrlWhiteListUtils.validateWhiteListUrlString(s).getWhiteListEntry())
                .collect(Collectors.toList());

        URL url = new URL("https://docs.oracle.com/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false));

        url = new URL("https://any.oracle.org:443/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false));

        url = new URL("https://any.one.oracle.org:443/j2se/tutorial");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false));

        url = new URL("https://any.net:443/j2se/tutorial");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false));
    }
}