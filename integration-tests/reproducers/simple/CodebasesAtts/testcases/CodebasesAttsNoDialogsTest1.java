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
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * null, empty, none, wrong, correct jnlp x html different codebases.
 *
 * no dialogs should be appeared. Second testsuite with ALL dialogs (head only)
 *
 *
 */
public class CodebasesAttsNoDialogsTest1 extends BrowserTest {

    public static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    public static final String[] JAVAWS_HTML_ARRAY = new String[]{OptionsDefinitions.OPTIONS.HTML.option};
    public static final List<String> JAVAWS_HTML_LIST = Arrays.asList(JAVAWS_HTML_ARRAY);

    public static final String JNLPAPP = "CodebasesAttsApp.jnlp";
    public static final String JNLPAPPLET = "CodebasesAttsApplet.jnlp";
    public static final String HTML = "CodebasesAtts.html";
    public static final String HTMLHREF = "CodebasesAttsJnlpHref.html";
    public static final String CodebasesAtts = "CodebasesAtts";

    private static ServerLauncher emptyServer;
    private static ServerLauncher secondValidServer;
    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier ensuredDP;

    public static final String[] files = new String[]{"CodebasesAttsApp.jnlp", "CodebasesAtts.html", "CodebasesAttsApplet.jnlp", "CodebasesAttsJnlpHref.html"};

    @BeforeClass
    public static void initSecondaryServers() throws IOException {
        emptyServer = ServerAccess.getIndependentInstanceOnTmpDir();
        secondValidServer = ServerAccess.getIndependentInstanceOnTmpDir();
    }

    @AfterClass
    public static void stopSecondaryServers() throws IOException {
        emptyServer.stop();
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
    public void codebasesAttsTestWorksJnlp1_dot() throws Exception {
        prepare("\".\"");
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_empty() throws Exception {
        prepare("\"\"");
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_space() throws Exception {
        prepare("\" \"");
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_spaces() throws Exception {
        prepare("\"     \"");
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksJnlp1_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_value() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    //all three are valid, but on l one bug is supported nows
    @Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    public void codebasesAttsTestWorksJnlp1_value2() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp1_value3() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeJavawsUponUrl(null, new URL(secondValidServer.getUrl().toString() + "/" + JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    //value3 and 4 tests ar emoreover testing taht our three servers are working as expected.
    @NeedsDisplay
    @Test
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7") 
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    public void codebasesAttsTestWorksJnlp1_value4() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeJavawsUponUrl(null, new URL(secondValidServer.getUrl().toString() + "/" + JNLPAPP), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    //jnlp applet
    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_dot() throws Exception {
        prepare("\".\"");
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_empty() throws Exception {
        prepare("\"\"");
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_space() throws Exception {
        prepare("\" \"");
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_spaces() throws Exception {
        prepare("\"     \"");
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksJnlp2_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_value() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7") 
    public void codebasesAttsTestWorksJnlp2_value2() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    public void codebasesAttsTestWorksJnlp2_value3() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeJavawsUponUrl(null, new URL(secondValidServer.getUrl().toString() + "/" + JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    //value3 and 4 tests ar emoreover testing taht our three servers are working as expected.
    @NeedsDisplay
    @Test
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    @Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7") 
    public void codebasesAttsTestWorksJnlp2_value4() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeJavawsUponUrl(null, new URL(secondValidServer.getUrl().toString() + "/" + JNLPAPPLET), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_dot() throws Exception {
        prepare("\".\"");
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_empty() throws Exception {
        prepare("\"\"");
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksJHtml1_space() throws Exception {
        prepare("\" \"");
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_spaces() throws Exception {
        prepare("\"     \"");
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksHtml1_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_value() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    @Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    public void codebasesAttsTestWorksHtml1_value2() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml1_value3() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeBrowser(null, new URL(secondValidServer.getUrl().toString() + "/" + HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    //value3 and 4 tests ar emoreover testing taht our three servers are working as expected.
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7") 
    public void codebasesAttsTestWorksHtml1_value4() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeBrowser(null, new URL(secondValidServer.getUrl().toString() + "/" + HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_dot() throws Exception {
        prepare("\".\"");
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_empty() throws Exception {
        prepare("\"\"");
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksJHtml2_space() throws Exception {
        prepare("\" \"");
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_spaces() throws Exception {
        prepare("\"     \"");
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksHtml2_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_value() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    @Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    //@Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7") 
    public void codebasesAttsTestWorksHtml2_value2() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1")); //param comes from original jnlp, this will be visible on SECOND jnlp_href tests
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void codebasesAttsTestWorksHtml2_value3() throws Exception {
        prepare(secondValidServer.getUrl().toString());
        ProcessResult pr = server.executeBrowser(null, new URL(secondValidServer.getUrl().toString() + "/" + HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID2"));
    }

    //value3 and 4 tests ar emoreover testing taht our three servers are working as expected.
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    //all three are valid, but on l one bug is supported now
    //@Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2016-January/034446.html")
    //@Bug(id = "http://icedtea.classpath.org/hg/release/icedtea-web-1.6/rev/0d9faf51357d")
    @Bug(id = "http://icedtea.classpath.org/hg/icedtea-web/rev/22b7becd48a7")
    public void codebasesAttsTestWorksHtml2_value4() throws Exception {
        prepare(server.getUrl().toString());
        ProcessResult pr = server.executeBrowser(null, new URL(secondValidServer.getUrl().toString() + "/" + HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 2")); //param comes from original jnlp, this will be visible on jnlp_href
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    public static void prepare(String codebase) throws IOException {
        prepare(codebase, codebase);
    }

    public static void prepare(String codebase1, String codebase2) throws IOException {
        prepareSingle(codebase1, null, null, '1', ServerAccess.getInstance().getDir(), files);
        prepareSingle(codebase2, null, null, '2', secondValidServer.getDir(), files);
    }

    public static void prepareSingle(URL codebase, String jnlphref, String jar, char id, File targetDir, String[] files) throws IOException {
        prepareSingle(codebase.toExternalForm(), jnlphref, jar, id, targetDir, files);
    }
    public static void prepareSingle(String codebase, String jnlphref, String jar, char id, File targetDir, String[] files) throws IOException {
        File srcDir = ServerAccess.getInstance().getDir();
        for (String file : files) {
            String s1 = FileUtils.loadFileAsString(new File(srcDir, file + ".in"));
            if (codebase == null) {
                s1 = s1.replace("@CODEBASE@", "");
            } else {
                s1 = s1.replace("@CODEBASE@", "codebase=\"" + codebase + "\"");
            }
            if (jnlphref == null) {
                s1 = s1.replace("@JNLPHREF@", "CodebasesAttsApplet.jnlp");
            } else {
                s1 = s1.replace("@JNLPHREF@", jnlphref + "/CodebasesAttsApplet.jnlp");
            }
            if (jar == null) {
                s1 = s1.replace("@JAR@", "CodebasesAtts");
            } else {
                s1 = s1.replace("@JAR@", jar + "/CodebasesAtts");
            }
            s1 = s1.replace("@ID@", "" + id);
            FileUtils.saveFile(s1, new File(targetDir, file));
        }
        String n = "CodebasesAtts.jar";
        if (!srcDir.equals(targetDir)) {
            copyJarAndChange(new File(srcDir, n), new File(targetDir, n), id);
        }
    }

    /**
     * This copy zip jar entry by entry, and for one particular class it do BYTE
     * changes
     *
     * @param from
     * @param to
     * @param id
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyJarAndChange(File from, File to, char id) throws FileNotFoundException, IOException {
        ZipFile original = new ZipFile(from);
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(to))) {
            Enumeration entries = original.entries();
            byte[] buffer = new byte[512];
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().endsWith("CodebasesAtts.class")) {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    outputStream.putNextEntry(newEntry);
                    try (InputStream in = original.getInputStream(entry)) {
                        copyStreamAndChange(in, outputStream, id);
                    }
                } else {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    outputStream.putNextEntry(newEntry);
                    try (InputStream in = original.getInputStream(entry)) {
                        while (0 < in.available()) {
                            int read = in.read(buffer);
                            outputStream.write(buffer, 0, read);
                        }
                    }
                }
                outputStream.closeEntry();
            }
        }
    }

    /**
     * This changes bytes BID0 to BID'idchar'.
     *
     * @param din
     * @param dout
     * @param id
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyStreamAndChange(InputStream din, OutputStream dout, char id) throws FileNotFoundException, IOException {
        int c;
        final boolean[] BID0 = new boolean[]{false, false, false};
        while ((c = din.read()) != -1) {
            if (c == 'B') {
                BID0[0] = true;
                dout.write((byte) c);
            } else if (c == 'I' && BID0[0]) {
                BID0[1] = true;
                dout.write((byte) c);
            } else if (c == 'D' && BID0[1]) {
                BID0[2] = true;
                dout.write((byte) c);
            } else if (c == '0' && BID0[2]) {
                dout.write((byte) id);
                reset(BID0);
            } else {
                reset(BID0);
                dout.write((byte) c);
            }
        }
    }

    private static void reset(final boolean[] b) {
        for (int i = 0; i < b.length; i++) {
            b[i] = false;

        }
    }

}
