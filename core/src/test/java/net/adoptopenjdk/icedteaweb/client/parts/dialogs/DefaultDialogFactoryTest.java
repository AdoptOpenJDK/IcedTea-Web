package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.SecurityDelegateNew;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.signing.JarCertVerifier;

import java.security.cert.X509Certificate;

/**
 * Helper class to start dialogs without starting ITW.
 */
public class DefaultDialogFactoryTest {

    private final JNLPFile file;
    private final HttpsCertVerifier httpsCertVerifier;
    private final JarCertVerifier jarCertVerifier;
    private final DefaultDialogFactory dialogFactory;

    public DefaultDialogFactoryTest() throws Exception {
        JNLPRuntime.initialize();
        file = new JNLPFileFactory().create(getClass().getResource("/net/sourceforge/jnlp/basic.jnlp"));
        httpsCertVerifier = new HttpsCertVerifier(new X509Certificate[0], true, true, "hostname");
        jarCertVerifier = new JarCertVerifier();
        dialogFactory = new DefaultDialogFactory();
    }

    public static void main(String[] args) throws Exception {
        new DefaultDialogFactoryTest().showPartiallySignedWarning();
    }

    private void showAccessWarning() {
        dialogFactory.showAccessWarningDialog(AccessType.CREATE_DESKTOP_SHORTCUT, file, new Object[]{"test"});
    }

    private void showUnsignedWarning() {
        dialogFactory.showUnsignedWarningDialog(file);
    }

    private void showCertWarning() {
        dialogFactory.showCertWarningDialog(AccessType.UNVERIFIED, file, jarCertVerifier, null);
        dialogFactory.showCertWarningDialog(AccessType.UNVERIFIED, file, httpsCertVerifier, null);
    }

    private void showPartiallySignedWarning() {
        dialogFactory.showPartiallySignedWarningDialog(file, jarCertVerifier, new SecurityDelegateNew(null, file, null));
    }

}
