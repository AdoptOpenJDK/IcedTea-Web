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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;

import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * This class helps maintain the ordering of most recently use items across
 * multiple jvm instances.
 * 
 */
public class CacheLRUWrapper {
    
    /*
     * back-end of how LRU is implemented This file is to keep track of the most
     * recently used items. The items are to be kept with key = (current time
     * accessed) followed by folder of item. value = path to file.
     */
    
    private final InfrastructureFileDescriptor recentlyUsedPropertiesFile;
    private final InfrastructureFileDescriptor cacheDir;
    
    public CacheLRUWrapper() {
        this(PathsAndFiles.getRecentlyUsedFile(), PathsAndFiles.CACHE_DIR);
    }
    
        
    /**
     * testing constructor
     * @param recentlyUsed file to be used as recently_used file
     * @param cacheDir dir with cache
     */
    public CacheLRUWrapper(final InfrastructureFileDescriptor recentlyUsed, final InfrastructureFileDescriptor cacheDir) {
        recentlyUsedPropertiesFile = recentlyUsed;
        this.cacheDir = cacheDir;
        if (!recentlyUsed.getFile().exists()) {
            try {
                FileUtils.createParentDir(recentlyUsed.getFile());
                FileUtils.createRestrictedFile(recentlyUsed.getFile(), true);
            } catch (IOException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }
        }
    }
    
    /**
     * Returns an instance of the policy.
     * 
     * @return an instance of the policy
     */
    public static CacheLRUWrapper getInstance() {
        return  CacheLRUWrapperHolder.INSTANCE;
    }

    
    private PropertiesFile cachedRecentlyUsedPropertiesFile = null ;
    /**
     * @return the recentlyUsedPropertiesFile
     */
    synchronized PropertiesFile getRecentlyUsedPropertiesFile() {
        if (cachedRecentlyUsedPropertiesFile == null) {
            //no properties file yet, create it
            cachedRecentlyUsedPropertiesFile = new PropertiesFile(recentlyUsedPropertiesFile.getFile());
            return cachedRecentlyUsedPropertiesFile;
        } 
        if (recentlyUsedPropertiesFile.getFile().equals(cachedRecentlyUsedPropertiesFile.getStoreFile())){
            //The underlying InfrastructureFileDescriptor is still pointing to the same file, use current properties file
            return cachedRecentlyUsedPropertiesFile;
        } else {
            //the InfrastructureFileDescriptor was set to different location, move to it
            if (cachedRecentlyUsedPropertiesFile.tryLock()) {
                cachedRecentlyUsedPropertiesFile.store();
                cachedRecentlyUsedPropertiesFile.unlock();
            }
            cachedRecentlyUsedPropertiesFile = new PropertiesFile(recentlyUsedPropertiesFile.getFile());
            return cachedRecentlyUsedPropertiesFile;
        }
        
    }

    /**
     * @return the cacheDir
     */
    public InfrastructureFileDescriptor getCacheDir() {
        return cacheDir;
    }

    /**
     * @return the recentlyUsedFile
     */
    public InfrastructureFileDescriptor getRecentlyUsedFile() {
        return recentlyUsedPropertiesFile;
    }
    
   private static class CacheLRUWrapperHolder{
       private static final CacheLRUWrapper INSTANCE = new CacheLRUWrapper();
   }

    /**
     * Update map for keeping track of recently used items.
     */
    public synchronized void load() {
        boolean loaded = getRecentlyUsedPropertiesFile().load();
        /* 
         * clean up possibly corrupted entries
         */
        if (loaded && checkData()) {
            OutputController.getLogger().log(new LruCacheException());
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CFakeCache"));
            store();
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CFakedCache"));
        }
    }

    /**
     * check content of recentlyUsedPropertiesFile and remove invalid/corrupt entries
     *
     * @return true, if cache was corrupted and affected entry removed
     */
    private boolean checkData () {
        boolean modified = false;
        Set<Entry<Object, Object>> q = getRecentlyUsedPropertiesFile().entrySet();
        for (Iterator<Entry<Object, Object>> it = q.iterator(); it.hasNext();) {
            Entry<Object, Object> currentEntry = it.next();

            final String key = (String) currentEntry.getKey();
            final String path = (String) currentEntry.getValue();

            // 1. check key format: "milliseconds,number"
            try {
                String sa[] = key.split(",");
                Long l1 = Long.parseLong(sa[0]);
                Long l2 = Long.parseLong(sa[1]);
            } catch (Exception ex) {
                it.remove();
                modified = true;
                continue;
            }

            // 2. check path format - does the path look correct?
            if (path != null) {
                if (!path.contains(getCacheDir().getFullPath())) {
                    it.remove();
                    modified = true;
                }
            } else {
                it.remove();
                modified = true;
            }
        }
        
        return modified;
    }

    /**
     * Write file to disk.
     * @return true if properties were successfully stored, false otherwise
     */
    public synchronized boolean store() {
        if (getRecentlyUsedPropertiesFile().isHeldByCurrentThread()) {
            getRecentlyUsedPropertiesFile().store();
            return true;
        }
        return false;
    }

    /**
     * This adds a new entry to file.
     * 
     * @param key key we want path to be associated with.
     * @param path path to cache item.
     * @return true if we successfully added to map, false otherwise.
     */
    public synchronized boolean addEntry(String key, String path) {
        PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (props.containsKey(key)) {
            return false;
        }
        props.setProperty(key, path);
        return true;
    }

    /**
     * This removed an entry from our map.
     * 
     * @param key key we want to remove.
     * @return true if we successfully removed key from map, false otherwise.
     */
    public synchronized boolean removeEntry(String key) {
        PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (!props.containsKey(key)) {
            return false;
        }
        props.remove(key);
        return true;
    }

    private String getIdForCacheFolder(String folder) {
        int len = getCacheDir().getFullPath().length();
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
        PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (!props.containsKey(oldKey)) {
            return false;
        }
        String value = props.getProperty(oldKey);
        String folder = getIdForCacheFolder(value);

        props.remove(oldKey);
        props.setProperty(Long.toString(System.currentTimeMillis()) + "," + folder, value);
        return true;
    }

    /**
     * Return a copy of the keys available.
     * 
     * @return List of Strings sorted by ascending order.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    //although Properties are pretending to be <object,Object> they are always <String,String>
    //bug in jdk?
    public synchronized List<Entry<String, String>> getLRUSortedEntries() {
        List<Entry<String, String>> entries = new ArrayList<>();

        for (Entry e : getRecentlyUsedPropertiesFile().entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry(e));
        }

        // sort by keys in descending order.
        Collections.sort(entries, new Comparator<Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                Long t1 = Long.parseLong(e1.getKey().split(",")[0]);
                Long t2 = Long.parseLong(e2.getKey().split(",")[0]);

                int c = t1.compareTo(t2);
                return c < 0 ? 1 : (c > 0 ? -1 : 0);
            }
        });
        return entries;
    }

    /**
     * Lock the file to have exclusive access.
     */
    public synchronized void lock() {
        getRecentlyUsedPropertiesFile().lock();
    }

    /**
     * Unlock the file.
     */
    public synchronized void unlock() {
        getRecentlyUsedPropertiesFile().unlock();
    }

    /**
     * Return the value of given key.
     * 
     * @param key key of property
     * @return value of given key, null otherwise.
     */
    public synchronized String getValue(String key) {
        return getRecentlyUsedPropertiesFile().getProperty(key);
    }

    public synchronized boolean containsKey(String key) {
        return getRecentlyUsedPropertiesFile().containsKey(key);
    }

    public synchronized boolean containsValue(String value) {
        return getRecentlyUsedPropertiesFile().containsValue(value);
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

    void clearLRUSortedEntries() {
        getRecentlyUsedPropertiesFile().clear();
    }
}
