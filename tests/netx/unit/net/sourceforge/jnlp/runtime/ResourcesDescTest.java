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
import java.util.jar.Manifest;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.util.FileTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class ResourcesDescTest {

    @Test
    public void checkGetMainJar_noMainSet() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();

        File jarLocation1 = new File(tempDirectory, "test1.jar");
        File jarLocation2 = new File(tempDirectory, "test2.jar");
        File jarLocation3 = new File(tempDirectory, "test3.jar");

        Manifest manifest1 = new Manifest();
        Manifest manifest2 = new Manifest();
        Manifest manifest3 = new Manifest();
        Manifest manifest4 = new Manifest();
        Manifest manifest5 = new Manifest();

        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(jarLocation1, jarLocation2, jarLocation3);
        JARDesc result = jnlpFile.getResources().getMainJAR();
        Assert.assertTrue("first jar must be returned", result.getLocation().getFile().endsWith("test1.jar"));
    }

    @Test
    public void checkGetMainJar_mainSet() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();

        File jarLocation1 = new File(tempDirectory, "test1.jar");
        File jarLocation2 = new File(tempDirectory, "test2.jar");
        File jarLocation3 = new File(tempDirectory, "test3.jar");

        Manifest manifest1 = new Manifest();
        Manifest manifest2 = new Manifest();
        Manifest manifest3 = new Manifest();
        Manifest manifest4 = new Manifest();
        Manifest manifest5 = new Manifest();

        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);

        DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(0, jarLocation1, jarLocation2, jarLocation3);
        JARDesc result = jnlpFile.getResources().getMainJAR();
        Assert.assertTrue("main jar must be returned", result.getLocation().getFile().endsWith("test1.jar"));

        jnlpFile = new DummyJNLPFileWithJar(1, jarLocation1, jarLocation2, jarLocation3);
        result = jnlpFile.getResources().getMainJAR();
        Assert.assertTrue("main jar must be returned", result.getLocation().getFile().endsWith("test2.jar"));

        jnlpFile = new DummyJNLPFileWithJar(2, jarLocation1, jarLocation2, jarLocation3);
        result = jnlpFile.getResources().getMainJAR();
        Assert.assertTrue("main jar must be returned", result.getLocation().getFile().endsWith("test3.jar"));
    }

}
