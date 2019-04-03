/* CodebasesAttsNoDialogsTest1.java
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.Browser;
import net.sourceforge.jnlp.browsertesting.BrowserFactory;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import static net.sourceforge.jnlp.browsertesting.BrowserTest.server;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * null, empty, none, wrong, correct jnlp x html different codebases.
 *
 * no dialogs should be appeared. Second testsuite with ALL dialogs (head only)
 *
 *
 */
public class CodebasesAttsNoDialogsTest3 extends BrowserTest {

    private static final String appletCloseString = CodebasesAttsNoDialogsTest1.appletCloseString;
    private static final String[] HTMLA = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_ARRAY;
    private static final List<String> HTMLL = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_LIST;

    private static final String JNLPAPP = CodebasesAttsNoDialogsTest1.JNLPAPP;
    private static final String JNLPAPPLET = CodebasesAttsNoDialogsTest1.JNLPAPPLET;
    private static final String HTML = CodebasesAttsNoDialogsTest1.HTML;
    private static final String HTMLHREF = CodebasesAttsNoDialogsTest1.HTMLHREF;
    private static final String CodebasesAtts = CodebasesAttsNoDialogsTest1.CodebasesAtts;

    private static ServerLauncher secondValidServer;
    private static ServerLauncher thirdValidServer;
    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier ensuredDP;

    private static final String ABS = "ABS";
    private static boolean WAS;
    private static final boolean Force_Outputs = false;//set to true to see outputs of apps in big test

    @BeforeClass
    public static void initSecondaryServers() throws IOException {
        secondValidServer = ServerAccess.getIndependentInstanceOnTmpDir();
        thirdValidServer = ServerAccess.getIndependentInstanceOnTmpDir();
    }

    @AfterClass
    public static void stopSecondaryServers() throws IOException {
        secondValidServer.stop();
        thirdValidServer.stop();
    }

    @BeforeClass
    public static void setProperties() throws IOException {
        ensuredDP = new DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier(
                new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.name()),
                new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.name())
        );
        ensuredDP.setProperties();
    }

    @AfterClass
    public static void resetProperties() throws IOException {
        ensuredDP.restoreProperties();
    }

    @BeforeClass
    public static void setOutput() throws IOException {
        WAS = ServerAccess.LOGS_REPRINT;
    }

    @AfterClass
    public static void resetOutput() throws IOException {
        ServerAccess.LOGS_REPRINT = WAS;
    }

    /*
     *Most fun. jnlp/applet is on page, jnlp on second and resource on third
     * (those should fail)
    
     * As adition,  jnlphref have applet on one side, jnlp on second and this one have codebase on THIRD
     * (agian representation of that  triple bug from CodebasesAttsNoDialogsTest1
     */
    //@Test test is disbaled. Is not testing much more then other CodebasesAttsNoDialogsTest1-3 tests and is fragile. Also its behaviour may change, if loading form non-codebase/docbase resources will be prohibited
    //@TestInBrowsers(testIn = Browsers.one) hacked manually. We really do not wont to iterate this test browser-times
    public void threeServers__okValues() throws IOException, Exception {
        Browser localBrowser = BrowserFactory.getFactory().getRandom();
        setBrowser(localBrowser.getID());
        Browsers browserBackup = getBrowser();
        try {
            int totalCounter = 0;
            //we know that "" and "   " behaves in same way, so let sminimalize this lopp
            //abs get substituted by hardcoded path
            String[] validValues = new String[]{ABS, null, "", "."};
            for (int a = 0; a < validValues.length; a++) {
                String codebaseIn = validValues[a];
                for (int b = 0; b < validValues.length; b++) {
                    String jnlpHrefIn = validValues[b];
                    for (int c = 0; c < validValues.length; c++) {
                        String jarIn = validValues[c];

                        if (jarIn != null) {
                            if (jarIn.trim().isEmpty()) {
                                jarIn = null;
                            }
                        }
                        if (jnlpHrefIn != null) {
                            if (jnlpHrefIn.trim().isEmpty()) {
                                jnlpHrefIn = null;
                            }
                        }

                        //ServerLauncher[] servers = new ServerLauncher[]{ServerAccess.getIndependentInstance(), secondValidServer, thirdValidServer};
                        //for (abs x abs or abs x relative or  realtive x relative) x (nonm jnlp href) are enough two servers
                        //lets yousee tmp ones, as they have aligned id and BID
                        ServerLauncher[] servers = new ServerLauncher[]{secondValidServer, thirdValidServer};
                        for (int i = 0; i < servers.length; i++) {
                            ServerLauncher usedServer = servers[i];
                            String[] codebaseJnlpHrefJar = setByServer(usedServer, codebaseIn, jnlpHrefIn, jarIn);
                            CodebasesAttsNoDialogsTest1.prepareSingle(codebaseJnlpHrefJar[0], codebaseJnlpHrefJar[1], codebaseJnlpHrefJar[2], ("" + (i + 1)).charAt(0), usedServer.getDir(), CodebasesAttsNoDialogsTest1.files);
                        }
                        for (int i = 0; i < servers.length; i++) {
                            totalCounter++;
                            ServerLauncher usedServer = servers[i];
                            //server is caller, only because it knows javaws/browser location
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(totalCounter + ") i=" + i + ", c=" + c + ", b=" + b + ", a=" + a);
                            ServerAccess.logOutputReprint(usedServer.getUrl().toExternalForm() + ": " + codebaseIn + ", " + jnlpHrefIn + ", " + jarIn);
                            ServerAccess.logOutputReprint("jnlpapp " + JNLPAPP);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr1 = server.executeJavawsUponUrl(null, usedServer.getUrl(JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr1.stdout);
                            ServerAccess.logOutputReprint(pr1.stderr);
                            generalPass(pr1);
                            ServerAccess.logOutputReprint("jnlpapplet " + JNLPAPPLET);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr2 = server.executeJavawsUponUrl(null, usedServer.getUrl(JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr2.stdout);
                            ServerAccess.logOutputReprint(pr2.stderr);
                            generalPass(pr2);
                            ServerAccess.logOutputReprint("html " + HTML);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr3 = server.executeBrowser(null, usedServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr3.stdout);
                            ServerAccess.logOutputReprint(pr3.stderr);
                            generalPass(pr3);
                            ServerAccess.logOutputReprint("javaws html " + HTML);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr33 = server.executeJavawsUponUrl(HTMLL, usedServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr33.stdout);
                            ServerAccess.logOutputReprint(pr33.stderr);
                            generalPass(pr33);
                            ServerAccess.logOutputReprint("htmlhref " + HTMLHREF);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr4 = server.executeBrowser(null, usedServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr4.stdout);
                            ServerAccess.logOutputReprint(pr4.stderr);
                            generalPass(pr4);
                            ServerAccess.logOutputReprint("javaws htmlhref " + HTMLHREF);
                            ServerAccess.LOGS_REPRINT = false;
                            ProcessResult pr44 = server.executeJavawsUponUrl(HTMLL, usedServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
                            ServerAccess.LOGS_REPRINT = Force_Outputs;
                            ServerAccess.logOutputReprint(pr44.stdout);
                            ServerAccess.logOutputReprint(pr44.stderr);
                            generalPass(pr44);
                        }

                    }
                }
            }
        } finally {
            setBrowser(browserBackup);
        }
    }

    //@Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a
    public void threeServers_resourceIsElsewhere1_html() throws IOException, Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, null, thirdValidServer.getUrl().toExternalForm(), '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, null, null, '3', thirdValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ServerAccess.LOGS_REPRINT = Force_Outputs;
        ServerAccess.logOutputReprint("htmlhref " + HTMLHREF);
        ServerAccess.LOGS_REPRINT = false;
        ProcessResult pr4 = server.executeBrowser(null, ServerAccess.getInstance().getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        ServerAccess.LOGS_REPRINT = Force_Outputs;
        ServerAccess.logOutputReprint(pr4.stdout);
        ServerAccess.logOutputReprint(pr4.stderr);
        Assert.assertTrue(pr4.stdout.contains("id: 1")); //param is from applet page
        Assert.assertTrue(pr4.stdout.contains("BID3"));
        ServerAccess.LOGS_REPRINT = WAS;
    }

    @Test
    @Bug(id = "PR2805")
    @KnownToFail
    //all three are valid, but on l one bug is supported now, but 2805 have priority
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a
    public void threeServers_resourceIsElsewhere1_javawshtml() throws IOException, Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, null, thirdValidServer.getUrl().toExternalForm(), '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle((String) null, null, null, '3', thirdValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ServerAccess.LOGS_REPRINT = Force_Outputs;
        ServerAccess.logOutputReprint("javaws htmlhref " + HTMLHREF);
        ServerAccess.LOGS_REPRINT = false;
        ProcessResult pr44 = server.executeJavawsUponUrl(HTMLL, ServerAccess.getInstance().getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        ServerAccess.LOGS_REPRINT = Force_Outputs;
        ServerAccess.logOutputReprint(pr44.stdout);
        ServerAccess.logOutputReprint(pr44.stderr);
        //Assert.assertTrue(pr44.stdout.contains("id: 2")); //param is from jnlphreffed file. This may be considered bug
        Assert.assertTrue(pr44.stdout.contains("BID3"));
        Assert.assertTrue(pr44.stdout.contains("id: 1")); //should be same as threeServers_resourceIsElsewhere1_html
        ServerAccess.LOGS_REPRINT = WAS;
    }

    private String[] setByServer(ServerLauncher instance, String codebaseIn, String jnlpHrefIn, String jarIn) throws MalformedURLException {
        String[] codebaseJnlpHrefJar = new String[]{codebaseIn, jnlpHrefIn, jarIn};

        if (ABS.equals(codebaseIn)) {
            codebaseJnlpHrefJar[0] = instance.getUrl().toExternalForm();
        }
        if (ABS.equals(jnlpHrefIn)) {
            codebaseJnlpHrefJar[1] = instance.getUrl().toExternalForm();
        }
        if (ABS.equals(jarIn)) {
            codebaseJnlpHrefJar[2] = instance.getUrl().toExternalForm();
        }
        return codebaseJnlpHrefJar;
    }

    private void generalPass(ProcessResult pr1) {
        generalPass(pr1.stdout);
    }

    private void generalPass(String s) {
        Assert.assertTrue(s.contains("id: "));
        Assert.assertTrue(s.contains("BID"));
    }

}
