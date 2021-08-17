package net.adoptopenjdk.icedteaweb.resources.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.sort;

class CacheIndexEntries {

    private final List<CacheIndexEntry> entries = new ArrayList<>();
    private final List<CacheIndexEntry> unmodifiableEntries = Collections.unmodifiableList(entries);

    List<CacheIndexEntry> getAllEntries() {
        return unmodifiableEntries;
    }

    Stream<CacheIndexEntry> stream() {
        return entries.stream();
    }

    boolean apply(CacheAction action) {
        return action.applyTo(this);
    }

    boolean addEntry(CacheIndexEntry entry) {
        entries.add(0, entry);
        return true;
    }

    boolean markAccessed(CacheIndexEntry entry, long lastAccessed) {
        final int idx = entries.indexOf(entry);
        if (idx > -1) {
            final CacheIndexEntry old = entries.remove(idx);
            CacheIndexEntry accessedEntry = new CacheIndexEntry(old.getId(), lastAccessed, old.getCacheKey());
            entries.add(0, accessedEntry);
        }
        return idx > -1;
    }

    boolean removeEntry(CacheIndexEntry entry) {
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
