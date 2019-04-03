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

package net.sourceforge.jnlp.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sourceforge.jnlp.ServerAccess;

public class FileTestUtils {

    /* Get the open file-descriptor count for the process. Note that this is
     * specific to Unix-like operating systems. */
    static public long getOpenFileDescriptorCount() {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            return (Long) beanServer.getAttribute(new ObjectName(
                    "java.lang:type=OperatingSystem"),
                    "OpenFileDescriptorCount");
        } catch (Exception e) {
            // Effectively disables leak tests
            ServerAccess.logErrorReprint("Warning: Cannot get file descriptors for this platform!");
            return 0;
        }
    }

    /* Check the amount of file descriptors before and after a Runnable */
    static public void assertNoFileLeak(Runnable runnable) throws InterruptedException {
        Thread.sleep(10);
        long filesOpenBefore = getOpenFileDescriptorCount();
        runnable.run();
        Thread.sleep(10);
        long filesLeaked = getOpenFileDescriptorCount() - filesOpenBefore;
        //how come? Appearently can...
        if (filesLeaked<0){
            return;
        }
        assertEquals(0, filesLeaked);
    }

    /* Creates a file with the given contents */
    static public void createFileWithContents(File file, String contents)
            throws IOException {
        PrintWriter out = new PrintWriter(file);
        out.write(contents);
        out.close();
    }

    /* Creates a jar in a temporary directory, with the given name & file contents */
    static public void createJarWithoutManifestContents(File jarFile, File... fileContents) throws Exception{
        createJarWithContents(jarFile, null, fileContents);
    }
    
    /* Creates a jar in a temporary directory, with the given name & file contents */
    static public void createJarWithContents(File jarFile, Manifest manifestContents, File... fileContents)
            throws Exception {
        /* Manifest quite evilly ignores all attributes if we don't specify a version! 
         * Make sure it's set here. */
        if (manifestContents != null){
            manifestContents.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }

        JarOutputStream jarWriter;
        if (manifestContents == null){
            jarWriter = new JarOutputStream(new FileOutputStream(jarFile));
        } else {
            jarWriter = new JarOutputStream(new FileOutputStream(jarFile), manifestContents);
        }
        for (File file : fileContents) {
            jarWriter.putNextEntry(new JarEntry(file.getName()));
            FileInputStream fileReader = new FileInputStream(file);
            StreamUtils.copyStream(fileReader, jarWriter);
            fileReader.close();
            jarWriter.closeEntry();
        }
        jarWriter.close();
    }

    /* Creates a jar in a temporary directory, with the given name, manifest & file contents */
    static public void createJarWithContents(File jarFile, File... fileContents) throws Exception {
        /* Note that we always specify a manifest, to avoid empty jars.
         * Empty jars are not allowed by icedtea-web during the zip-file header check. */
        createJarWithContents(jarFile, new Manifest(), fileContents);
    }

    /* Creates a temporary directory. Note that Java 7 has a method for this,
     * but we want to remain 6-compatible. */
    static public File createTempDirectory() throws IOException {
        File file = File.createTempFile("temp",
                Long.toString(System.nanoTime()));
        file.delete();
        if (!file.mkdir()) {
            throw new IOException("Failed to create temporary directory '"
                    + file + "' for test.");
        }
        return file;
    }

}
