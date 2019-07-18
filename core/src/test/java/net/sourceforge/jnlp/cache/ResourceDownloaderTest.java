package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.JarFile;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Pack200;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ResourceDownloaderTest extends NoStdOutErrTest {

    public static ServerLauncher testServer;
    public static ServerLauncher testServerWithBrokenHead;
    public static ServerLauncher downloadServer;

    private static final PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;

    private static final String nameStub1 = "itw-server";
    private static final String nameStub2 = "test-file";

    private static String cacheDir;

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
            int i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
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
            int i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, i);
            f.delete();
            i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.HEAD).getResponseCode();
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
            int i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServerWithBrokenHead.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
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
            int i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, i);
            f.delete();
            i = ResourceDownloader.getUrlResponseCodeWithRedirectionResult(testServer.getUrl(f.getName()), new HashMap<>(), HttpMethod.GET).getResponseCode();
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
            Resource r1 = Resource.getResource(testServer.getUrl(fileForServerWithHeader.getName()), null, UpdatePolicy.NEVER);
            Resource r2 = Resource.getResource(testServerWithBrokenHead.getUrl(fileForServerWithoutHeader.getName()), null, UpdatePolicy.NEVER);
            Resource r3 = Resource.getResource(testServer.getUrl(versionedFileForServerWithHeader.getName()), VersionString.fromString("1.0"), UpdatePolicy.NEVER);
            Resource r4 = Resource.getResource(testServerWithBrokenHead.getUrl(versionedFileForServerWithoutHeader.getName()), VersionString.fromString("1.0"), UpdatePolicy.NEVER);
            assertOnServerWithHeader(resourceDownloader.findBestUrl(r1).getRedirectURL());
            assertVersionedOneOnServerWithHeader(resourceDownloader.findBestUrl(r3).getRedirectURL());
            assertOnServerWithoutHeader(resourceDownloader.findBestUrl(r2).getRedirectURL());
            assertVersionedOneOnServerWithoutHeader(resourceDownloader.findBestUrl(r4).getRedirectURL());

            fileForServerWithHeader.delete();
            Assert.assertNull(resourceDownloader.findBestUrl(r1));
            assertVersionedOneOnServerWithHeader(resourceDownloader.findBestUrl(r3).getRedirectURL());
            assertOnServerWithoutHeader(resourceDownloader.findBestUrl(r2).getRedirectURL());
            assertVersionedOneOnServerWithoutHeader(resourceDownloader.findBestUrl(r4).getRedirectURL());

            versionedFileForServerWithHeader.delete();
            Assert.assertNull(resourceDownloader.findBestUrl(r1));
            Assert.assertNull(resourceDownloader.findBestUrl(r3));
            assertOnServerWithoutHeader(resourceDownloader.findBestUrl(r2).getRedirectURL());
            assertVersionedOneOnServerWithoutHeader(resourceDownloader.findBestUrl(r4).getRedirectURL());

            versionedFileForServerWithoutHeader.delete();
            Assert.assertNull(resourceDownloader.findBestUrl(r1));
            Assert.assertNull(resourceDownloader.findBestUrl(r3));
            assertOnServerWithoutHeader(resourceDownloader.findBestUrl(r2).getRedirectURL());
            Assert.assertNull(resourceDownloader.findBestUrl(r4));

            fileForServerWithoutHeader.delete();
            Assert.assertNull(resourceDownloader.findBestUrl(r1));
            Assert.assertNull(resourceDownloader.findBestUrl(r3));
            Assert.assertNull(resourceDownloader.findBestUrl(r2));
            Assert.assertNull(resourceDownloader.findBestUrl(r4));
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

    @BeforeClass
    public static void setupCache() throws IOException {
        File dir = new File(System.getProperty(JAVA_IO_TMPDIR), "itw-down");
        dir.mkdirs();
        dir.deleteOnExit();

        redirectErr();
        downloadServer = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), ServerAccess.findFreePort());
        redirectErrBack();

        cacheDir = PathsAndFiles.CACHE_DIR.getFullPath();
        PathsAndFiles.CACHE_DIR.setValue(System.getProperty(JAVA_IO_TMPDIR) + File.separator + "tempcache");
    }

    @AfterClass
    public static void teardownCache() {
        downloadServer.stop();

        CacheUtil.clearCache();
        PathsAndFiles.CACHE_DIR.setValue(cacheDir);
    }

    private File setupFile(String fileName, String text) throws IOException {
        File downloadDir = downloadServer.getDir();
        File file = new File(downloadDir, fileName);
        file.createNewFile();
        Files.write(file.toPath(), text.getBytes());
        file.deleteOnExit();

        return file;
    }

    private Resource setupResource(String fileName, String text) throws IOException {
        File f = setupFile(fileName, text);
        URL url = downloadServer.getUrl(fileName);
        Resource resource = Resource.getResource(url, null, UpdatePolicy.NEVER);
        return resource;
    }

    @Test
    public void testDownloadResource() throws IOException {
        String expected = "testDownloadResource";
        Resource resource = setupResource("download-resource", expected);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resourceDownloader.run();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        String output = new String(Files.readAllBytes(downloadedFile.toPath()));
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadPackGzResource() throws IOException {
        String expected = "1.2";

        setupPackGzFile("download-packgz", expected);

        Resource resource = Resource.getResource(downloadServer.getUrl("download-packgz.jar"), null, UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resource.setDownloadOptions(new DownloadOptions(true, false));

        resourceDownloader.run();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        JarFile jf = new JarFile(downloadedFile);
        Manifest m = jf.getManifest();
        String actual = (String) m.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION);

        assertEquals(expected, actual);
    }

    @Test
    public void testDownloadVersionedResource() throws IOException {
        String expected = "testVersionedResource";
        setupFile("download-version__V1.0.jar", expected);

        URL url = downloadServer.getUrl("download-version.jar");
        Resource resource = Resource.getResource(url, VersionString.fromString("1.0"), UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resource.setDownloadOptions(new DownloadOptions(false, true));
        resourceDownloader.run();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        String output = new String(Files.readAllBytes(downloadedFile.toPath()));
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadVersionedPackGzResource() throws IOException {
        String expected = "1.2";

        setupPackGzFile("download-packgz__V1.0", expected);

        Resource resource = Resource.getResource(downloadServer.getUrl("download-packgz.jar"), VersionString.fromString("1.0"), UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resource.setDownloadOptions(new DownloadOptions(true, true));

        resourceDownloader.run();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        JarFile jf = new JarFile(downloadedFile);
        Manifest m = jf.getManifest();
        String actual = (String) m.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION);

        assertEquals(expected, actual);
    }

    @Test
    public void testDownloadLocalResourceFails() throws IOException {
        String expected = "local-resource";
        File localFile = Files.createTempFile("download-local", ".temp").toFile();
        localFile.createNewFile();
        Files.write(localFile.toPath(), expected.getBytes());
        localFile.deleteOnExit();

        String stringURL = "file://" + localFile.getAbsolutePath();
        URL url = new URL(stringURL);

        Resource resource = Resource.getResource(url, null, UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resourceDownloader.run();

        assertTrue(resource.hasFlags(EnumSet.of(Resource.Status.ERROR)));
    }

    @Test
    public void testDownloadNotExistingResourceFails() throws IOException {
        Resource resource = Resource.getResource(new URL(downloadServer.getUrl() + "/notexistingfile"), null, UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.setStatusFlag(Resource.Status.PRECONNECT);
        resourceDownloader.run();

        assertTrue(resource.hasFlags(EnumSet.of(Resource.Status.ERROR)));
    }

    private void setupPackGzFile(String fileName, String version) throws IOException {
        File downloadDir = downloadServer.getDir();

        File orig = new File(downloadDir, fileName + ".jar");
        orig.deleteOnExit();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, version);
        JarOutputStream target = new JarOutputStream(new FileOutputStream(orig), manifest);
        target.close();

        File pack = new File(downloadDir, fileName + ".jar.pack");
        pack.deleteOnExit();

        JarFile jarFile = new JarFile(orig.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(pack);
        Pack200.Packer p = Pack200.newPacker();
        p.pack(jarFile, fos);
        fos.close();

        File packgz = new File(downloadDir, fileName + ".jar.pack.gz");
        packgz.deleteOnExit();
        FileOutputStream gzfos = new FileOutputStream(packgz);
        GZIPOutputStream gos = new GZIPOutputStream(gzfos);

        gos.write(Files.readAllBytes(pack.toPath()));
        gos.finish();
        gos.close();
    }
}
