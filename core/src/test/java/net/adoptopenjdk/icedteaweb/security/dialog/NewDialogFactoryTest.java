package net.adoptopenjdk.icedteaweb.security.dialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.signing.JarCertVerifier;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import static net.sourceforge.jnlp.security.AccessType.CLIPBOARD_READ;
import static net.sourceforge.jnlp.security.AccessType.CLIPBOARD_WRITE;
import static net.sourceforge.jnlp.security.AccessType.CREATE_DESKTOP_SHORTCUT;
import static net.sourceforge.jnlp.security.AccessType.NETWORK;
import static net.sourceforge.jnlp.security.AccessType.PRINTER;
import static net.sourceforge.jnlp.security.AccessType.READ_FILE;
import static net.sourceforge.jnlp.security.AccessType.READ_WRITE_FILE;
import static net.sourceforge.jnlp.security.AccessType.UNVERIFIED;
import static net.sourceforge.jnlp.security.AccessType.WRITE_FILE;

/**
 * Helper class to start dialogs without starting ITW.
 */
public class NewDialogFactoryTest {
    private final JNLPFile file;
    private final HttpsCertVerifier httpsCertVerifier;
    private final JarCertVerifier jarCertVerifier;
    private final NewDialogFactory dialogFactory;

    public NewDialogFactoryTest() throws Exception {
        JNLPRuntime.initialize();
        file = new JNLPFileFactory().create(getClass().getResource("/net/sourceforge/jnlp/basic.jnlp"));
        file.setSignedJNLPAsMissing();
        httpsCertVerifier = new HttpsCertVerifier(new X509Certificate[0], true, true, "hostname");
        jarCertVerifier = new JarCertVerifier();
        dialogFactory = new NewDialogFactory();
    }

    public static void main(String[] args) throws Exception {
        // new NewDialogFactoryTest().showAccessWarning();
        // new NewDialogFactoryTest().showCertWarning();
        new NewDialogFactoryTest().showUnsignedWarning();
    }

    private void showAccessWarning() {
        Arrays.asList(READ_WRITE_FILE, READ_FILE, WRITE_FILE, CLIPBOARD_READ, CLIPBOARD_WRITE, PRINTER, NETWORK, CREATE_DESKTOP_SHORTCUT)
                .forEach(accessType -> dialogFactory.showAccessWarningDialog(accessType, file, new Object[]{"test"}));
    }

    private void showUnsignedWarning() {
        dialogFactory.showUnsignedWarningDialog(file);
    }

    private void showCertWarning() {
        dialogFactory.showCertWarningDialog(UNVERIFIED, file, jarCertVerifier, null);
        dialogFactory.showCertWarningDialog(UNVERIFIED, file, httpsCertVerifier, null);
    }

    private void showPartiallySignedWarning() {
        dialogFactory.showPartiallySignedWarningDialog(file, jarCertVerifier, null);
    }
}
