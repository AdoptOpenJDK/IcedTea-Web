package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils.isUrlInWhitelist;
import static net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils.parseEntry;

public class UrlWhiteListUtilsTest {

    @Test
    public void validateWhitelistUrlString() {
        assertEffectiveUrl("http://subdomain.domain.com:8888", "http://subdomain.domain.com:8888");
        assertEffectiveUrl("https://subdomain.domain.com:9999", "https://subdomain.domain.com:9999");
        assertEffectiveUrl("https://123.134.145.156:9999", "https://123.134.145.156:9999");
        assertEffectiveUrl("http://123.134.145.156:8888", "http://123.134.145.156:8888");

        assertEffectiveUrl("https://domain.com:443", "domain.com");
        assertEffectiveUrl("https://*.domain.com:443", "*.domain.com");
        assertEffectiveUrl("https://123.134.145.156:443", "123.134.145.156:443");

        assertEffectiveUrl("http://subdomain.domain.com:80", "http://subdomain.domain.com");
        assertEffectiveUrl("http://subdomain.domain.com:80", "http://subdomain.domain.com/abc/efg");
        assertEffectiveUrl("https://subdomain.domain.com:443", "https://subdomain.domain.com");
        assertEffectiveUrl("https://subdomain.domain.com:443", "https://subdomain.domain.com/abc/efg");
        assertEffectiveUrl("https://123.134.145.156:443", "https://123.134.145.156");

        assertEffectiveUrl("http://subdomain.domain.com:*", "http://subdomain.domain.com:*");
        assertEffectiveUrl("https://subdomain.domain.com:*", "https://subdomain.domain.com:*");
        assertEffectiveUrl("https://123.134.145.156:*", "https://123.134.145.156:*");

        assertEffectiveUrl("https://*:443", "*");
        assertEffectiveUrl("http://*:80", "http://*:80");
        assertEffectiveUrl("https://*:443", "https://*:443");

        assertEffectiveUrl("http://*:*", "http://*:*");
        assertEffectiveUrl("https://*:*", "https://*:*");
    }

    private void assertEffectiveUrl(String expected, String wlUrlStr) {
        Assert.assertEquals(expected, parseEntry(wlUrlStr).getEffectiveWhitelistEntry());
    }

    @Test
    public void validateIllegalWhitelistUrlString() {
        assertInvalidWhitelistEntry("https://subdomain.domain.com:1*");
        assertInvalidWhitelistEntry("https://*jvms.domain.com:443");
        assertInvalidWhitelistEntry("https://jvms.*.com:443");
        assertInvalidWhitelistEntry("https://xyz.dom*.com:443");
        assertInvalidWhitelistEntry("*.domain.com:ABC");
        assertInvalidWhitelistEntry("//*.domain.com:123");
        assertInvalidWhitelistEntry(":*.domain.com:123");
        assertInvalidWhitelistEntry("://*.domain.com:123");
        assertInvalidWhitelistEntry(":/*.domain.com:123");
        assertInvalidWhitelistEntry("/*.domain.com:123");
        // Seem illegal but are valid
        assertValidWhitelistEntry("123.domain.com:123");
        assertValidWhitelistEntry("123.123.123:123");
        assertValidWhitelistEntry("-123.domain.com:123");
    }

    @Test
    public void validateIllegalWhitelistIPUrlString() {
        assertInvalidWhitelistEntry("https://*.156.145");
        assertInvalidWhitelistEntry("https://123.*.156.145");
        assertInvalidWhitelistEntry("https://123.1*.0.156");
    }

    private void assertInvalidWhitelistEntry(String wlUrlStr) {
        Assert.assertFalse(parseEntry(wlUrlStr).isValid());
    }

    private void assertValidWhitelistEntry(String wlUrlStr) {
        Assert.assertTrue(parseEntry(wlUrlStr).isValid());
    }

    @Test
    public void urlInWhiteList() throws Exception {
        List<WhitelistEntry> wList = parseWhiteListEntries(
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

        // "https://rfry.m-b.com"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://rfy.m-b.com:443/some_URL"), wList));

        // "https://rfy.m-b.com:443"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://rfy.m-b.com:445/some_URL"), wList));

        //  "https://*.m-b.com"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://rfyA.m-b.com:443/some_URL"), wList));

        // "https://rfy.*.com"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://rfy.m-b1.com:443/some_URL"), wList));

        //  "https://*.m-b.com"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://rfy.m-b.org:443/some_URL"), wList));
        Assert.assertTrue(isUrlInWhitelist(new URL("https://rfy.m-b.com:443/some_URL"), wList));

        // "https://*.*.*:446"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://rfy1.m-b1.org:446/some_URL"), wList));

        // "https://*:447"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://rfy1.m-b1.org:447/some_URL"), wList));

        // "https://*:446"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://rfy1.m-b1.com:445/some_URL"), wList));

        // "https://*.mydomain.com"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://abc.mydomain.com:443/some_URL"), wList));

        // "https://*.mydomain.com"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://abc.mydomain.com:444/some_URL"), wList));

        // "http://*.mydomain.com"
        Assert.assertTrue(isUrlInWhitelist(new URL("http://abc.mydomain.com:80/some_URL"), wList));

        // "http://*.mydomain.com"
        Assert.assertFalse(isUrlInWhitelist(new URL("http://abc.mydomain.com:81/some_URL"), wList));

        // "*.cintra.net"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://abc.cintra.net:443/some_URL"), wList));
        Assert.assertFalse(isUrlInWhitelist(new URL("http://abc.cintra.net:443/some_URL"), wList));

        // "*.dmlr.com"
        Assert.assertTrue(isUrlInWhitelist(new URL("https://abc.dmlr.com:443/some_URL"), wList));

        // "*.dmlr.com"
        Assert.assertFalse(isUrlInWhitelist(new URL("https://abc.dmlr.com:44/some_URL"), wList));
    }

    @Test
    public void demoUrlInWhiteList() throws Exception {
        List<WhitelistEntry> wList = parseWhiteListEntries(
                "docs.oracle.com",
                "*.oracle.org",
                "docs.*.net"
        );

        URL url = new URL("https://docs.oracle.com/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://any.oracle.org:443/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://any.one.oracle.org:443/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://any.net:443/j2se/tutorial");
        Assert.assertFalse(isUrlInWhitelist(url, wList));

        url = new URL("https://docs.any.net:443/j2se/tutorial");
        Assert.assertFalse(isUrlInWhitelist(url, wList));
    }

    @Test
    public void ipUrlInWhiteList() throws Exception {
        List<WhitelistEntry> wList = parseWhiteListEntries(
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

        URL url = new URL("https://123.134.145.156/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://123.134.145.156:443/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://124.134.145.156:443");
        Assert.assertFalse(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:80/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://125.134.145.157:90/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://123.134.145.156:167/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:333/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.158:333/j2se/tutorial");
        Assert.assertFalse(isUrlInWhitelist(url, wList));

        url = new URL("https://126.134.145.156:333/j2se/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:335/abc/efg");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://124.134.145.156:336/abcd/efg");
        Assert.assertFalse(isUrlInWhitelist(url, wList));
    }

    @Test
    public void wildCard() throws Exception {
        List<WhitelistEntry> wList = parseWhiteListEntries(
                "*",
                "http://*"
        );

        URL url = new URL("https://123.134.145.156/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("https://abc.efg.com/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://abc.com/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));

        url = new URL("http://123.134.145.156:80/tutorial");
        Assert.assertTrue(isUrlInWhitelist(url, wList));
    }

    private static List<WhitelistEntry> parseWhiteListEntries(String... rawEntries) {
        return Stream.of(rawEntries)
                .filter(s -> !StringUtils.isBlank(s))
                .map(UrlWhiteListUtils::parseEntry)
                .collect(Collectors.toList());
    }
}
