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

package net.sourceforge.jnlp.security;

import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetPermission;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.AppTrustWarningPanel.AppSigningWarningAction;
import net.sourceforge.jnlp.util.UrlUtils;

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
    /** Types of dialogs we can create */
    public static enum DialogType {
        CERT_WARNING,
        MORE_INFO,
        CERT_INFO,
        SINGLE_CERT_INFO,
        ACCESS_WARNING,
        PARTIALLYSIGNED_WARNING,
        UNSIGNED_WARNING,   /* requires confirmation with 'high-security' setting */
        APPLET_WARNING,
        AUTHENTICATION,
        UNSIGNED_EAS_NO_PERMISSIONS_WARNING,   /* when Extended applet security is at High Security and no permission attribute is find, */
        MISSING_ALACA, /*alaca - Application-Library-Allowable-Codebase Attribute*/
        MATCHING_ALACA
    }

    /** The types of access which may need user permission. */
    public static enum AccessType {
        READ_FILE,
        WRITE_FILE,
        CREATE_DESTKOP_SHORTCUT,
        CLIPBOARD_READ,
        CLIPBOARD_WRITE,
        PRINTER,
        NETWORK,
        VERIFIED,
        UNVERIFIED,
        PARTIALLYSIGNED,
        UNSIGNED,           /* requires confirmation with 'high-security' setting */
        SIGNING_ERROR
    }

    public static enum AppletAction {
        RUN,
        SANDBOX,
        CANCEL;
        public static AppletAction fromInteger(int i) {
            switch (i) {
                case 0:
                    return RUN;
                case 1:
                    return SANDBOX;
                case 2:
                    return CANCEL;
                default:
                    return CANCEL;
            }
        }
    }

    /**
     * Shows a warning dialog for different types of system access (i.e. file
     * open/save, clipboard read/write, printing, etc).
     *
     * @param accessType the type of system access requested.
     * @param file the jnlp file associated with the requesting application.
     * @return true if permission was granted by the user, false otherwise.
     */
    public static boolean showAccessWarningDialog(AccessType accessType, JNLPFile file) {
        return showAccessWarningDialog(accessType, file, null);
    }

    /**
     * Shows a warning dialog for different types of system access (i.e. file
     * open/save, clipboard read/write, printing, etc).
     *
     * @param accessType the type of system access requested.
     * @param file the jnlp file associated with the requesting application.
     * @param extras an optional array of Strings (typically) that gets
     * passed to the dialog labels.
     * @return true if permission was granted by the user, false otherwise.
     */
    public static boolean showAccessWarningDialog(final AccessType accessType,
            final JNLPFile file, final Object[] extras) {

        if (!shouldPromptUser()) {
            return false;
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();

        message.dialogType = DialogType.ACCESS_WARNING;
        message.accessType = accessType;
        message.file = file;
        message.extras = extras;

        Object selectedValue = getUserResponse(message);

        return getIntegerResponseAsBoolean(selectedValue);
    }

    /**
     * Shows a warning dialog for when a plugin applet is unsigned.
     * This is used with 'high-security' setting.
     *
     * @return true if permission was granted by the user, false otherwise.
     */
    public static AppSigningWarningAction showUnsignedWarningDialog(JNLPFile file) {

        if (!shouldPromptUser()) {
            return new AppSigningWarningAction(ExecuteAppletAction.NO, false);
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.UNSIGNED_WARNING;
        message.accessType = AccessType.UNSIGNED;
        message.file = file;

        return (AppSigningWarningAction) getUserResponse(message);
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
     *
     * @return RUN if the user accepted the certificate, SANDBOX if the user
     * wants the applet to run with only sandbox permissions, or CANCEL if the
     * user did not accept running the applet
     */
    public static AppletAction showCertWarningDialog(AccessType accessType,
            JNLPFile file, CertVerifier certVerifier, SecurityDelegate securityDelegate) {

        if (!shouldPromptUser()) {
            return AppletAction.CANCEL;
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.CERT_WARNING;
        message.accessType = accessType;
        message.file = file;
        message.certVerifier = certVerifier;
        message.extras = new Object[] { securityDelegate };

        Object selectedValue = getUserResponse(message);

        return getIntegerResponseAsAppletAction(selectedValue);
    }

    /**
     * Shows a warning dialog for when an applet or application is partially signed.
     *
     * @return true if permission was granted by the user, false otherwise.
     */
    public static AppSigningWarningAction showPartiallySignedWarningDialog(JNLPFile file, CertVerifier certVerifier,
            SecurityDelegate securityDelegate) {

        if (!shouldPromptUser()) {
            return new AppSigningWarningAction(ExecuteAppletAction.NO, false);
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.PARTIALLYSIGNED_WARNING;
        message.accessType = AccessType.PARTIALLYSIGNED;
        message.file = file;
        message.certVerifier = certVerifier;
        message.extras = new Object[] { securityDelegate };

        return (AppSigningWarningAction) getUserResponse(message);
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
     * @throws SecurityException if the caller does not have the appropriate permissions.
     */
    public static Object[] showAuthenicationPrompt(String host, int port, String prompt, String type) {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission
                = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();

        message.dialogType = DialogType.AUTHENTICATION;
        message.extras = new Object[] { host, port, prompt, type };

        Object response = getUserResponse(message);
        return (Object[]) response;
    }

     public static boolean  showMissingALACAttributePanel(String title, URL codeBase, Set<URL> remoteUrls) {

        if (!shouldPromptUser()) {
            return false;
        }

        SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.MISSING_ALACA;
        message.extras = new Object[]{title, codeBase.toString(), UrlUtils.setOfUrlsToHtmlList(remoteUrls)};
        Object selectedValue = getUserResponse(message);
        return getIntegerResponseAsBoolean(selectedValue);
    } 
     
     public static boolean showMatchingALACAttributePanel(String title, URL codeBase, Set<URL> remoteUrls) {

        if (!shouldPromptUser()) {
            return false;
        }

        SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.MATCHING_ALACA;
        message.extras = new Object[]{title, codeBase.toString(), UrlUtils.setOfUrlsToHtmlList(remoteUrls)};
        Object selectedValue = getUserResponse(message);
        return getIntegerResponseAsBoolean(selectedValue);
    } 
     
    /**
     * FIXME This is unused. Remove it?
     * @return (0, 1, 2) =&gt; (Yes, No, Cancel)
     */
    public static int showAppletWarning() {

        if (!shouldPromptUser()) {
            return 2;
        }

        SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.APPLET_WARNING;

        Object selectedValue = getUserResponse(message);

        // result 0 = Yes, 1 = No, 2 = Cancel
        if (selectedValue instanceof Integer) {
            // If the selected value can be cast to Integer, use that value
            return ((Integer) selectedValue).intValue();
        } else {
            // Otherwise default to "cancel"
            return 2;
        }
    }

     public static boolean showMissingPermissionsAttributeDialogue(String title, URL codeBase) {

         if (!shouldPromptUser()) {
             return false;
         }

         SecurityDialogMessage message = new SecurityDialogMessage();
         message.dialogType = DialogType.UNSIGNED_EAS_NO_PERMISSIONS_WARNING;
         message.extras = new Object[]{title, codeBase.toExternalForm()};
         Object selectedValue = getUserResponse(message);
         return SecurityDialogs.getIntegerResponseAsBoolean(selectedValue);
    }
    /**
     * Posts the message to the SecurityThread and gets the response. Blocks
     * until a response has been recieved. It's safe to call this from an
     * EventDispatchThread.
     *
     * @param message the SecuritDialogMessage indicating what type of dialog to
     * display
     * @return The user's response. Can be null. The exact answer depends on the
     * type of message, but generally an Integer corresponding to the value 0
     * indicates success/proceed, and everything else indicates failure
     */
    private static Object getUserResponse(final SecurityDialogMessage message) {
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
        if (SwingUtilities.isEventDispatchThread()) {
            /*
             * Create a tiny modal dialog (which creates a new EventQueue for
             * this AppContext, but blocks the original client EventQueue) and
             * then post the message - this makes the source EventQueue continue
             * running - but dot not allow the actual applet/application to
             * continue processing
             */
            final JDialog fakeDialog = new JDialog();
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

    /**
     * Returns true iff the given Object reference can be cast to Integer and that Integer's
     * intValue is 0.
     * @param ref the Integer (hopefully) reference
     * @return whether the given reference is both an Integer type and has intValue of 0
     */
    public static boolean getIntegerResponseAsBoolean(Object ref) {
        boolean isInteger = ref instanceof Integer;
        if (isInteger) {
            Integer i = (Integer) ref;
            return i.intValue() == 0;
        }
        return false;
    }

    public static AppletAction getIntegerResponseAsAppletAction(Object ref) {
        if (ref instanceof Integer) {
            return AppletAction.fromInteger((Integer) ref);
        }
        return AppletAction.CANCEL;
    }

    /**
     * Returns whether the current runtime configuration allows prompting user
     * for security warnings.
     *
     * @return true if security warnings should be shown to the user.
     */
    private static boolean shouldPromptUser() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean >() {
            @Override
            public Boolean run() {
                return Boolean.valueOf(JNLPRuntime.getConfiguration()
                        .getProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER));
            }
        });
    }
    
}
