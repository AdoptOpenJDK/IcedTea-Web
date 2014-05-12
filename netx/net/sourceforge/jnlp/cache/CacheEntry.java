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

package net.sourceforge.jnlp.cache;

import net.sourceforge.jnlp.util.logging.OutputController;
import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.*;
import java.net.*;

import net.sourceforge.jnlp.*;
import net.sourceforge.jnlp.util.*;

/**
 * Describes an entry in the cache.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.10 $
 */
public class CacheEntry {

    private static final String KEY_CONTENT_LENGTH = "content-length";
    private static final String KEY_LAST_MODIFIED = "last-modified";
    private static final String KEY_LAST_UPDATED = "last-updated";

    /** the remote resource location */
    private URL location;

    /** the requested version */
    private Version version;

    /** info about the cached file */
    private PropertiesFile properties;

    /**
     * Create a CacheEntry for the resources specified as a remote
     * URL.
     *
     * @param location the remote resource location
     * @param version the version of the resource
     */
    public CacheEntry(URL location, Version version) {
        this.location = location;
        this.version = version;

        File infoFile = CacheUtil.getCacheFile(location, version);
        infoFile = new File(infoFile.getPath() + ".info"); // replace with something that can't be clobbered

        properties = new PropertiesFile(infoFile, R("CAutoGen"));
    }

    /**
     * Returns the remote location this entry caches.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the time in the local system clock that the file was
     * most recently checked for an update.
     * @return 
     */
    public long getLastUpdated() {
        return getLongKey(KEY_LAST_UPDATED);
    }

    /**
     * Sets the time in the local system clock that the file was
     * most recently checked for an update.
     * @param updatedTime
     */
    public void setLastUpdated(long updatedTime) {
        setLongKey(KEY_LAST_UPDATED, updatedTime);
    }

    public long getRemoteContentLength() {
        return getLongKey(KEY_CONTENT_LENGTH);
    }

    public void setRemoteContentLength(long length) {
        setLongKey(KEY_CONTENT_LENGTH, length);
    }

    public long getLastModified() {
        return getLongKey(KEY_LAST_MODIFIED);
    }

    public void setLastModified(long modifyTime) {
        setLongKey(KEY_LAST_MODIFIED, modifyTime);
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

    /**
     * Returns whether there is a version of the URL contents in
     * the cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param lastModified
     * @return whether the cache contains the version
     */
    public boolean isCurrent(long lastModified) {
        boolean cached = isCached();

        if (!cached)
            return false;

        try {
            long remoteModified = lastModified;
            long cachedModified = Long.parseLong(properties.getProperty(KEY_LAST_MODIFIED));

            if (remoteModified > 0 && remoteModified <= cachedModified)
                return true;
            else
                return false;
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);;

            return cached; // if can't connect return whether already in cache
        }
    }

    /**
     * Returns true if the cache has a local copy of the contents
     * of the URL matching the specified version string.
     *
     * @return true if the resource is in the cache
     */
    public boolean isCached() {
        File localFile = getCacheFile();
        if (!localFile.exists())
            return false;

        try {
            long cachedLength = localFile.length();
            long remoteLength = Long.parseLong(properties.getProperty(KEY_CONTENT_LENGTH, "-1"));

            if (remoteLength >= 0 && cachedLength != remoteLength)
                return false;
            else
                return true;
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);

            return false; // should throw?
        }
    }

    /**
     * Seam for testing
     */
    File getCacheFile() {
        return CacheUtil.getCacheFile(location, version);
    }

    /**
     * Save the current information for the cache entry.
     */
    protected void store() {
        properties.store();
    }

    /**
     * Mark this entry for deletion at shutdown.
     */
    public void markForDelete() { // once marked it should not be unmarked.
        properties.setProperty("delete", Boolean.toString(true));
    }

    /**
     * Lock cache item.
     */
    protected void lock() {
        CacheUtil.lockFile(properties);
    }

    /**
     * Unlock cache item.
     */
    protected void unlock() {
        CacheUtil.unlockFile(properties);
    }

}
