package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.DownloadInfo;
import net.sourceforge.jnlp.cache.CachedDaemonThreadPoolProvider;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;

/**
 * Base class for resource downloader.
 */
abstract class BaseResourceDownloader implements ResourceDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(BaseResourceDownloader.class);

    private static final String VERSION_ID_HEADER = "x-java-jnlp-version-id";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String PACK_200_OR_GZIP = "pack200-gzip, gzip";
    private static final String INVALID_HTTP_RESPONSE = "Invalid Http response";

    protected final Resource resource;
    private final List<URL> downloadUrls;

    BaseResourceDownloader(final Resource resource, final List<URL> downloadUrls) {
        this.resource = resource;
        this.downloadUrls = downloadUrls;
    }

    @Override
    public Resource download() {
        // TODO:
        // would be nice if we could run the different urls in parallel.
        // this would require to write content to temporary file and only on success move the file into the cache.
        return downloadUrls.stream()
                .map(this::downloadFrom)
                .map(this::futureToOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(() -> {
                    LOG.warn("could not download resource {} from any of theses urls {}", resource, downloadUrls);
                    synchronized (resource) {
                        resource.changeStatus(null, EnumSet.of(ERROR));
                        resource.notifyAll();
                    }
                    return resource;
                });
    }

    private CompletableFuture<Resource> downloadFrom(final URL url) {
        final CompletableFuture<Resource> result = new CompletableFuture<>();
        CachedDaemonThreadPoolProvider.getThreadPool().execute(() -> {
            try {
                result.complete(tryDownloading(url));
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private Resource tryDownloading(final URL downloadFrom) throws IOException {
        LOG.debug("Downloading {} from {}", resource, downloadFrom);

        try (final CloseableConnection connection = getDownloadConnection(downloadFrom)) {
            final DownloadDetails downloadDetails = getDownloadDetails(connection);

            final long bytesTransferred = tryDownloading(downloadDetails);

            synchronized (resource) {
                resource.changeStatus(EnumSet.of(DOWNLOADING), EnumSet.of(DOWNLOADED));
                resource.setTransferred(bytesTransferred);
                resource.notifyAll();
            }
            return resource;
        } catch (Exception ex) {
            LOG.warn("Exception while downloading resource {} from {} - {}", resource, downloadFrom, ex.getMessage());
            throw ex;
        }
    }

    private CloseableConnection getDownloadConnection(final URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ACCEPT_ENCODING, PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties);
    }

    private long tryDownloading(final DownloadDetails downloadDetails) throws IOException {
        final URL resourceHref = resource.getLocation();
        final VersionId version = getVersion(downloadDetails.downloadFrom, downloadDetails.version);

        if (isUpToDate(resourceHref, version, downloadDetails.lastModified)) {
            final File cacheFile = Cache.getCacheFile(resourceHref, version);
            return cacheFile.length();
        } else {
            final StreamUnpacker unpacker = StreamUnpacker.toUnpack(downloadDetails);
            final CountingInputStream countingInputStream = new CountingInputStream(downloadDetails.inputStream);
            final InputStream unpackedStream = unpacker.unpack(countingInputStream);
            final DownloadInfo downloadInfo = new DownloadInfo(resourceHref, version, downloadDetails.lastModified);

            Cache.addToCache(downloadInfo, unpackedStream);

            return countingInputStream.numBytesRead();
        }
    }

    protected abstract VersionId getVersion(URL downloadFrom, final String versionHeaderValue);

    protected abstract boolean isUpToDate(final URL resourceHref, final VersionId version, final long lastModified);

    private DownloadDetails getDownloadDetails(final CloseableConnection connection) throws IOException {
        final URL downloadFrom = connection.getURL();
        try {
            // TODO handle redirect and 511 and not successful...

            final long lastModified = connection.getLastModified();
            final String version = connection.getHeaderField(VERSION_ID_HEADER);
            final String contentEncoding = connection.getContentEncoding();
            final InputStream inputStream = connection.getInputStream();
            return new DownloadDetails(downloadFrom, inputStream, contentEncoding, version, lastModified);
        } catch (IOException ex) {
            if (INVALID_HTTP_RESPONSE.equals(ex.getMessage())) {
                LOG.warn(INVALID_HTTP_RESPONSE + " message detected. Attempting direct socket");
                return getInputStreamFromDirectSocket(downloadFrom);
            } else {
                throw ex;
            }
        }
    }

    private DownloadDetails getInputStreamFromDirectSocket(final URL url) throws IOException {
        final Object[] result = UrlUtils.loadUrlWithInvalidHeaderBytes(url);
        final String head = (String) result[0];
        final byte[] body = (byte[]) result[1];
        LOG.debug("Header of: {} ({})", url, resource);
        LOG.debug(head);
        LOG.debug("Body is: {} bytes long", body.length);

        final Map<String, String> headerMap = Stream.of(head.split("\\n"))
                .map(s -> s.split(":"))
                .filter(a -> a.length != 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        final long lastModified = parseLong(headerMap.get("Last-Modified"), System.currentTimeMillis());
        final String version = headerMap.get(VERSION_ID_HEADER);
        final String contentEncoding = headerMap.get("Content-Encoding");
        final InputStream inputStream = new ByteArrayInputStream(body);

        return new DownloadDetails(url, inputStream, contentEncoding, version, lastModified);
    }

    private long parseLong(final String s, final long defaultValue) {
        try {
            return Long.parseLong(s);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private <R> Optional<R> futureToOptional(final Future<R> future) {
        try {
            return Optional.ofNullable(future.get());
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }

}
