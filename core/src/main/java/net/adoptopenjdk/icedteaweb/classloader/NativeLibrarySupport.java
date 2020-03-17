// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Handles loading and access of native code loading through a JNLP application.
 * Stores native code in a temporary folder.
 */
 class NativeLibrarySupport {

    private static final String[] NATIVE_LIBRARY_EXTENSIONS = {".so", ".dylib", ".jnilib", ".framework", ".dll"};

    private final File nativeSearchDirectory;

    public NativeLibrarySupport() {
        this.nativeSearchDirectory = createNativeStoreDirectory();
    }

    public Optional<String> findLibrary(final String libname) {
        final File target = new File(nativeSearchDirectory, System.mapLibraryName(libname));
        if (target.exists()) {
            return Optional.of(target.getPath());
        }
        return Optional.empty();
    }

    public void addSearchJar(final URL jarLocation) {
        try {
            final File localFile = Paths.get(jarLocation.toURI()).toFile();

            try (final JarFile jarFile = new JarFile(localFile, false)) {
                jarFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(entry -> isSupportedLibrary(entry.getName()))
                        .forEach(entry -> storeLibrary(jarFile, entry));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Unable to inspect jar for native libraries: " + jarLocation, e);
        }
    }

    private synchronized void storeLibrary(final JarFile jarFile, final JarEntry entry) {
        try {
            final File outFile = new File(nativeSearchDirectory, entry.getName());
            if (outFile.exists()) {
                throw new RuntimeException("Native file with given name " + entry.getName() + " already exists.");
            }
            if (!outFile.isFile()) {
                FileUtils.createRestrictedFile(outFile);
                outFile.deleteOnExit();
            }
            try (final FileOutputStream out = new FileOutputStream(outFile)) {
                IOUtils.copy(jarFile.getInputStream(entry), out);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error while storing native library " + entry + " that is part of jar " + jarFile.getName(), e);
        }
    }

    private boolean isSupportedLibrary(final String filename) {
        return Stream.of(NATIVE_LIBRARY_EXTENSIONS).anyMatch(filename::endsWith);
    }

    /**
     * Create a random base directory to cache native code files in.
     * The directory has restricted access such that only the current user can access it.
     * This is to reduce the chance some other user can manipulate the native libs in the cache.
     */
    private static File createNativeStoreDirectory() {
        final String javaTempDir = JavaSystemProperties.getJavaTempDir();
        final File parent = new File(javaTempDir);
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IllegalStateException("Java temp dir '" + javaTempDir + "' is not a directory and cannot be created");
        }

        final File nativeDir = new File(parent, "itw-native-" + UUID.randomUUID().toString());
        try {
            FileUtils.createRestrictedDirectory(nativeDir);
            nativeDir.deleteOnExit();
            return nativeDir;
        } catch (IOException e) {
            throw new RuntimeException("Exception while creating native storage directory '" + nativeDir + "'", e);
        }
    }
}
