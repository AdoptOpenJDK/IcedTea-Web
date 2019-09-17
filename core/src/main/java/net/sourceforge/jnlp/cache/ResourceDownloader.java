package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.cache.ResourceUrlCreator.UrlRequestResult;
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
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

import static net.sourceforge.jnlp.cache.CacheEntry.markForDelete;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTING;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static net.sourceforge.jnlp.cache.Resource.Status.PRECONNECT;
import static net.sourceforge.jnlp.cache.Resource.Status.PREDOWNLOAD;

class ResourceDownloader implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDownloader.class);

    private final Resource resource;
    private final Object lock;

    ResourceDownloader(Resource resource, Object lock) {
        this.resource = resource;
        this.lock = lock;
    }

    @Override
    public void run() {
        if (resource.isSet(PRECONNECT) && !resource.hasAllFlags(EnumSet.of(ERROR, CONNECTING, CONNECTED))) {
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(CONNECTING));
            initializeResource();
        }
        if (resource.isSet(PREDOWNLOAD) && !resource.hasAllFlags(EnumSet.of(ERROR, DOWNLOADING, DOWNLOADED))) {
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(DOWNLOADING));
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
            final UrlRequestResult finalLocation = ResourceUrlCreator.findBestUrl(resource);
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
        }
    }

    private void initializeFromURL(final UrlRequestResult location) throws IOException {
        CacheEntry entry = new CacheEntry(resource.getLocation(), resource.getRequestVersion());
        entry.lock();
        try (final CloseableConnection connection = getDownloadConnection(location.getRedirectURL())) {// this won't change so should be okay not-synchronized
            resource.setDownloadLocation(location.getRedirectURL());

            File localFile = CacheUtil.getCacheFile(resource.getLocation(), resource.getDownloadVersion());
            long size = location.getLength();
            long lm = location.getLastModified();
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
        } finally {
            entry.unlock();
        }

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
                if (downloadFrom.getFile().endsWith(".pack.gz")) {
                    downloadPackGzFile(connection, downloadFrom, downloadTo);
                } else {
                    downloadPackGzFile(connection, new URL(downloadFrom + ".pack.gz"), downloadTo);
                }
            } else if (gzip) {
                if (downloadFrom.getFile().endsWith(".gz")) {
                    downloadGZipFile(connection, downloadFrom, downloadTo);
                } else {
                    downloadGZipFile(connection, new URL(downloadFrom + ".gz"), downloadTo);
                }
            } else {
                downloadFile(connection, downloadTo);
            }

            resource.changeStatus(EnumSet.of(DOWNLOADING), EnumSet.of(DOWNLOADED));
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private CloseableConnection getDownloadConnection(URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ResourceUrlCreator.ACCEPT_ENCODING, ResourceUrlCreator.PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties);
    }

    private void downloadPackGzFile(CloseableConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(connection, downloadFrom);

        extractPackGz(downloadFrom, downloadTo, resource.getDownloadVersion());
        CacheEntry entry = new CacheEntry(downloadTo, resource.getDownloadVersion());
        entry.storeEntryFields(entry.getCacheFile().length(), connection.getLastModified());
        markForDelete(downloadFrom, resource.getDownloadVersion());
    }

    private void downloadGZipFile(CloseableConnection connection, URL downloadFrom, URL downloadTo) throws IOException {
        downloadFile(connection, downloadFrom);

        extractGzip(downloadFrom, downloadTo, resource.getDownloadVersion());
        CacheEntry entry = new CacheEntry(downloadTo, resource.getDownloadVersion());
        entry.storeEntryFields(entry.getCacheFile().length(), connection.getLastModified());
        markForDelete(downloadFrom, resource.getDownloadVersion());
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

        downloadEntry.storeEntryFields(connection.getContentLength(), connection.getLastModified());
    }

    private void writeDownloadToFile(URL downloadLocation, InputStream in) throws IOException {
        try (final OutputStream out = CacheUtil.getOutputStream(downloadLocation, resource.getDownloadVersion())) {
            IOUtils.copy(in, out);
        }
    }

    private void extractGzip(final URL compressedLocation, final URL uncompressedLocation, final VersionString version) throws IOException {
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

    private void extractPackGz(final URL compressedLocation, final URL uncompressedLocation, final VersionString version) throws IOException {
        LOG.debug("Extracting packgz: {} to {}", compressedLocation, uncompressedLocation);

        try (final GZIPInputStream gzInputStream = new GZIPInputStream(new FileInputStream(CacheUtil
                .getCacheFile(compressedLocation, version)))) {
            final InputStream inputStream = new BufferedInputStream(gzInputStream);

            final JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(CacheUtil
                    .getCacheFile(uncompressedLocation, version)));

            final Pack200.Unpacker unpacker = Pack200.newUnpacker();
            unpacker.unpack(inputStream, outputStream);

            outputStream.close();
            inputStream.close();
        }
    }

}
