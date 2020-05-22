/* CacheLRUWrapperTest.java
   Copyright (C) 2012 Thomas Meyer

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheIndex.ConversionResult;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.PropertiesFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheIndex.KEY_HREF;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheIndex.KEY_LAST_ACCESSED;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheIndex.convertPropertiesToEntries;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeastRecentlyUsedCacheIndexHolderTest {

    private static URL url;
    private static final VersionId version = VersionId.fromString("1.0");
    private static final String entryId = "1-1";
    private static final int noEntriesCacheFile = 1000;

    private File recentlyUsedFile;
    private LeastRecentlyUsedCacheIndexHolder holder;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
    }

    @Before
    public void setup() throws IOException {
        final File cacheDir = temporaryFolder.newFolder();
        recentlyUsedFile = new File(cacheDir, ConfigurationConstants.CACHE_INDEX_FILE_NAME);
        holder = new LeastRecentlyUsedCacheIndexHolder(new DummyInfrastructureFileDescriptor(recentlyUsedFile));
    }

    @Test
    @Ignore("some of the CI machines are just too slow...")
    public void testConversionTiming() {
        final int noLoops = 1000;
        fillCacheIndexFile();

        final PropertiesFile propertiesFile = new PropertiesFile(recentlyUsedFile);
        propertiesFile.load();

        final long start = System.nanoTime();
        for (int i = 0; i < noLoops; i++) {
            final ConversionResult conversionResult = convertPropertiesToEntries(propertiesFile);
            assertFalse(conversionResult.propertiesNeedToBeStored);
            assertEquals(noEntriesCacheFile, conversionResult.entries.size());
        }
        final long end = System.nanoTime();

        long avg = (end - start) / noLoops / 1000;
        ServerAccess.logErrorReprint("Average = " + avg + "ns");

        // wait more than 5000 microseconds for noEntries=1000 is bad
        assertTrue("convertPropertiesToEntries() must not take longer than 5000µs, but took in avg " + avg + "µs", avg < 5000);
    }

    @Test
    public void testModTimestampAfterClear() throws InterruptedException {
        holder.runSynchronized(idx -> idx.createEntry(url, version, "1-1"));
        final long lmBefore = recentlyUsedFile.lastModified();

        // required as file system only stores seconds in lastModified()
        Thread.sleep(1010);

        holder.runSynchronized(LeastRecentlyUsedCacheIndex::clear);
        final long lmAfter = recentlyUsedFile.lastModified();

        assertTrue("modification timestamp hasn't changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore < lmAfter);
    }

    @Test
    public void testModTimestampAfterNoop() throws InterruptedException {
        holder.runSynchronized(idx -> idx.createEntry(url, version, "1-1"));
        final long lmBefore = recentlyUsedFile.lastModified();

        // required as file system only stores seconds in lastModified()
        Thread.sleep(1010);

        holder.runSynchronized(idx -> {
            // do nothing
        });
        final long lmAfter = recentlyUsedFile.lastModified();

        assertEquals("modification timestamp has changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore, lmAfter);
    }

    @Test
    public void testModTimestampAfterStore() throws InterruptedException {
        holder.runSynchronized(idx -> idx.createEntry(url, version, "1-1"));
        final long lmBefore = recentlyUsedFile.lastModified();

        // required as file system only stores seconds in lastModified()
        Thread.sleep(1010);

        fillCacheIndexFile();
        final long lmAfter = recentlyUsedFile.lastModified();

        assertTrue("modification timestamp hasn't changed! Before = " + lmBefore + " After = " + lmAfter, lmBefore < lmAfter);
    }

    @Test
    public void testAddEntry() {
        holder.runSynchronized(idx -> idx.createEntry(url, version, entryId));
        final Optional<LeastRecentlyUsedCacheEntry> entry = holder.getSynchronized(idx -> idx.find(url, version));
        assertTrue(entry.isPresent());
        assertEquals(entry.get().getResourceHref(), url);
        assertEquals(entry.get().getVersion(), version);
        assertEquals(entry.get().getId(), entryId);
    }

    @Test
    public void testRemoveEntry() {
        holder.runSynchronized(idx -> idx.createEntry(url, version, entryId));
        final Optional<LeastRecentlyUsedCacheEntry> entryOne = holder.getSynchronized(idx -> idx.find(url, version));
        assertTrue(entryOne.isPresent());

        holder.runSynchronized(idx -> idx.removeEntry(url, version));
        final Optional<LeastRecentlyUsedCacheEntry> entryTwo = holder.getSynchronized(idx -> idx.find(url, version));
        assertFalse(entryTwo.isPresent());
    }

    @Test
    public void testSortingOfIndex() throws IOException {
        // given
        final List<String> ids = Arrays.asList("1-9", "0-1", "0-0", "1-3", "0-5");
        prefillRecentlyUsedFile(ids);

        final PropertiesFile propertiesFile = new PropertiesFile(recentlyUsedFile);
        propertiesFile.load();

        // when
        final List<String> result = convertPropertiesToEntries(propertiesFile).entries.stream()
                .map(LeastRecentlyUsedCacheEntry::getId)
                .collect(Collectors.toList());

        assertEquals(ids, result);
    }

    private void fillCacheIndexFile() {
        // fill cache index file
        holder.runSynchronized(idx -> {
            for (int i = 0; i < noEntriesCacheFile; i++) {
                final VersionId v = VersionId.fromString(Integer.toString(i));
                idx.createEntry(url, v, "2-" + i);
            }
        });
    }

    private void prefillRecentlyUsedFile(List<String> ids) throws IOException {
        final LocalDateTime now = LocalDateTime.now();
        final Properties props = new Properties();
        for (int i = 0; i < ids.size(); i++) {
            final String id = ids.get(i);
            final long lastAccessed = now.minusHours(i).toEpochSecond(UTC);
            props.setProperty(id + '.' + KEY_LAST_ACCESSED, Long.toString(lastAccessed));
            props.setProperty(id + '.' + KEY_HREF, url.toString());
        }

        props.store(new FileOutputStream(recentlyUsedFile), null);
    }

    private static class DummyInfrastructureFileDescriptor extends InfrastructureFileDescriptor {
        private final File backend;

        private DummyInfrastructureFileDescriptor(File backend) {
            super();
            this.backend = backend;
        }

        @Override
        public File getFile() {
            return backend;
        }

        @Override
        public String getFullPath() {
            return backend.getAbsolutePath();
        }
    }
}
