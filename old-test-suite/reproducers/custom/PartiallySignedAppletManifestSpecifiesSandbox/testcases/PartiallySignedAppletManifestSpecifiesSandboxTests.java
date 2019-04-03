/* SignedAppletManifestSpecifiesSandboxTests.java
Copyright (C) 2014 Red Hat, Inc.

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

import java.io.IOException;
import java.util.AbstractMap;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PartiallySignedAppletManifestSpecifiesSandboxTests extends BrowserTest {

    private static final String STARTING_STRING = "PartiallySignedAppletManifestSpecifiesSandbox Applet Starting";
    private static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    private static final String UNSIGNED_EXPECTED_STDOUT_PRINT = "UNSIGNED: SECURITY EXCEPTION";
	private static final String SIGNED_EXPECTED_STDOUT_PRINT = "IS SIGNED: SECURITY EXCEPTION";
    private static final String STACKTRACE_EX_TYPE = "AccessControlException";
    private static final String STACKTRACE_NOT_GRANT_PERMISSIONS_TYPE = "Cannot grant permissions to unsigned jars";
    private static final String USER_HOME = System.getProperty("user.home");

    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier modifier;
    
    @BeforeClass
    public static void setupDeploymentProperties() throws IOException {
        modifier = new DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier(
                new AbstractMap.SimpleEntry<>(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString()),
                new AbstractMap.SimpleEntry<>(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars()
                )
        );
        modifier.setProperties();
    }

    @AfterClass
    public static void setbackDeploymentProperties() throws IOException {
        modifier.restoreProperties();
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedAppletWithSandboxPermissionsInManifestLaunchWithUnsignedHTMLApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesSandboxUnsigned.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedAppletWithSandboxPermissionsInManifestLaunchWithSignedHTMLApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesSandboxSigned.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithSandboxPermissionsInManifestLaunchWithUnsignedApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesSandboxUnsignedJNLPhref.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithSandboxPermissionsInManifestLaunchWithSignedApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesSandboxSignedJNLPhref.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithSandboxPermissionsInManifestLaunchSigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedAppletManifestSpecifiesSandboxSigned.jnlp");
       assertProperResult(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithSandboxPermissionsInManifestLaunchUnsigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedAppletManifestSpecifiesSandboxUnsigned.jnlp");
       assertProperResult(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPApplicationWithSandboxPermissionsInManifestLaunchSigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedApplicationManifestSpecifiesSandboxSigned.jnlp");
       assertProperResult(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPApplicationWithSandboxPermissionsInManifestLaunchUnsigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedApplicationManifestSpecifiesSandboxUnsigned.jnlp");
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedAppletWithAllPermissionsInManifestLaunchWithUnsignedHTMLApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesAllPermissionUnsigned.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedAppletWithAllPermissionsInManifestLaunchWithSignedHTMLApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesAllPermissionSigned.html", new AutoOkClosingListener(), null);
        assertProperResultSignedAllowedAccess(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithAllPermissionsInManifestLaunchWithUnsignedApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesAllPermissionUnsignedJNLPhref.html", new AutoOkClosingListener(), null);
       assertProperResult(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithAllPermissionsInManifestLaunchWithSignedApp() throws Exception {
        final ProcessResult pr = server.executeBrowser("PartiallySignedAppletManifestSpecifiesAllPermissionSignedJNLPhref.html", new AutoOkClosingListener(), null);
        assertProperResultSignedAllowedAccess(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithAllPermissionsInManifestLaunchSigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedAppletManifestSpecifiesAllPermissionSigned.jnlp");
        assertJNLPPartialSignedNotRun(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPAppletWithAllPermissionsInManifestLaunchUnsigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedAppletManifestSpecifiesAllPermissionUnsigned.jnlp");
        assertJNLPPartialSignedNotRun(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPApplicationWithAllPermissionsInManifestLaunchSigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedApplicationManifestSpecifiesAllPermissionSigned.jnlp");
        assertJNLPPartialSignedNotRun(pr);
    }

    @Test
    @Bug(id="PR1769")
    public void testPartiallySignedJNLPApplicationWithAllPermissionsInManifestLaunchUnsigned() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("PartiallySignedApplicationManifestSpecifiesAllPermissionUnsigned.jnlp");
        assertJNLPPartialSignedNotRun(pr);
    }

    private static void assertProperResult(ProcessResult pr) {
        assertCorrectInit(pr);
        assertStdoutContainsUnsignedSecurityException(pr);
        assertStdoutContainsSignedSecurityException(pr);
        assertStderrContainsStacktrace(pr);
        assertCorrectClose(pr);
    }

    private static void assertProperResultSignedAllowedAccess(ProcessResult pr) {
        assertCorrectInit(pr);
        assertStdoutContainsUnsignedSecurityException(pr);
        assertStdoutContainsUserHome(pr);
        assertStderrContainsStacktrace(pr);
        assertCorrectClose(pr);
    }

    private static void assertJNLPPartialSignedNotRun(ProcessResult pr) {
        assertTrue("Applet should have not allowed the Partially Signed JNLP to launch", pr.stderr.contains(STACKTRACE_NOT_GRANT_PERMISSIONS_TYPE));
    }
    private static void assertCorrectInit(ProcessResult pr) {
        assertTrue("Applet should have initialized", pr.stdout.contains(STARTING_STRING));
    }

    private static void assertCorrectClose(ProcessResult pr) {
        assertTrue("Applet should have printed its exit string", pr.stdout.contains(CLOSE_STRING));
    }

    private static void assertStderrContainsStacktrace(ProcessResult pr) {
        assertTrue("Applet should have produced an AccessControlException stacktrace", pr.stderr.contains(STACKTRACE_EX_TYPE));
    }

    private static void assertStdoutContainsUnsignedSecurityException(ProcessResult pr) {
        assertTrue("Applet should have printed UNSIGNED: SECURITY EXCEPTION", pr.stdout.contains(UNSIGNED_EXPECTED_STDOUT_PRINT));
    }

    private static void assertStdoutContainsSignedSecurityException(ProcessResult pr) {
        assertTrue("Applet should have printed SIGNED: SECURITY EXCEPTION", pr.stdout.contains(SIGNED_EXPECTED_STDOUT_PRINT));
    }

    private static void assertStdoutContainsUserHome(ProcessResult pr) {
        assertTrue("Applet should have printed "+USER_HOME, pr.stdout.contains(USER_HOME));
    }
}
