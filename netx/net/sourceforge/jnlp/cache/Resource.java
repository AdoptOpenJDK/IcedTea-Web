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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.WeakList;

/**
 * <p>
 * Information about a single resource to download.
 * This class tracks the downloading of various resources of a
 * JNLP file to local files.  It can be used to download icons,
 * jnlp and extension files, jars, and jardiff files using the
 * version based protocol or any file using the basic download
 * protocol.
 * </p>
 * <p>
 * Resources can be put into download groups by specifying a part
 * name for the resource.  The resource tracker can also be
 * configured to prefetch resources, which are downloaded in the
 * order added to the media tracker.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.9 $
 */
public class Resource {
    // todo: fix resources to handle different versions

    // todo: IIRC, any resource is checked for being up-to-date
    // only once, regardless of UpdatePolicy.  verify and fix.

    public enum Status {
        PRECONNECT,
        CONNECTING,
        CONNECTED,
        PREDOWNLOAD,
        DOWNLOADING,
        DOWNLOADED,
        ERROR,
        PROCESSING // in queue or being worked on
    }

    /** list of weak references of resources currently in use */
    private static final WeakList<Resource> resources = new WeakList<>();

    /** weak list of trackers monitoring this resource */
    private final WeakList<ResourceTracker> trackers = new WeakList<>();

    /** the remote location of the resource */
    private final URL location;

    /** the location to use when downloading */
    private URL downloadLocation;

    /** the local file downloaded to */
    private File localFile;

    /** the requested version */
    private final Version requestVersion;

    /** the version downloaded from server */
    private Version downloadVersion;

    /** amount in bytes transferred */
    private volatile long transferred = 0;

    /** total size of the resource, or -1 if unknown */
    private volatile long size = -1;

    /** the status of the resource */
    private final EnumSet<Status> status = EnumSet.noneOf(Status.class);
    
    /** Update policy for this resource */
    private final UpdatePolicy updatePolicy;

    /** Download options for this resource */
    private DownloadOptions downloadOptions;

    /**
     * Create a resource.
     */
    private Resource(URL location, Version requestVersion, UpdatePolicy updatePolicy) {
        this.location = location;
        this.downloadLocation = location;
        this.requestVersion = requestVersion;
        this.updatePolicy = updatePolicy;
    }

    /**
     * Return a shared Resource object representing the given
     * location and version.
     * @param location final location of resource
     * @param requestVersion final version of resource
     * @param updatePolicy final policy for updating
     * @return new resource, which is already added in resources list
     */
    public static Resource getResource(URL location, Version requestVersion, UpdatePolicy updatePolicy) {
        //TODO -rename to create resource?
        synchronized (resources) {
            Resource resource = new Resource(location, requestVersion, updatePolicy);

            //FIXME - url ignores port during its comparison
            //this may affect test-suites
            int index = resources.indexOf(resource);
            if (index >= 0) { // return existing object
                Resource result = resources.get(index);
                if (result != null) {
                    return result;
                }
            }

            resources.add(resource);
            resources.trimToSize();

            return resource;
        }
    }

    /**
     * Returns the remote location of the resource.
     * @return the same location as the one with which this resource was created
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the URL to use for downloading the resource. This can be
     * different from the original location since it may use a different
     * file name to support versioning and compression
     * @return the url to use when downloading
     */
    public URL getDownloadLocation() {
        return downloadLocation;
    }

    /**
     * Set the url to use for downloading the resource
     * @param downloadLocation url to be donloaded
     */
    public void setDownloadLocation(URL downloadLocation) {
        this.downloadLocation = downloadLocation;
    }

    /**
     * Returns the tracker that first created or monitored the
     * resource, or null if no trackers are monitoring the resource.
     */
    ResourceTracker getTracker() {
        synchronized (trackers) {
            List<ResourceTracker> t = trackers.hardList();
            if (t.size() > 0) {
                return t.get(0);
            }

            return null;
        }
    }
    
    /**
     * @return the local file currently being downloaded
     */
    public File getLocalFile() {
    	return localFile;
    }
    
    /**
     * Sets the local file to be downloaded
     * @param localFile location of stored resource
     */
    public void setLocalFile(File localFile) {
    	this.localFile = localFile;
    }
    
    /**
     * @return the requested version
     */
    public Version getRequestVersion() {
    	return requestVersion;
    }
    
    /**
     * @return the version downloaded from server
     */
    public Version getDownloadVersion() {
    	return downloadVersion;
    }
    
    /**
     * Sets the version downloaded from server
     * @param downloadVersion version of downloaded resource
     */
    public void setDownloadVersion(Version downloadVersion) {
    	this.downloadVersion = downloadVersion;
    }
    
    /**
     * @return the amount in bytes transferred
     */
    public long getTransferred() {
    	return transferred;
    }
    
    /**
     * Sets the amount transferred
     * @param transferred set the whole transfered amount to this value
     */
    public void setTransferred(long transferred) {
    	this.transferred = transferred;
    }
    
    /**
     * Increments the amount transferred (in bytes)
     * @param incTrans transfered amount in last transfer
     */
    public void incrementTransferred(long incTrans) {
    	transferred += incTrans;
    }

    /**
     * Returns the size of the resource
     * @return size of resource (-1 if unknown)
     */
    public long getSize() {
    	return size;
    }

    /**
     * Sets the size of the resource
     * @param size desired size of resource
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the status of the resource
     */
    public Set<Status> getCopyOfStatus() {
        return EnumSet.copyOf(status);

    }

    /**
     * Check if the specified flag is set.
     * @param flag a status flag
     * @return true iff the flag is set
     */
    public boolean isSet(Status flag) {
        synchronized (status) {
            return status.contains(flag);
        }
    }

    /**
     * Check if all the specified flags are set.
     * @param flags a collection of flags
     * @return true iff all the flags are set
     */
    public boolean hasFlags(Collection<Status> flags) {
        synchronized (status) {
            return status.containsAll(flags);
        }
    }

    /**
     * @return the update policy for this resource
     */
    public UpdatePolicy getUpdatePolicy() {
        return this.updatePolicy;
    }

    /**
     * Returns a human-readable status string.
     */
    private String getStatusString() {
        StringBuilder result = new StringBuilder();

        synchronized (status) {
            if (status.isEmpty()) {
                return "<>";
            }
            for (Status stat : status) {
                result.append(stat.toString()).append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Changes the status by clearing the flags in the first
     * parameter and setting the flags in the second.  This method
     * is synchronized on this resource.
     * @param clear a collection of status flags to unset
     * @param add a collection of status flags to set
     */
    public void changeStatus(Collection<Status> clear, Collection<Status> add) {
        synchronized (status) {
            if (clear != null) {
                status.removeAll(clear);
            }
            if (add != null) {
                status.addAll(add);
            }
        }
    }

    /**
     * Set status flag
     * @param flag a flag to set
     */
    public void setStatusFlag(Status flag) {
        synchronized (status) {
            status.add(flag);
        }
    }

    /**
     * Set flags
     * @param flags a collection of flags to set
     */
    public void setStatusFlags(Collection<Status> flags) {
        synchronized (status) {
            status.addAll(flags);
        }
    }

    /**
     * Unset flags
     * @param flags a collection of flags to unset
     */
    public void unsetStatusFlag(Collection<Status> flags) {
        synchronized (status) {
            status.removeAll(flags);
        }
    }

    /**
     * Clear all flags
     */
    public void resetStatus() {
        synchronized (status) {
            status.clear();
        }
    }

    /**
     * Check if this resource has been initialized
     * @return true iff any flags have been set
     */
    public boolean isInitialized() {
        synchronized (status) {
            return !status.isEmpty();
        }
    }

    /**
     * Removes the tracker to the list of trackers monitoring this
     * resource.
     * 
     * @param tracker tracker to be removed
     */
    public void removeTracker(ResourceTracker tracker) {
        synchronized (trackers) {
            trackers.remove(tracker);
            trackers.trimToSize();
        }
    }

    /**
     * Adds the tracker to the list of trackers monitoring this
     * resource.
     * @param tracker to observing resource
     */
    public void addTracker(ResourceTracker tracker) {
        synchronized (trackers) {
            // prevent GC between contains and add
            List<ResourceTracker> t = trackers.hardList();
            if (!t.contains(tracker))
                trackers.add(tracker);

            trackers.trimToSize();
        }
    }

    /**
     * Instructs the trackers monitoring this resource to fire a
     * download event.
     */
    protected void fireDownloadEvent() {
        List<ResourceTracker> send;

        synchronized (trackers) {
            send = trackers.hardList();
        }

        for (ResourceTracker rt : send) {
            rt.fireDownloadEvent(this);
        }
    }

    public void setDownloadOptions(DownloadOptions downloadOptions) {
        this.downloadOptions = downloadOptions;
    }

    public DownloadOptions getDownloadOptions() {
        return this.downloadOptions;
    }

    public boolean isConnectable() {
        return JNLPRuntime.isConnectable(this.location);
    }

    @Override
    public int hashCode() {
        // FIXME: should probably have a better hashcode than this, but considering
        // #equals(Object) was already defined first (without also overriding hashcode!),
        // this is just being implemented in line with that so we don't break HashMaps,
        // HashSets, etc
        return location.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Resource) {
            // this prevents the URL handler from looking up the IP
            // address and doing name resolution; much faster so less
            // time spent in synchronized addResource determining if
            // Resource is already in a tracker, and better for offline
            // mode on some OS.
            return UrlUtils.urlEquals(location, ((Resource) other).location);
        }
        return false;
    }

    @Override
    public String toString() {
        return "location=" + location.toString() + " state=" + getStatusString();
    }
}
