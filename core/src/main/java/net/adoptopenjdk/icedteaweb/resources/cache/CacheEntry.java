// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.io.File;
import java.net.URL;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Describes an entry in the cache.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.10 $
 */
class CacheEntry implements ResourceInfo {

    private static final Logger LOG = LoggerFactory.getLogger(CacheEntry.class);

    static final String INFO_SUFFIX = ".info";

    private static final String KEY_SIZE = "content-length";
    private static final String KEY_LAST_MODIFIED = "last-modified";
    private static final String KEY_DOWNLOADED_AT = "last-updated";
    static final String KEY_JNLP_PATH = "jnlp-path";

    /** the remote resource location */
    private final URL location;

    /** the requested version */
    private final VersionId version;

    /** the cache file */
    private final File cacheFile;

    /** info about the cached file */
    private final PropertiesFile properties;

    CacheEntry(LeastRecentlyUsedCacheEntry lruEntry, File cacheFile, File infoFile) {
        this(lruEntry.getResourceHref(), lruEntry.getVersion(), cacheFile, infoFile);
    }

    CacheEntry(URL location, VersionId version, File cacheFile, File infoFile) {
        this.location = location;
        this.version = version;
        this.cacheFile = cacheFile;
        properties = new PropertiesFile(infoFile, R("CAutoGen"));
    }

    /**
     * Returns the remote location this entry caches.
     * @return URL same as the one on which this entry was created
     */
    @Override
    public URL getResourceHref() {
        return location;
    }

    @Override
    public VersionId getVersion() {
        return version;
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

    /**
     * Sets the time in the local system clock that the file was
     * most recently checked for an update.
     * @param updatedTime the time (in ms) to be set as last updated time
     */
    void setDownloadedAt(long updatedTime) {
        setLongKey(KEY_DOWNLOADED_AT, updatedTime);
    }

    @Override
    public long getSize() {
        return getLongKey(KEY_SIZE);
    }

    void setSize(long length) {
        setLongKey(KEY_SIZE, length);
    }

    @Override
    public long getLastModified() {
        return getLongKey(KEY_LAST_MODIFIED);
    }

    void setLastModified(long modifyTime) {
        setLongKey(KEY_LAST_MODIFIED, modifyTime);
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
        LOG.debug("{} - version {}: isCached {}", location, version, cached);

        if (!cached) {
            return false;
        }
        try {
            long cachedModified = getLastModified();
            final boolean isCurrent = lastModified > 0 && lastModified <= cachedModified;
            LOG.debug("{} - version {}: lastModified cache:{} actual:{} -> {}", location, version, cachedModified, lastModified, isCurrent);
            return isCurrent;
        } catch (Exception ex){
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return false;
        }
    }

    /**
     * Returns true if the cache has a local copy of the contents
     * of the URL matching the specified version string.
     *
     * @return true if the resource is in the cache
     */
    boolean isCached() {
        if (!cacheFile.exists()) {
            return false;
        }

        if (! properties.containsKey(KEY_SIZE)) {
            return false;
        }

        try {
            long cachedLength = cacheFile.length();
            long remoteLength = getSize();
            final boolean isCached = cachedLength == remoteLength;

            LOG.debug("isCached: remote size:{} cached size:{} -> {}", remoteLength, cachedLength, isCached);
            return isCached;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);

            return false; // should throw?
        }
    }

    /**
     * Save the current information for the cache entry.
     *
     * @return True if successfully stored into file, false otherwise
     */
    boolean store() {
        if (properties.isHeldByCurrentThread()) {
            properties.store();
            return true;
        } else {
            return false;
        }
    }

    void storeInfo(ResourceInfo info) {
        lock();
        try {
            setLongKey(KEY_SIZE, info.getSize());
            setLongKey(KEY_LAST_MODIFIED, info.getLastModified());
            setLongKey(KEY_DOWNLOADED_AT, info.getDownloadedAt());

            final String jnlpPath = JNLPRuntime.getJnlpPath();
            if (StringUtils.isBlank(jnlpPath)) {
                LOG.info("Not-setting jnlp-path for missing main/jnlp argument");
            } else {
                properties.setProperty(KEY_JNLP_PATH, jnlpPath);
            }

            store();
        } finally {
            unlock();
        }
    }

    /**
     * Lock cache item.
     */
    void lock() {
        properties.lock();
    }

    /**
     * Unlock cache item. Does not do anything if not holding the lock.
     */
    void unlock() {
        properties.unlock();
    }

    boolean tryLock() {
        return properties.tryLock();
    }

    boolean isHeldByCurrentThread() {
        return properties.isHeldByCurrentThread();
    }

    @Override
    public String toString() {
        return cacheFile.getName();
    }
}
