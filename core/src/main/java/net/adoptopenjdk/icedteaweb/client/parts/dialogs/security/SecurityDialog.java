/* SecurityDialog.java
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

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogType;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.AppTrustWarningDialog;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;

import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * Provides methods for showing security warning dialogs for a wide range of
 * JNLP security issues. Note that the security dialogs should be running in the
 * secure AppContext - this class should not be used directly from an applet or
 * application. See {@link Dialogs} for a way to show security dialogs.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class SecurityDialog {

    /**
     * The type of dialog we want to show
     */
    private final DialogType dialogType;

    /**
     * The type of access that this dialog is for
     */
    private final AccessType accessType;

    private SecurityDialogPanel panel;

    /**
     * The application file associated with this security warning
     */
    private final JNLPFile file;

    private final CertVerifier certVerifier;

    private final X509Certificate cert;

    /**
     * An optional String array that's only necessary when a dialog
     * label requires some parameters (e.g. showing which address an application
     * is trying to connect to).
     */
    private final Object[] extras;

    private DialogResult value;

    private final ViewableDialog viewableDialog;

    /**
     * Should show signed JNLP file warning
     */
    private boolean requiresSignedJNLPWarning;

    /**
     * Create a SecurityDialog to display a certificate-related warning
     */
    public SecurityDialog(DialogType dialogType, AccessType accessType,
                   JNLPFile file, CertVerifier certVerifier) {
        this(dialogType, accessType, file, certVerifier, null, null);
    }

    /**
     * Create a SecurityWarningDialog to display information about a single
     * certificate
     */
    public SecurityDialog(DialogType dialogType, X509Certificate c) {
        this(dialogType, null, null, null, c, null);
    }

    SecurityDialog(DialogType dialogType, AccessType accessType,
                   JNLPFile file, CertVerifier JarCertVerifier, X509Certificate cert, Object[] extras) {
        this.viewableDialog = new ViewableDialog();
        this.dialogType = dialogType;
        this.accessType = accessType;
        this.file = file;
        this.certVerifier = JarCertVerifier;
        this.cert = cert;
        this.extras = extras;

        if (file != null) {
            requiresSignedJNLPWarning = file.requiresSignedJNLPWarning();
        }

        String dialogTitle = createTitle(dialogType, accessType);

        // Note: ViewableDialog methods are deferred until show():
        getViewableDialog().setTitle(dialogTitle);
        getViewableDialog().setModalityType(ModalityType.MODELESS);

        getViewableDialog().setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize panel now as its constructor may call getViewableDialog() deferred methods
        // to modify dialog state:
        SwingUtils.invokeAndWait(() -> {
            panel = getPanel(SecurityDialog.this);
            getViewableDialog().add(panel, BorderLayout.CENTER);
        });

        getViewableDialog().pack();
        getViewableDialog().centerDialog();

        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    if (panel != null) {
                        panel.requestFocusOnDefaultButton();
                    }
                    gotFocus = true;
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
                getViewableDialog().setResizable(true);
                SecurityDialog.this.setValue(null);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // called if the user closes the window directly (dispose on close)
                // always dispose() to unlock message processing
                getViewableDialog().dispose();
            }
        };
        getViewableDialog().addWindowListener(adapter);
        getViewableDialog().addWindowFocusListener(adapter);
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public JNLPFile getFile() {
        return file;
    }

    public CertVerifier getCertVerifier() {
        return certVerifier;
    }

    public X509Certificate getCert() {
        return cert;
    }

    private static String createTitle(DialogType dtype, AccessType atype) {
        String dialogTitle = "";
        if (dtype == DialogType.CERT_WARNING) {
            if (atype == AccessType.VERIFIED)
                dialogTitle = "Security Approval Required";
            else
                dialogTitle = "Security Warning";
        } else if (dtype == DialogType.MORE_INFO)
            dialogTitle = "More Information";
        else if (dtype == DialogType.CERT_INFO)
            dialogTitle = "Details - Certificate";
        else if (dtype == DialogType.ACCESS_WARNING)
            dialogTitle = "Security Warning";
        else if (dtype == DialogType.APPLET_WARNING)
            dialogTitle = "Applet Warning";
        else if (dtype == DialogType.PARTIALLY_SIGNED_WARNING)
            dialogTitle = "Security Warning";
        else if (dtype == DialogType.AUTHENTICATION)
            dialogTitle = "Authentication Required";
        return dialogTitle;
    }

    private static SecurityDialogPanel getPanel(SecurityDialog sd) {
        final DialogType type = sd.dialogType;
        if (type == DialogType.CERT_WARNING) {
            return new CertWarningPane(sd, sd.certVerifier, (SecurityDelegate) sd.extras[0]);
        }
        if (type == DialogType.MORE_INFO) {
            return new MoreInfoPane(sd, sd.certVerifier);
        }
        if (type == DialogType.CERT_INFO) {
            return new CertsInfoPane(sd, sd.certVerifier);
        }
        if (type == DialogType.SINGLE_CERT_INFO) {
            return new SingleCertInfoPane(sd, sd.certVerifier);
        }
        if (type == DialogType.ACCESS_WARNING) {
            return new AccessWarningPane(sd, sd.extras, sd.certVerifier);
        }
        if (type == DialogType.APPLET_WARNING) {
            return new AppletWarningPane(sd, sd.certVerifier);
        }
        if (type == DialogType.PARTIALLY_SIGNED_WARNING) {
            return AppTrustWarningDialog.partiallySigned(sd, sd.file, (SecurityDelegate) sd.extras[0]);
        }
        if (type == DialogType.UNSIGNED_WARNING) {
            return AppTrustWarningDialog.unsigned(sd, sd.file); // Only necessary for applets on 'high security' or above
        }
        if (type == DialogType.AUTHENTICATION) {
            return new PasswordAuthenticationPane(sd, sd.extras);
        }
        if (type == DialogType.UNSIGNED_EAS_NO_PERMISSIONS_WARNING) {
            final String codeBase = sd.file.getNotNullProbableCodeBase().toExternalForm();
            return new MissingPermissionsAttributePanel(sd, sd.file.getTitle(), codeBase);
        }
        if (type == DialogType.MISSING_ALACA) {
            return new MissingALACAttributePanel(sd, sd.file.getTitle(), (String) sd.extras[0], (String) sd.extras[1]);
        }
        if (type == DialogType.MATCHING_ALACA) {
            return AppTrustWarningDialog.matchingAlaca(sd, sd.file, (String) sd.extras[0], (String) sd.extras[1]);
        }
        if (type == DialogType.SECURITY_511) {
            return new InetSecurity511Panel(sd, (URL) sd.extras[0]);
        }
        throw new RuntimeException("Unknown value of " + sd.dialogType + ". Panel will be null. That's not allowed.");
    }

    public void setValue(DialogResult value) {
        this.value = value;
    }

    public DialogResult getValue() {
        return value;
    }

    public boolean requiresSignedJNLPWarning() {
        return requiresSignedJNLPWarning;
    }

    public ViewableDialog getViewableDialog() {
        return viewableDialog;
    }

    public SecurityDialogPanel getSecurityDialogPanel() {
        return panel;
    }
}
