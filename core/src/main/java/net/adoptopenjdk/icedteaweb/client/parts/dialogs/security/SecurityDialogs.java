/* SecurityDialogs.java
 Copyright (C) 2010 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.adoptopenjdk.icedteaweb.Assert;
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

/**
 * <p>
 * A factory for showing many possible types of security warning to the user.
 * </p>
 * <p>
 * This contains all the public methods that classes outside this package should
 * use instead of using {@link SecurityDialog} directly.
 * </p>
 * <p>
 * All of these methods post a message to the
 * {@link SecurityDialogMessageHandler} and block waiting for a response.
 * </p>
 */
public class SecurityDialogs {

    /**
     * Types of dialogs we can create
     */
    enum DialogType {
        CERT_WARNING,
        MORE_INFO,
        CERT_INFO,
        SINGLE_CERT_INFO,
        ACCESS_WARNING,
        PARTIALLY_SIGNED_WARNING,
        UNSIGNED_WARNING, /* requires confirmation with 'high-security' setting */
        APPLET_WARNING,
        AUTHENTICATION,
        UNSIGNED_EAS_NO_PERMISSIONS_WARNING, /* when Extended applet security is at High Security and no permission attribute is find, */
        MISSING_ALACA, /*alaca - Application-Library-Allowable-Codebase Attribute*/
        MATCHING_ALACA,
        SECURITY_511
    }

    private static final Dialogs defaultDialogs = new SecurityDialogsImpl();
    private static Dialogs testDialogs = null;

    static synchronized Runnable setDialogForTesting(Dialogs dialogs) {
        Assert.requireNonNull(dialogs, "dialogs");
        if (testDialogs != null) {
            throw new IllegalStateException("test dialogs already set");
        }

        testDialogs = dialogs;
        return () -> testDialogs = null;
    }

    private static Dialogs getDialogs() {
        if (testDialogs != null) {
            return testDialogs;
        }
        return defaultDialogs;
    }


    /**
     * see {@link Dialogs#showAccessWarningDialog(AccessType, JNLPFile, Object[])}.
     */
    public static AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType,
                                                                         final JNLPFile file, final Object[] extras) {
        return getDialogs().showAccessWarningDialog(accessType, file, extras);
    }

    /**
     * see {@link Dialogs#showUnsignedWarningDialog(JNLPFile)}.
     */
    public static YesNoSandboxLimited showUnsignedWarningDialog(JNLPFile file) {
        return getDialogs().showUnsignedWarningDialog(file);
    }

    /**
     * see {@link Dialogs#showCertWarningDialog(AccessType, JNLPFile, CertVerifier, SecurityDelegate)}.
     */
    public static YesNoSandbox showCertWarningDialog(AccessType accessType,
                                                     JNLPFile file, CertVerifier certVerifier, SecurityDelegate securityDelegate) {
        return getDialogs().showCertWarningDialog(accessType, file, certVerifier, securityDelegate);
    }

    /**
     * see {@link Dialogs#showPartiallySignedWarningDialog(JNLPFile, CertVerifier, SecurityDelegate)}.
     */
    public static YesNoSandbox showPartiallySignedWarningDialog(JNLPFile file, CertVerifier certVerifier,
                                                                SecurityDelegate securityDelegate) {
        return getDialogs().showPartiallySignedWarningDialog(file, certVerifier, securityDelegate);
    }

    /**
     * see {@link Dialogs#showAuthenticationPrompt(String, int, String, String)}.
     */
    public static NamePassword showAuthenticationPrompt(String host, int port, String prompt, String type) {
        return getDialogs().showAuthenticationPrompt(host, port, prompt, type);
    }

    /**
     * see {@link Dialogs#showMissingALACAttributePanel(JNLPFile, URL, Set)}
     */
    public static boolean showMissingALACAttributePanel(JNLPFile file, URL codeBase, Set<URL> remoteUrls) {
        return getDialogs().showMissingALACAttributePanel(file, codeBase, remoteUrls);
    }

    /**
     * see {@link Dialogs#showMatchingALACAttributePanel(JNLPFile, URL, Set)}.
     */
    public static boolean showMatchingALACAttributePanel(JNLPFile file, URL documentBase, Set<URL> remoteUrls) {
        return getDialogs().showMatchingALACAttributePanel(file, documentBase, remoteUrls);
    }

    /**
     * see {@link Dialogs#showMissingPermissionsAttributeDialogue(JNLPFile)}.
     */
    public static boolean showMissingPermissionsAttributeDialogue(JNLPFile file) {
        return getDialogs().showMissingPermissionsAttributeDialogue(file);
    }

    /**
     * see {@link Dialogs#show511Dialogue(Resource)}.
     */
    public static boolean show511Dialogue(Resource r) {
        return getDialogs().show511Dialogue(r);
    }

    /**
     * see {@link Dialogs#showMoreInfoDialog(CertVerifier, SecurityDialog)}.
     */
    public static void showMoreInfoDialog(CertVerifier certVerifier, SecurityDialog parent) {
        getDialogs().showMoreInfoDialog(certVerifier, parent);
    }

    /**
     * see {@link Dialogs#showCertInfoDialog(CertVerifier, Component)}.
     */
    public static void showCertInfoDialog(CertVerifier certVerifier, Component parent) {
        getDialogs().showCertInfoDialog(certVerifier, parent);
    }

    /**
     * see {@link Dialogs#showSingleCertInfoDialog(X509Certificate, Window)}.
     */
    public static void showSingleCertInfoDialog(X509Certificate c, Window parent) {
        getDialogs().showSingleCertInfoDialog(c, parent);
    }

    public interface Dialogs {

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

}
