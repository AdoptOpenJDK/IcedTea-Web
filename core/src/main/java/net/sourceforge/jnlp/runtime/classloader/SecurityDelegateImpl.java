package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URL;
import java.security.Permission;
import java.util.Collection;

import static net.sourceforge.jnlp.LaunchException.FATAL;

/**
 * Handles security decision logic for the JNLPClassLoader, eg which
 * permission level to assign to JARs.
 */
public class SecurityDelegateImpl implements SecurityDelegate {

    private final JNLPClassLoader classLoader;
    private boolean runInSandbox;
    private boolean promptedForPartialSigning;

    SecurityDelegateImpl(final JNLPClassLoader classLoader) {
        this.classLoader = classLoader;
        runInSandbox = false;
    }

    @Override
    public SecurityDesc getCodebaseSecurityDesc(final JARDesc jarDesc, final URL codebaseHost) {
        if (runInSandbox) {
            return new SecurityDesc(classLoader.getJNLPFile(), AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        } else {
            return classLoader.getJNLPFile().getSecurity();
        }
    }

    @Override
    public SecurityDesc getClassLoaderSecurity(final URL codebaseHost) throws LaunchException {
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
        if (!runInSandbox && !classLoader.getSigning()
                && !classLoader.getJNLPFile().getSecurity().getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
            if (classLoader.jcv.allJarsSigned()) {
                LaunchException ex = new LaunchException(classLoader.getJNLPFile(), null, FATAL, "Application Error", "The JNLP application is not fully signed by a single cert.", "The JNLP application has its components individually signed, however there must be a common signer to all entries.");
                JNLPClassLoader.consultCertificateSecurityException(ex);
                return consultResult(codebaseHost);
            } else {
                LaunchException ex = new LaunchException(classLoader.getJNLPFile(), null, FATAL, "Application Error", "Cannot grant permissions to unsigned jars.", "Application requested security permissions, but jars are not signed.");
                JNLPClassLoader.consultCertificateSecurityException(ex);
                return consultResult(codebaseHost);
            }
        } else {
            return consultResult(codebaseHost);
        }
    }

    private SecurityDesc consultResult(URL codebaseHost) {
        if (!runInSandbox && classLoader.getSigning()) {
            return classLoader.getJNLPFile().getSecurity();
        } else {
            return new SecurityDesc(classLoader.getJNLPFile(), AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        }
    }

    @Override
    public SecurityDesc getJarPermissions(final URL codebaseHost) {
        if (!runInSandbox && classLoader.jcv.isFullySigned()) {
            // Already trust application, nested jar should be given
            return new SecurityDesc(classLoader.getJNLPFile(), AppletPermissionLevel.NONE,
                    SecurityDesc.ALL_PERMISSIONS,
                    codebaseHost);
        } else {
            return new SecurityDesc(classLoader.getJNLPFile(), AppletPermissionLevel.NONE,
                    SecurityDesc.SANDBOX_PERMISSIONS,
                    codebaseHost);
        }
    }

    @Override
    public void setRunInSandbox() throws LaunchException {
        if (runInSandbox && classLoader.getSecurity() != null
                && !classLoader.jarLocationSecurityMap.isEmpty()) {
            throw new LaunchException(classLoader.getJNLPFile(), null, FATAL, "Initialization Error", "Run in Sandbox call performed too late.", "The classloader was notified to run the applet sandboxed, but security settings were already initialized.");
        }

        JNLPRuntime.reloadPolicy();
        // ensure that we have the most up-to-date custom policy loaded since the user may have just launched PolicyEditor
        // to create a custom policy for the applet they are about to run
        this.runInSandbox = true;
    }

    @Override
    public void promptUserOnPartialSigning() throws LaunchException {
        if (promptedForPartialSigning) {
            return;
        }
        promptedForPartialSigning = true;
        UnsignedAppletTrustConfirmation.checkPartiallySignedWithUserIfRequired(this, classLoader.getJNLPFile(), classLoader.jcv);
    }

    @Override
    public boolean getRunInSandbox() {
        return this.runInSandbox;
    }

    @Override
    public void addPermissions(final Collection<Permission> perms) {
        for (final Permission perm : perms) {
            classLoader.addPermission(perm);
        }
    }

}
