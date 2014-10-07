/* CacheEntryTest -- unit test for CacheEntry
   Copyright (C) 2014 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.util.CacheTestUtils;
import net.sourceforge.jnlp.util.PropertiesFile;

public class CacheEntryTest {

    /** A custom subclass that allows supplying num predefined cache file */
    static class TestCacheEntry extends CacheEntry {
        private File cacheFile;
        public TestCacheEntry(URL location, Version version, File cacheFile) {
            super(location, version);
            this.cacheFile = cacheFile;
        }
        @Override
        protected File getCacheFile() {
            return cacheFile;
        }
        @Override
        protected PropertiesFile readCacheEntryInfo() {
            try {
                return new PropertiesFile(createFile(""));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    private URL url;
    private Version version;

    private ByteArrayOutputStream baos;
    private PrintStream out;

    @Before
    public void setUp() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
        version = new Version("1.0");
        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);
    }

    @Test
    public void verifyLocationIsSame() {
        CacheEntry entry = new TestCacheEntry(url, version, null);
        assertEquals(url, entry.getLocation());
    }

    @Test
    public void verifyLastModifiedIsSetCorrectly() {
        long LAST_MODIFIED = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setLastModified(LAST_MODIFIED);

        assertEquals(LAST_MODIFIED, entry.getLastModified());
    }

    @Test
    public void verifyLastUpdatedIsSetCorrectly() {
        long LAST_UPDATED = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setLastUpdated(LAST_UPDATED);

        assertEquals(LAST_UPDATED, entry.getLastUpdated());
    }

    @Test
    public void verifyContentLengthIsSetCorrectly() {
        long CONTENT_LENGTH = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setRemoteContentLength(CONTENT_LENGTH);

        assertEquals(CONTENT_LENGTH, entry.getRemoteContentLength());
    }

    @Test
    public void verifyOriginalContentLengthIsSetCorrectly() {
        long ORIGINAL_CONTENT_LENGTH = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setOriginalContentLength(ORIGINAL_CONTENT_LENGTH);

        assertEquals(ORIGINAL_CONTENT_LENGTH, entry.getOriginalContentLength());
    }

    @Test
    public void verifyNotCachedIfFileIsAbsent() {
        File doesNotExist = new File("/foo/bar/baz/spam/eggs");

        CacheEntry entry = new TestCacheEntry(url, version, doesNotExist);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyNotCachedIfContentLengthsDiffer() throws IOException {
        File cachedFile = createFile("Foo");

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(10000);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyCachedIfContentLengthsAreSame() throws IOException {
        String contents = "Foo";
        File cachedFile = createFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());

        assertTrue(entry.isCached());
    }

    @Test
    public void verifyCachedIfOriginalContentLengthsAreSame() throws IOException {
        String contents = "FooDECOMPRESSED";
        long compressedLength = 5;
        File cachedFile = createFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(compressedLength);
        entry.setOriginalContentLength(compressedLength);

        assertTrue(entry.isCached());
    }

    @Test
    public void verifyCurrentWhenCacheEntryHasSameTimeStamp() throws IOException {
        long lastModified = 10;
        String contents = "Foo";
        File cachedFile = createFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());
        entry.setLastModified(lastModified);

        assertTrue(entry.isCurrent(lastModified));
    }

    @Test
    public void verifyNotCurrentWhenRemoteContentIsNewer() throws IOException {
        long oldTimeStamp = 10;
        long newTimeStamp = 100;
        String contents = "Foo";
        File cachedFile = createFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());
        entry.setLastModified(oldTimeStamp);

        assertFalse(entry.isCurrent(newTimeStamp));
    }

    private static File createFile(String contents) throws IOException {
        File cachedFile = File.createTempFile("CacheEntryTest", null);
        Files.write(cachedFile.toPath(), contents.getBytes());
        cachedFile.deleteOnExit();
        return cachedFile;
    }

    @Test(timeout = 2000l)
    public void testLock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        try {
            entry.lock();
            assertTrue(entry.isHeldByCurrentThread());
        } finally {
            entry.unlock();
        }
    }

    @Test(timeout = 2000l)
    public void testUnlock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        try {
            entry.lock();
        } finally {
            entry.unlock();
        }
        assertTrue(!entry.isHeldByCurrentThread());
    }

    @Test(timeout = 2000l)
    public void testStoreFailsWithoutLock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        long num = 10;
        entry.setLastModified(num);
        assertTrue(!entry.store());
    }

    @Test(timeout = 2000l)
    public void testStoreWorksWithLocK() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        long num = 10;
        entry.setLastModified(num);
        try {
            entry.lock();
            assertTrue(entry.store());
        } finally {
            entry.unlock();
        }
    }

    @Test(timeout = 2000l)
    public void testMultithreadLockPreventsWrite() throws IOException, InterruptedException {
        int numThreads = 100;
        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        CountDownLatch writersDoneSignal = new CountDownLatch(numThreads);

        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        Thread[] list = new Thread[numThreads];

        for (int i=0; i<numThreads; i++) {
            list[i] = new Thread(new WriteWorker(i, entry, doneSignal, writersDoneSignal));
        }

        for (int i=0; i<numThreads; i++) {
            list[i].start();
        }

        //Wait for all children to finish
        for (int i = 0; i < numThreads; i++) {
            list[i].join();
        }

        String out = baos.toString();
        assertTrue(CacheTestUtils.stringContainsOnlySingleInstance(out, "true") && out.contains("false"));
    }


    @Test(timeout = 2000l)
    public void testMultithreadLockAllowsRead() throws IOException, InterruptedException {
        int numThreads = 2;
        int numWriterThreads = 1;

        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        CountDownLatch writersDoneSignal = new CountDownLatch(numWriterThreads);

        TestCacheEntry entry = new TestCacheEntry(url, version, null);

        Thread writerThread = new Thread(new WriteWorker(10, entry, doneSignal, writersDoneSignal));
        writerThread.start();

        Thread readerThread = new Thread(new ReadWorker(entry, writersDoneSignal, doneSignal));
        readerThread.start();

        writerThread.join();
        readerThread.join();

        String out = baos.toString();
        assertTrue(out.contains(":10:true") && out.contains(":read:10"));
    }

    private class WriteWorker implements Runnable {
        private final long input;
        private final TestCacheEntry entry;
        private final CountDownLatch doneSignal;
        private final CountDownLatch writersDoneSignal;

        public WriteWorker(long input, TestCacheEntry entry, CountDownLatch doneSignal, CountDownLatch writersDoneSignal) {
            this.input = input;
            this.entry = entry;
            this.doneSignal = doneSignal;
            this.writersDoneSignal = writersDoneSignal;
        }
        @Override
        public void run() {
            try {
                entry.tryLock();
                entry.setLastModified(input);
                boolean result = entry.store();
                synchronized (out) {
                    out.println(":" + input + ":" + result);
                    out.flush();
                }
                //Let parent know outputting is done
                doneSignal.countDown();
                writersDoneSignal.countDown();
                //Wait until everyone is done before continuing to clw.unlock()
                doneSignal.await();
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            } finally {
                entry.unlock();
            }
        }
    }

    private class ReadWorker implements Runnable {
        private final TestCacheEntry entry;

        private final CountDownLatch writersDone;
        private final CountDownLatch doneSignal;

        public ReadWorker(TestCacheEntry entry, CountDownLatch writersDone, CountDownLatch doneSignal) {
            this.entry = entry;
            this.writersDone = writersDone;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            try {
                writersDone.await();

                long lastModified = entry.getLastModified();
                synchronized (out) {
                    out.println(":read:" + lastModified);
                    out.flush();
                    doneSignal.countDown();
                    doneSignal.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }
    }
}
