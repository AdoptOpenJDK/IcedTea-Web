package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.cache.cache.Cache;
import net.sourceforge.jnlp.cache.cache.ResourceInfo;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTING;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static net.sourceforge.jnlp.cache.Resource.Status.PRECONNECT;
import static net.sourceforge.jnlp.cache.Resource.Status.PREDOWNLOAD;
import static net.sourceforge.jnlp.cache.ResourceUrlCreator.VERSION_ID_HEADER;
import static net.sourceforge.jnlp.cache.cache.Cache.isUpToDate;
import static net.sourceforge.jnlp.cache.cache.ResourceInfo.createInfoFromRemote;

class ResourceDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDownloader.class);

    private static final String INVALID_HTTP_RESPONSE = "Invalid Http response";

    private final Resource resource;
    private final Object lock;

    ResourceDownloader(Resource resource, Object lock) {
        this.resource = resource;
        this.lock = lock;
    }

    /**
     * Sets the resource status to connect and download, and
     * enqueues the resource if not already started.
     *
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     */
    static void startDownload(Resource resource, final Object lock) {
        final boolean isProcessing;

        synchronized (resource) {
            if (resource.isComplete()) {
                return;
            }

            isProcessing = resource.isBeingProcessed();

            if (!resource.isSet(CONNECTED) && !resource.isSet(CONNECTING)) {
                resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(PRECONNECT));
                resource.startProcessing();
            }
            if (!resource.isSet(DOWNLOADED) && !resource.isSet(DOWNLOADING)) {
                resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(PREDOWNLOAD));
                resource.startProcessing();
            }

            if (!resource.isSet(PREDOWNLOAD) && !resource.isSet(PRECONNECT)) {
                return;
            }
        }

        if (!isProcessing) {
            final ResourceDownloader rd = new ResourceDownloader(resource, lock);
            CachedDaemonThreadPoolProvider.getThreadPool().execute(() -> {
                rd.runInitialize();
                CachedDaemonThreadPoolProvider.getThreadPool().execute(() -> rd.runDownload());
            });
        }
    }

    public void runInitialize() {
        try {
            if (resource.isSet(PRECONNECT) && !resource.hasAllFlags(EnumSet.of(ERROR, CONNECTING, CONNECTED))) {
                resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(CONNECTING));
                initializeResource();
            }
        } finally {
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
        }
    }

    public void runDownload() {
        try {
            if (resource.isSet(PREDOWNLOAD) && !resource.hasAllFlags(EnumSet.of(ERROR, DOWNLOADING, DOWNLOADED))) {
                resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(DOWNLOADING));
                downloadResource();
            }
        } finally {
            synchronized (lock) {
                lock.notifyAll(); // wake up wait's to check for completion
            }
        }
    }

    private void initializeResource() {
        try {
            if (!JNLPRuntime.isOfflineForced() && resource.isConnectable()) {
                final UrlRequestResult location = ResourceUrlCreator.findBestUrl(resource);
                if (location != null) {
                    initializeFromURL(location);
                    return;
                }
            }
            initializeFromCache();
        } catch (Exception e) {
            LOG.error("Error while initializing resource from location " + resource.getLocation(), e);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
        }
    }

    private void initializeFromURL(final UrlRequestResult location) {
        final boolean isCached = Cache.isCached(resource.getLocation(), location.getVersion());
        final boolean isUpToDate = isCached && Cache.isUpToDate(resource.getLocation(), location.getVersion(), location.getLastModified());
        final boolean doUpdate = !isUpToDate || resource.getUpdatePolicy() == UpdatePolicy.FORCE;

        final File localFile;
        if (doUpdate && isCached) {
            localFile = Cache.replaceExistingCacheFile(resource.getLocation(), location.getVersion());
        } else {
            localFile = Cache.getCacheFile(resource.getLocation(), location.getVersion());
        }

        synchronized (resource) {
            resource.setDownloadLocation(location.getLocation());
            resource.setLocalFile(localFile);
            // resource.connection = connection;
            resource.setSize(location.getContentLength());
            resource.changeStatus(EnumSet.of(PRECONNECT, CONNECTING), EnumSet.of(CONNECTED, PREDOWNLOAD));

            // check if up-to-date; if so set as downloaded
            if (!doUpdate) {
                resource.changeStatus(EnumSet.of(PREDOWNLOAD, DOWNLOADING), EnumSet.of(DOWNLOADED));
                resource.setDownloadVersion(location.getVersion());
            }
        }
    }

    private void initializeFromCache() {
        if (Cache.isAnyCached(resource.getLocation(), resource.getRequestVersion())) {
            final VersionId versionId = Cache.getBestMatchingVersionInCache(resource.getLocation(), resource.getRequestVersion());
            final File localFile = Cache.getCacheFile(resource.getLocation(), versionId);

            if (localFile != null && localFile.exists()) {
                long size = localFile.length();

                synchronized (resource) {
                    resource.setLocalFile(localFile);
                    resource.setSize(size);
                    resource.setDownloadVersion(versionId);
                    resource.changeStatus(EnumSet.of(PREDOWNLOAD, DOWNLOADING), EnumSet.of(DOWNLOADED));
                }
                return;
            }
        }

        LOG.warn("You are trying to get resource {} but it is not in cache and could not be downloaded. Attempting to continue, but you may expect failure", resource.getLocation().toExternalForm());
        resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
    }

    private void downloadResource() {
        final URL downloadFrom = resource.getDownloadLocation(); //Where to download from
        final URL downloadLocation = resource.getLocation(); //Where to download to

        try (final CloseableConnection connection = getDownloadConnection(downloadFrom)) {
            final String contentEncoding = connection.getContentEncoding();
            LOG.debug("Downloading {} from URL {} (encoding : {})", downloadLocation, downloadFrom, contentEncoding);

            final StreamUnpacker unpacker = getStreamUnpacker(downloadFrom, contentEncoding);

            downloadFile(connection, downloadLocation, unpacker);
            resource.changeStatus(EnumSet.of(DOWNLOADING), EnumSet.of(DOWNLOADED));
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            resource.changeStatus(EnumSet.noneOf(Resource.Status.class), EnumSet.of(ERROR));
        }
    }

    private static CloseableConnection getDownloadConnection(URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ResourceUrlCreator.ACCEPT_ENCODING, ResourceUrlCreator.PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties);
    }

    private StreamUnpacker getStreamUnpacker(URL downloadFrom, String contentEncoding) {
        boolean packgz = "pack200-gzip".equals(contentEncoding) || downloadFrom.getPath().endsWith(".pack.gz");
        boolean gzip = "gzip".equals(contentEncoding);

        // It's important to check packgz first. If a stream is both
        // pack200 and gz encoded, then con.getContentEncoding() could
        // return ".gz", so if we check gzip first, we would end up
        // treating a pack200 file as a jar file.
        if (packgz) {
            return new PackGzipUnpacker();
        } else if (gzip) {
            return new GzipUnpacker();
        }

        return new NotUnpacker();
    }

    private void downloadFile(CloseableConnection connection, URL downloadLocation, StreamUnpacker unpacker) throws IOException {
        final VersionId version = resource.getDownloadVersion();
        if (isUpToDate(downloadLocation, version, connection.getLastModified())) {
            final File cacheFile = Cache.getCacheFile(downloadLocation, version);
            resource.setTransferred(cacheFile.length());
        } else {
            final String versionHeaderField = connection.getHeaderField(VERSION_ID_HEADER);
            final VersionId versionFromRemote = versionHeaderField != null ? VersionId.fromString(versionHeaderField) : resource.getDownloadVersion();

            final InputStream downloadStream = getDownloadInputStream(connection, downloadLocation);
            final InputStream unpackedStream = unpacker.unpack(downloadStream);
            final ResourceInfo resourceInfo = createInfoFromRemote(downloadLocation, versionFromRemote, connection);
            Cache.addToCache(resourceInfo, unpackedStream);
            resource.setTransferred(resourceInfo.getSize());
        }
    }

    private InputStream getDownloadInputStream(CloseableConnection connection, URL downloadLocation) throws IOException {
        try {
            return connection.getInputStream();
        } catch (IOException ex) {
            if (INVALID_HTTP_RESPONSE.equals(ex.getMessage())) {
                LOG.error(INVALID_HTTP_RESPONSE + " message detected. Attempting direct socket", ex);
                return getInputStreamFromDirectSocket(connection.getURL(), downloadLocation);
            } else {
                throw ex;
            }
        }
    }

    private InputStream getInputStreamFromDirectSocket(URL url, URL downloadLocation) throws IOException {
        final Object[] result = UrlUtils.loadUrlWithInvalidHeaderBytes(url);
        final String head = (String) result[0];
        final byte[] body = (byte[]) result[1];
        LOG.info("Header of: {} ({})", url, downloadLocation);
        LOG.info(head);
        LOG.info("Body is: {} bytes long", body.length);
        return new ByteArrayInputStream(body);
    }
}
