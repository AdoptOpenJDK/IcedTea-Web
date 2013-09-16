package net.sourceforge.jnlp.security.appletextendedsecurity;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UnsignedAppletTrustConfirmationTest {

    @Test
    public void testToRelativePaths() throws Exception {
        /* Absolute -> Relative */
        assertEquals(Arrays.asList("test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example.com/test.jar"), "http://example.com/"));

        /* Relative is unchanged */
        assertEquals(Arrays.asList("test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("test.jar"), "http://example.com/"));

        /* Different root URL is unchanged */
        assertEquals(Arrays.asList("http://example2.com/test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example2.com/test.jar"), "http://example.com/"));

        /* Path with invalid URL characters is handled */
        assertEquals(Arrays.asList("test .jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example.com/test .jar"), "http://example.com/"));
    }
}
