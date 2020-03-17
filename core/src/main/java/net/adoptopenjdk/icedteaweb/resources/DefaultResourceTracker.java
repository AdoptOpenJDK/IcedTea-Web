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

package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.ERROR;
import static net.adoptopenjdk.icedteaweb.resources.Resource.createResource;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;
import static net.sourceforge.jnlp.util.UrlUtils.normalizeUrlQuietly;

/**
 * This class tracks the downloading of various resources of a
 * JNLP file to local files in the cache. It can be used to
 * download icons, jnlp and extension files, jars, and jardiff
 * files using the version based protocol or any file using the
 * basic download protocol (jardiff and version not implemented
 * yet).
 * <p>
 * The resource tracker can be configured to prefetch resources,
 * which are downloaded in the order added to the media
 * tracker.
 * </p>
 * <p>
 * Multiple threads are used to download and cache resources that
 * are actively being waited for (blocking a caller) or those that
 * have been started downloading by calling the startDownload
 * method.  Resources that are prefetched are downloaded one at a
 * time and only if no other trackers have requested downloads.
 * This allows the tracker to start downloading many items without
 * using many system resources, but still quickly download items
 * as needed.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.22 $
 */
public class DefaultResourceTracker implements ResourceTracker {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceTracker.class);

    // todo: use event listener arrays instead of lists

    // todo: see if there is a way to set the socket options just
    // for use by the tracker so checks for updates don't hang for
    // a long time.

    // todo: ability to restart/retry a hung download

    // todo: move resource downloading/processing code into Resource
    // class, threading stays in ResourceTracker

    // todo: get status method? and some way to convey error status
    // to the caller.

    // todo: might make a tracker be able to download more than one
    // version of a resource, but probably not very useful.

    /**
     * the resources known about by this resource tracker
     */
    private final Map<URL, Resource> resources = new HashMap<>();

    /**
     * whether to download parts before requested
     */
    private final boolean prefetch;

    private final DownloadOptions downloadOptions;
    private final UpdatePolicy updatePolicy;

    /**
     * Creates a resource tracker that does not prefetch resources.
     */
    public DefaultResourceTracker() {
        this(false);
    }

    /**
     * Creates a resource tracker that does not prefetch resources.
     */
    public DefaultResourceTracker(boolean prefetch) {
        this(prefetch, DownloadOptions.NONE, UpdatePolicy.ALWAYS);
    }

    /**
     * Creates a resource tracker.
     *
     * @param prefetch whether to download resources before requested.
     */
    public DefaultResourceTracker(boolean prefetch, DownloadOptions downloadOptions, UpdatePolicy updatePolicy) {
        this.prefetch = prefetch;
        this.downloadOptions = Assert.requireNonNull(downloadOptions, "downloadOptions");
        this.updatePolicy = Assert.requireNonNull(updatePolicy, "updatePolicy");
    }

    public void addResource(URL location, final VersionId version) {
        final VersionString versionString = version != null ? version.asVersionString() : null;
        addResource(location, versionString, updatePolicy);
    }

    public void addResource(URL location, final VersionString version) {
        addResource(location, version, updatePolicy);
    }

    /**
     * Add a resource identified by the specified location and
     * version.  The tracker only downloads one version of a given
     * resource per instance (ie cannot download both versions 1 and
     * 2 of a resource in the same tracker).
     *
     * @param location     the location of the resource
     * @param version      the resource version
     * @param updatePolicy whether to check for updates if already in cache
     */
    public void addResource(URL location, final VersionString version, final UpdatePolicy updatePolicy) {
        Assert.requireNonNull(location, "location");

        final URL normalizedLocation = normalizeUrlQuietly(location);
        final Resource resource = createResource(normalizedLocation, version, downloadOptions, updatePolicy);

        if (addToResources(resource)) {
            startDownloadingIfPrefetch(resource);
        }
    }

    /**
     * @return {@code true} if no resource with the given URL is currently tracked.
     */
    private boolean addToResources(Resource resource) {
        synchronized (resources) {
            final Resource existingResource = resources.get(resource.getLocation());

            if (existingResource == null) {
                resources.put(resource.getLocation(), resource);
            } else {
                final VersionString newVersion = resource.getRequestVersion();
                final VersionString existingVersion = existingResource.getRequestVersion();
                if (!Objects.equals(existingVersion, newVersion)) {
                    throw new IllegalStateException("Found two resources with location '" + resource.getLocation() +
                            "' but different versions '" + newVersion + "' - '" + existingVersion + "'");
                }
            }

            return existingResource == null;
        }
    }

    private void startDownloadingIfPrefetch(Resource resource) {
        if (prefetch && !resource.isComplete() && !resource.isBeingProcessed()) {
            new ResourceHandler(resource).putIntoCache();
        }
    }

    /**
     * Returns a URL pointing to the cached location of the
     * resource, or the resource itself if it is a non-cacheable
     * resource.
     * <p>
     * If the resource has not downloaded yet, the method will block
     * until it has been transferred to the cache.
     * </p>
     *
     * @param location the resource location
     * @return the resource, or null if it could not be downloaded
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @see CacheUtil#isCacheable
     * @deprecated
     */
    @Deprecated
    public URL getCacheURL(URL location) {
        final File f = getCacheFile(location);
        if (f != null) {
            try {
                return f.toURI().toURL();
            } catch (MalformedURLException ex) {
                LOG.error("Invalid URL {} - {}", f.toURI(), ex.getMessage());
            }
        }

        return location;
    }

    /**
     * Returns a file containing the downloaded resource.  If the
     * resource is non-cacheable then null is returned unless the
     * resource is a local file (the original file is returned).
     * <p>
     * If the resource has not downloaded yet, the method will block
     * until it has been transferred to the cache.
     * </p>
     *
     * @param location the resource location
     * @return a local file containing the resource, or null
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @see CacheUtil#isCacheable
     */
    public File getCacheFile(URL location) {
        Resource resource = getResource(location);
        try {
            if (!resource.isComplete()) {
                wait(resource);
            }
        } catch (InterruptedException ex) {
            LOG.error("Interrupted while fetching resource {}: {}", location, ex.getMessage());
            return null; // need an error exception to throw
        }
        return getCacheFile(resource);
    }

    private static File getCacheFile(final Resource resource) {
        final URL location = resource.getLocation();
        if (resource.isSet(ERROR)) {
            LOG.debug("Error flag set for resource '{}'. Can not return a local file for the resource", resource.getLocation());
            return null;
        }

        if (resource.getLocalFile() != null) {
            return resource.getLocalFile();
        }

        if (location.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
            File file = UrlUtils.decodeUrlAsFile(location);
            if (file.exists()) {
                return file;
            }
            // try plain, not decoded file now
            // sometimes the jnlp app developers are encoding for us
            // so we end up encoding already encoded file. See RH1154177
            file = new File(location.getPath());
            if (file.exists()) {
                return file;
            }
            // have it sense to try also filename with whole query here?
            // => location.getFile() ?
        }
        LOG.debug("No local file defined for resource '{}'", resource.getLocation());

        return null;
    }

    /**
     * Wait for a group of resources to be downloaded and made
     * available locally.
     *
     * @param urls the resources to wait for
     * @throws InterruptedException               if thread is interrupted
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @deprecated
     */
    @Deprecated
    public void waitForResources(URL... urls) throws InterruptedException {
        if (urls.length > 0) {
            wait(getResources(urls));
        }
    }

    /**
     * Wait for a group of resources to be downloaded and made
     * available locally.
     *
     * @param urls     the resources to wait for
     * @param timeout  the time in ms to wait before returning, 0 for no timeout
     * @param timeUnit the unit for timeout
     * @return whether the resources downloaded before the timeout
     * @throws InterruptedException               if thread is interrupted
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @deprecated
     */
    @Deprecated
    public boolean waitForResources(URL[] urls, long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (urls.length > 0) {
            return wait(getResources(urls), timeout, timeUnit);
        }
        return true;
    }

    /**
     * Returns the number of bytes downloaded for a resource.
     *
     * @param location the resource location
     * @return the number of bytes transferred
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @deprecated
     */
    @Deprecated
    public long getAmountRead(URL location) {
        // not atomic b/c transferred is a long, but so what (each
        // byte atomic? so probably won't affect anything...)
        return getResource(location).getTransferred();
    }

    /**
     * Returns whether a resource is available for use (ie, can be
     * accessed with the getCacheFile method).
     *
     * @param location the resource location
     * @return resource availability
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @deprecated
     */
    @Deprecated
    public boolean checkResource(URL location) {
        Resource resource = getResource(location);
        return resource.isComplete();
    }

    public boolean isResourceAdded(URL location) {
        return getOptionalResource(location).isPresent();
    }

    /**
     * Returns the number of total size in bytes of a resource, or
     * -1 it the size is not known.
     *
     * @param location the resource location
     * @return the number of bytes, or -1
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @deprecated
     */
    @Deprecated
    public long getTotalSize(URL location) {
        return getResource(location).getSize(); // atomic
    }

    private Resource[] getResources(URL[] urls) {
        Resource[] lresources = new Resource[urls.length];

        synchronized (resources) {
            // keep the lock so getResource doesn't have to acquire it each time
            for (int i = 0; i < urls.length; i++) {
                lresources[i] = getResource(urls[i]);
            }
        }
        return lresources;
    }


    /**
     * Return the resource matching the specified URL.
     *
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    private Resource getResource(URL location) {
        return getOptionalResource(location)
                .orElseThrow(() -> new IllegalResourceDescriptorException("Location " + location + " does not specify a resource being tracked."));
    }

    private Optional<Resource> getOptionalResource(URL location) {
        final URL normalizedLocation = normalizeUrlQuietly(location);
        synchronized (resources) {
            return Optional.ofNullable(resources.get(normalizedLocation));
        }
    }

    /**
     * Wait for some resources.
     *
     * @param resources the resources to wait for
     * @throws InterruptedException if another thread interrupted the wait
     */
    private void wait(Resource... resources) throws InterruptedException {
        // save futures in list to allow parallel start of all resources
        final List<Future<Resource>> futures = Stream.of(resources)
                .map(ResourceHandler::new)
                .map(ResourceHandler::putIntoCache)
                .collect(Collectors.toList());

        for (Future<Resource> future : futures) {
            try {
                future.get();
            } catch (ExecutionException ignored) {
            }
        }
    }

    /**
     * Wait for some resources.
     *
     * @param resources the resources to wait for
     * @param timeout   the timeout, or {@code 0} to wait until completed
     * @return {@code true} if the resources were downloaded or had errors,
     * {@code false} if the timeout was reached
     * @throws InterruptedException if another thread interrupted the wait
     */
    private boolean wait(Resource[] resources, long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timout must be bigger than 0");
        }
        long startTime = System.nanoTime();
        long nanoTimeout = timeUnit.toNanos(timeout);

        // save futures in list to allow parallel start of all resources
        final List<Future<Resource>> futures = Stream.of(resources)
                .map(ResourceHandler::new)
                .map(ResourceHandler::putIntoCache)
                .collect(Collectors.toList());

        for (Future<Resource> future : futures) {
            final long nanoSinceStartOfMethod = System.nanoTime() - startTime;
            final long waitTime = nanoTimeout - nanoSinceStartOfMethod;

            if (waitTime <= 0) {
                return false;
            }

            try {
                future.get(waitTime, TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                return false;
            } catch (ExecutionException ignored) {
            }
        }
        return true;
    }
}
