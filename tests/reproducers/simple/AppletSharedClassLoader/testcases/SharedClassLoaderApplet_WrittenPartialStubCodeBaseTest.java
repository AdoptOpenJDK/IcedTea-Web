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
import java.io.FileInputStream;
import java.io.IOException;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SharedClassLoaderApplet_WrittenPartialStubCodeBaseTest extends BrowserTest {

    //this is shortcut to avoid SharedClassLoaderApplet_dotCodeBaseTest.something
    //as static import from default package is forbidden
    private static final class X extends SharedClassLoaderApplet_dotCodeBaseTest {
    };

    //this is shortcut to avoid SharedClassLoaderApplet_WrittenCompleteCodeBaseTest.something
    //as static import from default package is forbidden
    private static final class Y extends SharedClassLoaderApplet_WrittenCompleteCodeBaseTest {
    };
    private static final ServerLauncher secondServer = ServerAccess.getIndependentInstance();
    private static final String writtenCodeBaseSuffix = "_WCB2";
    private static final String writtenCodeBaseFileSuffix = writtenCodeBaseSuffix + ".html";
    private static final String subFolderName = "SharedClassLaoderSubCodebaseFolder";
    private static final File root = server.getDir();
    private static final File subRoot = new File(root, subFolderName);
    private static final File origJar1 = new File(server.getDir(), X.jar1);
    private static final File origJar2 = new File(server.getDir(), X.jar2);
    
    private static final String renamedJar1Name=X.jar1+"XYZ";
    private static final String renamedJar2Name=X.jar2+"XYZ";
    private static final File renamedJar1 = new File(server.getDir(), renamedJar1Name);
    private static final File renamedJar2 = new File(server.getDir(), renamedJar2Name);

    @BeforeClass
    public static void createAlternativeAndEnsureOriginalArchivesDontExist() throws IOException {
        if (!subRoot.exists()) {
            boolean b = subRoot.mkdir();
            if (!b){
                throw new RuntimeException(subRoot.toString()+" was not created");
                }
        }
        FirefoxProfilesOperator.copyFile(origJar1, new File(subRoot, X.jar1));
        FirefoxProfilesOperator.copyFile(origJar1, new File(subRoot, X.jar2));
        //origJar2 is actually created by SharedClassLoaderApplet_dotCodeBaseTestcreateAlternativeArchive
        //so it do not need to exists
        if (origJar2.exists()){
           boolean b =  origJar2.renameTo(renamedJar2);
            if (!b) {
                throw new RuntimeException(origJar2.toString()+" was not renamed");
            }
        }
        boolean b =  origJar1.renameTo(renamedJar1);
        if (!b) {
            throw new RuntimeException(origJar1.toString()+" was not renamed");
        }
        

    }
    
    @AfterClass
      public static void restoreRenamedJars(){
           if (renamedJar2.exists()){
           boolean b =  renamedJar2.renameTo(origJar2);
           if (!b){
               throw new RuntimeException(renamedJar2.toString()+" was not renamed");
           }
        }
        boolean b =  renamedJar1.renameTo(origJar1);
        if (!b) {
              throw new RuntimeException(renamedJar1.toString()+" was not renamed");
          }
        
    }

    @BeforeClass
    public static void prepareFakeFiles() throws IOException {
        for (int i = 0; i < Y.originalNames.length; i++) {
            String string = Y.originalNames[i];
            String content = ServerAccess.getContentOfStream(new FileInputStream(new File(server.getDir(), string + X.dotCodeBaseFileSuffix)), "utf-8");
            String content1 = content.replaceAll("codebase=\"\\.\"", "codebase=\"/" + subFolderName + "/\"");
            ServerAccess.saveFile(content1, new File(server.getDir(), string + writtenCodeBaseFileSuffix));
        }


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
        ProcessResult pr = server.executeBrowser(X.r1w1 + writtenCodeBaseFileSuffix, new RulesFolowingClosingListener(X.readShared), null);
        X.assertSharedLoader(pr, false);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllExceptMain_OnePage() throws Exception {
        ProcessResult pr = server.executeBrowser(X.r1w2 + writtenCodeBaseFileSuffix, new RulesFolowingClosingListener(X.readShared), null);
        X.assertSharedLoader(pr, false);
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBase_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(server.getUrl(X.r1 + writtenCodeBaseFileSuffix)), null);
        X.assertSharedLoader(pr, true);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndMain_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(server.getUrl(X.r2 + writtenCodeBaseFileSuffix)), null);
        X.assertSharedLoader(pr, true);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndCodeBase_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(secondServer.getUrl(X.r1 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseCodeBaseAndMain_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(secondServer.getUrl(X.r2 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButArchives_onePage() throws Exception {
        ProcessResult pr = server.executeBrowser(X.r1w1_2 + writtenCodeBaseFileSuffix, new RulesFolowingClosingListener(X.readShared), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllExceptMainAndArchives_OnePage() throws Exception {
        ProcessResult pr = server.executeBrowser(X.r1w2_2 + writtenCodeBaseFileSuffix, new RulesFolowingClosingListener(X.readShared), null);
        X.assertNotSharedLoader(pr);
    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1_2 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(server.getUrl(X.r1 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Bug(id = "PR580")
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndMainAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1_2 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(server.getUrl(X.r2 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseAndCodeBaseAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1_2 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(secondServer.getUrl(X.r1 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }

    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    @Test
    public void SharedClassLoaderAppletTest_sharedAllButDocumentBaseCodeBaseAndMainAndArchives_twoPages() throws Exception {
        ProcessResult pr = server.executeBrowser(X.w1_2 + writtenCodeBaseFileSuffix, new X.UrlLaunchingListener(secondServer.getUrl(X.r2 + writtenCodeBaseFileSuffix)), null);
        X.assertNotSharedLoader(pr);

    }
}
