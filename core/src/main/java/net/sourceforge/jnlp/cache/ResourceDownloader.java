package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.InetSecurity511Panel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.http.HttpUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTING;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static net.sourceforge.jnlp.cache.Resource.Status.PRECONNECT;
import static net.sourceforge.jnlp.cache.Resource.Status.PREDOWNLOAD;

public class ResourceDownloader implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDownloader.class);

    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String PACK_200_OR_GZIP = "pack200-gzip, gzip";

    private static final HttpMethod[] validRequestMethods = {HttpMethod.HEAD, HttpMethod.GET};

    private final Resource resource;
    private final Object lock;

    public ResourceDownloader(Resource resource, Object lock) {
        this.resource = resource;
        this.lock = lock;
    }

    /**
     * Connects to the given URL, and grabs a response code and redirection if
     * the URL uses the HTTP protocol, or returns an arbitrary valid HTTP
     * response code.
     *
     * @return the response code if HTTP connection and redirection value, or
     * HttpURLConnection.HTTP_OK and null if not.
     * @throws IOException if an I/O exception occurs.
     */
    static UrlRequestResult getUrlResponseCodeWithRedirectionResult(final URL url, final Map<String, String> requestProperties, final HttpMethod requestMethod) throws IOException {

        try (final CloseableConnection connection = ConnectionFactory.openConnection(url, requestMethod, requestProperties)) {

            final int responseCode = connection.getResponseCode();

            /* Fully consuming current request helps with connection re-use
             * See http://docs.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html */
            HttpUtils.consumeAndCloseConnectionSilently(connection);

            final Map<String, List<String>> header = connection.getHeaderFields();
            for (final Map.Entry<String, List<String>> entry : header.entrySet()) {
                LOG.info("Key : {} ,Value : {}", entry.getKey(), entry.getValue());
            }
            /*
             * Do this only on 301,302,303(?)307,308>
             * Now setting value for all, and lets upper stack to handle it
             */
            final String possibleRedirect = connection.getHeaderField("Location");

            final URL redirectUrl;
            if (possibleRedirect != null && possibleRedirect.trim().length() > 0) {
                redirectUrl = new URL(possibleRedirect);
            } else {
                redirectUrl = null;
            }

            final long lastModified = connection.getLastModified();
            final long length = connection.getContentLength();

            return new UrlRequestResult(responseCode, redirectUrl, lastModified, length);
        }

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
            final UrlRequestResult finalLocation = findBestUrl(resource);
            if (finalLocation != null) {
                initializeFromURL(finalLocation);
            } else {
                initializeOfflineResource();
            }
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire ERROR
        }
    }

    private void initializeFromURL(final UrlRequestResult location) throws IOException {
        CacheEntry entry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
        entry.lock();
        try (final CloseableConnection connection = getDownloadConnection(location.redirectUrl)) {// this won't change so should be okay not-synchronized
            resource.setDownloadLocation(location.redirectUrl);

            File localFile = CacheUtil.getCacheFile(resource.getLocation(), resource.getDownloadVersion());
            Long size = location.length;
            if (size == null) {
                size = connection.getContentLength();
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
            try {
                //do not die here no matter of cost. Just metadata
                //is the path from user best to store? He can run some jnlp from temp which then be stored
                //on contrary, this downloads the jnlp, we actually do not have jnlp parsed during first interaction
                //in addition, downloaded name can be really nasty (some generated has from dynamic servlet.jnlp)
                //another issue is forking. If this (eg local) jnlp starts its second instance, the url *can* be different
                //in contrary, usually si no. as fork is reusing all args, and only adding xmx/xms and xnofork.
                String jnlpPath = Boot.getOptionParser().getMainArg(); //get jnlp from args passed 
                if (jnlpPath == null || jnlpPath.equals("")) {
                    jnlpPath = Boot.getOptionParser().getParam(CommandLineOptions.JNLP);
                    if (jnlpPath == null || jnlpPath.equals("")) {
                        jnlpPath = Boot.getOptionParser().getParam(CommandLineOptions.HTML);
                        if (jnlpPath == null || jnlpPath.equals("")) {
                            LOG.info("Not-setting jnlp-path for missing main/jnlp/html argument");
                        } else {
                            entry.setJnlpPath(jnlpPath);
                        }
                    } else {
                        entry.setJnlpPath(jnlpPath);
                    }
                } else {
                    entry.setJnlpPath(jnlpPath);
                }
            } catch (Exception ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
            entry.store();

            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire CONNECTED
        } finally {
            entry.unlock();
        }
    }

    private void initializeOfflineResource() {
        final CacheEntry entry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
        entry.lock();

        try {
            final File localFile = CacheUtil.getCacheFile(resource.getLocation(), resource.getDownloadVersion());

            if (localFile != null && localFile.exists()) {
                long size = localFile.length();

                synchronized (resource) {
                    resource.setLocalFile(localFile);
                    resource.setSize(size);
                    resource.changeStatus(EnumSet.of(PREDOWNLOAD, DOWNLOADING), EnumSet.of(DOWNLOADED));
                }
            } else {
                LOG.warn("You are trying to get resource {} but it is not in cache and could not be downloaded. Attempting to continue, but you may expect failure", resource.getLocation().toExternalForm());
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
    protected UrlRequestResult findBestUrl(final Resource resource) {
        DownloadOptions options = resource.getDownloadOptions();
        if (options == null) {
            options = new DownloadOptions(false, false);
        }

        List<URL> urls = new ResourceUrlCreator(resource, options).getUrls();
        LOG.debug("Finding best URL for: {} : {}", resource.getLocation(), options.toString());
        LOG.debug("All possible urls for {} : {}", resource.toString(), urls);
        for (final HttpMethod requestMethod : validRequestMethods) {
            for (int i = 0; i < urls.size(); i++) {
                URL url = urls.get(i);
                try {
                    Map<String, String> requestProperties = new HashMap<>();
                    requestProperties.put(ACCEPT_ENCODING, PACK_200_OR_GZIP);

                    UrlRequestResult response = getUrlResponseCodeWithRedirectionResult(url, requestProperties, requestMethod);
                    if (response.responseCode == 511) {
                        if (!InetSecurity511Panel.isSkip()) {

                            boolean result511 = SecurityDialogs.show511Dialogue(resource);
                            if (!result511) {
                                throw new RuntimeException("Terminated on users request after encountering 'http 511 authentication'.");
                            }
                            //try again, what to do with original resource was nowhere specified
                            i--;
                            continue;
                        }
                    }
                    if (response.shouldRedirect()) {
                        if (response.redirectUrl == null) {
                            LOG.debug("Although {} got redirect {} code for {} request for {} the target was null. Not following", resource.toString(), response.responseCode, requestMethod, url.toExternalForm());
                        } else {
                            LOG.debug("Resource {} got redirect {} code for {} request for {} adding {} to list of possible urls", resource.toString(), response.responseCode, requestMethod, url.toExternalForm(), response.redirectUrl.toExternalForm());
                            if (!JNLPRuntime.isAllowRedirect()) {
                                throw new RedirectionException("The resource " + url.toExternalForm() + " is being redirected (" + response.responseCode + ") to " + response.redirectUrl.toExternalForm() + ". This is disabled by default. If you wont to allow it, run javaws with -allowredirect parameter.");
                            }
                            urls.add(response.redirectUrl);
                        }
                    } else if (response.isInvalid()) {
                        LOG.debug("For {} the server returned {} code for {} request for {}", resource.toString(), response.responseCode, requestMethod, url.toExternalForm());
                    } else {
                        LOG.debug("best url for {} is {} by {}", resource.toString(), url.toString(), requestMethod);
                        if (response.redirectUrl == null) {
                            return response.withRedirectUrl(url);
                        }
                        return response; /* This is the best URL */

                    }
                } catch (IOException e) {
                    // continue to next candidate
                    LOG.error("While processing " + url.toString() + " by " + requestMethod + " for resource " + resource.toString() + " got " + e + ": ", e);
                }
            }
        }

        /* No valid URL, return null */
        return null;
    }

    private void downloadResource() {
        URL downloadFrom = resource.getDownloadLocation(); //Where to download from
        URL downloadTo = resource.getLocation(); //Where to download to

        try (final CloseableConnection connection = getDownloadConnection(downloadFrom)) {

            String contentEncoding = connection.getContentEncoding();

            LOG.debug("Downloading {} using {} (encoding : {})", downloadTo, downloadFrom, contentEncoding);

            boolean packgz = "pack200-gzip".equals(contentEncoding)
                    || downloadFrom.getPath().endsWith(".pack.gz");
            boolean gzip = "gzip".equals(contentEncoding);

            // It's important to check packgz first. If a stream is both
            // pack200 and gz encoded, then con.getContentEncoding() could
            // return ".gz", so if we check gzip first, we would end up
            // treating a pack200 file as a jar file.
            if (packgz) {
                downloadPackGzFile(connection, downloadFrom, downloadTo);
            } else if (gzip) {
                downloadGZipFile(connection, downloadFrom, downloadTo);
            } else {
                downloadFile(connection, downloadTo);
            }

            resource.changeStatus(EnumSet.of(DOWNLOADING), EnumSet.of(DOWNLOADED));
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
            resource.fireDownloadEvent(); // fire DOWNLOADED
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            synchronized (lock) {
                lock.notifyAll();
            }
            resource.fireDownloadEvent(); // fire ERROR
        }
    }

    private CloseableConnection getDownloadConnection(URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ACCEPT_ENCODING, PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties);
    }

    private void downloadPackGzFile(CloseableConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(connection, downloadFrom);

        extractPackGz(downloadFrom, downloadTo, resource.getDownloadVersion());
        CacheEntry entry = new CacheEntry(downloadTo, resource.getDownloadVersion());
        storeEntryFields(entry, entry.getCacheFile().length(), connection.getLastModified());
        markForDelete(downloadFrom);
    }

    private void downloadGZipFile(CloseableConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(connection, downloadFrom);

        extractGzip(downloadFrom, downloadTo, resource.getDownloadVersion());
        CacheEntry entry = new CacheEntry(downloadTo, resource.getDownloadVersion());
        storeEntryFields(entry, entry.getCacheFile().length(), connection.getLastModified());
        markForDelete(downloadFrom);
    }

    private void downloadFile(CloseableConnection connection, URL downloadLocation) throws IOException {
        CacheEntry downloadEntry = new CacheEntry(downloadLocation, resource.getDownloadVersion());
        LOG.debug("Downloading file: {} into: {}", downloadLocation, downloadEntry.getCacheFile().getCanonicalPath());
        if (!downloadEntry.isCurrent(connection.getLastModified())) {
            try {
                writeDownloadToFile(downloadLocation, new BufferedInputStream(connection.getInputStream()));
            } catch (IOException ex) {
                String IH = "Invalid Http response";
                if (ex.getMessage().equals(IH)) {
                    LOG.error("'" + IH + "' message detected. Attempting direct socket", ex);
                    Object[] result = UrlUtils.loadUrlWithInvalidHeaderBytes(connection.getURL());
                    LOG.info("Header of: {} ({})", connection.getURL(), downloadLocation);
                    String head = (String) result[0];
                    byte[] body = (byte[]) result[1];
                    LOG.info(head);
                    LOG.info("Body is: {} bytes long", body.length);
                    writeDownloadToFile(downloadLocation, new ByteArrayInputStream(body));
                } else {
                    throw ex;
                }
            }
        } else {
            resource.setTransferred(CacheUtil.getCacheFile(downloadLocation, resource.getDownloadVersion()).length());
        }

        storeEntryFields(downloadEntry, connection.getContentLength(), connection.getLastModified());
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

    private void markForDelete(URL location) {
        CacheEntry entry = new CacheEntry(location, resource.getDownloadVersion());
        entry.lock();
        try {
            entry.markForDelete();
            entry.store();
        } finally {
            entry.unlock();
        }
    }

    private void writeDownloadToFile(URL downloadLocation, InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int rlen;
        try (final OutputStream out = CacheUtil.getOutputStream(downloadLocation, resource.getDownloadVersion())) {
            while (-1 != (rlen = in.read(buf))) {
                resource.incrementTransferred(rlen);
                out.write(buf, 0, rlen);
            }

            in.close();
        }
    }

    private void extractGzip(URL compressedLocation, URL uncompressedLocation, VersionString version) throws IOException {
        LOG.debug("Extracting gzip: {} to {}", compressedLocation, uncompressedLocation);

        final File compressedFile = CacheUtil.getCacheFile(compressedLocation, version);
        final File uncompressedFile = CacheUtil.getCacheFile(uncompressedLocation, version);

        final byte[] content;
        try (final GZIPInputStream gzInputStream = new GZIPInputStream(new FileInputStream(compressedFile))) {
            content = IOUtils.readContent(gzInputStream);
        }

        try (final OutputStream out = new FileOutputStream(uncompressedFile)) {
            IOUtils.writeContent(out, content);
        }
    }

    private void extractPackGz(URL compressedLocation, URL uncompressedLocation, VersionString version) throws IOException {
        LOG.debug("Extracting packgz: {} to {}", compressedLocation, uncompressedLocation);

        try (final GZIPInputStream gzInputStream = new GZIPInputStream(new FileInputStream(CacheUtil
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
     * last modified may be not, but apparently
     * (http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=2591) the url
     * connection is not always cached as expected, and so another request may
     * be sent when length and lastmodified are checked
     *
     */
    static class UrlRequestResult {

        private final int responseCode;
        private final URL redirectUrl;

        private final long lastModified;
        private final long length;

        UrlRequestResult(int responseCode, URL redirectUrl, long lastModified, long length) {
            this.responseCode = responseCode;
            this.redirectUrl = redirectUrl;
            this.lastModified = lastModified;
            this.length = length;
        }

        UrlRequestResult withRedirectUrl(URL url) {
            return new UrlRequestResult(responseCode, url, lastModified, length);
        }

        URL getRedirectURL() {
            return redirectUrl;
        }

        int getResponseCode() {
            return responseCode;
        }

        /**
         * @return whether the result code is redirect one. Right now 301-303 and 307-308
         */
        boolean shouldRedirect() {
            return (responseCode == 301
                    || responseCode == 302
                    || responseCode == 303 /*?*/
                    || responseCode == 307
                    || responseCode == 308);
        }

        /**
         * @return whether the return code is not a OK one - anything except <200 or >=300
         */
        public boolean isInvalid() {
            return (responseCode < 200 || responseCode >= 300);
        }

        @Override
        public String toString() {
            return ""
                    + "url: " + (redirectUrl == null ? "null" : redirectUrl.toExternalForm()) + "; "
                    + "result:" + responseCode + "; "
                    + "lastModified: " + lastModified + "; "
                    + "length: " + length + "; ";
        }
    }

    private static class RedirectionException extends RuntimeException {
        RedirectionException(String string) {
            super(string);
        }
    }

}
