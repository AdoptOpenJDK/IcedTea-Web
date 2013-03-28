package net.sourceforge.jnlp.util;

import static org.junit.Assert.*;

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
}
