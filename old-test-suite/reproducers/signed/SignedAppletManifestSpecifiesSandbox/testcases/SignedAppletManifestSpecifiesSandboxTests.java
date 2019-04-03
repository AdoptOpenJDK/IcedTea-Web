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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SignedAppletManifestSpecifiesSandboxTests extends BrowserTest {

    private static final String STARTING_STRING = "SignedAppletManifestSpecifiesSandbox applet starting";
    private static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    private static final String EXPECTED_STDOUT_PRINT = "SECURITY EXCEPTION";
    private static final String STACKTRACE_EX_TYPE = "AccessControlException";

    private static final String JNLP_EXPECTED_STDOUT = "Initialization Error";
    private static final String JNLP_EXPECTED_STDERR = "net.sourceforge.jnlp.LaunchException";

    private static DeploymentPropertiesModifier deploymentPropertiesModifier;

    @BeforeClass
    public static void setupDeploymentProperties() throws IOException {
        deploymentPropertiesModifier = new DeploymentPropertiesModifier();
        deploymentPropertiesModifier.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString());
    }

    @AfterClass
    public static void setbackDeploymentProperties() throws IOException {
        deploymentPropertiesModifier.restoreProperties();
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testSignedAppletWithSandboxPermissionsInManifestHtml() throws Exception {
        final ProcessResult pr = server.executeBrowser("SignedAppletManifestSpecifiesSandbox.html", new AutoOkClosingListener(), null);
        assertHtmlAppletInitializes(pr);
        assertHtmlAppletSecurityException(pr);
        assertHtmlAppletCloses(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    @Bug(id="PR1769")
    public void testSignedAppletWithSandboxPermissionsInManifestHtmlJnlpHref() throws Exception {
        final ProcessResult pr = server.executeBrowser("SignedAppletManifestSpecifiesSandboxJnlpHref.html", new AutoOkClosingListener(), null);
        assertHtmlAppletInitializes(pr);
        assertHtmlAppletSecurityException(pr);
        assertHtmlAppletCloses(pr);
    }

    /*
     * JNLP applets and applications are not allowed to have mismatched <security> tags and manifest attributes
     */
    @Test
    @Bug(id="PR1769")
    public void testSignedAppletWithSandboxPermissionsInManifestJnlpApplet() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("SignedAppletManifestSpecifiesSandboxApplet.jnlp");
        assertJnlpFailsToLaunch(pr);
    }

    /*
     * JNLP applets and applications are not allowed to have mismatched <security> tags and manifest attributes
     */
    @Test
    @Bug(id="PR1769")
    public void testSignedAppletWithSandboxPermissionsInManifestJnlpApplication() throws Exception {
        final ProcessResult pr = server.executeJavawsHeadless("SignedAppletManifestSpecifiesSandboxApplication.jnlp");
        assertJnlpFailsToLaunch(pr);
    }

    private static void assertHtmlAppletInitializes(final ProcessResult pr) {
        assertTrue("Applet should have initialized", pr.stdout.contains(STARTING_STRING));
    }

    private static void assertHtmlAppletSecurityException(final ProcessResult pr) {
        assertTrue("Applet should have printed SECURITY EXCEPTION", pr.stdout.contains(EXPECTED_STDOUT_PRINT));
        assertTrue("Applet should have produced an AccessControlException stacktrace", pr.stderr.contains(STACKTRACE_EX_TYPE));
    }

    private static void assertHtmlAppletCloses(final ProcessResult pr) {
        assertTrue("Applet should have printed its exit string", pr.stdout.contains(CLOSE_STRING));
    }

    private static void assertJnlpFailsToLaunch(final ProcessResult pr) {
        assertTrue("stdout should have contained Initialization Error", pr.stdout.contains(JNLP_EXPECTED_STDOUT));
        assertTrue("stderr should have contained LaunchException", pr.stderr.contains(JNLP_EXPECTED_STDERR));
    }
}
