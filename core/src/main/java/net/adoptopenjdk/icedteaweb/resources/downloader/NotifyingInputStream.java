package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Input stream which notifies a listener about its progress.
 */
class NotifyingInputStream extends FilterInputStream {
    private final Consumer<Long> downloadListener;
    private final long updateChunkSize;

    private long downloaded = 0;
    private long nextUpdateSize;

    public NotifyingInputStream(final InputStream inputStream, final long totalSize, final Consumer<Long> downloadListener) {
        super(inputStream);
        this.downloadListener = downloadListener;
        this.updateChunkSize = calculateChunkSize(totalSize);
        this.nextUpdateSize = updateChunkSize;
    }

    private long calculateChunkSize(long totalSize) {
        if (totalSize <= 0) {
            return 1000;
        }

        return Long.max(totalSize / 1000, 100);
    }

    @Override
    public int read() throws IOException {
        final int value = super.read();
        if (value >= 0) {
            if (nextUpdateSize <= ++downloaded) {
                nextUpdateSize = downloaded + updateChunkSize;
                notifyListener(downloaded);
            }
        }
        return value;
    }

    private void notifyListener(final long value) {
        try {
            downloadListener.accept(value);
        } catch (Exception ignored) {
        }
    }
}
