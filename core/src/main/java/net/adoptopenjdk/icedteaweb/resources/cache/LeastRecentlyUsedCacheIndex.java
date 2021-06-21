package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

/**
 * Index of the cached resources.
 * <p>
 * This implementation uses a least recently accessed approach
 * to evict resources when the total size of the cache exceeds the limit.
 */
class LeastRecentlyUsedCacheIndex {

    private static final Logger LOG = LoggerFactory.getLogger(LeastRecentlyUsedCacheIndex.class);

    static final String KEY_LAST_ACCESSED = "lastAccessed";
    private static final String KEY_DELETE = "delete";
    static final String KEY_HREF = "href";
    private static final String KEY_VERSION = "version";

    private final PropertiesFile propertiesFile;
    private final List<LeastRecentlyUsedCacheEntry> entries;

    private boolean dirty = false;

    LeastRecentlyUsedCacheIndex(PropertiesFile propertiesFile, List<LeastRecentlyUsedCacheEntry> entries) {
        this.propertiesFile = propertiesFile;
        this.entries = entries;
    }

    /**
     * Finds a single entry.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<LeastRecentlyUsedCacheEntry> find(URL resourceHref, VersionId version) {
        return entries.stream()
                .filter(e -> !e.isMarkedForDeletion())
                .filter(e -> e.matches(resourceHref, version))
                .findFirst();
    }

    /**
     * Finds a single entry and marks it as accessed.
     *
     * @return the entry found or {@code empty}, never {@code null}.
     */
    Optional<LeastRecentlyUsedCacheEntry> findAndMarkAsAccessed(URL resourceHref, VersionId version) {
        final Optional<LeastRecentlyUsedCacheEntry> result = find(resourceHref, version);

        result.ifPresent(this::markAccessed);

        return result;
    }

    /**
     * Finds all entries matching the resource independent of their version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<LeastRecentlyUsedCacheEntry> findAll(URL resourceHref) {
        return entries.stream()
                .filter(e -> !e.isMarkedForDeletion())
                .filter(e -> e.matches(resourceHref))
                .collect(Collectors.toSet());
    }

    /**
     * Finds all entries matching the resource and version.
     *
     * @return a set of all matching entries, never {@code null}.
     */
    Set<LeastRecentlyUsedCacheEntry> findAll(URL resourceHref, VersionString versionString) {
        return entries.stream()
                .filter(e -> !e.isMarkedForDeletion())
                .filter(e -> e.matches(resourceHref, versionString))
                .collect(Collectors.toSet());
    }

    /**
     * @return all entries which are not marked for deletion
     */
    List<LeastRecentlyUsedCacheEntry> getAllUnDeletedEntries() {
        return entries.stream()
                .filter(e -> !e.isMarkedForDeletion())
                .collect(Collectors.toList());
    }

    /**
     * @return all entries
     */
    List<LeastRecentlyUsedCacheEntry> getAllEntries() {
        return entries;
    }

    /**
     * Create a new entry.
     *
     * @return the newly created entry
     */
    LeastRecentlyUsedCacheEntry createEntry(URL resourceHref, VersionId version, String entryId) {
        final long now = System.currentTimeMillis();
        final LeastRecentlyUsedCacheEntry newEntry = new LeastRecentlyUsedCacheEntry(entryId, now, resourceHref, version);
        entries.add(0, newEntry);
        propertiesFile.setProperty(entryId + '.' + KEY_HREF, resourceHref.toString());
        if (version != null) {
            propertiesFile.setProperty(entryId + '.' + KEY_VERSION, version.toString());
        }
        propertiesFile.setProperty(entryId + '.' + KEY_LAST_ACCESSED, Long.toString(now));

        dirty = true;
        return newEntry;
    }

    /**
     * Marks the entry for deletion
     */
    void markEntryForDeletion(URL resourceHref, VersionId version) {
        find(resourceHref, version).ifPresent(entry -> {
            entries.remove(entry);
            propertiesFile.setProperty(entry.getId() + '.' + KEY_DELETE, TRUE.toString());
            dirty = true;
        });
    }

    /**
     * Removes an entry from the index. If not entry matches nothing is changed.
     */
    void removeEntry(URL resourceHref, VersionId version) {
        find(resourceHref, version).ifPresent(this::removeEntry);
    }

    /**
     * Removes an entry from the index.
     */
    void removeEntry(LeastRecentlyUsedCacheEntry entry) {
        entries.remove(entry);
        propertiesFile.remove(entry.getId() + '.' + KEY_HREF);
        propertiesFile.remove(entry.getId() + '.' + KEY_VERSION);
        propertiesFile.remove(entry.getId() + '.' + KEY_DELETE);
        propertiesFile.remove(entry.getId() + '.' + KEY_LAST_ACCESSED);
        dirty = true;
    }

    /**
     * Removes all entries.
     */
    void clear() {
        entries.clear();
        propertiesFile.clear();
        dirty = true;
    }

    boolean isDirty() {
        return dirty;
    }

    private void markAccessed(LeastRecentlyUsedCacheEntry entry) {
        final long now = System.currentTimeMillis();
        entries.remove(entry);
        entries.add(0, new LeastRecentlyUsedCacheEntry(entry.getId(), now, entry.getResourceHref(), entry.getVersion()));
        propertiesFile.setProperty(entry.getId() + '.' + KEY_LAST_ACCESSED, Long.toString(now));
        dirty = true;
    }

    /**
     * check content of recentlyUsedPropertiesFile and remove invalid/corrupt entries
     *
     * @return true, if cache was corrupted and affected entry removed
     */
    static ConversionResult convertPropertiesToEntries(PropertiesFile props) {
        boolean modified = false;

        // STEP 1
        // group all properties with the same ID together
        // throwing away entries which do not have a valid key
        final Map<String, Map<String, String>> id2ValueMap = new HashMap<>();
        for (Map.Entry<String, String> propEntry : new HashSet<>(props.entrySet())) {
            final String key = propEntry.getKey();
            if (key != null) {
                final String[] keyParts = splitKey(key);
                if (keyParts.length == 2) {
                    final String value = propEntry.getValue();
                    id2ValueMap.computeIfAbsent(keyParts[0], k -> new HashMap<>()).put(keyParts[1], value);
                    continue;
                }
            }

            // if we reach this point something is wrong with the property
            LOG.debug("found broken property: {}", key);
            props.remove(key);
            modified = true;
        }

        // STEP 2
        // convert the properties to actual entries
        // collecting the IDs of the ones which have invalid data
        final List<LeastRecentlyUsedCacheEntry> entries = new ArrayList<>(id2ValueMap.size());
        for (Map.Entry<String, Map<String, String>> valuesEntry : id2ValueMap.entrySet()) {
            final Map<String, String> values = valuesEntry.getValue();

            final String id = valuesEntry.getKey();
            final String lastAccessedValue = values.get(KEY_LAST_ACCESSED);
            final String markedForDeletionValue = values.get(KEY_DELETE);
            final String resourceHrefValue = values.get(KEY_HREF);
            final String versionValue = values.get(KEY_VERSION);

            try {
                final VersionId version = versionValue != null ? VersionId.fromString(versionValue) : null;
                final URL resourceHref = new URL(resourceHrefValue);
                if (!Boolean.parseBoolean(markedForDeletionValue)) {
                    final long lastAccessed = Long.parseLong(lastAccessedValue);
                    entries.add(new LeastRecentlyUsedCacheEntry(id, lastAccessed, resourceHref, version));
                }
            } catch (Exception e) {
                LOG.debug("found broken ID: {}", id);
                props.remove(id + '.' + KEY_LAST_ACCESSED);
                props.remove(id + '.' + KEY_DELETE);
                props.remove(id + '.' + KEY_HREF);
                props.remove(id + '.' + KEY_VERSION);
                modified = true;
            }
        }

        // make sure the entries are sorted most recent accessed to least recent accessed
        Collections.sort(entries);

        return new ConversionResult(modified, entries);
    }

    private static String[] splitKey(String key) {
        final int i = key.indexOf('.');
        if (i > 0 && i < key.length()) {
            return new String[]{key.substring(0, i), key.substring(i + 1)};
        }
        return new String[0];
    }

    static class ConversionResult {
        final boolean propertiesNeedToBeStored;
        final List<LeastRecentlyUsedCacheEntry> entries;

        private ConversionResult(boolean propertiesNeedToBeStored, List<LeastRecentlyUsedCacheEntry> entries) {
            this.propertiesNeedToBeStored = propertiesNeedToBeStored;
            this.entries = entries;
        }
    }
}
