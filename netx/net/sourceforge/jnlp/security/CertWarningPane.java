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

package net.sourceforge.jnlp.security;

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
import javax.swing.SwingConstants;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.KeyStores.Level;
import net.sourceforge.jnlp.security.KeyStores.Type;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * Provides the panel for using inside a SecurityDialog. These dialogs are
 * used to warn the user when either signed code (with or without signing
 * issues) is going to be run, or when service permission (file, clipboard,
 * printer, etc) is needed with unsigned code.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class CertWarningPane extends SecurityDialogPanel {

    JCheckBox alwaysTrust;
    CertVerifier certVerifier;

    public CertWarningPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
        this.certVerifier = certVerifier;
        addComponents();
    }

    /**
     * Creates the actual GUI components, and adds it to this panel
     */
    private void addComponents() {
        AccessType type = parent.getAccessType();
        JNLPFile file = parent.getFile();
        Certificate c = parent.getCertVerifier().getPublisher();

        String name = "";
        String publisher = "";
        String from = "";

        //We don't worry about exceptions when trying to fill in
        //these strings -- we just want to fill in as many as possible.
        try {
            if ((certVerifier instanceof HttpsCertVerifier) &&
                             (c instanceof X509Certificate))
                name = SecurityUtil.getCN(((X509Certificate) c)
                                        .getSubjectX500Principal().getName());
            else if (file instanceof PluginBridge)
                name = file.getTitle();
            else
                name = file.getInformation().getTitle();
        } catch (Exception e) {
        }

        try {
            if (c instanceof X509Certificate) {
                publisher = SecurityUtil.getCN(((X509Certificate) c)
                                        .getSubjectX500Principal().getName());
            }
        } catch (Exception e) {
        }

        try {
            if (file instanceof PluginBridge)
                from = file.getCodeBase().getHost();
            else
                from = file.getInformation().getHomepage().toString();
        } catch (Exception e) {
        }

        // Labels
        String topLabelText = "";
        String bottomLabelText = parent.getCertVerifier().getRootInCacerts() ?
                                 R("STrustedSource") : R("SUntrustedSource");
        String propertyName = "";
        String iconLocation = "net/sourceforge/jnlp/resources/";
        boolean alwaysTrustSelected = false;
        if (certVerifier instanceof HttpsCertVerifier) {
            // HTTPS certs that are verified do not prompt for a dialog.
            // @see VariableX509TrustManager#checkServerTrusted
            topLabelText = R("SHttpsUnverified") + " " + R("Continue");
            propertyName = "OptionPane.warningIcon";
            iconLocation += "warning.png";
        } else
            switch (type) {
                case VERIFIED:
                    topLabelText = R("SSigVerified");
                    propertyName = "OptionPane.informationIcon";
                    iconLocation += "question.png";
                    alwaysTrustSelected = true;
                    break;
                case UNVERIFIED:
                    topLabelText = R("SSigUnverified");
                    propertyName = "OptionPane.warningIcon";
                    iconLocation += "warning.png";
                    bottomLabelText += " " + R("SWarnFullPermissionsIgnorePolicy");
                    break;
                case SIGNING_ERROR:
                    topLabelText = R("SSignatureError");
                    propertyName = "OptionPane.warningIcon";
                    iconLocation += "warning.png";
                    bottomLabelText += " " + R("SWarnFullPermissionsIgnorePolicy");
                    break;
            }

        ImageIcon icon = new ImageIcon((new sun.misc.Launcher())
                                .getClassLoader().getResource(iconLocation));
        JLabel topLabel = new JLabel(htmlWrap(topLabelText), icon, SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                                Font.BOLD, 12));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(400, 75));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //application info
        JLabel nameLabel = new JLabel(R("Name") + ":   " + name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel publisherLabel = new JLabel(R("Publisher") + ": " + publisher);
        publisherLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel fromLabel = new JLabel(R("From") + ":   " + from);
        fromLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        alwaysTrust = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrust.setEnabled(true);
        alwaysTrust.setSelected(alwaysTrustSelected);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.add(nameLabel);
        infoPanel.add(publisherLabel);

        if (!(certVerifier instanceof HttpsCertVerifier))
            infoPanel.add(fromLabel);

        infoPanel.add(alwaysTrust);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton run = new JButton(R("ButRun"));
        JButton cancel = new JButton(R("ButCancel"));
        int buttonWidth = Math.max(run.getMinimumSize().width,
                        cancel.getMinimumSize().width);
        int buttonHeight = run.getMinimumSize().height;
        Dimension d = new Dimension(buttonWidth, buttonHeight);
        run.setPreferredSize(d);
        cancel.setPreferredSize(d);
        run.addActionListener(createSetValueListener(parent, 0));
        run.addActionListener(new CheckBoxListener());
        cancel.addActionListener(createSetValueListener(parent, 1));
        initialFocusComponent = cancel;
        buttonPanel.add(run);
        buttonPanel.add(cancel);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);

        JLabel bottomLabel = new JLabel(htmlWrap(bottomLabelText));;
        JButton moreInfo = new JButton(R("ButMoreInformation"));
        moreInfo.addActionListener(new MoreInfoButtonListener());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(bottomLabel);
        bottomPanel.add(moreInfo);
        bottomPanel.setPreferredSize(new Dimension(600, 100));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(bottomPanel);

    }

    private class MoreInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SecurityDialog.showMoreInfoDialog(parent.getCertVerifier(),
                                parent);
        }
    }

    /**
     * Updates the user's KeyStore of trusted Certificates.
     */
    private class CheckBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (alwaysTrust != null && alwaysTrust.isSelected()) {
                try {
                    KeyStore ks = KeyStores.getKeyStore(Level.USER, Type.CERTS);
                    X509Certificate c = (X509Certificate) parent.getCertVerifier().getPublisher();
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
                    if (JNLPRuntime.isDebug()) {
                        System.out.println("certificate is now permanently trusted");
                    }
                } catch (Exception ex) {
                    // TODO: Let NetX show a dialog here notifying user
                    // about being unable to add cert to keystore
                    ex.printStackTrace();
                }
            }
        }
    }

}
