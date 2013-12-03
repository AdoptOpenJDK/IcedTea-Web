/* MixedSigningAppletSignedTests.java
Copyright (C) 2013 Red Hat, Inc.

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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;

import static org.junit.Assert.*;
import org.junit.Test;

/* See also simple/MixedSigningApplet */
public class MixedSigningAppletSignedTests extends BrowserTest {

    private static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    private static final String userHome = System.getProperty("user.home");

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testNonPrivilegedAction() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testNonPrivilegedAction", AutoClose.CLOSE_ON_CORRECT_END);
        assertTrue("stdout should contain MixedSigningApplet Applet Running but did not", pr.stdout.contains("MixedSigningApplet Applet Running"));
        assertCloseString(pr);
    }

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReadProperties() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReadProperties", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedReadProperties() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedReadProperties", AutoClose.CLOSE_ON_CORRECT_END);
        assertTrue("stdout should contain " + userHome + " but did not", pr.stdout.contains(userHome));
        assertCloseString(pr);
    }

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedExportPropertiesToUnsigned() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedExportPropertiesToUnsigned", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedAttacksSigned() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedAttacksSigned", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @NeedsDisplay
    // @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReflectionAttack() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReflectionAttack", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Test
    public void testNonPrivilegedActionJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-1.jnlp");
        assertTrue("stdout should contain MixedSigningApplet Applet Running but did not", pr.stdout.contains("MixedSigningApplet Applet Running"));
        assertCloseString(pr);
    }

    @Test
    public void testUnsignedReadPropertiesJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-2.jnlp");
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Test
    public void testSignedReadPropertiesJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-3.jnlp");
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Test
    public void testSignedExportPropertiesToUnsignedJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-4.jnlp");
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Test
    public void testUnsignedAttacksSignedJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-5.jnlp");
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Test
    public void testUnsignedReflectionAttackJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("MixedSigningApplet-6.jnlp");
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    private static void assertAccessControlException(ProcessResult pr) {
        assertTrue("stderr should contain AccessControlException but did not", pr.stderr.contains("AccessControlException"));
    }

    private static void assertCloseString(ProcessResult pr) {
        assertTrue("stdout should contain " + appletCloseString + " but did not", pr.stdout.contains(appletCloseString));
    }
}
