package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class NativeLibrarySupport {

    private static final String[] NATIVE_LIBRARY_EXTENSIONS = {".so", ".dylib", ".jnilib", ".framework", ".dll"};

    private final File nativeSearchDirectory;

    public NativeLibrarySupport() throws IOException {
        //TODO: Old version uses FileUtils.createRestrictedDirectory(nativeDir); Is this needed??
        //TODO: Should we place the native files just in the temp folder? Maybe we can place them next to the cache?
        this.nativeSearchDirectory = Files.createTempDirectory("itw-native-" + UUID.randomUUID().toString()).toFile();
        this.nativeSearchDirectory.deleteOnExit();
    }

    public Optional<String> findLibrary(final String libname) {
        final File target = new File(nativeSearchDirectory, System.mapLibraryName(libname));
        if (target.exists()) {
            return Optional.of(target.getPath());
        }
        return Optional.empty();
    }

    public void addSearchJar(final URL jarLocation) throws IOException, URISyntaxException {
        final File localFile = Paths.get(jarLocation.toURI()).toFile();

        try (final JarFile jarFile = new JarFile(localFile, false)) {
            jarFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> isSupportedLibrary(entry.getName()))
                    .forEach(entry -> storeLibrary(jarFile, entry));
        }
    }

    private void storeLibrary(final JarFile jarFile, final JarEntry entry) {
        try {
            final File outFile = new File(nativeSearchDirectory, entry.getName());
            if (!outFile.isFile()) {
                FileUtils.createRestrictedFile(outFile);
            }
            try (final FileOutputStream out = new FileOutputStream(outFile)) {
                IOUtils.copy(jarFile.getInputStream(entry), out);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error while storing native library", e);
        }
    }

    private boolean isSupportedLibrary(final String filename) {
        return Stream.of(NATIVE_LIBRARY_EXTENSIONS).anyMatch(filename::endsWith);
    }
}
