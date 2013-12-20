/* ResourceTrackerTest.java
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
package net.sourceforge.jnlp.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.UrlUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceTrackerTest {

    public static ServerLauncher testServer;
    public static ServerLauncher testServerWithBrokenHead;
    private static PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;
    private static final String nameStub1 = "itw-server";
    private static final String nameStub2 = "test-file";

    @Test
    public void testNormalizeUrl() throws Exception {
        URL[] u = getUrls();

        URL[] n = getNormalizedUrls();

        Assert.assertNull("first url should be null", u[0]);
        Assert.assertNull("first normalized url should be null", n[0]);
        for (int i = 1; i < CHANGE_BORDER; i++) {
            Assert.assertTrue("url " + i + " must be equals too normalized url " + i, u[i].equals(n[i]));
        }
        for (int i = CHANGE_BORDER; i < n.length; i++) {
            Assert.assertFalse("url " + i + " must be normalized (and so not equals) too normalized url " + i, u[i].equals(n[i]));
        }
    }
    public static final int CHANGE_BORDER = 8;

    public static URL[] getUrls() throws MalformedURLException {
        URL[] u = {
            /*constant*/
            null,
            new URL("file:///home/jvanek/Desktop/icedtea-web/tests.build/jnlp_test_server/Spaces%20can%20be%20everywhere2.jnlp"),
            new URL("http://localhost:44321/SpacesCanBeEverywhere1.jnlp"),
            new URL("http:///SpacesCanBeEverywhere1.jnlp"),
            new URL("file://localhost/home/jvanek/Desktop/icedtea-web/tests.build/jnlp_test_server/Spaces can be everywhere2.jnlp"),
            new URL("http://localhost:44321/testpage.jnlp?applicationID=25"),
            new URL("http://localhost:44321/Spaces%20Can%20Be%20Everyw%2Fhere1.jnlp"),
            new URL("http://localhost/Spaces+Can+Be+Everywhere1.jnlp"),
            /*changing*/
            new URL("http://localhost/SpacesC anBeEverywhere1.jnlp?a=5&b=10#df"),
            new URL("http:///oook.jnlp?a=5&b=ahoj šš dd#df"),
            new URL("http://localhost/Spacesěčšžšřýžčřú can !@^*(){}[].jnlp?a=5&ahoj šš dd#df"),
            new URL("http://localhost:44321/SpaŠcesCan Be Everywhere1.jnlp"),
            new URL("http:/SpacesCanB eEverywhere1.jnlp")};
        return u;
    }

    public static URL[] getNormalizedUrls() throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        URL[] u = getUrls();

        URL[] n = new URL[u.length];
        for (int i = 0; i < n.length; i++) {
            n[i] = UrlUtils.normalizeUrl(u[i]);
        }
        return n;

    }

    @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() throws IOException {
        for (int i = 0; i < backedUpStream.length; i++) {
            if (backedUpStream[i] == null) {
                switch (i) {
                    case 0:
                        backedUpStream[i] = System.out;
                        break;
                    case 1:
                        backedUpStream[i] = System.err;
                        break;
                    case 2:
                        backedUpStream[i] = OutputController.getLogger().getOut();
                        break;
                    case 3:
                        backedUpStream[i] = OutputController.getLogger().getErr();
                        break;
                }

            }

        }
        currentErrorStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(currentErrorStream));
        System.setErr(new PrintStream(currentErrorStream));
        OutputController.getLogger().setOut(new PrintStream(currentErrorStream));
        OutputController.getLogger().setErr(new PrintStream(currentErrorStream));


    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString("utf-8"));
        System.setOut(backedUpStream[0]);
        System.setErr(backedUpStream[1]);
        OutputController.getLogger().setOut(backedUpStream[2]);
        OutputController.getLogger().setErr(backedUpStream[3]);


    }

    @BeforeClass
    public static void onDebug() {
        JNLPRuntime.setDebug(true);
    }

    @AfterClass
    public static void offDebug() {
        JNLPRuntime.setDebug(false);
    }

    @BeforeClass
    public static void startServer() throws Exception {
        redirectErr();
        testServer = ServerAccess.getIndependentInstance(System.getProperty("java.io.tmpdir"), ServerAccess.findFreePort());
        redirectErrBack();
    }

    @BeforeClass
    public static void startServer2() throws Exception {
        redirectErr();
        testServerWithBrokenHead = ServerAccess.getIndependentInstance(System.getProperty("java.io.tmpdir"), ServerAccess.findFreePort());
        testServerWithBrokenHead.setSupportingHeadRequest(false);
        redirectErrBack();
    }

    @AfterClass
    public static void stopServer() {
        testServer.stop();
    }

    @AfterClass
    public static void stopServer2() {
        testServerWithBrokenHead.stop();
    }

    @Test
    public void getUrlResponseCodeTestWorkingHeadRequest() throws Exception {
        redirectErr();
        try {
            File f = File.createTempFile(nameStub1, nameStub2);
            int i = ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "HEAD");
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "HEAD");
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, i);
        } finally {
            redirectErrBack();
        }
    }

    @Test
    public void getUrlResponseCodeTestNotWorkingHeadRequest() throws Exception {
        redirectErr();
        try {
            File f = File.createTempFile(nameStub1, nameStub2);
            int i = ResourceTracker.getUrlResponseCode(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<String, String>(), "HEAD");
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, i);
            f.delete();
            i = ResourceTracker.getUrlResponseCode(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<String, String>(), "HEAD");
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, i);
        } finally {
            redirectErrBack();
        }
    }

    @Test
    public void getUrlResponseCodeTestGetRequestOnNotWorkingHeadRequest() throws Exception {
        redirectErr();
        try {
            File f = File.createTempFile(nameStub1, nameStub2);
            int i = ResourceTracker.getUrlResponseCode(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<String, String>(), "GET");
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceTracker.getUrlResponseCode(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<String, String>(), "GET");
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, i);
        } finally {
            redirectErrBack();
        }
    }

    @Test
    public void getUrlResponseCodeTestGetRequest() throws Exception {
        redirectErr();
        try {
            File f = File.createTempFile(nameStub1, nameStub2);
            int i = ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "GET");
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "GET");
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, i);
        } finally {
            redirectErrBack();
        }
    }

    @Test
    public void getUrlResponseCodeTestWrongRequest() throws Exception {
        redirectErr();
        try {
            File f = File.createTempFile(nameStub1, nameStub2);
            Exception exception = null;
            try {
                ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "SomethingWrong");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNotNull(exception);
            exception = null;
            f.delete();
            try {
                ResourceTracker.getUrlResponseCode(testServer.getUrl(f.getName()), new HashMap<String, String>(), "SomethingWrong");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNotNull(exception);;
        } finally {
            redirectErrBack();
        }

    }

    @Test
    public void findBestUrltest() throws Exception {
        redirectErr();
        try {
            File fileForServerWithHeader = File.createTempFile(nameStub1, nameStub2);
            File versionedFileForServerWithHeader = new File(fileForServerWithHeader.getParentFile(), fileForServerWithHeader.getName() + "-2.0");
            versionedFileForServerWithHeader.createNewFile();

            File fileForServerWithoutHeader = File.createTempFile(nameStub1, nameStub2);
            File versionedFileForServerWithoutHeader = new File(fileForServerWithoutHeader.getParentFile(), fileForServerWithoutHeader.getName() + "-2.0");
            versionedFileForServerWithoutHeader.createNewFile();

            ResourceTracker rt = new ResourceTracker();
            Resource r1 = Resource.getResource(testServer.getUrl(fileForServerWithHeader.getName()), null, UpdatePolicy.NEVER);
            Resource r2 = Resource.getResource(testServerWithBrokenHead.getUrl(fileForServerWithoutHeader.getName()), null, UpdatePolicy.NEVER);
            Resource r3 = Resource.getResource(testServer.getUrl(versionedFileForServerWithHeader.getName()), new Version("1.0"), UpdatePolicy.NEVER);
            Resource r4 = Resource.getResource(testServerWithBrokenHead.getUrl(versionedFileForServerWithoutHeader.getName()), new Version("1.0"), UpdatePolicy.NEVER);
            assertOnServerWithHeader(rt.findBestUrl(r1));
            assertVersionedOneOnServerWithHeader(rt.findBestUrl(r3));
            assertOnServerWithoutHeader(rt.findBestUrl(r2));
            assertVersionedOneOnServerWithoutHeader(rt.findBestUrl(r4));

            fileForServerWithHeader.delete();
            Assert.assertNull(rt.findBestUrl(r1));
            assertVersionedOneOnServerWithHeader(rt.findBestUrl(r3));
            assertOnServerWithoutHeader(rt.findBestUrl(r2));
            assertVersionedOneOnServerWithoutHeader(rt.findBestUrl(r4));

            versionedFileForServerWithHeader.delete();
            Assert.assertNull(rt.findBestUrl(r1));
            Assert.assertNull(rt.findBestUrl(r3));
            assertOnServerWithoutHeader(rt.findBestUrl(r2));
            assertVersionedOneOnServerWithoutHeader(rt.findBestUrl(r4));

            versionedFileForServerWithoutHeader.delete();
            Assert.assertNull(rt.findBestUrl(r1));
            Assert.assertNull(rt.findBestUrl(r3));
            assertOnServerWithoutHeader(rt.findBestUrl(r2));
            Assert.assertNull(rt.findBestUrl(r4));


            fileForServerWithoutHeader.delete();
            Assert.assertNull(rt.findBestUrl(r1));
            Assert.assertNull(rt.findBestUrl(r3));
            Assert.assertNull(rt.findBestUrl(r2));
            Assert.assertNull(rt.findBestUrl(r4));
        } finally {
            redirectErrBack();
        }

    }

    private void assertOnServerWithHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServer.getPort());
    }

    private void assertVersionedOneOnServerWithHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServer.getPort());
        assertVersion(u);
    }

    private void assertOnServerWithoutHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServerWithBrokenHead.getPort());
    }

    private void assertVersionedOneOnServerWithoutHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServerWithBrokenHead.getPort());
        assertVersion(u);
    }

    private void assertCommonComponentsOfUrl(URL u) {
        Assert.assertTrue(u.getProtocol().equals("http"));
        Assert.assertTrue(u.getHost().equals("localhost"));
        Assert.assertTrue(u.getPath().contains(nameStub1));
        Assert.assertTrue(u.getPath().contains(nameStub2));
        ServerAccess.logOutputReprint(u.toExternalForm());
    }

    private void assertPort(URL u, int port) {
        Assert.assertTrue(u.getPort() == port);
    }

    private void assertVersion(URL u) {
        Assert.assertTrue(u.getPath().contains("-2.0"));
        Assert.assertTrue(u.getQuery().contains("version-id=1.0"));
    }
}
