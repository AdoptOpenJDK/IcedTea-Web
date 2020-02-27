package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

abstract class CertWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final CertVerifier certVerifier;
    private final JNLPFile file;
    private boolean initiallyAlwaysTrustedSelected;

    protected CertWarningDialog(final String message, final JNLPFile file, final CertVerifier certVerifier, boolean initiallyAlwaysTrustedSelected) {
        super(message);
        this.file = file;
        this.certVerifier = certVerifier;
        this.initiallyAlwaysTrustedSelected = initiallyAlwaysTrustedSelected;
    }

    @Override
    public String createTitle() {
        return TRANSLATOR.translate(initiallyAlwaysTrustedSelected ? "SSecurityApprovalRequired" : "SSecurityWarning");
    }

    protected JCheckBox createAlwaysTrustCheckbox() {
        JCheckBox alwaysTrustCheckBox = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrustCheckBox.setEnabled(true);
        alwaysTrustCheckBox.setSelected(initiallyAlwaysTrustedSelected);
        return alwaysTrustCheckBox;
    }

    protected JPanel createMoreInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        final JTextArea moreInfoTextArea = new JTextArea(getMoreInformationText());
        moreInfoTextArea.setBackground(getBackground());
        moreInfoTextArea.setWrapStyleWord(true);
        moreInfoTextArea.setLineWrap(true);
        moreInfoTextArea.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(moreInfoTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBar(null);

        panel.add(scrollPane, BorderLayout.CENTER);
        JButton moreInfoButton = new JButton(TRANSLATOR.translate("ButMoreInformation"));
        // TODO use Dialogs here?
        moreInfoButton.addActionListener((e) -> new NewDialogFactory().showMoreInfoDialog(certVerifier, file));
        JPanel alignHelperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        alignHelperPanel.add(moreInfoButton);
        panel.add(alignHelperPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(720, 110));
        return panel;
    }

    protected abstract String getMoreInformationText();
}
