/* CacheEntryTest -- unit test for CacheEntry
   Copyright (C) 2014 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import net.sourceforge.jnlp.Version;

import org.junit.Before;
import org.junit.Test;

public class CacheEntryTest {

    /** A custom subclass that allows supplying a predefined cache file */
    static class TestCacheEntry extends CacheEntry {
        private File cacheFile;
        public TestCacheEntry(URL location, Version version, File cacheFile) {
            super(location, version);
            this.cacheFile = cacheFile;
        }
        @Override
        protected File getCacheFile() {
            return cacheFile;
        }
    }

    private URL url;
    private Version version;

    @Before
    public void setUp() throws MalformedURLException {
        url = new URL("http://example.com/example.jar");
        version = new Version("1.0");
    }

    @Test
    public void verifyLocationIsSame() {
        CacheEntry entry = new TestCacheEntry(url, version, null);
        assertEquals(url, entry.getLocation());
    }

    @Test
    public void verifyLastModifiedIsSetCorrectly() {
        long LAST_MODIFIED = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setLastModified(LAST_MODIFIED);

        assertEquals(LAST_MODIFIED, entry.getLastModified());
    }

    @Test
    public void verifyLastUpdatedIsSetCorrectly() {
        long LAST_UPDATED = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setLastUpdated(LAST_UPDATED);

        assertEquals(LAST_UPDATED, entry.getLastUpdated());
    }

    @Test
    public void verifyContentLengthIsSetCorrectly() {
        long CONTENT_LENGTH = 1000;

        CacheEntry entry = new TestCacheEntry(url, version, null);
        entry.setRemoteContentLength(CONTENT_LENGTH);

        assertEquals(CONTENT_LENGTH, entry.getRemoteContentLength());
    }

    @Test
    public void verifyNotCachedIfFileIsAbsent() {
        File doesNotExist = new File("/foo/bar/baz/spam/eggs");

        CacheEntry entry = new TestCacheEntry(url, version, doesNotExist);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyNotCachedIfContentLengthsDiffer() throws IOException {
        File cachedFile = createCacheFile("Foo");

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(10000);

        assertFalse(entry.isCached());
    }

    @Test
    public void verifyCachedIfContentLengthsAreSame() throws IOException {
        String contents = "Foo";
        File cachedFile = createCacheFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());

        assertTrue(entry.isCached());
    }

    @Test
    public void verifyCurrentWhenCacheEntryHasSameTimeStamp() throws IOException {
        long lastModified = 10;
        String contents = "Foo";
        File cachedFile = createCacheFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());
        entry.setLastModified(lastModified);

        assertTrue(entry.isCurrent(lastModified));
    }

    @Test
    public void verifyNotCurrentWhenRemoteContentIsNewer() throws IOException {
        long oldTimeStamp = 10;
        long newTimeStamp = 100;
        String contents = "Foo";
        File cachedFile = createCacheFile(contents);

        CacheEntry entry = new TestCacheEntry(url, version, cachedFile);
        entry.setRemoteContentLength(contents.length());
        entry.setLastModified(oldTimeStamp);

        assertFalse(entry.isCurrent(newTimeStamp));
    }

    protected File createCacheFile(String contents) throws IOException {
        File cachedFile = File.createTempFile("CacheEntryTest", null);
        Files.write(cachedFile.toPath(), contents.getBytes());
        cachedFile.deleteOnExit();
        return cachedFile;
    }

}
