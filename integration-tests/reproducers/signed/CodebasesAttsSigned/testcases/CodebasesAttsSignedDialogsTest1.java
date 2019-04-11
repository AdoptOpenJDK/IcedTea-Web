/* CodebasesAttsSignedNoDialogsTest1.java
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static net.sourceforge.jnlp.browsertesting.BrowserTest.server;

/**
 *
 * null, empty, none, wrong, correct jnlp x html different codebases.
 *
 * no dialogs should be appeared. Second testsuite with ALL dialogs (head only)
 *
 *
 */
public class CodebasesAttsSignedDialogsTest1 extends BrowserTest {

    public static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    public static final String[] JAVAWS_HTML_ARRAY = new String[]{OptionsDefinitions.OPTIONS.HTML.option};
    public static final List<String> JAVAWS_HTML_LIST = Arrays.asList(JAVAWS_HTML_ARRAY);
    private static final String[] JAVAWS_HEADLES_ARRAY = new String[]{OptionsDefinitions.OPTIONS.HTML.HEADLESS.option};
    private static final List<String> JAVAWS_HEADLES_LIST = Arrays.asList(JAVAWS_HEADLES_ARRAY);

    public static final String JNLPAPP = "CodebasesAttsSignedApp.jnlp";
    public static final String JNLPAPPLET = "CodebasesAttsSignedApplet.jnlp";
    public static final String HTML = "CodebasesAttsSigned.html";
    public static final String HTMLHREF = "CodebasesAttsSignedJnlpHref.html";
    public static final String CodebasesAttsSigned = "CodebasesAttsSigned";

    private static ServerLauncher secondValidServer;
    private static DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier ensuredDP;

    public static final String[] files = new String[]{"CodebasesAttsSignedApp.jnlp", "CodebasesAttsSigned.html", "CodebasesAttsSignedApplet.jnlp", "CodebasesAttsSignedJnlpHref.html"};

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

    //jar from different source
    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJnlp1_null_foreignJar() throws Exception {
        prepareSwapResources();
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, secondValidServer.getUrl(JNLPAPP));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJnlp2_null_foreignJar() throws Exception {
        prepareSwapResources();
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, secondValidServer.getUrl(JNLPAPPLET));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test   browsers dont support headless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml1_null_foreignJar() throws Exception {
        prepareSwapResources();
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test browsers do not support ehadless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml2_null_foreignJar() throws Exception {
        prepareSwapResources();
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml1_null_foreignJar() throws Exception {
        prepareSwapResources();
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, secondValidServer.getUrl(HTML));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml2_null_foreignJar() throws Exception {
        prepareSwapResources();
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, secondValidServer.getUrl(HTMLHREF));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 2"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }
    //done cross jars

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    //the only alaca one in "normal mode"
    public void codebasesAttsSignedTestWorksJnlp1_null() throws Exception {
        prepare(null);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, server.getUrl(JNLPAPP));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJnlp2_null() throws Exception {
        prepare(null);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, server.getUrl(JNLPAPPLET));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test  browsers dont support headless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml1_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, HTML, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    //@Test browsers do not support ehadless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml2_null() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, HTMLHREF, new AutoOkClosingListener(), new AutoErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml1_null() throws Exception {
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
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml2_null() throws Exception {
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
        Assert.assertTrue(pr.stdout.contains(CodebasesAttsSigned));
        Assert.assertTrue(pr.stdout.contains("id: 1"));
        Assert.assertTrue(pr.stdout.contains("BID0"));
    }

    public static void prepare(String codebase) throws IOException {
        prepare(codebase, codebase);
    }

    public static void prepare(String codebase1, String codebase2) throws IOException {
        prepareSingle(codebase1, null, null, '1', ServerAccess.getInstance().getDir(), files, true);
        prepareSingle(codebase2, null, null, '2', secondValidServer.getDir(), files, true);
    }

    //note, that the modification of jar is breaking signature.. well worthy to add test :)
    public static void prepareSingle(String codebase, String jnlphref, String jar, char id, File targetDir, String[] files, boolean corrupt) throws IOException {
        File srcDir = ServerAccess.getInstance().getDir();
        for (String file : files) {
            String s1 = FileUtils.loadFileAsString(new File(srcDir, file + ".in"));
            if (codebase == null) {
                s1 = s1.replace("@CODEBASE@", "");
            } else {
                s1 = s1.replace("@CODEBASE@", "codebase=\"" + codebase + "\"");
            }
            if (jnlphref == null) {
                s1 = s1.replace("@JNLPHREF@", "CodebasesAttsSignedApplet.jnlp");
            } else {
                s1 = s1.replace("@JNLPHREF@", jnlphref + "/CodebasesAttsSignedApplet.jnlp");
            }
            if (jar == null) {
                s1 = s1.replace("@JAR@", "CodebasesAttsSigned");
            } else {
                s1 = s1.replace("@JAR@", jar + "/CodebasesAttsSigned");
            }
            s1 = s1.replace("@ID@", "" + id);
            FileUtils.saveFile(s1, new File(targetDir, file));
        }
        String n = "CodebasesAttsSigned.jar";
        if (!srcDir.equals(targetDir)) {
            copyJarAndChange(new File(srcDir, n), new File(targetDir, n), id, corrupt);
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
    private static void copyJarAndChange(File from, File to, char id, boolean corruptSignatures) throws FileNotFoundException, IOException {
        ZipFile original = new ZipFile(from);
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(to))) {
            Enumeration entries = original.entries();
            byte[] buffer = new byte[512];
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().endsWith("CodebasesAttsSigned.class") && corruptSignatures) {
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

    //reading corruptied jars
    @NeedsDisplay
    @Test
    //the only alaca one
    public void codebasesAttsSignedTestWorksJnlp1_null_corruptedJar() throws Exception {
        prepare(null);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, secondValidServer.getUrl(JNLPAPP));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        generalFailure(pr);
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJnlp2_null_corruptedJar() throws Exception {
        prepare(null);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), JAVAWS_HEADLES_LIST, secondValidServer.getUrl(JNLPAPPLET));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        generalFailure(pr);
    }

    @NeedsDisplay
    //@Test  browsers dont support headless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml1_null_corruptedJar() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTML), new AutoOkClosingListener(), new AutoErrorClosingListener());
        generalFailure(pr);
    }

    @NeedsDisplay
    //@Test browsers do not support ehadless dialogues
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksHtml2_null_corruptedJar() throws Exception {
        prepare(null);
        ProcessResult pr = server.executeBrowser(null, secondValidServer.getUrl(HTMLHREF), new AutoOkClosingListener(), new AutoErrorClosingListener());
        generalFailure(pr);
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml1_null_corruptedJar() throws Exception {
        prepare(null);
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, secondValidServer.getUrl(HTML));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        generalFailure(pr);
    }

    @NeedsDisplay
    @Test
    @Bug(id = "PR2489")
    public void codebasesAttsSignedTestWorksJavawsHtml2_null_corruptedJar() throws Exception {
        prepare(null);
        ArrayList<String> HTML_HEADLESS = new ArrayList<>();
        HTML_HEADLESS.addAll(JAVAWS_HEADLES_LIST);
        HTML_HEADLESS.addAll(JAVAWS_HTML_LIST);
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), HTML_HEADLESS, secondValidServer.getUrl(HTMLHREF));
        pw.addStdOutListener(new AutoOkClosingListener());
        pw.addStdErrListener(new AutoErrorClosingListener());
        pw.setWriter("YES\nYES\nYES\nYES\n");
        ProcessResult pr = pw.execute();
        generalFailure(pr);
    }

    public void generalFailure(String s) {
        Assert.assertFalse(s.contains(appletCloseString));
        Assert.assertFalse(s.contains("id:"));
        Assert.assertFalse(s.contains("BID"));
    }

    private void generalFailure(ProcessResult pr) {
        generalFailure(pr.stdout);
    }

    private void prepareSwapResources() throws IOException {
        prepareSingle(null, null, secondValidServer.getUrl().toExternalForm(), '1', ServerAccess.getInstance().getDir(), files, true);
        prepareSingle(null, null, server.getUrl().toExternalForm(), '2', secondValidServer.getDir(), files, true);
    }

}
