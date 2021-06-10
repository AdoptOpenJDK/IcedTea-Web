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
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.CachedDaemonThreadPoolProvider.DaemonThreadFactory;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import javax.jnlp.DownloadServiceListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.ERROR;
import static net.adoptopenjdk.icedteaweb.resources.Resource.createOrGetResource;
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
public class ResourceTracker {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTracker.class);

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
    public ResourceTracker() {
        this(false);
    }

    /**
     * Creates a resource tracker that does not prefetch resources.
     */
    public ResourceTracker(boolean prefetch) {
        this(prefetch, DownloadOptions.NONE, UpdatePolicy.ALWAYS);
    }

    /**
     * Creates a resource tracker.
     *
     * @param prefetch whether to download resources before requested.
     */
    public ResourceTracker(boolean prefetch, DownloadOptions downloadOptions, UpdatePolicy updatePolicy) {
        this.prefetch = prefetch;
        this.downloadOptions = Assert.requireNonNull(downloadOptions, "downloadOptions");
        this.updatePolicy = Assert.requireNonNull(updatePolicy, "updatePolicy");
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
        LOG.debug("Create resource for '{}'", location);


        final URL normalizedLocation = normalizeUrlQuietly(location);
        final Resource resource = createOrGetResource(normalizedLocation, version, downloadOptions, updatePolicy);
        LOG.debug("Will add resource '{}'", resource.getSimpleName());
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
            triggerDownloadFor(resource, Executors.newSingleThreadExecutor(new DaemonThreadFactory()));
        }
    }

    /**
     * Removes a resource from the tracker.  This method is useful
     * to allow memory to be reclaimed, but calling this method is
     * not required as resources are reclaimed when the tracker is
     * collected.
     *
     * @param location location of resource to be removed
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    public void removeResource(URL location) {
        synchronized (resources) {
            Resource resource = getResource(location);
            resources.remove(resource.getLocation());
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
     */
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
                waitForCompletion(resource);
            }
        } catch (Exception ex) {
            LOG.error("Error while fetching resource " + location, ex);
            return null; // need an error exception to throw
        }
        return getCacheFile(resource);
    }

    private static File getCacheFile(final Resource resource) {
        final URL location = resource.getLocation();
        if (resource.hasStatus(ERROR)) {
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
     */
    public void waitForResources(URL... urls) throws InterruptedException {
        waitForCompletion(getResources(urls));
    }

    /**
     * Returns the number of bytes downloaded for a resource.
     *
     * @param location the resource location
     * @return the number of bytes transferred
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
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
     */
    public boolean checkResource(URL location) {
        Resource resource = getResource(location);
        return resource.isComplete();
    }

    /**
     * Returns the number of total size in bytes of a resource, or
     * -1 it the size is not known.
     *
     * @param location the resource location
     * @return the number of bytes, or -1
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
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
        final URL normalizedLocation = normalizeUrlQuietly(location);
        synchronized (resources) {
            final Resource result = resources.get(normalizedLocation);
            if (result == null) {
                throw new IllegalResourceDescriptorException("Location " + location + " does not specify a resource being tracked.");
            }
            return result;
        }
    }

    /**
     * Wait for some resources.
     *
     * @param resources the resources to wait for
     * @return {@code true} if the resources were downloaded or had errors,
     * {@code false} if the timeout was reached
     * @throws InterruptedException if another thread interrupted the wait
     */
    private void waitForCompletion(Resource... resources) {
        if (resources == null || resources.length == 0) {
            return;
        }

        final int configuredThreadCount = Integer.parseInt(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_PARALLEL_RESOURCE_DOWNLOAD_COUNT));
        final int threadCount = Math.min(configuredThreadCount, resources.length);
        final ExecutorService downloadExecutor = Executors.newFixedThreadPool(threadCount, new DaemonThreadFactory());
        try {
            final List<Future<Resource>> futures = Arrays.asList(resources).stream()
                    .map(r -> triggerDownloadFor(r, downloadExecutor))
                    .collect(Collectors.toList());

            for (Future<Resource> future : futures) {
                future.get();
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error while waiting for download", e);
        } finally {
            LOG.debug("Download done. Shutting down executor");
            downloadExecutor.shutdownNow();
        }
    }

    private Future<Resource> triggerDownloadFor(Resource resource, final Executor downloadExecutor) {
        return new ResourceHandler(resource).putIntoCache(downloadExecutor);
    }

    public void addDownloadListener(final URL resourceUrl, URL[] allResources, final DownloadServiceListener listener) {
        final Resource resource = getResource(resourceUrl);
        resource.addPropertyChangeListener(Resource.SIZE_PROPERTY, e -> listener.progress(resourceUrl, "version", resource.getTransferred(), resource.getSize(), getPercentageDownloaded(allResources)));
        resource.addPropertyChangeListener(Resource.TRANSFERRED_PROPERTY, e -> listener.progress(resourceUrl, "version", resource.getTransferred(), resource.getSize(), getPercentageDownloaded(allResources)));
    }

    private int getPercentageDownloaded(URL[] allResources) {
        return (int) Arrays.asList(allResources).stream()
                .map(u -> getResource(u))
                .mapToDouble(r -> {
                    if (r.isComplete()) {
                        return 100.0d;
                    }
                    if (r.isBeingProcessed() && r.getSize() > 0) {
                        final double p = (100.0d * r.getTransferred()) / r.getSize();
                        return Math.max(0.0d, Math.min(100.0d, p));
                    }
                    return 0.0d;
                }).sum() / allResources.length;
    }
}
