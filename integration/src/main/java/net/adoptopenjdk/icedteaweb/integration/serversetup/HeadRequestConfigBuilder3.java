package net.adoptopenjdk.icedteaweb.integration.serversetup;

import com.github.tomakehurst.wiremock.http.HttpHeader;

/**
 * ...
 */
public interface HeadRequestConfigBuilder3 {
    HeadRequestConfigBuilder3 additionalHeader(HttpHeader header);

    GetRequestConfigBuilder1 withGetRequest();
}
