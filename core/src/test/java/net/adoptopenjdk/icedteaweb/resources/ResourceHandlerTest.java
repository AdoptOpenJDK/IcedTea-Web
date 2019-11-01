package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.cache.UpdatePolicy;
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
public class ResourceHandlerTest extends NoStdOutErrTest {

    private static final String MANIFEST_VERSION = "1.2";

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
    public static void redirectErrBack() throws Exception {
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
    public void testDownloadResource() throws Exception {
        final String expected = "testDownloadResource";
        final Resource resource = setupResource("download-resource", expected);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        final File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        final String output = readFile(downloadedFile);
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadPackGzResource() throws Exception {
        setupPackGzFile("download-packgz");

        final Resource resource = Resource.createResource(downloadServer.getUrl("download-packgz.jar"), null, new DownloadOptions(true, false), UpdatePolicy.NEVER);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        final File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        final JarFile jf = new JarFile(downloadedFile);
        final Manifest m = jf.getManifest();
        final String actual = (String) m.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION);

        assertEquals(MANIFEST_VERSION, actual);
    }

    @Test
    public void testDownloadVersionedResource() throws Exception {
        final String expected = "testVersionedResource";
        setupFile("download-version__V1.0.jar", expected);

        final URL url = downloadServer.getUrl("download-version.jar");
        final Resource resource = Resource.createResource(url, VersionString.fromString("1.0"), new DownloadOptions(false, true), UpdatePolicy.NEVER);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        final File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        final String output = readFile(downloadedFile);
        assertEquals(expected, output);
    }

    @Test
    public void testDownloadVersionedPackGzResource() throws Exception {
        setupPackGzFile("download-packgz__V1.0");
        final Resource resource = Resource.createResource(downloadServer.getUrl("download-packgz.jar"), VersionString.fromString("1.0"), new DownloadOptions(true, true), UpdatePolicy.NEVER);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        final File downloadedFile = resource.getLocalFile();
        assertTrue(downloadedFile.exists() && downloadedFile.isFile());

        final JarFile jf = new JarFile(downloadedFile);
        final Manifest m = jf.getManifest();
        final String actual = (String) m.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION);

        assertEquals(MANIFEST_VERSION, actual);
    }

    @Test
    public void testDownloadLocalResource() throws Exception {
        final String expected = "local-resource";
        final File localFile = temporaryFolder.newFile();
        Files.write(localFile.toPath(), expected.getBytes(UTF_8));

        final String stringURL = "file://" + localFile.getAbsolutePath();
        final URL url = new URL(stringURL);

        final Resource resource = Resource.createResource(url, null, null, UpdatePolicy.NEVER);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        assertTrue(resource.hasAllFlags(EnumSet.of(Resource.Status.DOWNLOADED)));
    }

    @Test
    public void testDownloadNotExistingResourceFails() throws Exception {
        final Resource resource = Resource.createResource(new URL(downloadServer.getUrl() + "/notexistingfile"), null, null, UpdatePolicy.NEVER);

        final ResourceHandler resourceHandler = new ResourceHandler(resource);
        resourceHandler.putIntoCache().get();

        assertTrue(resource.hasAllFlags(EnumSet.of(Resource.Status.ERROR)));
    }

    private void setupFile(String fileName, String text) throws Exception {
        final File file = new File(downloadServer.getDir(), fileName);
        Files.write(file.toPath(), text.getBytes(UTF_8));
    }

    private Resource setupResource(String fileName, String text) throws Exception {
        setupFile(fileName, text);
        final URL url = downloadServer.getUrl(fileName);
        return Resource.createResource(url, null, DownloadOptions.NONE, UpdatePolicy.NEVER);
    }

    private String readFile(File downloadedFile) throws Exception {
        return new String(Files.readAllBytes(downloadedFile.toPath()), UTF_8);
    }

    private void setupPackGzFile(String fileName) throws Exception {
        final File downloadDir = downloadServer.getDir();

        final File jar = new File(downloadDir, fileName + ".jar");
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        final JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar), manifest);
        jarOut.close();

        final File pack = new File(downloadDir, fileName + ".jar.pack");

        final JarFile jarFile = new JarFile(jar.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(pack);
        final Pack200.Packer p = Pack200.newPacker();
        p.pack(jarFile, fos);
        fos.close();

        final File packgz = new File(downloadDir, fileName + ".jar.pack.gz");
        final GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(packgz));
        gos.write(Files.readAllBytes(pack.toPath()));
        gos.close();
    }
}
