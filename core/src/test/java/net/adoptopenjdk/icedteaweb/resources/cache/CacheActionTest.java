package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.util.Arrays.asList;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.DO_NOTHING;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.createAccessActionFor;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.createAddActionFor;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.createRemoveActionFor;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CacheActionTest {

    private static final String RESOURCE_1_URL = "https://test.com";
    private static final String RESOURCE_1_VERSION = "1.1";
    private static final String CACHE_ID_1 = "1/11";
    public static final long LAST_ACCESSED_1 = 1234;
    public static final long LAST_ACCESSED_2 = 3456;

    public static final CacheKey CACHE_KEY_1 = new CacheKey(url(RESOURCE_1_URL), VersionId.fromString(RESOURCE_1_VERSION));
    public static final CacheIndexEntry ENTRY_1 = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_1, CACHE_KEY_1);

    private CacheIndexEntries entries;

    @Before
    public void setup() throws IOException {
        entries = new CacheIndexEntries();
    }

    @Test
    public void addActionAppliedToListWillAddEntry() {
        final CacheAction addAction = createAddActionFor(ENTRY_1);
        addAction.applyTo(entries);

        assertEqualEntries(asList(ENTRY_1), entries);
    }

    @Test
    public void removeActionAppliedToListWillRemoveEntry() {
        entries.addEntry(ENTRY_1);

        final CacheAction addAction = createRemoveActionFor(ENTRY_1.getId());
        addAction.applyTo(entries);

        assertTrue(entries.getAllEntries().isEmpty());
    }

    @Test
    public void updateAccessTimeActionAppliedToListWillChangeTheAccessTimeOfTheEntry() {
        entries.addEntry(ENTRY_1);

        final CacheAction addAction = createAccessActionFor(ENTRY_1.getId(), LAST_ACCESSED_2);
        addAction.applyTo(entries);

        final CacheIndexEntry modifiedEntry1 = new CacheIndexEntry(ENTRY_1.getId(), LAST_ACCESSED_2, ENTRY_1.getCacheKey());
        assertEqualEntries(asList(modifiedEntry1), entries);
    }

    @Test
    public void doNothingShouldDoNothing() {
        entries.addEntry(ENTRY_1);

        DO_NOTHING.applyTo(entries);

        assertEqualEntries(asList(ENTRY_1), entries);
    }

    @Test
    public void serializeAddAction() {
        final CacheAction addAction = createAddActionFor(ENTRY_1);
        final String result = addAction.serialize();

        assertEquals("::i=1/11::l=https://test.com::v=1.1::a=1234::", result);
    }

    @Test
    public void serializeAddActionWithoutVersion() {
        final CacheKey key = new CacheKey(url(RESOURCE_1_URL), null);
        final CacheIndexEntry entryWithoutVersion = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_1, key);
        final CacheAction addAction = createAddActionFor(entryWithoutVersion);
        final String result = addAction.serialize();

        assertEquals("::i=1/11::l=https://test.com::a=1234::", result);
    }

    @Test
    public void serializeAddActionWithDelimiter() {
        final CacheKey key = new CacheKey(url("https://test.com#::vv"), VersionId.fromString("2::3"));
        final CacheIndexEntry entryWithDelimiter = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_1, key);
        final CacheAction addAction = createAddActionFor(entryWithDelimiter);
        final String result = addAction.serialize();

        assertEquals("::i=1/11::l=https://test.com#::::vv::v=2::::3::a=1234::", result);
    }

    @Test
    public void serializeRemoveAction() {
        final CacheAction removeAction = createRemoveActionFor(CACHE_ID_1);
        final String result = removeAction.serialize();

        assertEquals("!1/11!", result);
    }

    @Test
    public void serializeAccessAction() {
        final CacheAction accessAction = createAccessActionFor(CACHE_ID_1, LAST_ACCESSED_1);
        final String result = accessAction.serialize();

        assertEquals("::i=1/11::a=1234::", result);
    }

    @Test
    public void parseAddAction() {
        final CacheAction removeAction = parse("::i=1/11::l=https://test.com::v=1.1::a=1234::");
        removeAction.applyTo(entries);

        assertEqualEntries(asList(ENTRY_1), entries);
    }

    @Test
    public void parseAddActionWithDelimiter() {
        final CacheKey key = new CacheKey(url("https://test.com#::vv"), VersionId.fromString("2::3"));

        final CacheAction removeAction = parse("::i=1/11::l=https://test.com#::::vv::v=2::::3::a=1234::");
        removeAction.applyTo(entries);

        final CacheIndexEntry entryWithDelimiter = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_1, key);
        assertEqualEntries(asList(entryWithDelimiter), entries);
    }

    @Test
    public void parseAddActionWithMultipleDelimiter() {
        final CacheKey key = new CacheKey(url("https://test.com#::vv::ww"), VersionId.fromString("2::3"));

        final CacheAction removeAction = parse("::i=1/11::l=https://test.com#::::vv::::ww::v=2::::3::a=1234::");
        removeAction.applyTo(entries);

        final CacheIndexEntry entryWithDelimiter = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_1, key);
        assertEqualEntries(asList(entryWithDelimiter), entries);
    }

    @Test
    public void parseRemovalAction() {
        entries.addEntry(ENTRY_1);

        final CacheAction removeAction = parse("!1/11!");
        removeAction.applyTo(entries);

        assertTrue(entries.getAllEntries().isEmpty());
    }

    @Test
    public void parseAccessAction() {
        entries.addEntry(ENTRY_1);

        final CacheAction removeAction = parse("::i=1/11::a=3456::");
        removeAction.applyTo(entries);

        final CacheIndexEntry updatedEntry = new CacheIndexEntry(CACHE_ID_1, LAST_ACCESSED_2, CACHE_KEY_1);
        assertEqualEntries(asList(updatedEntry), entries);
    }

    @Test
    public void parseInvalidLinesShouldReturnDoNothingAction() {
        final List<String> invalidLines = asList(
                "",
                "::",
                "abcd",

                "!", // missing ID
                "!1/11", // missing postfix

                "::l=https://test.com::v=1.1::a=1234::", // missing ID
                "::i=::l=https://test.com::v=1.1::a=1234::", // empty ID

                "::i=1/11::v=1.1::a=1234::", // missing location
                "::i=1/11::l=::v=1.1::a=1234::", // empty location
                "::i=1/11::l=https/test.com::v=1.1::a=1234::", // invalid location

                "::i=1/11::l=https://test.com::v=::a=1234::", // empty version
                "::i=1/11::l=https://test.com::v=1*1::a=1234::", // invalid version

                "::i=1/11::l=https://test.com::v=1.1::a=::", // empty access time
                "::i=1/11::l=https://test.com::v=1.1::", // missing access time
                "::i=1/11::l=https://test.com::v=1.1::a=now::", // invalid access time

                "i=1/11::l=https://test.com::v=1.1::a=1234::", // missing prefix
                "::i=1/11::l=https://test.com::v=1.1::a=1234", // missing postfix
                null
        );

        for (final String line : invalidLines) {
            assertSame("line does not pars to DO_NOTHING: " + line, DO_NOTHING, parse(line));
        }
    }

    private static URL url(final String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertEqualEntries(List<CacheIndexEntry> expected, CacheIndexEntries actualEntries) {
        final List<CacheIndexEntry> actual = actualEntries.getAllEntries();
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            final CacheIndexEntry expectedEntry = expected.get(0);
            final CacheIndexEntry actualEntry = actual.get(0);

            assertEquals(expectedEntry.getId(), actualEntry.getId());
            assertEquals(expectedEntry.getLastAccessed(), actualEntry.getLastAccessed());
            assertEquals(expectedEntry.getCacheKey(), actualEntry.getCacheKey());
        }
    }
}
