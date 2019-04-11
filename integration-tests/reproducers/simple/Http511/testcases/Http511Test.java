
import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.UrlUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static net.sourceforge.jnlp.browsertesting.BrowserTest.server;

/* AppletTest.java
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
public class Http511Test extends BrowserTest {

    private static final StringBasedClosingListener AOK = new AutoOkClosingListener();
    private static final StringBasedClosingListener AER = new AutoErrorClosingListener();
    //private static final String confirmation = "*** applet running ***";
    private static final String CONFIRMATION = "http 511 reprodcuer executed";
    private static ServerLauncher server511_returnsLast;
    private static ServerLauncher server511_notreturns;
    private static final long TIMEOUT_BACKUP = ServerAccess.PROCESS_TIMEOUT;

    private static final String HTTP511JNLP = "Http511.jnlp";
    private static final String HTTP511JNLP_APPLET = "Http511_applet.jnlp";
    private static final String HTTP511HTML = "Http511.html";
    private static final String HTTP511HTML_HREF = "Http511_href.html";
    private static final String HTTP511JAR = "Http511.jar";

    @BeforeClass
    public static void start511limitedServer1() throws IOException {
        server511_returnsLast = ServerAccess.getIndependentInstance();
        server511_returnsLast.setNeedsAuthentication511(true);
        server511_returnsLast.setRememberOrigianlUrl(true);
    }

    @BeforeClass
    public static void start511limitedServer2() throws IOException {
        server511_notreturns = ServerAccess.getIndependentInstance();
        server511_notreturns.setNeedsAuthentication511(true);
        server511_notreturns.setRememberOrigianlUrl(false);
    }

    @BeforeClass
    public static void clearCache2() throws Exception {
        server.executeJavawsClearCache();
    }

    @After
    public void resetTimeout() throws IOException {
        ServerAccess.PROCESS_TIMEOUT = TIMEOUT_BACKUP;
    }

    @AfterClass
    public static void stop511limitedServers1() throws IOException {
        server511_notreturns.stop();
    }

    @AfterClass
    public static void stop511limitedServers2() throws IOException {
        server511_returnsLast.stop();
    }

    @Before
    /**
     * When http511 fails, then record from cache is taken. And that is false
     * pass. So before each tes.. clear cache
     */
    public void clearCache() throws Exception {
        clearCache2();
    }

    @Before
    public void resetAuthTokens() {
        server511_notreturns.setWasuthenticated511(false);
        server511_returnsLast.setWasuthenticated511(false);
    }

    private static String LOGIN_SENTENCE() {
        return ServerLauncher.login501_2 + "?name=itw&passwd=itw";
    }

    @Test
    public void http511noAuthRequired_authDeauth1_works() throws Exception {
        URL u = server511_returnsLast.getUrl(HTTP511JNLP);
        String s = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertTrue(s.contains("Network Authentication Required"));
        Assert.assertFalse(s.contains(HTTP511JAR));
        URL u00 = server511_returnsLast.getUrl(ServerLauncher.login501_2 + "?name=wrong&passwd=wrong");
        String s00 = UrlUtils.loadUrl(u00);
        Assert.assertTrue(s00.contains("Network Authentication Required"));
        Assert.assertFalse(s00.contains(HTTP511JAR));
        URL u0 = server511_returnsLast.getUrl(LOGIN_SENTENCE());
        String s0 = UrlUtils.loadUrl(u0);
        //? empty is probably ok :) As it have no resoure to rember
        String s1 = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertFalse(s1.contains("Network Authentication Required"));
        Assert.assertTrue(s1.contains(HTTP511JAR));
        server511_returnsLast.setWasuthenticated511(false);
        String s2 = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertTrue(s2.contains("Network Authentication Required"));
        Assert.assertFalse(s2.contains(HTTP511JAR));
    }

    @Test
    public void http511noAuthRequired_authDeauth2_works() throws Exception {
        URL u = server511_notreturns.getUrl(HTTP511JNLP);
        String s = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertTrue(s.contains("Network Authentication Required"));
        Assert.assertFalse(s.contains(HTTP511JAR));
        URL u00 = server511_notreturns.getUrl(ServerLauncher.login501_2 + "?name=wrong&passwd=wrong");
        String s00 = UrlUtils.loadUrl(u00);
        Assert.assertTrue(s00.contains("Network Authentication Required"));
        Assert.assertFalse(s00.contains(HTTP511JAR));
        String s01 = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertTrue(s01.contains("Network Authentication Required"));
        Assert.assertFalse(s01.contains(HTTP511JAR));
        URL u0 = server511_notreturns.getUrl(LOGIN_SENTENCE());
        String s0 = UrlUtils.loadUrl(u0);
        // see TinnyHttpdImpl
        Assert.assertTrue(s0.contains("Authentication ok, get back to your resource"));
        String s1 = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertFalse(s1.contains("Network Authentication Required"));
        Assert.assertTrue(s1.contains(HTTP511JAR));
        server511_notreturns.setWasuthenticated511(false);
        String s2 = UrlUtils.loadUrl(u);
        //see ServerLauncher createReply1
        Assert.assertTrue(s2.contains("Network Authentication Required"));
        Assert.assertFalse(s2.contains(HTTP511JAR));
    }

    //all on same host
    @Test
    public void http511noAuthRequired_jnlp_testcaseJustWorks() throws Exception {
        ProcessResult p = server.executeJavawsHeadless(HTTP511JNLP);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511noAuthRequired_jnlpApplet_testcaseJustWorks() throws Exception {
        ProcessResult p = server.executeJavawsHeadless(HTTP511JNLP_APPLET, AOK, null);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    public void http511AuthRequiredXfull1_jnlp_notAutomatted() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        URL u = server511_returnsLast.getUrl(HTTP511JNLP);
        ProcessResult p = ServerAccess.executeProcessUponURL(server.getJavawsLocation(), null, u);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    public void http511AuthRequiredXfull2_jnlp_notAutomatted() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        URL u = server511_notreturns.getUrl(HTTP511JNLP);
        ProcessResult p = ServerAccess.executeProcessUponURL(server.getJavawsLocation(), null, u);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    public void http511AuthRequiredXfull1_jnlpApplet_notAutomatted() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        URL u = server511_returnsLast.getUrl(HTTP511JNLP_APPLET);
        ProcessResult p = ServerAccess.executeProcessUponURL(server.getJavawsLocation(), null, u);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    public void http511AuthRequiredXfull2_jnlpApplet_notAutomatted() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        URL u = server511_notreturns.getUrl(HTTP511JNLP_APPLET);
        ProcessResult p = ServerAccess.executeProcessUponURL(server.getJavawsLocation(), null, u);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511noAuthRequired_browser_testcaseJustWorks() throws Exception {
        ProcessResult p = server.executeBrowser(HTTP511HTML, ServerAccess.AutoClose.CLOSE_ON_CORRECT_END);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511noAuthRequired_browser_jnlpHref_testcaseJustWorks() throws Exception {
        ProcessResult p = server.executeBrowser(HTTP511HTML, ServerAccess.AutoClose.CLOSE_ON_CORRECT_END);
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull1_browser_notAutomated() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        try {
            URL u = server511_returnsLast.getUrl(HTTP511HTML);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull2_browser_notAutomated() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        try {
            URL u = server511_notreturns.getUrl(HTTP511HTML);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull1_jnlpHref_notAutomated() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        try {
            URL u = server511_returnsLast.getUrl(HTTP511HTML_HREF);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull2_jnlpHref_notAutomated() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        try {
            URL u = server511_notreturns.getUrl(HTTP511HTML_HREF);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull1_browser_autheticateFirst() throws Exception {
        try {
            URL u0 = server511_returnsLast.getUrl(LOGIN_SENTENCE());
            String s = UrlUtils.loadUrl(u0);
            URL u = server511_returnsLast.getUrl(HTTP511HTML);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull2_browser_autheticateFirst() throws Exception {
        try {
            URL u0 = server511_notreturns.getUrl(LOGIN_SENTENCE());
            String s = UrlUtils.loadUrl(u0);
            URL u = server511_notreturns.getUrl(HTTP511HTML);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();
        }
    }

    //real warhorses - stdout/in based logins
    //jnlp application
    @Test
    public void http511AuthRequired_jnlp_fails_noRemoteLogin() throws Exception {
        URL u = server511_returnsLast.getUrl(HTTP511JNLP);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_jnlp() throws Exception {
        URL u = server511_returnsLast.getUrl(HTTP511JNLP);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired2_jnlp_fails_noRemoteLogin() throws Exception {
        URL u = server511_notreturns.getUrl(HTTP511JNLP);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired2_jnlp() throws Exception {
        URL u = server511_notreturns.getUrl(HTTP511JNLP);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_notreturns), AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //jnlp applet
    @Test
    public void http511AuthRequired_jnlp_applet_fails_noRemoteLogin() throws Exception {
        URL u = server511_returnsLast.getUrl(HTTP511JNLP_APPLET);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_applet_jnlp() throws Exception {
        URL u = server511_returnsLast.getUrl(HTTP511JNLP_APPLET);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired2_jnlp_applet_fails_noRemoteLogin() throws Exception {
        URL u = server511_notreturns.getUrl(HTTP511JNLP_APPLET);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired2_jnlp_applet() throws Exception {
        URL u = server511_notreturns.getUrl(HTTP511JNLP_APPLET);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                u,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_notreturns), AOK}), null, null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //resources on different ports - html/jnlp(hreff)/jar
    @Test
    public void http511AuthRequired_jnlpOkServerResource511Server_fails_noRemoteLogin() throws Exception {
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server.getUrl(nwFile);
        final StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error"); //there is bug somewhere, this do not exit in headless mode
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        pw.setWriter("SKIP\n");
        new Thread() {
            @Override
            public void run() {
                while (sbc.getAssasin() == null) {
                    //this happens during pw.execute();
                }
                sbc.getAssasin().setUseKill(true);//for some reason this one do not repond on sigTerm
            }
        }.start();
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_jnlpOkServerResource511Server() throws Exception {
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server.getUrl(nwFile);
        StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error");
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_jnlpOneFirstAuthorisedServerServerResourceAnother511Server() throws Exception {
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server511_notreturns.getUrl(nwFile);
        StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error");
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), new ExternalLogin(server511_notreturns), AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        // although ExternlLoging is launched correctly after app is started and before prompt is requested,
        // sometimes the skip goes before ExternlLoging completesd
        // those empty lines seems to be causing enough delay
        pw.setWriter("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nSKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //@Test
    public void http511AuthRequired_jnlpOneFirstAuthorisedServerServerResourceAnother511Server_notAutomatted_xFull() throws Exception {
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server511_notreturns.getUrl(nwFile);
        StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error");
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                null,
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), new ExternalLogin(server511_notreturns), AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        ProcessResult p = pw.execute();
        Assert.assertTrue(p.stdout.contains(CONFIRMATION));
        Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_jnlpOneFirstAuthorisedServerServerResourceAnother511Server_fail1_oneLoginNotEnough() throws Exception {
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server511_notreturns.getUrl(nwFile);
        final StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error");
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_notreturns), AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        pw.setWriter("SKIP\n");
        new Thread() {
            @Override
            public void run() {
                while (sbc.getAssasin() == null) {
                    //this happens during pw.execute();
                }
                sbc.getAssasin().setUseKill(true);//for some reason this one do not repond on sigTerm
            }
        }.start();
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    @Test
    public void http511AuthRequired_jnlpOneFirstAuthorisedServerServerResourceAnother511Server_fail2_oneLoginNotEnough() throws Exception {
        URL remoteJar = server511_returnsLast.getUrl(HTTP511JAR);

        String original = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP));
        original = original.replace(HTTP511JAR, remoteJar.toExternalForm());
        original = original.replace("href=\"Http511.jnlp\"", "");
        String nwFile = "2_" + HTTP511JNLP;
        FileUtils.saveFile(original, new File(server.getDir(), nwFile));

        URL nwJnlp = server511_notreturns.getUrl(nwFile);
        StringBasedClosingListener sbc = new StringBasedClosingListener("Initialization Error");
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(),
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}),
                nwJnlp,
                Arrays.asList(new ContentReaderListener[]{new ExternalLogin(server511_returnsLast), AOK}),
                Arrays.asList(new ContentReaderListener[]{sbc}), null);
        pw.setWriter("SKIP\n");
        ProcessResult p = pw.execute();
        Assert.assertFalse(p.stdout.contains(CONFIRMATION));
        Assert.assertFalse(p.stdout.contains(AOK.getCondition()));
        //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
    }

    //brutal xfull killer - everthing from its separate  511server
    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    public void http511AuthRequiredXfull1_jnlpHref_notAutomated_allFromDifferent() throws Exception {
        //gui test
        ServerAccess.PROCESS_TIMEOUT = 1000 * 60;

        ServerLauncher special511 = ServerAccess.getIndependentInstance();
        special511.setNeedsAuthentication511(true);
        special511.setRememberOrigianlUrl(true);
        try {
            URL remoteJar = server511_notreturns.getUrl(HTTP511JAR);

            String originalJnlp = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511JNLP_APPLET));
            originalJnlp = originalJnlp.replace(HTTP511JAR, remoteJar.toExternalForm());
            originalJnlp = originalJnlp.replace("href=\"Http511_applet.jnlp\"", "");
            String nwJnlp = "2_" + HTTP511JNLP_APPLET;
            FileUtils.saveFile(originalJnlp, new File(server.getDir(), nwJnlp));

            URL remoteJnlp = server511_returnsLast.getUrl(nwJnlp);

            String originalHtml = FileUtils.loadFileAsString(new File(server.getDir(), HTTP511HTML_HREF));
            originalHtml = originalHtml.replace(HTTP511JNLP_APPLET, remoteJnlp.toExternalForm());
            String nwHtml = "2_" + HTTP511HTML_HREF;
            FileUtils.saveFile(originalHtml, new File(server.getDir(), nwHtml));

            URL u = special511.getUrl(nwHtml);
            ProcessResult p = server.executeBrowser(null, u, AOK, null);
            Assert.assertTrue(p.stdout.contains(CONFIRMATION));
            Assert.assertTrue(p.stdout.contains(AOK.getCondition()));
            //Assert.assertFalse(p.stderr.contains(aer.getCondition()));
        } finally {
            special511.stop();
            //browser tests canbe run in loop, so @before/@after are not enoug
            resetAuthTokens();
            clearCache();

        }
    }

    //squid 
    @Test
    public void squidReminder() throws Exception {
        //the patch was tested against two follwwing configs (to acces  outter resources):
        //none of them was perfect:(

        /**
         * Config 1, you need to know secret url where to log, see the root.cz
         * see it in the regex.
         */
        /*
# Set up the session helper in active mode. Mind the wrap - this is one line:
external_acl_type session concurrency=100 ttl=3 %SRC /usr/lib64/squid/ext_session_acl -a -T 10800 -b /var/lib/squid/session/
# Pass the LOGIN command to the session helper with this ACL
acl session_login external session LOGIN
# Normal session ACL as per simple example
acl session_is_active external session
# ACL to match URL
acl clicked_login_url url_regex -i a-url-that-must-match$
acl clicked_login_url url_regex -i .*root.cz.*
# First check for the login URL. If present, login session
http_access allow clicked_login_url session_login
# Deny page to display
deny_info 511:../splash.html existing_users
# If we get here, URL not present, so renew session or deny request.
http_access deny !session_is_active
         */
        /**
         * This is better and is really checking 511 in real world. Unluckily,
         * have bug. It have "pass" timeout. If you wait with logging attempt
         * long enough, it will log you in.
         */
        /*
# See ttl - thats the time how long ou need to wait for login
# mind the wrap. this is one line:
external_acl_type splash_page ttl=60 concurrency=100 %SRC /usr/lib64/squid/ext_session_acl -t 7200 -b /var/lib/squid/session.db
acl existing_users external splash_page
http_access deny !existing_users
# Deny page to display
deny_info 511:../splash.html existing_users
         */
    }

    private static class ExternalLogin implements ContentReaderListener {

        private final ServerLauncher lServer;
        private boolean was511 = false;

        private ExternalLogin(ServerLauncher llServer) {
            lServer = llServer;
        }

        public void login() throws IOException {
            URL u = lServer.getUrl(LOGIN_SENTENCE());
            String s0 = UrlUtils.loadUrl(u);
        }

        @Override
        public void charReaded(char ch) {
            //no op
        }

        @Override
        public void lineReaded(String s) {
            if (s.contains("511")) {
                was511 = true;
            }
            if (was511) {
                if (s.contains("YES") && s.contains("CANCEL") && s.contains("SKIP")) {
                    try {
                        login();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
