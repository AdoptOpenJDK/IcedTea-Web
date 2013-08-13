package net.sourceforge.jnlp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class TinyHttpdImplTest {

    private static final String HTTP_OK = "HTTP/1.0 200 OK";
    private static final String HTTP_404 = "HTTP/1.0 404 Not Found";
    private static final String HTTP_501 = "HTTP/1.0 501 Not Implemented";
    private static final String CONTENT_JNLP = "Content-Type: application/x-java-jnlp-file";
    private static final String CONTENT_HTML = "Content-Type: text/html";
    private static final String CONTENT_JAR = "Content-Type: application/x-jar";
    private static final Pattern CONTENT_LENGTH = Pattern.compile("Content-Length:([0-9]+)");

    private static final String[] FilePathTestUrls = {
            "/foo.html",
            "/foo/",
            "/foo/bar.jar",
            "/foo/bar.jar;path_param",
            "/foo/bar.jar%3Bpath_param",
            "/foo/bar?query=string&red=hat"
    };

    private static BufferedReader mReader;
    private static DataOutputStream mWriter;
    private static TinyHttpdImpl mServer;

    static {
        try {
            ServerSocket sSocket = new ServerSocket(44322);
            sSocket.setReuseAddress(true);
            File dir = new File(System.getProperty("test.server.dir"));
            Socket extSock = new Socket("localhost", 44322);
            extSock.setReuseAddress(true);
            mServer = new TinyHttpdImpl(extSock, dir);

            Socket socket = sSocket.accept();
            socket.setReuseAddress(true);
            mReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mWriter = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void urlToFilePathTest() throws Exception {
        for (String url : FilePathTestUrls) {
            String newUrl = TinyHttpdImpl.urlToFilePath(url);

            Assert.assertFalse("File path should not contain query string: " + newUrl, newUrl.contains("?"));
            Assert.assertTrue("File path should be relative: " + newUrl, newUrl.startsWith("./"));
            Assert.assertFalse("File path should not contain \"/XslowX\":" + newUrl,
                    newUrl.toLowerCase().contains("/XslowX".toLowerCase()));

            if (url.endsWith("/")) {
                Assert.assertTrue(newUrl.endsWith("/index.html"));
            }
        }
    }

    @Test
    public void urlToFilePathUrlDecodeTest() throws Exception {
        // This test may fail with strange original URLs, eg those containing the substring "%253B",
        // which can be decoded into "%3B", then decoded again into ';'.

        for (String url : FilePathTestUrls) {
            String newUrl = TinyHttpdImpl.urlToFilePath(url);
            Assert.assertEquals(newUrl, URLDecoder.decode(newUrl, "UTF-8"));
        }
    }

    @Test
    public void stripHttpPathParamTest() {
        String[] testBaseUrls = {
                "http://foo.com/bar",
                "localhost:8080",
                "https://bar.co.uk/site;para/baz?u=param1&v=param2"
        };

        String[] testJarNames = {
                "jar",
                "foo.jar",
                "bar;baz.jar",
                "nom.jar;",
                "rhat.jar.pack.gz;tag"
        };

        for (String url : testBaseUrls) {
            for (String jar : testJarNames) {
                String newUrl = TinyHttpdImpl.stripHttpPathParams(url), newJar = TinyHttpdImpl.stripHttpPathParams(jar), path = newUrl + "/" + newJar;
                Assert.assertTrue("Base URL should not have been modified: " + url + " => " + newUrl, newUrl.equals(url));
                Assert.assertTrue("JAR name should not be altered other than removing path param: " + jar + " => " + newJar, jar.startsWith(newJar));
                Assert.assertTrue("New path should be a substring of old path: " + path + " => " + url + "/" + jar, (url + "/" + jar).startsWith(path));
            }
        }
    }

    private void headTestHelper(String request, String contentType) {
        Matcher matcher = CONTENT_LENGTH.matcher(request);

        Assert.assertTrue("Status should have been " + HTTP_OK, request.contains(HTTP_OK));
        Assert.assertTrue("Content type should have been " + contentType, request.contains(contentType));
        Assert.assertTrue("Should have had a content length", matcher.find());
    }

    @Test
    public void JnlpHeadTest() throws IOException, InterruptedException {
        String head = getTinyHttpdImplResponse("HEAD", "/simpletest1.jnlp");
        headTestHelper(head, CONTENT_JNLP);
    }

    @Test
    public void HtmlHeadTest() throws Exception {
        String head = getTinyHttpdImplResponse("HEAD", "/StripHttpPathParams.html");
        headTestHelper(head, CONTENT_HTML);
    }

    @Test
    public void JarHeadTest() throws Exception {
        String head = getTinyHttpdImplResponse("HEAD", "/StripHttpPathParams.jar");
        headTestHelper(head, CONTENT_JAR);
    }

    @Test
    public void PngHeadTest() throws Exception {
        // TinyHttpdImpl doesn't recognize PNG type - default content type should be HTML
        String head = getTinyHttpdImplResponse("HEAD", "/netxPlugin.png");
        headTestHelper(head, CONTENT_HTML);
    }

    @Test
    public void SlowSendTest() throws Exception {
        // This test is VERY SLOW due to the extremely slow sending speed TinyHttpdImpl uses when XslowX is specified.
        // Running time will be over two minutes.
        long fastStartTime = System.nanoTime();
        String req1 = getTinyHttpdImplResponse("GET", "/simpletest1.jnlp");
        long fastElapsed = System.nanoTime() - fastStartTime;

        long slowStartTime = System.nanoTime();
        String req2 = getTinyHttpdImplResponse("GET", "/XslowXsimpletest1.jnlp");
        long slowElapsed = System.nanoTime() - slowStartTime;

        Assert.assertTrue("Slow request should have returned the same data as normal request", req1.equals(req2));

        // This isn't a very good test since as it is, getTinyHttpdImpl is slowing down its receive rate to
        // deal with the reduced sending rate. It is hardcoded to be slower.
        Assert.assertTrue("Slow request should have taken longer than normal request", slowElapsed > fastElapsed);
    }

    @Test
    public void GetTest() throws Exception {
        String jnlpHead = getTinyHttpdImplResponse("HEAD", "/simpletest1.jnlp");
        String jnlpGet = getTinyHttpdImplResponse("GET", "/simpletest1.jnlp");

        Assert.assertTrue("GET status should be " + HTTP_OK, jnlpGet.contains(HTTP_OK));
        Assert.assertTrue("GET content type should have been " + CONTENT_JNLP, jnlpGet.contains(CONTENT_JNLP));
        Assert.assertTrue("GET response should contain HEAD response", jnlpGet.contains(jnlpHead));
        Assert.assertTrue("GET response should have been longer than HEAD response", jnlpGet.length() > jnlpHead.length());
    }

    @Test
    public void Error404DoesNotCauseShutdown() throws Exception {
        // Pre-refactoring, 404 errors were sent after catching an IOException when trying to open the requested
        // resource. However this was caught by a try/catch clause around the entire while loop, so a 404 would
        // shut down the server.
        String firstRequest = getTinyHttpdImplResponse("HEAD", "/no_such_file");
        String secondRequest = getTinyHttpdImplResponse("HEAD", "/simpletest1.jnlp");

        Assert.assertTrue("First request should have been " + HTTP_404, firstRequest.trim().equals(HTTP_404));
        Assert.assertTrue("Second request should have been " + HTTP_OK, secondRequest.contains(HTTP_OK));
    }

    @Test
    public void NotSupportingHeadRequest() throws Exception {
        boolean headRequestSupport = mServer.isSupportingHeadRequest();
        mServer.setSupportingHeadRequest(false);
        String head = getTinyHttpdImplResponse("HEAD", "/simpletest1.jnlp");

        Assert.assertTrue("Status should have been " + HTTP_501, head.trim().equals(HTTP_501));

        mServer.setSupportingHeadRequest(headRequestSupport);
    }

    private String getTinyHttpdImplResponse(String requestType, String filePath) throws IOException, InterruptedException {
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        mWriter.writeBytes(requestType + " " + filePath + " HTTP/1.1\r\n");
        Thread.sleep(250); // Wait a while for server to be able to respond to request

        StringBuilder builder = new StringBuilder();
        while (mReader.ready()) {
            // TODO: come up with a better way to deal with slow sending - this works but is hackish
            if (filePath.startsWith("/XslowX")) {
                Thread.sleep(2100); // Wait for next chunk to have been sent, otherwise it'll appear as if the response
                // has finished being sent prematurely
            }
            builder.append(mReader.readLine());
            builder.append("\n");
        }

        return builder.toString();
    }

}
