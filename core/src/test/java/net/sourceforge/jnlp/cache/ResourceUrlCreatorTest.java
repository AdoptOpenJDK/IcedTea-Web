package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static org.junit.Assert.assertEquals;

public class ResourceUrlCreatorTest extends NoStdOutErrTest{

    private static final VersionString VERSION_11 = VersionString.fromString("1.1");
    private static final VersionString VERSION_20 = VersionString.fromString("2.0");
    private static final VersionString VERSION_STRING = VersionString.fromString("2.3.0 2.3.1");
    private static final DownloadOptions DLOPTS_NOPACK_USEVERSION = new DownloadOptions(false, true);
    private static final DownloadOptions DLOPTS_NOPACK_NOVERSION = new DownloadOptions(false, false);

    private static ServerLauncher testServer;
    private static ServerLauncher testServerWithBrokenHead;
    private static final String nameStub1 = "itw-server";
    private static final String nameStub2 = "test-file";
    private static final PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;

    private URL getResultUrl(final String url, final VersionString version, final boolean usePack /*use pack.gz suffix*/, final boolean useVersion /*use version suffix*/) throws MalformedURLException {
        final Resource resource = Resource.createResource(new URL(url), version, null, null);
        return ResourceUrlCreator.getUrl(resource, usePack, useVersion);
    }

    private URL getResultUrl(final String url, final VersionString version, final DownloadOptions downloadOptions) throws MalformedURLException {
        Resource resource = Resource.createResource(new URL(url), version, null, null);
        return ResourceUrlCreator.getVersionedUrl(resource);
    }

    @Test
    public void testVersionEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionEncode.jar", VERSION_11, false, true);
        assertEquals("http://example.com/versionEncode__V1.1.jar", result.toString());
    }

    @Test
    public void testVersionWithPeriods() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/test.version.with.periods.jar", VERSION_11, false, true);
        // A previous bug had this as "test__V1.1.with.periods.jar"
        assertEquals("http://example.com/test.version.with.periods__V1.1.jar", result.toString());
    }

    @Test
    public void testPackEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/packEncode.jar", VERSION_11, true, false);
        assertEquals("http://example.com/packEncode.jar.pack.gz", result.toString());
    }

    @Test
    public void testVersionAndPackEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionAndPackEncode.jar", VERSION_11, true, true);
        assertEquals("http://example.com/versionAndPackEncode__V1.1.jar.pack.gz", result.toString());
    }

    @Test
    public void testGetVersionedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrl.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrl.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testGetNonVersionIdUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/nonVersionIdUrl.jar", VERSION_STRING, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/nonVersionIdUrl.jar?version-id=2.3.0 2.3.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithQuery.jar?i=1234abcd", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrlWithQuery.jar?i=1234abcd&version-id=1.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersion() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithoutVersion.jar", null, DLOPTS_NOPACK_NOVERSION);
        assertEquals("http://example.com/versionedUrlWithoutVersion.jar", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersionWithQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd", null, DLOPTS_NOPACK_NOVERSION);
        assertEquals("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithLongQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd", VERSION_20, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd&version-id=2.0", result.toString());
    }

    @Test
    public void testPercentEncoded() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/percent encoded.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/percent encoded.jar", result.toString());
    }

    @Test
    public void testPercentEncodedOnlyOnce() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/percent%20encoded%20once.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/percent%20encoded%20once.jar", result.toString());
    }

    @Test
    public void testPartiallyEncodedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/partially encoded%20url.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/partially encoded%20url.jar", result.toString());
    }

    @Test
    public void testVersionedEncodedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versioned%20encoded.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versioned%20encoded.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testInvalidVersionedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/invalid versioned url.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/invalid versioned url.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testLongComplexUrl() throws MalformedURLException {
        String URL =
            "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexOne.jar";
        URL result = getResultUrl(URL, null, DLOPTS_NOPACK_USEVERSION);
        assertEquals(URL, result.toString());
    }

    @Test
    public void testLongComplexVersionedUrl() throws MalformedURLException {
        String URL =
            "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexTwo.jar";
        URL result = getResultUrl(URL, VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals(URL + "?version-id=" + VERSION_11, result.toString());
    }

    @Test
    public void testUserInfoAndVersioning() throws MalformedURLException {
        URL result = getResultUrl("http://foo:bar@example.com/userInfoAndVersion.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://foo:bar@example.com/userInfoAndVersion.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testPortAndVersioning() throws MalformedURLException {
        URL result = getResultUrl("http://example.com:1234/portAndVersioning.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com:1234/portAndVersioning.jar?version-id=1.1", result.toString());
    }




    @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() {
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
        ServerAccess.logErrorReprint(currentErrorStream.toString(UTF_8.name()));
        System.setOut(backedUpStream[0]);
        System.setErr(backedUpStream[1]);
        OutputController.getLogger().setOut(backedUpStream[2]);
        OutputController.getLogger().setErr(backedUpStream[3]);
    }


    @BeforeClass
    public static void startServer() throws Exception {
        redirectErr();
        testServer = ServerAccess.getIndependentInstance(System.getProperty(JAVA_IO_TMPDIR), ServerAccess.findFreePort());
        redirectErrBack();
    }

    @BeforeClass
    public static void startServer2() throws Exception {
        redirectErr();
        testServerWithBrokenHead = ServerAccess.getIndependentInstance(System.getProperty(JAVA_IO_TMPDIR), ServerAccess.findFreePort());
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
            int i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
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
            int i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, i);
            f.delete();
            i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
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
            int i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
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
            int i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, i);
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

            ResourceDownloader resourceDownloader = new ResourceDownloader(null, null);
            Resource r1 = Resource.createResource(testServer.getUrl(fileForServerWithHeader.getName()), null, null, UpdatePolicy.NEVER);
            Resource r2 = Resource.createResource(testServerWithBrokenHead.getUrl(fileForServerWithoutHeader.getName()), null, null, UpdatePolicy.NEVER);
            Resource r3 = Resource.createResource(testServer.getUrl(versionedFileForServerWithHeader.getName()), VersionString.fromString("1.0"), null, UpdatePolicy.NEVER);
            Resource r4 = Resource.createResource(testServerWithBrokenHead.getUrl(versionedFileForServerWithoutHeader.getName()), VersionString.fromString("1.0"), null, UpdatePolicy.NEVER);
            assertOnServerWithHeader(ResourceUrlCreator.findBestUrl(r1).getLocation());
            assertVersionedOneOnServerWithHeader(ResourceUrlCreator.findBestUrl(r3).getLocation());
            assertOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r2).getLocation());
            assertVersionedOneOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r4).getLocation());

            fileForServerWithHeader.delete();
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r1));
            assertVersionedOneOnServerWithHeader(ResourceUrlCreator.findBestUrl(r3).getLocation());
            assertOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r2).getLocation());
            assertVersionedOneOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r4).getLocation());

            versionedFileForServerWithHeader.delete();
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r1));
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r3));
            assertOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r2).getLocation());
            assertVersionedOneOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r4).getLocation());

            versionedFileForServerWithoutHeader.delete();
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r1));
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r3));
            assertOnServerWithoutHeader(ResourceUrlCreator.findBestUrl(r2).getLocation());
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r4));

            fileForServerWithoutHeader.delete();
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r1));
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r3));
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r2));
            Assert.assertNull(ResourceUrlCreator.findBestUrl(r4));
        } finally {
            redirectErrBack();
        }

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

    private void assertOnServerWithHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServer.getPort());
    }

    private void assertVersionedOneOnServerWithHeader(URL u) {
        assertCommonComponentsOfUrl(u);
        assertPort(u, testServer.getPort());
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
