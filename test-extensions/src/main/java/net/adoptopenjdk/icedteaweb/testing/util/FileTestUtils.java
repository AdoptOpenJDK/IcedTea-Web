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

package net.adoptopenjdk.icedteaweb.testing.util;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.Assert.assertEquals;

public class FileTestUtils {

    private static final ObjectName OS;
    static {
        try {
            OS = new ObjectName("java.lang:type=OperatingSystem");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the open file-descriptor count for the process. Note that this is
     * specific to Unix-like operating systems.
     */
    private static long getOpenFileDescriptorCount() {
        final MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            return (Long) beanServer.getAttribute(OS, "OpenFileDescriptorCount");
        } catch (final Exception e) {
            // Effectively disables leak tests
            ServerAccess.logErrorReprint("Warning: Cannot get file descriptors for this platform!");
            return 0;
        }
    }

    /**
     * Check the amount of file descriptors before and after a Runnable
     */
    public static void assertNoFileLeak(final Runnable runnable) throws InterruptedException {
        Thread.sleep(200);
        final long filesOpenBefore = getOpenFileDescriptorCount();
        runnable.run();
        Thread.sleep(200);
        final long filesLeaked = getOpenFileDescriptorCount() - filesOpenBefore;
        //how come? Apparently can...
        if (filesLeaked < 0) {
            return;
        }
        assertEquals(0, filesLeaked);
    }

    /**
     * Creates a file with the given contents
     */
    public static void createFileWithContents(final File file, final String contents) throws IOException {
        createFile(file);
        FileUtils.saveFileUtf8(contents, file);
    }

    /**
     * Creates a jar in a temporary directory, with the given name & file contents
     */
    public static void createJarWithoutManifestContents(final File jarFile, final File... fileContents) throws Exception {
        createJarWithContents(jarFile, null, fileContents);
    }

    /**
     * Creates a jar in a temporary directory, with the given name, manifest & file contents
     */
    public static void createJarWithContents(final File jarFile, final File... fileContents) throws Exception {
        /* Note that we always specify a manifest, to avoid empty jars.
         * Empty jars are not allowed by icedtea-web during the zip-file header check. */
        createJarWithContents(jarFile, new Manifest(), fileContents);
    }

    /**
     * Creates a jar in a temporary directory, with the given name & file contents
     */
    public static void createJarWithContents(final File jarFile, final Manifest manifestContents, final File... fileContents)
            throws Exception {
        createFile(jarFile);

        /* Manifest quite evilly ignores all attributes if we don't specify a version!
         * Make sure it's set here. */
        if (manifestContents != null) {
            manifestContents.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }

        final JarOutputStream jarWriter;
        if (manifestContents == null) {
            jarWriter = new JarOutputStream(new FileOutputStream(jarFile));
        } else {
            jarWriter = new JarOutputStream(new FileOutputStream(jarFile), manifestContents);
        }

        for (final File file : fileContents) {
            jarWriter.putNextEntry(new JarEntry(file.getName()));
            final FileInputStream fileReader = new FileInputStream(file);
            IOUtils.copy(fileReader, jarWriter);
            fileReader.close();
            jarWriter.closeEntry();
        }

        jarWriter.flush();
        jarWriter.finish();
        jarWriter.close();
    }

    /**
     * Creates a temporary directory. Note that Java 7 has a method for this,
     * but we want to remain 6-compatible.
     */
    public static File createTempDirectory() throws IOException {
        final File file = File.createTempFile("temp", Long.toString(System.nanoTime()));
        file.delete();
        if (!file.mkdir()) {
            throw new IllegalStateException("Failed to create temporary directory '" + file + "' for test.");
        }
        return file;
    }

    private static void createFile(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        } else {
            final File dir = file.getParentFile();
            if (dir.isFile()) {
                dir.delete();
            }
            if (!dir.isDirectory()) {
                if (!dir.mkdirs()) {
                    throw new IllegalStateException("Failed to create directory '" + dir + "' for test.");
                }
            }
        }
        if (!file.createNewFile()) {
            throw new IllegalStateException("Failed to create file '" + file + "' for test.");
        }
    }
}
