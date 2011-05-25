/* CacheLRUWrapper -- Handle LRU for cache files.
   Copyright (C) 2011 Red Hat, Inc.

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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.PropertiesFile;

/**
 * This class helps maintain the ordering of most recently use items across
 * multiple jvm instances.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
enum CacheLRUWrapper {
    INSTANCE;

    private int lockCount = 0;

    /* lock for the file RecentlyUsed */
    private FileLock fl = null;

    /* location of cache directory */
    private final String cacheDir = new File(JNLPRuntime.getConfiguration()
            .getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR)).getPath();

    /*
     * back-end of how LRU is implemented This file is to keep track of the most
     * recently used items. The items are to be kept with key = (current time
     * accessed) followed by folder of item. value = path to file.
     */
    private PropertiesFile cacheOrder = new PropertiesFile(
            new File(cacheDir + File.separator + "recently_used"));

    private CacheLRUWrapper(){
        File f = cacheOrder.getStoreFile();
        if (!f.exists()) {
            try {
                FileUtils.createParentDir(f);
                FileUtils.createRestrictedFile(f, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns an instance of the policy.
     * 
     * @param propertiesFile
     * @return
     */
    public static CacheLRUWrapper getInstance() {
        return INSTANCE;
    }

    /**
     * Update map for keeping track of recently used items.
     */
    public synchronized void load() {
        cacheOrder.load();
    }

    /**
     * Write file to disk.
     */
    public synchronized void store() {
        cacheOrder.store();
    }

    /**
     * This adds a new entry to file.
     * 
     * @param key key we want path to be associated with.
     * @param path path to cache item.
     * @return true if we successfully added to map, false otherwise.
     */
    public synchronized boolean addEntry(String key, String path) {
        if (cacheOrder.containsKey(key)) return false;
        cacheOrder.setProperty(key, path);
        return true;
    }

    /**
     * This removed an entry from our map.
     * 
     * @param key key we want to remove.
     * @return true if we successfully removed key from map, false otherwise.
     */
    public synchronized boolean removeEntry(String key) {
        if (!cacheOrder.containsKey(key)) return false;
        cacheOrder.remove(key);
        return true;
    }

    private String getIdForCacheFolder(String folder) {
        int len = cacheDir.length();
        int index = folder.indexOf(File.separatorChar, len + 1);
        return folder.substring(len + 1, index);
    }

    /**
     * This updates the given key to reflect it was recently accessed.
     * 
     * @param oldKey Key we wish to update.
     * @return true if we successfully updated value, false otherwise.
     */
    public synchronized boolean updateEntry(String oldKey) {
        if (!cacheOrder.containsKey(oldKey)) return false;
        String value = cacheOrder.getProperty(oldKey);
        String folder = getIdForCacheFolder(value);

        cacheOrder.remove(oldKey);
        cacheOrder.setProperty(Long.toString(System.currentTimeMillis()) + "," + folder, value);
        return true;
    }

    /**
     * Return a copy of the keys available.
     * 
     * @return List of Strings sorted by ascending order.
     */
    public synchronized List<Entry<String, String>> getLRUSortedEntries() {
        ArrayList<Entry<String, String>> entries = new ArrayList(cacheOrder.entrySet());
        // sort by keys in descending order.
        Collections.sort(entries, new Comparator<Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                try {
                    Long t1 = Long.parseLong(e1.getKey().split(",")[0]);
                    Long t2 = Long.parseLong(e2.getKey().split(",")[0]);

                    int c = t1.compareTo(t2);
                    return c < 0 ? 1 : (c > 0 ? -1 : 0);
                } catch (NumberFormatException e) {
                    // Perhaps an error is too harsh. Maybe just somehow turn
                    // caching off if this is the case.
                    throw new InternalError("Corrupt LRU file entries");
                }
            }
        });
        return entries;
    }

    /**
     * Lock the file to have exclusive access.
     */
    public synchronized void lock() {
        try {
            fl = FileUtils.getFileLock(cacheOrder.getStoreFile().getPath(), false, true);
        } catch (OverlappingFileLockException e) { // if overlap we just increase the count.
        } catch (Exception e) { // We didn't get a lock..
            e.printStackTrace();
        }
        if (fl != null) lockCount++;
    }

    /**
     * Unlock the file.
     */
    public synchronized void unlock() {
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

    /**
     * Return the value of given key.
     * 
     * @param key
     * @return value of given key, null otherwise.
     */
    public synchronized String getValue(String key) {
        return cacheOrder.getProperty(key);
    }

    /**
     * Test if we the key provided is in use.
     * 
     * @param key key to be tested.
     * @return true if the key is in use.
     */
    public synchronized boolean contains(String key) {
        return cacheOrder.contains(key);
    }

    /**
     * Generate a key given the path to file. May or may not generate the same
     * key given same path.
     * 
     * @param path Path to generate a key with.
     * @return String representing the a key.
     */
    public String generateKey(String path) {
        return System.currentTimeMillis() + "," + getIdForCacheFolder(path);
    }
}
