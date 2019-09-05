package net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases.applications.MissingCodebases;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Rule;
import org.junit.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class MissingCodebasesH3Test implements IntegrationTest {
    private static final String JAR_NAME = "App-missingCodebases.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());


    @Test(timeout = 100_000)
    public void codebaseMissingHrefNone() throws IOException, ConfigurationException {
        // given
        final String jnlpUrl = setupServer(wireMock, "MissingCodebasesH3.jnlp", MissingCodebases.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);
        Map<String, String> deplyment = new HashMap<>();
        deplyment.put(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK,"NONE" );
        deplyment.put(ConfigurationConstants.KEY_SECURITY_LEVEL,"ALLOW_UNSIGNED" );
        tmpItwHome.createDeploymentProps(deplyment);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        Boot.main(args);

        // then
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));
        //assertThat(getCachedFileAsString(tmpItwHome, MissingCodebases.ID), containsString("init MissingCodebases"));
    }

}
