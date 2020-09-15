package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Handles loading and access of native code loading through a JNLP application or applet.
 * Stores native code in a temporary folder.
 * Be sure to call {@link #cleanupTemporaryFolder()}  when finished with the object.
 */
public class NativeLibraryStorage {

    private static final Logger LOG = LoggerFactory.getLogger(NativeLibraryStorage.class);

    private static final Random RANDOM = new Random();
    public static final String NATIVE_LIB_EXT_DYLIB = ".dylib";
    public static final String NATIVE_LIB_EXT_JNILIB = ".jnilib";
    static final String[] NATIVE_LIBRARY_EXTENSIONS = {".so", NATIVE_LIB_EXT_DYLIB, NATIVE_LIB_EXT_JNILIB, ".framework", ".dll"};

    private final ResourceTracker tracker;
    private final List<File> nativeSearchDirectories = new ArrayList<>();

    /**
     * Temporary directory to store native jar entries, added to our search path
     */
    private File jarEntryDirectory = null;

    public NativeLibraryStorage(final ResourceTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Clean up our temporary folder if we created one.
     */
    public void cleanupTemporaryFolder() {
        if (jarEntryDirectory != null) {
            LOG.info("Cleaning up native directory {}", jarEntryDirectory.getAbsolutePath());
            try {
                FileUtils.recursiveDelete(jarEntryDirectory, jarEntryDirectory);
                nativeSearchDirectories.remove(jarEntryDirectory);
                jarEntryDirectory = null;
            } catch (IOException e) {
                /*
                 * failed to delete a file in tmpdir, no big deal (as well the VM
                 * might be shutting down at this point so no much we can do)
                 */
            }
        }
    }

    /**
     * Adds the {@link File} to the search path of this {@link NativeLibraryStorage}
     * when trying to find a native library
     *
     * @param directory directory to be added
     */
    public void addSearchDirectory(final File directory) {
        nativeSearchDirectories.add(directory);
    }

    public List<File> getSearchDirectories() {
        return nativeSearchDirectories;
    }

    /**
     * Looks in the search directories for 'fileName',
     * returning a path to the found file if it exists.
     *
     * @param fileName name of library to be found
     * @return path to library if found, null otherwise.
     */
    public File findLibrary(final String fileName) {
        for (final File dir : getSearchDirectories()) {
            final File target = new File(dir, fileName);
            if (target.exists()) {
                return target;
            }
        }
        return null;
    }

    /**
     * Search for and enable any native code contained in a JAR by copying the
     * native files into the filesystem. Called in the security context of the
     * classloader.
     *
     * @param jarLocation location of jar to be searched
     */
    public void addSearchJar(final URL jarLocation) {
        LOG.info("Activate native: {}", jarLocation);
        final File localFile = tracker.getCacheFile(jarLocation);
        if (localFile == null || !localFile.isFile()) {
            return;
        }

        try {
            try (JarFile jarFile = new JarFile(localFile, false)) {
                final Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    final JarEntry e = entries.nextElement();

                    if (e.isDirectory()) {
                        continue;
                    }

                    final String name = new File(e.getName()).getName();
                    boolean isLibrary = false;

                    for (final String suffix : NATIVE_LIBRARY_EXTENSIONS) {
                        if (name.endsWith(suffix)) {
                            isLibrary = true;
                            break;
                        }
                    }

                    if (isLibrary) {
                        final File outFile = new File(getNativeStoreDirectory(), name);
                        if (!outFile.isFile()) {
                            FileUtils.createRestrictedFile(outFile);
                        }
                        try (FileOutputStream out = new FileOutputStream(outFile)) {
                            IOUtils.copy(jarFile.getInputStream(e), out, 4096);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error("Exception while adding '" + localFile + "' to temporary search storage", ex);
        }
    }

    File getNativeStoreDirectory() {
        if (jarEntryDirectory == null) {
            jarEntryDirectory = createNativeStoreDirectory();
            addSearchDirectory(jarEntryDirectory);
        }
        return jarEntryDirectory;
    }

    /**
     * Create a random base directory to store native code files in.
     */
    private static File createNativeStoreDirectory() {
        final String javaTempDir = JavaSystemProperties.getJavaTempDir();
        final File parent = new File(javaTempDir);
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IllegalStateException("Java temp dir '" + javaTempDir + "' is not a directory and cannot be created");
        }

        final File nativeDir = new File(parent, "netx-native-" + RANDOM.nextInt(0xFFFF));
        try {
            FileUtils.createRestrictedDirectory(nativeDir);
            return nativeDir;
        } catch (IOException e) {
            throw new RuntimeException("Exception while creating native storage directory '" + nativeDir + "'", e);
        }
    }
}
