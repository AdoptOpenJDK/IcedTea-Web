package net.adoptopenjdk.icedteaweb.integration.testcase1;

import net.sourceforge.jnlp.runtime.Boot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * TODO: Scenario Description
 *
 */
public class Testcase1Test {
    @Before
    public void BeforeEach() {
        // modify .appletTrustSettings to prevent warning dialog??
        // redirect config root via system property
    }

    @After
    public void AfterEach() {
        // restore .appletTrustSettings to prevent warning dialog??
        // delete config root via system property
    }

    @Test(timeout=5000)
    public void testSuccessfullyLaunchSimpleJavaApplication() {
        final URL jnlpFile = getClass().getResource("SimpleJavaApplication.jnlp");

        // final String[] args = {"-jnlp", jnlpFile.getPath(), "-verbose", "-nosecurity", "-Xnofork", "-headless"};
        final String[] args = {"-jnlp", jnlpFile.getPath(), "-verbose", "-nosecurity", "-Xnofork"};

        new Boot().main(args);
    }
}
