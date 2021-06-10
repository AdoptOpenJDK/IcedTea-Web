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

package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.WeakList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.ERROR;
import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.INCOMPLETE;
import static net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils.validateWithApplicationWhiteList;

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

    /** list of weak references of resources currently in use */
    private static final WeakList<Resource> resources = new WeakList<>();
    public static final String SIZE_PROPERTY = "size";
    public static final String TRANSFERRED_PROPERTY = "transferred";

    private final PropertyChangeSupport propertyChangeSupport;

    /** the remote location of the resource */
    private final URL location;

    /** the requested version */
    private final VersionString requestVersion;

    /** Update policy for this resource */
    private final UpdatePolicy updatePolicy;

    /** Download options for this resource */
    private final DownloadOptions downloadOptions;

    /** the local file downloaded to */
    private File localFile;

    /** amount in bytes transferred */
    private volatile long transferred = 0;

    /** total size of the resource, or -1 if unknown */
    private volatile long size = -1;

    /** A future to wait for completion of download of this resource */
    private volatile Future<Resource> futureForDownloaded;

    /** the status of the resource */
    private volatile ResourceStatus status = INCOMPLETE;

    /**
     * Create a resource.
     */
    private Resource(final URL location, final VersionString requestVersion, final DownloadOptions downloadOptions, final UpdatePolicy updatePolicy) {
        this.location = location;
        this.requestVersion = requestVersion;
        this.downloadOptions = downloadOptions;
        this.updatePolicy = updatePolicy;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Creates and returns a shared Resource object representing the given
     * location and version.
     *
     * @param location        final location of resource
     * @param requestVersion  final version of resource
     * @param downloadOptions hint for downloading
     * @param updatePolicy    final policy for updating
     * @return new resource, which is already added in resources list
     */
    static Resource createOrGetResource(final URL location, final VersionString requestVersion, final DownloadOptions downloadOptions, final UpdatePolicy updatePolicy) {
        synchronized (resources) {
            Resource resource = new Resource(location, requestVersion, downloadOptions, updatePolicy);

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
     *
     * @return the same location as the one with which this resource was created
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @return the local file currently being downloaded
     */
    File getLocalFile() {
        return localFile;
    }

    /**
     * Sets the local file to be downloaded
     *
     * @param localFile location of stored resource
     */
    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    /**
     * @return the requested version
     */
    public VersionString getRequestVersion() {
        return requestVersion;
    }

    /**
     * @return the amount in bytes transferred
     */
    long getTransferred() {
        return transferred;
    }

    /**
     * Sets the amount transferred
     *
     * @param transferred set the whole transferred amount to this value
     */
    public void setTransferred(long transferred) {
        final long oldTransferred = this.transferred;
        this.transferred = transferred;
        this.propertyChangeSupport.firePropertyChange(TRANSFERRED_PROPERTY, oldTransferred, this.transferred);
    }

    /**
     * Returns the size of the resource
     *
     * @return size of resource (-1 if unknown)
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of the resource
     *
     * @param size desired size of resource
     */
    public void setSize(long size) {
        final long oldSize = this.size;
        this.size = size;
        this.propertyChangeSupport.firePropertyChange(SIZE_PROPERTY, oldSize, this.size);
    }

    boolean isBeingProcessed() {
        return futureForDownloaded != null;
    }

    /**
     * Check if the specified flag is set.
     *
     * @param flag a status flag
     * @return true iff the flag is set
     */
    boolean hasStatus(ResourceStatus flag) {
        return status == flag;
    }

    boolean isComplete() {
        return hasStatus(ERROR) || hasStatus(DOWNLOADED);
    }

    /**
     * @return the update policy for this resource
     */
    public UpdatePolicy getUpdatePolicy() {
        return this.updatePolicy;
    }

    public boolean forceUpdateRequested() {
        return updatePolicy == UpdatePolicy.FORCE;
    }

    /**
     * Changes the status.
     *
     * @param status a collection of status flags to set
     */
    public void setStatus(ResourceStatus status) {
        Assert.requireNonNull(status, "status");
        this.status = status;
    }

    public DownloadOptions getDownloadOptions() {
        return this.downloadOptions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, requestVersion);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Resource) {
            // this prevents the URL handler from looking up the IP
            // address and doing name resolution; much faster so less
            // time spent in synchronized addResource determining if
            // Resource is already in a tracker, and better for offline
            // mode on some OS.
            final Resource otherResource = (Resource) other;
            return UrlUtils.urlEquals(location, otherResource.location) && Objects.equals(requestVersion, otherResource.getRequestVersion());
        }
        return false;
    }

    @Override
    public String toString() {
        return "[l=" + location.toString() + " v=" + requestVersion + " s=" + status.getShortName() + "]";
    }

    public String getSimpleName() {
        return Optional.ofNullable(getLocation())
                .map(URL::getPath)
                .map(p -> p.split("/"))
                .map(a -> a[a.length - 1])
                .orElse("UNKNOWN");
    }

    Future<Resource> putIntoCache(final Executor downloadExecutor) {
        validateWithApplicationWhiteList(location);

        synchronized (this) {
            if (futureForDownloaded == null) {
                this.futureForDownloaded = ResourceHandler.putIntoCache(this, downloadExecutor);
            }
            return futureForDownloaded;
        }
    }
}
