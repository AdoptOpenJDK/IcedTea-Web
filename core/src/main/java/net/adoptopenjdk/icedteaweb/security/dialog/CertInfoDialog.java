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
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.CertificateDetailsPanel;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class CertInfoDialog extends DialogWithResult<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(CertInfoDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private CertificateDetailsPanel certificateDetailsPanel;
    private final DialogButton<Void> closeButton;

    private List<? extends Certificate> certificates;

    CertInfoDialog(final Dialog owner, final Certificate certificate) {
        super(owner);
        this.certificates = Collections.singletonList(certificate);
        this.closeButton = ButtonFactory.createCloseButton(() -> null);
    }

    CertInfoDialog(final Dialog owner, final List<? extends Certificate> certificates) {
        super(owner);
        this.certificates = certificates;
        this.closeButton = ButtonFactory.createCloseButton(() -> null);
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("SCertificateDetails");
    }

    private List<DialogButton<Void>> createButtons() {
        return Collections.singletonList(closeButton);
    }

    private JButton createCopyToClipboardButton() {
        JButton copyToClipboard = new JButton(R("ButCopy"));
        copyToClipboard.addActionListener(e -> {
            certificateDetailsPanel.copyToClipboard();
        });
        return copyToClipboard;
    }

    @Override
    protected JPanel createContentPane() {
        final JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.add(new CertificateDetailsPanel(certificates), BorderLayout.CENTER);
        contentPanel.add(createActionButtons(), BorderLayout.SOUTH);
        return contentPanel;
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
