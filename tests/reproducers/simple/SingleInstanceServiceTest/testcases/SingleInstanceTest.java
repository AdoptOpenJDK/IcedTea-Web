/* SingleInstanceTest.java
Copyright (C) 2012 Red Hat, Inc.

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

import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserFactory;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

public class SingleInstanceTest extends BrowserTest {

    private static boolean isJnlp(String launchFile) {
        return launchFile.toLowerCase().endsWith(".jnlp");
    }

    private static boolean isHtml(String launchFile) {
        return launchFile.toLowerCase().endsWith(".html");
    }

    private static void checkNulls(String testName, ProcessResult prSecondInst, String var) {
        Assert.assertNotNull(var + " SingleInstanceTest." + testName + "result was null ", prSecondInst);
        Assert.assertNotNull(var + " SingleInstanceTest." + testName + "result was null ", prSecondInst.stdout);
        Assert.assertNotNull(var + " SingleInstanceTest." + testName + "result was null ", prSecondInst.stderr);
    }

    private static void evaluateFirstInstance(ProcessResult prFirstInst, String testName) {
        checkNulls(testName, prFirstInst, "First");
        // First Instance's result should run without exceptions.
        String s0 = "SingleInstanceChecker: Adding listener to service.";
        String s1 = "Parameters received by SingleInstanceChecker";
        String ss = "SingleInstanceChecker: Service lookup failed.";
        Assert.assertTrue("SingleInstanceTest." + testName
                + "'s first PR stdout should contain " + s0 + " but didn't",
                prFirstInst.stdout.contains(s0));
        Assert.assertTrue("SingleInstanceTest." + testName
                + "'s first PR stdout should contain " + s1 + " but didn't",
                prFirstInst.stdout.contains(s1));
        Assert.assertFalse("SingleInstanceTest." + testName
                + "'s first PR stderr should not contain " + ss + " but did",
                prFirstInst.stderr.contains(ss));
    }

    private static void evaluateSecondInstance(ProcessResult prSecondInst, String testName) {
        checkNulls(testName, prSecondInst, "Second");
        // Second Instance's result should throw a LaunchException.
        String s2 = "net.sourceforge.jnlp.LaunchException";
        Assert.assertTrue("SingleInstanceTest." + testName
                + "'s second PR stderr should contain " + s2 + " but didn't",
                prSecondInst.stderr.contains(s2));
    }

    private static boolean bothHtml(String app1, String app2) {
        return isHtml(app1) && isHtml(app2);
    }

    private static class AsyncProcess extends Thread {

        private ProcessResult pr = null;
        private String launchFile;

        public AsyncProcess(String launchFile) {
            this.launchFile = launchFile;

        }

        @Override
        public void run() {
            try {
                boolean isJavawsTest = isJnlp(launchFile);
                pr = isJavawsTest ? server.executeJavawsHeadless(launchFile, null, null)
                        : server.executeBrowser(launchFile);
            } catch (Exception ex) {
                ServerAccess.logException(ex);
            } finally {
                if (pr == null) {
                    pr = new ProcessResult("", "", null, true, null, null);
                }
            }
        }

        public ProcessResult getPr() {
            return pr;
        }
    }

    private ProcessResult[] executeSingleInstanceCheck(String app1, String app2) throws Exception {

        final AsyncProcess ap = new AsyncProcess(app2);
        ContentReaderListener clr = new ContentReaderListener() {

            @Override
            public void charReaded(char ch) {
                //nothing to do
            }

            @Override
            public void lineReaded(String s) {
                if (s.contains(listenerConfirmed)) {
                    ap.start();
                }
            }
        };
        boolean isJavawsTest = isJnlp(app1);
        final ProcessResult pr = isJavawsTest ? server.executeJavawsHeadless(app1, clr, null)
                : server.executeBrowser(app1, clr, null);

        int timeout = 0;
        while (ap.pr == null) {
            timeout++;
            Thread.sleep(500);
            if (timeout > 20) {
                break;
            }
        }
        return new ProcessResult[]{pr, ap.getPr()};

    }
    //files
    private static final String jnlpApplet = "/SingleInstanceTest.jnlp";
    private static final String jnlpApplication = "/SingleInstanceTestWS.jnlp";
    private static final String htmlpApplet = "/SingleInstanceTest_clasical.html";
    private static final String htmlJnlpHrefApplet = "/SingleInstanceTest_jnlpHref.html";
    //constants
    private static final String listenerConfirmed = "SingleInstanceChecker: Listener added.";

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void htmlpAppletXhtmlpApplet() throws Exception {
        ProcessResult[] results = executeSingleInstanceCheck(htmlpApplet, htmlpApplet);
        String id = "htmlpAppletXhtmlpApplet";
        evaluateFirstInstance(results[0], id);
        //the first browser is consuming all the output
        evaluateSecondInstance(results[0], id);

    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void htmlJnlpHrefAppletXhtmlJnlpHrefApplet() throws Exception {
        ProcessResult[] results = executeSingleInstanceCheck(htmlJnlpHrefApplet, htmlJnlpHrefApplet);
        String id = "htmlJnlpHrefAppletXhtmlJnlpHrefApplet";
        evaluateFirstInstance(results[0], id);
        //the first browser is consuming all the output
        evaluateSecondInstance(results[0], id);

    }

    @Test
    public void jnlpApplicationXjnlpApplication() throws Exception {
        ProcessResult[] results = executeSingleInstanceCheck(jnlpApplication, jnlpApplication);
        String id = "jnlpApplicationXjnlpApplication";
        evaluateFirstInstance(results[0], id);
        evaluateSecondInstance(results[1], id);

    }

    @Test
    @NeedsDisplay
    public void jnlpAppleXjnlpApplet() throws Exception {
        ProcessResult[] results = executeSingleInstanceCheck(jnlpApplet, jnlpApplet);
        String id = "jnlpAppleXjnlpApplet";
        evaluateFirstInstance(results[0], id);
        evaluateSecondInstance(results[1], id);
    }

    public static void main(String[] args) throws Exception {
        new SingleInstanceTest().main();
    }

    /**
     * This "test" is testing all possible variations of launches.
     * However html x jnlp (or vice versa) tests are failing
     * I do not suppose this should ever happen in real life, so I'm not including this as @KnownToFail
     * See the list of results on below for couriosity ;)
     * 
     *Passed /SingleInstanceTest.jnlp x /SingleInstanceTest.jnlp
     *Passed /SingleInstanceTest.jnlp x /SingleInstanceTestWS.jnlp
     *FAILED /SingleInstanceTest.jnlp x /SingleInstanceTest_jnlpHref.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest.jnlp x /SingleInstanceTest_clasical.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *Passed /SingleInstanceTestWS.jnlp x /SingleInstanceTest.jnlp
     *Passed /SingleInstanceTestWS.jnlp x /SingleInstanceTestWS.jnlp
     *java.lang.NoSuchMethodException: SingleInstanceTest.access$000()
     *FAILED /SingleInstanceTestWS.jnlp x /SingleInstanceTest_jnlpHref.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTestWS.jnlp x /SingleInstanceTest_clasical.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest_jnlpHref.html x /SingleInstanceTest.jnlp - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest_jnlpHref.html x /SingleInstanceTestWS.jnlp - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *Passed /SingleInstanceTest_jnlpHref.html x /SingleInstanceTest_jnlpHref.html
     *FAILED /SingleInstanceTest_jnlpHref.html x /SingleInstanceTest_clasical.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest_clasical.html x /SingleInstanceTest.jnlp - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest_clasical.html x /SingleInstanceTestWS.jnlp - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *FAILED /SingleInstanceTest_clasical.html x /SingleInstanceTest_jnlpHref.html - java.lang.AssertionError: SingleInstanceTest.main's first PR stdout should contain Parameters received by SingleInstanceChecker but didn't
     *Passed /SingleInstanceTest_clasical.html x /SingleInstanceTest_clasical.html
     */
    public void main() throws Exception {
        //just for fun try all not so probable cominations
        String[] args = new String[]{jnlpApplet, jnlpApplication, htmlJnlpHrefApplet, htmlpApplet};
        //normally handled by annotation
        server.setCurrentBrowser(BrowserFactory.getFactory().getRandom());
        for (int i = 0; i < args.length; i++) {
            String app1 = args[i];
            for (int j = 0; j < args.length; j++) {
                String app2 = args[j];
                try {
                    ProcessResult[] results = executeSingleInstanceCheck(app1, app2);
                    evaluateFirstInstance(results[0], "main");
                    if (bothHtml(app1, app2)) {
                        evaluateSecondInstance(results[0], "main");
                    } else {
                        evaluateSecondInstance(results[1], "main");
                    }
                    System.out.println("Passed " + app1 + " x " + app2);
                } catch (Error ex) {
                    System.out.println("FAILED " + app1 + " x " + app2 + " - " + ex.toString());
                    //ex.printStackTrace();
                }
            }

        }

    }

}
