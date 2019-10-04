package net.sourceforge.jnlp.cache.cache;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Holder of the cached LeastRecentlyUsedCacheIndex data.
 * The responsibility of this class is to make sure that access to the content of the
 * LeastRecentlyUsedCacheIndex is synchronized and any changes are written back to the file system.
 */
class LeastRecentlyUsedCacheIndexHolder {

    private static final Logger LOG = LoggerFactory.getLogger(LeastRecentlyUsedCacheIndexHolder.class);

    private static ReentrantLock lock = new ReentrantLock();

    private final InfrastructureFileDescriptor recentlyUsed;

    private PropertiesFile cachedIndexPropertiesFile;
    private List<LeastRecentlyUsedCacheEntry> cachedEntries;

    LeastRecentlyUsedCacheIndexHolder(InfrastructureFileDescriptor recentlyUsed) {
        this.recentlyUsed = recentlyUsed;
    }

    /**
     * Executes the code passed in action in a synchronized fashion to allow exclusive access to the index.
     */
    void runSynchronized(Consumer<LeastRecentlyUsedCacheIndex> action) {
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
    <T> T getSynchronized(Function<LeastRecentlyUsedCacheIndex, T> action) {
        lock.lock();
        try {
            final PropertiesFile propertiesFile = lockPropertiesFile();
            try {
                final LeastRecentlyUsedCacheIndex index = load(propertiesFile);
                final T result = action.apply(index);
                if (index.isDirty()) {
                    store(propertiesFile);
                }
                return result;
            } finally {
                unlockPropertiesFile(propertiesFile);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Lock the properties file to have exclusive access.
     */
    private PropertiesFile lockPropertiesFile() {
        final PropertiesFile cacheProperties = getCacheProperties();
        cacheProperties.lock();
        return cacheProperties;
    }

    /**
     * Unlock the properties file.
     */
    private void unlockPropertiesFile(PropertiesFile propertiesFile) {
        propertiesFile.unlock();
    }

    /**
     * Loads the data from the properties file into an index.
     */
    private LeastRecentlyUsedCacheIndex load(PropertiesFile propertiesFile) {
        if (propertiesFile.load()) {
            final LeastRecentlyUsedCacheIndex.ConversionResult result = LeastRecentlyUsedCacheIndex.convertPropertiesToEntries(propertiesFile);
            if (result.propertiesNeedToBeStored) {
                LOG.warn("Cache is corrupt. Fixing...");
                store(propertiesFile);
                LOG.warn("Cache was corrupt and has been fixed. It is strongly recommended that you run ''javaws -Xclearcache'' and rerun your application as soon as possible. You can also use via itw-settings Cache -> View files -> Purge");
            }

            cachedEntries = result.entries;
        }
        return new LeastRecentlyUsedCacheIndex(propertiesFile, cachedEntries);
    }

    /**
     * Write file to disk.
     */
    private void store(PropertiesFile propertiesFile) {
        if (propertiesFile.isHeldByCurrentThread()) {
            propertiesFile.store();
        }
    }

    /**
     * @return the recentlyUsedPropertiesFile
     */
    private PropertiesFile getCacheProperties() {
        final File recentlyUsedFile = recentlyUsed.getFile();
        if (!recentlyUsedFile.exists()) {
            try {
                FileUtils.createParentDir(recentlyUsedFile);
                FileUtils.createRestrictedFile(recentlyUsedFile);
            } catch (IOException e) {
                LOG.error("Error in creating recently used cache items file.", e);
            }
        }

        if (cachedIndexPropertiesFile == null) {
            // no properties file yet, create it
            cachedIndexPropertiesFile = new PropertiesFile(recentlyUsedFile);
            return cachedIndexPropertiesFile;
        }

        if (recentlyUsedFile.equals(cachedIndexPropertiesFile.getStoreFile())) {
            // The underlying InfrastructureFileDescriptor is still pointing to the same file, use current properties file
            return cachedIndexPropertiesFile;
        } else {
            // the InfrastructureFileDescriptor was set to different location, move to it
            if (cachedIndexPropertiesFile.tryLock()) {
                cachedIndexPropertiesFile.store();
                cachedIndexPropertiesFile.unlock();
            }
            cachedIndexPropertiesFile = new PropertiesFile(recentlyUsedFile);
            return cachedIndexPropertiesFile;
        }
    }

}
