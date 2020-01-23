package net.adoptopenjdk.icedteaweb.resources;

/**
 * ...
 */
public interface JnlpDownloadProtocolConstants {
    String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    String CONTENT_ENCODING_HEADER = "Content-Encoding";
    String CONTENT_TYPE_HEADER = "Content-Type";
    String LAST_MODIFIED_HEADER = "Last-Modified";

    String VERSION_ID_HEADER = "x-java-jnlp-version-id";

    String ERROR_MIME_TYPE = "application/x-java-jnlp-error";
    String JAR_DIFF_MIME_TYPE = "application/x-java-archive-dif";

    String PACK_200_OR_GZIP = "pack200-gzip, gzip";
    String INVALID_HTTP_RESPONSE = "Invalid Http response";

    String VERSION_ID_QUERY_PARAM = "version-id";
    String CURRENT_VERSION_ID_QUERY_PARAM = "current-version-id";

    String VERSION_PREFIX = "__V";
}
