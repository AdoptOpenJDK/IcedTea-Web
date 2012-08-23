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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.KnownToFail;
import org.junit.Assert;

import org.junit.Test;

/**
 * set of tests  to reproduce PR905 behaviour
 */
public class ParametrizedJarUrlTests extends BrowserTest{
    
    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall"}));

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
        String s4 = "AppletTestSigned was stopped";
        Assert.assertFalse("AppletTestSigned stdout shouldn't contains " + s4 + " but did", pr.stdout.contains(s4));
        String s5 = "AppletTestSigned will be destroyed";
        Assert.assertFalse("AppletTestSigned stdout shouldn't contains " + s5 + " but did", pr.stdout.contains(s5));
        String ss = "xception";
        Assert.assertFalse("AppletTestSigned stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
        String s7 = "AppletTestSigned killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s7 + " but didn't", pr.stdout.contains(s7));
    }

    @Test
    @TestInBrowsers(testIn=Browsers.all)
    @Bug(id="PR905")
    @KnownToFail
    public void parametrizedAppletTestSignedBrowserTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ParametrizedJarUrlSigned.html");
        pr.process.destroy();
        evaluateSignedApplet(pr);
    }

    @Test
    public void testParametrizedJarUrlSigned1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned1.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned1 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrlSigned1 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
    }

    @Test
    public void testParametrizedJarUrlSigned2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned2.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrlSigned2 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
    }

    @Test
    public void testParametrizedJarUrlSigned3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/ParametrizedJarUrlSigned2.jnlp?test=123456");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrlSigned2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrlSigned2 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
    }

    @Test
    public void testParametrizedJarUrl1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl1.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl1 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrl1 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
    }

    @Test
    public void testParametrizedJarUrl2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl2.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrl2 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
    }

    @Test
    public void testParametrizedJarUrl3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ParametrizedJarUrl2.jnlp?test=123456");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("ParametrizedJarUrl2 stdout should contain " + s + " but didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("ParametrizedJarUrl2 stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
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
//        This is to strict, each browser is killing as it wish
//        String s4 = "applet was stopped";
//        Assert.assert("AppletTest stdout shouldn't contains " + s4 + " but did", pr.stdout.contains(s4));
//        String s5 = "applet will be destroyed";
//        Assert.assert("AppletTest stdout shouldn't contains " + s5 + " but did", pr.stdout.contains(s5));
        String ss = "xception";
        Assert.assertFalse("AppletTest stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
        String s7 = "Aplet killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTest stdout should contain " + s7 + " but didn't", pr.stdout.contains(s7));
    }

    @Test
    @TestInBrowsers(testIn=Browsers.all)
    public void parametrizedAppletInBrowserTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ParametrizedJarUrl.html");
        pr.process.destroy();
        evaluateApplet(pr);
    }
}
