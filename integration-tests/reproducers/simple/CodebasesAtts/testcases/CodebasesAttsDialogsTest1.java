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
import net.sourceforge.jnlp.ProcessWrapper;
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

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.sourceforge.jnlp.browsertesting.BrowserTest.server;

/**
 *
 * null, empty, none, wrong, correct jnlp x html different codebases.
 *
 * no dialogs should be appeared. Second testsuite with ALL dialogs (head only)
 *
 *
 */
public class CodebasesAttsDialogsTest1 extends BrowserTest {

    private static final String appletCloseString = CodebasesAttsNoDialogsTest1.appletCloseString;
    private static final String[] JAVAWS_HTML_ARRAY = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_ARRAY;
    private static final List<String> JAVAWS_HTML_LIST = CodebasesAttsNoDialogsTest1.JAVAWS_HTML_LIST;
    private static final String[] JAVAWS_HEADLES_ARRAY = new String[]{OptionsDefinitions.OPTIONS.HTML.HEADLESS.option};
    private static final List<String> JAVAWS_HEADLES_LIST = Arrays.asList(JAVAWS_HEADLES_ARRAY);

    private static final String JNLPAPP = CodebasesAttsNoDialogsTest1.JNLPAPP;
    private static final String JNLPAPPLET = CodebasesAttsNoDialogsTest1.JNLPAPPLET;
    private static final String HTML = CodebasesAttsNoDialogsTest1.HTML;
    private static final String HTMLHREF = CodebasesAttsNoDialogsTest1.HTMLHREF;
    private static final String CodebasesAtts = CodebasesAttsNoDialogsTest1.CodebasesAtts;

    private static ServerLauncher secondValidServer;
    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier ensuredDP;

    public static final String[] files = new String[]{"CodebasesAttsApp.jnlp", "CodebasesAtts.html", "CodebasesAttsApplet.jnlp", "CodebasesAttsJnlpHref.html"};

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
                new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL.name()),
                new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ASK_UNSIGNED.name())
        );
        ensuredDP.setProperties();
    }

    @AfterClass
    public static void resetProperties() throws IOException {
        ensuredDP.restoreProperties();
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksJnlp1_null() throws Exception {
        prepare(null);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, server.getUrl(JNLPAPP));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
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
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, server.getUrl(JNLPAPPLET));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test  rowsers dont support headless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksHtml1_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test browsers do not support ehadless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksHtml2_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksJavawsHtml1_null() throws Exception {
        prepare(null);
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, server.getUrl(HTML));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsTestWorksJavawsHtml2_null() throws Exception {
        prepare(null);
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, server.getUrl(HTMLHREF));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAtts));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    public static void prepare(String codebase) throws IOException {
        prepare(codebase, codebase);
    }

    public static void prepare(String codebase1, String codebase2) throws IOException {
        CodebasesAttsNoDialogsTest1.prepareSingle(codebase1, null, null, '1', ServerAccess.getInstance().getDir(), files);
        CodebasesAttsNoDialogsTest1.prepareSingle(codebase2, null, null, '2', secondValidServer.getDir(), files);
    }

}
