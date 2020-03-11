package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFile;
import org.junit.Test;

import java.awt.AWTPermission;
import java.net.URI;
import java.security.PermissionCollection;

import static org.junit.Assert.*;

public class PermissionsManagerTest {
    @Test
    public void testNotNullJnlpFile() throws Exception {
        Throwable t = null;
        try {
            new PermissionsManager(new DummyJNLPFile());
        } catch (Exception ex) {
            t = ex;
        }
        assertNull("securityDesc should not throw exception", t);
    }

    @Test(expected = NullPointerException.class)
    public void testNullJnlpFile() throws Exception {
        new PermissionsManager(null);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final String urlStr = "http://example.com";
        final String result = PermissionsManager.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString2() throws Exception {
        final String urlStr = "http://example.com/";
        final String result = PermissionsManager.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString3() throws Exception {
        final String urlStr = "http://example.com///";
        final String result = PermissionsManager.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostStringWithPort() throws Exception {
        final String urlStr = "http://example.com:8080";
        final String result = PermissionsManager.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com:8080/-";
        assertEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendRecursiveSubdirToCodebaseHostStringWithNull() throws Exception {
        PermissionsManager.appendRecursiveSubdirToCodebaseHostString(null);
    }

    @Test
    public void testGetHostWithSpecifiedPort() throws Exception {
        final URI codebase = new URI("http://example.com");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithFtpScheme() throws Exception {
        final URI codebase = new URI("ftp://example.com");
        final URI expected = new URI("ftp://example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithUserInfo() throws Exception {
        final URI codebase = new URI("http://user:password@example.com");
        final URI expected = new URI("http://user:password@example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithPort() throws Exception {
        final URI codebase = new URI("http://example.com:8080");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithPath() throws Exception {
        final URI codebase = new URI("http://example.com/applet/codebase/");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithAll() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final URI expected = new URI("ftp://user:password@example.com:80");
        assertEquals(expected, PermissionsManager.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test(expected = NullPointerException.class)
    public void testGetHostWithSpecifiedPortWithNull() throws Exception {
        PermissionsManager.getHostWithSpecifiedPort(null, 80);
    }

    @Test
    public void testGetHost() throws Exception {
        final URI codebase = new URI("http://example.com");
        final URI expected = new URI("http://example.com");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test
    public void testGetHostWithFtpScheme() throws Exception {
        final URI codebase = new URI("ftp://example.com");
        final URI expected = new URI("ftp://example.com");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test
    public void testGetHostWithUserInfo() throws Exception {
        final URI codebase = new URI("http://user:password@example.com");
        final URI expected = new URI("http://user:password@example.com");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test
    public void testGetHostWithPort() throws Exception {
        final URI codebase = new URI("http://example.com:8080");
        final URI expected = new URI("http://example.com:8080");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test
    public void testGetHostWithPath() throws Exception {
        final URI codebase = new URI("http://example.com/applet/codebase/");
        final URI expected = new URI("http://example.com");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test
    public void testGetHostWithAll() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final URI expected = new URI("ftp://user:password@example.com:8080");
        assertEquals(expected, PermissionsManager.getHost(codebase));
    }

    @Test(expected = NullPointerException.class)
    public void testGetHostNull() throws Exception {
        PermissionsManager.getHost(null);
    }

    @Test
    public void testGetHostWithAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final String expected = "ftp://user:password@example.com:8080/-";
        assertEquals(expected, PermissionsManager.appendRecursiveSubdirToCodebaseHostString(PermissionsManager.getHost(codebase).toString()));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final String expected = "ftp://user:password@example.com:80/-";
        assertEquals(expected, PermissionsManager.appendRecursiveSubdirToCodebaseHostString(PermissionsManager.getHostWithSpecifiedPort(codebase, 80).toString()));
    }

    @Test (expected = SecurityException.class)
    public void throwExpectionAsAttemptToAddPermissionToReadOnlyJ2EEPermissionCollection() {
        final PermissionCollection permissions = PermissionsManager.getJ2EEPermissions();
        permissions.add(new AWTPermission("someExtraPermission"));
    }

    @Test (expected = SecurityException.class)
    public void throwExpectionAsAttemptToAddPermissionToReadOnlyJnlpRiaPermissionCollection() {
        final PermissionCollection permissions = PermissionsManager.getJnlpRiaPermissions();
        permissions.add(new AWTPermission("someExtraPermission"));
    }
}