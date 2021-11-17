/*Copyright (C) 2013 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.CachedJarFileCallback;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.net.www.protocol.jar.URLJarFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VENDOR;
import static java.util.jar.Attributes.Name.MAIN_CLASS;
import static net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader.getAttributeFromJar;
import static net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader.getAttributeFromJars;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NotThreadSafe
@Ignore
public class JNLPClassLoaderTest extends NoStdOutErrTest {

    private final JNLPFileFactory jnlpFileFactory = new JNLPFileFactory();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static AppletSecurityLevel level;
    private static String askUser;

    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
    }

    @AfterClass
    public static void resetPermissions() {
        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, level.toChars());
    }

    @BeforeClass
    public static void noDialogs() {
        askUser = getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER);
        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER, Boolean.toString(false));
    }

    @AfterClass
    public static void restoreDialogs() {
        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER, askUser);
    }

    /* Note: Only does file leak testing for now. */
    @Test
    @Ignore
    public void constructorFileLeakTest() throws Exception {
        final File jarLocation = createJarWithoutContent();
        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);

        new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
    }

    /* Note: We should create a JNLPClassLoader with an invalid jar to test isInvalidJar with.
     * However, it is tricky without it erroring-out. */
    @Test
    public void isInvalidJarTest() throws Exception {
        final File jarLocation = createJarWithoutContent();
        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertFalse(classLoader.isInvalidJar(jnlpFile.getJarDesc()));
    }

    @Test
    public void getMainClassNameTest() throws Exception {
        File tempDirectory = temporaryFolder.newFolder();
        File jarLocation = new File(tempDirectory, "test.jar");

        /* Test with main-class in manifest */
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(MAIN_CLASS, "DummyClass");
        FileTestUtils.createJarWithContents(jarLocation, manifest);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertEquals("DummyClass", jnlpFile.getManifestAttributesReader().getMainClass(jnlpFile.getJarLocation(), classLoader.getTracker()));
    }

    @Test
    @Ignore
    public void getMainClassNameTestEmpty() throws Exception {
        /* Test with-out any main-class specified */
        File jarLocation = createJarWithoutContent();

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNull(jnlpFile.getManifestAttributesReader().getMainClass(jnlpFile.getJarLocation(), classLoader.getTracker()));
    }

    /* Note: Although it does a basic check, this mainly checks for file-descriptor leak */
    @Test
    public void checkForMainFileLeakTest() throws Exception {
        File jarLocation = createJarWithoutContent();

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
        classLoader.checkForMain(asList(jnlpFile.getJarDesc()));
        assertFalse(classLoader.hasMainJar());
    }

    @Test
    public void getCustomAttributes() throws Exception {
        File tempDirectory = temporaryFolder.newFolder();
        File jarLocation = new File(tempDirectory, "testX.jar");

        /* Test with attributes in manifest */
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(MAIN_CLASS, "DummyClass");
        manifest.getMainAttributes().put(IMPLEMENTATION_TITLE, "it");
        manifest.getMainAttributes().put(IMPLEMENTATION_VENDOR, "rh");
        FileTestUtils.createJarWithContents(jarLocation, manifest);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertEquals("rh", getAttributeFromJar(IMPLEMENTATION_VENDOR, jnlpFile.getJarLocation(), classLoader.getTracker()));
        assertEquals("DummyClass", getAttributeFromJar(MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
        assertEquals("it", getAttributeFromJar(IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
    }

    @Test
    public void getCustomAttributesEmpty() throws Exception {
        File jarLocation = createJarWithoutContent();

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNull(getAttributeFromJar(IMPLEMENTATION_VENDOR, jnlpFile.getJarLocation(), classLoader.getTracker()));
        assertNull(getAttributeFromJar(MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
        assertNull(getAttributeFromJar(IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
    }

    @Test
    public void checkOrderWhenReadingAttributes() throws Exception {
        File tempDirectory = temporaryFolder.newFolder();
        File jarLocation1 = new File(tempDirectory, "test1.jar");
        File jarLocation2 = new File(tempDirectory, "test2.jar");
        File jarLocation3 = new File(tempDirectory, "test3.jar");
        File jarLocation4 = new File(tempDirectory, "test4.jar");
        File jarLocation5 = new File(tempDirectory, "test5.jar");

        /* Test with various attributes in manifest!s! */
        Manifest manifest1 = new Manifest();
        manifest1.getMainAttributes().put(MAIN_CLASS, "DummyClass1"); //two times, but one in main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest2 = new Manifest();
        manifest2.getMainAttributes().put(IMPLEMENTATION_VENDOR, "rh1"); //two times, both in not main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest3 = new Manifest();
        manifest3.getMainAttributes().put(IMPLEMENTATION_TITLE, "it"); //just once in not main jar, see DummyJNLPFileWithJar constructor with int
        manifest3.getMainAttributes().put(IMPLEMENTATION_VENDOR, "rh2");

        Manifest manifest4 = new Manifest();
        manifest4.getMainAttributes().put(MAIN_CLASS, "DummyClass2"); //see jnlpFile.setMainJar(3);
        manifest4.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url2"); //see DummyJNLPFileWithJar constructor with int

        //first jar
        Manifest manifest5 = new Manifest();
        manifest5.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url1"); //see DummyJNLPFileWithJar constructor with int

        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);
        FileTestUtils.createJarWithContents(jarLocation4, manifest4);
        FileTestUtils.createJarWithContents(jarLocation5, manifest5);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(3, jarLocation5, jarLocation3, jarLocation4, jarLocation1, jarLocation2); //jar 1 should be main
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        //defined twice
        assertNull(getAttributeFromJars(IMPLEMENTATION_VENDOR, asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
        //defined twice, but one in main jar
        assertEquals("DummyClass1", getAttributeFromJars(MAIN_CLASS, asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
        //defined not in main jar
        assertEquals("it", getAttributeFromJars(IMPLEMENTATION_TITLE, asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
        //not defined
        assertNull(getAttributeFromJars(Attributes.Name.IMPLEMENTATION_VENDOR_ID, asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
        //defined in first jar
        assertEquals("some url1", getAttributeFromJars(Attributes.Name.IMPLEMENTATION_URL, asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
    }

    @Test
    public void tryNullManifest() throws Exception {
        File tempDirectory = temporaryFolder.newFolder();
        File jarLocation = new File(tempDirectory, "test-npe.jar");
        File dummyContent = File.createTempFile("dummy", "context", tempDirectory);

        /* Test with-out any attribute specified specified */
        FileTestUtils.createJarWithoutManifestContents(jarLocation, dummyContent);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
        assertNull(getAttributeFromJar(MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
        assertNull(getAttributeFromJar(IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
    }

    @Test
    @Bug(id = "PR3417")
    /**
     * The nested jar must be more 1024 bytes long. Better, longer
     * then  byte[] bytes = new byte[1024] on line 1273 in
     * net.sourceforge.jnlp.runtime.JNLPClassLoader otherwise the file
     * will not get rewritten while read  Also there must be more then
     * one item of this size, for same reason
     */
    public void testNameClashInNestedJars() throws Exception {
        //for this test is enough to not crash jvm
        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final File dir = temporaryFolder.newFolder();
        final File dirHolder = File.createTempFile("pf-", ".jar", dir);
        final File jarLocation = new File(dirHolder.getParentFile(), "pf.jar");
        try {
            //it is invalid jar, so we have to disable checks first
            JNLPRuntime.setVerify(false);
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/pf.jar-orig");
            assertNotNull(is);
            Files.copy(is, jarLocation.toPath());
            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);

            new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS) {
                @Override
                protected void activateJars(List<JARDesc> jars) {
                    super.activateJars(jars);
                }

            };
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
        }
    }

    @Test
    public void testFindLibrary() throws Exception {
        final File tempDirectory = temporaryFolder.newFolder();
        final String nativeLibName = "native";

        // Create jar to search in
        final File jarLocation = new File(tempDirectory, "app.jar");

        // Create native lib to search for using System.mapLibraryName(),
        // which maps as follows:
        // Windows: "native" -> "native.dll"
        // Linux/Solaris: "native" "libnative.so"
        // Mac: "native" -> "libnative.dylib"
        final String nativeLibPlatformSpecificName = System.mapLibraryName(nativeLibName);
        final File nativeLibFile = new File(tempDirectory, nativeLibPlatformSpecificName);
        FileTestUtils.createFileWithContents(nativeLibFile, "");

        FileTestUtils.createJarWithContents(jarLocation, nativeLibFile);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        final String nativeLib = classLoader.findLibrary(nativeLibName);

        assertNotNull(nativeLib);
    }

    @Test
    public void testRelativePathInUrl() throws Exception {
        clearCache();
        final int port = ServerAccess.findFreePort();
        final File dir = temporaryFolder.newFolder("base");
        final File jar = new File(dir, "j1.jar");
        final File jnlp = new File(dir + "/a/b/up.jnlp");
        jnlp.getParentFile().mkdirs();

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/up.jnlp")) {
            final String rawJnlpString = StreamUtils.readStreamAsString(is, UTF_8);
            final String jnlpString = rawJnlpString.replaceAll("8080", "" + port);
            Files.write(jnlp.toPath(), jnlpString.getBytes(UTF_8));
        }
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/j1.jar")) {
            try (final FileOutputStream out = new FileOutputStream(jar)) {
                IOUtils.copy(is, out);
            }
        }

        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final boolean trustBackup = JNLPRuntime.isTrustAll();
        final boolean securityBackup = JNLPRuntime.isSecurityEnabled();
        final boolean verbose = JNLPRuntime.isDebug();
        final String manifestAttsBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);

        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");

        final ServerLauncher as = ServerAccess.getIndependentInstance(jnlp.getParent(), port);
        try {
            final URL jnlpUrl = new URL("http://localhost:" + port + "/up.jnlp");
            final JNLPFile jnlpFile1 = jnlpFileFactory.create(jnlpUrl);
            final JNLPClassLoader classLoader1 = JNLPClassLoader.getInstance(jnlpFile1, UpdatePolicy.ALWAYS, false);
            openResourceAsStream(classLoader1, "Hello1.class");
            openResourceAsStream(classLoader1, "META-INF/MANIFEST.MF");
            assertTrue(Cache.isAnyCached(jnlpUrl, null));
            assertTrue(Cache.isAnyCached(new URL("http://localhost:" + port + "/../../../base/j1.jar"), null));
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBackup);
            JNLPRuntime.setDebug(verbose);
            getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }
    }

    @Test
    public void testEncodedPathIsNotDecodedForCache() throws Exception {
        clearCache();
        final int port = ServerAccess.findFreePort();
        final File dir = temporaryFolder.newFolder("base");
        final File jar = new File(dir, "j1.jar");
        final File jnlp = new File(dir + "/a/b/upEncoded.jnlp");
        jnlp.getParentFile().mkdirs();

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/upEncoded.jnlp")) {
            final String rawJnlpString = StreamUtils.readStreamAsString(is, UTF_8);
            final String jnlpString = rawJnlpString.replaceAll("8080", "" + port);
            Files.write(jnlp.toPath(), jnlpString.getBytes(UTF_8));
        }
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/j1.jar")) {
            try (final FileOutputStream out = new FileOutputStream(jar)) {
                IOUtils.copy(is, out);
            }
        }

        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final boolean trustBackup = JNLPRuntime.isTrustAll();
        final boolean securityBackup = JNLPRuntime.isSecurityEnabled();
        final boolean verbose = JNLPRuntime.isDebug();
        final String manifestAttsBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);

        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");

        final ServerLauncher as = ServerAccess.getIndependentInstance(jnlp.getParent(), port);
        try {
            final URL jnlpUrl = new URL("http://localhost:" + port + "/upEncoded.jnlp");
            final JNLPFile jnlpFile1 = jnlpFileFactory.create(jnlpUrl);
            final JNLPClassLoader classLoader1 = JNLPClassLoader.getInstance(jnlpFile1, UpdatePolicy.ALWAYS, false);
            openResourceAsStream(classLoader1, "Hello1.class");
            openResourceAsStream(classLoader1, "META-INF/MANIFEST.MF");
            assertTrue(Cache.isAnyCached(jnlpUrl, null));
            assertTrue(Cache.isAnyCached(new URL("http://localhost:" + port + "/%2E%2E/%2E%2E/%2E%2E/base/j1.jar"), null));
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBackup);
            JNLPRuntime.setDebug(verbose);
            getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }
    }

    @Test
    public void testRelativePathInNestedJars() throws Exception {
        clearCache();
        final int port = ServerAccess.findFreePort();
        final File dir = temporaryFolder.newFolder();
        final File jar = new File(dir, "jar03_dotdotN1.jar");
        final File jnlp = new File(dir, "jar_03_dotdot_jarN1.jnlp");
        try (InputStream is1 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar_03_dotdot_jarN1.jnlp")) {
            try (OutputStream fos1 = new FileOutputStream(jnlp)) {
                IOUtils.copy(is1, fos1);
            }
        }
        try (InputStream is2 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar03_dotdotN1.jar")) {
            try (OutputStream fos2 = new FileOutputStream(jar)) {
                IOUtils.copy(is2, fos2);
            }
        }

        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final boolean trustBackup = JNLPRuntime.isTrustAll();
        final boolean securityBackup = JNLPRuntime.isSecurityEnabled();
        final boolean verbose = JNLPRuntime.isDebug();
        final String ignoreBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES);
        final String manifestAttsBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);

        //fix of "All files, except signaturre files, are now  checked for signatures" make this actually correctly failing ahead of time
        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);

        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, "true");
        getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");
        final ServerLauncher as = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), port);
        try {
            //it is invalid jar, so we have to disable checks first
            final URL jnlpUrl = new URL("http://localhost:" + port + "/jar_03_dotdot_jarN1.jnlp");
            final JNLPFile jnlpFile = jnlpFileFactory.create(jnlpUrl);
            final JNLPClassLoader classLoader = JNLPClassLoader.getInstance(jnlpFile, UpdatePolicy.ALWAYS, false);

            //ThreadGroup group = Thread.currentThread().getThreadGroup();
            //ApplicationInstance app = new ApplicationInstance(jnlpFile, group, classLoader);
            //classLoader.setApplication(app);
            //app.initialize();

            //this test is actually not testing mutch. The app must be accessing the nested jar in plugin-like way
            final String path = "application/abev/nyomtatvanyinfo/1965.teminfo.enyk";
            openResourceAsStream(classLoader, path);
            openResourceAsStream(classLoader, "META-INF/MANIFEST.MF");
            openResourceAsStream(classLoader, "META-INF/j1.jar");
            openResourceAsStream(classLoader, "META-INF/../../jar01_to_be_injected.jar");
            // the .. is not recognized correctly
            // Class c = classLoader.getClass().forName("Hello1");
            // in j1.jar
            openResourceAsStream(classLoader, "Hello1.class");
            // nested jar is not on defualt CP
            // in  jar01
            // c = classLoader.getClass().forName("com.devdaily.FileUtilities");
            openResourceAsStream(classLoader, "com/devdaily/FileUtilities.class");
            // nested jar is not on defualt CP
            final URL jarUrl = new URL("http://localhost:" + port + "/jar03_dotdotN1.jar");
            assertTrue(Cache.isAnyCached(jnlpUrl, null));
            assertTrue(Cache.isAnyCached(jarUrl, null));
            final File jarFile = Cache.getCacheFile(jarUrl, null);
            final File nestedDir = new File(jarFile.getAbsolutePath() + ".nested");
            assertTrue(nestedDir.isDirectory());
            assertTrue(new File(nestedDir.getAbsolutePath() + "/99a90686bfbe84e3f9dbeed8127bba85672ed73688d3c69191aa1ee70916a.jar").exists());
            assertTrue(new File(nestedDir.getAbsolutePath() + "//META-INF/j1.jar").exists());
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBackup);
            JNLPRuntime.setDebug(verbose);
            getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, ignoreBackup);
            getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }

    }

    @Test
    public void testLoadClass() throws Exception {
        // test with cache folder with space
        final String cacheDir = temporaryFolder.newFolder("cache Folder").getCanonicalPath();
        final File cacheBackup = PathsAndFiles.CACHE_DIR.getFile();
        PathsAndFiles.CACHE_DIR.setValue(cacheDir);

        final int port = ServerAccess.findFreePort();
        final File dir = temporaryFolder.newFolder("base");
        final File jar = new File(dir, "j1.jar");
        final File jnlp = new File(dir + "/test.jnlp");
        jnlp.getParentFile().mkdirs();

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/test.jnlp")) {
            final String rawJnlpString = StreamUtils.readStreamAsString(is, UTF_8);
            final String jnlpString = rawJnlpString.replaceAll("8080", "" + port);
            Files.write(jnlp.toPath(), jnlpString.getBytes(UTF_8));
        }
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/j1.jar")) {
            try (final FileOutputStream out = new FileOutputStream(jar)) {
                IOUtils.copy(is, out);
            }
        }

        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final boolean trustBackup = JNLPRuntime.isTrustAll();
        final boolean securityBackup = JNLPRuntime.isSecurityEnabled();
        final boolean verbose = JNLPRuntime.isDebug();
        final String manifestAttsBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);

        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");
        URLJarFile.setCallBack(CachedJarFileCallback.getInstance());

        final ServerLauncher as = ServerAccess.getIndependentInstance(jnlp.getParent(), port);
        try {
            final URL jnlpUrl = new URL("http://localhost:" + port + "/test.jnlp");
            final JNLPFile jnlpFile1 = jnlpFileFactory.create(jnlpUrl);
            final JNLPClassLoader classLoader1 = JNLPClassLoader.getInstance(jnlpFile1, UpdatePolicy.ALWAYS, false);
            classLoader1.loadClass("Hello1");
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBackup);
            JNLPRuntime.setDebug(verbose);
            getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            URLJarFile.setCallBack(null);
            as.stop();

            clearCache();
            PathsAndFiles.CACHE_DIR.setValue(cacheBackup.getCanonicalPath());
        }

    }

    @SuppressWarnings("EmptyTryBlock")
    private void openResourceAsStream(JNLPClassLoader classLoader, String path) throws IOException {
        try (final InputStream ignored = classLoader.getResourceAsStream(path)) {
            // do nothing
        }
    }

    @Test(expected = Exception.class)
    public void testDifferentSignatureInManifestMf() throws Exception {
        clearCache();
        final int port = ServerAccess.findFreePort();
        final File dir = temporaryFolder.newFolder();
        final File jar = new File(dir, "jar03_dotdotN1.jar");
        final File jnlp = new File(dir, "jar_03_dotdot_jarN1.jnlp");

        try (InputStream is1 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar_03_dotdot_jarN1.jnlp")) {
            try (OutputStream fos1 = new FileOutputStream(jnlp)) {
                IOUtils.copy(is1, fos1);
            }
        }
        try (InputStream is2 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar03_dotdotN1.jar")) {
            try (OutputStream fos2 = new FileOutputStream(jar)) {
                IOUtils.copy(is2, fos2);
            }
        }

        final boolean verifyBackup = JNLPRuntime.isVerifying();
        final boolean trustBackup = JNLPRuntime.isTrustAll();
        final boolean securityBackup = JNLPRuntime.isSecurityEnabled();
        final boolean verbose = JNLPRuntime.isDebug();
        final String ignoreBackup = getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES);

        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, "false");

        final ServerLauncher as = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), port);
        try {
            //it is invalid jar, so we have to disable checks first
            final JNLPFile jnlpFile = jnlpFileFactory.create(new URL("http://localhost:" + port + "/jar_03_dotdot_jarN1.jnlp"));
            JNLPClassLoader.getInstance(jnlpFile, UpdatePolicy.ALWAYS, false);
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBackup);
            JNLPRuntime.setDebug(verbose);
            getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, ignoreBackup);
            as.stop();
        }
    }

    private void clearCache() {
        final File cacheDir = PathsAndFiles.CACHE_DIR.getFile();
        if (cacheDir.isDirectory()) {
            assertTrue("Failed to clear cache", Cache.clearCache());
        } else {
            if (cacheDir.isFile()) {
                assertTrue("Failed to delete file blocking cache dir", cacheDir.delete());
            }
            assertTrue("Failed to create empty cache dir", cacheDir.mkdirs());
        }
    }

    private File createJarWithoutContent() throws Exception {
        File tempDirectory = temporaryFolder.newFolder();
        File jarLocation = new File(tempDirectory, "test.jar");
        FileTestUtils.createJarWithContents(jarLocation /* no contents*/);
        return jarLocation;
    }
}
