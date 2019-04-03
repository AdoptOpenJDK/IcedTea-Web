/* SimpleTest1Test.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;

import org.junit.Test;

public class ParametrizedJarUrlTests extends BrowserTest{
    
    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall"}));

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR905")
    public void parametrizedAppletTestSignedBrowserTest_hardcodedDifferentCodeBase() throws Exception {
        ServerLauncher server2 = ServerAccess.getIndependentInstance();
        String originalResourceName = "ParametrizedJarUrlSigned.html";
        String newResourceName = "ParametrizedJarUrlSigned_COPY2.html";
        createCodeBase(originalResourceName, newResourceName, server2.getUrl(""));
        //set codebase to second server
        ProcessResult pr = server.executeBrowser(newResourceName);
        server2.stop();
        evaluateSignedApplet(pr);
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR905")
    public void parametrizedAppletTestSignedBrowserTest_hardcodedCodeBase() throws Exception {
        String originalResourceName = "ParametrizedJarUrlSigned.html";
        String newResourceName = "ParametrizedJarUrlSigned_COPY1.html";
        createCodeBase(originalResourceName, newResourceName, server.getUrl(""));
        ProcessResult pr = server.executeBrowser(newResourceName);
        evaluateSignedApplet(pr);
    }

    private void createCodeBase(String originalResourceName, String newResourceName, URL codebase) throws MalformedURLException, IOException {
        String originalContent = ServerAccess.getContentOfStream(new FileInputStream(new File(server.getDir(), originalResourceName)));
        String nwContent = originalContent.replaceAll("codebase=\".\"", "codebase=\"" + codebase + "\"");
        ServerAccess.saveFile(nwContent, new File(server.getDir(), newResourceName));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR905")
    public void parametrizedAppletTestSignedBrowserTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ParametrizedJarUrlSigned.html");
        evaluateSignedApplet(pr);
    }

    @Test
    @TestInBrowsers(testIn=Browsers.one)
    public void parametrizedAppletInBrowserWithParamTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ParametrizedJarUrl.html?giveMeMore?orNot");
        evaluateApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarAppletUrl2.jnlp");
        evaluateApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTest2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarAppletUrl2.jnlp?test=123456");
        evaluateApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTest3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarAppletUrl.jnlp");
        evaluateApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTestSignedTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarAppletUrlSigned2.jnlp");
        evaluateSignedApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTestSigned2Test() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarAppletUrlSigned2.jnlp?test=123456");
        evaluateSignedApplet(pr);
    }

    @Test
    public void parametrizedAppletJavawsTestSignedTest4() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarAppletUrlSigned.jnlp");
        evaluateSignedApplet(pr);
    }

    private void evaluateSignedApplet(ProcessResult pr) {
        String s3 = "AppletTestSigned was initialised";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s3 + " but didn't", pr.stdout.contains(s3));
        String s0 = "AppletTestSigned was started";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s0 + " but didn't", pr.stdout.contains(s0));
        String s1 = "value1";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s1 + " but didn't", pr.stdout.contains(s1));
        String s2 = "value2";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s2 + " but didn't", pr.stdout.contains(s2));
        String s7 = "AppletTestSigned killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s7 + " but didn't", pr.stdout.contains(s7));
    }

   
    @Test
    public void testParametrizedJarUrlSigned1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned1.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned1 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
    }

    @Test
    public void testParametrizedJarUrlSigned2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned2.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
    }

    @Test
    public void testParametrizedJarUrlSigned3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned2.jnlp?test=123456");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
    }

    @Test
    public void testParametrizedJarUrl1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl1.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl1 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
    }

    @Test
    public void testParametrizedJarUrl2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl2.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
    }

    @Test
    public void testParametrizedJarUrl3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl2.jnlp?test=123456");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
;
    }

    private void evaluateApplet(ProcessResult pr) {
        String s3 = "applet was initialised";
        Assert.assertTrue("AppletTest stdout should contain " + s3 + " but didn't", pr.stdout.contains(s3));
        String s0 = "applet was started";
        Assert.assertTrue("AppletTest stdout should contain " + s0 + " but didn't", pr.stdout.contains(s0));
        String s1 = "value1";
        Assert.assertTrue("AppletTest stdout should contain " + s1 + " but didn't", pr.stdout.contains(s1));
        String s2 = "value2";
        Assert.assertTrue("AppletTest stdout should contain " + s2 + " but didn't", pr.stdout.contains(s2));
        String s7 = "Aplet killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTest stdout should contain " + s7 + " but didn't", pr.stdout.contains(s7));
    }

    @Test
    @TestInBrowsers(testIn=Browsers.one)
    public void parametrizedAppletInBrowserTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ParametrizedJarUrl.html");
        pr.process.destroy();
        evaluateApplet(pr);
    }
}
