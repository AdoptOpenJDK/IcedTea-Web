package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream decorator which reads the number of bytes read.
 */
class CountingInputStream extends FilterInputStream {

    private long count;

    CountingInputStream(InputStream delegate) {
        super(Assert.requireNonNull(delegate, "delegate"));
    }

    /**
     * @return the number of bytes read.
     */
    long numBytesRead() {
        return count;
    }

    @Override
    public int read() throws IOException {
        int result = in.read();
        if (result != -1) {
            count++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            count += result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("skip not supported");
    }

    @Override
    public synchronized void mark(int readlimit) {
        // do nothing as we do not allow reset;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
