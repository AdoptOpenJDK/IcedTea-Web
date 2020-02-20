package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

/**
 * TODO: advancedOptions button
 * TODO: CertificateUtils.saveCertificate logic after runButton is pressed when alwaysTrustSelected
 * TODO: bottomPanel of old CertWarningPane
 * <p>
 * Required input
 * - Current certificate path
 * - is root of current path in CA trust store
 * - list of issues with current certificate path
 */
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
        // TODO localization
        return initiallyAlwaysTrustedSelected ? "Security Approval Required" : "Security Warning";
    }

    protected JCheckBox createAlwaysTrustCheckbox() {
        JCheckBox alwaysTrustCheckBox = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrustCheckBox.setEnabled(true);
        alwaysTrustCheckBox.setSelected(initiallyAlwaysTrustedSelected);
        return alwaysTrustCheckBox;
    }

    protected JPanel createMoreInformationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        final String moreInformationText = getMoreInformationText();
        final JLabel moreInformationLabel = new JLabel(htmlWrap(moreInformationText));
        panel.add(moreInformationLabel);
        JButton moreInfoButton = new JButton(TRANSLATOR.translate("ButMoreInformation"));
        // TODO use Dialogs here?
        moreInfoButton.addActionListener((e) -> new NewDialogFactory().showMoreInfoDialog(certVerifier, file));
        panel.add(moreInfoButton);
        panel.setPreferredSize(new Dimension(600, 100));
        return panel;
    }

    protected abstract String getMoreInformationText();
}
