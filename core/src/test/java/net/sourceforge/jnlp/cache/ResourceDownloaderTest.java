package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.JarFile;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Pack200;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class ResourceDownloaderTest extends NoStdOutErrTest {

    private static ServerLauncher testServer;
    private static ServerLauncher testServerWithBrokenHead;
    private static ServerLauncher downloadServer;

    private static final PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;
    private String cacheDir;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

        JNLPRuntime.setDebug(true);
    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString(UTF_8.name()));

        System.setOut(backedUpStream[0]);
        System.setErr(backedUpStream[1]);
        OutputController.getLogger().setOut(backedUpStream[2]);
        OutputController.getLogger().setErr(backedUpStream[3]);

        JNLPRuntime.setDebug(false);
    }

    @Before
    public void startServer() throws Exception {
        final File serverDir = temporaryFolder.newFolder();

        cacheDir = PathsAndFiles.CACHE_DIR.getFullPath();
        PathsAndFiles.CACHE_DIR.setValue(temporaryFolder.newFolder().getCanonicalPath());

        testServer = ServerAccess.getIndependentInstance(serverDir.getAbsolutePath(), ServerAccess.findFreePort());
        testServerWithBrokenHead = ServerAccess.getIndependentInstance(serverDir.getAbsolutePath(), ServerAccess.findFreePort());
        testServerWithBrokenHead.setSupportingHeadRequest(false);
        downloadServer = ServerAccess.getIndependentInstance(temporaryFolder.newFolder().getAbsolutePath(), ServerAccess.findFreePort());
        Thread.sleep(20);
    }

    @After
    public void stopServer() {
        testServerWithBrokenHead.stop();
        testServer.stop();
        downloadServer.stop();

        PathsAndFiles.CACHE_DIR.setValue(cacheDir);
    }

    @Test
    public void testDownloadResource() throws IOException {
        String expected = "testDownloadResource";
        Resource resource = setupResource("download-resource", expected);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));
        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        String output = readFile(downloadedFile);
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadPackGzResource() throws IOException {
        String expected = "1.2";

        setupPackGzFile("download-packgz", expected);

        Resource resource = Resource.createResource(downloadServer.getUrl("download-packgz.jar"), null, new DownloadOptions(true, false), UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));

        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

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
        Resource resource = Resource.createResource(url, VersionString.fromString("1.0"), new DownloadOptions(false, true), UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));
        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

        File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        String output = readFile(downloadedFile);
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadVersionedPackGzResource() throws IOException {
        String expected = "1.2";

        setupPackGzFile("download-packgz__V1.0", expected);

        Resource resource = Resource.createResource(downloadServer.getUrl("download-packgz.jar"), VersionString.fromString("1.0"), new DownloadOptions(true, true), UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));

        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

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

        Resource resource = Resource.createResource(url, null, null, UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));
        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

        assertTrue(resource.hasAllFlags(EnumSet.of(Resource.Status.ERROR)));
    }

    @Test
    public void testDownloadNotExistingResourceFails() throws IOException {
        Resource resource = Resource.createResource(new URL(downloadServer.getUrl() + "/notexistingfile"), null, null, UpdatePolicy.NEVER);

        ResourceDownloader resourceDownloader = new ResourceDownloader(resource, new Object());

        resource.changeStatus(null, EnumSet.of(Resource.Status.PRECONNECT));
        resourceDownloader.runInitialize();
        resourceDownloader.runDownload();

        assertTrue(resource.hasAllFlags(EnumSet.of(Resource.Status.ERROR)));
    }

    private File setupFile(String fileName, String text) throws IOException {
        final File file = new File(downloadServer.getDir(), fileName);
        Files.write(file.toPath(), text.getBytes(UTF_8));
        return file;
    }

    private Resource setupResource(String fileName, String text) throws IOException {
        setupFile(fileName, text);
        final URL url = downloadServer.getUrl(fileName);
        return Resource.createResource(url, null, null, UpdatePolicy.NEVER);
    }

    private String readFile(File downloadedFile) throws IOException {
        return new String(Files.readAllBytes(downloadedFile.toPath()), UTF_8);
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
