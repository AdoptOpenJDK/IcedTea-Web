package net.adoptopenjdk.icedteaweb.integration.serversetup;

import com.github.tomakehurst.wiremock.http.HttpHeader;

import java.io.IOException;

/**
 * ...
 */
public interface GetRequestConfigBuilder2 {
    GetRequestConfigBuilder2 additionalHeader(HttpHeader header);

    HeadRequestConfigBuilder1 servingExtensionJnlp(String jnlpFileName) throws IOException;
    ResourceServer servingResource(String resourceFileName) throws IOException;

    String getHttpUrl() throws IOException;
}
