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
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.ARGUMENTS_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * TODO: Scenario Description
 */
public class JvmArgumentsPassedTest implements IntegrationTest {
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    @Test(timeout = 5_000)
    public void testLaunchWithProgramArguments() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "SimpleJavaApplicationWithArguments.jnlp", SimpleJavaApplication.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        Boot.main(args);

        // then
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument1"));
        assertThat(getCachedFileAsString(tmpItwHome, ARGUMENTS_FILE), containsString("argument2 with spaces"));
    }
}
