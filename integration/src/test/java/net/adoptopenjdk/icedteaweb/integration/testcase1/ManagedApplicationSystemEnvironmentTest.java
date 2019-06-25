package net.adoptopenjdk.icedteaweb.integration.testcase1;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.SYSTEM_ENVIRONMENT_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Test the successful access to a system environment variable by an ITW-managed application.
 */
public class ManagedApplicationSystemEnvironmentTest implements IntegrationTest {
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    @Test(timeout = 100_000)
    public void testReadingSystemEnvironment() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "SimpleJavaApplication.jnlp", SimpleJavaApplication.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        Boot.main(args);

        // then
        final String cachedFileAsString = getCachedFileAsString(tmpItwHome, SYSTEM_ENVIRONMENT_FILE);
        assertThat(cachedFileAsString, containsString("USER"));
    }
}
