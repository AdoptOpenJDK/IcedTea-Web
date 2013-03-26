package net.sourceforge.jnlp.security.appletextendedsecurity;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class UnsignedAppletTrustConfirmationTest {

    private List<String> toList(String ... parts) {
        List<String> list = new ArrayList<String>();
        for (String part : parts) {
            list.add(part);
        }
        return list;
    }

    @Test
    public void testToRelativePaths() throws Exception {
        /* Absolute -> Relative */
        assertEquals(toList("test.jar"), 
                UnsignedAppletTrustConfirmation.toRelativePaths(toList("http://example.com/test.jar"), "http://example.com/"));

        /* Relative is unchanged */
        assertEquals(toList("test.jar"), 
                UnsignedAppletTrustConfirmation.toRelativePaths(toList("test.jar"), "http://example.com/"));

        /* Different root URL is unchanged */
        assertEquals(toList("http://example2.com/test.jar"), 
                UnsignedAppletTrustConfirmation.toRelativePaths(toList("http://example2.com/test.jar"), "http://example.com/"));

        /* Path with invalid URL characters is handled */
        assertEquals(toList("test .jar"), 
                UnsignedAppletTrustConfirmation.toRelativePaths(toList("http://example.com/test .jar"), "http://example.com/"));
    }

    @Test
    public void testNormalizeUrlAndStripParams() throws Exception {
        /* Test that URL is normalized (encoded if not already encoded, leading whitespace trimmed, etc) */
        assertEquals("http://example.com/%20test%20test",
                UnsignedAppletTrustConfirmation.normalizeUrlAndStripParams(new URL("http://example.com/ test%20test  ")).toString());
        /* Test that a URL without '?' is left unchanged */
        assertEquals("http://example.com/test",
                UnsignedAppletTrustConfirmation.normalizeUrlAndStripParams(new URL("http://example.com/test")).toString());
        /* Test that parts of a URL that come after '?' are stripped */
        assertEquals("http://example.com/test",
                UnsignedAppletTrustConfirmation.normalizeUrlAndStripParams(new URL("http://example.com/test?test=test")).toString());
        /* Test that everything after the first '?' is stripped */
        assertEquals("http://example.com/test",
                UnsignedAppletTrustConfirmation.normalizeUrlAndStripParams(new URL("http://example.com/test?http://example.com/?test")).toString());

        /* Test normalization + stripping */
        assertEquals("http://example.com/%20test%20test",
                UnsignedAppletTrustConfirmation.normalizeUrlAndStripParams(new URL("http://www.example.com/ test%20test  ?test=test")).toString());
    }
}