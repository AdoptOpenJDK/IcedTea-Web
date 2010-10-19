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

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.lang.reflect.*;
import java.security.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.*;
import net.sourceforge.jnlp.runtime.*;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {

    private static String R(String key) {
        return JNLPRuntime.getMessage(key);
    }

    private static String R(String key, Object param) {
        return JNLPRuntime.getMessage(key, new Object[] {param});
    }

    /**
     * Compares a URL using string compare of its protocol, host,
     * port, path, query, and anchor.  This method avoids the host
     * name lookup that URL.equals does for http: protocol URLs.
     * It may not return the same value as the URL.equals method
     * (different hostnames that resolve to the same IP address,
     * ie sourceforge.net and www.sourceforge.net).
     */
    public static boolean urlEquals(URL u1, URL u2) {
        if (u1==u2)
            return true;
        if (u1==null || u2==null)
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
        rt.addResource(location, version, policy);
        try {
            File f = rt.getCacheFile(location);
            return f.toURL();
        }
        catch (MalformedURLException ex) {
            return location;
        }
    }

    /**
     * Compare strings that can be null.
     */
    private static boolean compare(String s1, String s2, boolean ignore) {
        if (s1==s2)
            return true;
        if (s1==null || s2==null)
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
        }
        else {
            try {
                // this is what URLClassLoader does
                return location.openConnection().getPermission();
            }
            catch (java.io.IOException ioe) {
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

        File cacheDir = new File(JNLPRuntime.getBaseDir() + File.separator + "cache");
        if (!(cacheDir.isDirectory())) {
            return;
        }

        if (JNLPRuntime.isDebug()) {
            System.err.println("Clearing cache directory: " + cacheDir);
        }
        try {
            FileUtils.recursiveDelete(cacheDir, JNLPRuntime.getBaseDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a boolean indicating if it ok to clear the netx application cache at this point
     * @return true if the cache can be cleared at this time without problems
     */
    private static boolean okToClearCache() {
        File otherJavawsRunning = new File(JNLPRuntime.NETX_RUNNING_FILE);
        try {
            if (otherJavawsRunning.isFile()) {
                FileOutputStream fis = new FileOutputStream(otherJavawsRunning);
                try {
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

                } finally {
                    fis.close();
                }
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
                System.out.println("isCurrent: "+source+" = "+result);

            return result;
        }
        catch (Exception ex) {
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
            System.out.println("isCached: "+source+" = "+result);

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

        try {
            File localFile = urlToPath(source, "cache");
            localFile.getParentFile().mkdirs();

            return localFile;
        }
        catch (Exception ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();

            return null;
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
        }
        finally {
            is.close();
            os.close();
        }
    }

    /**
     * Converts a URL into a local path string within the runtime's
     * base directory.
     *
     * @param location the url
     * @param subdir subdirectory under the base directory
     * @return the file
     */
    public static File urlToPath(URL location, String subdir) {
        StringBuffer path = new StringBuffer();

        if (subdir != null) {
            path.append(subdir);
            path.append(File.separatorChar);
        }

        path.append(location.getProtocol());
        path.append(File.separatorChar);
        path.append(location.getHost());
        path.append(File.separatorChar);
        path.append(location.getPath().replace('/', File.separatorChar));

        return new File(JNLPRuntime.getBaseDir(), FileUtils.sanitizePath(path.toString()));
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
            List urlList = new ArrayList();
            for (int i=0; i < resources.length; i++) {
                if (!tracker.checkResource(resources[i]))
                    urlList.add(resources[i]);
            }
            URL undownloaded[] = (URL[]) urlList.toArray( new URL[urlList.size()] );

            listener = indicator.getListener(app, title, undownloaded);

            do {
                long read = 0;
                long total = 0;

                for (int i=0; i < undownloaded.length; i++) {
                    // add in any -1's; they're insignificant
                    total += tracker.getTotalSize(undownloaded[i]);
                    read += tracker.getAmountRead(undownloaded[i]);
                }

                int percent = (int)( (100*read)/Math.max(1,total) );

                for (int i=0; i < undownloaded.length; i++)
                    listener.progress(undownloaded[i], "version",
                                      tracker.getAmountRead(undownloaded[i]),
                                      tracker.getTotalSize(undownloaded[i]),
                                      percent);
            }
            while (!tracker.waitForResources(resources, indicator.getUpdateRate()));

            // make sure they read 100% until indicator closes
            for (int i=0; i < undownloaded.length; i++)
                listener.progress(undownloaded[i], "version",
                                  tracker.getTotalSize(undownloaded[i]),
                                  tracker.getTotalSize(undownloaded[i]),
                                  100);

        }
        catch (InterruptedException ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();
        }
        finally {
            if (listener != null)
                indicator.disposeListener(listener);
        }
    }

}
