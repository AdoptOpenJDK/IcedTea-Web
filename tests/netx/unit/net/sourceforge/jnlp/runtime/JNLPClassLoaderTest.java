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

import static net.sourceforge.jnlp.util.FileTestUtils.assertNoFileLeak;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.sourceforge.jnlp.util.FileTestUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import org.junit.Test;

public class JNLPClassLoaderTest extends NoStdOutErrTest{

    private static AppletSecurityLevel level;
    public static String askUser;
    
    
    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
    }

    @AfterClass
    public static void resetPermissions() {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, level.toChars());
    }
    @BeforeClass
    public static void noDialogs(){
        askUser = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER); 
       JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER, Boolean.toString(false)); 
    }
    
    @AfterClass
    public static void restoreDialogs(){
       JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER, askUser); 
    }
    /* Note: Only does file leak testing for now. */
    @Test
    public void constructorFileLeakTest() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "test.jar");
        FileTestUtils.createJarWithContents(jarLocation /* no contents*/);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);

        assertNoFileLeak( new Runnable () {
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

        assertNoFileLeak( new Runnable () {
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

        /* Test with main-class in manifest */ {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass");
            FileTestUtils.createJarWithContents(jarLocation, manifest);

            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals("DummyClass", classLoader.getMainClassName(jnlpFile.getJarLocation()));
                }
            });
        }
    }
    
    @Test
    public void getMainClassNameTestEmpty() throws Exception {
        /* Test with-out any main-class specified */ {
            File tempDirectory = FileTestUtils.createTempDirectory();
            File jarLocation = new File(tempDirectory, "test.jar");
            FileTestUtils.createJarWithContents(jarLocation /* No contents */);

            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals(null, classLoader.getMainClassName(jnlpFile.getJarLocation()));
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
    public void getCustomAtributes() throws Exception {
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
                assertEquals("rh", classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.IMPLEMENTATION_VENDOR));
                assertEquals("DummyClass", classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.MAIN_CLASS));
                assertEquals("it", classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.IMPLEMENTATION_TITLE));
            }
        });
    }

    @Test
    public void getCustomAtributesEmpty() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        File jarLocation = new File(tempDirectory, "testX.jar");

        /* Test with-out any attribute specified specified */
        FileTestUtils.createJarWithContents(jarLocation /* No contents */);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                assertEquals(null, classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.IMPLEMENTATION_VENDOR));
                assertEquals(null, classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.MAIN_CLASS));
                assertEquals(null, classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.IMPLEMENTATION_TITLE));
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
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "it"); //jsut once in not main jar, see DummyJNLPFileWithJar constructor with int
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
                assertEquals(null, classLoader.checkForAttributeInJars(Arrays.asList(jnlpFile.getJarDescs()), Attributes.Name.IMPLEMENTATION_VENDOR));
                //defined twice, but one in main jar
                assertEquals("DummyClass1", classLoader.checkForAttributeInJars(Arrays.asList(jnlpFile.getJarDescs()), Attributes.Name.MAIN_CLASS));
                //defined not in main jar 
                assertEquals("it", classLoader.checkForAttributeInJars(Arrays.asList(jnlpFile.getJarDescs()), Attributes.Name.IMPLEMENTATION_TITLE));
                //not deffined
                assertEquals(null, classLoader.checkForAttributeInJars(Arrays.asList(jnlpFile.getJarDescs()), Attributes.Name.IMPLEMENTATION_VENDOR_ID));
                //deffined in first jar
                assertEquals("some url1", classLoader.checkForAttributeInJars(Arrays.asList(jnlpFile.getJarDescs()), Attributes.Name.IMPLEMENTATION_URL));
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
                        assertEquals(null, classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.MAIN_CLASS));
                        assertEquals(null, classLoader.getManifestAttribute(jnlpFile.getJarLocation(), Attributes.Name.IMPLEMENTATION_TITLE));
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
}
