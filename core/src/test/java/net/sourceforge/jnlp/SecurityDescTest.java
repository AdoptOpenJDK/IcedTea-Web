/* 
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
 */
package net.sourceforge.jnlp;

import java.net.URI;
import net.sourceforge.jnlp.mock.DummyJNLPFile;
import org.junit.Test;

import static org.junit.Assert.*;

public class SecurityDescTest {

    @Test
    public void testNotNullJnlpFile() throws Exception {
        Throwable t = null;
        try {
            new SecurityDesc(new DummyJNLPFile(), SecurityDesc.SANDBOX_PERMISSIONS, null);
        } catch (Exception ex) {
            t = ex;
        }
        assertNull("securityDesc should not throw exception", t);
    }

    @Test(expected = NullPointerException.class)
    public void testNullJnlpFile() throws Exception {
        new SecurityDesc(null, SecurityDesc.SANDBOX_PERMISSIONS, null);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final String urlStr = "http://example.com";
        final String result = SecurityDesc.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString2() throws Exception {
        final String urlStr = "http://example.com/";
        final String result = SecurityDesc.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostString3() throws Exception {
        final String urlStr = "http://example.com///";
        final String result = SecurityDesc.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com/-";
        assertEquals(expected, result);
    }

    @Test
    public void testAppendRecursiveSubdirToCodebaseHostStringWithPort() throws Exception {
        final String urlStr = "http://example.com:8080";
        final String result = SecurityDesc.appendRecursiveSubdirToCodebaseHostString(urlStr);
        final String expected = "http://example.com:8080/-";
        assertEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendRecursiveSubdirToCodebaseHostStringWithNull() throws Exception {
        SecurityDesc.appendRecursiveSubdirToCodebaseHostString(null);
    }

    @Test
    public void testGetHostWithSpecifiedPort() throws Exception {
        final URI codebase = new URI("http://example.com");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithFtpScheme() throws Exception {
        final URI codebase = new URI("ftp://example.com");
        final URI expected = new URI("ftp://example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithUserInfo() throws Exception {
        final URI codebase = new URI("http://user:password@example.com");
        final URI expected = new URI("http://user:password@example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithPort() throws Exception {
        final URI codebase = new URI("http://example.com:8080");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithPath() throws Exception {
        final URI codebase = new URI("http://example.com/applet/codebase/");
        final URI expected = new URI("http://example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithAll() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final URI expected = new URI("ftp://user:password@example.com:80");
        assertEquals(expected, SecurityDesc.getHostWithSpecifiedPort(codebase, 80));
    }

    @Test(expected = NullPointerException.class)
    public void testGetHostWithSpecifiedPortWithNull() throws Exception {
        SecurityDesc.getHostWithSpecifiedPort(null, 80);
    }

    @Test
    public void testGetHost() throws Exception {
        final URI codebase = new URI("http://example.com");
        final URI expected = new URI("http://example.com");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test
    public void testGetHostWithFtpScheme() throws Exception {
        final URI codebase = new URI("ftp://example.com");
        final URI expected = new URI("ftp://example.com");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test
    public void testGetHostWithUserInfo() throws Exception {
        final URI codebase = new URI("http://user:password@example.com");
        final URI expected = new URI("http://user:password@example.com");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test
    public void testGetHostWithPort() throws Exception {
        final URI codebase = new URI("http://example.com:8080");
        final URI expected = new URI("http://example.com:8080");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test
    public void testGetHostWithPath() throws Exception {
        final URI codebase = new URI("http://example.com/applet/codebase/");
        final URI expected = new URI("http://example.com");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test
    public void testGetHostWithAll() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final URI expected = new URI("ftp://user:password@example.com:8080");
        assertEquals(expected, SecurityDesc.getHost(codebase));
    }

    @Test(expected = NullPointerException.class)
    public void testGetHostNull() throws Exception {
        SecurityDesc.getHost(null);
    }

    @Test
    public void testGetHostWithAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final String expected = "ftp://user:password@example.com:8080/-";
        assertEquals(expected, SecurityDesc.appendRecursiveSubdirToCodebaseHostString(SecurityDesc.getHost(codebase).toString()));
    }

    @Test
    public void testGetHostWithSpecifiedPortWithAppendRecursiveSubdirToCodebaseHostString() throws Exception {
        final URI codebase = new URI("ftp://user:password@example.com:8080/applet/codebase/");
        final String expected = "ftp://user:password@example.com:80/-";
        assertEquals(expected, SecurityDesc.appendRecursiveSubdirToCodebaseHostString(SecurityDesc.getHostWithSpecifiedPort(codebase, 80).toString()));
    }

}
