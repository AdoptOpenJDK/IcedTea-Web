package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * ...
 */
class NotifyingInputStream extends FilterInputStream {
    private final List<Consumer<Long>> downloadListener = new CopyOnWriteArrayList<>();
    private final long updateChunkSize;

    private long downloaded = 0;
    private long nextUpdateSize;

    public NotifyingInputStream(final InputStream inputStream, final long totalSize) {
        super(inputStream);
        this.updateChunkSize = totalSize > 0 ? totalSize / 1000 : 1000;
        this.nextUpdateSize = updateChunkSize;
    }

    public void addListener(Consumer<Long> listener) {
        downloadListener.add(listener);
    }

    @Override
    public int read() throws IOException {
        final int value = super.read();
        try {
            if (value >= 0) {
                final long currentSize = ++downloaded;
                if (nextUpdateSize <= currentSize) {
                    nextUpdateSize = currentSize + updateChunkSize;
                    downloadListener.forEach(l -> l.accept(currentSize));
                }
            }
        } catch (Exception ignored) {
        }
        return value;
    }
}
