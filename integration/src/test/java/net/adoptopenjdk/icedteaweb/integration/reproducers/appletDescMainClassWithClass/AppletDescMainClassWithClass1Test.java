package net.adoptopenjdk.icedteaweb.integration.reproducers.appletDescMainClassWithClass;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DefaultDownloadIndicator;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.reproducers.appletDescMainClassWithClass.applications.AppletDescMainClassWithClass;
import net.adoptopenjdk.icedteaweb.integration.reproducers.progressclass.applications.ProgressClassManagedApplication;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.adoptopenjdk.icedteaweb.integration.reproducers.progressclass.applications.ProgressClassManagedApplication.PROGRESS_CLASS_OUTPUT_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


public class AppletDescMainClassWithClass1Test implements IntegrationTest {
    private static final String JAR_NAME = "App-appletDescMainClassWithClass.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());


    @Test(timeout = 100_000)
    public void appletWithNormallMainClass() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "AppletDescMainClassWithClass1.jnlp", AppletDescMainClassWithClass.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork", "-headless"};
        Boot.main(args);

        // then
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));
        //assertThat(getCachedFileAsString(tmpItwHome, AppletDescMainClassWithClass.ID), containsString("init AppletDescMainClassWithClass"));
    }

}
