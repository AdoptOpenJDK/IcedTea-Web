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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * A factory for showing many possible types of security warning to the user.<p>
 *
 * This contains all the public methods that classes outside this package should
 * use instead of using {@link SecurityDialog} directly.
 *
 * All of these methods post a message to the
 * {@link SecurityDialogMessageHandler} and block waiting for a response.
 */
public class SecurityDialogs {
    /** Types of dialogs we can create */
    public static enum DialogType {
        CERT_WARNING,
        MORE_INFO,
        CERT_INFO,
        SINGLE_CERT_INFO,
        ACCESS_WARNING,
        NOTALLSIGNED_WARNING,
        APPLET_WARNING,
        AUTHENTICATION,
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
        NOTALLSIGNED,
        SIGNING_ERROR
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

        if (selectedValue == null) {
            return false;
        } else if (selectedValue instanceof Integer) {
            if (((Integer) selectedValue).intValue() == 0)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    /**
     * Shows a warning dialog for when the main application jars are signed,
     * but extensions aren't
     *
     * @return true if permission was granted by the user, false otherwise.
     */
    public static boolean showNotAllSignedWarningDialog(JNLPFile file) {

        if (!shouldPromptUser()) {
            return false;
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.NOTALLSIGNED_WARNING;
        message.accessType = AccessType.NOTALLSIGNED;
        message.file = file;
        message.extras = new Object[0];

        Object selectedValue = getUserResponse(message);

        if (selectedValue == null) {
            return false;
        } else if (selectedValue instanceof Integer) {
            if (((Integer) selectedValue).intValue() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Shows a security warning dialog according to the specified type of
     * access. If <code>type</code> is one of AccessType.VERIFIED or
     * AccessType.UNVERIFIED, extra details will be available with regards
     * to code signing and signing certificates.
     *
     * @param accessType the type of warning dialog to show
     * @param file the JNLPFile associated with this warning
     * @param jarSigner the JarSigner used to verify this application
     */
    public static boolean showCertWarningDialog(AccessType accessType,
            JNLPFile file, CertVerifier jarSigner) {

        if (!shouldPromptUser()) {
            return false;
        }

        final SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.CERT_WARNING;
        message.accessType = accessType;
        message.file = file;
        message.certVerifier = jarSigner;

        Object selectedValue = getUserResponse(message);

        if (selectedValue == null) {
            return false;
        } else if (selectedValue instanceof Integer) {
            if (((Integer) selectedValue).intValue() == 0)
                return true;
            else
                return false;
        } else {
            return false;
        }
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
        if (response == null) {
            return null;
        } else {
            return (Object[]) response;
        }
    }

    /**
     * FIXME This is unused. Remove it?
     * @return (0, 1, 2) => (Yes, No, Cancel)
     */
    public static int showAppletWarning() {

        if (!shouldPromptUser()) {
            return 2;
        }

        SecurityDialogMessage message = new SecurityDialogMessage();
        message.dialogType = DialogType.APPLET_WARNING;

        Object selectedValue = getUserResponse(message);

        // result 0 = Yes, 1 = No, 2 = Cancel
        if (selectedValue == null) {
            return 2;
        } else if (selectedValue instanceof Integer) {
            return ((Integer) selectedValue).intValue();
        } else {
            return 2;
        }
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
