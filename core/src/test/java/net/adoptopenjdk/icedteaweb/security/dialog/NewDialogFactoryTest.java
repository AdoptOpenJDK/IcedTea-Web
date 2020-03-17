package net.adoptopenjdk.icedteaweb.security.dialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.signing.JarCertVerifier;
import sun.security.x509.X509CertImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
        //Locale.setDefault(Locale.GERMAN);
        Locale.setDefault(Locale.ENGLISH);
        JNLPRuntime.initialize();
        file = new JNLPFileFactory().create(getClass().getResource("/net/sourceforge/jnlp/basic.jnlp"));
        httpsCertVerifier = new HttpsCertVerifier(new X509Certificate[]{}, true, true, "hostname");
        jarCertVerifier = new JarCertVerifier();
        dialogFactory = new NewDialogFactory();
    }

    public static void main(String[] args) throws Exception {
        // new NewDialogFactoryTest().showAccessWarningDialog();
        // new NewDialogFactoryTest().showCertWarningDialog();
        // new NewDialogFactoryTest().showMoreInfoDialog();
        // new NewDialogFactoryTest().showPartiallySignedWarningDialog();
        // new NewDialogFactoryTest().showCertInfoDialog();
        // new NewDialogFactoryTest().showMissingPermissionsAttributeDialog();
        // new NewDialogFactoryTest().showMissingALACAttributeDialog();
        // new NewDialogFactoryTest().showMatchingALACAttributeDialog();
        new NewDialogFactoryTest().showAuthenticationPrompt();
    }

    private void showAccessWarningDialog() {
        Arrays.asList(READ_WRITE_FILE, READ_FILE, WRITE_FILE, CLIPBOARD_READ, CLIPBOARD_WRITE, PRINTER, NETWORK, CREATE_DESKTOP_SHORTCUT)
                .forEach(accessType -> dialogFactory.showAccessWarningDialog(accessType, file, new Object[]{"test"}));
    }

    private void showCertWarningDialog() {
        dialogFactory.showCertWarningDialog(UNVERIFIED, file, jarCertVerifier, null);
        dialogFactory.showCertWarningDialog(UNVERIFIED, file, httpsCertVerifier, null);
    }

    private void showMoreInfoDialog() {
        dialogFactory.showMoreInfoDialog(jarCertVerifier, file);
    }

    private void showPartiallySignedWarningDialog() {
        dialogFactory.showPartiallySignedWarningDialog(file, jarCertVerifier, null);
    }

    private void showCertInfoDialog() {
        dialogFactory.showCertInfoDialog(httpsCertVerifier, null);
        dialogFactory.showSingleCertInfoDialog(new X509CertImpl(), null);
    }

    private void showMissingPermissionsAttributeDialog() {
        dialogFactory.showMissingPermissionsAttributeDialogue(file);
    }

    private void showMissingALACAttributeDialog() throws MalformedURLException {
        final URL codeBase = new URL("http://localhost/");
        Set<URL> remoteUrls = new HashSet<>();
        remoteUrls.add(new URL("http:/differentlocation.com/one"));
        remoteUrls.add(new URL("http:/differentlocation.com/one/two"));

        dialogFactory.showMissingALACAttributePanel(file, codeBase, remoteUrls);
    }

    private void showMatchingALACAttributeDialog() throws MalformedURLException {
        final URL codeBase = new URL("http://localhost/");
        Set<URL> remoteUrls = new HashSet<>();
        remoteUrls.add(new URL("http:/localhost/one"));
        remoteUrls.add(new URL("http:/localhost/one/two"));

        dialogFactory.showMatchingALACAttributePanel(file, codeBase, remoteUrls);
    }

    private void showAuthenticationPrompt() {
        dialogFactory.showAuthenticationPrompt("http://localhost/", 666, "Authentication required with pro account credentials", "Web");
    }
}
