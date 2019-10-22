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
package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.WindowsShortcutManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.CollectionUtils.isNullOrEmpty;

/**
 * This class helps maintain the ordering of most recently use cache items across
 * multiple jvm instances. The LRU information is stored as a properties file in the
 * root of the file cache directory.
 */
class LeastRecentlyUsedCache {

    private static final Logger LOG = LoggerFactory.getLogger(LeastRecentlyUsedCache.class);

    static LeastRecentlyUsedCache getInstance() {
        return CacheHolder.INSTANCE;
    }

    private final LeastRecentlyUsedCacheIndexHolder cacheIndex;
    private final InfrastructureFileDescriptor rootCacheDir;

    /**
     * @param recentlyUsed file to be used as recently_used file
     * @param cacheDir     dir with cache
     */
    private LeastRecentlyUsedCache(final InfrastructureFileDescriptor recentlyUsed, final InfrastructureFileDescriptor cacheDir) {
        this.cacheIndex = new LeastRecentlyUsedCacheIndexHolder(recentlyUsed);
        this.rootCacheDir = cacheDir;
    }

    File getOrCreateCacheFile(URL resourceHref, VersionId version) {
        final LeastRecentlyUsedCacheEntry entry = cacheIndex.getSynchronized(idx ->
                getOrCreateCacheEntry(idx, resourceHref, version)
        );
        return getCacheFile(entry);
    }

    private LeastRecentlyUsedCacheEntry getOrCreateCacheEntry(LeastRecentlyUsedCacheIndex idx, URL resourceHref, VersionId version) {
        return idx.findAndMarkAsAccessed(resourceHref, version)
                .orElseGet(() -> createNewInfoFileAndIndexEntry(idx, resourceHref, version));
    }

    File replaceExistingCacheFile(URL resourceHref, VersionId version) {
        final LeastRecentlyUsedCacheEntry entry = cacheIndex.getSynchronized(idx -> {
            // Old entry will still exist. (but removed at cleanup)
            idx.markEntryForDeletion(resourceHref, version);
            return createNewInfoFileAndIndexEntry(idx, resourceHref, version);
        });
        return getCacheFile(entry);
    }

    private LeastRecentlyUsedCacheEntry createNewInfoFileAndIndexEntry(LeastRecentlyUsedCacheIndex idx, URL resourceHref, VersionId version) {
        final File dir = makeNewCacheDir();
        final String entryId = entryIdFromCacheDir(dir);
        createInfoFile(dir);
        return idx.createEntry(resourceHref, version, entryId);
    }

    private File makeNewCacheDir() {
        final String cacheDirPath = rootCacheDir.getFullPath();
        for (int i = 0; i < 250; i++) {
            for (int j = 0; j < 250; j++) {
                String path = cacheDirPath + File.separator + i + File.separator + j;
                File cDir = new File(path);
                if (!cDir.exists()) {
                    if (cDir.mkdirs()) {
                        return cDir;
                    }
                    throw new RuntimeException("Can't create directory " + cDir);
                }
            }
        }
        throw new RuntimeException("Out of directories :-)");
    }

    private void createInfoFile(File dir) {
        try {
            final File infoFile = new File(dir, CacheEntry.INFO_SUFFIX);
            FileUtils.createRestrictedFile(infoFile); // Create the info file for marking later.

            final String jnlpPath = JNLPRuntime.getJnlpPath(); //get jnlp from args passed
            if (StringUtils.isBlank(jnlpPath)) {
                LOG.info("Not-setting jnlp-path for missing main/jnlp argument");
            } else {
                FileUtils.saveFileUtf8(CacheEntry.KEY_JNLP_PATH + "=" + jnlpPath, infoFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create info file in dir " + dir, e);
        }
    }

    void addToCache(ResourceInfo info, InputStream inputStream) throws IOException {
        final List<IOException> ex = new ArrayList<>();

        final LeastRecentlyUsedCacheEntry entry = cacheIndex.getSynchronized(idx ->
                getOrCreateCacheEntry(idx, info.getResourceHref(), info.getVersion())
        );

        final CacheEntry infoFile = getInfoFile(entry);
        final File cacheFile = infoFile.getCacheFile();
        try {
            LOG.debug("Downloading file: {} into: {}", info.getResourceHref(), cacheFile.getCanonicalPath());
            try (final OutputStream out = new FileOutputStream(cacheFile)) {
                IOUtils.copy(inputStream, out);
            }
            infoFile.storeInfo(info);
        } catch (IOException e) {
            ex.add(e);
        }

        if (!ex.isEmpty()) {
            throw ex.get(0);
        }
    }

    Optional<ResourceInfo> getResourceInfo(URL resourceHref, VersionId version) {
        return cacheIndex.getSynchronized(idx -> idx.findAndMarkAsAccessed(resourceHref, version))
                .map(this::getInfoFile);
    }

    /**
     * Returns whether there is a version of the URL contents in the cache.
     *
     * @param resourceHref the resourceHref {@link URL}
     * @param version      the versions to check for
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resourceHref is not cacheable
     */
    boolean isCached(URL resourceHref, VersionId version) {
        final Boolean isCached = cacheIndex.getSynchronized(idx -> idx.findAndMarkAsAccessed(resourceHref, version))
                .map(e -> getInfoFile(e).isCached())
                .orElse(false);
        LOG.info("isCached: {} - (v: {}) = {}", resourceHref, version, isCached);
        return isCached;
    }

    /**
     * Returns whether there is a version of the URL contents in the cache and it is up to date.
     *
     * @param resourceHref the resourceHref {@link URL}
     * @param version      the versions to check for
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resourceHref is not cacheable
     */
    boolean isUpToDate(URL resourceHref, VersionId version, long lastModified) {
        final Boolean isUpToDate = cacheIndex.getSynchronized(idx -> idx.findAndMarkAsAccessed(resourceHref, version))
                .map(e -> getInfoFile(e).isCurrent(lastModified))
                .orElse(false);
        LOG.info("isUpToDate: {} - (v: {}) = {}", resourceHref, version, isUpToDate);
        return isUpToDate;
    }

    Optional<LeastRecentlyUsedCacheEntry> getBestMatchingEntryInCache(final URL resourceHref, final VersionString version) {
        return cacheIndex.getSynchronized(idx -> idx.findBestAndMarkAsAccessed(resourceHref, version));
    }

    List<CacheId> getCacheIds(String filter, boolean includeJnlpPath, boolean includeDomain) {
        if (!includeJnlpPath && !includeDomain) {
            return Collections.emptyList();
        }

        final List<LeastRecentlyUsedCacheEntry> entries = cacheIndex.getSynchronized(LeastRecentlyUsedCacheIndex::getAllUnDeletedEntries);

        final Map<String, CacheId> result = new LinkedHashMap<>();
        entries.forEach(entry -> {
            final CacheFile fileEntry = createPaneObjectArray(entry);
            if (includeJnlpPath) {
                final CacheEntry infoFile = getInfoFile(entry);
                final String jnlpPath = infoFile.getJnlpPath();
                if (jnlpPath != null && jnlpPath.matches(filter)) {
                    final CacheId cacheId = result.computeIfAbsent(jnlpPath, CacheId::jnlpPathId);
                    cacheId.getFiles().add(fileEntry);
                }
            }
            if (includeDomain) {
                final String domain = entry.getDomain();
                if (domain != null && domain.matches(filter)) {
                    final CacheId cacheId = result.computeIfAbsent(domain, CacheId::domainId);
                    cacheId.getFiles().add(fileEntry);
                }
            }
        });

        return new ArrayList<>(result.values());
    }

    private CacheFile createPaneObjectArray(LeastRecentlyUsedCacheEntry entry) {
        final CacheEntry infoFile = getInfoFile(entry);
        return new CacheFile(infoFile, entry);
    }

    void deleteFromCache(URL resourceHref, VersionId version) {
        cacheIndex.runSynchronized(idx -> idx
                .find(resourceHref, version)
                .ifPresent(entry -> deleteFromCache(idx, entry)));
    }

    void deleteFromCache(URL resourceHref, VersionString version) {
        cacheIndex.runSynchronized(idx -> idx
                .findAll(resourceHref, version)
                .forEach(entry -> deleteFromCache(idx, entry)));
    }

    void deleteFromCache(String cacheId) {
        cacheIndex.runSynchronized(idx -> {
            final List<LeastRecentlyUsedCacheEntry> allEntries = idx.getAllUnDeletedEntries();
            allEntries.stream()
                    .filter(entry -> cacheId.equals(entry.getDomain()) || cacheId.equals(getInfoFile(entry).getJnlpPath()))
                    .forEach(entry -> deleteFromCache(idx, entry));
        });
        if (OsUtil.isWindows()) {
            WindowsShortcutManager.removeWindowsShortcuts(cacheId.toLowerCase());
        }
    }

    private void deleteFromCache(LeastRecentlyUsedCacheIndex idx, LeastRecentlyUsedCacheEntry entry) {
        final File cacheFile = getCacheFile(entry);
        final File directory = cacheFile.getParentFile();

        try {
            LOG.info("Deleting cache file: {}", cacheFile.getAbsolutePath());
            FileUtils.recursiveDelete(cacheFile, directory);

            LOG.info("Deleting cached directory: {}", directory.getAbsolutePath());
            FileUtils.recursiveDelete(directory, directory);
        } catch (IOException e) {
            LOG.error("Failed to delete '{}'. continue..." + directory.getAbsolutePath());
        }

        idx.removeEntry(entry.getResourceHref(), entry.getVersion());
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

        final File dir = rootCacheDir.getFile();
        LOG.debug("Clearing cache directory: {}", dir);

        cacheIndex.runSynchronized(idx -> {
            deleteAll(dir.listFiles(File::isDirectory));
            idx.clear();
        });

        if (OsUtil.isWindows()) {
            WindowsShortcutManager.removeWindowsShortcuts("ALL");
        }

        return true;
    }

    private void deleteAll(Collection<File> files) {
        deleteAll(files.toArray(new File[0]));
    }

    private void deleteAll(File... files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                FileUtils.recursiveDelete(file, file);
            } catch (IOException e) {
                LOG.error("Failed to delete directory {} - {}", file, e.getMessage());
            }
        }
    }

    /**
     * This will remove all old cache items.
     */
    void cleanCache() {
        if (cannotClearCache()) {
            return;
        }

        final File[] levelOneDirs = rootCacheDir.getFile().listFiles(File::isDirectory);
        if (levelOneDirs == null) {
            cacheIndex.runSynchronized(LeastRecentlyUsedCacheIndex::clear);
        } else {
            final Set<String> entryIds = collectAllEntryIdsFromFileSystem(levelOneDirs);
            cacheIndex.runSynchronized(idx -> {

                final long maxSize = getMaxSizeInBytes();
                long curSize = 0;

                for (LeastRecentlyUsedCacheEntry entry : idx.getAllEntries()) {
                    entryIds.remove(entry.getId());

                    final CacheEntry infoFile = getInfoFile(entry);
                    final File cacheFile = infoFile.getCacheFile();
                    final File directory = cacheFile.getParentFile();

                    if (!infoFile.exists()) {
                        idx.removeEntry(entry);
                        if (directory.exists()) {
                            deleteAll(directory);
                        }
                        continue;
                    }

                    final long size = cacheFile.length();
                    if (entry.isMarkedForDeletion() || !cacheFile.isFile() || (maxSize >= 0 && curSize + size > maxSize)) {
                        idx.removeEntry(entry);
                        deleteAll(directory);
                        continue;
                    }

                    final File[] cacheDirFiles = cacheFile.getParentFile().listFiles();
                    if (null != cacheDirFiles) {
                        for (File file : cacheDirFiles) {
                            if (!file.equals(cacheFile) && !file.getName().equals(CacheEntry.INFO_SUFFIX)) {
                                deleteAll(file);
                            }
                        }

                    }

                    curSize += size;
                }
            });

            // delete dirs with no entry in the least recently used index
            final List<File> dirsWithNoEntryInTheIndex = entryIds.stream()
                    .map(this::cacheDirFromEntryId)
                    .collect(Collectors.toList());
            deleteAll(dirsWithNoEntryInTheIndex);

            // delete empty level one dirs
            final List<File> emptyDirs = Arrays.stream(levelOneDirs)
                    .filter(dir -> isNullOrEmpty(dir.list()))
                    .collect(Collectors.toList());
            deleteAll(emptyDirs);
        }
    }

    private Set<String> collectAllEntryIdsFromFileSystem(File[] levelOneDirs) {
        final Set<String> entryIds = new HashSet<>();
        for (File levelOneDir : levelOneDirs) {
            final File[] levelTwoDirs = levelOneDir.listFiles(File::isDirectory);
            if (levelTwoDirs != null) {
                for (File levelTwoDir : levelTwoDirs) {
                    final String entryId = entryIdFromCacheDir(levelTwoDir);
                    if (new File(levelTwoDir, CacheEntry.INFO_SUFFIX).isFile()) {
                        entryIds.add(entryId);
                    }
                }
            }
        }
        return entryIds;
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

    // Helpers

    private File getCacheFile(LeastRecentlyUsedCacheEntry entry) {
        final String[] idParts = entry.getId().split("-");
        final String cacheFilName = getCacheFileName(entry.getResourceHref());
        return new File(String.join("/", rootCacheDir.getFullPath(), idParts[0], idParts[1], cacheFilName));
    }

    private CacheEntry getInfoFile(LeastRecentlyUsedCacheEntry entry) {
        final File cacheFile = getCacheFile(entry);
        final File infoFile = new File(cacheFile.getParentFile(), CacheEntry.INFO_SUFFIX);
        return new CacheEntry(entry, cacheFile, infoFile);
    }

    private String entryIdFromCacheDir(File dir) {
        return dir.getParentFile().getName() + "-" + dir.getName();
    }

    private File cacheDirFromEntryId(String entryId) {
        final String[] idParts = entryId.split("-");
        return new File(String.join("/", rootCacheDir.getFullPath(), idParts[0], idParts[1]));
    }

    private String getCacheFileName(URL resourceHref) {
        final String path = resourceHref.getPath();
        final int i = path.lastIndexOf('/');
        if (i < 0) {
            return path;
        }
        return path.substring(i + 1);
    }

    private boolean cannotClearCache() {
        if (okToClearCache()) {
            final File cacheRoot = rootCacheDir.getFile();
            if (cacheRoot.isDirectory()) {
                return false;
            } else {
                LOG.error("Cannot clear the cache as there exists no such directory {}", cacheRoot);
                return true;
            }
        }

        LOG.error("Cannot clear the cache at this time. Try later. " +
                "If the problem persists, try closing your browser(s) & JNLP applications. " +
                "At the end you can try to kill all java applications. " +
                "You can clear cache by javaws -Xclearcache or via itw-settings Cache -> View files -> Purge");
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
            LOG.error("Failed to lock MAIN_LOCK (" + PathsAndFiles.MAIN_LOCK.getFullPath() + ")", e);
            return false;
        } finally {
            if (locking != null) {
                try {
                    locking.release();
                } catch (IOException ex) {
                    LOG.error("failed to unlock the MAIN_LOCK", ex);
                }
            }
        }
    }

    private static class CacheHolder {
        private static final LeastRecentlyUsedCache INSTANCE = new LeastRecentlyUsedCache(PathsAndFiles.getRecentlyUsedFile(), PathsAndFiles.CACHE_DIR);
    }

}
