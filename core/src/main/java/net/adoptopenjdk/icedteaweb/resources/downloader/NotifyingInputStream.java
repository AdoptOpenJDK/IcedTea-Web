package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Input stream which notifies a listener about its progress.
 */
class NotifyingInputStream extends CountingInputStream {
    private final Consumer<Long> downloadListener;
    private final long updateChunkSize;

    private long nextUpdateSize;

    public NotifyingInputStream(final InputStream inputStream, final long totalSize, final Consumer<Long> downloadListener) {
        super(inputStream);
        this.downloadListener = downloadListener;
        this.updateChunkSize = calculateChunkSize(totalSize);
        this.nextUpdateSize = updateChunkSize;
    }

    private long calculateChunkSize(final long totalSize) {
        return Long.max(totalSize / 1000, 1024);
    }

    @Override
    public int read() throws IOException {
        final int value = super.read();
        handleRead(value);
        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        handleRead(result);
        return result;
    }

    private void handleRead(long result) {
        if (result != -1) {
            if (numBytesRead() >= nextUpdateSize) {
                nextUpdateSize += updateChunkSize;
                notifyListener();
            }
        } else {
            notifyListener();
        }
    }

    private void notifyListener() {
        try {
            downloadListener.accept(numBytesRead());
        } catch (Exception ignored) {
        }
    }
}
