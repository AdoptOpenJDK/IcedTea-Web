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

class NativeLibrarySupport {

    private static final String[] NATIVE_LIBRARY_EXTENSIONS = {".so", ".dylib", ".jnilib", ".framework", ".dll"};

    private final File nativeSearchDirectory;

    public NativeLibrarySupport() {
        this.nativeSearchDirectory = createNativeStoreDirectory();
        this.nativeSearchDirectory.deleteOnExit();
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
            return nativeDir;
        } catch (IOException e) {
            throw new RuntimeException("Exception while creating native storage directory '" + nativeDir + "'", e);
        }
    }
}
