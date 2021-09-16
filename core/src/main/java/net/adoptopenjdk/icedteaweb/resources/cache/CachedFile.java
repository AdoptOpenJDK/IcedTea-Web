// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.io.File;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Describes an entry in the cache.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.10 $
 */
class CachedFile implements ResourceInfo {

    private static final Logger LOG = LoggerFactory.getLogger(CachedFile.class);

    static final String INFO_SUFFIX = ".info";

    private static final String KEY_SIZE = "content-length";
    private static final String KEY_LAST_MODIFIED = "last-modified";
    private static final String KEY_DOWNLOADED_AT = "last-updated";
    static final String KEY_JNLP_PATH = "jnlp-path";

    private final CacheKey key;

    /** the cache file */
    private final File cacheFile;

    /** info about the cached file */
    private final PropertiesFile properties;

    CachedFile(CacheIndexEntry lruEntry, File cacheFile, File infoFile) {
        this(lruEntry.getCacheKey(), cacheFile, infoFile);
    }

    CachedFile(CacheKey key, File cacheFile, File infoFile) {
        this.key = key;
        this.cacheFile = cacheFile;
        this.properties = new PropertiesFile(infoFile, R("CAutoGen"));
    }

    @Override
    public CacheKey getCacheKey() {
        return key;
    }

    /**
     * Returns the time in the local system clock that the file was
     * most recently checked for an update.
     * @return when the item was updated (in ms)
     */
    @Override
    public long getDownloadedAt() {
        return getLongKey(KEY_DOWNLOADED_AT);
    }

    @Override
    public long getSize() {
        return getLongKey(KEY_SIZE);
    }

    @Override
    public long getLastModified() {
        return getLongKey(KEY_LAST_MODIFIED);
    }

    String getJnlpPath() {
        return properties.getProperty(KEY_JNLP_PATH);
    }

    File getCacheFile() {
        return cacheFile;
    }

    private long getLongKey(String key) {
        try {
            return Long.parseLong(properties.getProperty(key));
        } catch (Exception ex) {
            return 0;
        }
    }

    private void setLongKey(String key, long value) {
        properties.setProperty(key, Long.toString(value));
    }

    boolean exists() {
        return properties.getStoreFile().isFile();
    }

    /**
     * Returns whether there is a version of the URL contents in
     * the cache and it is up to date.
     *
     * @param lastModified - current time as get from server (in ms). Mostly value of "Last-Modified" http header'?
     * @return whether the cache contains the version
     */
    boolean isCurrent(long lastModified) {
        boolean cached = isCached();
        LOG.debug("{}: isCached {}", key, cached);

        if (!cached) {
            return false;
        }
        try {
            long cachedModified = getLastModified();
            final boolean isCurrent = lastModified > 0 && lastModified <= cachedModified;
            LOG.debug("{}: lastModified cache:{} actual:{} -> {}", key, cachedModified, lastModified, isCurrent);
            return isCurrent;
        } catch (Exception ex){
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return false;
        }
    }

    /**
     * Returns true if the cache has a local copy of the resource.
     *
     * @return true if the resource is in the cache
     */
    boolean isCached() {
        try {
            if (!cacheFile.exists() || !properties.containsPropertyKey(KEY_SIZE)) {
                return false;
            }

            long actualFileSize = cacheFile.length();
            long storedFileSize = getSize();
            final boolean hasExpectedSize = actualFileSize == storedFileSize;

            if (hasExpectedSize) {
                return true;
            }

            LOG.warn("expected {} to have size {} but found file size to be {}", cacheFile, storedFileSize, actualFileSize);
        } catch (Exception ex) {
            LOG.error("Unexpected exception", ex);
        }
        return false; // should throw?
    }

    void storeInfo(long downloadedAt, long lastModified, long size) {
        properties.lock();
        try {
            setLongKey(KEY_SIZE, size);
            setLongKey(KEY_LAST_MODIFIED, lastModified);
            setLongKey(KEY_DOWNLOADED_AT, downloadedAt);

            final String jnlpPath = JNLPRuntime.getJnlpPath();
            if (StringUtils.isBlank(jnlpPath)) {
                LOG.info("Not-setting jnlp-path for missing main/jnlp argument");
            } else {
                properties.setProperty(KEY_JNLP_PATH, jnlpPath);
            }

            if (properties.isHeldByCurrentThread()) {
                properties.store();
            }
        } finally {
            properties.unlock();
        }
    }

    @Override
    public String toString() {
        return cacheFile.getName();
    }
}
