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
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.CertificateDetailsPanel;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;
import net.sourceforge.jnlp.JNLPFile;

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
import java.awt.GridLayout;
import java.security.cert.Certificate;
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

    private final List<String> details;
    private List<? extends Certificate> certificates;

    CertWarningDetailsDialog(final Dialog owner, final JNLPFile file, final List<? extends Certificate> certificates, final List<String> certIssues) {
        super(owner);
        this.certificates = certificates;
        details = new ArrayList<>(certIssues);

        // TODO remove this after debugging
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras lacinia tortor nec sem laoreet consectetur. ");
        details.add("Vivamus ac faucibus erat, quis placerat dolor. Quisque tincidunt vel orci ut accumsan.");
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras consectetur adipiscing lacinia consectetur. Vivamus ac faucibus erat, quis placerat dolor. Quisque tincidunt vel consectetur adipiscing orci ut accumsan.");
        details.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras lacinia tortor nec sem laoreet consectetur. ");

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
        return copyToClipboard;
    }

    private JComponent createDetailPaneContent() {
        int numLabels = details.size();
        JPanel errorPanel = new JPanel(new GridLayout(numLabels, 1, 0, 10));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (String detail : details) {
            ImageIcon icon = null;
            if (detail.equals(TRANSLATOR.translate("STrustedCertificate"))) {
                icon = ImageGallery.INFO_SMALL.asImageIcon();
            } else {
                icon = ImageGallery.WARNING_SMALL.asImageIcon();
            }

            final JLabel imageLabel = new JLabel(htmlWrap(detail), icon, SwingConstants.LEFT);
            imageLabel.setIconTextGap(10);
            errorPanel.add(imageLabel);
        }
        return errorPanel;
    }

    @Override
    protected JPanel createContentPane() {
        final JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(createDetailPaneContent(), BorderLayout.NORTH);
        contentPanel.add(createCertificateDetailsCollapsiblePanel(), BorderLayout.CENTER);
        contentPanel.add(createActionButtons(), BorderLayout.SOUTH);
        return contentPanel;
    }

    private JPanel createCertificateDetailsCollapsiblePanel() {
        final JPanel collapsiblePanel = new JPanel(new BorderLayout());
        collapsiblePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JButton showCertificateDetailsButton = createShowCertificateDetailsButton();
        collapsiblePanel.add(showCertificateDetailsButton, BorderLayout.NORTH);

        certificateDetailsPanel = new CertificateDetailsPanel(certificates);
        certificateDetailsPanel.setVisible(false);

        showCertificateDetailsButton.addActionListener(e -> {
            certificateDetailsPanel.setVisible(!certificateDetailsPanel.isVisible());
            pack();
        });

        collapsiblePanel.add(certificateDetailsPanel, BorderLayout.CENTER);
        return collapsiblePanel;
    }

    private JPanel createActionButtons() {
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        buttonPanel.add(createCopyToClipboardButton());

        final List<DialogButton<Void>> buttons = createButtons();
        buttons.forEach(b -> {
            final JButton button = b.createButton(this::close);
            buttonPanel.add(button);
        });

        return buttonPanel;
    }
}
