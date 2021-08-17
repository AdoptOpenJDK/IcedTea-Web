package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Index of the cached resources.
 * <p>
 * This implementation uses a least recently accessed approach
 * to evict resources when the total size of the cache exceeds the limit.
 */
class LeastRecentlyUsedCacheIndex {

    private final LeastRecentlyUsedCacheFile cacheFile;

    LeastRecentlyUsedCacheIndex(LeastRecentlyUsedCacheFile cacheFile) {
        this.cacheFile = cacheFile;
    }

    /**
     * Finds a single entry.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<LeastRecentlyUsedCacheEntry> findEntry(CacheKey key) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(key))
                .findFirst();
    }

    /**
     * Finds a single entry and marks it as accessed.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<LeastRecentlyUsedCacheEntry> findAndMarkAsAccessed(CacheKey key) {
        final Optional<LeastRecentlyUsedCacheEntry> result = findEntry(key);

        result.ifPresent(this::markAccessed);

        return result;
    }

    /**
     * Finds all entries matching the resource independent of their version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<LeastRecentlyUsedCacheEntry> findAllEntries(URL resourceHref) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(resourceHref))
                .collect(Collectors.toSet());
    }

    /**
     * Finds all entries matching the resource and version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<LeastRecentlyUsedCacheEntry> findAllEntries(URL resourceHref, VersionString versionString) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(resourceHref, versionString))
                .collect(Collectors.toSet());
    }

    /**
     * @return all entries
     */
    List<LeastRecentlyUsedCacheEntry> getAllEntries() {
        return cacheFile.getAllEntries();
    }

    /**
     * Create a new entry.
     *
     * @return the newly created entry
     */
    LeastRecentlyUsedCacheEntry createEntry(CacheKey key, String entryId) {
        final long now = System.currentTimeMillis();
        final LeastRecentlyUsedCacheEntry newEntry = new LeastRecentlyUsedCacheEntry(entryId, now, key);
        cacheFile.addEntry(newEntry);
        return newEntry;
    }

    /**
     * Marks the entry for deletion
     */
    void removeEntry(CacheKey key) {
        findEntry(key).ifPresent(cacheFile::removeEntry);
    }

    /**
     * Removes an entry from the index.
     */
    void removeEntry(LeastRecentlyUsedCacheEntry entry) {
        cacheFile.removeEntry(entry);
    }

    void requestCompression() {
        cacheFile.requestCompression();
    }

    /**
     * Removes all entries.
     */
    void clear() {
        cacheFile.clear();
    }

    private void markAccessed(LeastRecentlyUsedCacheEntry entry) {
        final long now = System.currentTimeMillis();
        cacheFile.markAccessed(entry, now);
    }
}
