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

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.security.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.*;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.*;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.PropertiesFile;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {

    private static final String cacheDir = new File(JNLPRuntime.getConfiguration()
            .getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR)).getPath(); // Do this with file to standardize it.
    private static final CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
    private static final HashMap<String, FileLock> propertiesLockPool = new HashMap<String, FileLock>();

    /**
     * Compares a URL using string compare of its protocol, host,
     * port, path, query, and anchor.  This method avoids the host
     * name lookup that URL.equals does for http: protocol URLs.
     * It may not return the same value as the URL.equals method
     * (different hostnames that resolve to the same IP address,
     * ie sourceforge.net and www.sourceforge.net).
     */
    public static boolean urlEquals(URL u1, URL u2) {
        if (u1 == u2)
            return true;
        if (u1 == null || u2 == null)
            return false;

        if (!compare(u1.getProtocol(), u2.getProtocol(), true) ||
                !compare(u1.getHost(), u2.getHost(), true) ||
                //u1.getDefaultPort() != u2.getDefaultPort() || // only in 1.4
                !compare(u1.getPath(), u2.getPath(), false) ||
                !compare(u1.getQuery(), u2.getQuery(), false) ||
                !compare(u1.getRef(), u2.getRef(), false))
            return false;
        else
            return true;
    }

    /**
     * Caches a resource and returns a URL for it in the cache;
     * blocks until resource is cached.  If the resource location is
     * not cacheable (points to a local file, etc) then the original
     * URL is returned.<p>
     *
     * @param location location of the resource
     * @param version the version, or null
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
     * Compare strings that can be null.
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
     * resource, or null if no permission is needed.
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
                if (JNLPRuntime.isDebug())
                    ioe.printStackTrace();
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
    public static void clearCache() {

        if (!okToClearCache()) {
            System.err.println(R("CCannotClearCache"));
            return;
        }

        File cacheDir = new File(CacheUtil.cacheDir);
        if (!(cacheDir.isDirectory())) {
            return;
        }

        if (JNLPRuntime.isDebug()) {
            System.err.println("Clearing cache directory: " + cacheDir);
        }
        try {
            cacheDir = cacheDir.getCanonicalFile();
            FileUtils.recursiveDelete(cacheDir, cacheDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a boolean indicating if it ok to clear the netx application cache at this point
     * @return true if the cache can be cleared at this time without problems
     */
    private static boolean okToClearCache() {
        File otherJavawsRunning = new File(JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE));
        try {
            if (otherJavawsRunning.isFile()) {
                FileOutputStream fis = new FileOutputStream(otherJavawsRunning);
                
                FileChannel channel = fis.getChannel();
                if (channel.tryLock() == null) {
                    if (JNLPRuntime.isDebug()) {
                        System.out.println("Other instances of netx are running");
                    }
                    return false;
                }

                if (JNLPRuntime.isDebug()) {
                    System.out.println("No other instances of netx are running");
                }
                return true;

            } else {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("No instance file found");
                }
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param source the source URL
     * @param version the versions to check for
     * @param connection a connection to the URL, or null
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

            if (JNLPRuntime.isDebug())
                System.out.println("isCurrent: " + source + " = " + result);

            return result;
        } catch (Exception ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();

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

        if (JNLPRuntime.isDebug())
            System.out.println("isCached: " + source + " = " + result);

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
     * @param source the source URL
     * @param version the version id of the local file
     * @return the file location in the cache, or null if no versions cached
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
            }
            lruHandler.store();
            lruHandler.unlock();
        }
        return cacheFile;
    }

    /**
     * This will return a File pointing to the location of cache item.
     * 
     * @param urlPath Path of cache item within cache directory.
     * @return File if we have searched before, null otherwise.
     */
    private static File getCacheFileIfExist(File urlPath) {
        synchronized (lruHandler) {
            File cacheFile = null;
            List<Entry<String, String>> entries = lruHandler.getLRUSortedEntries();
            // Start searching from the most recent to least recent.
            for (Entry<String, String> e : entries) {
                final String key = e.getKey();
                final String path = e.getValue();

                if (path != null) {
                    if (pathToURLPath(path).equals(urlPath.getPath())) { // Match found.
                        cacheFile = new File(path);
                        lruHandler.updateEntry(key);
                        break; // Stop searching since we got newest one already.
                    }
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
                        ioe.printStackTrace();
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
            for (int i = 0; i < resources.length; i++) {
                if (!tracker.checkResource(resources[i]))
                    urlList.add(resources[i]);
            }
            URL undownloaded[] = urlList.toArray(new URL[urlList.size()]);

            listener = indicator.getListener(app, title, undownloaded);

            do {
                long read = 0;
                long total = 0;

                for (int i = 0; i < undownloaded.length; i++) {
                    // add in any -1's; they're insignificant
                    total += tracker.getTotalSize(undownloaded[i]);
                    read += tracker.getAmountRead(undownloaded[i]);
                }

                int percent = (int) ((100 * read) / Math.max(1, total));

                for (int i = 0; i < undownloaded.length; i++)
                    listener.progress(undownloaded[i], "version",
                                      tracker.getAmountRead(undownloaded[i]),
                                      tracker.getTotalSize(undownloaded[i]),
                                      percent);
            } while (!tracker.waitForResources(resources, indicator.getUpdateRate()));

            // make sure they read 100% until indicator closes
            for (int i = 0; i < undownloaded.length; i++)
                listener.progress(undownloaded[i], "version",
                                  tracker.getTotalSize(undownloaded[i]),
                                  tracker.getTotalSize(undownloaded[i]),
                                  100);

        } catch (InterruptedException ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();
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
                final String value = e.getValue();

                if (value != null) {
                    File file = new File(value);
                    PropertiesFile pf = new PropertiesFile(new File(value + ".info"));
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
                    } else {
                        curSize += len;
                        keep.add(file.getPath().substring(rStr.length()));

                        for (File f : file.getParentFile().listFiles()) {
                            if (!(f.equals(file) || f.equals(pf.getStoreFile()))){
                                try {
                                    FileUtils.recursiveDelete(f, f);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    lruHandler.removeEntry(key);
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
