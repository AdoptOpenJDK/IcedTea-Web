package net.sourceforge.jnlp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class UrlUtilsTest {

    @Test
    public void testNormalizeUrlAndStripParams() throws Exception {
        /* Test that URL is normalized (encoded if not already encoded, leading whitespace trimmed, etc) */
        assertEquals("http://example.com/%20test%20test",
                UrlUtils.normalizeUrlAndStripParams(new URL("http://example.com/ test%20test  ")).toString());
        /* Test that a URL without '?' is left unchanged */
        assertEquals("http://example.com/test",
                UrlUtils.normalizeUrlAndStripParams(new URL("http://example.com/test")).toString());
        /* Test that parts of a URL that come after '?' are stripped */
        assertEquals("http://example.com/test",
                UrlUtils.normalizeUrlAndStripParams(new URL("http://example.com/test?test=test")).toString());
        /* Test that everything after the first '?' is stripped */
        assertEquals("http://example.com/test",
                UrlUtils.normalizeUrlAndStripParams(new URL("http://example.com/test?http://example.com/?test")).toString());

        /* Test normalization + stripping */
        assertEquals("http://example.com/%20test%20test",
                UrlUtils.normalizeUrlAndStripParams(new URL("http://example.com/ test%20test  ?test=test")).toString());
    }

    @Test
    public void testDecodeUrlQuietly() throws Exception {
        // This is a wrapper over URLDecoder.decode, simple test suffices
        assertEquals("http://example.com/ test test",
                UrlUtils.decodeUrlQuietly(new URL("http://example.com/%20test%20test")).toString());
    }

    @Test
    public void testNormalizeUrl() throws Exception {
        boolean[] encodeFileUrlPossiblities = {false, true};

        // encodeFileUrl flag should have no effect on non-file URLs, but let's be sure.
        for (boolean encodeFileUrl : encodeFileUrlPossiblities ) {
            // Test URL with no previous encoding
            assertEquals("http://example.com/%20test",
                    UrlUtils.normalizeUrl(new URL("http://example.com/ test"), encodeFileUrl).toString());
            // Test partially encoded URL with trailing spaces
            assertEquals("http://example.com/%20test%20test",
                    UrlUtils.normalizeUrl(new URL("http://example.com/ test%20test  "), encodeFileUrl).toString());
        }

        // Test file URL with file URL encoding turned off
        assertFalse("file://example/%20test".equals(
                  UrlUtils.normalizeUrl(new URL("file://example/ test"), false).toString()));

        // Test file URL with file URL encoding turned on
        assertEquals("file://example/%20test",
                  UrlUtils.normalizeUrl(new URL("file://example/ test"), true).toString());
    }

    @Test
    public void testNormalizeUrlQuietly() throws Exception {
        // This is a wrapper over UrlUtils.normalizeUrl(), simple test suffices
        assertEquals("http://example.com/%20test%20test",
                UrlUtils.normalizeUrl(new URL("http://example.com/ test%20test  ")).toString());
    }

    @Test
    public void testDecodeUrlAsFile() throws Exception {
        String[] testPaths = {"/simple", "/ with spaces", "/with /multiple=/ odd characters?"};

        for (String testPath : testPaths) {
            File testFile = new File(testPath);
            URL notEncodedUrl = testFile.toURL();
            URL encodedUrl = testFile.toURI().toURL();
            assertEquals(testFile, UrlUtils.decodeUrlAsFile(notEncodedUrl));
            assertEquals(testFile, UrlUtils.decodeUrlAsFile(encodedUrl));
        }
    }
}