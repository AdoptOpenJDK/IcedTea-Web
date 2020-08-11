package net.sourceforge.jnlp.util.whitelist;

import org.junit.Test;

import java.net.URL;

import static net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils.parseEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WhitelistEntryTest {

    @Test
    public void invalidEntryShouldMatchNothing() throws Exception {
        final WhitelistEntry invalidEntry = parseEntry("");

        assertInvalid(invalidEntry);

        assertNoMatch(invalidEntry, null);
        assertNoMatch(invalidEntry, "http://localhost");
        assertNoMatch(invalidEntry, "https://192.168.1.1");
    }

    @Test
    public void exactEntryShouldOnlyMatchExactUrl() throws Exception {
        final WhitelistEntry validEntry = parseEntry("http://test.com:88");

        assertMatch(validEntry, "http://test.com:88");

        assertNoMatch(validEntry, "https://test.com:88"); // wrong protocol
        assertNoMatch(validEntry, "http://test2.com:88"); // wrong host
        assertNoMatch(validEntry, "http://test.com:888"); // wrong port
    }

    @Test
    public void defaultPortShouldMatchExplicitAndAbsentValue() throws Exception {
        final WhitelistEntry explicitHttpEntry = parseEntry("http://test.com:80");
        final WhitelistEntry implicitHttpEntry = parseEntry("http://test.com");
        final WhitelistEntry explicitHttpsEntry = parseEntry("https://test.com:443");
        final WhitelistEntry implicitHttpsEntry = parseEntry("https://test.com");

        assertMatch(explicitHttpEntry, "http://test.com:80");
        assertMatch(explicitHttpEntry, "http://test.com");
        assertMatch(implicitHttpEntry, "http://test.com:80");
        assertMatch(implicitHttpEntry, "http://test.com");

        assertNoMatch(explicitHttpEntry, "http://test.com:88"); // wrong port
        assertNoMatch(implicitHttpEntry, "http://test.com:88"); // wrong port

        assertMatch(explicitHttpsEntry, "https://test.com:443");
        assertMatch(explicitHttpsEntry, "https://test.com");
        assertMatch(implicitHttpsEntry, "https://test.com:443");
        assertMatch(implicitHttpsEntry, "https://test.com");

        assertNoMatch(explicitHttpsEntry, "https://test.com:444"); // wrong port
        assertNoMatch(implicitHttpsEntry, "https://test.com:444"); // wrong port
    }

    @Test
    public void wildcardPortShouldMatchAnyPort() throws Exception {
        final WhitelistEntry validEntry = parseEntry("http://test.com:*");

        assertMatch(validEntry, "http://test.com");
        assertMatch(validEntry, "http://test.com:80");
        assertMatch(validEntry, "http://test.com:443");
        assertMatch(validEntry, "http://test.com:1");
        assertMatch(validEntry, "http://test.com:10");
        assertMatch(validEntry, "http://test.com:100");
        assertMatch(validEntry, "http://test.com:1000");
        assertMatch(validEntry, "http://test.com:10000");
    }

    @Test
    public void wildcardHostPartShouldMatchAnyPrefix() throws Exception {
        final WhitelistEntry validEntry = parseEntry("http://*.test.com");

        assertMatch(validEntry, "http://sub.test.com");
        // FIXME: assertMatch(validEntry, "http://sub.sub.test.com");

        assertNoMatch(validEntry, "http://test.com"); // missing subdomain
        assertNoMatch(validEntry, "http://sub.test2.com"); // wrong top level domain
    }

    @Test
    public void wildcardHostShouldMatchAnyHost() throws Exception {
        final WhitelistEntry validEntry = parseEntry("https://*:456");

        assertMatch(validEntry, "https://test.com:456");
        assertMatch(validEntry, "https://probe.net:456");
    }

    @Test
    public void defaultProtocolShouldMatchOnlyHttps() throws Exception {
        final WhitelistEntry validEntry = parseEntry("test.com");

        assertMatch(validEntry, "https://test.com");

        assertNoMatch(validEntry, "http://test.com"); // wrong protocol
        assertNoMatch(validEntry, "ftp://test.com"); // wrong protocol
    }

    private void assertInvalid(WhitelistEntry entry) {
        assertFalse(entry.isValid());
    }

    private void assertMatch(WhitelistEntry entry, String urlString) throws Exception {
        final URL url = urlString != null ? new URL(urlString) : null;
        assertTrue("Expected " + urlString + " to match " + entry.getRawWhitelistEntry(), entry.matches(url));
    }

    private void assertNoMatch(WhitelistEntry entry, String urlString) throws Exception {
        final URL url = urlString != null ? new URL(urlString) : null;
        assertFalse(entry.matches(url));
    }
}
