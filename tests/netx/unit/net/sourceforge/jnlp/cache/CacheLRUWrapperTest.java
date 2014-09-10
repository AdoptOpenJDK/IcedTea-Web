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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.util.PropertiesFile;

public class CacheLRUWrapperTest {

    private static final CacheLRUWrapper clw = CacheLRUWrapper.getInstance();
    private static String cacheDirBackup;
    private static PropertiesFile cacheOrderBackup;
    // does no DeploymentConfiguration exist for this file name? 
    private static  final String cacheIndexFileName = CacheLRUWrapper.CACHE_INDEX_FILE_NAME + "_testing";

    private final int noEntriesCacheFile = 1000;

    private ByteArrayOutputStream baos;
    private PrintStream out;
    private ExecutorService executorService;
    Object listener = new Object();

    int num = 0;

    @BeforeClass
    static public void setupJNLPRuntimeConfig() {
        cacheDirBackup = clw.cacheDir;
        cacheOrderBackup = clw.cacheOrder;
        clw.cacheDir=System.getProperty("java.io.tmpdir");
        clw.cacheOrder = new PropertiesFile( new File(clw.cacheDir + File.separator + cacheIndexFileName));
        
    }
    
    @AfterClass
    static public void restoreJNLPRuntimeConfig() {
        clw.cacheDir = cacheDirBackup;
        clw.cacheOrder = cacheOrderBackup;
    }

    @Before
    public void setup() {
        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testLoadStoreTiming() throws InterruptedException {

        final File cacheIndexFile = new File(clw.cacheDir + File.separator + cacheIndexFileName);
        cacheIndexFile.delete();
        try{
        
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
            String path = clw.cacheDir + File.separatorChar + i + File.separatorChar + "test" + i + ".jar";
            String key = clw.generateKey(path);
            clw.addEntry(key, path);
        }
    }

    @Test
    public void testModTimestampAfterStore() throws InterruptedException {

        final File cacheIndexFile = new File(clw.cacheDir + File.separator + cacheIndexFileName);
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

    @Test
    public void testLock() throws IOException {
        try {
            clw.lock();
            assertTrue(clw.cacheOrder.isHeldByCurrentThread());
        } finally {
            clw.unlock();
        }
    }

    @Test
    public void testUnlock() throws IOException {
        try {
            clw.lock();
        } finally {
            clw.unlock();
        }
        assertTrue(!clw.cacheOrder.isHeldByCurrentThread());
    }

    @Test
    public void testStoreFailsWithoutLock() throws IOException {
        assertTrue(!clw.store());
    }

    @Test
    public void testStoreWorksWithLocK() throws IOException {
        try {
            clw.lock();
            assertTrue(clw.store());
        } finally {
            clw.unlock();
        }
    }

    @Test
    public void testMultithreadLockPreventsStore() throws IOException, InterruptedException {
        Thread a = new Thread(new StoreWorker());
        a.start();

        Thread b = new Thread(new StoreWorker());
        b.start();

        Thread.sleep(2000l);

        synchronized (listener) {
            num = 1;
            listener.notifyAll();
        }

        String out = baos.toString();
        System.out.println();
        assertTrue(out.contains("true") && out.contains("false"));
    }

    private class StoreWorker implements Runnable {

        public StoreWorker() {
        }
        @Override
        public void run() {
            try {
                clw.cacheOrder.tryLock();
                boolean result = clw.store();
                executorService.execute(new WriteRunnable(String.valueOf(result)));
                while (num == 0) {
                    synchronized (listener) {
                        listener.wait();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clw.unlock();
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

}
