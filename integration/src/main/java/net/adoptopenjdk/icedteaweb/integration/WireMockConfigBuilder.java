package net.adoptopenjdk.icedteaweb.integration;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.integration.serversetup.ResourceServer;
import net.adoptopenjdk.icedteaweb.integration.serversetup.GetRequestConfigBuilder1;
import net.adoptopenjdk.icedteaweb.integration.serversetup.GetRequestConfigBuilder2;
import net.adoptopenjdk.icedteaweb.integration.serversetup.HeadRequestConfigBuilder1;
import net.adoptopenjdk.icedteaweb.integration.serversetup.HeadRequestConfigBuilder2;
import net.adoptopenjdk.icedteaweb.integration.serversetup.HeadRequestConfigBuilder3;
import net.adoptopenjdk.icedteaweb.integration.serversetup.JnlpServer1;
import net.adoptopenjdk.icedteaweb.integration.serversetup.JnlpServer2;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.http.HttpUtils.lastModifiedDate;
import static net.adoptopenjdk.icedteaweb.integration.MapBuilder.replace;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CURRENT_VERSION_ID_QUERY_PARAM;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_ID_HEADER;

/**
 * Builder for easy configuration of WireMock.
 */
public class WireMockConfigBuilder implements
        JnlpServer1,
        JnlpServer2,
        HeadRequestConfigBuilder1,
        HeadRequestConfigBuilder2,
        HeadRequestConfigBuilder3,
        GetRequestConfigBuilder1,
        GetRequestConfigBuilder2,
        ResourceServer
{

    private static final String PORT = "PORT";
    private static final String MAIN_CLASS = "MAIN_CLASS";

    private final WireMockRule wireMock;
    private final Class<?> testClass;

    private ConfigBuilder activeBuilder;
    private JnlpConfigBuilder jnlpBuilder;
    private ResourceConfigBuilder resourceConfigBuilder;

    WireMockConfigBuilder(WireMockRule wireMock, Class<?> testClass) {
        this.wireMock = wireMock;
        this.testClass = testClass;
    }

    public WireMockConfigBuilder servingJnlp(String jnlpFileName) throws IOException {
        jnlpBuilder = new JnlpConfigBuilder(jnlpFileName);
        startBuilder(jnlpBuilder);
        return this;
    }

    @Override
    public WireMockConfigBuilder withMainClass(Class<?> mainClass) {
        jnlpBuilder.withMainClass(mainClass);
        return this;
    }

    @Override
    public WireMockConfigBuilder withHeadRequest() {
        activeBuilder.startHeadRequest();
        return this;
    }

    @Override
    public WireMockConfigBuilder withGetRequest() {
        activeBuilder.startGetRequest();
        return this;
    }

    @Override
    public WireMockConfigBuilder returnsNotFound() {
        activeBuilder.activeRequest().response(WireMock.notFound());
        return this;
    }

    @Override
    public WireMockConfigBuilder returnsServerError() {
        activeBuilder.activeRequest().response(WireMock.serverError());
        return this;
    }

    @Override
    public WireMockConfigBuilder lastModifiedAt(ZonedDateTime lastModified) {
        activeBuilder.activeRequest().lastModified(lastModified);
        return this;
    }

    @Override
    public WireMockConfigBuilder withoutLastModificationDate() {
        // nothing to do
        return this;
    }

    @Override
    public WireMockConfigBuilder additionalHeader(HttpHeader header) {
        activeBuilder.activeRequest().addHeader(header);
        return this;
    }


    @Override
    public WireMockConfigBuilder servingExtensionJnlp(String jnlpFileName) throws IOException {
        startBuilder(new JnlpExtensionConfigBuilder(jnlpFileName));
        return this;
    }

    @Override
    public WireMockConfigBuilder servingResource(String resourceFileName) throws IOException {
        resourceConfigBuilder = new ResourceConfigBuilder(resourceFileName);
        startBuilder(resourceConfigBuilder);
        return this;
    }

    @Override
    public WireMockConfigBuilder withoutVersion() {
        // nothing to do
        return this;
    }

    @Override
    public WireMockConfigBuilder withVersion(VersionId versionId) {
        return withVersion(versionId.asVersionString(), versionId);
    }

    @Override
    public WireMockConfigBuilder withVersion(VersionString requestedVersion, VersionId versionId) {
        resourceConfigBuilder.withVersion(requestedVersion, versionId);
        return this;
    }

    @Override
    public String getHttpUrl() throws IOException {
        buildActiveBuilder();
        return "http://localhost:" + wireMock.port() + "/" + jnlpBuilder.jnlpFileName;
    }

    private void startBuilder(ConfigBuilder nextBuilder) throws IOException {
        buildActiveBuilder();
        this.activeBuilder = nextBuilder;
    }

    private void buildActiveBuilder() throws IOException {
        if (activeBuilder != null) {
            activeBuilder.build(wireMock, testClass);
        }
        activeBuilder = null;
    }

    private abstract static class ConfigBuilder {

        RequestConfigBuilder headRequest;
        RequestConfigBuilder getRequest;

        abstract void build(WireMockRule wireMock, Class<?> testClass) throws IOException;

        void startHeadRequest() {
            headRequest = new RequestConfigBuilder();
        }

        void startGetRequest() {
            getRequest = new RequestConfigBuilder();
        }

        RequestConfigBuilder activeRequest() {
            if (getRequest != null) {
                return getRequest;
            } else if (headRequest != null) {
                return headRequest;
            }
            throw new IllegalStateException("neither head nor get are available");
        }
    }

    private static class JnlpConfigBuilder extends ConfigBuilder {

        private final String jnlpFileName;
        private Class<?> mainClass;

        JnlpConfigBuilder(String jnlpFileName) {
            this.jnlpFileName = jnlpFileName;
        }

        void withMainClass(Class<?> mainClass) {
            this.mainClass = mainClass;
        }

        @Override
        void build(WireMockRule wireMock, Class<?> testClass) throws IOException {
            wireMock.stubFor(head(urlEqualTo("/" + jnlpFileName))
                    .willReturn(headRequest.response != null ? headRequest.response : ok()
                            .withHeaders(new HttpHeaders(headRequest.headers))
                    )
            );
            wireMock.stubFor(get(urlEqualTo("/" + jnlpFileName))
                    .willReturn(getResponse(wireMock.port(), testClass)
                            .withHeaders(new HttpHeaders(headRequest.headers))
                    )
            );
        }

        private ResponseDefinitionBuilder getResponse(int port, Class<?> testClass) throws IOException {
            if (getRequest.response != null) {
                return getRequest.response;
            } else {
                return aResponse().withBody(fileContent("jnlps/" + jnlpFileName, testClass,
                        replace(PORT).with(port)
                                .and(MAIN_CLASS).with(mainClass)));
            }
        }
    }

    private static class JnlpExtensionConfigBuilder extends ConfigBuilder {

        private final String jnlpFileName;

        JnlpExtensionConfigBuilder(String jnlpFileName) {
            this.jnlpFileName = jnlpFileName;
        }

        @Override
        void build(WireMockRule wireMock, Class<?> testClass) throws IOException {
            wireMock.stubFor(head(urlEqualTo("/" + jnlpFileName))
                    .willReturn(headRequest.response != null ? headRequest.response : ok()
                            .withHeaders(new HttpHeaders(headRequest.headers))
                    )
            );
            wireMock.stubFor(get(urlEqualTo("/" + jnlpFileName))
                    .willReturn(getResponse(wireMock.port(), testClass)
                            .withHeaders(new HttpHeaders(headRequest.headers))
                    )
            );
        }

        private ResponseDefinitionBuilder getResponse(int port, Class<?> testClass) throws IOException {
            if (getRequest.response == null) {
                return aResponse().withBody(fileContent("jnlps/" + jnlpFileName, testClass,
                        replace(PORT).with(port)));
            } else {
                return getRequest.response;
            }
        }

    }

    private static class ResourceConfigBuilder extends ConfigBuilder {

        private final String resourceFileName;
        private VersionString requestedVersion;
        private VersionId versionId;

        ResourceConfigBuilder(String resourceFileName) {
            this.resourceFileName = resourceFileName;
        }

        void withVersion(VersionString requestedVersion, VersionId versionId) {
            this.requestedVersion = Assert.requireNonNull(requestedVersion, "requestedVersion");
            this.versionId = Assert.requireNonNull(versionId, "versionId");
        }

        @Override
        void build(WireMockRule wireMock, Class<?> testClass) throws IOException {
            final ResponseDefinitionBuilder headResponse = headRequest.response != null ? headRequest.response : ok();
            final ResponseDefinitionBuilder getResponse = getResponse(testClass);

            if (versionId != null) {
                final HttpHeader versionHeader = new HttpHeader(VERSION_ID_HEADER, versionId.toString());
                headRequest.addHeader(versionHeader);
                getRequest.addHeader(versionHeader);

                wireMock.stubFor(head(urlEqualTo("/resources/" + resourceFileName))
                        .withQueryParam(CURRENT_VERSION_ID_QUERY_PARAM, equalTo(requestedVersion.toString()))
                        .willReturn(headResponse.withHeaders(new HttpHeaders(headRequest.headers)))
                );
                wireMock.stubFor(get(urlEqualTo("/resources/" + resourceFileName))
                        .withQueryParam(CURRENT_VERSION_ID_QUERY_PARAM, equalTo(requestedVersion.toString()))
                        .willReturn(getResponse
                                .withHeaders(new HttpHeaders(headRequest.headers))
                        )
                );
            }

            wireMock.stubFor(head(urlEqualTo("/resources/" + resourceFileName))
                    .willReturn(headResponse.withHeaders(new HttpHeaders(headRequest.headers)))
            );
            wireMock.stubFor(get(urlEqualTo("/resources/" + resourceFileName))
                    .willReturn(getResponse.withHeaders(new HttpHeaders(headRequest.headers)))
            );
        }

        private ResponseDefinitionBuilder getResponse(Class<?> testClass) throws IOException {
            if (getRequest.response == null) {
                return aResponse().withBody(fileContent("resources/" + resourceFileName, testClass));
            } else {
                return getRequest.response;
            }
        }
    }

    private static class RequestConfigBuilder {

        private ResponseDefinitionBuilder response;
        private final List<HttpHeader> headers = new ArrayList<>();

        void response(ResponseDefinitionBuilder responseDefinitionBuilder) {
            this.response = responseDefinitionBuilder;
        }

        void lastModified(ZonedDateTime lastModified) {
            headers.add(new HttpHeader("last-modified", lastModifiedDate(lastModified)));
        }

        void addHeader(HttpHeader header) {
            headers.add(header);
        }
    }

    private static byte[] fileContent(String file, Class<?> testClass, MapBuilder replacements) throws IOException {
        return fileContent(file, testClass, replacements.build());
    }

    private static byte[] fileContent(String file, Class<?> testClass, Map<String, String> replacements) throws IOException {
        String content;
        try (final InputStream in = testClass.getResourceAsStream(file)) {
            content = IOUtils.readContentAsUtf8String(in);
        }

        for (Map.Entry<String, String> replacePair : replacements.entrySet()) {
            content = content.replaceAll(Pattern.quote("${" + replacePair.getKey() + "}"), replacePair.getValue());
        }

        return content.getBytes(UTF_8);
    }

    private static byte[] fileContent(String file, Class<?> testClass) throws IOException {
        try (final InputStream in = testClass.getResourceAsStream(file)) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            return out.toByteArray();
        }
    }
}
