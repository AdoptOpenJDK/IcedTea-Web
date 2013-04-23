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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.util.StreamUtils;

import org.junit.Test;

public class JNLPClassLoaderTest {

    /* Get the open file-descriptor count for the process.
     * Note that this is specific to Unix-like operating systems.
     * As well, it relies on */
    static public long getOpenFileDescriptorCount() {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            return (Long) beanServer.getAttribute(
                    new ObjectName("java.lang:type=OperatingSystem"), 
                    "OpenFileDescriptorCount"
            );
        } catch (Exception e) {
            // Effectively disables leak tests
            ServerAccess.logErrorReprint("Warning: Cannot get file descriptors for this platform!");
            return 0;
        }
    }

    /* Check the amount of file descriptors before and after a Runnable */
    static private void assertNoFileLeak(Runnable runnable) {
        long filesOpenBefore = getOpenFileDescriptorCount();
        runnable.run();
        long filesLeaked = getOpenFileDescriptorCount() - filesOpenBefore;
        assertEquals(0, filesLeaked);
    }

    static private String cleanExec(File directory, String... command) throws Exception {
        Process p = Runtime.getRuntime().exec(command, new String[]{}, directory);

        String stdOut = StreamUtils.readStreamAsString(p.getInputStream());
        String stdErr = StreamUtils.readStreamAsString(p.getErrorStream());

        ServerAccess.logNoReprint("Running " + Arrays.toString(command));
        ServerAccess.logNoReprint("Standard output was: \n" + stdOut);
        ServerAccess.logNoReprint("Standard error was: \n" + stdErr);

        p.getInputStream().close();
        p.getErrorStream().close();
        p.getOutputStream().close();

        return stdOut;

    }

    /* Creates a jar in a temporary directory, with the given name & manifest contents. */
    static private File createTempJar(String jarName, String manifestContents) throws Exception {
        File dir = new File(cleanExec(null /* current working dir */, "mktemp", "-d"));
        cleanExec(dir, "/bin/bash", "-c", "echo '" + manifestContents + "' > Manifest.txt");
        cleanExec(dir, "jar", "-cfm", jarName, "Manifest.txt");
        return new File(dir.getAbsolutePath() + "/" + jarName);
    }

    /* Creates a jar in a temporary directory, with the given name & an empty manifest. */
    static private File createTempJar(String jarName) throws Exception {
        return createTempJar(jarName, "");
    }

    /* Create a JARDesc for the given URL location */
    static private JARDesc makeJarDesc(URL jarLocation) {
        return new JARDesc(jarLocation, new Version("1"), null, false,false, false,false);
    }

    /* A mocked dummy JNLP file with a single JAR. */
    private class MockedOneJarJNLPFile extends JNLPFile {
        URL codeBase, jarLocation;
        JARDesc jarDesc;

        MockedOneJarJNLPFile(File jarFile) throws MalformedURLException {
            codeBase = jarFile.getParentFile().toURI().toURL();
            jarLocation = jarFile.toURI().toURL();
            jarDesc = makeJarDesc(jarLocation); 
            info = new ArrayList<InformationDesc>();
        }

        @Override
        public ResourcesDesc getResources() {
            ResourcesDesc resources = new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
            resources.addResource(jarDesc);
            return resources;
        }
        @Override
        public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
            return new ResourcesDesc[] { getResources() };
        }

        @Override
        public URL getCodeBase() {
            return codeBase;
        }

        @Override
        public SecurityDesc getSecurity() {
            return new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS, null);
        }
    };

    /* Note: Only does file leak testing for now. */
    @Test
    public void constructorFileLeakTest() throws Exception {
        final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar"));

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
        final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar"));
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak( new Runnable () {
            @Override
            public void run() {
                    assertFalse(classLoader.isInvalidJar(jnlpFile.jarDesc));
            }
        });

    }

    /* Note: Only does file leak testing for now, but more testing could be added. */
    @Test
    public void activateNativeFileLeakTest() throws Exception {
        final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar"));
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        assertNoFileLeak( new Runnable () {
            @Override
            public void run() {
                    classLoader.activateNative(jnlpFile.jarDesc);
            }
        });
    }
    
    @Test
    public void getMainClassNameTest() throws Exception {
        /* Test with main-class */{
            final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar", "Main-Class: DummyClass\n"));
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals("DummyClass", classLoader.getMainClassName(jnlpFile.jarLocation));
                }
            });
        }
        /* Test with-out main-class */{
            final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar", ""));
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals(null, classLoader.getMainClassName(jnlpFile.jarLocation));
                }
            });
        }
    }

    static private <T> List<T> toList(T ... parts) {
        List<T> list = new ArrayList<T>();
        for (T part : parts) {
            list.add(part);
        }
        return list;
    }

    /* Note: Although it does a basic check, this mainly checks for file-descriptor leak */
    @Test
    public void checkForMainFileLeakTest() throws Exception {
        final MockedOneJarJNLPFile jnlpFile = new MockedOneJarJNLPFile(createTempJar("test.jar", ""));
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);
        assertNoFileLeak(new Runnable() {
            @Override
            public void run() {
                try {
                    classLoader.checkForMain(toList(jnlpFile.jarDesc));
                } catch (LaunchException e) {
                    fail(e.toString());
                }
            }
         });
        assertFalse(classLoader.hasMainJar());
    }
}