/* CacheLRUWrapperTest.java
   Copyright (C) 2012 Thomas Meyer

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.CacheTestUtils;

public class CacheLRUWrapperTest {

    // does no DeploymentConfiguration exist for this file name? 
    private static final String cacheIndexFileName = PathsAndFiles.CACHE_INDEX_FILE_NAME + "_testing";
    private static final File javaTmp = new File(System.getProperty("java.io.tmpdir"));
    private static final File tmpCache;
    private static final File tmpIndexFile;

    static {
        try {
            tmpCache = File.createTempFile("itw", "CacheLRUWrapperTest", javaTmp);
            tmpCache.delete();
            tmpCache.mkdir();
            tmpCache.deleteOnExit();
            if (!tmpCache.isDirectory()) {
                throw new IOException("Unsuccess to create tmpfile, remove it and createsame directory");
            }
            tmpIndexFile = new File(tmpCache, cacheIndexFileName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
    
    private static class DummyInfrastructureFileDescriptor extends InfrastructureFileDescriptor{
        private final File backend;

        
        private DummyInfrastructureFileDescriptor(File backend) {
            super();
            this.backend=backend;
        }

        @Override
        public File getFile() {
            return backend;
        }

        @Override
        public String getFullPath() {
            return backend.getAbsolutePath();
        }
        
    }
    
    private static final CacheLRUWrapper clw = new CacheLRUWrapper(
            new DummyInfrastructureFileDescriptor(tmpIndexFile),
            new DummyInfrastructureFileDescriptor(tmpCache));

    private final int noEntriesCacheFile = 1000;

    private ByteArrayOutputStream baos;
    private PrintStream out;



    @Before
    public void setup() {
        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);
    }

    @Test
    public void testLoadStoreTiming() throws InterruptedException {

        final File cacheIndexFile = clw.getRecentlyUsedFile().getFile();
        cacheIndexFile.delete();
        try {
            int noLoops = 1000;

            long time[] = new long[noLoops];

            clw.lock();
            clearCacheIndexFile();

            fillCacheIndexFile(noEntriesCacheFile);
            clw.store();

            // FIXME: wait a second, because of file modification timestamp only provides accuracy on seconds.
            Thread.sleep(1000);

            long sum = 0;
            for(int i=0; i < noLoops - 1; i++) {
                time[i]= System.nanoTime();
                clw.load();
                time[i+1]= System.nanoTime();
                if(i==0)
                    continue;
                sum = sum + time[i] - time[i-1];
            }

            double avg = sum / time.length;
            ServerAccess.logErrorReprint("Average = " + avg + "ns");

            // wait more than 100 microseconds for noLoops = 1000 and noEntries=1000 is bad
            assertTrue("load() must not take longer than 100 µs, but took in avg " + avg/1000 + "µs", avg < 100 * 1000);
        } finally {
            clw.unlock();
            cacheIndexFile.delete();
        }
    }

    private void fillCacheIndexFile(int noEntries) {

        // fill cache index file
        for(int i = 0; i < noEntries; i++) {
            String path = clw.getRecentlyUsedFile().getFullPath() + File.separatorChar + i + File.separatorChar + "test" + i + ".jar";
            String key = clw.generateKey(path);
            clw.addEntry(key, path);
        }
    }

    @Test
    public void testModTimestampAfterStore() throws InterruptedException {

        final File cacheIndexFile = clw.getRecentlyUsedFile().getFile();
        cacheIndexFile.delete();
        try{
        clw.lock();
        
        // 1. clear cache entries + store
        clw.addEntry("aa", "bb");
        clw.store();
        long lmBefore = cacheIndexFile.lastModified();
        Thread.sleep(1010);
        clearCacheIndexFile();
        long lmAfter = cacheIndexFile.lastModified();
        assertTrue("modification timestamp hasn't changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore < lmAfter);

        // FIXME: wait a second, because of file modification timestamp only provides accuracy on seconds.
        Thread.sleep(1010);

        // 2. load cache file
        lmBefore = cacheIndexFile.lastModified();
        clw.load();
        lmAfter = cacheIndexFile.lastModified();
        assertTrue("modification timestamp has changed!", lmBefore == lmAfter);

        // 3. add some cache entries and store
        lmBefore = cacheIndexFile.lastModified();
        fillCacheIndexFile(noEntriesCacheFile);
        clw.store();
        lmAfter = cacheIndexFile.lastModified();
        assertTrue("modification timestamp hasn't changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore < lmAfter);

        } finally {
            cacheIndexFile.delete();
            clw.unlock();
        }
    }
    
    private void clearCacheIndexFile() {

        clw.lock();

        try {
            // clear cache + store file
            clw.clearLRUSortedEntries();
            clw.store();
        } finally {
            clw.unlock();
        }
    }

    @Test
    public void testAddEntry() {
        String key = "key";
        String value = "value";

        clw.addEntry(key, value);
        assertTrue(clw.containsKey(key) && clw.containsValue(value));
    }

    @Test
    public void testRemoveEntry() {
        String key = "key";
        String value = "value";

        clw.addEntry(key, value);
        clw.removeEntry(key);
        assertFalse(clw.containsKey(key) && clw.containsValue(value));
    }

    @Test(timeout = 2000l)
    public void testLock() throws IOException {
        try {
            clw.lock();
            assertTrue(clw.getRecentlyUsedPropertiesFile().isHeldByCurrentThread());
        } finally {
            clw.unlock();
        }
    }

    @Test(timeout = 2000l)
    public void testUnlock() throws IOException {
        try {
            clw.lock();
        } finally {
            clw.unlock();
        }
        assertTrue(!clw.getRecentlyUsedPropertiesFile().isHeldByCurrentThread());
    }

    @Test(timeout = 2000l)
    public void testStoreFailsWithoutLock() throws IOException {
        assertTrue(!clw.store());
    }

    @Test(timeout = 2000l)
    public void testStoreWorksWithLocK() throws IOException {
        try {
            clw.lock();
            assertTrue(clw.store());
        } finally {
            clw.unlock();
        }
    }

    @Test(timeout = 2000l)
    public void testMultithreadLockPreventsStore() throws IOException, InterruptedException {
        int numThreads = 100;
        CountDownLatch doneSignal = new CountDownLatch(numThreads);

        Thread[] list = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            list[i] = new Thread(new StoreWorker(doneSignal));
        }

        for (int i = 0; i < numThreads; i++) {
            list[i].start();
        }

        //Wait for all children to finish
        for (int i = 0; i < numThreads; i++) {
            list[i].join();
        }

        String out = baos.toString();

        assertTrue(CacheTestUtils.stringContainsOnlySingleInstance(out, "true") && out.contains("false"));
    }

    private class StoreWorker implements Runnable {

        private final CountDownLatch doneSignal;

        public StoreWorker(CountDownLatch doneSignal) {
            this.doneSignal = doneSignal;
        }
        @Override
        public void run() {
            try {
                clw.getRecentlyUsedPropertiesFile().tryLock();
                boolean result = clw.store();
                synchronized (out) {
                    out.println(String.valueOf(result));
                    out.flush();
                }
                //Let parent know outputting is done
                doneSignal.countDown();
                //Wait until able to continue to clw.unlock()
                doneSignal.await();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clw.unlock();
            }
        }
    }
}
