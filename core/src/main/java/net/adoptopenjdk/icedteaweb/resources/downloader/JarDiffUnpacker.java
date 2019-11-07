package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.jardiff.JarDiffMerger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static net.sourceforge.jnlp.config.PathsAndFiles.TMP_DIR;

/**
 * ...
 */
class JarDiffUnpacker implements StreamUnpacker {

    private static final Logger LOG = LoggerFactory.getLogger(JarDiffUnpacker.class);

    private static final String JARDIFF_EXTENSION = ".jardiff";

    private final File cacheFile;

    JarDiffUnpacker(final File cacheFile) {
        this.cacheFile = Assert.requireNonNull(cacheFile, "cacheFile");
    }

    @Override
    public InputStream unpack(final InputStream input) throws IOException {
        Assert.requireNonNull(input, "input");

        LOG.info("Trying to merge JarDiff for '{}'", cacheFile.getName());

        final JarFile originalJar = new JarFile(cacheFile);
        final File diffJarFile = new File(TMP_DIR.getFile(), UUID.randomUUID().toString() + JARDIFF_EXTENSION);
        try(final FileOutputStream outputStream = new FileOutputStream(diffJarFile)) {
            IOUtils.copy(input, outputStream);
            final JarFile diffJar = new JarFile(diffJarFile);
            final ByteArrayOutputStream mergedJarContent = new ByteArrayOutputStream();
            try(final JarOutputStream resultJarOutputStream = new JarOutputStream(mergedJarContent)) {
                JarDiffMerger.merge(originalJar, diffJar, resultJarOutputStream);
            }
            return new ByteArrayInputStream(mergedJarContent.toByteArray());
        } finally {
            if(diffJarFile.exists() && !diffJarFile.delete()) {
                LOG.debug("Temp file '{}' for JarDiff merge can not be deleted. Will try to delete it on exit", diffJarFile.getName());
                diffJarFile.deleteOnExit();
            }
        }
    }
}
