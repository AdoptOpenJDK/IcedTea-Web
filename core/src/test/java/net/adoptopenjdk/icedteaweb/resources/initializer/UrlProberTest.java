package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.StdInOutErrController;
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
import java.net.HttpURLConnection;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrlProberTest extends NoStdOutErrTest {

    private static ServerLauncher testServer;
    private static ServerLauncher testServerWithBrokenHead;
    private static ByteArrayOutputStream currentErrorStream;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File serverDir;

    @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() {
        currentErrorStream = new ByteArrayOutputStream();
        OutputController.getLogger().setInOutErrController(new StdInOutErrController(currentErrorStream, currentErrorStream));

    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString(UTF_8.name()));
        OutputController.getLogger().setInOutErrController(StdInOutErrController.getInstance());
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
    public void getUrlResponseCodeTestWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = UrlProber.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(existing.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = UrlProber.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }

    @Test
    public void getUrlResponseCodeTestNotWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = UrlProber.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(existing.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = UrlProber.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, second);
    }

    @Test
    public void getUrlResponseCodeTestGetRequestOnNotWorkingHeadRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = UrlProber.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(existing.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = UrlProber.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }

    @Test
    public void getUrlResponseCodeTestGetRequest() throws Exception {
        final File existing = new File(serverDir, "existing");
        assertTrue(existing.createNewFile());
        final int first = UrlProber.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(existing.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, first);

        final File notExisting = new File(serverDir, "notExisting");
        final int second = UrlProber.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(notExisting.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, second);
    }
}
