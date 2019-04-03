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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;

import org.junit.Test;

/*
 * All JNLP tests expect to be unable to perform restricted actions,
 * such as reading from System.getProperty. This is because partially signed
 * applet support (PR1592) is enabled *only* for browser plugin applets, and
 * not for JNLP applets or applications. The expected result in all JNLP
 * tests is therefore an AccessControlException. Most plugin applets expect
 * AccessControlExceptions as well, since they test to ensure that the signed
 * JAR(s) of an applet cannot leak information to unsigned parts of the applet
 * nor allow them to perform restricted actions. These tests also similarly
 * expect AccessControlExceptions. The only tests that expect to be able to
 * read successfully from System.getProperty are the plugin applet tests
 * where the signed JAR reads the data and then does not in any way transfer
 * it to the unsigned code, except when the signed JAR method specifically uses
 * AccessController.doPrivileged. These are "testSignedReadProperties",
 * "testSignedReadPropertiesDoPrivileged", and
 * "testUnsignedAttacksSignedDoPrivileged2".
 */
public class MixedSigningAppletSignedTests extends BrowserTest {

    private static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    private static final String USER_HOME = System.getProperty("user.home");

    private static final String HREF_TARGET = "JNLP_HREF", APP_TYPE_TARGET = "APP_TYPE_TARGET", ARG_TARGET = "PARAM_ARG_TARGET",
            SECURITY_TARGET = "SECURITY_TAG_TARGET";

    private static final String JNLP_SECURITY_TAG = "<security><all-permissions/></security>";

    private static ProcessResult runJnlpApplet(String arg, boolean security) throws Exception {
        String argString = "<param name=\"testName\" value=\"" + arg + "\"/>";
        String href = "MixedSigningApplet-Applet-" + arg + ".jnlp";
        return prepareJnlpFromTemplate(href, "applet-desc", argString, security);
    }

    private static ProcessResult runJnlpApplication(String arg, boolean security) throws Exception {
        String argString = "<argument>\"" + arg + "\"</argument>";
        String href = "MixedSigningApplet-Application-" + arg + ".jnlp";
        return prepareJnlpFromTemplate(href, "application-desc", argString, security);
    }

    private static ProcessResult prepareJnlpFromTemplate(String href, String type, String arg, boolean security) throws Exception {
        File src = new File(server.getDir(), "MixedSigningApplet.jnlp");
        File dest = new File(server.getDir(), href);
        String srcJnlp = ServerAccess.getContentOfStream(new FileInputStream(src));
        String resultJnlp = srcJnlp.replaceAll(HREF_TARGET, href)
            .replaceAll(APP_TYPE_TARGET, type)
            .replaceAll(ARG_TARGET, arg)
            .replaceAll(SECURITY_TARGET, security ? JNLP_SECURITY_TAG : "");
        ServerAccess.saveFile(resultJnlp, dest);
        return server.executeJavawsHeadless(href);
    }

    /*
     * All browser tests disabled due to requiring user intervention to run
     * (partially signed dialog will appear)
     */
    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testNonPrivilegedAction() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testNonPrivilegedAction", AutoClose.CLOSE_ON_CORRECT_END);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testNonPrivilegedActionDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testNonPrivilegedActionDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testNonPrivilegedActionDoPrivileged2() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testNonPrivilegedActionDoPrivileged2", AutoClose.CLOSE_ON_CORRECT_END);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReadProperties() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReadProperties", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReadPropertiesDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReadPropertiesDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReadPropertiesDoPrivileged2() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReadPropertiesDoPrivileged2", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedReadProperties() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedReadProperties", AutoClose.CLOSE_ON_CORRECT_END);
        assertContainsUserHome(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedReadPropertiesDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedReadPropertiesDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertContainsUserHome(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedExportPropertiesToUnsigned() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedExportPropertiesToUnsigned", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedExportPropertiesToUnsignedDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedExportPropertiesToUnsignedDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedExportPropertiesToUnsignedDoPrivileged2() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testSignedExportPropertiesToUnsignedDoPrivileged2", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedAttacksSigned() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedAttacksSigned", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedAttacksSignedDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedAttacksSignedDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedAttacksSignedDoPrivileged2() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedAttacksSignedDoPrivileged2", AutoClose.CLOSE_ON_CORRECT_END);
        assertContainsUserHome(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReflectionAttack() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReflectionAttack", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReflectionAttackDoPrivileged() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReflectionAttackDoPrivileged", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @NeedsDisplay
    //@Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedReflectionAttackDoPrivileged2() throws Exception {
        ProcessResult pr = server.executeBrowser("MixedSigningApplet.html?testUnsignedReflectionAttackDoPrivileged2", AutoClose.CLOSE_ON_CORRECT_END);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedAction", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedAction", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedActionDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedActionDoPrivileged", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivileged2JNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedActionDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivileged2JNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testNonPrivilegedActionDoPrivileged2", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReadProperties", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReadProperties", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReadPropertiesDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReadPropertiesDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedReadProperties", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedReadProperties", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedReadPropertiesDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedReadPropertiesDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsigned", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsigned", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsignedDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsignedDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivileged2JNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsignedDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivileged2JNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testSignedExportPropertiesToUnsignedDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSigned", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSigned", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSignedDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSignedDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivileged2JNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSignedDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivileged2JNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedAttacksSignedDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttack", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttack", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivilegedJNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttackDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivilegedJNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttackDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivileged2JNLPAppletWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttackDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivileged2JNLPApplet() throws Exception {
        ProcessResult pr = runJnlpApplet("testUnsignedReflectionAttackDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedAction", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedAction", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedActionDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedActionDoPrivileged", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivileged2JNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedActionDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testNonPrivilegedActionDoPrivileged2JNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testNonPrivilegedActionDoPrivileged2", false);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReadProperties", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReadProperties", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReadPropertiesDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReadPropertiesDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReadPropertiesDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedReadProperties", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedReadProperties", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedReadPropertiesDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedReadPropertiesDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedReadPropertiesDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsigned", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsigned", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsignedDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsignedDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivileged2JNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsignedDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testSignedExportPropertiesToUnsignedDoPrivileged2JNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testSignedExportPropertiesToUnsignedDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSigned", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSigned", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSignedDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSignedDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivileged2JNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSignedDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedAttacksSignedDoPrivileged2JNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedAttacksSignedDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttack", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttack", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivilegedJNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttackDoPrivileged", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivilegedJNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttackDoPrivileged", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivileged2JNLPApplicationWithSecurity() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttackDoPrivileged2", true);
        assertSecurityTagException(pr);
    }

    @Bug(id="PR1592")
    @Test
    public void testUnsignedReflectionAttackDoPrivileged2JNLPApplication() throws Exception {
        ProcessResult pr = runJnlpApplication("testUnsignedReflectionAttackDoPrivileged2", false);
        assertAccessControlException(pr);
        assertCloseString(pr);
    }

    private static void assertProperStart(ProcessResult pr) {
        assertTrue("stdout should contain MixedSigningApplet Applet Running but did not", pr.stdout.contains("MixedSigningApplet Applet Running"));
    }

    private static void assertContainsUserHome(ProcessResult pr) {
        assertTrue("stdout should contain " + USER_HOME + " but did not", pr.stdout.contains(USER_HOME));
    }

    private static void assertAccessControlException(ProcessResult pr) {
        assertTrue("stderr should contain \"AccessControlException: access denied\" but did not", pr.stderr.contains("AccessControlException: access denied"));
    }

    private static void assertSecurityTagException(ProcessResult pr) {
        final String errorMessage = "Cannot grant permissions to unsigned jars. Application requested security permissions, but jars are not signed";
        assertTrue("stderr should contain \"" + errorMessage + "\" but did not.", pr.stderr.contains(errorMessage));
    }

    private static void assertCloseString(ProcessResult pr) {
        assertTrue("stdout should contain " + CLOSE_STRING + " but did not", pr.stdout.contains(CLOSE_STRING));
    }
}
