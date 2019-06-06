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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * TODO: Scenario Description
 */
public class SplashScreenIconTest implements IntegrationTest {
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";
    private static final String SPLASH_ICON = "javaws.png";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    @Test(timeout = 5_000)
    public void testSplashIcon() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "SimpleJavaApplicationWithSplash.jnlp", SimpleJavaApplication.class, JAR_NAME, SPLASH_ICON);
        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork"};
        Boot.main(args);

        // then
        assertThat(hasCachedFile(tmpItwHome, SPLASH_ICON), is(true));
    }
}
