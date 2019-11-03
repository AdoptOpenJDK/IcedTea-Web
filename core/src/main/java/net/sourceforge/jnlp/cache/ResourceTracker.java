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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.ResourceInfo;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

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

    // defines
    //    ResourceTracker.Downloader (download threads)

      /** notified on initialization or download of a resource */
    private static final Object lock = new Object(); // used to lock static structures

    /** the resources known about by this resource tracker */
    private final List<Resource> resources = new ArrayList<>();

    /** whether to download parts before requested */
    private final boolean prefetch;

    /**
     * Creates a resource tracker that does not prefetch resources.
     */
    public ResourceTracker() {
        this(false);
    }

    /**
     * Creates a resource tracker.
     *
     * @param prefetch whether to download resources before requested.
     */
    public ResourceTracker(boolean prefetch) {
        this.prefetch = prefetch;
    }

    public void addResource(URL location, final VersionId version, final UpdatePolicy updatePolicy) {
        final VersionString versionString = version != null ? version.asVersionString() : null;
        addResource(location, versionString, new DownloadOptions(false, false), updatePolicy);
    }

    public void addResource(URL location, final VersionString version, final UpdatePolicy updatePolicy) {
        addResource(location, version, new DownloadOptions(false, false), updatePolicy);
    }

    /**
     * Add a resource identified by the specified location and
     * version.  The tracker only downloads one version of a given
     * resource per instance (ie cannot download both versions 1 and
     * 2 of a resource in the same tracker).
     *
     * @param location the location of the resource
     * @param version the resource version
     * @param options options to control download
     * @param updatePolicy whether to check for updates if already in cache
     */
    public void addResource(URL location, final VersionString version, final DownloadOptions options, final UpdatePolicy updatePolicy) {
        requireNonNull(options, "options");

        if (location == null) {
            throw new IllegalResourceDescriptorException("location==null");
        }

        try {
            location = UrlUtils.normalizeUrl(location);
        } catch (Exception ex) {
            LOG.error("Normalization of " + location.toString() + " has failed", ex);
        }

        Resource resource = Resource.createResource(location, version, options, updatePolicy);

        synchronized (resources) {
            if (resources.contains(resource)) {
                return;
            }
            resources.add(resource);
        }

        // checkCache may take a while (loads properties file).  this
        // should really be synchronized on resources, but the worst
        // case should be that the resource will be updated once even
        // if unnecessary.
        initResourceFromCache(resource, updatePolicy);

        if (prefetch && !resource.isSet(DOWNLOADED)) {
            ResourceDownloader.startDownload(resource, lock);
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
            resources.remove(resource);
        }
    }

    /**
     * Check the cache for a resource, and initialize the resource
     * as already downloaded if found.
     *
     * @param updatePolicy whether to check for updates if already in cache
     */
    private static void initResourceFromCache(final Resource resource, final UpdatePolicy updatePolicy) {
        if (!CacheUtil.isCacheable(resource.getLocation())) {
            // pretend that they are already downloaded; essentially
            // they will just 'pass through' the tracker as if they were
            // never added (for example, not affecting the total download size).
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(DOWNLOADED, CONNECTED));
            resource.startProcessing();
            return;
        }

        if (updatePolicy != UpdatePolicy.ALWAYS && updatePolicy != UpdatePolicy.FORCE) { // save loading entry props file
            ResourceInfo entry = CacheUtil.getInfoFromCache(resource.getLocation(), resource.getRequestVersion());

            if (entry != null && !updatePolicy.shouldUpdate(entry)) {
                LOG.info("not updating: {}", resource.getLocation());

                synchronized (resource) {
                    resource.setLocalFile(Cache.getCacheFile(resource.getLocation(), resource.getDownloadVersion()));
                    resource.setDownloadVersion(entry.getVersion());
                    resource.setSize(resource.getLocalFile().length());
                    resource.setTransferred(resource.getLocalFile().length());
                    resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(DOWNLOADED, CONNECTED));
                    resource.startProcessing();
                }
                return;
            }
        }

        if (updatePolicy == UpdatePolicy.FORCE) { // ALWAYS update
            // When we are "always" updating, we update for each instance. Reset resource status.
            resource.resetStatus();
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
        try {
            final File f = getCacheFile(location);
            if (f != null) {
                return f.toURI().toURL();
            }
        } catch (MalformedURLException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
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
                wait(new Resource[]{resource}, 0);
            }
        } catch (InterruptedException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return null; // need an error exception to throw
        }
        return getCacheFile(resource);
    }

    private static File getCacheFile(Resource resource) {
        final URL location = resource.getLocation();
        if (resource.isSet(ERROR)) {
            return null;
        }

        if (resource.getLocalFile() != null) {
            return resource.getLocalFile();
        }

        if (location.getProtocol().equalsIgnoreCase("file")) {
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

        return null;
    }

    /**
     * Wait for a group of resources to be downloaded and made
     * available locally.
     *
     * @param urls the resources to wait for
     * @param timeout the time in ms to wait before returning, 0 for no timeout
     * @return whether the resources downloaded before the timeout
     * @throws java.lang.InterruptedException if thread is interrupted
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    boolean waitForResources(URL[] urls, long timeout) throws InterruptedException {
        Resource[] lresources = new Resource[urls.length];

        synchronized (resources) {
            // keep the lock so getResource doesn't have to acquire it each time
            for (int i = 0; i < urls.length; i++) {
                lresources[i] = getResource(urls[i]);
            }
        }

        if (lresources.length > 0) {
            return wait(lresources, timeout);
        }

        return true;
    }

    /**
     * Returns the number of bytes downloaded for a resource.
     *
     * @param location the resource location
     * @return the number of bytes transferred
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    long getAmountRead(URL location) {
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
    boolean checkResource(URL location) {
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
    long getTotalSize(URL location) {
        return getResource(location).getSize(); // atomic
    }

    /**
     * Return the resource matching the specified URL.
     *
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    private Resource getResource(URL location) {
        synchronized (resources) {
            return resources.stream()
                    .filter(r -> UrlUtils.urlEquals(r.getLocation(), location))
                    .findFirst()
                    .orElseThrow(() -> new IllegalResourceDescriptorException("Location does not specify a resource being tracked."));
        }
    }

    /**
     * Wait for some resources.
     *
     * @param resources the resources to wait for
     * @param timeout the timeout, or {@code 0} to wait until completed
     * @return {@code true} if the resources were downloaded or had errors,
     * {@code false} if the timeout was reached
     * @throws InterruptedException if another thread interrupted the wait
     */
    private boolean wait(Resource[] resources, long timeout) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // start them downloading / connecting in background
        for (Resource resource : resources) {
            if (isWhitelistURL(resource.getLocation())) {
                LOG.info(" Download Resource : " + resource.getLocation());
                ResourceDownloader.startDownload(resource, lock);
             } else {
                 BasicExceptionDialog.show(new SecurityException(Translator.R("SWPInvalidURL") + ": " + resource.getLocation()));
                 LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE + " Server Not In Whitelist: " + resource.getLocation());
                 JNLPRuntime.exit(-1);
            }
        }

        // wait for completion
        while (true) {
            boolean finished = true;

            synchronized (lock) {
                // check for completion
                for (Resource resource : resources) {
                    if (!resource.isComplete()) {
                        finished = false;
                        break;
                    }
                }

                if (finished) {
                    return true;
                }

                // wait
                long waitTime = 0;

                if (timeout > 0) {
                    final long timeSinceStartOfMethod = System.currentTimeMillis() - startTime;
                    waitTime = timeout - timeSinceStartOfMethod;
                    if (waitTime <= 0)
                        return false;
                }

                lock.wait(waitTime);
            }
        }
    }

    public boolean isWhitelistURL(final URL url) {
        requireNonNull(url, "url");

        final String whitelistString = JNLPRuntime.getConfiguration().getProperty(KEY_SECURITY_SERVER_WHITELIST);
        if (whitelistString == null || (whitelistString != null && whitelistString.isEmpty())) {
            return false; // No whitelist
        }

        final String urlString = url.getProtocol() + "://" + url.getHost() +  ((url.getPort() != -1) ? ":" + url.getPort() : "");

        final List<String> whitelist = Arrays.stream(whitelistString.split("\\s*,\\s*")).collect(Collectors.toList());
        return whitelist.contains(urlString);
    }
}
