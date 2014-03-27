/* CertWarningPane.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.dialogs;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.CertificateUtils;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.security.KeyStores.Level;
import net.sourceforge.jnlp.security.KeyStores.Type;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.security.policyeditor.PolicyEditor.PolicyEditorWindow;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Provides the panel for using inside a SecurityDialog. These dialogs are
 * used to warn the user when either signed code (with or without signing
 * issues) is going to be run, or when service permission (file, clipboard,
 * printer, etc) is needed with unsigned code.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class CertWarningPane extends SecurityDialogPanel {

    private final JNLPFile file;
    private final AccessType accessType;
    private final Certificate cert;
    private JCheckBox alwaysTrust;
    private final CertVerifier certVerifier;
    private SecurityDelegate securityDelegate;
    private JPopupMenu policyMenu;
    private JPanel topPanel, infoPanel, buttonPanel, bottomPanel;
    private JLabel topLabel, nameLabel, publisherLabel, fromLabel, bottomLabel;
    private JButton run, sandbox, advancedOptions, cancel, moreInfo;
    private boolean alwaysTrustSelected;
    private String bottomLabelWarningText;
    private PolicyEditorWindow policyEditor = null;

    public CertWarningPane(SecurityDialog x, CertVerifier certVerifier, SecurityDelegate securityDelegate) {
        super(x, certVerifier);
        this.certVerifier = certVerifier;
        this.securityDelegate = securityDelegate;

        this.accessType = parent.getAccessType();
        this.file = parent.getFile();
        this.cert = parent.getCertVerifier().getPublisher(null);

        addComponents();
    }

    /**
     * Creates the actual GUI components, and adds it to this panel
     */
    private void addComponents() {
        setTextAndLabels();
        addButtons();
    }

    private void setTextAndLabels() {
        String name = "";
        String publisher = "";
        String from = "";

        //We don't worry about exceptions when trying to fill in
        //these strings -- we just want to fill in as many as possible.
        try {
            if ((certVerifier instanceof HttpsCertVerifier) &&
                    (cert instanceof X509Certificate)) {
                name = SecurityUtil.getCN(((X509Certificate) cert)
                                        .getSubjectX500Principal().getName());
            } else if (file instanceof PluginBridge) {
                name = file.getTitle();
            } else {
                name = file.getInformation().getTitle();
            }
        } catch (Exception e) {
        }

        try {
            if (cert instanceof X509Certificate) {
                publisher = SecurityUtil.getCN(((X509Certificate) cert)
                                        .getSubjectX500Principal().getName());
            }
        } catch (Exception e) {
        }

        try {
            if (file instanceof PluginBridge) {
                from = file.getCodeBase().getHost();
            } else {
                from = file.getInformation().getHomepage().toString();
            }
        } catch (Exception e) {
        }

        // Labels
        String topLabelText = "";
        bottomLabelWarningText = parent.getCertVerifier().getRootInCacerts() ?
                R("STrustedSource") : R("SUntrustedSource");
        String iconLocation = "net/sourceforge/jnlp/resources/";
        alwaysTrustSelected = false;
        if (certVerifier instanceof HttpsCertVerifier) {
            // HTTPS certs that are verified do not prompt for a dialog.
            // @see VariableX509TrustManager#checkServerTrusted
            topLabelText = R("SHttpsUnverified") + " " + R("Continue");
            iconLocation += "warning.png";
        } else {
            switch (accessType) {
                case VERIFIED:
                    topLabelText = R("SSigVerified");
                    iconLocation += "question.png";
                    alwaysTrustSelected = true;
                    break;
                case UNVERIFIED:
                    topLabelText = R("SSigUnverified");
                    iconLocation += "warning.png";
                    bottomLabelWarningText += " " + R("SWarnFullPermissionsIgnorePolicy");
                    break;
                case SIGNING_ERROR:
                    topLabelText = R("SSignatureError");
                    iconLocation += "warning.png";
                    bottomLabelWarningText += " " + R("SWarnFullPermissionsIgnorePolicy");
                    break;
            }
        }
        ImageIcon icon = getImageIcon(iconLocation);
        topLabel = new JLabel(htmlWrap(topLabelText), icon, SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                                Font.BOLD, 12));
        topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(400, 75));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //application info
        nameLabel = new JLabel(R("Name") + ":   " + name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        publisherLabel = new JLabel(R("Publisher") + ": " + publisher);
        publisherLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fromLabel = new JLabel(R("From") + ":   " + from);
        fromLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private ImageIcon getImageIcon(final String imageLocation) {
        return new ImageIcon((new sun.misc.Launcher())
                .getClassLoader().getResource(imageLocation));
    }

    private void addButtons() {
        alwaysTrust = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrust.setEnabled(true);
        alwaysTrust.setSelected(alwaysTrustSelected);

        infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.add(nameLabel);
        infoPanel.add(publisherLabel);

        if (!(certVerifier instanceof HttpsCertVerifier)) {
            infoPanel.add(fromLabel);
        }

        infoPanel.add(alwaysTrust);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        //run and cancel buttons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        run = new JButton(R("ButRun"));
        sandbox = new JButton(R("ButSandbox"));
        advancedOptions = new TemporaryPermissionsButton(file, securityDelegate, sandbox);
        cancel = new JButton(R("ButCancel"));

        run.setToolTipText(R("CertWarnRunTip"));
        sandbox.setToolTipText(R("CertWarnSandboxTip"));
        advancedOptions.setToolTipText(R("CertWarnPolicyTip"));
        cancel.setToolTipText(R("CertWarnCancelTip"));

        alwaysTrust.addActionListener(new ButtonDisableListener(sandbox));
        int buttonWidth = Math.max(run.getMinimumSize().width,
                sandbox.getMinimumSize().width);
        buttonWidth = Math.max(buttonWidth, cancel.getMinimumSize().width);
        int buttonHeight = run.getMinimumSize().height;
        Dimension d = new Dimension(buttonWidth, buttonHeight);

        run.setPreferredSize(d);
        sandbox.setPreferredSize(d);
        advancedOptions.setPreferredSize(new Dimension(advancedOptions.getMinimumSize().width, buttonHeight));
        cancel.setPreferredSize(d);

        sandbox.setEnabled(!alwaysTrust.isSelected());

        run.addActionListener(createSetValueListener(parent, 0));
        run.addActionListener(new CheckBoxListener());

        sandbox.addActionListener(createSetValueListener(parent, 1));

        cancel.addActionListener(createSetValueListener(parent, 2));

        initialFocusComponent = cancel;
        buttonPanel.add(run);
        // file will be null iff this dialog is being called from VariableX509TrustManager.
        // In this case, the "sandbox" button does not make any sense, as we are asking
        // the user if they trust some certificate that is not being used to sign an app.
        // Since there is no app, there is nothing to run sandboxed.
        if (file != null) {
            buttonPanel.add(sandbox);
            buttonPanel.add(advancedOptions);
        }
        buttonPanel.add(cancel);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);

        bottomLabel = new JLabel(htmlWrap(bottomLabelWarningText));
        moreInfo = new JButton(R("ButMoreInformation"));
        moreInfo.addActionListener(new MoreInfoButtonListener());

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(bottomLabel);
        bottomPanel.add(moreInfo);
        bottomPanel.setPreferredSize(new Dimension(600, 100));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(bottomPanel);
    }

    private class MoreInfoButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SecurityDialog.showMoreInfoDialog(parent.getCertVerifier(),
                                parent);
        }
    }

    /**
     * Disable the Sandbox button when the AlwaysTrust checkbox is checked
     */
    private class ButtonDisableListener implements ActionListener {
        private JButton button;

        public ButtonDisableListener(JButton button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            button.setEnabled(!alwaysTrust.isSelected());
        }
    }

    /**
     * Updates the user's KeyStore of trusted Certificates.
     */
    private class CheckBoxListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (alwaysTrust != null && alwaysTrust.isSelected()) {
                try {
                    KeyStore ks = KeyStores.getKeyStore(Level.USER, Type.CERTS);
                    X509Certificate c = (X509Certificate) parent.getCertVerifier().getPublisher(null);
                    CertificateUtils.addToKeyStore(c, ks);
                    File keyStoreFile = new File(KeyStores.getKeyStoreLocation(Level.USER, Type.CERTS));
                    if (!keyStoreFile.isFile()) {
                        FileUtils.createRestrictedFile(keyStoreFile, true);
                    }

                    OutputStream os = new FileOutputStream(keyStoreFile);
                    try {
                        ks.store(os, KeyStores.getPassword());
                    } finally {
                        os.close();
                    }
                    OutputController.getLogger().log("certificate is now permanently trusted");
                } catch (Exception ex) {
                    // TODO: Let NetX show a dialog here notifying user
                    // about being unable to add cert to keystore
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
                }
            }
        }
    }

}
