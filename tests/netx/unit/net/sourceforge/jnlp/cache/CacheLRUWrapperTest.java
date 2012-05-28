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

import static org.junit.Assert.assertTrue;

import java.io.File;
import net.sourceforge.jnlp.ServerAccess;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import org.junit.BeforeClass;
import org.junit.Test;

public class CacheLRUWrapperTest {

    private final CacheLRUWrapper clw = CacheLRUWrapper.getInstance();
    private final String cacheDir = new File(JNLPRuntime.getConfiguration()
            .getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR)).getPath();

    // does no DeploymentConfiguration exist for this file name? 
    private final String cacheIndexFileName = "recently_used";

    private final int noEntriesCacheFile = 1000;

    @BeforeClass
    static public void setupJNLPRuntimeConfig() {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR, System.getProperty("java.io.tmpdir"));
    }
    
    @Test
    public void testLoadStoreTiming() throws InterruptedException {

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

        clw.unlock();
    }

    private void fillCacheIndexFile(int noEntries) {

        // fill cache index file
        for(int i = 0; i < noEntries; i++) {
            String path = cacheDir + File.separatorChar + i + File.separatorChar + "test" + i + ".jar";
            String key = clw.generateKey(path);
            clw.addEntry(key, path);
        }
    }

    @Test
    public void testModTimestampAfterStore() throws InterruptedException {

        final File cacheIndexFile = new File(cacheDir + File.separator + cacheIndexFileName);

        clw.lock();
        
        // 1. clear cache entries + store
        long lmBefore = cacheIndexFile.lastModified();
        clearCacheIndexFile();
        long lmAfter = cacheIndexFile.lastModified();
        assertTrue("modification timestamp hasn't changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore < lmAfter);

        // FIXME: wait a second, because of file modification timestamp only provides accuracy on seconds.
        Thread.sleep(1000);

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

        clw.unlock();
    }
    
    private void clearCacheIndexFile() {

        clw.lock();

        // clear cache + store file
        clw.clearLRUSortedEntries();
        clw.store();

        clw.unlock();
    }
}
