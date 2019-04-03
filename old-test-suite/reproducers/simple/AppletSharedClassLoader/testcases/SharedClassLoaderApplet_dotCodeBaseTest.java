/* 
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.closinglisteners.Rule;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SharedClassLoaderApplet_dotCodeBaseTest extends BrowserTest {

    private static final ServerLauncher secondServer = ServerAccess.getIndependentInstance();
    public static final String readingKeyword = "Reading";
    private static String reaadOneKeyword = readingKeyword + " 1 X";
    public static final String writingKeyword = "Writing";
    public static final String unknownKeyword = "Unknown destiny";
    public static final RulesFolowingClosingListener.MatchesRule readShared = new RulesFolowingClosingListener.MatchesRule("(?s).*" + readingKeyword + "\\s+[1-9][0-9]+\\sX.*");
    public static final RulesFolowingClosingListener.MatchesRule writeShared = new RulesFolowingClosingListener.MatchesRule("(?s).*" + writingKeyword + "\\s+[1-9][0-9]+\\sX.*");
    public static final Rule<Object, String> tooMuchReading = new Rule<Object, String>() {
        public static final int countsToBelieve = 5;

        @Override
        public void setRule(Object rule) {
            //noop
        }

        @Override
        public boolean evaluate(String upon) {
            return countStrings(upon) > countsToBelieve;
        }

        @Override
        public String toPassingString() {
            return "should contain at least" + countsToBelieve + " occurences of: " + reaadOneKeyword;
        }

        @Override
        public String toFailingString() {
            return "should contain no more than " + countsToBelieve + " occurences of: " + reaadOneKeyword;
        }
    };

    public static class UrlLaunchingListener extends net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener {

        private final URL url;
        private boolean launched = false;

        public UrlLaunchingListener(URL url) {
            super(writeShared);
            this.url = url;
        }

        @Override
        protected boolean isAlowedToFinish(String content) {
            boolean b = super.isAlowedToFinish(content);
            if (b && !launched) {
                launched = true;
                try {
                    //should imidately return because browser is running, if not, launch ins another thread
                    ProcessWrapper pw = new ProcessWrapper(server.getBrowserLocation(),
                            new ArrayList<String>(),
                            url);
                    pw.execute();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            return readShared.evaluate(content)/*ok*/ || tooMuchReading.evaluate(content)/*not ok*/;
        }
    }
    
    public static final String dotCodeBaseSuffix = "";
    public static final String dotCodeBaseFileSuffix = dotCodeBaseSuffix+".html";
    
    //names of used resources
    public static final String namePrefix = "LaunchSharedClassLoaderApplet-";
    public static final String namePrefix2 = "LaunchSharedClassLoaderApplet2-";
    public static final String r1w1 = namePrefix + "reader1-writer1";
    public static final String r1w2 = namePrefix + "reader1-writer2";
    public static final String r1 = namePrefix + "reader1";
    public static final String w1 = namePrefix + "writer1";
    public static final String r2 = namePrefix + "reader2";
    public static final String w2 = namePrefix + "writer2";
    public static final String r1w1_2 = namePrefix2 + "reader1-writer1";
    public static final String r1w2_2 = namePrefix2 + "reader1-writer2";
    public static final String r1_2 = namePrefix2 + "reader1";
    public static final String w1_2 = namePrefix2 + "writer1";
    public static final String r2_2 = namePrefix2 + "reader2";
    public static final String w2_2 = namePrefix2 + "writer2";
    public static final String jarPrefix = "AppletSharedClassLoader";
    public static final String jar1 = jarPrefix + ".jar";
    public static final String jar2 = jarPrefix + "2.jar";

    @BeforeClass
    public static void createAlternativeArchive() throws IOException {
        FirefoxProfilesOperator.copyFile(new File(server.getDir(), jar1), new File(server.getDir(), jar2));

    }

    @AfterClass
    public static void stopSecondServer() {
        secondServer.stop();
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAll_onePage() throws Exception {
        ProcessResult pr = server.executeBrowser(r1w1 + dotCodeBaseFileSuffix, new RulesFolowingClosingListener(readShared), null);
        assertSharedLoader(pr,false);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllExceptMain_OnePage() throws Exception {
        ProcessResult pr = server.executeBrowser(r1w2 + dotCodeBaseFileSuffix, new RulesFolowingClosingListener(readShared), null);
        assertSharedLoader(pr,false);
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBase_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1 + dotCodeBaseFileSuffix, new UrlLaunchingListener(server.getUrl(r1 + dotCodeBaseFileSuffix)), null);
        assertSharedLoader(pr,true);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndMain_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1 + dotCodeBaseFileSuffix, new UrlLaunchingListener(server.getUrl(r2 + dotCodeBaseFileSuffix)), null);
        assertSharedLoader(pr,true);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    //codebase seems to be compared by dots only
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndCodeBase_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1 + dotCodeBaseFileSuffix, new UrlLaunchingListener(secondServer.getUrl(r1 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    //codebase seems to be compared by dots only
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseCodeBaseAndMain_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1 + dotCodeBaseFileSuffix, new UrlLaunchingListener(secondServer.getUrl(r2 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }

    public static int countStrings(String where) {
        return countStrings(reaadOneKeyword, where);

    }

    public static int countStrings(String what, String where) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = where.indexOf(what, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += what.length();
            }
        }
        return count;
    }

    public  static void assertSharedLoader(ProcessResult pr, boolean twoSyncPages) {
        Assert.assertFalse("stdout must not contains " + unknownKeyword, pr.stdout.contains(unknownKeyword));
        Assert.assertTrue("stdout " + readShared.toPassingString(), readShared.evaluate(pr.stdout));
        Assert.assertTrue("stdout " + writeShared.toPassingString(), writeShared.evaluate(pr.stdout));
        if (twoSyncPages) {
            //for not synchronised applets there is danger of reading before writing
            //so this would be to strict and so randomly failing
            Assert.assertFalse("stdout should NOT contains several " + readingKeyword + " strings, have", tooMuchReading.evaluate(pr.stdout));
        }
    }

    public static void assertNotSharedLoader(ProcessResult pr) {
        Assert.assertFalse("stdout " + readShared.toFailingString(), readShared.evaluate(pr.stdout));
        Assert.assertTrue("stdout " + writeShared.toPassingString(), writeShared.evaluate(pr.stdout));
        Assert.assertTrue("stdout should contain several " + readingKeyword + " strings, have not", tooMuchReading.evaluate(pr.stdout));
        Assert.assertFalse("stdout must not contain " + unknownKeyword, pr.stdout.contains(unknownKeyword));
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButArchives_onePage() throws Exception {
        ProcessResult pr = server.executeBrowser(r1w1_2 + dotCodeBaseFileSuffix, new RulesFolowingClosingListener(readShared), null);
        assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllExceptMainAndArchives_OnePage() throws Exception {
        ProcessResult pr = server.executeBrowser(r1w2_2 + dotCodeBaseFileSuffix, new RulesFolowingClosingListener(readShared), null);
        assertNotSharedLoader(pr);
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1_2 + dotCodeBaseFileSuffix,new UrlLaunchingListener(server.getUrl(r1 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndMainAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1_2 + dotCodeBaseFileSuffix, new UrlLaunchingListener(server.getUrl(r2 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    //although codebase seems to be compared by dots only, the archive does its job
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndCodeBaseAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1_2 + dotCodeBaseFileSuffix, new UrlLaunchingListener(secondServer.getUrl(r1 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    //although codebase seems to be compared by dots only, the archive does its job
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseCodeBaseAndMainAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(w1_2 + dotCodeBaseFileSuffix, new UrlLaunchingListener(secondServer.getUrl(r2 + dotCodeBaseFileSuffix)), null);
        assertNotSharedLoader(pr);

    }
}
