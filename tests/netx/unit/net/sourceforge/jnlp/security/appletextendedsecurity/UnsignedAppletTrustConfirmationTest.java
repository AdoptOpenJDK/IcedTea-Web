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
}