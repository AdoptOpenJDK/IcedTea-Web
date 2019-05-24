package net.adoptopenjdk.icedteaweb.integration.testcase1;

import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * TODO: Scenario Description
 *
 */
public class Testcase1Test {
    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Before
    public void BeforeEach() throws IOException {
        final File tempHome = temporaryFolder.newFolder();
        environmentVariables.set(ConfigurationConstants.XDG_CONFIG_HOME_VAR, tempHome.getAbsolutePath());
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
