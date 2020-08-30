package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * ...
 */
class NotifyingInputStream extends FilterInputStream {
    private final Consumer<Long> downloadListener;
    private final long updateChunkSize;

    private long downloaded = 0;
    private long nextUpdateSize;

    public NotifyingInputStream(final InputStream inputStream, final long totalSize, final Consumer<Long> downloadListener) {
        super(inputStream);
        this.downloadListener = downloadListener;
        this.updateChunkSize = totalSize > 0 ? totalSize / 1000 : 1000;
        this.nextUpdateSize = updateChunkSize;
    }

    @Override
    public int read() throws IOException {
        final int value = super.read();
        try {
            if (value >= 0) {
                final long currentSize = ++downloaded;
                if (nextUpdateSize <= currentSize) {
                    nextUpdateSize = currentSize + updateChunkSize;
                    downloadListener.accept(currentSize);
                }
            }
        } catch (Exception ignored) {
        }
        return value;
    }
}
