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

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.InstallerDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.CacheFile;
import net.adoptopenjdk.icedteaweb.resources.cache.CacheId;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.jnlp.DownloadServiceListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;
import static net.sourceforge.jnlp.util.UrlUtils.JAR_PROTOCOL;

/**
 * Provides static methods to interact with the cache, download
 * indicator, and other utility methods.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
public class CacheUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUtil.class);

    private static final List<String> NON_CACHEABLE_PROTOCOLS = Arrays.asList(FILE_PROTOCOL, JAR_PROTOCOL);

    /**
     * Caches a resource and returns a URL for it in the cache;
     * blocks until resource is cached. If the resource location is
     * not cacheable (points to a local file, etc) then the original
     * URL is returned.
     *
     * @param location location of the resource
     * @param version  the version, or {@code null}
     * @return either the location in the cache or the original location
     */
    public static File downloadAndGetCacheFile(final URL location, final VersionString version) {
        try {
            final ResourceTracker rt = new ResourceTracker();
            rt.addResource(location, version);
            return rt.getCacheFile(location);
        } catch (Exception ex) {
            if (location.toString().startsWith("file:")) {
                try {
                    return new File(location.toURI());
                } catch (URISyntaxException ignored) {
                }
            }
            return null;
        }
    }

    public static void logCacheIds(String filter) {
        List<CacheId> items = Cache.getCacheIds(filter);
        if (JNLPRuntime.isDebug()) {
            for (CacheId id : items) {
                LOG.info("{} ({}) [{}]", id.getId(), id.getType(), id.getFiles().size());
                for (CacheFile cacheFile : id.getFiles()) {
                    final StringBuilder sb = new StringBuilder();
                    final Consumer<Object> appender = v -> sb.append(Optional.ofNullable(v).orElse("??")).append(" ;  ");
                    appender.accept(cacheFile.getInfoFile());
                    appender.accept(cacheFile.getParentFile());
                    appender.accept(cacheFile.getProtocol());
                    appender.accept(cacheFile.getDomain());
                    appender.accept(cacheFile.getSize());
                    appender.accept(cacheFile.getLastModified());
                    appender.accept(cacheFile.getJnlpPath());
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
     * Returns whether the resource can be cached as a local file;
     * if not, then URLConnection.openStream can be used to obtain
     * the contents.
     *
     * @param source  the url of resource
     * @return whether this resource can be cached
     */
    public static boolean isCacheable(URL source) {
        final String protocol = source != null ? source.getProtocol() : null;
        return !NON_CACHEABLE_PROTOCOLS.contains(protocol);
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

        if (location.getPort() > 0) {
            path.append(location.getPort());
        } else {
            path.append(location.getDefaultPort());
        }
        path.append(File.separatorChar);

        final String locationPath = location.getPath();
        final String queryPart = location.getQuery();
        if (locationPath.contains("..") || (queryPart != null && queryPart.contains(".."))) {
            try {
                /**
                 * if path contains .. then it can harm local system
                 * So without mercy, hash it
                 */
                String hexed = hex(new File(locationPath).getName(), locationPath);
                return new File(path.toString(), hexed);
            } catch (NoSuchAlgorithmException ex) {
                // should not occur, cite from javadoc:
                // every java implementation should support
                // MD5 SHA-1 SHA-256
                throw new RuntimeException(ex);
            }
        } else {
            path.append(locationPath.replace('/', File.separatorChar));
            if (!StringUtils.isBlank(queryPart)) {
                path.append(".").append(queryPart);
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
                    candidate = new File(candidate.getParentFile(), hexed);
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
                tracker.waitForResources(resources);
                return;
            }

            // see if resources can be downloaded very quickly; avoids
            // overhead of creating display components for the resources
            if (tracker.waitForResources(resources, indicator.getInitialDelay(), MILLISECONDS)) {
                return;
            }

            // only resources not starting out downloaded are displayed
            final List<URL> urlList = new ArrayList<>();
            for (URL url : resources) {
                if (!tracker.checkResource(url))
                    urlList.add(url);
            }
            final URL[] undownloaded = urlList.toArray(new URL[0]);

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
            } while (!tracker.waitForResources(resources, indicator.getUpdateRate(), MILLISECONDS));

            // make sure they read 100% until indicator closes
            for (URL url : undownloaded) {
                listener.progress(url, "version",
                        tracker.getTotalSize(url),
                        tracker.getTotalSize(url),
                        100);
            }
        } catch (InterruptedException ex) {
            LOG.error("Downloading of resources was interrupted", ex);
        } finally {
            if (indicator != null && listener != null)
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
