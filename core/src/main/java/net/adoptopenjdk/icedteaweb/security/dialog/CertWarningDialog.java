package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.security.cert.Certificate;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

abstract class CertWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final List<? extends Certificate> certificates;
    private final List<String> certIssues;
    private boolean initiallyAlwaysTrustedSelected;
    private final String moreInformationText;

    protected CertWarningDialog(final String message, final JNLPFile file, final List<? extends Certificate> certificates, final List<String> certIssues, boolean initiallyAlwaysTrustedSelected, final String moreInformationText) {
        super(message);
        this.file = file;
        this.certificates = certificates;
        this.certIssues = certIssues;
        this.initiallyAlwaysTrustedSelected = initiallyAlwaysTrustedSelected;
        this.moreInformationText = moreInformationText;
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
        final JTextArea moreInfoTextArea = new JTextArea(moreInformationText);
        moreInfoTextArea.setBackground(getBackground());
        moreInfoTextArea.setWrapStyleWord(true);
        moreInfoTextArea.setLineWrap(true);
        moreInfoTextArea.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(moreInfoTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBar(null);

        panel.add(scrollPane, BorderLayout.CENTER);
        JButton moreInfoButton = new JButton(TRANSLATOR.translate("ButMoreInformation"));

        moreInfoButton.addActionListener((e) -> DialogProvider.showMoreInfoDialog(certificates, certIssues, file));
        JPanel alignHelperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        alignHelperPanel.add(moreInfoButton);
        panel.add(alignHelperPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(720, 110));
        return panel;
    }
}
