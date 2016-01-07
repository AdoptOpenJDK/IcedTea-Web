/*
   Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package net.sourceforge.jnlp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import net.sourceforge.jnlp.annotations.KnownToFail;
import org.junit.Assert;

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

        // PR1465: Test that RFC2396-compliant URLs are not touched
        // Example taken from bug report: http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=1465
        String rfc2396Valid = "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/JICA-sicaN.jar";
        assertEquals(rfc2396Valid,
                UrlUtils.normalizeUrl(new URL(rfc2396Valid)).toString());
    }

    @Test
    public void testIsValidRFC2396Url() throws Exception {
        String rfc2396Valid = "https://example.com/,foo=bar+baz/JICA-sicaN.jar";
        assertTrue(UrlUtils.isValidRFC2396Url(new URL(rfc2396Valid)));

        // These should invalidate the URL
        // See http://www.ietf.org/rfc/rfc2396.txt (2.4.3. Excluded US-ASCII Characters)
        char[] invalidCharacters = {'<', '>', '%', '"', };
        for (char chr : invalidCharacters) {
            assertFalse("validation failed with '" + chr + "'",UrlUtils.isValidRFC2396Url(new URL(rfc2396Valid + chr)));
        }
        //special test for space inisde. Space at the end can be trimmed
        assertFalse("validation failed with '" + ' ' + "'",UrlUtils.isValidRFC2396Url(new URL("https://example.com/,foo=bar+ba z/JICA-sicaN.jar")));
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
            URL notEncodedUrl = testFile.toURI().toURL();
            URL encodedUrl = testFile.toURI().toURL();
            assertEquals(testFile, UrlUtils.decodeUrlAsFile(notEncodedUrl));
            assertEquals(testFile, UrlUtils.decodeUrlAsFile(encodedUrl));
        }
    }
    
     
    @Test
    public void testNormalizeUrlSlashStrings() throws Exception {
        String u11 = UrlUtils.sanitizeLastSlash("http://aa.bb/aaa/bbb////");
        String u22 = UrlUtils.sanitizeLastSlash("http://aa.bb/aaa/bbb");
        assertEquals(u11, u22);
        assertEquals(u11, "http://aa.bb/aaa/bbb");

        String u1 = UrlUtils.sanitizeLastSlash(("http://aa.bb/aaa\\bbb\\"));
        String u2 = UrlUtils.sanitizeLastSlash(("http://aa.bb/aaa\\bbb"));
        assertEquals(u1, u2);
        assertEquals(u1, ("http://aa.bb/aaa\\bbb"));
    }
    
    @Test
    public void testNormalizeUrlSlashUrls() throws Exception {
        URL u11 = UrlUtils.sanitizeLastSlash(new URL("http://aa.bb/aaa/bbb////"));
        URL u22 = UrlUtils.sanitizeLastSlash(new URL("http://aa.bb/aaa/bbb"));
        assertEquals(u11, u22);
        assertEquals(u11, new URL("http://aa.bb/aaa/bbb"));

        URL u1 = UrlUtils.sanitizeLastSlash(new URL("http://aa.bb/aaa\\bbb\\"));
        URL u2 = UrlUtils.sanitizeLastSlash(new URL("http://aa.bb/aaa\\bbb"));
        assertEquals(u1, u2);
        assertEquals(u1, new URL("http://aa.bb/aaa\\bbb"));
    }

    @Test
    public void testEqualsIgnoreLastSlash() throws Exception {
        URL u11 = (new URL("http://aa.bb/aaa/bbb////"));
        URL u22 = (new URL("http://aa.bb/aaa/bbb"));
        assertTrue(UrlUtils.equalsIgnoreLastSlash(u11, u22));
        assertTrue(UrlUtils.equalsIgnoreLastSlash(u11, new URL("http://aa.bb/aaa/bbb")));

        URL u1 = (new URL("http://aa.bb/aaa\\bbb\\"));
        URL u2 = (new URL("http://aa.bb/aaa\\bbb"));
        assertTrue(UrlUtils.equalsIgnoreLastSlash(u1, u2));
        assertTrue(UrlUtils.equalsIgnoreLastSlash(u1, new URL("http://aa.bb/aaa\\bbb")));

        assertTrue(UrlUtils.equalsIgnoreLastSlash(new URL("http://aa.bb/aaa\\bbb\\"), new URL("http://aa.bb/aaa\\bbb/")));
        assertFalse(UrlUtils.equalsIgnoreLastSlash(new URL("http://aa.bb/aaa\\bbb\\"), new URL("http://aa.bb/aaa/bbb/")));
    }

    @Test
    public void removeFileName1() throws Exception {
        URL l1 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr/jar.jar"));
        assertEquals(l1, new URL("http://aaa.bb/xyz/hchkr"));

        URL l2 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr/"));
        assertEquals(l2, new URL("http://aaa.bb/xyz/hchkr"));

        URL l3 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr"));
        assertEquals(l3, new URL("http://aaa.bb/xyz"));

        URL l4 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/jar.jar"));
        assertEquals(l4, new URL("http://aaa.bb/xyz"));

        URL l5 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/"));
        assertEquals(l5, new URL("http://aaa.bb/xyz"));

        URL l6 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz"));
        assertEquals(l6, new URL("http://aaa.bb"));

        URL l7 = UrlUtils.removeFileName(new URL("http://aaa.bb/jar.jar"));
        assertEquals(l7, new URL("http://aaa.bb"));

        URL l8 = UrlUtils.removeFileName(new URL("http://aaa.bb/"));
        assertEquals(l8, new URL("http://aaa.bb"));

        URL l9 = UrlUtils.removeFileName(new URL("http://aaa.bb"));
        assertEquals(l9, new URL("http://aaa.bb"));

    }

    public void removeFileName2() throws Exception {
        URL l1 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz\\hchkr\\jar.jar"));
        assertEquals(l1, new URL("http://aaa.bb/xyz\\hchkr"));

        URL l2  = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz\\hchkr\\"));
         assertEquals(l2, new URL("http://aaa.bb/xyz\\hchkr"));
         
         URL l3  = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz\\hchkr"));
         assertEquals(l3, new URL("http://aaa.bb/xyz"));
         
         URL l4  = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz\\jar.jar"));
         assertEquals(l4, new URL("http://aaa.bb/xyz"));
         
         URL l5  = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz\\"));
         assertEquals(l5, new URL("http://aaa.bb/xyz"));
         
         URL l6  = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz"));
         assertEquals(l6, new URL("http://aaa.bb"));
         
         URL l7  = UrlUtils.removeFileName(new URL("http://aaa.bb/jar.jar"));
         assertEquals(l7, new URL("http://aaa.bb"));
         
         URL l8  = UrlUtils.removeFileName(new URL("http://aaa.bb/"));
         assertEquals(l8, new URL("http://aaa.bb"));
         
         URL l9  = UrlUtils.removeFileName(new URL("http://aaa.bb"));
         assertEquals(l9, new URL("http://aaa.bb"));
        
    }
    
     @Test
    public void removeFileName3() throws Exception {
        URL l1 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr/jar.jar?someParam=some&param=very\\evil\\"));
        assertEquals(l1, new URL("http://aaa.bb/xyz/hchkr"));

        URL l2 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr/?another=Param&param=very/evil/"));
        assertEquals(l2, new URL("http://aaa.bb/xyz/hchkr"));

        URL l3 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/hchkr?stillSomePArams=aa"));
        assertEquals(l3, new URL("http://aaa.bb/xyz"));

        URL l4 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/jar.jar?again=param/bad\\bad/params"));
        assertEquals(l4, new URL("http://aaa.bb/xyz"));

        URL l5 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz/?goingOnWith=params/bad/params"));
        assertEquals(l5, new URL("http://aaa.bb/xyz"));

        URL l6 = UrlUtils.removeFileName(new URL("http://aaa.bb/xyz?someParam=some"));
        assertEquals(l6, new URL("http://aaa.bb"));

        URL l7 = UrlUtils.removeFileName(new URL("http://aaa.bb/jar.jar?someParam=some"));
        assertEquals(l7, new URL("http://aaa.bb"));

        URL l8 = UrlUtils.removeFileName(new URL("http://aaa.bb/?someParam=some"));
        assertEquals(l8, new URL("http://aaa.bb"));

    }
    
    @Test
    public void testUrlEquals() throws Exception {
        final URL n1 = null, n2 = null, u1 = new URL("http://example.com"), u2 = u1, u3 = new URL("http://example.com");
        Assert.assertTrue("Two nulls should be equal", UrlUtils.urlEquals(n1, n2));
        Assert.assertFalse("Null URL should not equal a non-null", UrlUtils.urlEquals(n1, u1));
        Assert.assertTrue("URL should equal itself (same reference)", UrlUtils.urlEquals(u1, u2));
        Assert.assertTrue("URLs should be equal when different reference but the same URL", UrlUtils.urlEquals(u1, u3));
    }

    @Test(expected = NullPointerException.class)
    public void verifyNotNullUrlEqualsThrowsExceptionWhenBothArgumentsAreNull() throws Exception {
        UrlUtils.notNullUrlEquals(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void nverifyNotNullUrlEqualsThrowsExceptionWhenFirstArgumentIsNull() throws Exception {
        UrlUtils.notNullUrlEquals(null, new URL("ftp://aa.bb"));
    }

    @Test(expected = NullPointerException.class)
    public void verifyNotNullUrlEqualsThrowsExceptionWhenSecondArgumentIsNull() throws Exception {
        UrlUtils.notNullUrlEquals(new URL("ftp://aa.bb"), null);
    }

    @Test
    public void notNullUrlValuesEqualsCaseSensitiveIssuesTest() throws Exception {
        final URL u1 = new URL("http://example.com"), u2 = u1, u3 = new URL("HTTP://example.com");
        final URL u11 = new URL("http://example.com/path"), u12 = new URL("http://EXAMPLE.com/path"), u13 = new URL("http://example.com/PATH"), u14 = new URL("http://example.com/path2");
        final URL u21 = new URL("http://example2.com");
        final URL u22 = new URL("ftp://example.com");

        Assert.assertTrue(UrlUtils.notNullUrlEquals(u1, u2));
        Assert.assertTrue(UrlUtils.notNullUrlEquals(u1, u3));

        Assert.assertTrue(UrlUtils.notNullUrlEquals(u11, u12));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u11, u13));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u12, u13));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u11, u14));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u12, u14));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u13, u14));

        Assert.assertFalse(UrlUtils.notNullUrlEquals(u1, u21));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u1, u22));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u21, u22));
    }

    @Test
    //the ports are for some unknown reason ignored from comparsion
    @KnownToFail
    public void notNullUrlComapreWithPorts() throws Exception {
        final URL u1 = new URL("http://example.com:1"), u2 = new URL("http://example.com:1"), u3 = new URL("http://example.com:3");
        Assert.assertTrue(UrlUtils.notNullUrlEquals(u1, u2));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u1, u3));
        Assert.assertFalse(UrlUtils.notNullUrlEquals(u2, u3));
    }

    @Test
    public void testCompareNullableStrings_nulls() throws Exception {
        Assert.assertTrue(UrlUtils.compareNullableStrings(null, null, true));
        Assert.assertTrue(UrlUtils.compareNullableStrings(null, null, false));
        Assert.assertFalse(UrlUtils.compareNullableStrings(null, "blah", true));
        Assert.assertFalse(UrlUtils.compareNullableStrings("blah", null, false));
    }

    @Test
    public void testCompareNullableStrings_values() throws Exception {
        Assert.assertTrue(UrlUtils.compareNullableStrings("aaa", "aaa", true));
        Assert.assertTrue(UrlUtils.compareNullableStrings("aaa", "aaa", false));

        Assert.assertFalse(UrlUtils.compareNullableStrings("aaa", "AAA", false));
        Assert.assertTrue(UrlUtils.compareNullableStrings("aaa", "AAA", true));

        Assert.assertFalse(UrlUtils.compareNullableStrings("AAA", "aaa", false));
        Assert.assertTrue(UrlUtils.compareNullableStrings("AAA", "aaa", true));

        Assert.assertFalse(UrlUtils.compareNullableStrings("aaa", "bbb", true));
        Assert.assertFalse(UrlUtils.compareNullableStrings("aaa", "BBB", false));
        Assert.assertFalse(UrlUtils.compareNullableStrings("BBB", "aaa", false));

    }
    
    @Test
    public void sanitizePortTest() throws MalformedURLException {
        Assert.assertEquals(0, UrlUtils.getSanitizedPort(new URL("http://aaa.cz:0")));
        Assert.assertEquals(1, UrlUtils.getSanitizedPort(new URL("https://aaa.cz:1")));
        Assert.assertEquals(100, UrlUtils.getSanitizedPort(new URL("ftp://aaa.cz:100")));
        //Assert.assertEquals(1001, UrlUtils.getSanitizedPort(new URL("ssh://aaa.cz:1001"))); unknown protocol :(
        //Assert.assertEquals(22, UrlUtils.getSanitizedPort(new URL("ssh://aaa.cz")));
        Assert.assertEquals(80, UrlUtils.getSanitizedPort(new URL("http://aaa.cz")));
        Assert.assertEquals(443, UrlUtils.getSanitizedPort(new URL("https://aaa.cz")));
        Assert.assertEquals(21, UrlUtils.getSanitizedPort(new URL("ftp://aaa.cz")));
        
   }

    @Test
    public void getPortTest() throws MalformedURLException {
        Assert.assertEquals(1, UrlUtils.getPort(new URL("http://aa.bb:1")));
        Assert.assertEquals(10, UrlUtils.getPort(new URL("http://aa.bb:10/aa")));
        Assert.assertEquals(1000, UrlUtils.getPort(new URL("http://aa.bb:1000/aa.fs")));
        Assert.assertEquals(443, UrlUtils.getPort(new URL("https://aa.bb/aa.fs")));
        Assert.assertEquals(80, UrlUtils.getPort(new URL("http://aa.bb")));
        Assert.assertEquals(80, UrlUtils.getPort(new URL("http://aa.bb:80/a/b/c")));
    }

    @Test
    public void getHostAndPortTest() throws MalformedURLException {
        Assert.assertEquals("aa.bb:2", UrlUtils.getHostAndPort(new URL("http://aa.bb:2")));
        Assert.assertEquals("aa.bb:12", UrlUtils.getHostAndPort(new URL("http://aa.bb:12/aa")));
        Assert.assertEquals("aa.bb:1002", UrlUtils.getHostAndPort(new URL("http://aa.bb:1002/aa.fs")));
        Assert.assertEquals("aa.bb:443", UrlUtils.getHostAndPort(new URL("https://aa.bb/aa.fs")));
        Assert.assertEquals("aa.bb:80", UrlUtils.getHostAndPort(new URL("http://aa.bb")));
        Assert.assertEquals("aa.bb:80", UrlUtils.getHostAndPort(new URL("http://aa.bb:80/a/b/c")));
    }
    
    @Test
    public void ensureSlashTailTest() {
        Assert.assertEquals("a/", UrlUtils.ensureSlashTail("a"));
        Assert.assertEquals("aa/a/", UrlUtils.ensureSlashTail("aa/a"));
        Assert.assertEquals("aa/a/", UrlUtils.ensureSlashTail("aa/a/"));
        Assert.assertEquals("/aa/a/", UrlUtils.ensureSlashTail("/aa/a/"));
        Assert.assertEquals("/aa/a/", UrlUtils.ensureSlashTail("/aa/a"));
        
        Assert.assertEquals("aa\\a\\", UrlUtils.ensureSlashTail("aa\\a"));
        Assert.assertEquals("aa\\a\\", UrlUtils.ensureSlashTail("aa\\a\\"));
        Assert.assertEquals("\\aa\\a\\", UrlUtils.ensureSlashTail("\\aa\\a\\"));
        Assert.assertEquals("\\aa\\a\\", UrlUtils.ensureSlashTail("\\aa\\a"));
        
        Assert.assertEquals("\\aa/a/", UrlUtils.ensureSlashTail("\\aa/a"));
        Assert.assertEquals("//aa\\a/", UrlUtils.ensureSlashTail("//aa\\a"));
        Assert.assertEquals("\\aa/a/", UrlUtils.ensureSlashTail("\\aa/a/"));
        Assert.assertEquals("\\aa/a\\", UrlUtils.ensureSlashTail("\\aa/a\\"));
    }
    
     @Test
    public void ensureSlashTailTest3() throws MalformedURLException {
        Assert.assertEquals("http://aa.bb:2/aa/", UrlUtils.ensureSlashTail(new URL("http://aa.bb:2/aa")).toExternalForm());
        Assert.assertEquals("http://aa.bb/aa/", UrlUtils.ensureSlashTail(new URL("http://aa.bb/aa/")).toExternalForm());
    }
}
