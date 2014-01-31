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
import net.sourceforge.jnlp.runtime.*;
import net.sourceforge.jnlp.util.*;

/**
 * Describes an entry in the cache.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.10 $
 */
public class CacheEntry {

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
     * Initialize the cache entry data from a connection to the
     * remote resource (does not store data).
     */
    void initialize(URLConnection connection) {
        long modified = connection.getLastModified();
        long length = connection.getContentLength(); // an int

        properties.setProperty("content-length", Long.toString(length));
        properties.setProperty("last-modified", Long.toString(modified));
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
     */
    public long getLastUpdated() {
        try {
            return Long.parseLong(properties.getProperty("last-updated"));
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Sets the time in the local system clock that the file was
     * most recently checked for an update.
     */
    public void setLastUpdated(long updatedTime) {
        properties.setProperty("last-updated", Long.toString(updatedTime));
    }

    /**
     * Returns whether there is a version of the URL contents in
     * the cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param connection a connection to the remote URL
     * @return whether the cache contains the version
     */
    public boolean isCurrent(URLConnection connection) {
        boolean cached = isCached();

        if (!cached)
            return false;

        try {
            long remoteModified = connection.getLastModified();
            long cachedModified = Long.parseLong(properties.getProperty("last-modified"));

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
        File localFile = CacheUtil.getCacheFile(location, version);
        if (!localFile.exists())
            return false;

        try {
            long cachedLength = localFile.length();
            long remoteLength = Long.parseLong(properties.getProperty("content-length", "-1"));

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
