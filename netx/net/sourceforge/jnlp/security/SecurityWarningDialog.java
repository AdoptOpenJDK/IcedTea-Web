/* SecurityWarningDialog.java
   Copyright (C) 2008 Red Hat, Inc.

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

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;

import java.security.cert.X509Certificate;

/**
 * Provides methods for showing security warning dialogs
 * for a wide range of JNLP security issues.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class SecurityWarningDialog extends JDialog {

        /** Types of dialogs we can create */
        public static enum DialogType {
                CERT_WARNING,
                MORE_INFO,
                CERT_INFO,
                SINGLE_CERT_INFO,
                ACCESS_WARNING,
                NOTALLSIGNED_WARNING,
                APPLET_WARNING
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

        /** The type of dialog we want to show */
        private DialogType dialogType;

        /** The type of access that this dialog is for */
        private AccessType accessType;

        private SecurityDialogPanel panel;

        /** The application file associated with this security warning */
        private JNLPFile file;

        private CertVerifier certVerifier;

        private X509Certificate cert;

        /** An optional String array that's only necessary when a dialog
         * label requires some parameters (e.g. showing which address an application
         * is trying to connect to).
         */
        private Object[] extras;

        /** Whether or not this object has been fully initialized */
        private boolean initialized = false;

    /**
     * the return value of this dialog. result: 0 = Yes, 1 = No, 2 = Cancel,
     * null = Window closed.
     */
        private Object value;

        public SecurityWarningDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file) {
            super();
                this.dialogType = dialogType;
                this.accessType = accessType;
                this.file = file;
                this.certVerifier = null;
                initialized = true;
                initDialog();
        }

        public SecurityWarningDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, CertVerifier jarSigner) {
            super();
                this.dialogType = dialogType;
                this.accessType = accessType;
                this.file = file;
                this.certVerifier = jarSigner;
                initialized = true;
                initDialog();
        }

        public SecurityWarningDialog(DialogType dialogType, AccessType accessType,
                CertVerifier certVerifier) {
            super();
            this.dialogType = dialogType;
            this.accessType = accessType;
            this.file = null;
            this.certVerifier = certVerifier;
            initialized = true;
            initDialog();
        }

        public SecurityWarningDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, Object[] extras) {
            super();
                this.dialogType = dialogType;
                this.accessType = accessType;
                this.file = file;
                this.certVerifier = null;
                initialized = true;
                this.extras = extras;
                initDialog();
        }

        //for displaying a single certificate
        public SecurityWarningDialog(DialogType dialogType, X509Certificate c) {
            super();
                this.dialogType = dialogType;
                this.accessType = null;
                this.file = null;
                this.certVerifier = null;
                this.cert = c;
                initialized = true;
                initDialog();
        }

        /**
         * Returns if this dialog has been fully initialized yet.
         * @return true if this dialog has been initialized, and false otherwise.
         */
        public boolean isInitialized(){
                return initialized;
        }

        /**
         * Shows a warning dialog for different types of system access (i.e. file
         * open/save, clipboard read/write, printing, etc).
         *
         * @param accessType the type of system access requested.
         * @param file the jnlp file associated with the requesting application.
         * @return true if permission was granted by the user, false otherwise.
         */
        public static boolean showAccessWarningDialog(AccessType accessType,
                JNLPFile file) {
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
        public static boolean showAccessWarningDialog(AccessType accessType,
                        JNLPFile file, Object[] extras) {
                        SecurityWarningDialog dialog = new SecurityWarningDialog(
                                        DialogType.ACCESS_WARNING, accessType, file, extras);
                        dialog.setVisible(true);
                        dialog.dispose();

                        Object selectedValue = dialog.getValue();
                        if (selectedValue == null) {
                                return false;
                        } else if (selectedValue instanceof Integer) {
                                if (((Integer)selectedValue).intValue() == 0)
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
                        SecurityWarningDialog dialog = new SecurityWarningDialog(
                                        DialogType.NOTALLSIGNED_WARNING, AccessType.NOTALLSIGNED, file, (new Object[0]));
                        dialog.setVisible(true);
                        dialog.dispose();

                        Object selectedValue = dialog.getValue();
                        if (selectedValue == null) {
                                return false;
                        } else if (selectedValue instanceof Integer) {
                                if (((Integer)selectedValue).intValue() == 0)
                                        return true;
                                else
                                        return false;
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
                SecurityWarningDialog dialog =
                        new SecurityWarningDialog(DialogType.CERT_WARNING, accessType, file,
                        jarSigner);
                dialog.setVisible(true);
                dialog.dispose();

                Object selectedValue = dialog.getValue();
                if (selectedValue == null) {
                        return false;
                } else if (selectedValue instanceof Integer) {
                        if (((Integer)selectedValue).intValue() == 0)
                                return true;
                        else
                                return false;
                } else {
                        return false;
                }
        }

        /**
         * Shows more information regarding jar code signing
         *
         * @param jarSigner the JarSigner used to verify this application
         * @param parent the parent option pane
         */
        public static void showMoreInfoDialog(
                CertVerifier jarSigner, SecurityWarningDialog parent) {

                SecurityWarningDialog dialog =
                        new SecurityWarningDialog(DialogType.MORE_INFO, null, null,
                        jarSigner);
                dialog.setVisible(true);
                dialog.dispose();
        }

        /**
         * Displays CertPath information in a readable table format.
         *
         * @param certs the certificates used in signing.
         */
        public static void showCertInfoDialog(CertVerifier jarSigner,
                SecurityWarningDialog parent) {
                SecurityWarningDialog dialog = new SecurityWarningDialog(DialogType.CERT_INFO,
                        null, null, jarSigner);
                dialog.setLocationRelativeTo(parent);
                dialog.setVisible(true);
                dialog.dispose();
        }

        /**
         * Displays a single certificate's information.
         *
         * @param c
         * @param optionPane
         */
        public static void showSingleCertInfoDialog(X509Certificate c,
                        JDialog parent) {
                SecurityWarningDialog dialog = new SecurityWarningDialog(DialogType.SINGLE_CERT_INFO, c);
                        dialog.setLocationRelativeTo(parent);
                        dialog.setVisible(true);
                        dialog.dispose();
        }

        public static int showAppletWarning() {
                SecurityWarningDialog dialog = new SecurityWarningDialog(DialogType.APPLET_WARNING,
                        null, null, (CertVerifier) null);
                dialog.setVisible(true);
                dialog.dispose();

                Object selectedValue = dialog.getValue();

                //result 0 = Yes, 1 = No, 2 = Cancel
                if (selectedValue == null) {
                        return 2;
                } else if (selectedValue instanceof Integer) {
                        return ((Integer)selectedValue).intValue();
                } else {
                        return 2;
                }
        }

        private void initDialog() {
            setSystemLookAndFeel();

                String dialogTitle = "";
                if (dialogType == DialogType.CERT_WARNING)
                        dialogTitle = "Warning - Security";
                else if (dialogType == DialogType.MORE_INFO)
                        dialogTitle = "More Information";
                else if (dialogType == DialogType.CERT_INFO)
                        dialogTitle = "Details - Certificate";
                else if (dialogType == DialogType.ACCESS_WARNING)
                        dialogTitle = "Security Warning";
                else if (dialogType == DialogType.APPLET_WARNING)
                        dialogTitle = "Applet Warning";
                else if (dialogType == DialogType.NOTALLSIGNED_WARNING)
                        dialogTitle = "Security Warning";

                setTitle(dialogTitle);
                setModal(true);

                setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                installPanel();

                pack();

                WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;
            @Override
            public void windowClosing(WindowEvent we) {
                setValue(null);
            }
            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    selectDefaultButton();
                    gotFocus = true;
                }
            }
            @Override
            public void windowOpened(WindowEvent e) {
                if (e.getSource() instanceof SecurityWarningDialog) {
                    SecurityWarningDialog dialog = (SecurityWarningDialog) e.getSource();
                    dialog.setResizable(true);
                    centerDialog(dialog);
                    dialog.setValue(null);
                }
            }
        };
                addWindowListener(adapter);
                addWindowFocusListener(adapter);

        }

        public AccessType getAccessType() {
                return accessType;
        }

        public JNLPFile getFile() {
                return file;
        }

        public CertVerifier getJarSigner() {
                return certVerifier;
        }

        public X509Certificate getCert() {
                return cert;
        }

        /**
         * Adds the appropriate JPanel to this Dialog, based on {@link DialogType}.
         */
        private void installPanel() {

                if (dialogType == DialogType.CERT_WARNING)
                        panel = new CertWarningPane(this, this.certVerifier);
                else if (dialogType == DialogType.MORE_INFO)
                        panel = new MoreInfoPane(this, this.certVerifier);
                else if (dialogType == DialogType.CERT_INFO)
                        panel = new CertsInfoPane(this, this.certVerifier);
                else if (dialogType == DialogType.SINGLE_CERT_INFO)
                        panel = new SingleCertInfoPane(this, this.certVerifier);
                else if (dialogType == DialogType.ACCESS_WARNING)
                        panel = new AccessWarningPane(this, extras, this.certVerifier);
                else if (dialogType == DialogType.APPLET_WARNING)
                        panel = new AppletWarningPane(this, this.certVerifier);
                else if (dialogType == DialogType.NOTALLSIGNED_WARNING)
                        panel = new NotAllSignedWarningPane(this);

                add(panel, BorderLayout.CENTER);
        }

        private static void centerDialog(JDialog dialog) {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension dialogSize = dialog.getSize();

                dialog.setLocation((screen.width - dialogSize.width)/2,
                        (screen.height - dialogSize.height)/2);
        }

    private void selectDefaultButton() {
        if (panel == null) {
            System.out.println("initial value panel is null");
        }
        panel.requestFocusOnDefaultButton();
    }

    protected void setValue(Object value) {
        if (JNLPRuntime.isDebug()) {
            System.out.println("Setting value:" + value);
        }
        this.value = value;
    }

    protected Object getValue() {
        if (JNLPRuntime.isDebug()) {
            System.out.println("Returning value:" + value);
        }
        return value;
    }

    /**
     * Updates the look and feel of the window to be the system look and feel
     */
    protected void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //don't worry if we can't.
        }
    }
}
