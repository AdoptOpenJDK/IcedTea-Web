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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.util.FileTestUtils;

import org.junit.Test;

public class JNLPClassLoaderTest {

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
                    assertFalse(classLoader.isInvalidJar(jnlpFile.jarDesc));
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
                    assertEquals("DummyClass", classLoader.getMainClassName(jnlpFile.jarLocation));
                }
            });
        }
        /* Test with-out any main-class specified */ {
            FileTestUtils.createJarWithContents(jarLocation /* No contents */);

            final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation);
            final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

            assertNoFileLeak(new Runnable() {
                @Override
                public void run() {
                    assertEquals(null, classLoader.getMainClassName(jnlpFile.jarLocation));
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
                    classLoader.checkForMain(Arrays.asList(jnlpFile.jarDesc));
                } catch (LaunchException e) {
                    fail(e.toString());
                }
            }
         });
        assertFalse(classLoader.hasMainJar());
    }
}