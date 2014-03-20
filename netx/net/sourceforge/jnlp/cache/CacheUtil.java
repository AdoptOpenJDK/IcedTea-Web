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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.jnlp.DownloadServiceListener;

import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.UrlUtils;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {

    private static final String setCacheDir = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR);
    private static final String cacheDir = new File(setCacheDir != null ? setCacheDir : System.getProperty("java.io.tmpdir")).getPath(); // Do this with file to standardize it.
    private static final CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
    private static final HashMap<String, FileLock> propertiesLockPool = new HashMap<String, FileLock>();

    /**
     * Compares a URL using string compare of its protocol, host,
     * port, path, query, and anchor. This method avoids the host
     * name lookup that URL.equals does for http: protocol URLs.
     * It may not return the same value as the URL.equals method
     * (different hostnames that resolve to the same IP address,
     * ie sourceforge.net and www.sourceforge.net).
     */
    public static boolean urlEquals(URL u1, URL u2) {
        if (u1 == u2) {
            return true;
        }
        if (u1 == null || u2 == null) {
            return false;
        }

        if (notNullUrlEquals(u1, u2)) {
            return true;
        }
        try {
            URL nu1 = UrlUtils.normalizeUrl(u1);
            URL nu2 = UrlUtils.normalizeUrl(u2);
            if (notNullUrlEquals(nu1, nu2)) {
                return true;
            }
        } catch (Exception ex) {
            //keep silent here and return false
        }
        return false;
    }

    private static boolean notNullUrlEquals(URL u1, URL u2) {
        if (!compare(u1.getProtocol(), u2.getProtocol(), true)
                || !compare(u1.getHost(), u2.getHost(), true)
                || //u1.getDefaultPort() != u2.getDefaultPort() || // only in 1.4
                !compare(u1.getPath(), u2.getPath(), false)
                || !compare(u1.getQuery(), u2.getQuery(), false)
                || !compare(u1.getRef(), u2.getRef(), false)) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * Caches a resource and returns a URL for it in the cache;
     * blocks until resource is cached. If the resource location is
     * not cacheable (points to a local file, etc) then the original
     * URL is returned.
     *
     * @param location location of the resource
     * @param version the version, or {@code null}
     * @return either the location in the cache or the original location
     */
    public static URL getCachedResource(URL location, Version version, UpdatePolicy policy) {
        ResourceTracker rt = new ResourceTracker();
        rt.addResource(location, version, null, policy);
        try {
            File f = rt.getCacheFile(location);
            // TODO: Should be toURI().toURL()
            return f.toURL();
        } catch (MalformedURLException ex) {
            return location;
        }
    }

    /**
     * Compare strings that can be {@code null}.
     */
    private static boolean compare(String s1, String s2, boolean ignore) {
        if (s1 == s2)
            return true;
        if (s1 == null || s2 == null)
            return false;

        if (ignore)
            return s1.equalsIgnoreCase(s2);
        else
            return s1.equals(s2);
    }

    /**
     * Returns the Permission object necessary to access the
     * resource, or {@code null} if no permission is needed.
     */
    public static Permission getReadPermission(URL location, Version version) {
        if (CacheUtil.isCacheable(location, version)) {
            File file = CacheUtil.getCacheFile(location, version);

            return new FilePermission(file.getPath(), "read");
        } else {
            try {
                // this is what URLClassLoader does
                return location.openConnection().getPermission();
            } catch (java.io.IOException ioe) {
                // should try to figure out the permission
                OutputController.getLogger().log(ioe);
            }
        }

        return null;
    }

    /**
     * Clears the cache by deleting all the Netx cache files
     *
     * Note: Because of how our caching system works, deleting jars of another javaws
     * process is using them can be quite disasterous. Hence why Launcher creates lock files
     * and we check for those by calling {@link #okToClearCache()}
     */
    public static boolean clearCache() {

        if (!okToClearCache()) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, R("CCannotClearCache"));
            return false;
        }

        File cacheDir = new File(CacheUtil.cacheDir);
        if (!(cacheDir.isDirectory())) {
            return false;
        }

        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Clearing cache directory: " + cacheDir);
        try {
            cacheDir = cacheDir.getCanonicalFile();
            FileUtils.recursiveDelete(cacheDir, cacheDir);
            cacheDir.mkdir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Returns a boolean indicating if it ok to clear the netx application cache at this point
     * @return true if the cache can be cleared at this time without problems
     */
    private static boolean okToClearCache() {
        File otherJavawsRunning = new File(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE));
        FileLock locking = null;
        try {
            if (otherJavawsRunning.isFile()) {
                FileOutputStream fis = new FileOutputStream(otherJavawsRunning);
                
                FileChannel channel = fis.getChannel();
                locking  = channel.tryLock();
                if (locking == null) {
                    OutputController.getLogger().log("Other instances of netx are running");
                    return false;
                }
                OutputController.getLogger().log("No other instances of netx are running");
                return true;

            } else {
                OutputController.getLogger().log("No instance file found");
                return true;
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (locking != null) {
                try {
                    locking.release();
                } catch (IOException ex) {
                    OutputController.getLogger().log(ex);
                }
            }
        }
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param source the source {@link URL}
     * @param version the versions to check for
     * @param connection a connection to the {@link URL}, or {@code null}
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static boolean isCurrent(URL source, Version version, URLConnection connection) {

        if (!isCacheable(source, version))
            throw new IllegalArgumentException(R("CNotCacheable", source));

        try {
            if (connection == null)
                connection = source.openConnection();

            connection.connect();

            CacheEntry entry = new CacheEntry(source, version); // could pool this
            boolean result = entry.isCurrent(connection);

            OutputController.getLogger().log("isCurrent: " + source + " = " + result);

            return result;
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            return isCached(source, version); // if can't connect return whether already in cache
        }
    }

    /**
     * Returns true if the cache has a local copy of the contents of
     * the URL matching the specified version string.
     *
     * @param source the source URL
     * @param version the versions to check for
     * @return true if the source is in the cache
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static boolean isCached(URL source, Version version) {
        if (!isCacheable(source, version))
            throw new IllegalArgumentException(R("CNotCacheable", source));

        CacheEntry entry = new CacheEntry(source, version); // could pool this
        boolean result = entry.isCached();

        OutputController.getLogger().log("isCached: " + source + " = " + result);

        return result;
    }

    /**
     * Returns whether the resource can be cached as a local file;
     * if not, then URLConnection.openStream can be used to obtain
     * the contents.
     */
    public static boolean isCacheable(URL source, Version version) {
        if (source == null)
            return false;

        if (source.getProtocol().equals("file"))
            return false;

        if (source.getProtocol().equals("jar"))
            return false;

        return true;
    }

    /**
     * Returns the file for the locally cached contents of the
     * source.  This method returns the file location only and does
     * not download the resource.  The latest version of the
     * resource that matches the specified version will be returned.
     *
     * @param source the source {@link URL}
     * @param version the version id of the local file
     * @return the file location in the cache, or {@code null} if no versions cached
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static File getCacheFile(URL source, Version version) {
        // ensure that version is an version id not version string

        if (!isCacheable(source, version))
            throw new IllegalArgumentException(R("CNotCacheable", source));

        File cacheFile = null;
        synchronized (lruHandler) {
            lruHandler.lock();

            // We need to reload the cacheOrder file each time
            // since another plugin/javaws instance may have updated it.
            lruHandler.load();
            cacheFile = getCacheFileIfExist(urlToPath(source, ""));
            if (cacheFile == null) { // We did not find a copy of it.
                cacheFile = makeNewCacheFile(source, version);
            } else
                lruHandler.store();
            lruHandler.unlock();
        }
        return cacheFile;
    }

    /**
     * This will return a File pointing to the location of cache item.
     * 
     * @param urlPath Path of cache item within cache directory.
     * @return File if we have searched before, {@code null} otherwise.
     */
    private static File getCacheFileIfExist(File urlPath) {
        synchronized (lruHandler) {
            File cacheFile = null;
            List<Entry<String, String>> entries = lruHandler.getLRUSortedEntries();
            // Start searching from the most recent to least recent.
            for (Entry<String, String> e : entries) {
                final String key = e.getKey();
                final String path = e.getValue();

                if (pathToURLPath(path).equals(urlPath.getPath())) { // Match found.
                    cacheFile = new File(path);
                    lruHandler.updateEntry(key);
                    break; // Stop searching since we got newest one already.
                }
            }
            return cacheFile;
        }
    }

    /**
     * Get the path to file minus the cache directory and indexed folder.
     */
    private static String pathToURLPath(String path) {
        int len = cacheDir.length();
        int index = path.indexOf(File.separatorChar, len + 1);
        return path.substring(index);
    }

    /**
     * Returns the parent directory of the cached resource.
     * @param filePath The path of the cached resource directory.
     */
    public static String getCacheParentDirectory(String filePath) {
        String path = filePath;
        String tempPath = "";

        while(path.startsWith(cacheDir) && !path.equals(cacheDir)){
                tempPath = new File(path).getParent();

                if (tempPath.equals(cacheDir))
                    break;

                path = tempPath;
        }
        return path;
    }

    /**
     * This will create a new entry for the cache item. It is however not
     * initialized but any future calls to getCacheFile with the source and
     * version given to here, will cause it to return this item.
     * 
     * @param source the source URL
     * @param version the version id of the local file
     * @return the file location in the cache.
     */
    public static File makeNewCacheFile(URL source, Version version) {
        synchronized (lruHandler) {
            lruHandler.lock();
            lruHandler.load();

            File cacheFile = null;
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                String path = cacheDir + File.separator + i;
                File cDir = new File(path);
                if (!cDir.exists()) {
                    // We can use this directory.
                    try {
                        cacheFile = urlToPath(source, path);
                        FileUtils.createParentDir(cacheFile);
                        File pf = new File(cacheFile.getPath() + ".info");
                        FileUtils.createRestrictedFile(pf, true); // Create the info file for marking later.
                        lruHandler.addEntry(lruHandler.generateKey(cacheFile.getPath()), cacheFile.getPath());
                    } catch (IOException ioe) {
                       OutputController.getLogger().log(ioe);
                    }

                    break;
                }
            }

            lruHandler.store();
            lruHandler.unlock();
            return cacheFile;
        }
    }

    /**
     * Returns a buffered output stream open for writing to the
     * cache file.
     *
     * @param source the remote location
     * @param version the file version to write to
     */
    public static OutputStream getOutputStream(URL source, Version version) throws IOException {
        File localFile = getCacheFile(source, version);
        OutputStream out = new FileOutputStream(localFile);

        return new BufferedOutputStream(out);
    }

    /**
     * Copies from an input stream to an output stream.  On
     * completion, both streams will be closed.  Streams are
     * buffered automatically.
     */
    public static void streamCopy(InputStream is, OutputStream os) throws IOException {
        if (!(is instanceof BufferedInputStream))
            is = new BufferedInputStream(is);

        if (!(os instanceof BufferedOutputStream))
            os = new BufferedOutputStream(os);

        try {
            byte b[] = new byte[4096];
            while (true) {
                int c = is.read(b, 0, b.length);
                if (c == -1)
                    break;

                os.write(b, 0, c);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    /**
     * Converts a URL into a local path string within the given directory. For
     * example a url with subdirectory /tmp/ will
     * result in a File that is located somewhere within /tmp/
     *
     * @param location the url
     * @param subdir the subdirectory
     * @return the file
     */
    public static File urlToPath(URL location, String subdir) {
        if (subdir == null) {
            throw new NullPointerException();
        }

        StringBuffer path = new StringBuffer();

        path.append(subdir);
        path.append(File.separatorChar);

        path.append(location.getProtocol());
        path.append(File.separatorChar);
        path.append(location.getHost());
        path.append(File.separatorChar);
        path.append(location.getPath().replace('/', File.separatorChar));

        return new File(FileUtils.sanitizePath(path.toString()));
    }

    /**
     * Waits until the resources are downloaded, while showing a
     * progress indicator.
     *
     * @param tracker the resource tracker
     * @param resources the resources to wait for
     * @param title name of the download
     */
    public static void waitForResources(ApplicationInstance app, ResourceTracker tracker, URL resources[], String title) {
        DownloadIndicator indicator = JNLPRuntime.getDefaultDownloadIndicator();
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
            List<URL> urlList = new ArrayList<URL>();
            for (URL url : resources) {
                if (!tracker.checkResource(url))
                    urlList.add(url);
            }
            URL undownloaded[] = urlList.toArray(new URL[urlList.size()]);

            listener = indicator.getListener(app, title, undownloaded);

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
            OutputController.getLogger().log(ex);
        } finally {
            if (listener != null)
                indicator.disposeListener(listener);
        }
    }

    /**
     * This will remove all old cache items.
     */
    public static void cleanCache() {

        if (okToClearCache()) {
            // First we want to figure out which stuff we need to delete.
            HashSet<String> keep = new HashSet<String>();
            HashSet<String> remove = new HashSet<String>();
            lruHandler.load();
            
            long maxSize = -1; // Default
            try {
                maxSize = Long.parseLong(JNLPRuntime.getConfiguration().getProperty("deployment.cache.max.size"));
            } catch (NumberFormatException nfe) {
            }
            
            maxSize = maxSize << 20; // Convert from megabyte to byte (Negative values will be considered unlimited.)
            long curSize = 0;

            for (Entry<String, String> e : lruHandler.getLRUSortedEntries()) {
                // Check if the item is contained in cacheOrder.
                final String key = e.getKey();
                final String path = e.getValue();

                File file = new File(path);
                PropertiesFile pf = new PropertiesFile(new File(path + ".info"));
                boolean delete = Boolean.parseBoolean(pf.getProperty("delete"));

                /*
                 * This will get me the root directory specific to this cache item.
                 * Example:
                 *  cacheDir = /home/user1/.icedtea/cache
                 *  file.getPath() = /home/user1/.icedtea/cache/0/http/www.example.com/subdir/a.jar
                 *  rStr first becomes: /0/http/www.example.com/subdir/a.jar
                 *  then rstr becomes: /home/user1/.icedtea/cache/0
                 */
                String rStr = file.getPath().substring(cacheDir.length());
                rStr = cacheDir + rStr.substring(0, rStr.indexOf(File.separatorChar, 1));
                long len = file.length();

                if (keep.contains(file.getPath().substring(rStr.length()))) {
                    lruHandler.removeEntry(key);
                    continue;
                }

                /*
                 * we remove entries from our lru if any of the following condition is met.
                 * Conditions:
                 *  - delete: file has been marked for deletion.
                 *  - !file.isFile(): if someone tampered with the directory, file doesn't exist.
                 *  - maxSize >= 0 && curSize + len > maxSize: If a limit was set and the new size
                 *  on disk would exceed the maximum size.
                 */
                if (delete || !file.isFile() || (maxSize >= 0 && curSize + len > maxSize)) {
                    lruHandler.removeEntry(key);
                    remove.add(rStr);
                    continue;
                }

                curSize += len;
                keep.add(file.getPath().substring(rStr.length()));

                for (File f : file.getParentFile().listFiles()) {
                    if (!(f.equals(file) || f.equals(pf.getStoreFile()))) {
                        try {
                            FileUtils.recursiveDelete(f, f);
                        } catch (IOException e1) {
                            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e1);
                        }
                    }

                }
            }
            lruHandler.store();

            /*
             * FIXME: if cacheDir is for example $USER_HOME and they have a folder called http
             * and/or https. These would get removed.
             */
            remove.add(cacheDir + File.separator + "http");
            remove.add(cacheDir + File.separator + "https");

            removeSetOfDirectories(remove);

        }
    }

    private static void removeSetOfDirectories(Set<String> remove) {
        for (String s : remove) {
            File f = new File(s);
            try {
                FileUtils.recursiveDelete(f, f);
            } catch (IOException e) {
            }
        }
    }

    /**
     * Lock the property file and add it to our pool of locks.
     * 
     * @param properties Property file to lock.
     */
    public static void lockFile(PropertiesFile properties) {
        String storeFilePath = properties.getStoreFile().getPath();
        try {
            propertiesLockPool.put(storeFilePath, FileUtils.getFileLock(storeFilePath, false, true));
        } catch (OverlappingFileLockException e) {
        } catch (FileNotFoundException e) {
           OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    /**
     * Unlock the property file and remove it from our pool. Nothing happens if
     * it wasn't locked.
     * 
     * @param properties Property file to unlock.
     */
    public static void unlockFile(PropertiesFile properties) {
        File storeFile = properties.getStoreFile();
        FileLock fl = propertiesLockPool.get(storeFile.getPath());
        try {
            if (fl == null) return;
            fl.release();
            fl.channel().close();
            propertiesLockPool.remove(storeFile.getPath());
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }
}
