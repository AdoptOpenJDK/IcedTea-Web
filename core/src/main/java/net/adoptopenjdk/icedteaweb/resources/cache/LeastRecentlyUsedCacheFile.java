package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.lockingfile.LockableFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    private static final String LINE_END = "\\R";

    private final LockableFile lockableFile;

    private final List<LeastRecentlyUsedCacheAction> unsavedActions = new ArrayList<>();

    /**
     * time of last modification, lazy loaded on getProperty
     */
    private long lastLoadOrStore = -1;
    private boolean requestCompression = false;

    private final LeastRecentlyUsedCacheEntries entries = new LeastRecentlyUsedCacheEntries();

    LeastRecentlyUsedCacheFile(File file) {
        this(LockableFile.getInstance(file));
    }

    LeastRecentlyUsedCacheFile(LockableFile lockableFile) {
        this.lockableFile = requireNonNull(lockableFile, "lockableFile");
    }

    boolean usesFile(final File file) {
        return Objects.equals(file, lockableFile.getFile());
    }

    List<LeastRecentlyUsedCacheEntry> getAllEntries() {
        if (hasNeverBeenLoaded()) {
            throw new IllegalStateException("Cannot access entries before loading the file");
        }
        return entries.getAllEntries();
    }

    void addEntry(LeastRecentlyUsedCacheEntry entry) {
        applyAndSort(createAddActionFor(entry));
    }

    void markAccessed(LeastRecentlyUsedCacheEntry entry, long lastAccessed) {
        applyAndSort(createAccessActionFor(entry.getId(), lastAccessed));
    }

    void removeEntry(LeastRecentlyUsedCacheEntry entry) {
        applyAndSort(createRemoveActionFor(entry.getId()));
    }

    private void applyAndSort(LeastRecentlyUsedCacheAction action) {
        if (entries.apply(action)) {
            unsavedActions.add(action);
            entries.sortByLastAccessed();
        }
    }

    void clear() {
        if (entries.clear()) {
            requestCompression();
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
        if (hasNeverBeenLoaded() || hasBeenModifiedSinceLastLoadOrStore || almostNoTimeHasPassedSinceLastModification) {

            final String content = loadFileAsUtf8String(file);
            final String[] lines = content.split(LINE_END);

            Stream.of(lines)
                    .map(LeastRecentlyUsedCacheAction::parse)
                    .forEach(entries::apply);

            lastLoadOrStore = lastModified;
            entries.sortByLastAccessed();
        }
    }

    boolean isDirty() {
        return !unsavedActions.isEmpty() || requestCompression;
    }

    void requestCompression() {
        requestCompression = true;
    }

    void persistChanges() throws IOException {
        if (!lockableFile.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot persist changes to cache file when not locked");
        }
        if (requestCompression) {
            saveCompactedFile();
        } else if (!unsavedActions.isEmpty()) {
            appendUnsavedActionsToFile();
        }
    }

    private void appendUnsavedActionsToFile() throws IOException {
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

    private void saveCompactedFile() throws IOException {
        if (!lockableFile.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot save compacted cache file when not locked");
        }

        final byte[] content = entries.stream()
                .map(LeastRecentlyUsedCacheAction::createAddActionFor)
                .map(LeastRecentlyUsedCacheAction::serialize)
                .collect(Collectors.joining("\n"))
                .concat("\n")
                .getBytes(UTF_8);

        try (FileOutputStream out = new FileOutputStream(lockableFile.getFile())) {
            out.write(content);
            out.flush();
        }

        lastLoadOrStore = System.currentTimeMillis();
        requestCompression = false;
        unsavedActions.clear();
    }

    void lock() throws IOException {
        lockableFile.lock();
    }

    void unlock() throws IOException {
        lockableFile.unlock();
    }

    private boolean hasNeverBeenLoaded() {
        return lastLoadOrStore == -1;
    }
}
