/* SOPBypassHtmlAppletTest.java
   Copyright (C) 2015 Red Hat, Inc.

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

package sopbypasstests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import java.io.File;

import static sopbypasstests.SOPBypassUtil.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SOPBypassSignedHtmlAppletTest extends BrowserTest {

    private static ServerLauncher serverA;
    private static ServerLauncher serverB;
    private static ServerLauncher serverC;
    private static DeploymentPropertiesModifier mod1 = new DeploymentPropertiesModifier();
    private static DeploymentPropertiesModifier mod2 = new DeploymentPropertiesModifier();

    @BeforeClass
    public static void setup() throws Exception {
        serverA = ServerAccess.getIndependentInstance();
        serverB = ServerAccess.getIndependentInstance();
        serverC = ServerAccess.getIndependentInstance();

        File file = mod1.src.getFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        mod1.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.name());
        mod2.setProperties(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.name());
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalAbsoluteArchiveLocalPathCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", server.getUrl("SOPBypassSigned.jar"), server.getUrl("codebase"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalAbsoluteArchiveUnrelatedRemoteCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", server.getUrl("SOPBypassSigned.jar"), serverC.getUrl("codebase"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteAbsoluteArchiveSameRemoteCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", serverC.getUrl("SOPBypassSigned.jar"), serverC.getUrl("codebase"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteAbsoluteArchiveUnrelatedRemoteCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", serverB.getUrl("SOPBypassSigned.jar"), serverC.getUrl("codebase"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteAbsoluteArchiveLocalPathCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", serverB.getUrl("SOPBypassSigned.jar"), server.getUrl("codebase"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteAbsoluteArchiveLocalDotCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", serverB.getUrl("SOPBypassSigned.jar"), ".");
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteAbsoluteArchiveNoCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", serverB.getUrl("SOPBypassSigned.jar"), (String) null);
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalAbsoluteArchiveNoCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", server.getUrl("SOPBypassSigned.jar"), (String) null);
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalRelativeArchiveNoCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", "SOPBypassSigned.jar", (String) null);
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalRelativeArchiveUnrelatedRemoteCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", "SOPBypassSigned.jar", serverC.getUrl());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalAbsoluteArchiveLocalDotCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", server.getUrl("SOPBypassSigned.jar"), ".");
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalRelativeArchiveLocalPathCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", "SOPBypassSigned.jar", server.getUrl("/"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testLocalRelativeArchiveLocalDotCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", "SOPBypassSigned.jar", ".");
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void testRemoteRelativeArchiveSameRemoteCodebase() throws Exception {
        TemplatedHtmlDoc templatedDoc = filterHtml("SOPBypassSigned.html", "SOPBypassSigned", "SOPBypassSigned.jar", serverC.getUrl("/"));
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr);
        assertDocumentBaseConnection(pr);
        assertUnrelatedConnection(pr);
    }

    public ProcessResult performTest(TemplatedHtmlDoc templatedDoc) throws Exception {
        ProcessResult pr = server.executeBrowser(templatedDoc.getFileName(), getClosingListener(), null);
        assertStart(pr);
        assertEnd(pr);
        assertPrivileged(pr);
        return pr;
    }

    @AfterClass
    public static void teardown() throws Exception {
        serverA.stop();
        serverB.stop();
        serverC.stop();

        mod1.restoreProperties();
        mod2.restoreProperties();
    }

}
