/*
 Copyright (C) 2015 Red Hat, Inc.

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
package sopbypasstests;

import net.sourceforge.jnlp.ClosingListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * To workaround ignorance of AccessDenied exception from calls from localhost
 * to localhost the check on pass fail is done in different way. If connection
 * is not expected, then no security exception is allowed to appear nor "Denying
 * permissions ..." string is allowed to appear. If connection is expected, then
 * appearance of security exception or "Denying permissions ..." string is
 * considered as failure.
*
 */
public class SOPBypassUtil extends BrowserTest {

    public static final String APPLET_START_STRING = "Applet Started";
    public static final String APPLET_CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    public static final String URL_CONNECTION_PREFIX = "URLConnection";
    public static final String SOCKET_CONNECTION_PREFIX = "SocketConnection";
    public static final String CONNECTION_DELIMITER = ":";
    public static final String CODEBASE = "codeBase";
    public static final String DOCUMENTBASE = "documentBase";
    public static final String UNRELATED = "unrelated";
    public static final String RESOURCES = "resource's";
    public static final String SUCCESS = " true";
    public static final String FAILURE = " false";
    public static final String URL_CODEBASE = URL_CONNECTION_PREFIX + CONNECTION_DELIMITER + CODEBASE;
    public static final String URL_CODEBASE_SUCCESS = URL_CODEBASE + SUCCESS;
    public static final String URL_CODEBASE_FAILURE = URL_CODEBASE + FAILURE;
    public static final String URL_DOCUMENTBASE = URL_CONNECTION_PREFIX + CONNECTION_DELIMITER + DOCUMENTBASE;
    public static final String URL_DOCUMENTBASE_SUCCESS = URL_DOCUMENTBASE + SUCCESS;
    public static final String URL_DOCUMENTBASE_FAILURE = URL_DOCUMENTBASE + FAILURE;
    public static final String SOCKET_CODEBASE = SOCKET_CONNECTION_PREFIX + CONNECTION_DELIMITER + CODEBASE;
    public static final String SOCKET_CODEBASE_SUCCESS = SOCKET_CODEBASE + SUCCESS;
    public static final String SOCKET_CODEBASE_FAILURE = SOCKET_CODEBASE + FAILURE;
    public static final String SOCKET_DOCUMENTBASE = SOCKET_CONNECTION_PREFIX + CONNECTION_DELIMITER + DOCUMENTBASE;
    public static final String SOCKET_DOCUMENTBASE_SUCCESS = SOCKET_DOCUMENTBASE + SUCCESS;
    public static final String SOCKET_DOCUMENTBASE_FAILURE = SOCKET_DOCUMENTBASE + FAILURE;
    public static final String URL_UNRELATED = URL_CONNECTION_PREFIX + CONNECTION_DELIMITER + UNRELATED;
    public static final String URL_UNRELATED_SUCCESS = URL_UNRELATED + SUCCESS;
    public static final String URL_UNRELATED_FAILURE = URL_UNRELATED + FAILURE;
    public static final String SOCKET_UNRELATED = SOCKET_CONNECTION_PREFIX + CONNECTION_DELIMITER + UNRELATED;
    public static final String SOCKET_UNRELATED_SUCCESS = SOCKET_UNRELATED + SUCCESS;
    public static final String SOCKET_UNRELATED_FAILURE = SOCKET_UNRELATED + FAILURE;

    public static final String URL_RESOURCES = URL_CONNECTION_PREFIX + CONNECTION_DELIMITER + RESOURCES;
    public static final String URL_RESOURCES_SUCCESS = URL_RESOURCES + SUCCESS;
    public static final String URL_RESOURCES_FAILURE = URL_RESOURCES + FAILURE;
    public static final String SOCKET_RESOURCES = SOCKET_CONNECTION_PREFIX + CONNECTION_DELIMITER + RESOURCES;
    public static final String SOCKET_RESOURCES_SUCCESS = SOCKET_RESOURCES + SUCCESS;
    public static final String SOCKET_RESOURCES_FAILURE = SOCKET_RESOURCES + FAILURE;

    public static TemplatedHtmlDoc filterHtml(String doc, String code, String archive, String codebase, String unrelatedServer) throws IOException {
        printDevMessage("html: code: " + code + ", archive: " + archive + ", codebase: " + codebase + ", unrelated: " + unrelatedServer);
        TemplatedHtmlDoc templatedDoc = new TemplatedHtmlDoc(server, doc);
        templatedDoc.setCode(code);
        templatedDoc.setArchive(archive);
        templatedDoc.setCodeBase(codebase);
        String s = templatedDoc.setAppletParams(unrelatedServer, templatedDoc.getFileName(), archive);
        printDevMessage(s);
        assertFalse(templatedDoc.toString(), templatedDoc.toString().contains("TOKEN"));
        templatedDoc.save();
        String content = server.getResourceAsString(templatedDoc.getFileName());
        assertFalse(content, content.contains("TOKEN"));
        return templatedDoc;
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, URL archive, URL codebase, String unrelatedServer) throws IOException {
        return filterHtml(doc, code, archive == null ? "" : archive.toString(), codebase == null ? "" : codebase.toString(), unrelatedServer);
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, URL archive, String codebase, String unrelatedServer) throws IOException {
        return filterHtml(doc, code, archive == null ? "" : archive.toString(), codebase, unrelatedServer);
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, String archive, URL codebase, String unrelatedServer) throws IOException {
        return filterHtml(doc, code, archive, codebase == null ? "" : codebase.toString(), unrelatedServer);
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, String jarHref, String codebase, String unrelatedServer) throws IOException {
        printDevMessage("jnlp: doc: " + doc + ", archive: " + jarHref + ", codebase: " + codebase + ", unrelated: " + unrelatedServer);
        TemplatedJnlpDoc templatedDoc = new TemplatedJnlpDoc(server, doc);
        templatedDoc.setJarHref(jarHref);
        templatedDoc.setCodeBase(codebase);
        String s = templatedDoc.setAppletParams(unrelatedServer, templatedDoc.getFileName(), jarHref);
        printDevMessage(s);
        templatedDoc.setDocumentBase(server.getUrl("SOPBypass.jnlp").toString());
        assertFalse(templatedDoc.toString(), templatedDoc.toString().contains("TOKEN"));
        templatedDoc.save();
        String content = server.getResourceAsString(templatedDoc.getFileName());
        assertFalse(content, content.contains("TOKEN"));
        return templatedDoc;
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, URL archive, URL codebase, String unrelatedServer) throws IOException {
        return filterJnlp(doc, archive == null ? "" : archive.toString(), codebase == null ? "" : codebase.toString(), unrelatedServer);
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, URL archive, String codebase, String unrelatedServer) throws IOException {
        return filterJnlp(doc, archive == null ? "" : archive.toString(), codebase, unrelatedServer);
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, String archive, URL codebase, String unrelatedServer) throws IOException {
        return filterJnlp(doc, archive, codebase == null ? "" : codebase.toString(), unrelatedServer);
    }

    public static ClosingListener getClosingListener() {
        RulesFolowingClosingListener listener = new RulesFolowingClosingListener();
        listener.addContainsRule(APPLET_START_STRING);
        listener.addContainsRule(APPLET_CLOSE_STRING);
        return listener;
    }

    public static void assertStart(ProcessResult pr) {
        assertTrue("Applet did not start", pr.stdout.contains(APPLET_START_STRING));
    }

    public static void assertEnd(ProcessResult pr) {
        assertTrue("Applet did not close correctly", pr.stdout.contains(APPLET_CLOSE_STRING));
    }

    public static void assertPrivileged(ProcessResult pr) {
        assertTrue("Applet should have had privileges to read system properties", pr.stdout.contains("Elevated privileges: true"));
    }

    public static void assertUnprivileged(ProcessResult pr) {
        assertTrue("Applet should have had privileges to read system properties", pr.stdout.contains("Elevated privileges: false"));
    }

    public static void assertCodebaseConnection(ProcessResult pr, ServerLauncher codebaseLocalhost) throws MalformedURLException, UnknownHostException {
        codebaseImpl(true, true, true, pr, codebaseLocalhost);
    }

    public static void codebaseImpl(boolean url, boolean socket, boolean b, ProcessResult pr, ServerLauncher codebaseLocalhost) throws UnknownHostException, MalformedURLException {
        printDevMessage("Codebase " + b);
        if (url) {
            printDevMessage("Url");
            assertUrlCodebase(pr, b, codebaseLocalhost);
        }
        if (socket) {
            printDevMessage("Socket");
            assertSocketCodebase(pr, b, codebaseLocalhost);
        }
    }

    public static void assertNoCodebaseConnection(ProcessResult pr, ServerLauncher codebaseLocalhost) throws MalformedURLException, UnknownHostException {
        codebaseImpl(true, true, false, pr, codebaseLocalhost);
    }

    public static void assertUrlCodebase(ProcessResult pr, boolean b, ServerLauncher codebaseLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(URL_CODEBASE, pr.stdout.contains(URL_CODEBASE));
        int i = countStrings(pr.stdout, SOCKET_RESOURCES, URL_CODEBASE, denyingUrlPermissions(codebaseLocalhost));
        String expected;
        if (b) {
            expected = URL_CODEBASE_SUCCESS;
        } else {
            expected = URL_CODEBASE_FAILURE;
        }
        evaluate(codebaseLocalhost, b, i, expected, pr);

    }

    public static void assertSocketCodebase(ProcessResult pr, boolean b, ServerLauncher codebaseLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(SOCKET_CODEBASE, pr.stdout.contains(SOCKET_CODEBASE));
        int i = countStrings(pr.stdout, null, SOCKET_CODEBASE, denyingSocketPermissions(codebaseLocalhost));
        String expected;
        if (b) {
            expected = SOCKET_CODEBASE_SUCCESS;
        } else {
            expected = SOCKET_CODEBASE_FAILURE;
        }
        evaluate(codebaseLocalhost, b, i, expected, pr);
    }

    public static void assertDocumentBaseConnection(ProcessResult pr, ServerLauncher documentBaseLocalhost) throws MalformedURLException, UnknownHostException {
        docbaseImpl(true, true, true, pr, documentBaseLocalhost);
    }

    public static void docbaseImpl(boolean url, boolean socket, boolean b, ProcessResult pr, ServerLauncher documentBaseLocalhost) throws MalformedURLException, UnknownHostException {
        printDevMessage("Docbase " + b);
        if (url) {
            printDevMessage("Url");
            assertUrlDocumentBase(pr, b, documentBaseLocalhost);
        }
        if (socket) {
            printDevMessage("Socket");
            assertSocketDocumentBase(pr, b, documentBaseLocalhost);
        }
    }

    public static void assertNoDocumentBaseConnection(ProcessResult pr, ServerLauncher documentBaseLocalhost) throws MalformedURLException, UnknownHostException {
        docbaseImpl(true, true, false, pr, documentBaseLocalhost);
    }

    public static void assertUrlDocumentBase(ProcessResult pr, boolean b, ServerLauncher documentBaseLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(URL_DOCUMENTBASE, pr.stdout.contains(URL_DOCUMENTBASE));
        int i = countStrings(pr.stdout, URL_CODEBASE, URL_DOCUMENTBASE, denyingUrlPermissions(documentBaseLocalhost));
        String expected;
        if (b) {
            expected = URL_DOCUMENTBASE_SUCCESS;
        } else {
            expected = URL_DOCUMENTBASE_FAILURE;
        }
        evaluate(documentBaseLocalhost, b, i, expected, pr);
    }

    public static void assertSocketDocumentBase(ProcessResult pr, boolean b, ServerLauncher documentBaseLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(SOCKET_DOCUMENTBASE, pr.stdout.contains(SOCKET_DOCUMENTBASE));
        int i = countStrings(pr.stdout, SOCKET_CODEBASE, SOCKET_DOCUMENTBASE, denyingSocketPermissions(documentBaseLocalhost));
        String expected;
        if (b) {
            expected = SOCKET_DOCUMENTBASE_SUCCESS;
        } else {
            expected = SOCKET_DOCUMENTBASE_FAILURE;
        }
        evaluate(documentBaseLocalhost, b, i, expected, pr);
    }

    public static void assertUnrelatedConnection(ProcessResult pr, ServerLauncher unrelatedLocalhost) throws MalformedURLException, UnknownHostException {
        unrelatedImpl(true, true, true, pr, unrelatedLocalhost);
    }

    public static void unrelatedImpl(boolean url, boolean socket, boolean b, ProcessResult pr, ServerLauncher unrelatedLocalhost) throws MalformedURLException, UnknownHostException {
        printDevMessage("Unrelated " + b);
        if (url) {
            printDevMessage("Url");
            assertUnrelatedUrlConnection(pr, b, unrelatedLocalhost);
        }
        if (socket) {
            printDevMessage("Socket");
            assertUnrelatedSocketConnection(pr, b, unrelatedLocalhost);
        }
    }

    public static void assertNoUnrelatedConnection(ProcessResult pr, ServerLauncher unrelatedLocalhost) throws MalformedURLException, UnknownHostException {
        unrelatedImpl(true, true, false, pr, unrelatedLocalhost);
    }

    public static void assertUnrelatedUrlConnection(ProcessResult pr, boolean b, ServerLauncher unrelatedLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(URL_UNRELATED, pr.stdout.contains(URL_UNRELATED));
        int i = countStrings(pr.stdout, URL_DOCUMENTBASE, URL_UNRELATED, denyingUrlPermissions(unrelatedLocalhost));
        String expected;
        if (b) {
            expected = URL_UNRELATED_SUCCESS;
        } else {
            expected = URL_UNRELATED_FAILURE;
        }
        evaluate(unrelatedLocalhost, b, i, expected, pr);
    }

    public static void assertUnrelatedSocketConnection(ProcessResult pr, boolean b, ServerLauncher unrelatedLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(SOCKET_UNRELATED, pr.stdout.contains(SOCKET_UNRELATED));
        int i = countStrings(pr.stdout, SOCKET_DOCUMENTBASE, SOCKET_UNRELATED, denyingSocketPermissions(unrelatedLocalhost));
        String expected;
        if (b) {
            expected = SOCKET_UNRELATED_SUCCESS;
        } else {
            expected = SOCKET_UNRELATED_FAILURE;
        }
        evaluate(unrelatedLocalhost, b, i, expected, pr);
    }

    public static void assertResourcesConnection(ProcessResult pr, ServerLauncher resourcesLocalhost) throws MalformedURLException, UnknownHostException {
        resourcesImpl(true, true, true, pr, resourcesLocalhost);
    }

    public static void resourcesImpl(boolean url, boolean socket, boolean b, ProcessResult pr, ServerLauncher resourcesLocalhost) throws MalformedURLException, UnknownHostException {
        printDevMessage("Resource's " + b);
        if (url) {
            printDevMessage("Url");
            assertResourcesUrlConnection(pr, b, resourcesLocalhost);
        }
        if (socket) {
            printDevMessage("Socket");
            assertResourcesSocketConnection(pr, b, resourcesLocalhost);
        }
    }

    public static void assertNoResourcesConnection(ProcessResult pr, ServerLauncher resourcesLocalhost) throws MalformedURLException, UnknownHostException {
        resourcesImpl(true, true, false, pr, resourcesLocalhost);
    }

    public static void assertResourcesUrlConnection(ProcessResult pr, boolean b, ServerLauncher resourcesLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(URL_RESOURCES, pr.stdout.contains(URL_UNRELATED));
        int i = countStrings(pr.stdout, URL_UNRELATED, URL_RESOURCES, denyingUrlPermissions(resourcesLocalhost));
        String expected;
        if (b) {
            expected = URL_RESOURCES_SUCCESS;
        } else {
            expected = URL_RESOURCES_FAILURE;
        }
        evaluate(resourcesLocalhost, b, i, expected, pr);

    }

    public static void assertResourcesSocketConnection(ProcessResult pr, boolean b, ServerLauncher resourcesLocalhost) throws MalformedURLException, UnknownHostException {
        assertTrue(SOCKET_UNRELATED, pr.stdout.contains(SOCKET_UNRELATED));
        int i = countStrings(pr.stdout, SOCKET_UNRELATED, SOCKET_RESOURCES, denyingSocketPermissions(resourcesLocalhost));
        String expected;
        if (b) {
            expected = SOCKET_RESOURCES_SUCCESS;
        } else {
            expected = SOCKET_RESOURCES_FAILURE;
        }
        evaluate(resourcesLocalhost, b, i, expected, pr);
    }

    /**
     * This is tricky nasty method. Issue with testing this reproducer on
     * localhost rise, when jdk built shortcut for localhost connections. So the
     * itw's checkPermission of jnlpclassloader is called, but jdk happily
     * consume this. So for some connections the exception is thrown, and resutl
     * is in assertAchieved, some just report, and result is in assertCounts
     * Thats also why somtetimes it OR and sometimes AND between connections.
     * When connection is expected (b == true) then only one failure is enough
     * to make the test fail. When connection is expected to be denied,(b ==
     * false) then only both check must fail to result into test failure.
     *
     * @param usedServer - if null then none, if not null, then the one used for
     * this particular test (eg docbase, or codebase, or resoures base...)
     * @param b - true - should be connected, false should not be connected
     * @param i - hoe many occurrences were counted
     * @param expected - expected string in app output
     * @param pr - processresult where to searh
     */
    private static void evaluate(ServerLauncher usedServer, boolean b, int i, String expected, ProcessResult pr) {
        AssertionError f1 = null;
        if (usedServer != null) {
            f1 = assertCounts(b, i);
        }
        AssertionError f2 = assertAchieved(expected, pr);
        AssertionError throwme = null;
        if (!b) {
            //considering the test as failed, both conditions must fail
            //of course when  usedServer is in play
            if (usedServer == null) {
                //otherwise the only one is enough
                if (f2 != null) {
                    printDevMessage("FAILED 1!");
                    throwme = f2;
                }
            } else {
                if (f2 != null && f1 != null) {
                    printDevMessage("FAILED 2!");
                    throwme = f2;
                }
            }
            if (throwme != null) {
                throw throwme;
            }
            printDevMessage("Passed 1.");
        } else {
            //considering the test as failed, one condition is enough to fail
            if (f1 == null && f2 != null) {
                printDevMessage("FAILED 3!");
                throwme = f2;
            }
            if (f2 == null && f1 != null) {
                printDevMessage("FAILED 4!");
                throwme = f1;
            }
            if (f2 != null || f1 != null) {
                printDevMessage("FAILED 5!");
                if (f2 != null) {
                    throwme = f2; //better one
                } else {
                    throwme = f1;
                }
            }
            if (throwme != null) {
                throw throwme;
            }
            printDevMessage("Passed. 2");
        }
    }

    private static AssertionError assertAchieved(String expected, ProcessResult pr) {
        try {
            assertAchievedImpl(expected, pr);
        } catch (AssertionError e) {
            return e;
        }
        return null;
    }

    private static AssertionError assertCounts(boolean b, int i) {
        try {
            assertCountsImpl(b, i);
        } catch (AssertionError e) {
            return e;
        }
        return null;
    }

    private static void assertAchievedImpl(String expected, ProcessResult pr) {
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    private static void assertCountsImpl(boolean b, int i) {
        if (b) {
            Assert.assertEquals("`Denying permission: (` was not expected, but occurred (on subsegment) " + i + " times", 0, i);
        } else {
            Assert.assertTrue("`Denying permission: (` was expected, but  did not occurred (on subsegment)", i >= 1); //test and impl depndentt
        }
    }

    private static void printDevMessage(String string) {
        //System.out.println(string);
    }

    @Test
    public void testHtmlSetCode() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"code=\"", doc.toString().contains("code="));
        doc.setCode("foo");
        assertTrue("Doc should contain \"code=\"foo\"\"", doc.toString().contains("code=\"foo\""));
    }

    @Test
    public void testHtmlSetCodeEmpty() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"code=\"", doc.toString().contains("code="));
        doc.setCode("");
        assertFalse("Doc should not contain \"code=\"", doc.toString().contains("code="));
    }

    @Test
    public void testHtmlSetArchive() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"archive=\"", doc.toString().contains("archive="));
        doc.setArchive("foo");
        assertTrue("Doc should contain \"archive=\"foo\"\"", doc.toString().contains("archive=\"foo\""));
    }

    @Test
    public void testHtmlSetArchiveEmpty() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"archive=\"", doc.toString().contains("archive="));
        doc.setArchive("");
        assertFalse("Doc should not contain \"archive=\"", doc.toString().contains("archive="));
    }

    @Test
    public void testHtmlSetCodebase() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"codebase=\"", doc.toString().contains("codebase="));
        doc.setCodeBase("foo");
        assertTrue("Doc should contain \"codebase=\"foo\"\"", doc.toString().contains("codebase=\"foo\""));
    }

    @Test
    public void testHtmlSetCodebaseEmpty() throws Exception {
        TemplatedHtmlDoc doc = new TemplatedHtmlDoc(server, "SOPBypass.html");
        assertFalse("Doc should not contain \"codebase=\"", doc.toString().contains("codebase="));
        doc.setCodeBase("");
        assertFalse("Doc should not contain \"codebase=\"", doc.toString().contains("codebase="));
    }

    @Test
    public void testAppletParamReplacement2() throws Exception {
        AppletTemplate doc = new AppletTemplate(server, "SOPBypass.html") {
        };
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        doc.setAppletParams("param1", null, null);
        assertTrue("Doc should contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
    }

    @Test
    public void testAppletParamReplacement1() throws Exception {
        AppletTemplate doc = new AppletTemplate(server, "SOPBypass.jnlp") {
        };
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        doc.setAppletParams(null, "param2", null);
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        assertTrue("Doc should contain param2", doc.toString().contains("param2"));
    }

    @Test
    public void testAppletParamReplacement3() throws Exception {
        AppletTemplate doc = new AppletTemplate(server, "SOPBypass.html") {
        };
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        doc.setAppletParams(null, null, null);
        assertFalse("Doc should not contain \"codebase=\"", doc.toString().contains("codebase="));
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
    }

    @Test
    public void testAppletParamReplacement4() throws Exception {
        AppletTemplate doc = new AppletTemplate(server, "SOPBypass.jnlp") {
        };
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        doc.setAppletParams("param1", "param2", "nonUrlParam");
        assertTrue("Doc should contain param1", doc.toString().contains("param1"));
        assertTrue("Doc should contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain nonUrlParam", doc.toString().contains("nonUrlParam"));
    }

    @Test
    public void testAppletParamReplacement5() throws Exception {
        AppletTemplate doc = new AppletTemplate(server, "SOPBypass.jnlp") {
        };
        assertFalse("Doc should not contain param1", doc.toString().contains("param1"));
        assertFalse("Doc should not contain param2", doc.toString().contains("param2"));
        assertFalse("Doc should not contain param3", doc.toString().contains("param3"));
        doc.setAppletParams("param1", "param2", "http://some.url:123/somePath");
        assertTrue("Doc should contain param1", doc.toString().contains("param1"));
        assertTrue("Doc should contain param2", doc.toString().contains("param2"));
        assertTrue("Doc should contain http://some.url:123", doc.toString().contains("http://some.url:123"));
    }

    public static abstract class BasicTempalte {

        protected final String docName;
        protected final ServerAccess access;
        protected String content = null;

        public BasicTempalte(ServerAccess access, String docName) throws IOException {
            this.docName = docName;
            this.access = access;
            content = access.getResourceAsString(docName);
        }

        @Override
        public String toString() {
            return content;
        }

        public String getFileName() {
            String[] parts = docName.split(Pattern.quote("."));
            String name = parts[0];
            String extension = parts[1];
            return name + "-filtered." + extension;
        }

        public File getLocation() {
            return new File(access.getDir(), getFileName());
        }

        public void save() throws IOException {
            //This can help with socket permissions, but is making test unstable
            ServerAccess.saveFile(content.replace("localhost", InetAddress.getLocalHost().getHostName()), getLocation());
            printDevMessage("Replaced all localhost by " + InetAddress.getLocalHost().getHostName() + " in " + getLocation());
            //ServerAccess.saveFile(content, getLocation());
        }

    }

    public static abstract class AppletTemplate extends BasicTempalte {

        static final String CODEBASE_TOKEN = "CODEBASE_REPLACEMENT_TOKEN";
        static final String APPLET_PARAMS_TOKEN = "APPLET_PARAMS_TOKEN";

        public AppletTemplate(ServerAccess access, String docName) throws IOException {
            super(access, docName);
        }

        public String setAppletParams(String unrelatedUrl, String reachableResource, String resource) {
            String debug = "";
            String urlParam = "";
            if (unrelatedUrl != null) {
                urlParam = "<PARAM NAME=\"unrelatedUrl\" VALUE=\"" + unrelatedUrl + "\">";
                debug += " passed unrelated: " + unrelatedUrl;
            }
            String resourceParam = "";
            if (reachableResource != null) {
                resourceParam = "<PARAM NAME=\"reachableResource\" VALUE=\"" + reachableResource + "\">";
                debug += " passed reachable-resource: " + reachableResource;
            }
            String resourceUrlParam = "";
            String resourcesUrl = resourceToResourcesUrl(resource);
            if (resourcesUrl != null) {
                resourceUrlParam = "<PARAM NAME=\"resourceUrl\" VALUE=\"" + resourcesUrl + "\">";
                debug += " passed jar's-url: " + resourcesUrl;
            }
            String params = urlParam + "\n" + resourceParam + "\n" + resourceUrlParam + "\n";
            content = content.replaceAll(APPLET_PARAMS_TOKEN, params);
            return debug;
        }

        private String resourceToResourcesUrl(String resource) {
            if (resource == null) {
                return null;
            }
            try {
                URL u = new URL(resource);
                String pr = u.getProtocol();
                String h = u.getHost();
                int po = u.getPort();
                if (po == -1) {
                    po = 80;
                }
                return pr + "://" + h + ":" + po;
            } catch (MalformedURLException ex) {
                // . name.jar or similar on-codebase-resources
                return null;
            }
        }

    }

    public static class TemplatedHtmlDoc extends AppletTemplate {

        private static final String CODE_TOKEN = "CODE_REPLACEMENT_TOKEN";
        private static final String ARCHIVE_TOKEN = "ARCHIVE_REPLACEMENT_TOKEN";
        private static final String NEWLINE = System.lineSeparator();

        public TemplatedHtmlDoc(ServerAccess access, String resourceLocation) throws IOException {
            super(access, resourceLocation);
        }

        public void setCode(String code) {
            if (code == null || code.isEmpty()) {
                content = content.replaceAll(CODE_TOKEN, NEWLINE);
            } else {
                content = content.replaceAll(CODE_TOKEN, "code=\"" + code + "\"" + NEWLINE);
            }
        }

        public void setArchive(String archive) {
            if (archive == null || archive.isEmpty()) {
                content = content.replaceAll(ARCHIVE_TOKEN, NEWLINE);
            } else {
                content = content.replaceAll(ARCHIVE_TOKEN, "archive=\"" + archive + "\"" + NEWLINE);
            }
        }

        public void setCodeBase(String codeBase) {
            if (codeBase == null || codeBase.isEmpty()) {
                content = content.replaceAll(CODEBASE_TOKEN, NEWLINE);
            } else {
                content = content.replaceAll(CODEBASE_TOKEN, "codebase=\"" + codeBase + "\"" + NEWLINE);
            }
        }

    }

    public static class TemplatedJnlpDoc extends AppletTemplate {

        private static final String DOCUMENTBASE_TOKEN = "DOCUMENTBASE_REPLACEMENT_TOKEN";
        private static final String JAR_TOKEN = "JAR_HREF_REPLACEMENT_TOKEN";

        public TemplatedJnlpDoc(ServerAccess access, String resourceLocation) throws IOException {
            super(access, resourceLocation);
        }

        public void setDocumentBase(String documentBase) {
            String replacement;
            if (documentBase == null || documentBase.isEmpty()) {
                replacement = ".";
            } else {
                replacement = documentBase;
            }
            content = content.replaceAll(DOCUMENTBASE_TOKEN, replacement);
        }

        public void setCodeBase(String codeBase) {
            String replacement;
            if (codeBase == null || codeBase.isEmpty()) {
                replacement = ".";
            } else {
                replacement = codeBase;
            }
            content = content.replaceAll(CODEBASE_TOKEN, replacement);
        }

        public void setJarHref(String jarHref) {
            content = content.replaceAll(JAR_TOKEN, jarHref);
        }

    }

    @Test
    public void countStringsTests() {
        int c = countString("hello bayby", "Hello bayby!\n"
                + "How hello bayby are you. hello bayby is my word!\n"
                + "So hello bayby. please promiss hell\n"
                + "best regards, hello bayby");
        Assert.assertEquals(4, c);
    }

    @Test
    public void countStringsAnchorsTests() {
        int c1 = countString("1", "1231567190");
        Assert.assertEquals(3, c1);
        int c2 = countString("1", "1231567190", 2, 6);
        Assert.assertEquals(1, c2);
        int c3 = countString("1", "1231567190", "2", "0");
        Assert.assertEquals(2, c3);
        c3 = countString("1", "1231567190", "3", "9");
        Assert.assertEquals(2, c3);
        c3 = countString("1", "1231567190", "3", "7");
        Assert.assertEquals(1, c3);
        c3 = countString("1", "1231567190", "5", "7");
        Assert.assertEquals(0, c3);
        c3 = countString("1", "1231567190", "6", "7");
        Assert.assertEquals(0, c3);

        c3 = countString("1", "1231567190", null, "7");
        Assert.assertEquals(2, c3);
        c3 = countString("1", "1231567190", "3", null);
        Assert.assertEquals(2, c3);
        c3 = countString("1", "1231567190", null, null);
        Assert.assertEquals(3, c3);
        c3 = countStrings("1231567190", null, null, "2", "3", "4", "5", "8", "0");
        Assert.assertEquals(3, c3);
    }

    public static int countStrings(String where, String fromAnchor, String toAnchor, String... what) {
        int count = 0;
        for (String string : what) {
            count += countString(string, where, fromAnchor, toAnchor);
        }
        return count;
    }

    public static int countString(String what, String where, String fromAnchor, String toAnchor) {
        int i1 = 0;
        if (fromAnchor != null) {
            i1 = where.indexOf(fromAnchor);
        }
        if (i1 < 0) {
            i1 = 0;
        }
        int i2 = 0;
        if (toAnchor != null) {
            if (fromAnchor != null) {
                i2 = where.indexOf(toAnchor, i1 + fromAnchor.length() - 1);
            } else {
                i2 = where.indexOf(toAnchor);
            }
        }
        if (i2 <= i1) {
            i2 = where.length() - 1;
        }
        return countString(what, where, i1, i2);
    }

    public static int countString(String what, String where, int from, int to) {
        return countString(what, where.substring(from, to));
    }

    public static int countString(String what, String where) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = where.indexOf(what, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += what.length();
            }
        }
        return count;
    }

    public static String[] denyingUrlPermissions(ServerLauncher s) throws MalformedURLException, UnknownHostException {
        if (s == null) {
            return new String[0];
        }
        List<URL> l = s.getUrlAliases(""); //we never know which will be evaulated against permissiion
        String[] r = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            URL url = l.get(i);
            r[i] = denyingUrlPermission(url.toExternalForm());
        }
        return r;
    }

    public static String[] denyingSocketPermissions(ServerLauncher s) throws MalformedURLException, UnknownHostException {
        if (s == null) {
            return new String[0];
        }
        List<URL> l = s.getUrlAliases(""); //we never know which will be evaulated against permissiion
        String[] r = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            URL url = l.get(i);
            r[i] = denyingSocektPermission(url.toExternalForm());
        }
        return r;
    }

    public static String denyingPermission(String permission, String url) {
        //Denying permission: ("java.net.URLPermission" "http://localhost:37271/codebase/" "GET:")
        //Denying permission: ("java.net.SocketPermission" "127.0.0.1:60036" "connect,resolve")
        return "Denying permission: (\"" + permission + "\" \"" + url;
    }

    public static String denyingUrlPermission(String address) {
        //Denying permission: ("java.net.URLPermission" "http://localhost:37271/codebase/" "GET:")
        return denyingPermission("java.net.URLPermission", address.replaceAll("/$", ""));
    }

    public static String denyingSocektPermission(String address) {
        //Denying permission: ("java.net.SocketPermission" "127.0.0.1:60036" "connect,resolve")
        return denyingPermission("java.net.SocketPermission", address.replaceAll("http.*://", "").replaceAll("/$", ""));
    }

}
