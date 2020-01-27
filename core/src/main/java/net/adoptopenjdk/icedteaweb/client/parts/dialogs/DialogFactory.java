package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessage;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;

import java.awt.Component;
import java.awt.Window;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Set;

public interface DialogFactory {

    /**
     * Shows a warning dialog for different types of system access (i.e. file
     * open/save, clipboard read/write, printing, etc).
     *
     * @param accessType the type of system access requested.
     * @param file       the jnlp file associated with the requesting application.
     * @param extras     array of objects used as extra.toString or similarly later
     * @return true if permission was granted by the user, false otherwise.
     */
    AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType,
                                                           final JNLPFile file, final Object[] extras);

    /**
     * Shows a warning dialog for when a plugin applet is unsigned. This is used
     * with 'high-security' setting.
     *
     * @param file the file to be base as information source for this dialogue
     * @return true if permission was granted by the user, false otherwise.
     */
    YesNoSandboxLimited showUnsignedWarningDialog(JNLPFile file);

    /**
     * Shows a security warning dialog according to the specified type of
     * access. If {@code accessType} is one of {@link AccessType#VERIFIED} or
     * {@link AccessType#UNVERIFIED}, extra details will be available with
     * regards to code signing and signing certificates.
     *
     * @param accessType       the type of warning dialog to show
     * @param file             the JNLPFile associated with this warning
     * @param certVerifier     the JarCertVerifier used to verify this application
     * @param securityDelegate the delegate for security atts.
     * @return RUN if the user accepted the certificate, SANDBOX if the user
     * wants the applet to run with only sandbox permissions, or CANCEL if the
     * user did not accept running the applet
     */
    YesNoSandbox showCertWarningDialog(AccessType accessType,
                                       JNLPFile file, CertVerifier certVerifier, SecurityDelegate securityDelegate);

    /**
     * Shows a warning dialog for when an applet or application is partially
     * signed.
     *
     * @param file             the JNLPFile associated with this warning
     * @param certVerifier     the JarCertVerifier used to verify this application
     * @param securityDelegate the delegate for security atts.
     * @return true if permission was granted by the user, false otherwise.
     */
    YesNoSandbox showPartiallySignedWarningDialog(JNLPFile file, CertVerifier certVerifier,
                                                  SecurityDelegate securityDelegate);

    /**
     * Present a dialog to the user asking them for authentication information,
     * and returns the user's response. The caller must have
     * NetPermission("requestPasswordAuthentication") for this to work.
     *
     * @param host   The host for with authentication is needed
     * @param port   The port being accessed
     * @param prompt The prompt (realm) as presented by the server
     * @param type   The type of server (proxy/web)
     * @return an array of objects representing user's authentication tokens
     * @throws SecurityException if the caller does not have the appropriate
     *                           permissions.
     */
    NamePassword showAuthenticationPrompt(String host, int port, String prompt, String type);

    boolean showMissingALACAttributePanel(JNLPFile file, URL codeBase, Set<URL> remoteUrls);

    boolean showMatchingALACAttributePanel(JNLPFile file, URL documentBase, Set<URL> remoteUrls);

    boolean showMissingPermissionsAttributeDialogue(JNLPFile file);

    /**
     * Posts the message to the SecurityThread and gets the response. Blocks
     * until a response has been received. It's safe to call this from an
     * EventDispatchThread.
     *
     * @param message the SecurityDialogMessage indicating what type of dialog to
     *                display
     * @return The user's response. Can be null. The exact answer depends on the
     * type of message, but generally an Integer corresponding to the value 0
     * indicates success/proceed, and everything else indicates failure
     */
    DialogResult getUserResponse(final SecurityDialogMessage message);

    /**
     * false = terminate ITW
     * true = continue
     */
    boolean show511Dialogue(Resource r);

    /**
     * Shows more information regarding jar code signing
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent       the parent NumberOfArguments pane
     */
    void showMoreInfoDialog(CertVerifier certVerifier, SecurityDialog parent);

    /**
     * Displays CertPath information in a readable table format.
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent       the parent NumberOfArguments pane
     */
    void showCertInfoDialog(CertVerifier certVerifier, Component parent);

    /**
     * Displays a single certificate's information.
     *
     * @param c      the X509 certificate.
     * @param parent the parent pane.
     */
    void showSingleCertInfoDialog(X509Certificate c, Window parent);
}
