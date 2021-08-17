package net.adoptopenjdk.icedteaweb.resources.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.sort;

class LeastRecentlyUsedCacheEntries {

    private final List<LeastRecentlyUsedCacheEntry> entries = new ArrayList<>();
    private final List<LeastRecentlyUsedCacheEntry> unmodifiableEntries = Collections.unmodifiableList(entries);

    List<LeastRecentlyUsedCacheEntry> getAllEntries() {
        return unmodifiableEntries;
    }

    Stream<LeastRecentlyUsedCacheEntry> stream() {
        return entries.stream();
    }

    boolean apply(LeastRecentlyUsedCacheAction action) {
        return action.applyTo(this);
    }

    boolean addEntry(LeastRecentlyUsedCacheEntry entry) {
        entries.add(0, entry);
        return true;
    }

    boolean markAccessed(LeastRecentlyUsedCacheEntry entry, long lastAccessed) {
        final int idx = entries.indexOf(entry);
        if (idx > -1) {
            final LeastRecentlyUsedCacheEntry old = entries.remove(idx);
            LeastRecentlyUsedCacheEntry accessedEntry = new LeastRecentlyUsedCacheEntry(old.getId(), lastAccessed, old.getCacheKey());
            entries.add(0, accessedEntry);
        }
        return idx > -1;
    }

    boolean removeEntry(LeastRecentlyUsedCacheEntry entry) {
        return entries.remove(entry);
    }

    boolean clear() {
        if (entries.isEmpty()) {
            return false;
        }

        entries.clear();
        return true;
    }

    void sortByLastAccessed() {
        sort(entries);
    }
}
