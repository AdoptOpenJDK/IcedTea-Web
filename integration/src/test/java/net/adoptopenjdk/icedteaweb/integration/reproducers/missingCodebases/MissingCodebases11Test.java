package net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases.applications.MissingCodebases;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class MissingCodebases11Test implements IntegrationTest {
    private static final String JAR_NAME = "App-missingCodebases.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());


    @Test(timeout = 100_000)
    public void codebaseDotHrefFullApp() throws IOException {
        // given
        final ZonedDateTime someTime = now();
        final String jnlpUrl = setupServer(wireMock)
                .servingJnlp("MissingCodebases11.jnlp").withMainClass(MissingCodebases.class)
                .withHeadRequest().lastModifiedAt(someTime)
                .withGetRequest().lastModifiedAt(someTime)
                .servingResource(JAR_NAME).withoutVersion()
                .withHeadRequest().lastModifiedAt(someTime)
                .withGetRequest().lastModifiedAt(someTime)
                .getHttpUrl();

        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        final int result = Boot.mainWithReturnCode(args);

        // then
        assertThat(result, is(SUCCESS));
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));
        //assertThat(getCachedFileAsString(tmpItwHome, MissingCodebases.ID), containsString("init MissingCodebases"));
    }

}
