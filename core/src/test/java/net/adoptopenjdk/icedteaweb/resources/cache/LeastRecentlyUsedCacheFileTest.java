package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static net.adoptopenjdk.icedteaweb.io.FileUtils.loadFileAsUtf8String;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeastRecentlyUsedCacheFileTest {

    private static final URL RESOURCE_1_URL = url("https://test.com");
    private static final URL RESOURCE_2_URL = url("https://foo.com");
    private static final VersionId RESOURCE_1_VERSION = VersionId.fromString("1.1");
    private static final VersionId RESOURCE_2_VERSION = VersionId.fromString("2.2");
    private static final String CACHE_ID_1 = "1/11";
    private static final String CACHE_ID_2 = "2/22";
    public static final long LAST_ACCESSED_1 = 1234;
    public static final long LAST_ACCESSED_2 = 3456;
    public static final long LAST_ACCESSED_3 = 5678;

    public static final LeastRecentlyUsedCacheEntry ENTRY_1 = new LeastRecentlyUsedCacheEntry(CACHE_ID_1, LAST_ACCESSED_1, new CacheKey(RESOURCE_1_URL, RESOURCE_1_VERSION));
    public static final LeastRecentlyUsedCacheEntry ENTRY_2 = new LeastRecentlyUsedCacheEntry(CACHE_ID_2, LAST_ACCESSED_2, new CacheKey(RESOURCE_2_URL, RESOURCE_2_VERSION));

    private LeastRecentlyUsedCacheFile cacheFile;
    private File physicalFile;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        physicalFile = temporaryFolder.newFile("cache_file");
        cacheFile = new LeastRecentlyUsedCacheFile(physicalFile);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotLoadWhenNotLocked() throws Exception {
        cacheFile.load();
    }

    @Test(expected = IllegalStateException.class)
    public void cannotLoadWhenUnLocked() throws Exception {
        cacheFile.lock();
        cacheFile.unlock();
        cacheFile.load();
    }

    @Test(expected = IllegalStateException.class)
    public void cannotReadEntriesWhenNotLoaded() {
        cacheFile.getAllEntries();
    }

    @Test
    public void loadingAnEmptyFileCreatesNoEntries() throws Exception {
        loadFile("");
        final List<LeastRecentlyUsedCacheEntry> result = cacheFile.getAllEntries();

        assertTrue(result.isEmpty());
    }

    @Test
    public void loadingNonEmptyFileWillApplyAppropriateActions() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");
        final List<LeastRecentlyUsedCacheEntry> result = cacheFile.getAllEntries();

        assertEqualEntries(asList(ENTRY_1), result);
    }

    @Test
    public void loadingNonEmptyFileWillApplyAppropriateActions2() throws Exception {
        loadFile(
                "::i=1/11::l=https://test.com::v=1.1::a=1234::",
                "::i=1/11::a=3456::"
        );
        final List<LeastRecentlyUsedCacheEntry> result = cacheFile.getAllEntries();

        final LeastRecentlyUsedCacheEntry updatedEntry1 = new LeastRecentlyUsedCacheEntry(ENTRY_1.getId(), LAST_ACCESSED_2, ENTRY_1.getCacheKey());
        assertEqualEntries(asList(updatedEntry1), result);
    }

    @Test
    public void loadingNonEmptyFileWillApplyAppropriateActions3() throws Exception {
        loadFile(
                "::i=1/11::l=https://test.com::v=1.1::a=1234::",
                "::i=2/22::l=https://test.com::v=1.1::a=1234::",
                "!2/22"
        );
        final List<LeastRecentlyUsedCacheEntry> result = cacheFile.getAllEntries();

        assertEqualEntries(asList(ENTRY_1), result);
    }

    @Test
    public void addedEntriesAreSortedByLastAccess() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);

        assertEqualEntries(asList(ENTRY_2, ENTRY_1), cacheFile.getAllEntries());
    }

    @Test
    public void addedEntriesAreSortedByLastAccess2() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_2);
        cacheFile.addEntry(ENTRY_1);

        assertEqualEntries(asList(ENTRY_2, ENTRY_1), cacheFile.getAllEntries());
    }

    @Test
    public void markingEntry1AsAccessedChangesSortOrder() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.markAccessed(ENTRY_1, LAST_ACCESSED_3);

        final LeastRecentlyUsedCacheEntry updatedEntry1 = new LeastRecentlyUsedCacheEntry(ENTRY_1.getId(), LAST_ACCESSED_3, ENTRY_1.getCacheKey());
        assertEqualEntries(asList(updatedEntry1, ENTRY_2), cacheFile.getAllEntries());
    }

    @Test
    public void markingEntry2AsAccessedChangesSortOrder() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.markAccessed(ENTRY_2, LAST_ACCESSED_3);

        final LeastRecentlyUsedCacheEntry updatedEntry2 = new LeastRecentlyUsedCacheEntry(ENTRY_2.getId(), LAST_ACCESSED_3, ENTRY_2.getCacheKey());
        assertEqualEntries(asList(updatedEntry2, ENTRY_1), cacheFile.getAllEntries());
    }

    @Test
    public void removingEntry1TakesItOutOfTheList() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.removeEntry(ENTRY_1);

        assertEqualEntries(asList(ENTRY_2), cacheFile.getAllEntries());
    }

    @Test
    public void removingEntry2TakesItOutOfTheList() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.removeEntry(ENTRY_2);

        assertEqualEntries(asList(ENTRY_1), cacheFile.getAllEntries());
    }

    @Test
    public void initiallyTheFileIsNotDirty() throws Exception {
        loadFile();

        assertFalse(cacheFile.isDirty());
    }

    @Test
    public void initiallyTheNonEmptyFileIsNotDirty() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");

        assertFalse(cacheFile.isDirty());
    }

    @Test
    public void addingAnEntryMakesTheFileDirty() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);

        assertTrue(cacheFile.isDirty());
    }

    @Test
    public void removingAnExistingEntryMakesTheFileDirty() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");

        cacheFile.removeEntry(ENTRY_1);

        assertTrue(cacheFile.isDirty());
    }

    @Test
    public void removingANonExistingEntryMakesTheFileNotDirty() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");

        cacheFile.removeEntry(ENTRY_2);

        assertFalse(cacheFile.isDirty());
    }

    @Test
    public void accessingAnExistingEntryMakesTheFileDirty() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");

        cacheFile.markAccessed(ENTRY_1, LAST_ACCESSED_3);

        assertTrue(cacheFile.isDirty());
    }

    @Test
    public void accessingANonExistingEntryMakesTheFileNotDirty() throws Exception {
        loadFile("::i=1/11::l=https://test.com::v=1.1::a=1234::");

        cacheFile.markAccessed(ENTRY_2, LAST_ACCESSED_3);

        assertFalse(cacheFile.isDirty());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotLoadDirtyFile() throws Exception {
        loadFile("");
        cacheFile.addEntry(ENTRY_1);

        try {
            cacheFile.lock();
            cacheFile.load();
        } finally {
            cacheFile.unlock();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void cannotPersistIfNotLocked() throws Exception {
        loadFile();
        cacheFile.addEntry(ENTRY_1);
        cacheFile.persistChanges();
    }

    @Test
    public void persistingClearsTheDirtyState() throws Exception {
        loadFile();
        cacheFile.addEntry(ENTRY_1);

        try {
            cacheFile.lock();
            cacheFile.persistChanges();
        }
        finally {
            cacheFile.unlock();
        }

        assertFalse(cacheFile.isDirty());
    }

    @Test
    public void persistingDoesAddALineForEveryChange() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.markAccessed(ENTRY_1, LAST_ACCESSED_3);
        cacheFile.removeEntry(ENTRY_2);

        try {
            cacheFile.lock();
            cacheFile.persistChanges();
        }
        finally {
            cacheFile.unlock();
        }

        assertFileContent(physicalFile,
                "::i=1/11::l=https://test.com::v=1.1::a=1234::",
                "::i=2/22::l=https://foo.com::v=2.2::a=3456::",
                "::i=1/11::a=5678::",
                "!2/22");
    }

    @Test(expected = IllegalStateException.class)
    public void cannotSaveCompactedIfNotLocked() throws Exception {
        loadFile();
        cacheFile.addEntry(ENTRY_1);
        cacheFile.saveCompactedFile();
    }

    @Test
    public void saveCompactedClearsTheDirtyState() throws Exception {
        loadFile();
        cacheFile.addEntry(ENTRY_1);

        try {
            cacheFile.lock();
            cacheFile.saveCompactedFile();
        }
        finally {
            cacheFile.unlock();
        }

        assertFalse(cacheFile.isDirty());
    }

    @Test
    public void saveCompactedDoesAddALineForEveryEntry() throws Exception {
        loadFile();

        cacheFile.addEntry(ENTRY_1);
        cacheFile.addEntry(ENTRY_2);
        cacheFile.markAccessed(ENTRY_1, LAST_ACCESSED_3);
        cacheFile.removeEntry(ENTRY_2);

        try {
            cacheFile.lock();
            cacheFile.saveCompactedFile();
        }
        finally {
            cacheFile.unlock();
        }

        assertFileContent(physicalFile, "::i=1/11::l=https://test.com::v=1.1::a=5678::");
    }

    private void loadFile(String... lines) throws IOException {
        try (FileOutputStream out = new FileOutputStream(physicalFile)) {
            out.write(String.join("\n", lines).getBytes(UTF_8));
        }

        try {
            cacheFile.lock();
            cacheFile.load();
        } finally {
            cacheFile.unlock();
        }
    }

    private void assertEqualEntries(List<LeastRecentlyUsedCacheEntry> expected, List<LeastRecentlyUsedCacheEntry> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            final LeastRecentlyUsedCacheEntry expectedEntry = expected.get(0);
            final LeastRecentlyUsedCacheEntry actualEntry = actual.get(0);

            assertEquals(expectedEntry.getId(), actualEntry.getId());
            assertEquals(expectedEntry.getLastAccessed(), actualEntry.getLastAccessed());
            assertEquals(expectedEntry.getCacheKey(), actualEntry.getCacheKey());
        }
    }

    private void assertFileContent(File actual, String... expectedLines) throws IOException {
        final String[] actualLines = loadFileAsUtf8String(actual).split("\\R");
        assertArrayEquals(expectedLines, actualLines);
    }

    private static URL url(final String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
