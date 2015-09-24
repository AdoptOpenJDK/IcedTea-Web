/* SOPBypassUtil.java
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

import org.junit.AfterClass;
import org.junit.Test;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.ClosingListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.ServerLauncher;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class SOPBypassUtil extends BrowserTest {

    public static final String APPLET_START_STRING = "Applet Started";
    public static final String APPLET_CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    public static final String URL_CONNECTION_PREFIX = "URLConnection";
    public static final String SOCKET_CONNECTION_PREFIX = "SocketConnection";
    public static final String CONNECTION_DELIMITER = ":";
    public static final String CODEBASE = "codeBase";
    public static final String DOCUMENTBASE = "documentBase";
    public static final String UNRELATED = "unrelated";
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

    public static TemplatedHtmlDoc filterHtml(String doc, String code, String archive, String codebase) throws IOException {
        TemplatedHtmlDoc templatedDoc = new TemplatedHtmlDoc(server, doc);
        templatedDoc.setCode(code);
        templatedDoc.setArchive(archive);
        templatedDoc.setCodeBase(codebase);
        assertFalse(templatedDoc.toString(), templatedDoc.toString().contains("TOKEN"));
        templatedDoc.save();
        String content = server.getResourceAsString(templatedDoc.getFileName());
        assertFalse(content, content.contains("TOKEN"));
        return templatedDoc;
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, URL archive, URL codebase) throws IOException {
        return filterHtml(doc, code, archive == null ? "" : archive.toString(), codebase == null ? "" : codebase.toString());
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, URL archive, String codebase) throws IOException {
        return filterHtml(doc, code, archive == null ? "" : archive.toString(), codebase);
    }

    public static TemplatedHtmlDoc filterHtml(String doc, String code, String archive, URL codebase) throws IOException {
        return filterHtml(doc, code, archive, codebase == null ? "" : codebase.toString());
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, String jarHref, String codebase) throws IOException {
        TemplatedJnlpDoc templatedDoc = new TemplatedJnlpDoc(server, doc);
        templatedDoc.setJarHref(jarHref);
        templatedDoc.setCodeBase(codebase);
        templatedDoc.setDocumentBase(server.getUrl("SOPBypass.jnlp").toString());
        assertFalse(templatedDoc.toString(), templatedDoc.toString().contains("TOKEN"));
        templatedDoc.save();
        String content = server.getResourceAsString(templatedDoc.getFileName());
        assertFalse(content, content.contains("TOKEN"));
        return templatedDoc;
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, URL archive, URL codebase) throws IOException {
        return filterJnlp(doc, archive == null ? "" : archive.toString(), codebase == null ? "" : codebase.toString());
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, URL archive, String codebase) throws IOException {
        return filterJnlp(doc, archive == null ? "" : archive.toString(), codebase);
    }

    public static TemplatedJnlpDoc filterJnlp(String doc, String archive, URL codebase) throws IOException {
        return filterJnlp(doc, archive, codebase == null ? "" : codebase.toString());
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

    public static void assertCodebaseConnection(ProcessResult pr) {
        assertUrlCodebase(pr, true);
        assertSocketCodebase(pr, true);
    }

    public static void assertNoCodebaseConnection(ProcessResult pr) {
        assertUrlCodebase(pr, false);
        assertSocketCodebase(pr, false);
    }

    public static void assertUrlCodebase(ProcessResult pr, boolean b) {
        assertTrue(URL_CODEBASE, pr.stdout.contains(URL_CODEBASE));
        String expected;
        if (b) {
            expected = URL_CODEBASE_SUCCESS;
        } else {
            expected = URL_CODEBASE_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    public static void assertSocketCodebase(ProcessResult pr, boolean b) {
        assertTrue(SOCKET_CODEBASE, pr.stdout.contains(SOCKET_CODEBASE));
        String expected;
        if (b) {
            expected = SOCKET_CODEBASE_SUCCESS;
        } else {
            expected = SOCKET_CODEBASE_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    public static void assertDocumentBaseConnection(ProcessResult pr) {
        assertUrlDocumentBase(pr, true);
        assertSocketDocumentBase(pr, true);
    }

    public static void assertNoDocumentBaseConnection(ProcessResult pr) {
        assertUrlDocumentBase(pr, false);
        assertSocketDocumentBase(pr, false);
    }

    public static void assertUrlDocumentBase(ProcessResult pr, boolean b) {
        assertTrue(URL_DOCUMENTBASE, pr.stdout.contains(URL_DOCUMENTBASE));
        String expected;
        if (b) {
            expected = URL_DOCUMENTBASE_SUCCESS;
        } else {
            expected = URL_DOCUMENTBASE_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    public static void assertSocketDocumentBase(ProcessResult pr, boolean b) {
        assertTrue(SOCKET_DOCUMENTBASE, pr.stdout.contains(SOCKET_DOCUMENTBASE));
        String expected;
        if (b) {
            expected = SOCKET_DOCUMENTBASE_SUCCESS;
        } else {
            expected = SOCKET_DOCUMENTBASE_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    public static void assertUnrelatedConnection(ProcessResult pr) {
        assertUnrelatedUrlConnection(pr, true);
        assertUnrelatedSocketConnection(pr, true);
    }

    public static void assertNoUnrelatedConnection(ProcessResult pr) {
        assertUnrelatedUrlConnection(pr, false);
        assertUnrelatedSocketConnection(pr, false);
    }

    public static void assertUnrelatedUrlConnection(ProcessResult pr, boolean b) {
        assertTrue(URL_UNRELATED, pr.stdout.contains(URL_UNRELATED));
        String expected;
        if (b) {
            expected = URL_UNRELATED_SUCCESS;
        } else {
            expected = URL_UNRELATED_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
    }

    public static void assertUnrelatedSocketConnection(ProcessResult pr, boolean b) {
        assertTrue(SOCKET_UNRELATED, pr.stdout.contains(SOCKET_UNRELATED));
        String expected;
        if (b) {
            expected = SOCKET_UNRELATED_SUCCESS;
        } else {
            expected = SOCKET_UNRELATED_FAILURE;
        }
        assertTrue("Expected " + expected, pr.stdout.contains(expected));
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

    public static class TemplatedHtmlDoc {

        private static final String CODE_TOKEN = "CODE_REPLACEMENT_TOKEN";
        private static final String ARCHIVE_TOKEN = "ARCHIVE_REPLACEMENT_TOKEN";
        private static final String CODEBASE_TOKEN = "CODEBASE_REPLACEMENT_TOKEN";
        private static final String NEWLINE = System.lineSeparator();

        private String docName = null;
        private ServerAccess access = null;
        private String content = null;

        public TemplatedHtmlDoc(ServerAccess access, String resourceLocation) throws IOException {
            this.docName = resourceLocation;
            this.access = access;
            content = access.getResourceAsString(resourceLocation);
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
            access.saveFile(content, getLocation());
        }

    }

    public static class TemplatedJnlpDoc {

        private static final String DOCUMENTBASE_TOKEN = "DOCUMENTBASE_REPLACEMENT_TOKEN";
        private static final String CODEBASE_TOKEN = "CODEBASE_REPLACEMENT_TOKEN";
        private static final String JAR_TOKEN = "JAR_HREF_REPLACEMENT_TOKEN";

        private String docName;
        private ServerAccess access;
        private String content;

        public TemplatedJnlpDoc(ServerAccess access, String resourceLocation) throws IOException {
            this.docName = resourceLocation;
            this.access = access;
            content = access.getResourceAsString(resourceLocation);
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
            access.saveFile(content, getLocation());
        }

    }

}
