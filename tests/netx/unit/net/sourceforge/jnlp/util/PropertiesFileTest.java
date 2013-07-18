/*  PropertiesFileTest.java
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

package net.sourceforge.jnlp.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import net.sourceforge.jnlp.cache.CacheLRUWrapper;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import org.junit.BeforeClass;
import org.junit.Test;

public class PropertiesFileTest {

    private int lockCount = 0;

    /* lock for the file RecentlyUsed */
    private FileLock fl = null;

    private final String cacheDir = new File(JNLPRuntime.getConfiguration()
            .getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR)).getPath();

    // does no DeploymentConfiguration exist for this file name? 
    private final String cacheIndexFileName = CacheLRUWrapper.CACHE_INDEX_FILE_NAME;

    private final PropertiesFile cacheIndexFile = new PropertiesFile(new File(cacheDir + File.separatorChar + cacheIndexFileName));
    private final int noEntriesCacheFile = 1000;

    @BeforeClass
    static public void setupJNLPRuntimeConfig() {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR, System.getProperty("java.io.tmpdir"));
    }

    private void fillCacheIndexFile(int noEntries) {

        // fill cache index file
        for(int i = 0; i < noEntries; i++) {
            String path = cacheDir + File.separatorChar + i + File.separatorChar + "test" + i + ".jar";
            String key = String.valueOf(System.currentTimeMillis());
            cacheIndexFile.setProperty(key, path);
        }
    }

    @Test
    public void testReloadAfterStore() {

        lock();

        boolean reloaded = false;

        // 1. clear cache entries + store
        clearCacheIndexFile();

        // 2. load cache file
        reloaded = cacheIndexFile.load();
        assertTrue("File was not reloaded!", reloaded);

        // 3. add some cache entries and store
        fillCacheIndexFile(noEntriesCacheFile);
        cacheIndexFile.store();
        reloaded = cacheIndexFile.load();
        assertTrue("File was not reloaded!", reloaded);

        unlock();
    }
    
    private void clearCacheIndexFile() {

        lock();

        // clear cache + store file
        cacheIndexFile.clear();
        cacheIndexFile.store();

        unlock();
    }

    
    // add locking, because maybe some JNLP runtime user is running. copy/paste from CacheLRUWrapper

    /**
     * Lock the file to have exclusive access.
     */
    private void lock() {
        try {
            fl = FileUtils.getFileLock(cacheIndexFile.getStoreFile().getPath(), false, true);
        } catch (OverlappingFileLockException e) { // if overlap we just increase the count.
        } catch (Exception e) { // We didn't get a lock..
            e.printStackTrace();
        }
        if (fl != null) lockCount++;
    }
    
    /**
     * Unlock the file.
     */
    private void unlock() {
        if (fl != null) {
            lockCount--;
            try {
                if (lockCount == 0) {
                    fl.release();
                    fl.channel().close();
                    fl = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
