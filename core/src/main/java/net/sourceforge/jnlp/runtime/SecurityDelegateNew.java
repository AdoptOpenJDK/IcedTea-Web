package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.ConfigurationConstants;

import java.security.Permission;
import java.util.Collection;

import static net.sourceforge.jnlp.LaunchException.FATAL;

/**
 * Various combinations of the jars being signed and <security> tags being
 * present are possible. They are treated as follows
 *
 * Jars          JNLP File         Result
 *
 * Signed        <security>        Appropriate Permissions
 * Signed        no <security>     Sandbox
 * Unsigned      <security>        Error
 * Unsigned      no <security>     Sandbox
 *
 */
public class SecurityDelegateNew implements SecurityDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityDelegateNew.class);

    private boolean runInSandbox;
    private boolean promptedForPartialSigning;

    private final ApplicationPermissions applicationPermissions;

    private final JNLPFile jnlpFile;

    public SecurityDelegateNew(final ApplicationPermissions applicationPermissions, final JNLPFile jnlpFile) {
        this.applicationPermissions = applicationPermissions;
        this.jnlpFile = jnlpFile;
        runInSandbox = false;
    }

    static void consultCertificateSecurityException(LaunchException ex) throws LaunchException {
        if (isCertUnderestimated()) {
            LOG.error("{} and {} are declared. Ignoring certificate issue", CommandLineOptions.NOSEC.getOption(), ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES);
        } else {
            throw ex;
        }
    }

    private static boolean isCertUnderestimated() {
        return Boolean.parseBoolean(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES))
                && !JNLPRuntime.isSecurityEnabled();
    }

    @Override
    public void setRunInSandbox() throws LaunchException {
        if (runInSandbox && !applicationPermissions.getAllJarLocations().isEmpty()) {
            throw new LaunchException(jnlpFile, null, FATAL, "Initialization Error", "Run in Sandbox call performed too late.", "The classloader was notified to run the applet sandboxed, but security settings were already initialized.");
        }

        // TODO: refresh policy to make sure we have the latest and greatest from the file system
        this.runInSandbox = true;
    }

    @Override
    public void promptUserOnPartialSigning() throws LaunchException {
        if (promptedForPartialSigning) {
            return;
        }
        promptedForPartialSigning = true;
        // TODO: the following line will trigger a NPE further down in the call
        UnsignedAppletTrustConfirmation.checkPartiallySignedWithUserIfRequired(this, jnlpFile, null);
    }

    @Override
    public boolean getRunInSandbox() {
        return this.runInSandbox;
    }

    @Override
    public void addPermissions(final Collection<Permission> perms) {
        for (final Permission perm : perms) {
            applicationPermissions.addRuntimePermission(perm);
        }
    }

}
