/*
Copyright (C) 2013 Red Hat, Inc.

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

package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.DefaultResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils.assertNoFileLeak;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class NativeLibraryStorageTest {

    /**************************************************************************
     *                          Test helpers                                    *
     **************************************************************************/

    /* Associates an extension with whether it represents a native library */
    static class FileExtension {
        FileExtension(String extension, boolean isNative) {
            this.extension = extension;
            this.isNative = isNative;
        }

        final String extension;
        final boolean isNative;
    }

    private static List<FileExtension> makeExtensionsToTest() {
        List<FileExtension> exts = new ArrayList<>();
        exts.add(new FileExtension(".foobar", false)); /* Dummy non-native test extension */
        for (String ext : NativeLibraryStorage.NATIVE_LIBRARY_EXTENSIONS) {
            exts.add(new FileExtension(ext, true));
        }
        return exts;
    }

    /* All the native library types we support, as well as one negative test */
    private static final List<FileExtension> extensionsToTest = makeExtensionsToTest();

    /* Creates a NativeLibraryStorage object, caching the given URLs */
    private static NativeLibraryStorage nativeLibraryStorageWithCache(URL... urlsToCache) {
        ResourceTracker tracker = new DefaultResourceTracker();
        for (URL urlToCache : urlsToCache) {
            tracker.addResource(urlToCache, VersionString.fromString("1.0"), UpdatePolicy.ALWAYS);
        }

        return new NativeLibraryStorage(tracker);
    }

    /**************************************************************************
     *                          Test cases                                    *
     **************************************************************************/

    /* Tests searching for native libraries in jars */
    @Test
    public void testJarFileSearch() throws Exception {
        /* Create a temporary directory to create jars in */
        File tempDirectory = FileTestUtils.createTempDirectory();

        for (FileExtension ext : extensionsToTest) {
            /* Create empty file to search for */
            String testFileName = "foobar" + ext.extension;
            File testFile = new File(tempDirectory, testFileName);
            FileTestUtils.createFileWithContents(testFile, "");

            /* Create jar to search in */
            File jarLocation = new File(tempDirectory, "test.jar");
            FileTestUtils.createJarWithContents(jarLocation, testFile);

            final URL tempJarUrl = jarLocation.toURI().toURL();
            final NativeLibraryStorage storage = nativeLibraryStorageWithCache(tempJarUrl);

            assertNoFileLeak(() -> storage.addSearchJar(tempJarUrl));

            /* If the file we added is native, it should be found
             * Due to an implementation detail, non-native files will not be found */
            boolean testFileWasFound = storage.findLibrary(testFileName) != null;
            assertEquals(ext.isNative, testFileWasFound);
        }
    }

    /* Tests searching for native libraries in directories */
    @Test
    public void testDirectorySearch() throws Exception {
        /* Create a temporary directory to search in */
        File tempDirectory = FileTestUtils.createTempDirectory();

        for (FileExtension ext : extensionsToTest) {
            /* Create empty file in the directory */
            String testFileName = "foobar" + ext.extension;
            FileTestUtils.createFileWithContents(new File(tempDirectory, testFileName), "");

            /* Add the directory to the search list */
            NativeLibraryStorage storage = nativeLibraryStorageWithCache(/* None needed */);
            storage.addSearchDirectory(tempDirectory);

            /* Ensure directory is in our search list */
            assertTrue(storage.getSearchDirectories().contains(tempDirectory));

            /* The file should be found, regardless if it was native */
            boolean testFileWasFound = storage.findLibrary(testFileName) != null;
            assertTrue(testFileWasFound);
        }
    }

    @Test
    public void testCleanupTemporaryFolder() {
        NativeLibraryStorage storage = nativeLibraryStorageWithCache(/* None needed */);
        storage.getNativeStoreDirectory();

        /* The temporary native store directory should be our only search folder */
        assertEquals(1, storage.getSearchDirectories().size());

        File searchDirectory = storage.getSearchDirectories().get(0);
        assertTrue(searchDirectory.exists());

        /* Test that it has been deleted */
        storage.cleanupTemporaryFolder();
        assertFalse(searchDirectory.exists());
    }
}
