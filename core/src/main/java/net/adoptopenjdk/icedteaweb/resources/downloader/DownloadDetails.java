package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * ...
 */
class DownloadDetails {
    final URL downloadFrom;
    final InputStream inputStream;
    final String contentType;
    final String contentEncoding;
    final String version;
    final long lastModified;
    final long totalSize;

    private List<BiConsumer<Long, Long>> downloadListener = new CopyOnWriteArrayList<>();

    private final AtomicLong downloaded = new AtomicLong(0);

    private final AtomicLong lastUpdateSize = new AtomicLong(0);


    private final AtomicLong updateChunkSize = new AtomicLong(1000);



    DownloadDetails(URL downloadFrom, InputStream inputStream, String contentType, String contentEncoding, String version, long lastModified, long totalSize) {
        this.downloadFrom = downloadFrom;

        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.version = version;
        this.lastModified = lastModified;
        this.totalSize = totalSize;
        if (totalSize > 0) {
            this.updateChunkSize.set(totalSize / 1000);
        }
        this.inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                final int value = inputStream.read();
                try {
                    if (value >= 0) {
                        final long currentSize = downloaded.incrementAndGet();
                        if (lastUpdateSize.get() + updateChunkSize.get() <= currentSize) {
                            lastUpdateSize.set(currentSize);
                            downloadListener.forEach(l -> l.accept(currentSize, totalSize));
                        }
                    }
                } finally {
                    return value;
                }
            }
        };
    }

    public void addListener(BiConsumer<Long, Long> listener) {
        downloadListener.add(listener);
    }

    public long getDownloaded() {
        return downloaded.get();
    }
}
