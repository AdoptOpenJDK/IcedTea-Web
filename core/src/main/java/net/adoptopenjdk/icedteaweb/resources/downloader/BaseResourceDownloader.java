package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.CachedDaemonThreadPoolProvider;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.DownloadInfo;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.ACCEPT_ENCODING_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CONTENT_ENCODING_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CONTENT_TYPE_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.ERROR_MIME_TYPE;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.INVALID_HTTP_RESPONSE;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.LAST_MODIFIED_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.PACK_200_OR_GZIP;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_ID_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.ERROR;

/**
 * Base class for resource downloader.
 */
abstract class BaseResourceDownloader implements ResourceDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(BaseResourceDownloader.class);

    protected final Resource resource;
    private final List<URL> downloadUrls;
    private final List<Exception> downLoadExceptions = new ArrayList<>();

    BaseResourceDownloader(final Resource resource, final List<URL> downloadUrls) {
        this.resource = resource;
        this.downloadUrls = downloadUrls;
    }

    @Override
    public Resource download() {
        downLoadExceptions.clear();
        // TODO:
        // would be nice if we could run the different urls in parallel.
        // this would require to write content to temporary file and only on success move the file into the cache.
        downloadUrls.stream()
                .map(this::downloadFrom)
                .map(this::futureToOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(() -> {
                    LOG.error("could not download resource {} from any of theses urls {}", resource, downloadUrls);
                    resource.setStatus(ERROR);
                    return resource;
                });

        // if all urls failed then check for proxy error
        // TODO : should we flag any other exception?
        if (resource.isSet(ERROR)) {
            for(Exception excp : downLoadExceptions) {
                if (excp.getCause() instanceof IOException && excp.getMessage().toLowerCase().contains("proxy")) {
                    BasicExceptionDialog.show(excp);
                    JNLPRuntime.exit(-1);
                }
            }
        }
        return resource;
    }

    private CompletableFuture<Resource> downloadFrom(final URL url) {
        LOG.debug("Will download in background: {}", url);
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
        try (final CloseableConnection connection = getDownloadConnection(downloadFrom)) {
            final DownloadDetails downloadDetails = getDownloadDetails(connection);

            if (downloadDetails.contentType != null && downloadDetails.contentType.startsWith(ERROR_MIME_TYPE)) {
                final String serverResponse = StreamUtils.readStreamAsString(downloadDetails.inputStream);
                throw new RuntimeException("Server error: " + serverResponse);
            }

            downloadDetails.addListener((current, total) -> {
                resource.setSize(total);
                resource.setTransferred(current);
            });
            final long bytesTransferred = tryDownloading(downloadDetails);

            resource.setStatus(DOWNLOADED);
            resource.setTransferred(bytesTransferred);
            return resource;
        } catch (Exception ex) {
            LOG.debug("Exception while downloading resource {} from {} - {}", resource, downloadFrom, ex.getMessage());
            throw ex;
        }
    }

    private CloseableConnection getDownloadConnection(final URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ACCEPT_ENCODING_HEADER, PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties);
    }

    private long tryDownloading(final DownloadDetails downloadDetails) throws IOException {
        final URL resourceHref = resource.getLocation();
        final VersionId version = getVersion(downloadDetails.downloadFrom, downloadDetails.version);

        if (isUpToDate(resourceHref, version, downloadDetails.lastModified)) {
            final File cacheFile = Cache.getCacheFile(resourceHref, version);
            resource.setLocalFile(cacheFile);
            return cacheFile.length();
        } else {
            final CountingInputStream countingInputStream = new CountingInputStream(downloadDetails.inputStream);

            final StreamUnpacker compressionUpacker = StreamUnpacker.getCompressionUnpacker(downloadDetails);
            final InputStream unpackedStream = compressionUpacker.unpack(countingInputStream);

            final StreamUnpacker contentUnpacker = StreamUnpacker.getContentUnpacker(downloadDetails, resourceHref);
            final InputStream unpackedContent = contentUnpacker.unpack(unpackedStream);

            final DownloadInfo downloadInfo = new DownloadInfo(resourceHref, version, downloadDetails.lastModified);
            final File cacheFile = Cache.addToCache(downloadInfo, unpackedContent);
            resource.setLocalFile(cacheFile);
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
            final String contentType = connection.getHeaderField(CONTENT_TYPE_HEADER);
            final String contentEncoding = connection.getHeaderField(CONTENT_ENCODING_HEADER);
            final InputStream inputStream = connection.getInputStream();
            final long totalSize = connection.getContentLength();

            if (!String.valueOf(connection.getResponseCode()).startsWith("2")) {
                throw new IllegalStateException("Request returned " + connection.getResponseCode() + " for URL " + connection.getURL());
            }

            return new DownloadDetails(downloadFrom, inputStream, contentType, contentEncoding, version, lastModified, totalSize);
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
                .filter(a -> a.length == 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        final long lastModified = parseLong(headerMap.get(LAST_MODIFIED_HEADER), System.currentTimeMillis());
        final String version = headerMap.get(VERSION_ID_HEADER);
        final String contentType = headerMap.get(CONTENT_TYPE_HEADER);
        final String contentEncoding = headerMap.get(CONTENT_ENCODING_HEADER);
        final InputStream inputStream = new ByteArrayInputStream(body);

        return new DownloadDetails(url, inputStream, contentType, contentEncoding, version, lastModified, body.length);
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
            downLoadExceptions.add(e);
            return Optional.empty();
        }
    }

    void invalidateExistingEntryInCache(VersionId version) {
        final URL location = resource.getLocation();
        LOG.debug("Invalidating resource in cache: {} / {}", location, version);
        Cache.replaceExistingCacheFile(location, version);
    }
}
