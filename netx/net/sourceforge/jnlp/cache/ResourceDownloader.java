package net.sourceforge.jnlp.cache;

import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTING;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static net.sourceforge.jnlp.cache.Resource.Status.PRECONNECT;
import static net.sourceforge.jnlp.cache.Resource.Status.PREDOWNLOAD;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.ConnectionFactory;
import net.sourceforge.jnlp.util.HttpUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

public class ResourceDownloader implements Runnable {

    private final Resource resource;
    private final Object lock;

    public ResourceDownloader(Resource resource, Object lock) {
        this.resource = resource;
        this.lock = lock;
    }

    static int getUrlResponseCode(URL url, Map<String, String> requestProperties, ResourceTracker.RequestMethods requestMethod) throws IOException {
        return getUrlResponseCodeWithRedirectonResult(url, requestProperties, requestMethod).result;
    }

    /**
     * Connects to the given URL, and grabs a response code and redirecton if
     * the URL uses the HTTP protocol, or returns an arbitrary valid HTTP
     * response code.
     *
     * @return the response code if HTTP connection and redirection value, or
     * HttpURLConnection.HTTP_OK and null if not.
     * @throws IOException
     */
    static UrlRequestResult getUrlResponseCodeWithRedirectonResult(URL url, Map<String, String> requestProperties, ResourceTracker.RequestMethods requestMethod) throws IOException {
        UrlRequestResult result = new UrlRequestResult();
        URLConnection connection = ConnectionFactory.getConnectionFactory().openConnection(url);

        for (Map.Entry<String, String> property : requestProperties.entrySet()) {
            connection.addRequestProperty(property.getKey(), property.getValue());
        }

        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod(requestMethod.toString());

            int responseCode = httpConnection.getResponseCode();

            /* Fully consuming current request helps with connection re-use
             * See http://docs.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html */
            HttpUtils.consumeAndCloseConnectionSilently(httpConnection);

            result.result = responseCode;
        }

        Map<String, List<String>> header = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : header.entrySet()) {
            OutputController.getLogger().log("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
        }
        /*
         * Do this only on 301,302,303(?)307,308>
         * Now setting value for all, and lets upper stack to handle it
         */
        String possibleRedirect = connection.getHeaderField("Location");
        if (possibleRedirect != null && possibleRedirect.trim().length() > 0) {
            result.URL = new URL(possibleRedirect);
        }
        ConnectionFactory.getConnectionFactory().disconnect(connection);

        result.lastModified = connection.getLastModified();
        result.length = connection.getContentLengthLong();

        return result;

    }

    @Override
    public void run() {
        if (resource.isSet(PRECONNECT) && !resource.hasFlags(EnumSet.of(ERROR, CONNECTING, CONNECTED))) {
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(CONNECTING));
            resource.fireDownloadEvent(); // fire CONNECTING
            initializeResource();
        }
        if (resource.isSet(PREDOWNLOAD) && !resource.hasFlags(EnumSet.of(ERROR, DOWNLOADING, DOWNLOADED))) {
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(DOWNLOADING));
            resource.fireDownloadEvent(); // fire CONNECTING
            downloadResource();
        }
    }

    private void initializeResource() {
        if (!JNLPRuntime.isOfflineForced() && resource.isConnectable()) {
            initializeOnlineResource();
        } else {
            initializeOfflineResource();
        }
    }

    private void initializeOnlineResource() {
        try {
            UrlRequestResult finalLocation = findBestUrl(resource);
            if (finalLocation != null) {
                initializeFromURL(finalLocation);
            } else {
                initializeOfflineResource();
            }
        } catch (Exception e) {
            OutputController.getLogger().log(e);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire ERROR
        }
    }

    private void initializeFromURL(UrlRequestResult location) throws IOException {
        CacheEntry entry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
        entry.lock();
        try {
            resource.setDownloadLocation(location.URL);
            URLConnection connection = ConnectionFactory.getConnectionFactory().openConnection(location.URL); // this won't change so should be okay not-synchronized
            connection.addRequestProperty("Accept-Encoding", "pack200-gzip, gzip");

            File localFile = CacheUtil.getCacheFile(resource.getLocation(), resource.getDownloadVersion());
            Long size = location.length;
            if (size == null) {
                size = connection.getContentLengthLong();
            }
            Long lm = location.lastModified;
            if (lm == null) {
                lm = connection.getLastModified();
            }

            boolean current = CacheUtil.isCurrent(resource.getLocation(), resource.getRequestVersion(), lm) && resource.getUpdatePolicy() != UpdatePolicy.FORCE;
            if (!current) {
                if (entry.isCached()) {
                    entry.markForDelete();
                    entry.store();
                    // Old entry will still exist. (but removed at cleanup)
                    localFile = CacheUtil.makeNewCacheFile(resource.getLocation(), resource.getDownloadVersion());
                    CacheEntry newEntry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
                    newEntry.lock();
                    entry.unlock();
                    entry = newEntry;
                }
            }

            synchronized (resource) {
                resource.setLocalFile(localFile);
                // resource.connection = connection;
                resource.setSize(size);
                resource.changeStatus(EnumSet.of(PRECONNECT, CONNECTING), EnumSet.of(CONNECTED, PREDOWNLOAD));

                // check if up-to-date; if so set as downloaded
                if (current) {
                    resource.changeStatus(EnumSet.of(PREDOWNLOAD, DOWNLOADING), EnumSet.of(DOWNLOADED));
                }
            }

            // update cache entry
            if (!current) {
                entry.setRemoteContentLength(size);
                entry.setLastModified(lm);
            }

            entry.setLastUpdated(System.currentTimeMillis());
            entry.store();

            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire CONNECTED

            // explicitly close the URLConnection.
            ConnectionFactory.getConnectionFactory().disconnect(connection);
        } finally {
            entry.unlock();
        }
    }

    private void initializeOfflineResource() {
        CacheEntry entry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
        entry.lock();

        try {
            File localFile = CacheUtil.getCacheFile(resource.getLocation(), resource.getDownloadVersion());

            if (localFile != null && localFile.exists()) {
                long size = localFile.length();

                synchronized (resource) {
                    resource.setLocalFile(localFile);
                    resource.setSize(size);
                    resource.changeStatus(EnumSet.of(PREDOWNLOAD, DOWNLOADING), EnumSet.of(DOWNLOADED));
                }
            } else {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "You are trying to get resource " + resource.getLocation().toExternalForm() + " but it is not in cache and could not be downloaded. Attempting to continue, but you may expect failure");
                resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            }

            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire CONNECTED or ERROR

        } finally {
            entry.unlock();
        }

    }

    /**
     * Returns the 'best' valid URL for the given resource. This first adjusts
     * the file name to take into account file versioning and packing, if
     * possible.
     *
     * @param resource the resource
     * @return the best URL, or null if all failed to resolve
     */
    protected UrlRequestResult findBestUrl(Resource resource) {
        DownloadOptions options = resource.getDownloadOptions();
        if (options == null) {
            options = new DownloadOptions(false, false);
        }

        List<URL> urls = new ResourceUrlCreator(resource, options).getUrls();
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Finding best URL for: " + resource.getLocation() + " : " + options.toString());
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "All possible urls for "
                + resource.toString() + " : " + urls);
        for (ResourceTracker.RequestMethods requestMethod : ResourceTracker.RequestMethods.getValidRequestMethods()) {
            for (int i = 0; i < urls.size(); i++) {
                URL url = urls.get(i);
                try {
                    Map<String, String> requestProperties = new HashMap<>();
                    requestProperties.put("Accept-Encoding", "pack200-gzip, gzip");

                    UrlRequestResult response = getUrlResponseCodeWithRedirectonResult(url, requestProperties, requestMethod);
                    if (response.shouldRedirect()) {
                        if (response.URL == null) {
                            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Although " + resource.toString() + " got redirect " + response.result + " code for " + requestMethod + " request for " + url.toExternalForm() + " the target was null. Not following");
                        } else {
                            OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG, "Resource " + resource.toString() + " got redirect " + response.result + " code for " + requestMethod + " request for " + url.toExternalForm() + " adding " + response.URL.toExternalForm() + " to list of possible urls");
                            if (!JNLPRuntime.isAllowRedirect()) {
                                throw new RedirectionException("The resource " + url.toExternalForm() + " is being redirected (" + response.result + ") to " + response.URL.toExternalForm() + ". This is disabled by default. If you wont to allow it, run javaws with -allowredirect parameter.");
                            }
                            urls.add(response.URL);
                        }
                    } else if (response.isInvalid()) {
                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "For " + resource.toString() + " the server returned " + response.result + " code for " + requestMethod + " request for " + url.toExternalForm());
                    } else {
                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "best url for " + resource.toString() + " is " + url.toString() + " by " + requestMethod);
                        if (response.URL == null) {
                            response.URL = url;
                        }
                        return response; /* This is the best URL */

                    }
                } catch (IOException e) {
                    // continue to next candidate
                    OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "While processing " + url.toString() + " by " + requestMethod + " for resource " + resource.toString() + " got " + e + ": ");
                    OutputController.getLogger().log(e);
                }
            }
        }

        /* No valid URL, return null */
        return null;
    }

    private void downloadResource() {
        URLConnection connection = null;
        URL downloadFrom = resource.getDownloadLocation(); //Where to download from
        URL downloadTo = resource.getLocation(); //Where to download to

        try {
            connection = getDownloadConnection(downloadFrom);

            String contentEncoding = connection.getContentEncoding();

            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Downloading " + downloadTo + " using "
                    + downloadFrom + " (encoding : " + contentEncoding + ") ");

            boolean packgz = "pack200-gzip".equals(contentEncoding)
                    || downloadFrom.getPath().endsWith(".pack.gz");
            boolean gzip = "gzip".equals(contentEncoding);

            // It's important to check packgz first. If a stream is both
            // pack200 and gz encoded, then con.getContentEncoding() could
            // return ".gz", so if we check gzip first, we would end up
            // treating a pack200 file as a jar file.
            if (packgz) {
                downloadPackGzFile(resource, connection, new URL(downloadFrom + ".pack.gz"), downloadTo);
            } else if (gzip) {
                downloadGZipFile(resource, connection, new URL(downloadFrom + ".gz"), downloadTo);
            } else {
                downloadFile(resource, connection, downloadTo);
            }

            resource.changeStatus(EnumSet.of(DOWNLOADING), EnumSet.of(DOWNLOADED));
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire DOWNLOADED
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            synchronized (lock) {
                lock.notifyAll();
            }
            resource.fireDownloadEvent(); // fire ERROR
        } finally {
            if (connection != null) {
                ConnectionFactory.getConnectionFactory().disconnect(connection);
            }
        }
    }

    private URLConnection getDownloadConnection(URL location) throws IOException {
        URLConnection con = ConnectionFactory.getConnectionFactory().openConnection(location);
        con.addRequestProperty("Accept-Encoding", "pack200-gzip, gzip");
        con.connect();
        return con;
    }

    private void downloadPackGzFile(Resource resource, URLConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(resource, connection, downloadFrom);

        uncompressPackGz(downloadFrom, downloadTo, resource.getDownloadVersion());
        storeEntryFields(new CacheEntry(downloadTo, resource.getDownloadVersion()), connection.getContentLength(), connection.getLastModified());
    }

    private void downloadGZipFile(Resource resource, URLConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(resource, connection, downloadFrom);

        uncompressGzip(downloadFrom, downloadTo, resource.getDownloadVersion());
        storeEntryFields(new CacheEntry(downloadTo, resource.getDownloadVersion()), connection.getContentLength(), connection.getLastModified());
    }

    private void downloadFile(Resource resource, URLConnection connection, URL downloadLocation) throws IOException {
        CacheEntry downloadEntry = new CacheEntry(downloadLocation, resource.getDownloadVersion());
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Downloading file: " + downloadLocation + " into: " + downloadEntry.getCacheFile().getCanonicalPath());
        if (!downloadEntry.isCurrent(connection.getLastModified())) {
            writeDownloadToFile(resource, downloadLocation, new BufferedInputStream(connection.getInputStream()));
        } else {
            resource.setTransferred(CacheUtil.getCacheFile(downloadLocation, resource.getDownloadVersion()).length());
        }

        storeEntryFields(downloadEntry, connection.getContentLengthLong(), connection.getLastModified());
    }

    private void storeEntryFields(CacheEntry entry, long contentLength, long lastModified) {
        entry.lock();
        try {
            entry.setRemoteContentLength(contentLength);
            entry.setLastModified(lastModified);
            entry.store();
        } finally {
            entry.unlock();
        }
    }

    private void writeDownloadToFile(Resource resource, URL downloadLocation, InputStream in) throws IOException {
        byte buf[] = new byte[1024];
        int rlen;
        try (OutputStream out = CacheUtil.getOutputStream(downloadLocation, resource.getDownloadVersion())) {
            while (-1 != (rlen = in.read(buf))) {
                resource.incrementTransferred(rlen);
                out.write(buf, 0, rlen);
            }

            in.close();
        }
    }

    private void uncompressGzip(URL compressedLocation, URL uncompressedLocation, Version version) throws IOException {
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Extracting gzip: " + compressedLocation + " to " + uncompressedLocation);
        byte buf[] = new byte[1024];
        int rlen;

        try (GZIPInputStream gzInputStream = new GZIPInputStream(new FileInputStream(CacheUtil
                .getCacheFile(compressedLocation, version)))) {
            InputStream inputStream = new BufferedInputStream(gzInputStream);

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(CacheUtil
                    .getCacheFile(uncompressedLocation, version)));

            while (-1 != (rlen = inputStream.read(buf))) {
                outputStream.write(buf, 0, rlen);
            }

            outputStream.close();
            inputStream.close();
        }
    }

    private void uncompressPackGz(URL compressedLocation, URL uncompressedLocation, Version version) throws IOException {
        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Extracting packgz: " + compressedLocation + " to " + uncompressedLocation);

        try (GZIPInputStream gzInputStream = new GZIPInputStream(new FileInputStream(CacheUtil
                .getCacheFile(compressedLocation, version)))) {
            InputStream inputStream = new BufferedInputStream(gzInputStream);

            JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(CacheUtil
                    .getCacheFile(uncompressedLocation, version)));

            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            unpacker.unpack(inputStream, outputStream);

            outputStream.close();
            inputStream.close();
        }
    }

    /**
     * Complex wrapper around url request Contains return code (default is
     * HTTP_OK), length and last modified
     *
     * The storing of redirect target is quite obvious The storing length and
     * last modified may be not, but appearently
     * (http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=2591) the url
     * conenction is not always chaced as expected, and so another request may
     * be sent when length and lastmodified are checked
     *
     */
    static class UrlRequestResult {

        int result = HttpURLConnection.HTTP_OK;
        URL URL;

        Long lastModified;
        Long length;

        public UrlRequestResult() {
        }

        public UrlRequestResult(URL URL) {
            this.URL = URL;
        }

        URL getURL() {
            return URL;
        }

        /**
         * @return whether the result code is redirect one. Rigth now 301-303
         * and 307-308
         */
        public boolean shouldRedirect() {
            return (result == 301
                    || result == 302
                    || result == 303/*?*/
                    || result == 307
                    || result == 308);
        }

        /**
         * @return whether the return code is OK one - anything except <200,300)
         */
        public boolean isInvalid() {
            return (result < 200 || result >= 300);
        }

        @Override
        public String toString() {
            return ""
                    + "url: " + (URL == null ? "null" : URL.toExternalForm()) + "; "
                    + "result:" + result + "; "
                    + "lastModified: " + (lastModified == null ? "null" : lastModified.toString()) + "; "
                    + "length: " + length == null ? "null" : length.toString() + "; ";
        }
    }

    private static class RedirectionException extends RuntimeException {

        public RedirectionException(String string) {
            super(string);
        }

        public RedirectionException(Throwable cause) {
            super(cause);
        }

    }

}
