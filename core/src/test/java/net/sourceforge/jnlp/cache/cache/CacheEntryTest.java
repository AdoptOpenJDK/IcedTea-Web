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

package net.sourceforge.jnlp.cache.cache;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.testing.util.CacheTestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CacheEntryTest {

    private static URL url;
    private static VersionId version;

    private List<String> sharedBuffer;

    private File cacheFile;
    private File infoFile;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
        version = VersionId.fromString("1.0");
    }

    @Before
    public void setUp() throws IOException {
        sharedBuffer = new ArrayList<>();

        final File tmpDir = temporaryFolder.newFolder();
        cacheFile = new File(tmpDir, "example.jar");
        infoFile = new File(tmpDir, CacheEntry.INFO_SUFFIX);

        FileUtils.saveFileUtf8("", infoFile);
        FileUtils.saveFileUtf8("", cacheFile);
    }

    @Test
    public void verifyLocationIsSame() {
        assertEquals(url, createEntry().getResourceHref());
    }

    @Test
    public void verifyDataIsSetCorrectly() {
        long LAST_MODIFIED = 999;
        long DOWNLOADED_AT = 888;
        long CONTENT_LENGTH = 777;

        final CacheEntry firstEntry = createEntry();
        assertNotEquals(LAST_MODIFIED, firstEntry.getLastModified());
        assertNotEquals(DOWNLOADED_AT, firstEntry.getDownloadedAt());
        assertNotEquals(CONTENT_LENGTH, firstEntry.getSize());

        firstEntry.storeInfo(new ResourceInfoBean(url, version, CONTENT_LENGTH, LAST_MODIFIED, DOWNLOADED_AT));

        final CacheEntry secondEntry = createEntry();
        assertEquals(LAST_MODIFIED, secondEntry.getLastModified());
        assertEquals(DOWNLOADED_AT, secondEntry.getDownloadedAt());
        assertEquals(CONTENT_LENGTH, secondEntry.getSize());
    }

    @Test
    public void verifyNotCachedIfFileIsAbsent() {
        assertTrue(cacheFile.delete());
        assertFalse(createEntry().isCached());
    }

    @Test
    public void verifyNotCachedIfContentLengthsDiffer() {
        final CacheEntry entry = createEntry();
        entry.setSize(10000);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyCachedIfContentLengthsAreSame() throws IOException {
        final String contents = "Foo";
        FileUtils.saveFileUtf8(contents, cacheFile);

        final CacheEntry entry = createEntry();
        entry.setSize(contents.length());

        assertTrue(entry.isCached());
    }

    @Test
    public void verifyCurrentWhenCacheEntryHasSameTimeStamp() throws IOException {
        final long lastModified = 10;
        final String contents = "Foo";
        FileUtils.saveFileUtf8(contents, cacheFile);

        final CacheEntry entry = createEntry();
        entry.setSize(cacheFile.length());
        entry.setLastModified(lastModified);

        assertTrue(entry.isCurrent(lastModified));
    }

    @Test
    public void verifyNotCurrentWhenRemoteContentIsNewer() throws IOException {
        final long oldTimeStamp = 10;
        final long newTimeStamp = 100;
        final String contents = "Foo";
        FileUtils.saveFileUtf8(contents, cacheFile);

        final CacheEntry entry = createEntry();
        entry.setSize(cacheFile.length());
        entry.setLastModified(oldTimeStamp);

        assertFalse(entry.isCurrent(newTimeStamp));
    }

    @Test(timeout = 2000L)
    public void testLock() {
        final CacheEntry entry = createEntry();
        try {
            entry.lock();
            assertTrue(entry.isHeldByCurrentThread());
        } finally {
            entry.unlock();
        }
    }

    @Test(timeout = 2000L)
    public void testUnlock() {
        final CacheEntry entry = createEntry();
        try {
            entry.lock();
        } finally {
            entry.unlock();
        }
        assertFalse(entry.isHeldByCurrentThread());
    }

    @Test(timeout = 2000L)
    public void testStoreFailsWithoutLock() {
        final CacheEntry entry = createEntry();
        long num = 10;
        entry.setLastModified(num);
        assertFalse(entry.store());
    }

    @Test(timeout = 2000L)
    public void testStoreWorksWithLocK() {
        final CacheEntry entry = createEntry();
        long num = 10;
        entry.setLastModified(num);
        try {
            entry.lock();
            assertTrue(entry.store());
        } finally {
            entry.unlock();
        }
    }

    @Test(timeout = 2000L)
    public void testMultiThreadLockPreventsWrite() throws Exception {
        final int numThreads = 100;
        final CountDownLatch doneSignal = new CountDownLatch(numThreads);
        final CountDownLatch writersDoneSignal = new CountDownLatch(numThreads);

        final CacheEntry entry = createEntry();
        final Thread[] list = new Thread[numThreads];

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

        final String out = String.join("\n", sharedBuffer);
        assertTrue(CacheTestUtils.stringContainsOnlySingleInstance(out, "true") && out.contains("false"));
    }


    @Test(timeout = 2000L)
    public void testMultiThreadLockAllowsRead() throws InterruptedException {
        final int numThreads = 2;
        final int numWriterThreads = 1;

        final CountDownLatch doneSignal = new CountDownLatch(numThreads);
        final CountDownLatch writersDoneSignal = new CountDownLatch(numWriterThreads);

        final CacheEntry entry = createEntry();

        final Thread writerThread = new Thread(new WriteWorker(10, entry, doneSignal, writersDoneSignal));
        writerThread.start();

        final Thread readerThread = new Thread(new ReadWorker(entry, writersDoneSignal, doneSignal));
        readerThread.start();

        writerThread.join();
        readerThread.join();

        final String out = String.join("\n", sharedBuffer);
        assertTrue(out.contains(":10:true") && out.contains(":read:10"));
    }

    private CacheEntry createEntry() {
        return new CacheEntry(url, version, cacheFile, infoFile);
    }

    private class WriteWorker implements Runnable {
        private final long input;
        private final CacheEntry entry;
        private final CountDownLatch doneSignal;
        private final CountDownLatch writersDoneSignal;

        WriteWorker(long input, CacheEntry entry, CountDownLatch doneSignal, CountDownLatch writersDoneSignal) {
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
                sharedBuffer.add(":" + input + ":" + entry.store());
                //Let parent know outputting is done
                doneSignal.countDown();
                writersDoneSignal.countDown();
                //Wait until everyone is done before continuing to entry.unlock()
                doneSignal.await();
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                entry.unlock();
            }
        }
    }

    private class ReadWorker implements Runnable {

        private final CacheEntry entry;
        private final CountDownLatch writersDone;

        private final CountDownLatch doneSignal;

        ReadWorker(CacheEntry entry, CountDownLatch writersDone, CountDownLatch doneSignal) {
            this.entry = entry;
            this.writersDone = writersDone;
            this.doneSignal = doneSignal;
        }
        @Override
        public void run() {
            try {
                writersDone.await();

                long lastModified = entry.getLastModified();
                sharedBuffer.add(":read:" + lastModified);
                doneSignal.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }
}
