/* AppletTestTests.java
 Copyright (C) 2011 Red Hat, Inc.

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

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class FakeCodebaseTests extends BrowserTest {

    private static DeploymentPropertiesModifier dp;
    private static File backup;
    private static final String HTMLIN = "FakeCodebase.html";
    private static final String ORIG_BASE = "OriginalCodebase.html";

    private static final String JHTMLIN = "FakeCodebase.jnlp";
    private static final String JORIG_BASE = "OriginalCodebase.jnlp";

    private static final ServerLauncher evilServer1 = ServerAccess.getIndependentInstance();
    private static final ServerLauncher evilServer2 = ServerAccess.getIndependentInstance();

    @AfterClass
    public static void killServer1() throws IOException {
        evilServer1.stop();
    }

    @AfterClass
    public static void killServer2() throws IOException {
        evilServer2.stop();
    }

    @BeforeClass
    public static void setSecurity() throws IOException {
        dp = new DeploymentPropertiesModifier();
        dp.setProperties(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ASK_UNSIGNED.name());
    }

    @BeforeClass
    public static void backupAppTrust() throws IOException {
        backup = File.createTempFile("fakeCodebase", "itwReproducers");
        backup.deleteOnExit();
        FirefoxProfilesOperator.copyFile(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), backup);
    }

    @AfterClass
    public static void restoreAppTrust() throws IOException {
        FirefoxProfilesOperator.copyFile(backup, PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
    }

    @AfterClass
    public static void resetSecurity() throws IOException {
        dp.restoreProperties();

    }

    //headless dialogues now works only for javaws.
    //@Test
    @TestInBrowsers(testIn = {Browsers.all})
    @NeedsDisplay
    public void FakeCodebaseTest() throws Exception {
        DeploymentPropertiesModifier dp = new DeploymentPropertiesModifier();
        dp.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.name());
        try {
            String ob1 = FileUtils.loadFileAsString(new File(server.getDir(), ORIG_BASE));
            assertTrue(ob1.contains("id=\"FakeCodebase0\"")); //check orig.html is correct one
            PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file is an must
            //run normal applet on normal codebase with standard server
            //answer YES + rember for ever + for codebase
            ProcessResult pr1 = server.executeBrowser("/" + ORIG_BASE, AutoClose.CLOSE_ON_CORRECT_END);
            assertTrue(pr1.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
            //the record was added to .appletSecuritySettings
            assertRecordsCountInAppletTrustSettings(1);
            //create atacker
            String htmlin = FileUtils.loadFileAsString(new File(server.getDir(), HTMLIN + ".in"));
            //now change codebase to be same as ^ but launch applet from  evilServer1
            htmlin = htmlin.replaceAll("EVILURL2", server.getUrl().toExternalForm());
            //and as bonus get resources from evilServer2
            htmlin = htmlin.replaceAll("EVILURL1", evilServer2.getUrl().toExternalForm());
            FileUtils.saveFile(htmlin, new File(server.getDir(), HTMLIN));
            String ob2 = FileUtils.loadFileAsString(new File(server.getDir(), HTMLIN));
            assertTrue(ob2.contains("id=\"FakeCodebase1\""));
            ProcessResult pr2 = ServerAccess.executeProcessUponURL(
                    server.getBrowserLocation(),
                    null,
                    evilServer1.getUrl("/" + HTMLIN),
                    new AutoOkClosingListener(),
                    null
            );
            //this  MUST ask for permissions to run, otherwise fail
            assertTrue(pr2.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
            assertRecordsCountInAppletTrustSettings(2);
        } finally {
            dp.restoreProperties();
        }
    }

    @Test
    @NeedsDisplay
    public void FakeCodebaseTestJavawsRemberCodebaseAndPassBoth() throws Exception {
        testJavaws(true);
    }

    @Test
    @NeedsDisplay
    public void FakeCodebaseTestJavawsRemberCodebaseAndFailSecond() throws Exception {
        testJavaws(true, true);
    }

    @Test
    @NeedsDisplay
    public void FakeCodebaseTestJavawsRemeberFileAndPassSecond() throws Exception {
        testJavaws(false);
    }

    @Test
    @NeedsDisplay
    public void FakeCodebaseTestJavawsRemeberFileAndFailSecond() throws Exception {
        testJavaws(false, true);
    }

    public void testJavaws(boolean codebase) throws Exception {
        testJavaws(codebase, false);
    }

    public void testJavaws(boolean codebase, boolean stopSecond) throws Exception {
        DeploymentPropertiesModifier dp = new DeploymentPropertiesModifier();
        dp.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.name());
        try {
            String ob1 = FileUtils.loadFileAsString(new File(server.getDir(), JORIG_BASE));
            assertTrue(ob1.contains("FakeCodebase0")); //check orig.html is correct one
            PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file is an must
            ProcessWrapper pw1 = new ProcessWrapper(
                    server.getJavawsLocation(),
                    Arrays.asList(OptionsDefinitions.OPTIONS.HEADLESS.option),
                    server.getUrl("/" + JORIG_BASE));
            if (codebase) {
                pw1.setWriter("RC YES");
            } else {
                pw1.setWriter("R YES");
            }
            ProcessResult pr1 = pw1.execute();
            Assert.assertTrue(pr1.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
            //the record was added to .appletSecuritySettings
            String s2 = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile()).trim();
            Assert.assertNotEquals("on codebase only, the file must not be stroed, stright save, it must be", codebase, s2.contains(JORIG_BASE));
            assertRecordsCountInAppletTrustSettings(1);
            //create atacker
            String htmlin = FileUtils.loadFileAsString(new File(server.getDir(), JHTMLIN + ".in"));
            //now change codebase to be same as ^ but launch applet from  evilServer1
            htmlin = htmlin.replaceAll("EVILURL2", server.getUrl().toExternalForm());
            //and as bonus get resources from evilServer2
            htmlin = htmlin.replaceAll("EVILURL1", evilServer2.getUrl().toExternalForm());
            FileUtils.saveFile(htmlin, new File(server.getDir(), JHTMLIN));
            String ob2 = FileUtils.loadFileAsString(new File(server.getDir(), JHTMLIN));
            assertTrue(ob2.contains("FakeCodebase1"));
            ProcessWrapper pw2 = new ProcessWrapper(
                    server.getJavawsLocation(),
                    Arrays.asList(OptionsDefinitions.OPTIONS.HEADLESS.option),
                    evilServer1.getUrl("/" + JHTMLIN)
            );
            String word = "YES";
            if (stopSecond) {
                word = "NO";
            }
            if (codebase) {
                pw2.setWriter("RC " + word);
            } else {
                pw2.setWriter("R " + word);
            }
            ProcessResult pr2 = pw2.execute();
            //this  MUST ask for permissions to run, otherwise fail
            Assert.assertNotEquals("if second was run, correct end must be printed, otherwise mus tnot be printed",stopSecond, pr2.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
            String s1 = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile()).trim();
            Assert.assertNotEquals("on codebase only, the file must not be stroed, stright save, it must be", codebase, s1.contains(JHTMLIN));
            Assert.assertNotEquals("on codebase only, the file must not be stroed, stright save, it must be", codebase, s1.contains(JORIG_BASE));
            assertRecordsCountInAppletTrustSettings(2);
        } finally {
            dp.restoreProperties();
        }
    }
private void assertRecordsCountInAppletTrustSettings(int expected) throws Exception{
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
      //i1.readContents();
     Method readContents = UnsignedAppletActionStorageImpl.class.getDeclaredMethod("readContents");
     readContents.setAccessible(true);
     readContents.invoke(i1);
     Assert.assertEquals(expected, i1.getMatchingItems(null, null, null).size());
    
}
}
