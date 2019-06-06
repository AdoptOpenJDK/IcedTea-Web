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

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesCancel;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.util.UrlUtils;

import javax.swing.JDialog;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetPermission;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.Semaphore;

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

    private final static Logger LOG = LoggerFactory.getLogger(SecurityDialogs.class);

    /**
     * Types of dialogs we can create
     */
    public static enum DialogType {

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

    /**
     * Shows a warning dialog for different types of system access (i.e. file
     * open/save, clipboard read/write, printing, etc).
     *
     * @param accessType the type of system access requested.
     * @param file the jnlp file associated with the requesting application.
     * @param extras array of objects used as extra.toString or similarly later
     * @return true if permission was granted by the user, false otherwise.
     */
    public static AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType,
            final JNLPFile file, final Object[] extras) {

        final SecurityDialogMessage message = new SecurityDialogMessage(file);

        message.dialogType = DialogType.ACCESS_WARNING;
        message.accessType = accessType;
        message.extras = extras;

        return (AccessWarningPaneComplexReturn) getUserResponse(message);

    }

    /**
     * Shows a warning dialog for when a plugin applet is unsigned. This is used
     * with 'high-security' setting.
     *
     * @param file the file to be base as information source for this dialogue
     * @return true if permission was granted by the user, false otherwise.
     */
    public static YesNoSandboxLimited showUnsignedWarningDialog(JNLPFile file) {

        final SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.UNSIGNED_WARNING;
        message.accessType = AccessType.UNSIGNED;

        DialogResult r = getUserResponse(message);

        return (YesNoSandboxLimited) r;
    }

    /**
     * Shows a security warning dialog according to the specified type of
     * access. If {@code accessType} is one of {@link AccessType#VERIFIED} or
     * {@link AccessType#UNVERIFIED}, extra details will be available with
     * regards to code signing and signing certificates.
     *
     * @param accessType the type of warning dialog to show
     * @param file the JNLPFile associated with this warning
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param securityDelegate the delegate for security atts.
     *
     * @return RUN if the user accepted the certificate, SANDBOX if the user
     * wants the applet to run with only sandbox permissions, or CANCEL if the
     * user did not accept running the applet
     */
    public static YesNoSandbox showCertWarningDialog(AccessType accessType,
                                                     JNLPFile file, CertVerifier certVerifier, SecurityDelegate securityDelegate) {

        final SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.CERT_WARNING;
        message.accessType = accessType;
        message.certVerifier = certVerifier;
        message.extras = new Object[]{securityDelegate};

        DialogResult selectedValue = getUserResponse(message);

        return (YesNoSandbox) selectedValue;
    }

    /**
     * Shows a warning dialog for when an applet or application is partially
     * signed.
     *
     * @param file the JNLPFile associated with this warning
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param securityDelegate the delegate for security atts.
     * @return true if permission was granted by the user, false otherwise.
     */
    public static YesNoSandbox showPartiallySignedWarningDialog(JNLPFile file, CertVerifier certVerifier,
            SecurityDelegate securityDelegate) {

        final SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.PARTIALLY_SIGNED_WARNING;
        message.accessType = AccessType.PARTIALLY_SIGNED;
        message.certVerifier = certVerifier;
        message.extras = new Object[]{securityDelegate};

        DialogResult r = getUserResponse(message);
        return (YesNoSandbox) r;
    }

    /**
     * Present a dialog to the user asking them for authentication information,
     * and returns the user's response. The caller must have
     * NetPermission("requestPasswordAuthentication") for this to work.
     *
     * @param host The host for with authentication is needed
     * @param port The port being accessed
     * @param prompt The prompt (realm) as presented by the server
     * @param type The type of server (proxy/web)
     * @return an array of objects representing user's authentication tokens
     * @throws SecurityException if the caller does not have the appropriate
     * permissions.
     */
    public static NamePassword showAuthenticationPrompt(String host, int port, String prompt, String type) {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission
                    = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }

        final SecurityDialogMessage message = new SecurityDialogMessage(null);

        message.dialogType = DialogType.AUTHENTICATION;
        message.extras = new Object[]{host, port, prompt, type};

        DialogResult response = getUserResponse(message);
        LOG.debug("Decided action for matching alaca at  was {}", response);
        return (NamePassword) response;
    }

    public static boolean showMissingALACAttributePanel(JNLPFile file, URL codeBase, Set<URL> remoteUrls) {

        SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.MISSING_ALACA;
        String urlToShow = file.getNotNullProbableCodeBase().toExternalForm();
        if (codeBase != null) {
            urlToShow = codeBase.toString();
        } else {
            LOG.warn("Warning, null codebase wants to show in ALACA!");
        }
        message.extras = new Object[]{urlToShow, UrlUtils.setOfUrlsToHtmlList(remoteUrls)};
        DialogResult selectedValue = getUserResponse(message);

        LOG.debug("Decided action for matching alaca at {} was {}", file.getCodeBase(), selectedValue);

        if (selectedValue == null) {
            return false;
        }
        return selectedValue.toBoolean();
    }

    public static boolean showMatchingALACAttributePanel(JNLPFile file, URL documentBase, Set<URL> remoteUrls) {

        SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.MATCHING_ALACA;
        String docBaseString = "null-documentbase";
        if (documentBase != null) {
            docBaseString = documentBase.toString();
        }
        message.extras = new Object[]{docBaseString, UrlUtils.setOfUrlsToHtmlList(remoteUrls)};
        DialogResult selectedValue = getUserResponse(message);

        LOG.debug("Decided action for matching alaca at {} was {}", file.getCodeBase(), selectedValue);

        if (selectedValue != null) {
            return selectedValue.toBoolean();
        }

        return false;

    }

    public static boolean showMissingPermissionsAttributeDialogue(JNLPFile file) {

        SecurityDialogMessage message = new SecurityDialogMessage(file);
        message.dialogType = DialogType.UNSIGNED_EAS_NO_PERMISSIONS_WARNING;
        DialogResult selectedValue = getUserResponse(message);
        LOG.debug("Decided action for missing permissions at {} was {}", file.getCodeBase(), selectedValue);

        if (selectedValue != null) {
            return selectedValue.toBoolean();
        }

        return false;
    }

    /**
     * Posts the message to the SecurityThread and gets the response. Blocks
     * until a response has been received. It's safe to call this from an
     * EventDispatchThread.
     *
     * @param message the SecurityDialogMessage indicating what type of dialog to
     * display
     * @return The user's response. Can be null. The exact answer depends on the
     * type of message, but generally an Integer corresponding to the value 0
     * indicates success/proceed, and everything else indicates failure
     */
    private static DialogResult getUserResponse(final SecurityDialogMessage message) {
        /*
         * Want to show a security warning, while blocking the client
         * application. This would be easy except there is a bug in showing
         * modal JDialogs in a different AppContext. The source EventQueue -
         * that sends the message to the (destination) EventQueue which is
         * supposed to actually show the dialog - must not block. If the source
         * EventQueue blocks, the destination EventQueue stops responding. So we
         * have a hack here to work around it.
         */

        /*
         * If this is the event dispatch thread the use the hack
         */
        if (SwingUtils.isEventDispatchThread()) {
            /*
             * Create a tiny modal dialog (which creates a new EventQueue for
             * this AppContext, but blocks the original client EventQueue) and
             * then post the message - this makes the source EventQueue continue
             * running - but dot not allow the actual applet/application to
             * continue processing
             */
            final JDialog fakeDialog = new JDialog();
            fakeDialog.setName("FakeDialog");
            SwingUtils.info(fakeDialog);
            fakeDialog.setSize(0, 0);
            fakeDialog.setResizable(false);
            fakeDialog.setModalityType(ModalityType.APPLICATION_MODAL);
            fakeDialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    message.toDispose = fakeDialog;
                    message.lock = null;
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        @Override
                        public Void run() {
                            JNLPRuntime.getSecurityDialogHandler().postMessage(message);
                            return null;
                        }
                    });
                }
            });

            /* this dialog will be disposed/hidden when the user closes the security prompt */
            fakeDialog.setVisible(true);
        } else {
            /*
             * Otherwise do it the normal way. Post a message to the security
             * thread to make it show the security dialog. Wait until it tells us
             * to proceed.
             */
            message.toDispose = null;
            message.lock = new Semaphore(0);
            JNLPRuntime.getSecurityDialogHandler().postMessage(message);

            boolean done = false;
            while (!done) {
                try {
                    message.lock.acquire();
                    done = true;
                } catch (InterruptedException e) {
                    // ignore; retry
                }
            }

        }
        return message.userResponse;
    }

    // false = terminate ITW
    // true = continue
    public static boolean show511Dialogue(Resource r) {
        SecurityDialogMessage message = new SecurityDialogMessage(null);
        message.dialogType = DialogType.SECURITY_511;
        message.extras = new Object[]{r.getLocation()};
        DialogResult selectedValue = getUserResponse(message);
        if (selectedValue != null && selectedValue.equals(YesCancel.cancel())) {
            return false; //kill command
        }
        return true;
    }

}
