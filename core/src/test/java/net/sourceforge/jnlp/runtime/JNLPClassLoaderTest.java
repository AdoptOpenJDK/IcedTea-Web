/*Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils.assertNoFileLeak;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class JNLPClassLoaderTest extends NoStdOutErrTest {

    private static AppletSecurityLevel level;
    public static String askUser;

    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
    }

    @AfterClass
    public static void resetPermissions() {
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, level.toChars());
    }

    @BeforeClass
    public static void noDialogs() {
        askUser = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER, Boolean.toString(false));
    }

    @AfterClass
    public static void restoreDialogs() {
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER, askUser);
    }

    /* Note: Only does file leak testing for now. */
    @Test
    @Ignore
    public void constructorFileLeakTest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test.jar");
        FileTestUtils.createJarWithContents(jarLocation /* no contents*/);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                try {
                    new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
                } catch (LaunchException e) {
                    fail(e.toString());
                }
            }
        });
    }

    /* Note: We should create a JNLPClassLoader with an invalid jar to test isInvalidJar with.
     * However, it is tricky without it erroring-out. */
    @Test
    public void isInvalidJarTest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test.jar");
        FileTestUtils.createJarWithContents(jarLocation /* no contents*/);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                assertFalse(classLoader.isInvalidJar(jnlpFile.getJarDesc()));
            }
        });
    }

    @Test
    public void getMainClassNameTest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test.jar");

        /* Test with main-class in manifest */
        {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass");
            FileTestUtils.createJarWithContents(jarLocation, manifest);

            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals("DummyClass", jnlpFile.getManifestAttributesReader().getMainClass(jnlpFile.getJarLocation(), classLoader.getTracker()));
                }
            });
        }
    }

    @Test
    @Ignore
    public void getMainClassNameTestEmpty() throws Exception {
        /* Test with-out any main-class specified */
        {
            File tempDirectory = FileTestUtils.createTempDirectory();
            File jarLocation = new File(tempDirectory, "test.jar");
            FileTestUtils.createJarWithContents(jarLocation /* No contents */);

            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals(null, jnlpFile.getManifestAttributesReader().getMainClass(jnlpFile.getJarLocation(), classLoader.getTracker()));
                }
            });
        }
    }

    /* Note: Although it does a basic check, this mainly checks for file-descriptor leak */
    @Test
    public void checkForMainFileLeakTest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test.jar");
        FileTestUtils.createJarWithContents(jarLocation /* No contents */);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                try {
                    classLoader.checkForMain(Arrays.asList(jnlpFile.getJarDesc()));
                } catch (LaunchException e) {
                    fail(e.toString());
                }
            }
        });
        assertFalse(classLoader.hasMainJar());
    }

    @Test
    public void getCustomAttributes() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "testX.jar");

        /* Test with attributes in manifest */
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "it");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh");
        FileTestUtils.createJarWithContents(jarLocation, manifest);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                assertEquals("rh", jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.IMPLEMENTATION_VENDOR, jnlpFile.getJarLocation(), classLoader.getTracker()));
                assertEquals("DummyClass", jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
                assertEquals("it", jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
            }
        });
    }

    @Test
    public void getCustomAttributesEmpty() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "testX.jar");

        /* Test with-out any attribute specified specified */
        FileTestUtils.createJarWithContents(jarLocation /* No contents */);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.IMPLEMENTATION_VENDOR, jnlpFile.getJarLocation(), classLoader.getTracker()));
                assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
                assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
            }
        });
    }

    @Test
    public void checkOrderWhenReadingAttributes() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation1 = new File(tempDirectory, "test1.jar");
        File jarLocation2 = new File(tempDirectory, "test2.jar");
        File jarLocation3 = new File(tempDirectory, "test3.jar");
        File jarLocation4 = new File(tempDirectory, "test4.jar");
        File jarLocation5 = new File(tempDirectory, "test5.jar");

        /* Test with various attributes in manifest!s! */
        Manifest manifest1 = new Manifest();
        manifest1.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass1"); //two times, but one in main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest2 = new Manifest();
        manifest2.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh1"); //two times, both in not main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest3 = new Manifest();
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "it"); //just once in not main jar, see DummyJNLPFileWithJar constructor with int
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh2");

        Manifest manifest4 = new Manifest();
        manifest4.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass2"); //see jnlpFile.setMainJar(3);
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

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                //defined twice
                assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJars(Attributes.Name.IMPLEMENTATION_VENDOR, Arrays.asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
                //defined twice, but one in main jar
                assertEquals("DummyClass1", jnlpFile.getManifestAttributesReader().getAttributeFromJars(Attributes.Name.MAIN_CLASS, Arrays.asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
                //defined not in main jar 
                assertEquals("it", jnlpFile.getManifestAttributesReader().getAttributeFromJars(Attributes.Name.IMPLEMENTATION_TITLE, Arrays.asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
                //not defined
                assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJars(Attributes.Name.IMPLEMENTATION_VENDOR_ID, Arrays.asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
                //defined in first jar
                assertEquals("some url1", jnlpFile.getManifestAttributesReader().getAttributeFromJars(Attributes.Name.IMPLEMENTATION_URL, Arrays.asList(jnlpFile.getJarDescs()), classLoader.getTracker()));
            }
        });
    }

    @Test
    public void tryNullManifest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test-npe.jar");
        File dummyContent = File.createTempFile("dummy", "context", tempDirectory);
        jarLocation.deleteOnExit();

        /* Test with-out any attribute specified specified */
        FileTestUtils.createJarWithoutManifestContents(jarLocation, dummyContent);

        final Exception[] exs = new Exception[2];
        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        try {
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    try {
                        assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.MAIN_CLASS, jnlpFile.getJarLocation(), classLoader.getTracker()));
                        assertEquals(null, jnlpFile.getManifestAttributesReader().getAttributeFromJar(Attributes.Name.IMPLEMENTATION_TITLE, jnlpFile.getJarLocation(), classLoader.getTracker()));
                    } catch (Exception e) {
                        exs[0] = e;
                    }
                }
            });
        } catch (Exception e) {
            exs[1] = e;
        }
        Assert.assertNotNull(exs);
        Assert.assertNull(exs[0]);
        Assert.assertNull(exs[1]);
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
        boolean verifyBackup = JNLPRuntime.isVerifying();
        File dirHolder = File.createTempFile("pf-", ".jar");
        dirHolder.deleteOnExit();
        File jarLocation = new File(dirHolder.getParentFile(), "pf.jar");
        jarLocation.deleteOnExit();
        try {
            //it is invalid jar, so we have to disable checks first
            JNLPRuntime.setVerify(false);
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/pf.jar-orig");
            Files.copy(is, jarLocation.toPath());
            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS) {
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
        final File tempDirectory = FileTestUtils.createTempDirectory();
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
        CacheUtil.clearCache();
        int port = ServerAccess.findFreePort();
        File dir = FileTestUtils.createTempDirectory();
        dir.deleteOnExit();
        dir = new File(dir,"base");
        dir.mkdir();
        File jar = new File(dir,"j1.jar");
        File jnlp = new File(dir+"/a/b/up.jnlp");
        jnlp.getParentFile().mkdirs();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/up.jnlp");
        String jnlpString = StreamUtils.readStreamAsString(is, UTF_8);
        is.close();
        jnlpString = jnlpString.replaceAll("8080", ""+port);
        is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/j1.jar");
        IOUtils.copy(is, new FileOutputStream(jar));
        Files.write(jnlp.toPath(),jnlpString.getBytes(UTF_8));
        ServerLauncher as = ServerAccess.getIndependentInstance(jnlp.getParent(), port);
        boolean verifyBackup = JNLPRuntime.isVerifying();
        boolean trustBackup= JNLPRuntime.isTrustAll();
        boolean securityBAckup= JNLPRuntime.isSecurityEnabled();
        boolean verbose= JNLPRuntime.isDebug();
        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        String manifestAttsBackup = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");
        try {
            final JNLPFile jnlpFile1 = new JNLPFile(new URL("http://localhost:" + port + "/up.jnlp"));
            final JNLPClassLoader classLoader1 = JNLPClassLoader.getInstance(jnlpFile1, UpdatePolicy.ALWAYS, false);
            InputStream is1 = classLoader1.getResourceAsStream("Hello1.class");
            is1.close();
            is1 = classLoader1.getResourceAsStream("META-INF/MANIFEST.MF");
            is1.close();
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/0/http/localhost/"+port+"/up.jnlp").exists());
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/1/http/localhost/"+port+"/f812acb32c857fd916c842e2bf4fb32b9c3837ef63922b167a7e163305058b7.jar").exists());
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBAckup);
            JNLPRuntime.setDebug(verbose);
            JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }

    }

    @Test
    public void testEncodedPathIsNotDecodedForCache() throws Exception {
        CacheUtil.clearCache();
        int port = ServerAccess.findFreePort();
        File dir = FileTestUtils.createTempDirectory();
        dir.deleteOnExit();
        dir = new File(dir,"base");
        dir.mkdir();
        File jar = new File(dir,"j1.jar");
        File jnlp = new File(dir+"/a/b/upEncoded.jnlp");
        jnlp.getParentFile().mkdirs();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/upEncoded.jnlp");
        String jnlpString = StreamUtils.readStreamAsString(is, UTF_8);
        is.close();
        jnlpString = jnlpString.replaceAll("8080", ""+port);
        is = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/j1.jar");
        IOUtils.copy(is, new FileOutputStream(jar));
        Files.write(jnlp.toPath(),jnlpString.getBytes(UTF_8));
        ServerLauncher as = ServerAccess.getIndependentInstance(jnlp.getParent(), port);
        boolean verifyBackup = JNLPRuntime.isVerifying();
        boolean trustBackup= JNLPRuntime.isTrustAll();
        boolean securityBAckup= JNLPRuntime.isSecurityEnabled();
        boolean verbose= JNLPRuntime.isDebug();
        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        String manifestAttsBackup = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK); JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");
        try {
            final JNLPFile jnlpFile1 = new JNLPFile(new URL("http://localhost:" + port + "/upEncoded.jnlp"));
            final JNLPClassLoader classLoader1 = JNLPClassLoader.getInstance(jnlpFile1, UpdatePolicy.ALWAYS, false);
            InputStream is1 = classLoader1.getResourceAsStream("Hello1.class");
            is1.close();
            is1 = classLoader1.getResourceAsStream("META-INF/MANIFEST.MF");
            is1.close();
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/0/http/localhost/"+port+"/upEncoded.jnlp").exists());
            //be aware; if decoding ever come in play here, thios will leak out of cache folder. Thus harm user system. See fix for " Fixed bug when relative path (..) could leak up (even out of cache)"
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/1/http/localhost/"+port+"/%2E%2E/%2E%2E/%2E%2E/base").exists());
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBAckup);
            JNLPRuntime.setDebug(verbose);
            JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }

    }

    @Test
    public void testRelativePathInNestedJars() throws Exception {
        CacheUtil.clearCache();
        int port = ServerAccess.findFreePort();
        File dir = FileTestUtils.createTempDirectory();
        dir.deleteOnExit();
        File jar = new File(dir,"jar03_dotdotN1.jar");
        File jnlp = new File(dir,"jar_03_dotdot_jarN1.jnlp");
        InputStream is1 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar_03_dotdot_jarN1.jnlp");
        InputStream is2 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar03_dotdotN1.jar");
        OutputStream fos1 = new FileOutputStream(jnlp);
        OutputStream fos2 = new FileOutputStream(jar);
        IOUtils.copy(is1, fos1);
        IOUtils.copy(is2, fos2);
        fos1.flush();;
        fos2.flush();
        fos1.close();
        fos2.close();
        ServerLauncher as = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), port);
        boolean verifyBackup = JNLPRuntime.isVerifying();
        boolean trustBackup= JNLPRuntime.isTrustAll();
        boolean securityBAckup= JNLPRuntime.isSecurityEnabled();
        boolean verbose= JNLPRuntime.isDebug();
        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        //fix of "All files, except signaturre files, are now  checked for signatures" make this actually correctly failing ahead of time
        String ignoreBackup = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, "true");
        String manifestAttsBackup = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, "NONE");
        try {
            //it is invalid jar, so we have to disable checks first
            final JNLPFile jnlpFile = new JNLPFile(new URL("http://localhost:" + port + "/jar_03_dotdot_jarN1.jnlp"));
            final JNLPClassLoader classLoader = JNLPClassLoader.getInstance(jnlpFile, UpdatePolicy.ALWAYS, false);

            //ThreadGroup group = Thread.currentThread().getThreadGroup();
            //ApplicationInstance app = new ApplicationInstance(jnlpFile, group, classLoader);
            //classLoader.setApplication(app);
            //app.initialize();

            //this test is actually not testing mutch. The app must be accessing the nested jar in plugin-like way
            InputStream is = classLoader.getResourceAsStream("application/abev/nyomtatvanyinfo/1965.teminfo.enyk");
            is.close();
            is = classLoader.getResourceAsStream("META-INF/MANIFEST.MF");
            is.close();
            is = classLoader.getResourceAsStream("META-INF/j1.jar");
            is.close();
            is = classLoader.getResourceAsStream("META-INF/../../jar01_to_be_injected.jar");
            //the .. is not recognized correctly
            //is.close();
            //Class c = classLoader.getClass().forName("Hello1");
            // in j1.jar
            is = classLoader.getResourceAsStream("Hello1.class");
            //is.close(); nested jar is not on defualt CP
            //in  jar01
            //c = classLoader.getClass().forName("com.devdaily.FileUtilities");
            is = classLoader.getResourceAsStream("com/devdaily/FileUtilities.class");
            // is.close(); nested jar is not on defualt CP
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/0/http/localhost/"+port+"/jar_03_dotdot_jarN1.jnlp").exists());
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/1/http/localhost/"+port+"/jar03_dotdotN1.jar").exists());
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/1/http/localhost/"+port+"/jar03_dotdotN1.jar.nested/99a90686bfbe84e3f9dbeed8127bba85672ed73688d3c69191aa1ee70916a.jar").exists());
            Assert.assertTrue(new File(PathsAndFiles.CACHE_DIR.getFullPath()+"/1/http/localhost/"+port+"/jar03_dotdotN1.jar.nested/META-INF/j1.jar").exists());
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBAckup);
            JNLPRuntime.setDebug(verbose);
            JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, ignoreBackup);
            JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, manifestAttsBackup);
            as.stop();
        }

    }

    @Test(expected = Exception.class)
    public void testDifferentSignatureInManifestMf() throws Exception {
        CacheUtil.clearCache();
        int port = ServerAccess.findFreePort();
        File dir = FileTestUtils.createTempDirectory();
        dir.deleteOnExit();
        File jar = new File(dir,"jar03_dotdotN1.jar");
        File jnlp = new File(dir,"jar_03_dotdot_jarN1.jnlp");
        InputStream is1 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar_03_dotdot_jarN1.jnlp");
        InputStream is2 = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/runtime/jar03_dotdotN1.jar");
        OutputStream fos1 = new FileOutputStream(jnlp);
        OutputStream fos2 = new FileOutputStream(jar);
        IOUtils.copy(is1, fos1);
        IOUtils.copy(is2, fos2);
        fos1.flush();;
        fos2.flush();
        fos1.close();
        fos2.close();
        ServerLauncher as = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), port);
        boolean verifyBackup = JNLPRuntime.isVerifying();
        boolean trustBackup= JNLPRuntime.isTrustAll();
        boolean securityBAckup= JNLPRuntime.isSecurityEnabled();
        boolean verbose= JNLPRuntime.isDebug();
        JNLPRuntime.setVerify(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setSecurityEnabled(false);
        JNLPRuntime.setDebug(true);
        String ignoreBackup = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, "false");
        try {
            //it is invalid jar, so we have to disable checks first
            final JNLPFile jnlpFile = new JNLPFile(new URL("http://localhost:" + port + "/jar_03_dotdot_jarN1.jnlp"));
            final JNLPClassLoader classLoader = JNLPClassLoader.getInstance(jnlpFile, UpdatePolicy.ALWAYS, false);
        } finally {
            JNLPRuntime.setVerify(verifyBackup);
            JNLPRuntime.setTrustAll(trustBackup);
            JNLPRuntime.setSecurityEnabled(securityBAckup);
            JNLPRuntime.setDebug(verbose);
            JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES, ignoreBackup);
            as.stop();
        }
    }

}
