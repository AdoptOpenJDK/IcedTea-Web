package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingPermissionsAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UrlRegEx;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction.ALWAYS;

/**
 * JUnit Rule to set temporary config and cache directory for ITW within a test.
 */
public class TemporaryItwHome implements TestRule {

    private final TemporaryFolder tmpFolder = new TemporaryFolder();
    private final EnvironmentVariables envVars = new EnvironmentVariables();

    private File tempConfigHome;
    private File tempCacheHome;

    @Override
    public Statement apply(Statement base, Description description) {
        Statement with = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                base.evaluate();
            }
        };
        return envVars.apply(tmpFolder.apply(with, description), description);
    }

    private void before() throws IOException {
        final File configBase = tmpFolder.newFolder("config");
        final File cacheBase = tmpFolder.newFolder("cache");

        envVars.set(ConfigurationConstants.XDG_CONFIG_HOME_VAR, configBase.getAbsolutePath());
        envVars.set(ConfigurationConstants.XDG_CACHE_HOME_VAR, cacheBase.getAbsolutePath());

        tempConfigHome = new File(configBase, "icedtea-web");
        tempConfigHome.mkdirs();

        tempCacheHome = new File(cacheBase, "icedtea-web");
        tempCacheHome.mkdirs();
    }

    public File getConfigHome() {
        return tempConfigHome;
    }

    public File getCacheHome() {
        return tempCacheHome;
    }

    public void createTrustSettings(String jnlpUrl) throws IOException {
        final String source = StringUtils.substringBeforeLast(jnlpUrl, "/") + "/";
        final AppletSecurityActions securityActions = new AppletSecurityActions();
        securityActions.setAction(MissingPermissionsAttributePanel.class, new SavedRememberAction(ALWAYS, "YES"));
        securityActions.setAction(MissingALACAttributePanel.class, new SavedRememberAction(ALWAYS, "YES"));

        final UnsignedAppletActionEntry unsignedAppletActionEntry = new UnsignedAppletActionEntry(
                securityActions,
                new Date(1558708474622L),
                UrlRegEx.quoteAndStar(source),
                UrlRegEx.quote(source),
                null);

        final File trustSettings = new File(getConfigHome(), ".appletTrustSettings");
        trustSettings.createNewFile();

        try (final FileWriter writer = new FileWriter(trustSettings)) {
            writer.write(UnsignedAppletActionStorageImpl.versionPrefix + " " + UnsignedAppletActionStorageImpl.currentVersion + " - generated\n");
            unsignedAppletActionEntry.write(writer);
            writer.flush();
        }
    }

}
