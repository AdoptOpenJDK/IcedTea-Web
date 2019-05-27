package net.adoptopenjdk.icedteaweb.integration.testcase1;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.integration.MapBuilder.replace;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.HELLO_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Scenario Description
 */
public class Testcase1Test implements IntegrationTest {

    private static final String JNLP_NAME = "SimpleJavaApplication.jnlp";
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private String jnlpUrl;

    @Before
    public void BeforeEach() throws Exception {

        // setup server
        wireMock.stubFor(head(urlEqualTo("/" + JNLP_NAME)).willReturn(
                ok()
        ));
        wireMock.stubFor(get(urlEqualTo("/" + JNLP_NAME)).willReturn(
                aResponse().withBody(fileContent(JNLP_NAME,
                        replace(PORT).with(wireMock.port())
                                .and(MAIN_CLASS).with(SimpleJavaApplication.class))
                )
        ));

        wireMock.stubFor(head(urlEqualTo("/resources/" + JAR_NAME)).willReturn(
                ok()
        ));
        wireMock.stubFor(get(urlEqualTo("/resources/" + JAR_NAME)).willReturn(
                aResponse().withBody(fileContent("resources/" + JAR_NAME))
        ));

        // setup itw config
        jnlpUrl = "http://localhost:" + wireMock.port() + "/" + JNLP_NAME;
        tmpItwHome.createTrustSettings(jnlpUrl);
    }

    @Test(timeout = 5_000)
    public void testSuccessfullyLaunchSimpleJavaApplication() throws IOException {
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};

        Boot.main(args);

        // control cache content
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));

        // control application output
        assertThat(getCachedFileAsString(tmpItwHome, HELLO_FILE), startsWith("Hello"));
    }
}
