/* CacheLRUWrapper -- Handle LRU for cache files.
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.client.controlpanel.CacheIdInfo;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionIdComparator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.RestrictedFileUtils;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static net.adoptopenjdk.icedteaweb.CollectionUtils.isNullOrEmpty;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * This class helps maintain the ordering of most recently use cache items across
 * multiple jvm instances. The LRU information is stored as a properties file in the
 * root of the file cache directory.
 */
class CacheImpl {

    private static final Logger LOG = LoggerFactory.getLogger(CacheImpl.class);

    static CacheImpl getInstance() {
        return CacheHolder.INSTANCE;
    }

    private final CacheIndexHolder cacheIndex;
    private final InfrastructureFileDescriptor rootCacheDir;

    /**
     * @param recentlyUsed file to be used as recently_used file
     * @param cacheDir     dir with cache
     */
    private CacheImpl(final InfrastructureFileDescriptor recentlyUsed, final InfrastructureFileDescriptor cacheDir) {
        this.cacheIndex = new CacheIndexHolder(recentlyUsed);
        this.rootCacheDir = cacheDir;
    }

    File getOrCreateCacheFile(CacheKey key) {
        final CacheIndexEntry entry = cacheIndex.getSynchronized(idx ->
                getOrCreateCacheEntry(idx, key)
        );
        return getCacheFile(entry);
    }

    private CacheIndexEntry getOrCreateCacheEntry(CacheIndex idx, CacheKey key) {
        return idx.findAndMarkAsAccessed(key)
                .orElseGet(() -> createNewInfoFileAndIndexEntry(idx, key));
    }

    private CacheIndexEntry createNewInfoFileAndIndexEntry(CacheIndex idx, CacheKey key) {
        final File dir = makeNewCacheDir();
        final String entryId = entryIdFromCacheDir(dir);
        createInfoFile(dir);
        return idx.createEntry(key, entryId);
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
                    throw new RuntimeException("Cannot create directory " + cDir);
                }
            }
        }
        throw new RuntimeException("Out of directories :-)");
    }

    private void createInfoFile(File dir) {
        try {
            final File infoFile = new File(dir, CachedFile.INFO_SUFFIX);
            RestrictedFileUtils.createRestrictedFile(infoFile); // Create the info file for marking later.

            final String jnlpPath = JNLPRuntime.getJnlpPath(); //get jnlp from args passed
            if (isBlank(jnlpPath)) {
                LOG.info("Not-setting jnlp-path for missing main/jnlp argument");
            } else {
                final PropertiesFile propertiesFile = new PropertiesFile(infoFile, R("CAutoGen"));
                propertiesFile.setProperty(CachedFile.KEY_JNLP_PATH, jnlpPath);
                propertiesFile.store();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create info file in dir " + dir, e);
        }
    }

    File addToCache(DownloadInfo info, InputStream inputStream) throws IOException {
        final List<IOException> ex = new ArrayList<>();

        final CacheIndexEntry entry = cacheIndex.getSynchronized(idx ->
                getOrCreateCacheEntry(idx, info.getCacheKey())
        );

        final CachedFile infoFile = getInfoFile(entry);
        final File cacheFile = infoFile.getCacheFile();
        try {
            LOG.debug("Downloading file: {} into: {}", info.getCacheKey().getLocation(), cacheFile.getCanonicalPath());
            try (final OutputStream out = new FileOutputStream(cacheFile)) {
                IOUtils.copy(inputStream, out);
            }
            infoFile.storeInfo(info.getDownloadedAt(), info.getLastModified(), cacheFile.length());
        } catch (IOException e) {
            ex.add(e);
        }

        if (!ex.isEmpty()) {
            throw ex.get(0);
        }

        return cacheFile;
    }

    Optional<CachedFile> getResourceInfo(CacheKey key) {
        return cacheIndex.getSynchronized(idx -> idx.findEntry(key))
                .map(this::getInfoFile);
    }

    /**
     * Returns whether there is a version of the URL contents in the cache.
     *
     * @param key the key of the cache entry
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resourceHref is not cacheable
     */
    boolean isCached(CacheKey key) {
        final boolean isCached = getResourceInfo(key)
                .map(CachedFile::isCached)
                .orElse(false);
        LOG.info("isCached: {} = {}", key, isCached);
        return isCached;
    }

    /**
     * Returns whether there is a version of the URL contents in the cache and it is up to date.
     *
     * @param key the key of the cache entry
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resourceHref is not cacheable
     */
    boolean isUpToDate(CacheKey key, long lastModified) {
        final Boolean isUpToDate = cacheIndex.getSynchronized(idx -> idx.findAndMarkAsAccessed(key))
                .map(e -> getInfoFile(e).isCurrent(lastModified))
                .orElse(false);
        LOG.info("isUpToDate: {} = {}", key, isUpToDate);
        return isUpToDate;
    }

    Optional<CacheIndexEntry> getBestMatchingEntryInCache(final URL resourceHref, final VersionString version) {
        return getAllMatchingEntryInCache(resourceHref, version).stream()
                .filter(entry -> getInfoFile(entry).isCached())
                .findFirst();
    }

    private List<CacheIndexEntry> getAllMatchingEntryInCache(final URL resourceHref, final VersionString version) {
        final List<CacheIndexEntry> all = new ArrayList<>(cacheIndex.getSynchronized(idx -> idx.findAllEntries(resourceHref, version)));

        if (all.size() > 1) {
            final Comparator<VersionId> versionIdComparator = version != null ? new VersionIdComparator(version) : VersionId::compareTo;
            final Comparator<CacheIndexEntry> versionComparator = comparing(CacheIndexEntry::getVersion, versionIdComparator);
            all.sort(versionComparator);
        }

        return all;
    }

    List<CacheIndexEntry> getAllEntriesInCache(final URL resourceHref) {
        final Comparator<CacheIndexEntry> versionComparator = comparing(CacheIndexEntry::getVersion);
        return cacheIndex.getSynchronized(idx -> {
            final Set<CacheIndexEntry> allSet = idx.findAllEntries(resourceHref);

            return allSet.stream()
                    .filter(entry -> getInfoFile(entry).isCached())
                    .sorted(versionComparator)
                    .collect(Collectors.toList());
        });
    }

    List<CacheIdInfo> getCacheIds(String filter, boolean includeJnlpPath, boolean includeDomain) {
        if (!includeJnlpPath && !includeDomain) {
            return Collections.emptyList();
        }

        final List<CacheIndexEntry> entries = cacheIndex.getSynchronized(idx -> idx.getAllEntries());

        final Map<String, CacheIdInfoImpl> result = new LinkedHashMap<>();
        entries.forEach(entry -> {
            final CacheFileInfoImpl fileEntry = createPaneObjectArray(entry);
            if (includeJnlpPath) {
                final CachedFile infoFile = getInfoFile(entry);
                final String jnlpPath = infoFile.getJnlpPath();
                if (jnlpPath != null && jnlpPath.matches(filter)) {
                    final CacheIdInfoImpl cacheId = result.computeIfAbsent(jnlpPath, CacheIdInfoImpl::jnlpPathId);
                    cacheId.addFileInfo(fileEntry);
                }
            }
            if (includeDomain) {
                final String domain = entry.getDomain();
                if (domain != null && domain.matches(filter)) {
                    final CacheIdInfoImpl cacheId = result.computeIfAbsent(domain, CacheIdInfoImpl::domainId);
                    cacheId.addFileInfo(fileEntry);
                }
            }
        });

        return new ArrayList<>(result.values());
    }

    private CacheFileInfoImpl createPaneObjectArray(CacheIndexEntry entry) {
        final CachedFile infoFile = getInfoFile(entry);
        return new CacheFileInfoImpl(infoFile, entry);
    }

    void invalidateExistingCacheFile(final CacheKey key) {
        cacheIndex.runSynchronized(idx -> idx.removeEntry(key));
    }

    void deleteFromCache(CacheKey key) {
        cacheIndex.runSynchronized(idx -> idx
                .findEntry(key)
                .ifPresent(entry -> deleteFromCache(idx, entry)));
    }

    void deleteFromCache(URL resourceHref, VersionString version) {
        cacheIndex.runSynchronized(idx -> idx
                .findAllEntries(resourceHref, version)
                .forEach(entry -> deleteFromCache(idx, entry)));
    }

    void deleteFromCache(CacheIdInfo cacheId) {
        final String idToDelete = cacheId.getId();
        final Function<CacheIndexEntry, String> idExtractor = createExtractor(cacheId.getType());

        cacheIndex.runSynchronized(idx -> {
            final List<CacheIndexEntry> allEntries = idx.getAllEntries();
            allEntries.stream()
                    .filter(entry -> Objects.equals(idToDelete, idExtractor.apply(entry)))
                    .forEach(entry -> deleteFromCache(idx, entry));
        });

        if (OsUtil.isWindows()) {
            WindowsShortcutManager.removeWindowsShortcuts(idToDelete.toLowerCase());
        }
    }

    private Function<CacheIndexEntry, String> createExtractor(CacheIdInfo.CacheIdType idType) {
        switch (idType) {
            case DOMAIN:
                return CacheIndexEntry::getDomain;
            case JNLP_PATH:
                return e -> getInfoFile(e).getJnlpPath();
            default:
                throw new IllegalStateException("Unknown CacheIdType: " + idType);
        }
    }

    private void deleteFromCache(CacheIndex idx, CacheIndexEntry entry) {
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

        idx.removeEntry(entry);
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
        if (files == null) {
            return;
        }

        for (File file : files) {
            deleteDir(file);
        }
    }

    private void deleteAll(File... files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            deleteDir(file);
        }
    }

    private void deleteDir(File file) {
        try {
            FileUtils.recursiveDelete(file, file);
        } catch (IOException e) {
            LOG.error("Failed to delete directory {} - {}", file, e.getMessage());
        }
    }

    /**
     * This will remove all old cache items.
     */
    void cleanCache() {
        LOG.debug("preparing to cleanup the cache");

        if (cannotClearCache()) {
            return;
        }

        LOG.debug("cleanup cache");
        final File[] levelOneDirs = rootCacheDir.getFile().listFiles(File::isDirectory);
        if (isNullOrEmpty(levelOneDirs)) {
            LOG.debug("found no folders in the cache dir - clear cache index");
            cacheIndex.runSynchronized(CacheIndex::clear);
        } else {
            LOG.debug("start cleaning the cache");
            final Set<String> entryIdsFromFileSystem = collectAllEntryIdsFromFileSystem(levelOneDirs);
            final Set<String> entryIdsFromIndex = new HashSet<>();
            cacheIndex.runSynchronized(idx -> {
                idx.requestCompression();

                final long maxSize = getMaxSizeInBytes();
                long curSize = 0;

                final List<CacheIndexEntry> toRemoveFromIndex = new ArrayList<>();
                for (CacheIndexEntry entry : idx.getAllEntries()) {
                    entryIdsFromIndex.add(entry.getId());

                    final CachedFile infoFile = getInfoFile(entry);
                    final File cacheFile = infoFile.getCacheFile();
                    final File directory = cacheFile.getParentFile();

                    if (!infoFile.exists()) {
                        LOG.debug("missing info file for {}", entry.getResourceHref());
                        toRemoveFromIndex.add(entry);
                        deleteDir(directory);
                        continue;
                    }

                    if (!cacheFile.isFile()) {
                        LOG.debug("missing cache file {}", entry.getResourceHref());
                        toRemoveFromIndex.add(entry);
                        deleteDir(directory);
                        continue;
                    }
                    final long size = cacheFile.length();
                    if (maxSize >= 0 && curSize + size > maxSize) {
                        LOG.debug("Current cache size is {} - file {} has size {} and would exceed max cache size {}",
                                curSize, entry.getResourceHref(), size, maxSize);
                        toRemoveFromIndex.add(entry);
                        deleteDir(directory);
                        continue;
                    }

                    final File[] cacheDirFiles = directory.listFiles();
                    if (!isNullOrEmpty(cacheDirFiles)) {
                        for (File file : cacheDirFiles) {
                            if (!file.equals(cacheFile) && !file.getName().equals(CachedFile.INFO_SUFFIX)) {
                                LOG.debug("found unknown file {}", file);
                                deleteDir(file);
                            }
                        }
                    }

                    curSize += size;
                }

                toRemoveFromIndex.forEach(idx::removeEntry);
            });

            // delete dirs with no entry in the least recently used index
            entryIdsFromFileSystem.removeAll(entryIdsFromIndex);
            final List<File> dirsWithNoEntryInTheIndex = entryIdsFromFileSystem.stream()
                    .map(this::cacheDirFromEntryId)
                    .collect(Collectors.toList());
            if (dirsWithNoEntryInTheIndex.size() > 0) {
                LOG.debug("found directories with no entry in the index");
                deleteAll(dirsWithNoEntryInTheIndex);
            }

            // delete empty level one dirs
            final List<File> emptyDirs = Arrays.stream(levelOneDirs)
                    .filter(dir -> isNullOrEmpty(dir.list()))
                    .collect(Collectors.toList());
            if (emptyDirs.size() > 0) {
                LOG.debug("found empty directories");
                deleteAll(emptyDirs);
            }
        }
        LOG.debug("done cleaning the cache");
    }

    private Set<String> collectAllEntryIdsFromFileSystem(File[] levelOneDirs) {
        final Set<String> entryIds = new HashSet<>();
        for (File levelOneDir : levelOneDirs) {
            final File[] levelTwoDirs = levelOneDir.listFiles(File::isDirectory);
            if (levelTwoDirs != null) {
                for (File levelTwoDir : levelTwoDirs) {
                    final String entryId = entryIdFromCacheDir(levelTwoDir);
                    if (new File(levelTwoDir, CachedFile.INFO_SUFFIX).isFile()) {
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
        } catch (NumberFormatException | NullPointerException ignored) {
            return -1;
        }
    }

    // Helpers

    private File getCacheFile(CacheIndexEntry entry) {
        final String cacheFilName = getCacheFileName(entry.getResourceHref());
        return new File(cacheDirFromEntryId(entry.getId()), cacheFilName);
    }

    private CachedFile getInfoFile(CacheIndexEntry entry) {
        final File cacheFile = getCacheFile(entry);
        final File infoFile = new File(cacheFile.getParentFile(), CachedFile.INFO_SUFFIX);
        return new CachedFile(entry, cacheFile, infoFile);
    }

    private String entryIdFromCacheDir(File dir) {
        return dir.getParentFile().getName() + File.separatorChar + dir.getName();
    }

    private File cacheDirFromEntryId(String entryId) {
        return new File(rootCacheDir.getFullPath(), entryId);
    }

    private String getCacheFileName(URL resourceHref) {
        final String fileName = extractFileNameFromUrl(resourceHref);
        return isBlank(fileName) ? "0" : fileName;
    }

    private String extractFileNameFromUrl(URL resourceHref) {
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

        LOG.info("Cannot clear the cache at this time. Try later. " +
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
        private static final CacheImpl INSTANCE = new CacheImpl(PathsAndFiles.getRecentlyUsedFile(), PathsAndFiles.CACHE_DIR);
    }

}
