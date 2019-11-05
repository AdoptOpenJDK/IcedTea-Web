package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * ...
 */
class JarDiffUnpacker implements StreamUnpacker {
    private final File cacheFile;

    public JarDiffUnpacker(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    @Override
    public InputStream unpack(InputStream input) throws IOException {

        throw new RuntimeException("Not implemented yet!");
    }
}
