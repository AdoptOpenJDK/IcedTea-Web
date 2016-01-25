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
import java.net.URL;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
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
public class CodebasesAttsNoDialogsTest2 extends BrowserTest {

    private static final String appletCloseString = CodebasesAttsNoDialogsTest1.appletCloseString;
    private static final String[] HTMLA = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_ARRAY;
    private static final List<String> HTMLL = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_LIST;

    private static final String JNLPAPP = CodebasesAttsNoDialogsTest1.JNLPAPP;
    private static final String JNLPAPPLET = CodebasesAttsNoDialogsTest1.JNLPAPPLET;
    private static final String HTML = CodebasesAttsNoDialogsTest1.HTML;
    private static final String HTMLHREF = CodebasesAttsNoDialogsTest1.HTMLHREF;
    private static final String CodebasesAtts = CodebasesAttsNoDialogsTest1.CodebasesAtts;

    private static ServerLauncher secondValidServer;
    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier ensuredDP;

    @BeforeClass
    public static void initSecondaryServers() throws IOException {
        secondValidServer = ServerAccess.getIndependentInstanceOnTmpDir();
    }

    @AfterClass
    public static void stopSecondaryServers() throws IOException {
        secondValidServer.stop();
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

    //jnlp app 
    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_NormalValid_normal() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, server.getUrl(JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_NormalValid_second() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, secondValidServer.getUrl(JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_ValidNormal_normal() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, server.getUrl(JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_ValidNormal_second() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, secondValidServer.getUrl(JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    //jnlp app let
    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_NormalValid_normal() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, server.getUrl(JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_NormalValid_second() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, secondValidServer.getUrl(JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_ValidNormal_normal() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, server.getUrl(JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_ValidNormal_second() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeJavawsUponUrl(null, secondValidServer.getUrl(JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    //html
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_NormalValid_normal() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_NormalValid_second() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_ValidNormal_normal() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_ValidNormal_second() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    //htmlhref relative hrefs
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_NormalValid_normal() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_NormalValid_second() throws Exception {
        prepare(server.getUrl(), secondValidServer.getUrl());
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_ValidNormal_normal() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0")); //codebase is relative, so launchiong server is used to locate jnlp
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_ValidNormal_second() throws Exception {
        prepare(secondValidServer.getUrl(), server.getUrl());
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));//jnlphref is relative, so launchiong server is used to locate jnlp
    }

    public void prepare(URL c1, URL c2) throws IOException {
        prepare(c1.toExternalForm(), c2.toExternalForm());
    }

    public static void prepare(String codebase1, String codebase2) throws IOException {
        CodebasesAttsNoDialogsTest1.prepareSingle(codebase1, null, null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(codebase2, null, null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
    }

    //htmlhref absolute hrefs
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_NormalValid_normal_absoluteJnlpHrefNormal() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), server.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), server.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);

        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    public void codebasesAttsTestWorksHtml2_NormalValid_normal_absoluteJnlpHrefSecond() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);

        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    @Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    public void codebasesAttsTestWorksHtml2_NormalValid_second_absoluteJnlpHrefNormal() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), server.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), server.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);

        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_NormalValid_second_absoluteJnlpHrefSecond() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);

        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    @Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    public void codebasesAttsTestWorksHtml2_ValidNormal_normal_absoluteJnlpHrefNormal() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), server.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), server.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_ValidNormal_normal_absoluteJnlpHrefSecond() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ProcessResult pr = server.executeBrowser(null, server.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_ValidNormal_second_bsoluteJnlpHrefNormal() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), server.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), server.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a
    public void codebasesAttsTestWorksHtml2_ValidNormal_second_bsoluteJnlpHrefSecond() throws Exception {
        CodebasesAttsNoDialogsTest1.prepareSingle(secondValidServer.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '1', ServerAccess.getInstance().getDir(), CodebasesAttsNoDialogsTest1.files);
        CodebasesAttsNoDialogsTest1.prepareSingle(server.getUrl(), secondValidServer.getUrl().toExternalForm(), null, '2', secondValidServer.getDir(), CodebasesAttsNoDialogsTest1.files);
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

}
