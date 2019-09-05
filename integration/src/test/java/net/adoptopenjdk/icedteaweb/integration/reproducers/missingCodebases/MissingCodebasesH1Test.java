package net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases.applications.MissingCodebases;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class MissingCodebasesH1Test implements IntegrationTest {
    private static final String JAR_NAME = "App-missingCodebases.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());


    @Test(timeout = 100_000)
    public void codebaseDotHrefNo() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "MissingCodebasesH1.jnlp", MissingCodebases.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        Boot.main(args);

        // then
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));
        //assertThat(getCachedFileAsString(tmpItwHome, MissingCodebases.ID), containsString("init MissingCodebases"));
    }

}
