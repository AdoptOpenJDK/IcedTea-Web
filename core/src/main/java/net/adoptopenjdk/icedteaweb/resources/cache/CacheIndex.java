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
class CacheIndex {

    private final CacheIndexFile cacheFile;

    CacheIndex(CacheIndexFile cacheFile) {
        this.cacheFile = cacheFile;
    }

    /**
     * Finds a single entry.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<CacheIndexEntry> findEntry(CacheKey key) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(key))
                .findFirst();
    }

    /**
     * Finds a single entry and marks it as accessed.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<CacheIndexEntry> findAndMarkAsAccessed(CacheKey key) {
        final Optional<CacheIndexEntry> result = findEntry(key);

        result.ifPresent(this::markAccessed);

        return result;
    }

    /**
     * Finds all entries matching the resource independent of their version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<CacheIndexEntry> findAllEntries(URL resourceHref) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(resourceHref))
                .collect(Collectors.toSet());
    }

    /**
     * Finds all entries matching the resource and version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<CacheIndexEntry> findAllEntries(URL resourceHref, VersionString versionString) {
        return cacheFile.getAllEntries().stream()
                .filter(e -> e.matches(resourceHref, versionString))
                .collect(Collectors.toSet());
    }

    /**
     * @return all entries
     */
    List<CacheIndexEntry> getAllEntries() {
        return cacheFile.getAllEntries();
    }

    /**
     * Create a new entry.
     *
     * @return the newly created entry
     */
    CacheIndexEntry createEntry(CacheKey key, String entryId) {
        final long now = System.currentTimeMillis();
        final CacheIndexEntry newEntry = new CacheIndexEntry(entryId, now, key);
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
    void removeEntry(CacheIndexEntry entry) {
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

    private void markAccessed(CacheIndexEntry entry) {
        final long now = System.currentTimeMillis();
        cacheFile.markAccessed(entry, now);
    }
}
