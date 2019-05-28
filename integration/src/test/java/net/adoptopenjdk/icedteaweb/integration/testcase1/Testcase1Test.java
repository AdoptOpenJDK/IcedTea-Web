package net.adoptopenjdk.icedteaweb.integration.testcase1;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.adoptopenjdk.icedteaweb.integration.MapBuilder.replace;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.ARGUMENTS_FILE;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.HELLO_FILE;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.SYSTEM_PROPERTIES_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

/**
 * TODO: Scenario Description
 */
public class Testcase1Test implements IntegrationTest {
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private String jnlpUrl;

    private void beforeEach(final String jnlpFilename) throws IOException {
        // setup server
        wireMock.stubFor(head(urlEqualTo("/" + jnlpFilename)).willReturn(
                ok()
        ));
        wireMock.stubFor(get(urlEqualTo("/" + jnlpFilename)).willReturn(
                aResponse().withBody(fileContent(jnlpFilename,
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
        jnlpUrl = "http://localhost:" + wireMock.port() + "/" + jnlpFilename;
        tmpItwHome.createTrustSettings(jnlpUrl);
    }

    @Test(timeout = 5_000)
    public void testSuccessfullyLaunchSimpleJavaApplication() throws IOException {
        beforeEach("SimpleJavaApplication.jnlp");

        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};

        Boot.main(args);

        // control cache content
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));

        // control application output
        assertThat(getCachedFileAsString(tmpItwHome, HELLO_FILE), startsWith("Hello"));

        // test program arguments
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument1"));
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument2 with spaces"));

        // test system properties
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key1"), containsString("value1"));
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key2"), containsString("value2"));
    }

    @Ignore
    @Test(timeout = 5_000)
    public void testLaunchWithProgramArguments() throws IOException {
        beforeEach("SimpleJavaApplicationWithArguments.jnlp");

        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};

        Boot.main(args);

        // test program arguments
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument1"));
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument2 with spaces"));
    }

    @Ignore
    @Test(timeout = 5_000)
    public void testLaunchWithSystemProperty() throws IOException {
        beforeEach("SimpleJavaApplicationWithProperties.jnlp");

        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};

        Boot.main(args);

        // test system properties
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key1"), containsString("value1"));
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key2"), containsString("value2"));
     }
}
