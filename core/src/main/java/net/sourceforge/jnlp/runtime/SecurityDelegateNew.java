package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.signing.JarCertVerifier;

import java.net.URL;
import java.security.Permission;
import java.util.Collection;

import static net.sourceforge.jnlp.LaunchException.FATAL;

public class SecurityDelegateNew implements SecurityDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityDelegateNew.class);

    private boolean runInSandbox;
    private boolean promptedForPartialSigning;

    private final ApplicationPermissions applicationPermissions;

    private final JNLPFile jnlpFile;

    private final JarCertVerifier certVerifier;

    public SecurityDelegateNew(final ApplicationPermissions applicationPermissions, final JNLPFile jnlpFile, final JarCertVerifier certVerifier) {
        this.applicationPermissions = applicationPermissions;
        this.jnlpFile = jnlpFile;
        this.certVerifier = certVerifier;
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

    boolean isPluginApplet() {
        return false;
    }

    @Override
    public SecurityDesc getCodebaseSecurityDesc(final JARDesc jarDesc, final URL codebaseHost) {
        if (runInSandbox) {
            return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        } else {
            return jnlpFile.getSecurity();
        }
    }

    @Override
    public SecurityDesc getClassLoaderSecurity(final URL codebaseHost) throws LaunchException {
        if (isPluginApplet()) {
            if (!runInSandbox && certVerifier.isFullySigned()) {
                return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                        SecurityDesc.ALL_PERMISSIONS,
                        codebaseHost);
            } else {
                return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                        SecurityDesc.SANDBOX_PERMISSIONS,
                        codebaseHost);
            }
        } else
            /*
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
            if (!runInSandbox && !jnlpFile.getSecurity().getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
                if (certVerifier.allJarsSigned()) {
                    LaunchException ex = new LaunchException(jnlpFile, null, FATAL, "Application Error", "The JNLP application is not fully signed by a single cert.", "The JNLP application has its components individually signed, however there must be a common signer to all entries.");
                    consultCertificateSecurityException(ex);
                    return consultResult(codebaseHost);
                } else {
                    LaunchException ex = new LaunchException(jnlpFile, null, FATAL, "Application Error", "Cannot grant permissions to unsigned jars.", "Application requested security permissions, but jars are not signed.");
                    consultCertificateSecurityException(ex);
                    return consultResult(codebaseHost);
                }
            } else return consultResult(codebaseHost);
    }

    private SecurityDesc consultResult(URL codebaseHost) {
        if (!runInSandbox && certVerifier.isFullySigned()) {
            return jnlpFile.getSecurity();
        } else {
            return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        }
    }

    @Override
    public SecurityDesc getJarPermissions(final URL codebaseHost) {
        if (!runInSandbox && certVerifier.isFullySigned()) {
            // Already trust application, nested jar should be given
            return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                    SecurityDesc.ALL_PERMISSIONS,
                    codebaseHost);
        } else {
            return new SecurityDesc(jnlpFile, AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        }
    }

    @Override
    public void setRunInSandbox() throws LaunchException {
        if (runInSandbox && applicationPermissions.getSecurity() != null
                && !applicationPermissions.getAllJarLocations().isEmpty()) {
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
        UnsignedAppletTrustConfirmation.checkPartiallySignedWithUserIfRequired(this, jnlpFile, certVerifier);
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
