package net.adoptopenjdk.icedteaweb.integration.testcase1;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingPermissionsAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UrlRegEx;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Date;

import static net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction.ALWAYS;

/**
 * TODO: Scenario Description
 *
 */
public class Testcase1Test {
    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private URL jnlpUrl;
    private File tempHome;

    @Before
    public void BeforeEach() throws Exception {
        jnlpUrl = getClass().getResource("SimpleJavaApplication.jnlp");
        tempHome = temporaryFolder.newFolder("config");
        environmentVariables.set(ConfigurationConstants.XDG_CONFIG_HOME_VAR, tempHome.getAbsolutePath());

        final String source = new File(jnlpUrl.toURI()).getParentFile().toURI().toURL().toExternalForm();
        final AppletSecurityActions securityActions = new AppletSecurityActions();
        securityActions.setAction(MissingPermissionsAttributePanel.class, new SavedRememberAction(ALWAYS, "YES"));
        securityActions.setAction(MissingALACAttributePanel.class, new SavedRememberAction(ALWAYS, "YES"));

        final UnsignedAppletActionEntry unsignedAppletActionEntry = new UnsignedAppletActionEntry(
                securityActions,
                new Date(1558708474622L),
                UrlRegEx.quoteAndStar(source),
                UrlRegEx.quote(source),
                null);

        final File configHome = new File(tempHome, "icedtea-web");
        final File trustSettings = new File(configHome, ".appletTrustSettings");
        configHome.mkdirs();
        trustSettings.createNewFile();

        try (final FileWriter writer = new FileWriter(trustSettings)) {
            writer.write(UnsignedAppletActionStorageImpl.versionPrefix + " " + UnsignedAppletActionStorageImpl.currentVersion + " - generated\n");
            unsignedAppletActionEntry.write(writer);
            writer.flush();
        }

        // modify .appletTrustSettings to prevent warning dialog??
        // redirect config root via system property
    }

    @After
    public void AfterEach() {
        // restore .appletTrustSettings to prevent warning dialog??
        // delete config root via system property
    }

    @Test(timeout=5_000)
    public void testSuccessfullyLaunchSimpleJavaApplication() {
        final URL jnlpFile = getClass().getResource("SimpleJavaApplication.jnlp");

        // final String[] args = {"-jnlp", jnlpFile.getPath(), "-verbose", "-nosecurity", "-Xnofork", "-headless"};
        final String[] args = {"-jnlp", jnlpFile.getPath(), "-verbose", "-nosecurity", "-Xnofork"};

        Boot.main(args);
    }
}
