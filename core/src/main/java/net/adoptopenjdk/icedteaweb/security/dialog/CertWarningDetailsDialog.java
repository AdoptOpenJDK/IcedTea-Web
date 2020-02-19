// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.CertificateDetailsPanel;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.security.cert.CertPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

public class CertWarningDetailsDialog extends DialogWithResult<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(CertWarningDetailsDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private CertificateDetailsPanel certificateDetailsPanel;
    private final DialogButton<Void> closeButton;

    private final JNLPFile file;
    private final List<String> details;
    private CertPath certPath;

    CertWarningDetailsDialog(final Dialog owner, final JNLPFile file, final CertVerifier certVerifier) {
        super(owner);
        this.file = file;
        details = new ArrayList<>(certVerifier.getDetails(null));

        // TODO remove this after debugging
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras lacinia tortor nec sem laoreet consectetur. ");
        details.add("Vivamus ac faucibus erat, quis placerat dolor. Quisque tincidunt vel orci ut accumsan.");
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras consectetur adipiscing lacinia consectetur. Vivamus ac faucibus erat, quis placerat dolor. Quisque tincidunt vel consectetur adipiscing orci ut accumsan.");
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras lacinia tortor nec sem laoreet consectetur. ");


        certPath = certVerifier.getCertPath();

        // Show signed JNLP warning if the signed main jar does not have a
        // signed JNLP file and the launching JNLP file contains special properties
        if (file != null && file.requiresSignedJNLPWarning()) {
            details.add(TRANSLATOR.translate("SJNLPFileIsNotSigned"));
        }

        this.closeButton = ButtonFactory.createCloseButton(() -> null);
    }

    @Override
    protected String createTitle() {
        // TODO localization
        return "More Information";
    }

    private List<DialogButton<Void>> createButtons() {
        return Arrays.asList(closeButton);
    }

    private JButton createShowCertificateDetailsButton() {
        final JButton button = new JButton(TRANSLATOR.translate("SCertificateDetails"));
        return button;
    }

    private JButton createCopyToClipboardButton() {
        JButton copyToClipboard = new JButton(R("ButCopy"));
        copyToClipboard.addActionListener(e -> {
            certificateDetailsPanel.copyToClipboard();
        });
        return copyToClipboard;    }

    private JComponent createDetailPaneContent() {
        int numLabels = details.size();
        JPanel errorPanel = new JPanel(new GridLayout(numLabels, 1));
        errorPanel.setPreferredSize(new Dimension(600, 50 * (numLabels)));

        for (String detail : details) {
            ImageIcon icon = null;
            if (detail.equals(TRANSLATOR.translate("STrustedCertificate"))) {
                icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/info-small.png");
            } else {
                icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning-small.png");
            }

            errorPanel.add(new JLabel(htmlWrap(detail), icon, SwingConstants.LEFT));
        }
        return errorPanel;
    }

    @Override
    protected JPanel createContentPane() {
        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detailPanel.add(createDetailPaneContent());

        final JPanel collapsiblePanel = createCertificateDetailsCollapsiblePanel();

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        actionWrapperPanel.add(Box.createHorizontalGlue());

        actionWrapperPanel.add(createCopyToClipboardButton());

        final List<DialogButton<Void>> buttons = createButtons();
        buttons.forEach(b -> {
            final JButton button = b.createButton(this::close);
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.add(detailPanel, BorderLayout.NORTH);
        contentPanel.add(collapsiblePanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }

    private JPanel createCertificateDetailsCollapsiblePanel() {
        final JPanel collapsiblePanel = new JPanel(new BorderLayout());

        final JButton showCertificateDetailsButton = createShowCertificateDetailsButton();
        collapsiblePanel.add(showCertificateDetailsButton, BorderLayout.NORTH);

        certificateDetailsPanel = new CertificateDetailsPanel(certPath);
        certificateDetailsPanel.setVisible(false);

        showCertificateDetailsButton.addActionListener(e-> {
            certificateDetailsPanel.setVisible(!certificateDetailsPanel.isVisible());
            this.pack();
        });

        collapsiblePanel.add(certificateDetailsPanel, BorderLayout.CENTER);
        return collapsiblePanel;
    }
}
