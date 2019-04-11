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
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class UnicodeLineBreakTests extends BrowserTest {
    private static DeploymentPropertiesModifier dp;
    private static File backup;


    @BeforeClass
    public static void setSecurity() throws IOException{
        dp = new DeploymentPropertiesModifier();
        dp.setProperties(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ASK_UNSIGNED.name());
    }
    
    @BeforeClass
    public static void backupAppTrust() throws IOException{
        backup = File.createTempFile("unicodeNewLIne", "itwReproducers");
        backup.deleteOnExit();
        FirefoxProfilesOperator.copyFile(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), backup);
    }
    
    @AfterClass
    public static void restoreAppTrust() throws IOException{
        FirefoxProfilesOperator.copyFile(backup, PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
    }
    
    
     @AfterClass
    public static void resetSecurity() throws IOException{
        dp.restoreProperties();
        
    }
    //headless dialogues now works only for javaws. press ok, otherwise first assert  fails
    //@Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void unicodeLineBreakTest() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        ProcessResult pr = server.executeBrowser("/UnicodeLineBreak.html", AutoClose.CLOSE_ON_CORRECT_END);
        assertTrue(pr.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        String[] ss = s.split("\n");
        for (String string : ss) {
            Assert.assertFalse(string.contains("\\Qhttp://evil-site/evil.page/\\E \\Qhttp://evil-site/\\E malware.jar"));
        }
    }
    
    //javaws -html is imune to this trick when tagsoup is used
    
    @Test
    @NeedsDisplay
    public void unicodeLineBreakTestJavaWsHtmlTagsupProbablyOn() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        //tagsoup remove the new line. So here we must really test the startWith, which may be fragile
        ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option,OptionsDefinitions.OPTIONS.HTML.option}), server.getUrl("/UnicodeLineBreak.html"), new AutoOkClosingListener(), null, null);
        pw.setWriter("YES\n");
        ProcessResult pr = pw.execute();
        assertTrue(pr.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        String[] ss = s.split("\n");
        for (String string : ss) {
            Assert.assertFalse(string.startsWith("A 1432197956873 \\Qhttp://evil-site/evil.page/\\E \\Qhttp://evil-site/\\E malware.jar"));
        }
    }
    
    @Test
    @NeedsDisplay
    public void unicodeLineBreakTestJavaWsHtmlTagsupForcedOff() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
         ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.XML.option, OptionsDefinitions.OPTIONS.HEADLESS.option, OptionsDefinitions.OPTIONS.HTML.option}), server.getUrl("/UnicodeLineBreak.html"), new AutoOkClosingListener(), null, null);
        pw.setWriter("YES\n");
        ProcessResult pr = pw.execute();
        assertTrue(pr.stdout.contains(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING));
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        String[] ss = s.split("\n");
        for (String string : ss) {
            Assert.assertFalse(string.contains("\\Qhttp://evil-site/evil.page/\\E \\Qhttp://evil-site/\\E malware.jar"));
        }
    }
}
