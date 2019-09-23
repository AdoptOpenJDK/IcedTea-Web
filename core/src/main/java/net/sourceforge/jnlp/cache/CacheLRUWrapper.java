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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.WindowsShortcutManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class helps maintain the ordering of most recently use cache items across
 * multiple jvm instances. The LRU information is stored as a properties file in the
 * root of the file cache directory. The property key is a combination of a timestamp
 * and the cache folder id. The property value is the path to the cached item.
 */
class CacheLRUWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(CacheLRUWrapper.class);

    /*
     * back-end of how LRU is implemented This file is to keep track of the most
     * recently used items. The items are to be kept with key = (current time
     * accessed) followed by folder of item. value = path to file.
     */

    private final InfrastructureFileDescriptor recentlyUsedPropertiesFile;
    private final InfrastructureFileDescriptor cacheDir;

    private CacheLRUWrapper() {
        this(PathsAndFiles.getRecentlyUsedFile(), PathsAndFiles.CACHE_DIR);
    }


    /**
     * testing constructor
     * @param recentlyUsed file to be used as recently_used file
     * @param cacheDir dir with cache
     */
    CacheLRUWrapper(final InfrastructureFileDescriptor recentlyUsed, final InfrastructureFileDescriptor cacheDir) {
        recentlyUsedPropertiesFile = recentlyUsed;
        this.cacheDir = cacheDir;
        if (!recentlyUsed.getFile().exists()) {
            try {
                FileUtils.createParentDir(recentlyUsed.getFile());
                FileUtils.createRestrictedFile(recentlyUsed.getFile(), true);
            } catch (IOException e) {
                LOG.error("Error in creating recently used cache items file.", e);
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

    File getCacheFile(URL source, VersionId version) {
        File cacheFile = null;
        synchronized (this) {
            try {
                lock();

                // We need to reload the cacheOrder file each time
                // since another plugin/javaws instance may have updated it.
                load();
                cacheFile = getCacheFileIfExist(CacheUtil.urlToPath(source, ""));
                if (cacheFile == null) { // We did not find a copy of it.
                    cacheFile = makeNewCacheFile(source, version);
                } else
                    store();
            } finally {
                unlock();
            }
        }
        return cacheFile;
    }

    File makeNewCacheFile(URL source, VersionId version) {

        // TODO: handle Version

        synchronized (this) {
            File cacheFile = null;
            try {
                lock();
                load();
                for (long i = 0; i < Long.MAX_VALUE; i++) {
                    String path = cacheDir.getFullPath() + File.separator + i;
                    File cDir = new File(path);
                    if (!cDir.exists()) {
                        // We can use this directory.
                        try {
                            cacheFile = CacheUtil.urlToPath(source, path);
                            FileUtils.createParentDir(cacheFile);
                            File pf = new File(cacheFile.getPath() + CacheEntry.INFO_SUFFIX);
                            FileUtils.createRestrictedFile(pf, true); // Create the info file for marking later.
                            addEntry(generateKey(cacheFile.getPath()), cacheFile.getPath());
                        } catch (IOException ioe) {
                            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
                        }

                        break;
                    }
                }

                store();
            } finally {
                unlock();
            }
            return cacheFile;
        }
    }

    /**
     * This will return a File pointing to the location of cache item.
     *
     * @param urlPath Path of cache item within cache directory.
     * @return File if we have searched before, {@code null} otherwise.
     */
    private File getCacheFileIfExist(File urlPath) {
        synchronized (this) {
            File cacheFile = null;
            List<Entry<String, String>> entries = getLRUSortedEntries();
            // Start searching from the most recent to least recent.
            for (Entry<String, String> e : entries) {
                final String key = e.getKey();
                final String path = e.getValue();

                if (pathToURLPath(path).equals(urlPath.getPath())) { // Match found.
                    cacheFile = new File(path);
                    updateEntry(key);
                    break; // Stop searching since we got newest one already.
                }
            }
            return cacheFile;
        }
    }

    /**
     * Get the path to file minus the cache directory and indexed folder.
     */
    private String pathToURLPath(String path) {
        int len = cacheDir.getFullPath().length();
        int index = path.indexOf(File.separatorChar, len + 1);
        return path.substring(index);
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param source      the source {@link URL}
     * @param version     the versions to check for
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    boolean isUpToDate(URL source, VersionId version, long lastModified) {
        final CacheEntry entry = new CacheEntry(source, version);
        final boolean isCached = entry.isCached();
        if (! isCached) {
            LOG.info("isUpToDate: {} = false", source);
            return false;
        }

        boolean isCurrent = entry.isCurrent(lastModified);
        LOG.info("isUpToDate: {} = {}", source, isCurrent);
        return isCurrent;
    }

    List<CacheId> getCacheIds(String filter, boolean jnlpPath, boolean domain) {
        synchronized (this) {
            lock();
            final List<CacheId> r = new ArrayList<>();
            try {
                Files.walk(Paths.get(cacheDir.getFile().getCanonicalPath()))
                        .filter(t -> Files.isRegularFile(t))
                        .forEach(path -> {
                            if (path.getFileName().toString().endsWith(CacheEntry.INFO_SUFFIX)) {
                                final PropertiesFile pf = new PropertiesFile(new File(path.toString()));
                                if (jnlpPath) {
                                    // if jnlp-path in .info equals path of app to delete mark to delete
                                    String jnlpPath1 = pf.getProperty(CacheEntry.KEY_JNLP_PATH);
                                    if (jnlpPath1 != null && jnlpPath1.matches(filter)) {
                                        CacheId jnlpPathId = new CacheId.CacheJnlpId(jnlpPath1);
                                        if (!r.contains(jnlpPathId)) {
                                            r.add(jnlpPathId);
                                            jnlpPathId.populate();

                                        }
                                    }
                                }
                                if (domain) {
                                    String domain1 = getDomain(path);
                                    if (domain1.matches(filter)) {
                                        CacheId domainId = new CacheId.CacheDomainId(domain1);
                                        if (!r.contains(domainId)) {
                                            r.add(domainId);
                                            domainId.populate();

                                        }
                                    }
                                }
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
            return r;
        }
    }

    void deleteFromCache(URL location, VersionString version) {
        for (VersionId versionId : CacheUtil.getAllMatchingVersionInCache(location, version)) {
            File cachedFile = CacheUtil.getCacheFile(location, versionId);
            String directoryUrl = getCacheParentDirectory(cachedFile.getAbsolutePath());

            File directory = new File(directoryUrl);

            LOG.info("Deleting cached file: {}", cachedFile.getAbsolutePath());

            cachedFile.delete();

            LOG.info("Deleting cached directory: {}", directory.getAbsolutePath());

            directory.delete();
        }
    }

    /**
     * Returns the parent directory of the cached resource.
     *
     * @param path The path of the cached resource directory.
     * @return parent dir of cache
     */
    private String getCacheParentDirectory(String path) {
        String tempPath;
        String cacheDir = CacheLRUWrapper.getInstance().getCacheDir().getFullPath();

        while (path.startsWith(cacheDir) && !path.equals(cacheDir)) {
            tempPath = new File(path).getParent();

            if (tempPath.equals(cacheDir))
                break;

            path = tempPath;
        }
        return path;
    }

    boolean deleteFromCache(String cacheId) {
        // clear one app
        if (cannotClearCache()) {
            return false;
        }

        synchronized (this) {
            lock();
            try {
                Files.walk(Paths.get(cacheDir.getFile().getCanonicalPath()))
                        .filter(t -> Files.isRegularFile(t))
                        .forEach(path -> {
                            if (path.getFileName().toString().endsWith(CacheEntry.INFO_SUFFIX)) {
                                PropertiesFile pf = new PropertiesFile(new File(path.toString()));
                                // if jnlp-path in .info equals path of app to delete mark to delete
                                final String jnlpPath = pf.getProperty(CacheEntry.KEY_JNLP_PATH);
                                final String domain = getDomain(path);
                                if (cacheId.equalsIgnoreCase(jnlpPath) || cacheId.equalsIgnoreCase(domain)) {
                                    pf.setProperty("delete", "true");
                                    pf.store();
                                    LOG.info("marked for deletion: {}", path);
                                }
                            }
                        });
                if (OsUtil.isWindows()) {
                    WindowsShortcutManager.removeWindowsShortcuts(cacheId.toLowerCase());
                }
                // clean the cache of entries now marked for deletion
                cleanCache();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
        return true;
    }

    /**
     * Clears the cache by deleting all the Netx cache files
     * <p>
     * Note: Because of how our caching system works, deleting jars of another javaws
     * process is using them can be quite disastrous. Hence why Launcher creates lock files
     * and we check for those by calling {@link #okToClearCache()}
     *
     * @return true if the cache could be cleared and was cleared
     */
    boolean clearCache() {
        if (cannotClearCache()) {
            return false;
        }

        final String fullPath = cacheDir.getFullPath();
        LOG.debug("Clearing cache directory: {}", fullPath);
        synchronized (this) {
            lock();
            try {
                final File cacheDirFile = cacheDir.getFile().getCanonicalFile();
                // remove windows shortcuts before cache dir is gone
                if (OsUtil.isWindows()) {
                    WindowsShortcutManager.removeWindowsShortcuts("ALL");
                }
                removeDirectories(Collections.singleton(fullPath));
                cacheDirFile.mkdir();
                clearLRUSortedEntries();
                store();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
        return true;
    }
    /**
     * This will remove all old cache items.
     */
    void cleanCache() {
        if (okToClearCache()) {
            // First we want to figure out which stuff we need to delete.
            HashSet<String> keep = new HashSet<>();
            HashSet<String> remove = new HashSet<>();
            synchronized (this) {
                try {
                    lock();
                    load();

                    long maxSize = -1; // Default
                    try {
                        final String maxSizePropertyValue = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_CACHE_MAX_SIZE);
                        maxSize = Long.parseLong(maxSizePropertyValue);
                    } catch (NumberFormatException nfe) {
                        // ignore, maxSize will stay at -1
                    }

                    maxSize = maxSize << 20; // Convert from megabyte to byte (Negative values will be considered unlimited.)
                    long curSize = 0;

                    for (Entry<String, String> e : getLRUSortedEntries()) {
                        // Check if the item is contained in cacheOrder.
                        final String key = e.getKey();
                        final String path = e.getValue();

                        File file = new File(path);
                        PropertiesFile pf = new PropertiesFile(new File(path + CacheEntry.INFO_SUFFIX));
                        boolean delete = Boolean.parseBoolean(pf.getProperty("delete"));

                        /*
                         * This will get me the root directory specific to this cache item.
                         * Example:
                         *  cacheDir = /home/user1/.icedtea/cache
                         *  file.getPath() = /home/user1/.icedtea/cache/0/http/www.example.com/subdir/a.jar
                         *  rStr first becomes: /0/http/www.example.com/subdir/a.jar
                         *  then rstr becomes: /home/user1/.icedtea/cache/0
                         */
                        final String fullPath = cacheDir.getFullPath();
                        String rStr = file.getPath().substring(fullPath.length());
                        rStr = fullPath + rStr.substring(0, rStr.indexOf(File.separatorChar, 1));
                        long len = file.length();

                        if (keep.contains(file.getPath().substring(rStr.length()))) {
                            removeEntry(key);
                            continue;
                        }

                        /*
                         * we remove entries from our lru if any of the following condition is met.
                         * Conditions:
                         *  - delete: file has been marked for deletion.
                         *  - !file.isFile(): if someone tampered with the directory, file doesn't exist.
                         *  - maxSize >= 0 && curSize + len > maxSize: If a limit was set and the new size
                         *  on disk would exceed the maximum size.
                         */
                        if (delete || !file.isFile() || (maxSize >= 0 && curSize + len > maxSize)) {
                            removeEntry(key);
                            remove.add(rStr);
                            continue;
                        }

                        curSize += len;
                        keep.add(file.getPath().substring(rStr.length()));

                        for (File f : file.getParentFile().listFiles()) {
                            if (!(f.equals(file) || f.equals(pf.getStoreFile()))) {
                                try {
                                    FileUtils.recursiveDelete(f, f);
                                } catch (IOException e1) {
                                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e1);
                                }
                            }

                        }
                    }
                    store();
                } finally {
                    unlock();
                }
                removeDirectories(remove);
            }
        }
    }

    private void removeDirectories(Set<String> remove) {
        for (String s : remove) {
            File f = new File(s);
            try {
                FileUtils.recursiveDelete(f, f);
            } catch (IOException ignored) {
            }
        }
    }

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
    InfrastructureFileDescriptor getCacheDir() {
        return cacheDir;
    }

    /**
     * @return the recentlyUsedFile
     */
    InfrastructureFileDescriptor getRecentlyUsedFile() {
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
            LOG.warn("Cache is corrupt. Fixing...");
            store();
            LOG.warn("Cache was corrupt and has been fixed. It is strongly recommended that you run ''javaws -Xclearcache'' and rerun your application as soon as possible. You can also use via itw-settings Cache -> View files -> Purge");
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
                String[] sa = key.split(",");
                Long.parseLong(sa[0]);
                Long.parseLong(sa[1]);
            } catch (Exception ex) {
                it.remove();
                modified = true;
                continue;
            }

            // 2. check path format - does the path look correct?
            if (path != null) {
                if (!path.contains(cacheDir.getFullPath())) {
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
    synchronized boolean addEntry(String key, String path) {
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
    synchronized boolean removeEntry(String key) {
        PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (!props.containsKey(key)) {
            return false;
        }
        props.remove(key);
        return true;
    }

    private String getIdForCacheFolder(String folder) {
        int len = cacheDir.getFullPath().length();
        int index = folder.indexOf(File.separatorChar, len + 1);
        return folder.substring(len + 1, index);
    }

    /**
     * This updates the given key to reflect it was recently accessed.
     *
     * @param oldKey Key we wish to update.
     * @return true if we successfully updated value, false otherwise.
     */
    synchronized boolean updateEntry(String oldKey) {
        PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (!props.containsKey(oldKey)) {
            return false;
        }
        String value = props.getProperty(oldKey);

        props.remove(oldKey);
        props.setProperty(generateKey(value), value);
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
    synchronized List<Entry<String, String>> getLRUSortedEntries() {
        List<Entry<String, String>> entries = new ArrayList<>();

        for (Entry e : getRecentlyUsedPropertiesFile().entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry(e));
        }

        // sort by keys in descending order.
        entries.sort(Comparator.comparingLong(e -> Long.parseLong(e.getKey().split(",")[0])));
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

    public synchronized boolean containsKey(String key) {
        return getRecentlyUsedPropertiesFile().containsKey(key);
    }

    synchronized boolean containsValue(String value) {
        return getRecentlyUsedPropertiesFile().containsValue(value);
    }

    /**
     * Generate a key given the path to file. May or may not generate the same
     * key given same path.
     *
     * @param path Path to generate a key with.
     * @return String representing the a key.
     */
    String generateKey(String path) {
        return System.currentTimeMillis() + "," + getIdForCacheFolder(path);
    }

    void clearLRUSortedEntries() {
        getRecentlyUsedPropertiesFile().clear();
    }

    private String getDomain(Path path) {
        final String relativeToCache = path.relativize(Paths.get(cacheDir.getFullPath())).toString();
        final String[] parts = relativeToCache.split(Pattern.quote(File.separator));
        return parts[3];
    }

    private boolean cannotClearCache() {
        if (okToClearCache()) {
            final File cacheRoot = cacheDir.getFile();
            return !cacheRoot.isDirectory();
        }

        LOG.error("Cannot clear the cache at this time. Try later. If the problem persists, try closing your browser(s) & JNLP applications. At the end you can try to kill all java applications. \\\\\\n You can clear cache by javaws -Xclearcache or via itw-settings Cache -> View files -> Purge");
        return true;
    }

    /**
     * Returns a boolean indicating if it ok to clear the netx application cache at this point
     *
     * @return true if the cache can be cleared at this time without problems
     */
    private boolean okToClearCache() {
        File otherJavawsRunning = PathsAndFiles.MAIN_LOCK.getFile();
        FileLock locking = null;
        try {
            if (otherJavawsRunning.isFile()) {
                FileOutputStream fis = new FileOutputStream(otherJavawsRunning);

                FileChannel channel = fis.getChannel();
                locking = channel.tryLock();
                if (locking == null) {
                    LOG.info("Other instances of javaws are running");
                    return false;
                }
                LOG.info("No other instances of javaws are running");
                return true;

            } else {
                LOG.info("No instance file found");
                return true;
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (locking != null) {
                try {
                    locking.release();
                } catch (IOException ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                }
            }
        }
    }

}
