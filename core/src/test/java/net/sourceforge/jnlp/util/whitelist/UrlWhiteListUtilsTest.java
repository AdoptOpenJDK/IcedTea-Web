package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UrlWhiteListUtilsTest {

    @Test
    public void validateWhitelistUrlString() {
        Assert.assertEquals("http://subdomain.domain.com:8888", UrlWhiteListUtils.validateWhitelistUrl("http://subdomain.domain.com:8888").getValidatedWhitelistEntry());
        Assert.assertEquals("https://subdomain.domain.com:9999", UrlWhiteListUtils.validateWhitelistUrl("https://subdomain.domain.com:9999").getValidatedWhitelistEntry());
        Assert.assertEquals("https://123.134.145.156:9999", UrlWhiteListUtils.validateWhitelistUrl("https://123.134.145.156:9999").getValidatedWhitelistEntry());
        Assert.assertEquals("http://123.134.145.156:8888", UrlWhiteListUtils.validateWhitelistUrl("http://123.134.145.156:8888").getValidatedWhitelistEntry());

        Assert.assertEquals("https://domain.com:443", UrlWhiteListUtils.validateWhitelistUrl("domain.com").getValidatedWhitelistEntry());
        Assert.assertEquals("https://*.domain.com:443", UrlWhiteListUtils.validateWhitelistUrl("*.domain.com").getValidatedWhitelistEntry());
        Assert.assertEquals("https://123.134.145.156:443", UrlWhiteListUtils.validateWhitelistUrl("123.134.145.156:443").getValidatedWhitelistEntry());

        Assert.assertEquals("http://subdomain.domain.com:80", UrlWhiteListUtils.validateWhitelistUrl("http://subdomain.domain.com").getValidatedWhitelistEntry());
        Assert.assertEquals("http://subdomain.domain.com:80", UrlWhiteListUtils.validateWhitelistUrl("http://subdomain.domain.com/abc/efg").getValidatedWhitelistEntry());
        Assert.assertEquals("https://subdomain.domain.com:443", UrlWhiteListUtils.validateWhitelistUrl("https://subdomain.domain.com").getValidatedWhitelistEntry());
        Assert.assertEquals("https://subdomain.domain.com:443", UrlWhiteListUtils.validateWhitelistUrl("https://subdomain.domain.com/abc/efg").getValidatedWhitelistEntry());
        Assert.assertEquals("https://123.134.145.156:443", UrlWhiteListUtils.validateWhitelistUrl("https://123.134.145.156").getValidatedWhitelistEntry());

        Assert.assertEquals("http://subdomain.domain.com", UrlWhiteListUtils.validateWhitelistUrl("http://subdomain.domain.com:*").getValidatedWhitelistEntry());
        Assert.assertEquals("https://subdomain.domain.com", UrlWhiteListUtils.validateWhitelistUrl("https://subdomain.domain.com:*").getValidatedWhitelistEntry());
        Assert.assertEquals("https://123.134.145.156", UrlWhiteListUtils.validateWhitelistUrl("https://123.134.145.156:*").getValidatedWhitelistEntry());

        Assert.assertEquals("https://*:443", UrlWhiteListUtils.validateWhitelistUrl("*").getValidatedWhitelistEntry());
        Assert.assertEquals("http://*:80", UrlWhiteListUtils.validateWhitelistUrl("http://*:80").getValidatedWhitelistEntry());
        Assert.assertEquals("https://*:443", UrlWhiteListUtils.validateWhitelistUrl("https://*:443").getValidatedWhitelistEntry());

        Assert.assertEquals("http://*", UrlWhiteListUtils.validateWhitelistUrl("http://*:*").getValidatedWhitelistEntry());
        Assert.assertEquals("https://*", UrlWhiteListUtils.validateWhitelistUrl("https://*:*").getValidatedWhitelistEntry());
    }

    @Test
    public void validateIllegalWhitelistUrlString() {
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://subdomain.domain.com:1*").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://*jvms.domain.com:443").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://jvms.*.com:443").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://xyz.dom*.com:443").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("*.domain.com:ABC").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("//*.domain.com:123").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl(":*.domain.com:123").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("://*.domain.com:123").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl(":/*.domain.com:123").isValid());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("/*.domain.com:123").isValid());
        // Seem illegal but are valid
        Assert.assertTrue(UrlWhiteListUtils.validateWhitelistUrl("123.domain.com:123").isValid());
        Assert.assertTrue(UrlWhiteListUtils.validateWhitelistUrl("123.123.123:123").isValid());
        Assert.assertTrue(UrlWhiteListUtils.validateWhitelistUrl("-123.domain.com:123").isValid());
    }

    @Test
    public void validateIllegalWhitelistIPUrlString() {
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://123.*.156.145").getErrorMessage().isEmpty());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://123.1*.0.156").getErrorMessage().isEmpty());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://123.134.145.-1").getErrorMessage().isEmpty());
        Assert.assertFalse(UrlWhiteListUtils.validateWhitelistUrl("https://256.134.145.255").getErrorMessage().isEmpty());
    }

    @Test
    public void urlInWhiteList() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(
                "https://rfy.m-b.com",
                "https://*.m-b.com",
                "https://rfy.*.com",
                "https://rfy.m-b.*",
                "https://*.*.*:446",
                "https://*:447",
                "https://*.mydomain.com",
                "http://*.mydomain.com",
                "*.cintra.net",
                "*.dmlr.com"
        );

        List<UrlWhiteListUtils.WhitelistEntry> wList = getValidatedWhitelist(wildcardWhiteList);

        // "https://rfry.m-b.com"
        URL url = new URL("https://rfy.m-b.com:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); //+ " https://rfy.m-b.com:443/some_URL");

        // "https://rfy.m-b.com:443"
        url = new URL("https://rfy.m-b.com:445/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy.m-b.com:445/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfyA.m-b.com:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b.com:443/some_URL");

        // "https://rfy.*.com"
        url = new URL("https://rfy.m-b1.com:443/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy.m-b1.com:443/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfy.m-b.org:443/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy.m-b.org:443/some_URL");

        url = new URL("https://rfy.m-b.com:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy.m-b.org:443/some_URL");

        // "https://*.*.*:446"
        url = new URL("https://rfy1.m-b1.org:446/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*:447"
        url = new URL("https://rfy1.m-b1.org:447/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*:446"
        url = new URL("https://rfy1.m-b1.com:445/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.com:445/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:444/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:80/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:81/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.cintra.net"
        url = new URL("https://abc.cintra.net:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        url = new URL("http://abc.cintra.net:443/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:443/some_URL");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:44/some_URL");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList)); // + " https://rfy1.m-b1.org:446/some_URL");
    }

    @Test
    public void demoUrlInWhiteList() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(
                "docs.oracle.com",
                "*.oracle.org",
                "docs.*.net"
        );

        List<UrlWhiteListUtils.WhitelistEntry> wList = getValidatedWhitelist(wildcardWhiteList);

        URL url = new URL("https://docs.oracle.com/j2se/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://any.oracle.org:443/j2se/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://any.one.oracle.org:443/j2se/tutorial");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://any.net:443/j2se/tutorial");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://docs.any.net:443/j2se/tutorial");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));
    }

    @Test
    public void ipUrlInWhiteList() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(
                "123.134.145.156",
                "123.134.145.156:167",
                "*.134.145.156:167",
                "http://124.134.145.156",
                "http://125.134.145.157:*",
                "https://123.134.145.156",
                "https://126.134.145.156:333",
                "http://124.134.145.156:333",
                "http://124.134.145.156:335/abc/efg"
        );

        List<UrlWhiteListUtils.WhitelistEntry> wList = getValidatedWhitelist(wildcardWhiteList);

        URL url = new URL("https://123.134.145.156/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://123.134.145.156:443/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://124.134.145.156:443");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:80/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://125.134.145.157:90/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://123.134.145.156:167/j2se/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:333/j2se/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.158:333/j2se/tutorial");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://126.134.145.156:333/j2se/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:335/abc/efg");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:336/abcd/efg");
        Assert.assertFalse(UrlWhiteListUtils.isUrlInWhitelist(url, wList));
    }

    @Test
    public void wildCard() throws Exception {
        List<String> wildcardWhiteList = Arrays.asList(
                "*",
                "http://*"
        );

        List<UrlWhiteListUtils.WhitelistEntry> wList = getValidatedWhitelist(wildcardWhiteList);

        URL url = new URL("https://123.134.145.156/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("https://abc.efg.com/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://abc.com/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));

        url = new URL("http://123.134.145.156:80/tutorial");
        Assert.assertTrue(UrlWhiteListUtils.isUrlInWhitelist(url, wList));
    }

    private static List<UrlWhiteListUtils.WhitelistEntry> getValidatedWhitelist(List<String> wildcardWhiteList) {
        return wildcardWhiteList
                .stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(UrlWhiteListUtils::validateWhitelistUrl)
                .collect(Collectors.toList());
    }
}
