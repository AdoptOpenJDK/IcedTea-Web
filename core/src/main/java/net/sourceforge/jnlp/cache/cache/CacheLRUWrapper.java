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
package net.sourceforge.jnlp.cache.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionIdComparator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.WindowsShortcutManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class helps maintain the ordering of most recently use cache items across
 * multiple jvm instances. The LRU information is stored as a properties file in the
 * root of the file cache directory. The property key is a combination of a timestamp
 * and the cache folder id. The property value is the path to the cached item.
 */
class CacheLRUWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(CacheLRUWrapper.class);

    /**
     * Returns an instance of the policy.
     *
     * @return an instance of the policy
     */
    static CacheLRUWrapper getInstance() {
        return CacheLRUWrapperHolder.INSTANCE;
    }

    private final InfrastructureFileDescriptor recentlyUsedPropertiesFile;
    private final InfrastructureFileDescriptor cacheDir;

    private PropertiesFile cachedRecentlyUsedPropertiesFile = null;

    /**
     * @param recentlyUsed file to be used as recently_used file
     * @param cacheDir     dir with cache
     */
    CacheLRUWrapper(final InfrastructureFileDescriptor recentlyUsed, final InfrastructureFileDescriptor cacheDir) {
        recentlyUsedPropertiesFile = recentlyUsed;
        this.cacheDir = cacheDir;
        if (!recentlyUsed.getFile().exists()) {
            try {
                FileUtils.createParentDir(recentlyUsed.getFile());
                FileUtils.createRestrictedFile(recentlyUsed.getFile());
            } catch (IOException e) {
                LOG.error("Error in creating recently used cache items file.", e);
            }
        }
    }

    File replaceExistingCacheFile(URL source, VersionId version) {
        // Old entry will still exist. (but removed at cleanup)
        invalidate(source, version);
        return makeNewCacheFile(source, version);
    }

    ResourceInfo getInfo(URL location, VersionId versionId) {
        return new CacheEntry(location, versionId);
    }

    File getCacheFile(URL source, VersionId version) {

        // TODO: handle Version

        synchronized (this) {
            try {
                lock();

                // We need to reload the cacheOrder file each time
                // since another plugin/javaws instance may have updated it.
                load();
                File cacheFile = getCacheFileIfExist(CacheUtil.urlToPath(source, ""));
                if (cacheFile == null) { // We did not find a copy of it.
                    cacheFile = makeNewCacheFile(source, version);
                }
                store();
                return cacheFile;
            } finally {
                unlock();
            }
        }
    }

    private File makeNewCacheFile(URL source, VersionId version) {

        // TODO: handle Version

        synchronized (this) {
            try {
                lock();
                load();
                for (long i = 0; i < Long.MAX_VALUE; i++) {
                    String path = cacheDir.getFullPath() + File.separator + i;
                    File cDir = new File(path);
                    if (!cDir.exists()) {
                        // We can use this directory.
                        File cacheFile = createCacheFile(source, path);
                        store();
                        return cacheFile;
                    }
                }

            } finally {
                unlock();
            }
            return null;
        }
    }

    private File createCacheFile(URL source, String path) {
        final File cacheFile = CacheUtil.urlToPath(source, path);
        try {
            FileUtils.createParentDir(cacheFile);
            createInfoFile(cacheFile);
            addEntry(generateKey(cacheFile.getPath()), cacheFile.getPath());
        } catch (IOException ioe) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
        }
        return cacheFile;
    }

    private void createInfoFile(File cacheFile) throws IOException {
        final File infoFile = new File(cacheFile.getPath() + CacheEntry.INFO_SUFFIX);
        FileUtils.createRestrictedFile(infoFile); // Create the info file for marking later.

        final String jnlpPath = JNLPRuntime.getJnlpPath(); //get jnlp from args passed
        if (StringUtils.isBlank(jnlpPath)) {
            LOG.info("Not-setting jnlp-path for missing main/jnlp argument");
        } else {
            FileUtils.saveFileUtf8(CacheEntry.KEY_JNLP_PATH + "=" + jnlpPath, infoFile);
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
            return getLRUSortedEntries().stream()
                    .filter(e -> pathToURLPath(e.getValue()).equals(urlPath.getPath()))
                    .findFirst()
                    .map(e -> {
                        markEntryAsAccessed(e.getKey());
                        return new File(e.getValue());
                    })
                    .orElse(null);
        }
    }

    /**
     * Get the path to file minus the cache directory and indexed folder.
     */
    private String pathToURLPath(String path) {
        final int len = cacheDir.getFullPath().length();
        final int index = path.indexOf(File.separatorChar, len + 1);
        return path.substring(index);
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache.
     *
     * @param source  the source {@link URL}
     * @param version the versions to check for
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    boolean isCached(URL source, VersionId version) {
        final CacheEntry entry = new CacheEntry(source, version);

        boolean isCurrent = entry.isCached();
        LOG.info("isCached: {} = {}", source, isCurrent);
        return isCurrent;
    }

    VersionId getBestMatchingVersionInCache(final URL resource, final VersionString version) {
        final VersionIdComparator versionComparator = new VersionIdComparator(version);
        return getAllMatchingVersionInCache(resource, version).stream()
                .max(versionComparator)
                .orElse(null);
    }

    private Set<VersionId> getAllMatchingVersionInCache(final URL resource, final VersionString version) {
        // TODO: handle Version
        throw new RuntimeException("not implemented");
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param source       the source {@link URL}
     * @param version      the versions to check for
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    boolean isUpToDate(URL source, VersionId version, long lastModified) {
        final CacheEntry entry = new CacheEntry(source, version);

        boolean isCurrent = entry.isCurrent(lastModified);
        LOG.info("isUpToDate: {} = {}", source, isCurrent);
        return isCurrent;
    }

    private void invalidate(URL source, VersionId version) {
        final CacheEntry entry = new CacheEntry(source, version);
        entry.markForDelete();
    }

    List<CacheId> getCacheIds(String filter, boolean includeJnlpPath, boolean includeDomain) {
        synchronized (this) {
            lock();
            try {
                final Set<CacheId> r = new LinkedHashSet<>();
                Files.walk(Paths.get(cacheDir.getFile().getCanonicalPath()))
                        .filter(t -> Files.isRegularFile(t))
                        .filter(path -> path.getFileName().toString().endsWith(CacheEntry.INFO_SUFFIX))
                        .forEach(path -> {
                            if (includeJnlpPath) {
                                final File infoFile = new File(path.toString());
                                final String jnlpPath = new PropertiesFile(infoFile).getProperty(CacheEntry.KEY_JNLP_PATH);
                                if (jnlpPath != null && jnlpPath.matches(filter)) {
                                    r.add(new CacheId.CacheJnlpId(jnlpPath));
                                }
                            }
                            if (includeDomain) {
                                final String domain = getDomain(path);
                                if (domain != null && domain.matches(filter)) {
                                    r.add(new CacheId.CacheDomainId(domain));
                                }
                            }
                        });

                return r.stream()
                        .peek(CacheId::populate)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
    }

    void addToCache(ResourceInfo info, InputStream inputStream) throws IOException {
        final CacheEntry entry = new CacheEntry(info.getLocation(), info.getVersion());

        final File outputFile = getCacheFile(info.getLocation(), info.getVersion());
        LOG.debug("Downloading file: {} into: {}", info.getLocation(), outputFile.getCanonicalPath());
        try (final OutputStream out = new FileOutputStream(outputFile)) {
            IOUtils.copy(inputStream, out);
        }

        entry.storeInfo(info);
    }

    void deleteFromCache(URL location, VersionString version) {
        for (final VersionId versionId : getAllMatchingVersionInCache(location, version)) {
            final File cachedFile = getCacheFile(location, versionId);
            final String directoryUrl = getCacheParentDirectory(cachedFile.getAbsolutePath());


            LOG.info("Deleting cached file: {}", cachedFile.getAbsolutePath());
            cachedFile.delete();

            final File directory = new File(directoryUrl);
            try {
                LOG.info("Deleting cached directory: {}", directory.getAbsolutePath());
                FileUtils.recursiveDelete(directory, directory);
            } catch (IOException e) {
                LOG.error("Failed to delete '{}'. continue..." + directory.getAbsolutePath());
            }
        }
    }

    /**
     * Returns the parent directory of the cached resource.
     *
     * @param path The path of the cached resource directory.
     * @return parent dir of cache
     */
    private String getCacheParentDirectory(String path) {
        final String cacheDirPath = cacheDir.getFullPath();

        while (path.startsWith(cacheDirPath) && !path.equals(cacheDirPath)) {
            final String parentPath = new File(path).getParent();
            if (parentPath.equals(cacheDirPath)) {
                break;
            }

            path = parentPath;
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
                                    pf.setProperty(CacheEntry.KEY_DELETE, Boolean.toString(true));
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
            final Set<String> keep = new HashSet<>();
            final Set<String> remove = new HashSet<>();
            synchronized (this) {
                try {
                    lock();
                    load();

                    final long maxSize = getMaxSizeInBytes();
                    long curSize = 0;

                    for (final Entry<String, String> e : getLRUSortedEntries()) {
                        // Check if the item is contained in cacheOrder.
                        final String key = e.getKey();
                        final String path = e.getValue();

                        final File file = new File(path);
                        final PropertiesFile pf = new PropertiesFile(new File(path + CacheEntry.INFO_SUFFIX));
                        boolean delete = Boolean.parseBoolean(pf.getProperty(CacheEntry.KEY_DELETE));

                        /*
                         * This will get me the root directory specific to this cache item.
                         * Example:
                         *  cacheDir = /home/user1/.icedtea/cache
                         *  file.getPath() = /home/user1/.icedtea/cache/0/http/www.example.com/subdir/a.jar
                         *  relativePath = /0/http/www.example.com/subdir/a.jar
                         *  cacheDirOfFile = /home/user1/.icedtea/cache/0
                         */
                        final String fullPath = cacheDir.getFullPath();
                        final String relativePath = file.getPath().substring(fullPath.length());
                        final String cacheDirOfFile = fullPath + relativePath.substring(0, relativePath.indexOf(File.separatorChar, 1));

                        if (keep.contains(file.getPath().substring(cacheDirOfFile.length()))) {
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
                        final long len = file.length();
                        if (delete || !file.isFile() || (maxSize >= 0 && curSize + len > maxSize)) {
                            removeEntry(key);
                            remove.add(cacheDirOfFile);
                            continue;
                        }

                        curSize += len;
                        keep.add(file.getPath().substring(cacheDirOfFile.length()));

                        final File[] siblings = file.getParentFile().listFiles();
                        if (siblings != null) {
                            for (File f : siblings) {
                                if (!(f.equals(file) || f.equals(pf.getStoreFile()))) {
                                    try {
                                        FileUtils.recursiveDelete(f, f);
                                    } catch (IOException e1) {
                                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e1);
                                    }
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

    @SuppressWarnings("ConstantConditions")
    private long getMaxSizeInBytes() {
        try {
            final String maxSizePropertyValue = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_CACHE_MAX_SIZE);
            final long maxSizeInMegaBytes = Long.parseLong(maxSizePropertyValue);
            return maxSizeInMegaBytes << 20; // Convert from megabyte to byte (Negative values will be considered unlimited.)
        } catch (NumberFormatException ignored) {
            return -1;
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
        if (recentlyUsedPropertiesFile.getFile().equals(cachedRecentlyUsedPropertiesFile.getStoreFile())) {
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
     * Update map for keeping track of recently used items.
     */
    synchronized void load() {
        final boolean loaded = getRecentlyUsedPropertiesFile().load();
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
    private boolean checkData() {
        boolean modified = false;
        final Set<Entry<Object, Object>> q = getRecentlyUsedPropertiesFile().entrySet();
        for (Iterator<Entry<Object, Object>> it = q.iterator(); it.hasNext(); ) {
            final Entry<Object, Object> currentEntry = it.next();

            final String key = (String) currentEntry.getKey();
            final String path = (String) currentEntry.getValue();

            // 1. check key format: "milliseconds,number"
            try {
                final String[] sa = key.split(",");
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
     *
     * @return true if properties were successfully stored, false otherwise
     */
    synchronized boolean store() {
        final PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (props.isHeldByCurrentThread()) {
            props.store();
            return true;
        }
        return false;
    }

    /**
     * This adds a new entry to file.
     *
     * @param key  key we want path to be associated with.
     * @param path path to cache item.
     */
    synchronized void addEntry(String key, String path) {
        final PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (!props.containsKey(key)) {
            props.setProperty(key, path);
        }
    }

    /**
     * This removed an entry from our map.
     *
     * @param key key we want to remove.
     */
    synchronized void removeEntry(String key) {
        final PropertiesFile props = getRecentlyUsedPropertiesFile();
        props.remove(key);
    }

    private String getIdForCacheFolder(String folder) {
        final int len = cacheDir.getFullPath().length();
        final int index = folder.indexOf(File.separatorChar, len + 1);
        return folder.substring(len + 1, index);
    }

    /**
     * This updates the given key to reflect it was recently accessed.
     *
     * @param oldKey Key we wish to update.
     */
    private synchronized void markEntryAsAccessed(String oldKey) {
        final PropertiesFile props = getRecentlyUsedPropertiesFile();
        if (props.containsKey(oldKey)) {
            final String value = props.getProperty(oldKey);

            props.remove(oldKey);
            props.setProperty(generateKey(value), value);
        }
    }

    /**
     * Return a copy of the keys available.
     *
     * @return List of Strings sorted by ascending order.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    //although Properties are pretending to be <object,Object> they are always <String,String>
    //bug in jdk?
    private synchronized List<Entry<String, String>> getLRUSortedEntries() {
        final List<Entry<String, String>> entries = new ArrayList<>();

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
    synchronized void lock() {
        getRecentlyUsedPropertiesFile().lock();
    }

    /**
     * Unlock the file.
     */
    synchronized void unlock() {
        getRecentlyUsedPropertiesFile().unlock();
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
        final File otherJavawsRunning = PathsAndFiles.MAIN_LOCK.getFile();
        FileLock locking = null;
        try {
            if (otherJavawsRunning.isFile()) {
                final FileOutputStream fis = new FileOutputStream(otherJavawsRunning);

                final FileChannel channel = fis.getChannel();
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


    /* **********
     * only used in tests
     * **********/

    synchronized boolean containsKey(String key) {
        return getRecentlyUsedPropertiesFile().containsKey(key);
    }

    synchronized boolean containsValue(String value) {
        return getRecentlyUsedPropertiesFile().containsValue(value);
    }

    InfrastructureFileDescriptor getRecentlyUsedFile() {
        return recentlyUsedPropertiesFile;
    }


    private static class CacheLRUWrapperHolder {
        private static final CacheLRUWrapper INSTANCE = new CacheLRUWrapper(PathsAndFiles.getRecentlyUsedFile(), PathsAndFiles.CACHE_DIR);
    }
}
