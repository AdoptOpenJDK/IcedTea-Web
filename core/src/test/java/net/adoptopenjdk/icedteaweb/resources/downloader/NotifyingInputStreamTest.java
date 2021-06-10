package net.adoptopenjdk.icedteaweb.resources.downloader;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.resources.downloader.NotifyingInputStream.MIN_CHUNK_SIZE;
import static net.adoptopenjdk.icedteaweb.resources.downloader.NotifyingInputStream.UNKNOWN_CHUNK_SIZE;
import static org.junit.Assert.assertEquals;

public class NotifyingInputStreamTest {

    private TestListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestListener();
    }

    @Test
    public void shouldNotifyOnceForEmptyStreamWhenReadByteByByte() throws IOException {
        // arrange
        final int size = 0;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readByteByByte(sut);

        // assert
        assertEquals(listOf(size), listener.notifications);
    }

    @Test
    public void shouldNotifyOnceForEmptyStreamWhenReadBlockwise() throws IOException {
        // arrange
        final int size = 0;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readBlock(sut, MIN_CHUNK_SIZE);

        // assert
        assertEquals(listOf(size), listener.notifications);
    }

    @Test
    public void shouldNotifyOnceForSmallStreamWhenReadByteByByte() throws IOException {
        // arrange
        final int size = MIN_CHUNK_SIZE / 2;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readByteByByte(sut);

        // assert
        assertEquals(listOf(size), listener.notifications);
    }

    @Test
    public void shouldNotifyOnceForSmallStreamWhenReadBlockwise() throws IOException {
        // arrange
        final int size = MIN_CHUNK_SIZE / 2;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readBlock(sut, MIN_CHUNK_SIZE);

        // assert
        assertEquals(listOf(size), listener.notifications);
    }

    @Test
    public void shouldNotifyMultipleTimesForLargeStreamWhenReadByteByByte() throws IOException {
        // arrange
        final int size = (MIN_CHUNK_SIZE * 3) + 1;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readByteByByte(sut);

        // assert
        assertEquals(listOf(MIN_CHUNK_SIZE, MIN_CHUNK_SIZE*2, MIN_CHUNK_SIZE*3, size), listener.notifications);
    }

    @Test
    public void shouldNotifyMultipleTimesForLargeStreamWhenReadBlockwise() throws IOException {
        // arrange
        final int size = (MIN_CHUNK_SIZE * 3) + 1;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, size, listener);

        // act
        readBlock(sut, MIN_CHUNK_SIZE);

        // assert
        assertEquals(listOf(MIN_CHUNK_SIZE, MIN_CHUNK_SIZE*2, MIN_CHUNK_SIZE*3, size), listener.notifications);
    }

    @Test
    public void shouldNotifyMultipleTimesForLargeStreamOfUnknownSizeWhenReadByteByByte() throws IOException {
        // arrange
        final int size = (UNKNOWN_CHUNK_SIZE * 3) + 1;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, 0, listener);

        // act
        readByteByByte(sut);

        // assert
        assertEquals(listOf(UNKNOWN_CHUNK_SIZE, UNKNOWN_CHUNK_SIZE*2, UNKNOWN_CHUNK_SIZE*3, size), listener.notifications);
    }

    @Test
    public void shouldNotifyMultipleTimesForLargeStreamOfUnknownSizeWhenReadBlockwise() throws IOException {
        // arrange
        final int size = (UNKNOWN_CHUNK_SIZE * 3) + 1;
        final InputStream in = inputStream(size);
        final NotifyingInputStream sut = new NotifyingInputStream(in, 0, listener);

        // act
        readBlock(sut, MIN_CHUNK_SIZE);

        // assert
        assertEquals(listOf(UNKNOWN_CHUNK_SIZE, UNKNOWN_CHUNK_SIZE*2, UNKNOWN_CHUNK_SIZE*3, size), listener.notifications);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void readByteByByte(InputStream in) throws IOException {
        while (in.read() != -1) ;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void readBlock(final InputStream in, final int blockSize) throws IOException {
        final byte[] block = new byte[blockSize];
        while (in.read(block, 0, MIN_CHUNK_SIZE) != -1) ;
    }

    private List<Long> listOf(int... ints) {
        return Arrays.stream(ints).asLongStream().boxed().collect(Collectors.toList());
    }

    private InputStream inputStream(final long size) {
        return new InputStream() {

            long bytesRemaining = size;

            @Override
            public int read() {
                if (bytesRemaining == 0) {
                    return -1;
                }
                bytesRemaining--;
                return 0;
            }
        };
    }

    private static class TestListener implements Consumer<Long> {

        private final List<Long> notifications = new ArrayList<>();

        @Override
        public void accept(final Long numBytes) {
            notifications.add(numBytes);
        }
    }
}
