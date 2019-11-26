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
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction.ALWAYS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit Rule to set temporary config and cache directory for ITW within a test.
 */
public class TemporaryItwHome implements TestRule {

    private final TemporaryFolder tmpFolder = new TemporaryFolder();
    private final EnvironmentVariables envVars = new EnvironmentVariables();

    private File tempConfigHome;
    private File tempCacheHome;

    private File saveTo;

    @Override
    public Statement apply(Statement base, Description description) {
        Statement with = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
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
        assertTrue(tempConfigHome.mkdirs());

        tempCacheHome = new File(cacheBase, "icedtea-web");
        assertTrue(tempCacheHome.mkdirs());
    }

    private void after() {
        if (saveTo != null) {
            if (!saveTo.exists()) {
                assertTrue(saveTo.mkdirs());
            }
            if (!saveTo.isDirectory()) {
                fail(saveTo + " is not a directory");
            }
            copyFolder(getConfigHome().toPath(), new File(saveTo, "config").toPath());
            copyFolder(getCacheHome().toPath(), new File(saveTo, "cache").toPath());
        }
    }

    private void copyFolder(Path src, Path dest, CopyOption... options) {
        try {
            Files.walk(src).forEach(s -> {
                try {
                    Path d = dest.resolve(src.relativize(s));
                    if (Files.isDirectory(s)) {
                        if (!Files.exists(d))
                            Files.createDirectory(d);
                        return;
                    }
                    Files.copy(s, d, options);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public File getConfigHome() {
        return tempConfigHome;
    }

    public File getCacheHome() {
        return tempCacheHome;
    }

    public void saveLogsTo(File saveTo) throws IOException {
        addDeploymentProps(KEY_ENABLE_LOGGING_TOFILE, true);
        setSaveTo(saveTo);
    }

    public void setSaveTo(File saveTo) {
        this.saveTo = saveTo;
    }

    public void addDeploymentProps(String key, Object value) throws IOException {
        final Map<String, String> props = new HashMap<>();
        props.put(key, value != null ? value.toString() : null);
        createDeploymentProps(props);
    }

    public void createDeploymentProps(Map<String, String> deployment) throws IOException {
        Set<Map.Entry<String, String>> values = deployment.entrySet();
        for (Map.Entry<String, String> e: values) {
            JNLPRuntime.getConfiguration().setProperty(e.getKey(), e.getValue());
        }
        JNLPRuntime.getConfiguration().save();
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
        assertTrue(trustSettings.createNewFile());

        try (final FileWriter writer = new FileWriter(trustSettings)) {
            writer.write(UnsignedAppletActionStorageImpl.versionPrefix + " " + UnsignedAppletActionStorageImpl.currentVersion + " - generated\n");
            unsignedAppletActionEntry.write(writer);
            writer.flush();
        }
    }

}
