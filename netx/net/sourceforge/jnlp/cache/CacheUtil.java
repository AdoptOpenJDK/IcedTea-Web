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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.jnlp.DownloadServiceListener;

import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.controlpanel.CachePane;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import static net.sourceforge.jnlp.runtime.Translator.R;

import net.sourceforge.jnlp.security.ConnectionFactory;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.PropertiesFile;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {



    /**
     * Caches a resource and returns a URL for it in the cache;
     * blocks until resource is cached. If the resource location is
     * not cacheable (points to a local file, etc) then the original
     * URL is returned.
     *
     * @param location location of the resource
     * @param version the version, or {@code null}
     * @param policy how to handle update
     * @return either the location in the cache or the original location
     */
    public static URL getCachedResourceURL(URL location, Version version, UpdatePolicy policy) {
        try {
            File f = getCachedResourceFile(location, version, policy);
            //url was ponting to nowhere eg 404
            if (f == null){
                //originally  f.toUrl was throwing NPE
                return null;
                //returning null seems to be better
            }
            // TODO: Should be toURI().toURL()
            return f.toURL();
        } catch (MalformedURLException ex) {
            return location;
        }
    }
    
    /**
     * This is returning File object of cached resource originally from URL
     * @param location original location of blob
     * @param version version of resource
     * @param policy update policy of resource
     * @return location in ITW cache on filesystem 
     */
    public static File  getCachedResourceFile(URL location, Version version, UpdatePolicy policy) {
        ResourceTracker rt = new ResourceTracker();
        rt.addResource(location, version, null, policy);
        File f = rt.getCacheFile(location);
        return f;
    }

    /**
     * Returns the Permission object necessary to access the
     * resource, or {@code null} if no permission is needed.
     * @param location location of the resource
     * @param version the version, or {@code null}
     * @return permissions of the location
     */
    public static Permission getReadPermission(URL location, Version version) {
        Permission result = null;
        if (CacheUtil.isCacheable(location, version)) {
            File file = CacheUtil.getCacheFile(location, version);
            result = new FilePermission(file.getPath(), "read");
        } else {
            try {
                // this is what URLClassLoader does
                URLConnection conn = ConnectionFactory.getConnectionFactory().openConnection(location);
                result = conn.getPermission();
                 ConnectionFactory.getConnectionFactory().disconnect(conn);                
            } catch (java.io.IOException ioe) {
                // should try to figure out the permission
                OutputController.getLogger().log(ioe);
            }
        }

        return result;
    }

    /**
     * Clears the cache by deleting all the Netx cache files
     *
     * Note: Because of how our caching system works, deleting jars of another javaws
     * process is using them can be quite disasterous. Hence why Launcher creates lock files
     * and we check for those by calling {@link #okToClearCache()}
     * @return true if the cache could be cleared and was cleared
     */
    public static boolean clearCache() {
        // clear all cache
        CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        File cacheDir = lruHandler.getCacheDir().getFile();

        if (!checkToClearCache()) {
            return false;
        }

        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Clearing cache directory: " + cacheDir);
        synchronized (lruHandler) {
        lruHandler.lock();
        try {
            cacheDir = cacheDir.getCanonicalFile();
            // remove windows shortcuts before cache dir is gone
            if (JNLPRuntime.isWindows()) {
                removeWindowsShortcuts("ALL");
            }
            FileUtils.recursiveDelete(cacheDir, cacheDir);
            cacheDir.mkdir();
            lruHandler.clearLRUSortedEntries();
            lruHandler.store();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lruHandler.unlock();
        }
        }
        return true;
    }

    public static boolean clearCache(final String application, boolean jnlpPath, boolean domain) {
        // clear one app
        if (!checkToClearCache()) {
            return false;
        }

        OutputController.getLogger().log(OutputController.Level.WARNING_ALL, Translator.R("BXSingleCacheCleared", application));
        List<CacheId> ids = getCacheIds(".*", jnlpPath, domain);
        int found = 0;
        int files = 0;
        for (CacheId id : ids) {
            if (id.getId().equalsIgnoreCase(application)) {
                found++;
                files += id.files.size();
            }
        }
        if (found == 0) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, Translator.R("BXSingleCacheClearNotFound", application));
        }
        if (found > 1) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, Translator.R("BXSingleCacheMoreThenOneId", application));
        }
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("BXSingleCacheFileCount", files));
        final CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        synchronized (lruHandler) {
        lruHandler.lock();
        try {
            Files.walk(Paths.get(lruHandler.getCacheDir().getFile().getCanonicalPath())).filter(new Predicate<Path>() {
                @Override
                public boolean test(Path t) {
                    return Files.isRegularFile(t);
                }
            }).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    if (path.getFileName().toString().endsWith(CacheDirectory.INFO_SUFFIX)) {
                        PropertiesFile pf = new PropertiesFile(new File(path.toString()));
                        // if jnlp-path in .info equals path of app to delete mark to delete
                        String jnlpPath = pf.getProperty(CacheEntry.KEY_JNLP_PATH);
                        if (application.equalsIgnoreCase(jnlpPath) || application.equalsIgnoreCase(getDomain(path))) {
                            pf.setProperty("delete", "true");
                            pf.store();
                            OutputController.getLogger().log("marked for deletion: " + path);
                        }
                    }
                }
            });
            if (JNLPRuntime.isWindows()) {
                removeWindowsShortcuts(application.toLowerCase());
            }
            // clean the cache of entries now marked for deletion
            cleanCache();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lruHandler.unlock();
        }
        }
        return true;
    }

    public static boolean checkToClearCache() {
        if (!okToClearCache()) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, R("CCannotClearCache"));
            return false;
        }
        return CacheLRUWrapper.getInstance().getCacheDir().getFile().isDirectory();
    }

    public static void removeWindowsShortcuts(String jnlpApp)
            throws IOException {
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Clearing Windows shortcuts");
        if (CacheLRUWrapper.getInstance().getWindowsShortcutList().exists()) {
            List<String> lines = Files.readAllLines(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath(), Charset.forName("UTF-8"));
            Iterator it = lines.iterator();
            Boolean fDelete;
            while (it.hasNext()) {
                String sItem = it.next().toString();
                String[] sArray = sItem.split(",");
                String application = sArray[0];
                String sPath = sArray[1];
                // if application is codebase then delete files
                if (application.equalsIgnoreCase(jnlpApp)) {
                    fDelete = true;
                    it.remove();
                } else {
                    fDelete = false;
                }
                if (jnlpApp.equals("ALL")) {
                    fDelete = true;
                }
                if (fDelete) {
                    OutputController.getLogger().log("Deleting item = " + sPath);
                    File scList = new File(sPath);
                    try {
                        FileUtils.recursiveDelete(scList, scList);
                    } catch (Exception e) {
                        OutputController.getLogger().log(e);
                    }
                }
            }
            if (jnlpApp.equals("ALL")) {
                //delete shortcut list file
                Files.deleteIfExists(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath());
            } else {
                //write file after application scuts have been removed
                Files.write(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath(), lines, Charset.forName("UTF-8"));
            }
        }

    }
    
     public static void listCacheIds(String filter, boolean jnlpPath, boolean domain) {
         List<CacheId> items = getCacheIds(filter, jnlpPath, domain);
         if (JNLPRuntime.isDebug()) {
             for (CacheId id : items) {
                 OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, id.getId()+" ("+id.getType()+") ["+id.files.size()+"]");
                 for(Object[] o: id.getFiles()){
                     StringBuilder sb = new StringBuilder();
                     for (int i = 0; i < o.length; i++) {
                         Object object = o[i];
                         if (object == null) {
                             object = "??";
                         }
                         sb.append(object.toString()).append(" ;  ");
                     }
                     OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "  * " + sb);
                 }
             }
         } else {
             for (CacheId id : items) {
                 OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, id.getId());
             }
         }
     }
     
     /**
      * This method load all known IDs of applications and  will gather all members, which share the id
     * @param filter - regex to filter keys
      * @return 
      */
      public static List<CacheId> getCacheIds(final String filter, final boolean jnlpPath, final boolean domain) {
        final List<CacheId> r = new ArrayList<>();
        final CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        synchronized (lruHandler) {
        lruHandler.lock();
        try {
            Files.walk(Paths.get(lruHandler.getCacheDir().getFile().getCanonicalPath())).filter(new Predicate<Path>() {
                @Override
                public boolean test(Path t) {
                    return Files.isRegularFile(t);
                }
            }).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    if (path.getFileName().toString().endsWith(CacheDirectory.INFO_SUFFIX)) {
                        PropertiesFile pf = new PropertiesFile(new File(path.toString()));
                        if (jnlpPath) {
                            // if jnlp-path in .info equals path of app to delete mark to delete
                            String jnlpPath = pf.getProperty(CacheEntry.KEY_JNLP_PATH);
                            if (jnlpPath != null && jnlpPath.matches(filter)) {
                                CacheId jnlpPathId = new CacheJnlpId(jnlpPath);
                                if (!r.contains(jnlpPathId)) {
                                    r.add(jnlpPathId);
                                    jnlpPathId.populate();

                                }
                            }
                        }
                        if (domain) {
                            String domain = getDomain(path);
                            if (domain != null && domain.matches(filter)) {
                                CacheId doaminId = new CacheDomainId(domain);
                                if (!r.contains(doaminId)) {
                                    r.add(doaminId);
                                    doaminId.populate();

                                }
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lruHandler.unlock();
        }
        }
        return r;
    }

    /**
     * Returns a boolean indicating if it ok to clear the netx application cache at this point
     * @return true if the cache can be cleared at this time without problems
     */
    private static boolean okToClearCache() {
        File otherJavawsRunning = PathsAndFiles.MAIN_LOCK.getFile();
        FileLock locking = null;
        try {
            if (otherJavawsRunning.isFile()) {
                FileOutputStream fis = new FileOutputStream(otherJavawsRunning);
                
                FileChannel channel = fis.getChannel();
                locking  = channel.tryLock();
                if (locking == null) {
                    OutputController.getLogger().log("Other instances of javaws are running");
                    return false;
                }
                OutputController.getLogger().log("No other instances of javaws are running");
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
     * @param lastModifed time in milis since epoch of last modfication
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static boolean isCurrent(URL source, Version version, long lastModifed, CacheEntry entry, File cachedFile) {

        if (!isCacheable(source, version))
            throw new IllegalArgumentException(R("CNotCacheable", source));

        try {
            boolean result = entry.isCurrent(lastModifed, cachedFile);

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
     * @param source the url of resource
     * @param version version of resource
     * @return whether this resource can be cached
     */
    public static boolean isCacheable(URL source, Version version) {
        if (source == null)
            return false;

        if (source.getProtocol().equals("file")){
            return false;
        }
        if (source.getProtocol().equals("jar")){
            return false;
        }
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
        CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        synchronized (lruHandler) {
            try {
                lruHandler.lock();

                // We need to reload the cacheOrder file each time
                // since another plugin/javaws instance may have updated it.
                lruHandler.load();
                cacheFile = getCacheFileIfExist(urlToPath(source, ""));
                if (cacheFile == null) { // We did not find a copy of it.
                    cacheFile = makeNewCacheFile(source, version);
                } else
                    lruHandler.store();
            } finally {
                lruHandler.unlock();
            }
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
        CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
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
        int len = CacheLRUWrapper.getInstance().getCacheDir().getFullPath().length();
        int index = path.indexOf(File.separatorChar, len + 1);
        return path.substring(index);
    }

    /**
     * Returns the parent directory of the cached resource.
     * @param filePath The path of the cached resource directory.
     * @return parent dir of cache
     */
    public static String getCacheParentDirectory(String filePath) {
        String path = filePath;
        String tempPath;
        String cacheDir = CacheLRUWrapper.getInstance().getCacheDir().getFullPath();

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
        CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        synchronized (lruHandler) {
            File cacheFile = null;
            try {
                lruHandler.lock();
                lruHandler.load();
                for (long i = 0; i < Long.MAX_VALUE; i++) {
                    String path = lruHandler.getCacheDir().getFullPath()+ File.separator + i;
                    File cDir = new File(path);
                    if (!cDir.exists()) {
                        // We can use this directory.
                        try {
                            cacheFile = urlToPath(source, path);
                            FileUtils.createParentDir(cacheFile);
                            File pf = new File(cacheFile.getPath() + CacheDirectory.INFO_SUFFIX);
                            FileUtils.createRestrictedFile(pf, true); // Create the info file for marking later.
                            lruHandler.addEntry(lruHandler.generateKey(cacheFile.getPath()), cacheFile.getPath());
                        } catch (IOException ioe) {
                            OutputController.getLogger().log(ioe);
                        }

                        break;
                    }
                }

                lruHandler.store();
            } finally {
                lruHandler.unlock();
            }
            return cacheFile;
        }
    }

    /**
     * Returns a buffered output stream open for writing to the
     * cache file.
     *
     * @param source the remote location
     * @param version the file version to write to
     * @return the stream to write to resource
     * @throws java.io.IOException if IO breaks
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
     * @param is stream to read from
     * @param os stream to write to
     * @throws java.io.IOException if copy fails
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

        StringBuilder path = new StringBuilder();

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
                return new File(subdir, path.append(File.separatorChar).append(hexed).toString());
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

            File candidate = new File(subdir, FileUtils.sanitizePath(path.toString()));
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
        byte[] sum = md.digest(candidate.getBytes(StandardCharsets.UTF_8));
        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < sum.length; i++) {
            hexString.append(Integer.toHexString(0xFF & sum[i]));
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
     *
     * @param app application instance with context for this resource
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
            List<URL> urlList = new ArrayList<>();
            for (URL url : resources) {
                if (!tracker.checkResource(url))
                    urlList.add(url);
            }
            URL undownloaded[] = urlList.toArray(new URL[urlList.size()]);

            final int maxUrls = Integer.parseInt(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_MAX_URLS_DOWNLOAD_INDICATOR));

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

                int urlCounter = 0;
                for (URL url : undownloaded) {
                    if (urlCounter > maxUrls) {
                        break;
                    }
                    listener.progress(url, "version",
                                      tracker.getAmountRead(url),
                                      tracker.getTotalSize(url),
                                      percent);
                    urlCounter += 1;
                }
            } while (!tracker.waitForResources(resources, indicator.getUpdateRate()));

            // make sure they read 100% until indicator closes
            int urlCounter = 0;
            for (URL url : undownloaded) {
                if (urlCounter > maxUrls) {
                    break;
                }
                listener.progress(url, "version",
                                  tracker.getTotalSize(url),
                                  tracker.getTotalSize(url),
                                  100);
                urlCounter += 1;
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
        CacheLRUWrapper lruHandler = CacheLRUWrapper.getInstance();
        if (okToClearCache()) {
            // First we want to figure out which stuff we need to delete.
            HashSet<String> keep = new HashSet<>();
            HashSet<String> remove = new HashSet<>();
            synchronized (lruHandler) {
            try {
                lruHandler.lock();
                lruHandler.load();

                long maxSize = -1; // Default
                try {
                    maxSize = Long.parseLong(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_CACHE_MAX_SIZE));
                } catch (NumberFormatException nfe) {
                }

                maxSize = maxSize << 20; // Convert from megabyte to byte (Negative values will be considered unlimited.)
                long curSize = 0;

                for (Entry<String, String> e : lruHandler.getLRUSortedEntries()) {
                    // Check if the item is contained in cacheOrder.
                    final String key = e.getKey();
                    final String path = e.getValue();

                    File file = new File(path);
                    PropertiesFile pf = new PropertiesFile(new File(path + CacheDirectory.INFO_SUFFIX));
                    boolean delete = Boolean.parseBoolean(pf.getProperty("delete"));

                /*
                 * This will get me the root directory specific to this cache item.
                 * Example:
                 *  cacheDir = /home/user1/.icedtea/cache
                 *  file.getPath() = /home/user1/.icedtea/cache/0/http/www.example.com/subdir/a.jar
                 *  rStr first becomes: /0/http/www.example.com/subdir/a.jar
                 *  then rstr becomes: /home/user1/.icedtea/cache/0
                 */
                    String rStr = file.getPath().substring(lruHandler.getCacheDir().getFullPath().length());
                    rStr = lruHandler.getCacheDir().getFullPath()+ rStr.substring(0, rStr.indexOf(File.separatorChar, 1));
                    long len = file.length();

                    if (keep.contains(path)) {
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
                    keep.add(path);

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
            } finally {
                lruHandler.unlock();
            }
            }
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

    static class CacheJnlpId extends CacheId {

        public CacheJnlpId(String id) {
            super(id);
        }

        @Override
        public void populate() {
            ArrayList<Object[]> all = CachePane.generateData();
            for (Object[] object : all) {
                if (id.equals(object[6])) {
                    this.files.add(object);
                }
            }
        }

        @Override
        String getType() {
            return "JNLP-PATH";
        }

        @Override
        //hascode in super is ok
        public boolean equals(Object obj) {
            if (obj instanceof CacheJnlpId) {
                return super.equals(obj);
            } else {
                return false;
            }
        }

    }

    static class CacheDomainId extends CacheId {

        public CacheDomainId(String id) {
            super(id);
        }

        @Override
        public void populate() {
            ArrayList<Object[]> all = CachePane.generateData();
            for (Object[] object : all) {
                if (id.equals(object[3].toString())) {
                    this.files.add(object);
                }
            }
        }

        @Override
        String getType() {
            return "DOMAIN";
        }

        @Override
        //hascode in super is ok
        public boolean equals(Object obj) {
            if (obj instanceof CacheDomainId) {
                return super.equals(obj);
            } else {
                return false;
            }
        }

    }

    public abstract static class CacheId {

        //last century array of objects instead of some nice class inherited from previous century
        protected final List<Object[]> files = new ArrayList<>();

        abstract void populate();

        abstract String getType();

        protected final String id;

        public CacheId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }   

        public List<Object[]> getFiles() {
            return files;
        }

        public String getId() {
            return id;
        }
        
        
        

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CacheId) {
                CacheId c = (CacheId) obj;
                if (c.id == null && this.id == null) {
                    return true;
                }
                if (c.id == null) {
                    return false;
                }
                return c.id.equals(this.id);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.id);
        }

    }

    private static String getDomain(Path path) {
        String relativeToCache = path.toAbsolutePath().toString().replace(CacheLRUWrapper.getInstance().getCacheDir().getFullPath(), "");
        for (int x = 0; x < 3; x++) {
            int i = relativeToCache.indexOf(File.separator);
            relativeToCache = relativeToCache.substring(i + 1);
        }
        int i = relativeToCache.indexOf(File.separator);
        relativeToCache = relativeToCache.substring(0, i);
        return relativeToCache;
    }
}
