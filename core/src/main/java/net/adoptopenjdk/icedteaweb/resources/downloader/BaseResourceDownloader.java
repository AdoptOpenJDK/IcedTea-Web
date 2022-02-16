package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.DownloadInfo;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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

import static net.adoptopenjdk.icedteaweb.resources.DaemonThreadPoolProvider.globalFixedThreadPool;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.ACCEPT_ENCODING_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CONTENT_ENCODING_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CONTENT_TYPE_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.ERROR_MIME_TYPE;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.INVALID_HTTP_RESPONSE;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.LAST_MODIFIED_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.PACK_200_OR_GZIP;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_ID_HEADER;
import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.ERROR;

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
        return downloadUrls.stream()
                .map(this::downloadFrom)
                .map(this::futureToOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(() -> {
                    LOG.error("could not download resource {} from any of theses urls {} {}", resource, downloadUrls, exceptionMessage());
                    resource.setStatus(ERROR);
                    checkForProxyError();
                    return resource;
                });
    }

    private String exceptionMessage() {
        return downLoadExceptions.stream()
                .findFirst()
                .map(this::exceptionMessage)
                .map(s -> "caused by" + s)
                .orElse("");
    }

    private String exceptionMessage(final Exception e) {
        Throwable throwable = e;
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }

        return list.stream()
                .map(t -> t.getClass().getSimpleName() + ": " + t.getMessage())
                .collect(Collectors.joining(" caused by "));
    }

    private void checkForProxyError() {
        for (final Exception excp : downLoadExceptions) {
            final Throwable cause = excp.getCause();
            if (cause instanceof IOException && cause.getMessage().toLowerCase().contains("proxy")) {
                BasicExceptionDialog.show((IOException) cause);
                JNLPRuntime.exit(-1);
            }
        }
    }

    private CompletableFuture<Resource> downloadFrom(final URL url) {
        LOG.debug("Will download in background: {}", url);
        final CompletableFuture<Resource> result = new CompletableFuture<>();
        globalFixedThreadPool().execute(() -> {
            try {
                result.complete(tryDownloading(url));
            } catch (Exception | Error e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private Resource tryDownloading(final URL downloadFrom) throws IOException {
        DownloadDetails downloadDetails = null;
        try (final CloseableConnection connection = getDownloadConnection(downloadFrom)) {
            downloadDetails = getDownloadDetails(connection);

            if (downloadDetails.contentType != null && downloadDetails.contentType.startsWith(ERROR_MIME_TYPE)) {
                final String serverResponse = StreamUtils.readStreamAsString(downloadDetails.inputStream);
                throw new RuntimeException("Server error: " + serverResponse);
            }

            resource.setSize(downloadDetails.totalSize);
            final long bytesTransferred = tryDownloading(downloadDetails);

            resource.setStatus(DOWNLOADED);
            resource.setTransferred(bytesTransferred);
            return resource;
        } catch (Exception ex) {
            if (downloadDetails != null) {
                LOG.debug("Marking as corrupted {}", resource);
                Cache.invalidateExistingCacheFile(resource.getLocation(), getVersion(downloadDetails.downloadFrom, downloadDetails.version));
            }
            LOG.debug("Exception while downloading resource {} from {} - message: {} cause: {} ", resource, downloadFrom, ex.getMessage(), ex.getCause());
            throw ex;
        }
    }

    private CloseableConnection getDownloadConnection(final URL location) throws IOException {
        final Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(ACCEPT_ENCODING_HEADER, PACK_200_OR_GZIP);
        return ConnectionFactory.openConnection(location, HttpMethod.GET, requestProperties, getTimeoutValue(ConfigurationConstants.KEY_HTTPCONNECTION_CONNECT_TIMEOUT), getTimeoutValue(ConfigurationConstants.KEY_HTTPCONNECTION_READ_TIMEOUT));
    }

    private int getTimeoutValue(final String key) {
        int timeout = 0;
        final String value = JNLPRuntime.getConfiguration().getProperty(key);
        if (value != null && value.trim().length() != 0) {
            try {
                timeout = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse {} with value '{}' - reason {}", key, value, e.getMessage());
            }
        }
        return timeout;
    }

    private long tryDownloading(final DownloadDetails downloadDetails) throws IOException {
        final URL resourceHref = resource.getLocation();
        final VersionId version = getVersion(downloadDetails.downloadFrom, downloadDetails.version);

        if (isUpToDate(resourceHref, version, downloadDetails.lastModified)) {
            final File cacheFile = Cache.getCacheFile(resourceHref, version);
            resource.setLocalFile(cacheFile);
            return cacheFile.length();
        } else {
            final CountingInputStream countingInputStream = downloadDetails.inputStream;

            final StreamUnpacker compressionUnpacker = StreamUnpacker.getCompressionUnpacker(downloadDetails);
            final InputStream unpackedStream = compressionUnpacker.unpack(countingInputStream);

            final StreamUnpacker contentUnpacker = StreamUnpacker.getContentUnpacker(downloadDetails, resourceHref);
            final InputStream unpackedContent = contentUnpacker.unpack(unpackedStream);

            final DownloadInfo downloadInfo = new DownloadInfo(resourceHref, version, downloadDetails.lastModified);
            final File cacheFile = Cache.addToCache(downloadInfo, unpackedContent);

            final long expectedBytes = downloadDetails.totalSize;
            final long actualBytes = countingInputStream.numBytesRead();

            if (expectedBytes > 0 && expectedBytes > actualBytes) {
                cacheFile.delete();
                LOG.debug("Deleted Corrupt File {}", cacheFile.getName());
                throw new IOException(String.format("Did read %d bytes from server but expected %d", actualBytes, expectedBytes));
            }

            resource.setLocalFile(cacheFile);
            return actualBytes;
        }
    }

    protected abstract VersionId getVersion(URL downloadFrom, final String versionHeaderValue);

    protected abstract boolean isUpToDate(final URL resourceHref, final VersionId version, final long lastModified);

    private DownloadDetails getDownloadDetails(final CloseableConnection connection) throws IOException {
        final URL downloadFrom = connection.getURL();
        try {
            final long lastModified = connection.getLastModified();
            final String version = connection.getHeaderField(VERSION_ID_HEADER);
            final String contentType = connection.getHeaderField(CONTENT_TYPE_HEADER);
            final String contentEncoding = connection.getHeaderField(CONTENT_ENCODING_HEADER);
            final long totalSize = connection.getContentLength();
            final NotifyingInputStream inputStream = new NotifyingInputStream(connection.getInputStream(), totalSize, resource::setTransferred);

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
        final Object[] result = loadUrlWithInvalidHeaderBytes(url);
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
        final CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(body));

        return new DownloadDetails(url, inputStream, contentType, contentEncoding, version, lastModified, body.length);
    }

    private Object[] loadUrlWithInvalidHeaderBytes(final URL url) throws IOException {
        try (final Socket s = UrlUtils.createSocketFromUrl(url)) {
            UrlUtils.writeRequest(s.getOutputStream(), url);
            String head = "";
            byte[] body = new byte[0];
            //we can't use buffered reader, otherwise buffer consume also part of body
            try (InputStream is = s.getInputStream()) {
                while (true) {
                    int readChar = is.read();
                    if (readChar < 0) {
                        break;
                    }
                    head = head + ((char) readChar);
                    if (endsWithBlankLine(head)) {
                        body = IOUtils.readContent(new NotifyingInputStream(is, -1, resource::setTransferred));
                    }
                }
            }
            return new Object[]{head, body};
        }
    }

    private static boolean endsWithBlankLine(String head) {
        return head.endsWith("\n\n")
                || head.endsWith("\r\n\r\n")
                || head.endsWith("\n\r\n\r")
                || head.endsWith("\r\r");
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
        Cache.invalidateExistingCacheFile(location, version);
    }
}
