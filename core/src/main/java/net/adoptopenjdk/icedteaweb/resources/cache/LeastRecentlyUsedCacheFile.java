package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.lockingfile.LockableFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.sort;
import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;
import static net.adoptopenjdk.icedteaweb.io.FileUtils.loadFileAsUtf8String;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.createAccessActionFor;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.createAddActionFor;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.createRemoveActionFor;

/**
 * Class to interface with the LRU cache index file.
 * <p>
 * The following restriction are enforced by the class:
 * - loading and saving can only be done when locked
 * - reading and changing the entities can only be done after loading
 */
class LeastRecentlyUsedCacheFile {

    private final LockableFile lockableFile;

    private final List<LeastRecentlyUsedCacheAction> unsavedActions = new ArrayList<>();

    /**
     * time of last modification, lazy loaded on getProperty
     */
    private long lastLoadOrStore = -1;

    private final List<LeastRecentlyUsedCacheEntry> entries = new ArrayList<>();
    private final List<LeastRecentlyUsedCacheEntry> unmodifiableEntries = Collections.unmodifiableList(entries);

    LeastRecentlyUsedCacheFile(File file) {
        this(LockableFile.getInstance(file));
    }

    LeastRecentlyUsedCacheFile(LockableFile lockableFile) {
        this.lockableFile = requireNonNull(lockableFile, "lockableFile");
    }

    List<LeastRecentlyUsedCacheEntry> getAllEntries() {
        if (isHasNeverBeenLoaded()) {
            throw new IllegalStateException("Cannot access entries before loading the file");
        }
        return unmodifiableEntries;
    }

    void addEntry(LeastRecentlyUsedCacheEntry entry) {
        entries.add(entry);
        unsavedActions.add(createAddActionFor(entry));
        sort(entries);
    }

    void markAccessed(LeastRecentlyUsedCacheEntry entry, long lastAccessed) {
        final int idx = entries.indexOf(entry);
        if (idx > -1) {
            final LeastRecentlyUsedCacheEntry old = entries.remove(idx);
            LeastRecentlyUsedCacheEntry accessedEntry = new LeastRecentlyUsedCacheEntry(old.getId(), lastAccessed, old.getCacheKey());
            entries.add(accessedEntry);
            unsavedActions.add(createAccessActionFor(old.getId(), lastAccessed));
            sort(entries);
        }
    }

    void removeEntry(LeastRecentlyUsedCacheEntry entry) {
        final boolean entryRemoved = entries.remove(entry);
        if (entryRemoved) {
            unsavedActions.add(createRemoveActionFor(entry.getId()));
        }
    }

    void load() throws IOException {
        if (!lockableFile.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot load cache file when not locked");
        }
        if (isDirty()) {
            throw new IllegalStateException("loading dirty properties file");
        }

        final File file = lockableFile.getFile();
        final long lastModified = file.lastModified();
        final long now = System.currentTimeMillis();

        final boolean hasBeenModifiedSinceLastLoadOrStore = lastModified != lastLoadOrStore;
        final boolean almostNoTimeHasPassedSinceLastModification = lastModified / 1000 == now / 1000;
        // the last option is for file systems which do not store milliseconds in the lastModified field.
        // in such a case one can only see a difference in between lastModified and lastLoadOrStore if at least one second has passed.
        if (isHasNeverBeenLoaded() || hasBeenModifiedSinceLastLoadOrStore || almostNoTimeHasPassedSinceLastModification) {

            final String content = loadFileAsUtf8String(file);
            final String[] lines = content.split("\\R");

            Stream.of(lines)
                    .map(LeastRecentlyUsedCacheAction::parse)
                    .forEach(action -> action.applyTo(this));

            lastLoadOrStore = now;
            unsavedActions.clear();
        }
    }

    boolean isDirty() {
        return !unsavedActions.isEmpty();
    }

    public void persistChanges() throws IOException {
        if (!lockableFile.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot persist changes to cache file when not locked");
        }
        if (!isDirty()) {
            return;
        }

        final byte[] changes = unsavedActions.stream()
                .map(LeastRecentlyUsedCacheAction::serialize)
                .collect(Collectors.joining("\n"))
                .concat("\n")
                .getBytes(UTF_8);

        try (FileOutputStream out = new FileOutputStream(lockableFile.getFile(), true)) {
            out.write(changes);
            out.flush();
        }

        lastLoadOrStore = System.currentTimeMillis();
        unsavedActions.clear();
    }

    public void saveCompactedFile() throws IOException {
        if (!lockableFile.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot save compacted cache file when not locked");
        }
        if (!isDirty()) {
            return;
        }

        final String content = entries.stream()
                .map(LeastRecentlyUsedCacheAction::createAddActionFor)
                .map(LeastRecentlyUsedCacheAction::serialize)
                .collect(Collectors.joining("\n"))
                .concat("\n");

        FileUtils.saveFileUtf8(content, lockableFile.getFile());

        lastLoadOrStore = System.currentTimeMillis();
        unsavedActions.clear();
    }

    public void lock() throws IOException {
        lockableFile.lock();
    }

    public void unlock() throws IOException {
        lockableFile.unlock();
    }

    private boolean isHasNeverBeenLoaded() {
        return lastLoadOrStore == -1;
    }
}
