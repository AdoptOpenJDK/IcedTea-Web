package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.RestrictedFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.sourceforge.jnlp.config.ConfigurationConstants.OLD_CACHE_INDEX_FILE_NAME;

/**
 * Holder of the cached LeastRecentlyUsedCacheIndex data.
 * The responsibility of this class is to make sure that access to the content of the
 * LeastRecentlyUsedCacheIndex is synchronized and any changes are written back to the file system.
 */
class CacheIndexHolder {

    private static final Logger LOG = LoggerFactory.getLogger(CacheIndexHolder.class);

    private final InfrastructureFileDescriptor recentlyUsed;

    private CacheIndexFile cachedCacheFile;

    CacheIndexHolder(InfrastructureFileDescriptor recentlyUsed) {
        this.recentlyUsed = recentlyUsed;
    }

    /**
     * Executes the code passed in action in a synchronized fashion to allow exclusive access to the index.
     */
    void runSynchronized(Consumer<CacheIndex> action) {
        getSynchronized(idx -> {
            action.accept(idx);
            return null;
        });
    }

    /**
     * Executes the function passed in action in a synchronized fashion to allow exclusive access to the index.
     *
     * @return the result of the passed function.
     */
    <T> T getSynchronized(Function<CacheIndex, T> action) {
        CacheIndexFile cacheFile = null;
        try {
            cacheFile = lockCacheFile();
            final CacheIndex index = load(cacheFile);
            final T result = action.apply(index);
            persistChanges(cacheFile);
            return result;
        } finally {
            unlockCacheFile(cacheFile);
        }
    }

    /**
     * Lock the properties file to have exclusive access.
     */
    private CacheIndexFile lockCacheFile() {
        try {
            final CacheIndexFile cacheFile = getCacheFile();
            cacheFile.lock();
            return cacheFile;
        } catch (IOException e) {
            LOG.error("Failed to lock cache file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Unlock the properties file.
     */
    private void unlockCacheFile(CacheIndexFile cacheFile) {
        if (cacheFile != null) {
            try {
                cacheFile.unlock();
            } catch (IOException e) {
                LOG.error("Failed to unlock cache file", e);
            }
        }
    }

    /**
     * Loads the data from the properties file into an index.
     */
    private CacheIndex load(CacheIndexFile cacheFile) {
        try {
            cacheFile.load();
            return new CacheIndex(cacheFile);
        } catch (IOException e) {
            LOG.error("Failed to load cache file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Write file to disk.
     */
    private void persistChanges(CacheIndexFile cacheFile) {
        try {
            cacheFile.persistChanges();
        } catch (IOException e) {
            LOG.error("Failed to store cache file", e);
        }
    }

    /**
     * @return the recentlyUsedPropertiesFile
     */
    private CacheIndexFile getCacheFile() {
        final File underlyingFile = recentlyUsed.getFile();
        if (!underlyingFile.exists()) {
            try {
                FileUtils.createParentDir(underlyingFile);
                RestrictedFileUtils.createRestrictedFile(underlyingFile);
                cachedCacheFile = loadOldIndex();
            } catch (IOException e) {
                LOG.error("Error in creating recently used cache items file.", e);
            }
        }

        if (cachedCacheFile == null || !cachedCacheFile.usesFile(underlyingFile)) {
            cachedCacheFile = new CacheIndexFile(underlyingFile);
        }
        return cachedCacheFile;
    }


    private CacheIndexFile loadOldIndex() {
        final File cacheDir = recentlyUsed.getFile().getParentFile();
        final File oldIndex = new File(cacheDir, OLD_CACHE_INDEX_FILE_NAME);

        if (!oldIndex.exists()) {
            return null;
        }

        try {
            final List<CacheIndexEntry> oldEntries = OldCacheFileReader.loadEntriesFromOldIndex(oldIndex);

            if (oldEntries == null || oldEntries.isEmpty()) {
                return null;
            }

            final CacheIndexFile result = new CacheIndexFile(recentlyUsed.getFile());
            try {
                result.lock();
                result.load();
                oldEntries.stream()
                        .map(e -> new CacheIndexEntry(convertOldId(e.getId()), e.getLastAccessed(), e.getCacheKey()))
                        .forEach(result::addEntry);
                result.persistChanges();
            } finally {
                result.unlock();
            }
            return result;
        } catch (Exception ignored) {
            return null;
        } finally {
            oldIndex.delete();
        }
    }

    private String convertOldId(String id) {
        return id.replace('-', File.pathSeparatorChar);
    }
}
