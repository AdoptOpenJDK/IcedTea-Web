/* TrustedOnlyAttributeTest.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;

import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

public class TrustedOnlyAttributeTest extends BrowserTest {

    private static final String RUNNING_STRING = "TrustedOnlyAttribute applet running";
    private static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    private static File deployFile;
    private static String attributesCheck;

    @BeforeClass
    public static void setupDeploymentProperties() throws IOException {
        deployFile = PathsAndFiles.USER_DEPLOYMENT_FILE.getFile();
        String properties = FileUtils.loadFileAsString(deployFile);

        for (String line : properties.split("\n")) {
            if (line.contains("deployment.manifest.attribute.check")) {
                attributesCheck = line;
                properties = properties.replace(line, "deployment.manifest.attributes.check=TRUSTED\n");
            }
        }
        if (attributesCheck == null) {
            properties += "deployment.manifest.attributes.check=TRUSTED\n";
        }

        FileUtils.saveFile(properties, deployFile);
    }

    @AfterClass
    public static void setbackDeploymentProperties() throws IOException {
        String properties = FileUtils.loadFileAsString(deployFile);
        if (attributesCheck != null) {
            properties = properties.replace("deployment.manifest.attributes.check=TRUSTED\n", attributesCheck);
        } else {
            properties = properties.replace("deployment.manifest.attributes.check=TRUSTED\n", "");
        }

        FileUtils.saveFile(properties, deployFile);
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedAppletWithManifestAttributeAndNoHtmlSecurity() throws Exception {
        ProcessResult pr = server.executeBrowser("TrustedOnlyAttribute-signed.html", AutoClose.CLOSE_ON_BOTH);
        assertFalse("Applet should not have failed to launch", pr.stderr.contains("LaunchException"));
        assertTrue("Applet should have run", pr.stdout.contains(RUNNING_STRING));
    }

    @Test
    public void testSignedAppletWithManifestAttributeAndNoJnlpSecurity() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("TrustedOnlyAttribute-signed-nosecurity.jnlp");
        assertTrue("Applet should have failed to launch", pr.stderr.contains("LaunchException"));
        assertFalse("Applet should not have run", pr.stdout.contains(RUNNING_STRING));
    }

    @Test
    public void testSignedAppletWithManifestAttributeWithJnlpSecurity() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("TrustedOnlyAttribute-signed-security.jnlp");
        assertFalse("Applet should not have failed to launch", pr.stderr.contains("LaunchException"));
        assertTrue("Applet should have run", pr.stdout.contains(RUNNING_STRING));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testUnsignedAppletWithManifestAttributeAndNoHtmlSecurity() throws Exception {
        ProcessResult pr = server.executeBrowser("TrustedOnlyAttribute-unsigned.html", AutoClose.CLOSE_ON_BOTH);
        assertTrue("Applet should have failed to launch", pr.stderr.contains("LaunchException"));
        assertFalse("Applet should not have run", pr.stdout.contains(RUNNING_STRING));
    }

    @Test
    public void testUnsignedAppletWithManifestAttributeAndNoJnlpSecurity() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("TrustedOnlyAttribute-unsigned-nosecurity.jnlp");
        assertTrue("Applet should have failed to launch", pr.stderr.contains("LaunchException"));
        assertFalse("Applet should not have run", pr.stdout.contains(RUNNING_STRING));
    }

    @Test
    public void testUnsignedAppletWithManifestAttributeWithJnlpSecurity() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("TrustedOnlyAttribute-unsigned-security.jnlp");
        assertTrue("Applet should have failed to launch", pr.stderr.contains("LaunchException"));
        assertFalse("Applet should not have run", pr.stdout.contains(RUNNING_STRING));
    }
}
