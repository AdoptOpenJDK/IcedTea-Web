package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourceUrlCreatorTest extends NoStdOutErrTest {

    private static final VersionString VERSION_11 = VersionString.fromString("1.1");
    private static final VersionString VERSION_20 = VersionString.fromString("2.0");
    private static final VersionString VERSION_STRING = VersionString.fromString("2.3.0 2.3.1");

    private static ServerLauncher testServer;
    private static ServerLauncher testServerWithBrokenHead;
    private static final PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File serverDir;

    @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() {
        backedUpStream[0] = System.out;
        backedUpStream[1] = System.err;
        backedUpStream[2] = OutputController.getLogger().getOut();
        backedUpStream[3] = OutputController.getLogger().getErr();

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

    @Before
    public void startServer() throws Exception {
        serverDir = temporaryFolder.newFolder();
        testServer = ServerAccess.getIndependentInstance(serverDir.getAbsolutePath(), ServerAccess.findFreePort());
        testServerWithBrokenHead = ServerAccess.getIndependentInstance(serverDir.getAbsolutePath(), ServerAccess.findFreePort());
        testServerWithBrokenHead.setSupportingHeadRequest(false);
        Thread.sleep(20);
    }

    @After
    public void stopServer() {
        testServerWithBrokenHead.stop();
        testServer.stop();
    }

    @Test
    public void testVersionEncode() throws MalformedURLException {
        URL result = getUrl("http://example.com/versionEncode.jar", VERSION_11, false, true);
        assertEquals("http://example.com/versionEncode__V1.1.jar", result.toString());
    }

    @Test
    public void testVersionWithPeriods() throws MalformedURLException {
        URL result = getUrl("http://example.com/test.version.with.periods.jar", VERSION_11, false, true);
        // A previous bug had this as "test__V1.1.with.periods.jar"
        assertEquals("http://example.com/test.version.with.periods__V1.1.jar", result.toString());
    }

    @Test
    public void testPackEncode() throws MalformedURLException {
        URL result = getUrl("http://example.com/packEncode.jar", null, true, false);
        assertEquals("http://example.com/packEncode.jar.pack.gz", result.toString());
    }

    @Test
    public void testVersionAndPackEncode() throws MalformedURLException {
        URL result = getUrl("http://example.com/versionAndPackEncode.jar", VERSION_11, true, true);
        assertEquals("http://example.com/versionAndPackEncode__V1.1.jar.pack.gz", result.toString());
    }

    @Test
    public void testGetVersionedUrl() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versionedUrl.jar", VERSION_11);
        assertEquals("http://example.com/versionedUrl.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testGetNonVersionIdUrl() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/nonVersionIdUrl.jar", VERSION_STRING);
        assertEquals("http://example.com/nonVersionIdUrl.jar?version-id=2.3.0+2.3.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithQuery() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versionedUrlWithQuery.jar?i=1234abcd", VERSION_11);
        assertEquals("http://example.com/versionedUrlWithQuery.jar?i=1234abcd&version-id=1.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersion() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versionedUrlWithoutVersion.jar", VERSION_20);
        assertEquals("http://example.com/versionedUrlWithoutVersion.jar?version-id=2.0", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersionWithQuery() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd", VERSION_20);
        assertEquals("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd&version-id=2.0", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithLongQuery() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd", VERSION_20);
        assertEquals("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd&version-id=2.0", result.toString());
    }

    @Test
    public void testPercentEncoded() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/percent encoded.jar", VERSION_20);
        assertEquals("http://example.com/percent encoded.jar?version-id=2.0", result.toString());
    }

    @Test
    public void testPercentEncodedOnlyOnce() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/percent%20encoded%20once.jar", VERSION_20);
        assertEquals("http://example.com/percent%20encoded%20once.jar?version-id=2.0", result.toString());
    }

    @Test
    public void testPartiallyEncodedUrl() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/partially encoded%20url.jar", VERSION_20);
        assertEquals("http://example.com/partially encoded%20url.jar?version-id=2.0", result.toString());
    }

    @Test
    public void testVersionedEncodedUrl() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/versioned%20encoded.jar", VERSION_11);
        assertEquals("http://example.com/versioned%20encoded.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testInvalidVersionedUrl() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com/invalid versioned url.jar", VERSION_11);
        assertEquals("http://example.com/invalid versioned url.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testLongComplexUrl() throws MalformedURLException {
        String URL = "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexOne.jar";
        URL result = getVersionedUrl(URL, VERSION_20);
        assertEquals(URL + "?version-id=2.0", result.toString());
    }

    @Test
    public void testLongComplexVersionedUrl() throws MalformedURLException {
        String URL = "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexTwo.jar";
        URL result = getVersionedUrl(URL, VERSION_11);
        assertEquals(URL + "?version-id=" + VERSION_11, result.toString());
    }

    @Test
    public void testUserInfoAndVersioning() throws MalformedURLException {
        URL result = getVersionedUrl("http://foo:bar@example.com/userInfoAndVersion.jar", VERSION_11);
        assertEquals("http://foo:bar@example.com/userInfoAndVersion.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testPortAndVersioning() throws MalformedURLException {
        URL result = getVersionedUrl("http://example.com:1234/portAndVersioning.jar", VERSION_11);
        assertEquals("http://example.com:1234/portAndVersioning.jar?version-id=1.1", result.toString());
    }

    @Test
    public void getUrlResponseCodeTestWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(existing.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }

    @Test
    public void getUrlResponseCodeTestNotWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(existing.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, second);
    }

    @Test
    public void getUrlResponseCodeTestGetRequestOnNotWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(existing.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }

    @Test
    public void getUrlResponseCodeTestGetRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(existing.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = ResourceUrlCreator.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }

    private URL getUrl(final String url, final VersionString version, final boolean usePack, final boolean useVersion) throws MalformedURLException {
        final Resource resource = Resource.createResource(new URL(url), version, null, null);
        return ResourceUrlCreator.getUrl(resource, usePack, useVersion);
    }

    private URL getVersionedUrl(final String url, final VersionString version) throws MalformedURLException {
        Resource resource = Resource.createResource(new URL(url), version, null, null);
        return ResourceUrlCreator.getVersionedUrl(resource.getLocation(), resource.getRequestVersion(), null);
    }
}
