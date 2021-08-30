/* CacheEntryTest -- unit test for CacheEntry
   Copyright (C) 2014 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CacheEntryTest {

    private static URL url;
    private static VersionId version;
    private static long downloadedAt;

    private File cacheFile;
    private File infoFile;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
        version = VersionId.fromString("1.0");
        downloadedAt = System.currentTimeMillis();
    }

    @Before
    public void setUp() throws IOException {
        final File tmpDir = temporaryFolder.newFolder();
        cacheFile = new File(tmpDir, "example.jar");
        infoFile = new File(tmpDir, CacheEntry.INFO_SUFFIX);

        FileUtils.saveFileUtf8("", infoFile);
        FileUtils.saveFileUtf8("Some content in cache file", cacheFile);
    }

    @Test
    public void verifyLocationIsSame() {
        assertEquals(url, createEntry().getCacheKey().getLocation());
    }

    @Test
    public void verifyDataIsSetCorrectly() {
        long LAST_MODIFIED = 999;
        long DOWNLOADED_AT = 888;
        long CONTENT_LENGTH = 777;

        final CacheEntry firstEntry = createEntry();
        assertNotEquals(LAST_MODIFIED, firstEntry.getLastModified());
        assertNotEquals(DOWNLOADED_AT, firstEntry.getDownloadedAt());
        assertNotEquals(CONTENT_LENGTH, firstEntry.getSize());

        firstEntry.storeInfo(DOWNLOADED_AT, LAST_MODIFIED, CONTENT_LENGTH);

        final CacheEntry secondEntry = createEntry();
        assertEquals(LAST_MODIFIED, secondEntry.getLastModified());
        assertEquals(DOWNLOADED_AT, secondEntry.getDownloadedAt());
        assertEquals(CONTENT_LENGTH, secondEntry.getSize());
    }

    @Test
    public void verifyCachedIfFileExistsAndLengthIsSame() {
        final CacheEntry entry = createEntry();
        entry.storeInfo(downloadedAt, cacheFile.lastModified(), cacheFile.length());

        assertTrue(entry.isCached());
    }


    @Test
    public void verifyNotCachedIfFileIsAbsent() {
        assertTrue(cacheFile.delete());
        assertFalse(createEntry().isCached());
    }

    @Test
    public void verifyNotCachedIfContentLengthsDiffer() {
        final CacheEntry entry = createEntry();
        entry.storeInfo(downloadedAt, cacheFile.lastModified(), cacheFile.length() + 1);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyCurrentWhenCacheEntryHasSameTimeStamp() {
        final CacheEntry entry = createEntry();
        entry.storeInfo(downloadedAt, cacheFile.lastModified(), cacheFile.length());

        assertTrue(entry.isCurrent(cacheFile.lastModified()));
    }

    @Test
    public void verifyCurrentWhenRemoteContentIsOlder() {
        final CacheEntry entry = createEntry();
        entry.storeInfo(downloadedAt, cacheFile.lastModified(), cacheFile.length());

        assertTrue(entry.isCurrent(cacheFile.lastModified() - 10));
    }

    @Test
    public void verifyNotCurrentWhenRemoteContentIsNewer() {
        final CacheEntry entry = createEntry();
        entry.storeInfo(downloadedAt, cacheFile.lastModified(), cacheFile.length());

        assertFalse(entry.isCurrent(cacheFile.lastModified() + 10));
    }

    private CacheEntry createEntry() {
        return new CacheEntry(new CacheKey(url, version), cacheFile, infoFile);
    }
}
