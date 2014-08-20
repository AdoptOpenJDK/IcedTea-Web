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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.jnlp.Version;
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
    private ExecutorService executorService;
    Object listener = new Object();

    int num = 0;

    @Before
    public void setUp() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
        version = new Version("1.0");
        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);
        executorService = Executors.newSingleThreadExecutor();
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

    @Test
    public void testLock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        try {
            entry.lock();
            assertTrue(entry.isHeldByCurrentThread());
        } finally {
            entry.unlock();
        }
    }

    @Test
    public void testUnlock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        try {
            entry.lock();
        } finally {
            entry.unlock();
        }
        assertTrue(!entry.isHeldByCurrentThread());
    }

    @Test
    public void testStoreFailsWithoutLock() throws IOException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        long num = 10;
        entry.setLastModified(num);
        assertTrue(!entry.store());
    }

    @Test
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

    @Test
    public void testMultithreadLockPreventsWrite() throws IOException, InterruptedException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        Thread a = new Thread(new WriteWorker(10, entry));
        a.start();

        Thread b = new Thread(new WriteWorker(5, entry));
        b.start();

        Thread.sleep(2000l);

        synchronized (listener) {
            num = 1;
            listener.notifyAll();
        }

        String out = baos.toString();
        assertTrue(out.contains(":10:true") && out.contains(":5:false"));
    }

    @Test
    public void testMultithreadLockAllowsRead() throws IOException, InterruptedException {
        TestCacheEntry entry = new TestCacheEntry(url, version, null);
        Thread a = new Thread(new WriteWorker(10, entry));
        a.start();

        Thread.sleep(2000l);

        Thread b = new Thread(new ReadWorker(entry));
        b.start();

        Thread.sleep(1000l);

        synchronized (listener) {
            num = 1;
            listener.notifyAll();
        }

        String out = baos.toString();
        assertTrue(out.contains(":10:true") && out.contains(":read:10"));
    }

    private class WriteWorker implements Runnable {
        long input;
        TestCacheEntry entry;

        public WriteWorker(long input, TestCacheEntry entry) {
            this.input = input;
            this.entry = entry;
        }
        @Override
        public void run() {

            try {
                boolean result = entry.tryLock();
                entry.setLastModified(input);
                result = entry.store();
                executorService.execute(new WriteRunnable(":" + input + ":" + result));
                while (num == 0) {
                    synchronized (listener) {
                        listener.wait();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                entry.unlock();
            }
        }
    }

    private class WriteRunnable implements Runnable {
        private String msg;
        public WriteRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            out.println(msg);
            out.flush();
        }
    }

    private class ReadWorker implements Runnable {
        private TestCacheEntry entry;
        public ReadWorker(TestCacheEntry entry) {
            this.entry = entry;
        }
        @Override
        public void run() {
            long lastModified = entry.getLastModified();
            executorService.execute(new WriteRunnable(":read:" + lastModified));
        }
    }
}
