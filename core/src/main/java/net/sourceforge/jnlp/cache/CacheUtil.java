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
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.InstallerDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.jnlp.DownloadServiceListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUtil.class);

    /**
     * Caches a resource and returns a URL for it in the cache;
     * blocks until resource is cached. If the resource location is
     * not cacheable (points to a local file, etc) then the original
     * URL is returned.
     *
     * @param location location of the resource
     * @param version  the version, or {@code null}
     * @param policy   how to handle update
     * @return either the location in the cache or the original location
     */
    public static URL getCachedResourceURL(final URL location, final VersionString version, final UpdatePolicy policy) {
        try {
            final File f = getCachedResourceFile(location, version, policy);
            //url was pointing to nowhere eg 404
            if (f == null) {
                //originally  f.toUrl was throwing NPE
                return null;
                //returning null seems to be better
            }
            return f.toURI().toURL();
        } catch (MalformedURLException ex) {
            return location;
        }
    }

    /**
     * This is returning File object of cached resource originally from URL
     *
     * @param location original location of blob
     * @param version  version of resource
     * @param policy   update policy of resource
     * @return location in ITW cache on filesystem
     */
    private static File getCachedResourceFile(final URL location, final VersionString version, final UpdatePolicy policy) {
        final ResourceTracker rt = new ResourceTracker();
        rt.addResource(location, version, policy);
        return rt.getCacheFile(location);
    }

    /**
     * Clears the cache by deleting all the Netx cache files
     * <p>
     * Note: Because of how our caching system works, deleting jars of another javaws
     * process is using them can be quite disastrous. Hence why Launcher creates lock files
     * and we check for those.
     *
     * @return true if the cache could and was cleared
     */
    public static boolean clearCache() {
        return CacheLRUWrapper.getInstance().clearCache();
    }

    public static boolean deleteFromCache(final String cacheId) {
        return CacheLRUWrapper.getInstance().deleteFromCache(cacheId);
    }

    /**
     * This method load all known IDs of applications and  will gather all members, which share the id
     *
     * @param filter - regex to filter keys
     */
    public static List<CacheId> getCacheIds(final String filter, final boolean jnlpPath, final boolean domain) {
        return CacheLRUWrapper.getInstance().getCacheIds(filter, jnlpPath, domain);
    }

    /**
     * This will remove all old cache items.
     */
    public static void cleanCache() {
        CacheLRUWrapper.getInstance().cleanCache();
    }

    /**
     * Returns the file for the locally cached contents of the
     * source.  This method returns the file location only and does
     * not download the resource.  The latest version of the
     * resource that matches the specified version will be returned.
     *
     * @param source  the source {@link URL}
     * @param version the version of the local file
     * @return the file location in the cache, or {@code null} if no versions cached
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static File getCacheFile(final URL source, final VersionId version) {

        // TODO: handle Version

        if (!isCacheable(source)) {
            throw new IllegalArgumentException(source + " is not a cacheable resource");
        }

        return CacheLRUWrapper.getInstance().getCacheFile(source, version);
    }

    public static void removeFiles(URL location, VersionString version) {
        CacheLRUWrapper.getInstance().deleteFromCache(location, version);
    }

    /**
     * This will create a new entry for the cache item. It is however not
     * initialized but any future calls to getCacheFile with the source and
     * version given to here, will cause it to return this item.
     *
     * @param source  the source URL
     * @param version the version id of the local file
     * @return the file location in the cache.
     */
    static File makeNewCacheFile(final URL source, final VersionId version) {
        return CacheLRUWrapper.getInstance().makeNewCacheFile(source, version);
    }

    /**
     * Returns true if the cache has a local copy of the contents of
     * the URL matching the specified version.
     *
     * @param location the location URL
     * @param version  the version to check for
     * @return true if the source is in the cache
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static boolean isAnyCached(final URL location, final VersionString version) {
        final VersionId versionId = getBestMatchingVersionInCache(location, version);
        return versionId != null;
    }

    static VersionId getBestMatchingVersionInCache(final URL location, final VersionString version) {
        // TODO: handle Version
        throw new RuntimeException("not implemented");
    }

    public static Set<VersionId> getAllMatchingVersionInCache(final URL location, final VersionString version) {
        // TODO: handle Version
        throw new RuntimeException("not implemented");
    }

    public static void logCacheIds(String filter) {
        List<CacheId> items = getCacheIds(filter, true, true);
        if (JNLPRuntime.isDebug()) {
            for (CacheId id : items) {
                LOG.info("{} ({}) [{}]", id.getId(), id.getType(), id.files.size());
                for (Object[] o : id.getFiles()) {
                    StringBuilder sb = new StringBuilder();
                    for (Object value : o) {
                        Object object = value;
                        if (object == null) {
                            object = "??";
                        }
                        sb.append(object.toString()).append(" ;  ");
                    }
                    LOG.info("  * {}", sb);
                }
            }
        } else {
            for (CacheId id : items) {
                LOG.info(id.getId());
            }
        }
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param source      the source {@link URL}
     * @param version     the versions to check for
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    static boolean isCurrent(final URL source, final VersionId version, long lastModified) {

        if (!isCacheable(source))
            throw new IllegalArgumentException(source + " is not a cacheable resource");

        try {
            CacheEntry entry = new CacheEntry(source, version); // could pool this
            boolean result = entry.isCurrent(lastModified);

            LOG.info("isCurrent: {} = {}", source, result);

            return result;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return isCached(source, version); // if can't connect return whether already in cache
        }
    }

    /**
     * Returns true if the cache has a local copy of the contents of
     * the URL matching the specified version.
     *
     * @param source  the source URL
     * @param version the version to check for
     * @return true if the source is in the cache
     * @throws IllegalArgumentException if the source is not cacheable
     */
    private static boolean isCached(final URL source, final VersionId version) {
        if (!isCacheable(source))
            throw new IllegalArgumentException(source + " is not a cacheable resource");

        CacheEntry entry = new CacheEntry(source, version); // could pool this
        boolean result = entry.isCached();

        LOG.info("isCached: {} = {}", source, result);

        return result;
    }

    /**
     * Returns whether the resource can be cached as a local file;
     * if not, then URLConnection.openStream can be used to obtain
     * the contents.
     *
     * @param source  the url of resource
     * @return whether this resource can be cached
     */
    public static boolean isCacheable(URL source) {
        if (source == null) {
            return false;
        } else if (source.getProtocol().equals("file")) {
            return false;
        } else if (source.getProtocol().equals("jar")) {
            return false;
        }
        return true;
    }



    /**
     * Converts a URL into a local path string within the given directory. For
     * example a url with subdirectory /tmp/ will
     * result in a File that is located somewhere within /tmp/
     *
     * @param location the url
     * @param root   the subdirectory
     * @return the file
     */
    public static File urlToPath(URL location, String root) {
        if (root == null) {
            throw new NullPointerException();
        }

        StringBuilder path = new StringBuilder();

        path.append(root);
        path.append(File.separatorChar);

        path.append(location.getProtocol());
        path.append(File.separatorChar);
        path.append(location.getHost());
        path.append(File.separatorChar);
        /**
         * This is a bit of imprecise. The usage of default port would be
         * better, but it would cause terrible backward incompatibility.
         */
        if (location.getPort() > 0) {
            path.append(location.getPort());
            path.append(File.separatorChar);
        }
        String locationPath = location.getPath();
        String query = "";
        if (location.getQuery() != null) {
            query = location.getQuery();
        }
        if (locationPath.contains("..") || query.contains("..")){
            try {
                /**
                 * if path contains .. then it can harm lcoal system
                 * So without mercy, hash it
                 */
                String hexed = hex(new File(locationPath).getName(), locationPath);
                return new File(path.toString(), hexed.toString());
            } catch (NoSuchAlgorithmException ex) {
                // should not occur, cite from javadoc:
                // every java implementation should support
                // MD5 SHA-1 SHA-256
                throw new RuntimeException(ex);
            }
        } else {
            path.append(locationPath.replace('/', File.separatorChar));
            if (location.getQuery() != null && !location.getQuery().trim().isEmpty()) {
                path.append(".").append(location.getQuery());
            }

            File candidate = new File(FileUtils.sanitizePath(path.toString()));
            try {
                if (candidate.getName().length() > 255) {
                    /**
                     * When filename is longer then 255 chars, then then various
                     * filesystems have issues to save it. By saving the file by its
                     * sum, we are trying to prevent collision of two files differs in
                     * suffixes (general suffix of name, not only 'filetype suffix')
                     * only. It is also preventing bug when truncate (files with 1000
                     * chars hash in query) cuts to much.
                     */
                    String hexed = hex(candidate.getName(), candidate.getName());
                    candidate = new File(candidate.getParentFile(), hexed.toString());
                }
            } catch (NoSuchAlgorithmException ex) {
                // should not occur, cite from javadoc:
                // every java implementation should support
                // MD5 SHA-1 SHA-256
                throw new RuntimeException(ex);
            }
            return candidate;
        }
    }

    public static String hex(String origName, String candidate) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] sum = md.digest(candidate.getBytes(UTF_8));
        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte b : sum) {
            hexString.append(Integer.toHexString(0xFF & b));
        }
        String extension = "";
        int i = origName.lastIndexOf('.');
        if (i > 0) {
            extension = origName.substring(i);//contains dot
        }
        if (extension.length() < 10 && extension.length() > 1) {
            hexString.append(extension);
        }
        return hexString.toString();
    }

    /**
     * Waits until the resources are downloaded, while showing a
     * progress indicator.
     * @param jnlpClassLoader the classloader
     * @param tracker   the resource tracker
     * @param resources the resources to wait for
     * @param title     name of the download
     */
    public static void waitForResources(final JNLPClassLoader jnlpClassLoader, final ResourceTracker tracker, final URL[] resources, final String title) {
        final DownloadIndicator indicator = JNLPRuntime.getDefaultDownloadIndicator();
        DownloadServiceListener listener = null;

        try {
            if (indicator == null) {
                tracker.waitForResources(resources, 0);
                return;
            }

            // see if resources can be downloaded very quickly; avoids
            // overhead of creating display components for the resources
            if (tracker.waitForResources(resources, indicator.getInitialDelay()))
                return;

            // only resources not starting out downloaded are displayed
            final List<URL> urlList = new ArrayList<>();
            for (URL url : resources) {
                if (!tracker.checkResource(url))
                    urlList.add(url);
            }
            final URL[] undownloaded = urlList.toArray(new URL[urlList.size()]);

            listener = getDownloadServiceListener(jnlpClassLoader, title, undownloaded, indicator);

            do {
                long read = 0;
                long total = 0;

                for (URL url : undownloaded) {
                    // add in any -1's; they're insignificant
                    total += tracker.getTotalSize(url);
                    read += tracker.getAmountRead(url);
                }

                int percent = (int) ((100 * read) / Math.max(1, total));

                for (URL url : undownloaded) {
                    listener.progress(url, "version",
                            tracker.getAmountRead(url),
                            tracker.getTotalSize(url),
                            percent);
                }
            } while (!tracker.waitForResources(resources, indicator.getUpdateRate()));

            // make sure they read 100% until indicator closes
            for (URL url : undownloaded) {
                listener.progress(url, "version",
                        tracker.getTotalSize(url),
                        tracker.getTotalSize(url),
                        100);
            }
        } catch (InterruptedException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        } finally {
            if (listener != null)
                indicator.disposeListener(listener);
        }
    }

    private static DownloadServiceListener getDownloadServiceListener(final JNLPClassLoader jnlpClassLoader, final String title, final URL[] undownloaded, final DownloadIndicator indicator) {
        final EntryPoint entryPoint = jnlpClassLoader.getJNLPFile().getEntryPointDesc();
        String progressClass = null;

        if (entryPoint instanceof ApplicationDesc) {
            final ApplicationDesc applicationDesc = (ApplicationDesc) entryPoint;
            progressClass = applicationDesc.getProgressClass();
        } else if (entryPoint instanceof AppletDesc) {
            final AppletDesc appletDesc = (AppletDesc) entryPoint;
            progressClass = appletDesc.getProgressClass();
        } else if (entryPoint instanceof InstallerDesc) {
            final InstallerDesc installerDesc = (InstallerDesc) entryPoint;
            progressClass = installerDesc.getProgressClass();
        }

        if (progressClass != null) {
            try {
                final Class<?> downloadProgressIndicatorClass = jnlpClassLoader.loadClass(progressClass);
                return (DownloadServiceListener) downloadProgressIndicatorClass.newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.warn(format("Could not load progress class '%s' specified in JNLP file, " +
                        "use default download progress indicator instead.", progressClass), ex);
            }
        }

        return indicator.getListener(title, undownloaded);
    }
}
