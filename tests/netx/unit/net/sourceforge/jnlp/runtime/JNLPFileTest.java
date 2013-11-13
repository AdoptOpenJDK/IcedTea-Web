/* 
 Copyright (C) 2013 Red Hat, Inc.

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

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.util.FileTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class JNLPFileTest {

    @Test
    public void removeTitle() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();
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
        manifest5.getMainAttributes().put(new Attributes.Name(JNLPFile.APP_NAME), "Manifested Name");


        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);
        FileTestUtils.createJarWithContents(jarLocation4, manifest4);
        FileTestUtils.createJarWithContents(jarLocation5, manifest5);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(3, jarLocation5, jarLocation3, jarLocation4, jarLocation1, jarLocation2); //jar 1 should be main
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getMainClass());
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNull("no classlaoder attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.APP_NAME)));

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertNull(jnlpFile.getTitle());

        setTitle(jnlpFile);

        Assert.assertEquals("jnlp title", jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertEquals("jnlp title", jnlpFile.getTitle());

        removeTitle(jnlpFile);

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertNull(jnlpFile.getTitle());

        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);

        // thsi si strange, but not part of this test
        // Assert.assertNotNull("classlaoder attached, should be not null", jnlpFile.getManifestsAttributes().getMainClass());
        Assert.assertNull("defined twice, shoud be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNotNull("classlaoder attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNotNull("classlaoder attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("not deffined, should benull", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNotNull("classlaoder attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNotNull("classlaoder attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.APP_NAME)));
        //correct values are also tested in JnlpClassloaderTest
        Assert.assertEquals("classlaoder attached, should be not null", "it", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertEquals("classlaoder attached, should be not null", "DummyClass1", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertEquals("classlaoder attached, should be not null", "some url1", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertEquals("classlaoder attached, should be not null", "Manifested Name", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.APP_NAME)));

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitleFromManifest());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitle());

        setTitle(jnlpFile);

        Assert.assertEquals("jnlp title", jnlpFile.getTitleFromJnlp());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitleFromManifest());
        Assert.assertEquals("jnlp title (Manifested Name)", jnlpFile.getTitle());

    }

    private void setTitle(final DummyJNLPFileWithJar jnlpFile) {
        setTitle(jnlpFile, "jnlp title");
    }

    private void setTitle(final DummyJNLPFileWithJar jnlpFile, final String title) {
        jnlpFile.setInfo(Arrays.asList(new InformationDesc[]{
                    new InformationDesc(new Locale[]{}) {
                        @Override
                        public String getTitle() {
                            return title;
                        }
                    }
                }));
    }

    private void removeTitle(final DummyJNLPFileWithJar jnlpFile) {
        jnlpFile.setInfo(Arrays.asList(new InformationDesc[]{}));
    }
}
