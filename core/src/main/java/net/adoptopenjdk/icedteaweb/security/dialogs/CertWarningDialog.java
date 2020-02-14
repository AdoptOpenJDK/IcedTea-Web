package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

/**
 * TODO: advancedOptions button
 * TODO: CertificateUtils.saveCertificate logic after runButton is pressed when alwaysTrustSelected
 * TODO: bottomPanel of old CertWarningPane
 *
 *
 */
public class CertWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private static final Logger LOG = LoggerFactory.getLogger(CertWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> runButton;
    private final DialogButton<AccessWarningResult> sandboxButton;
    private final DialogButton<AccessWarningResult> cancelButton;
    private final JButton advancedOptionsButton;

    private final AccessType accessType;
    protected final CertVerifier certVerifier;
    protected final SecurityDelegate securityDelegate;
    protected final JNLPFile file;
    protected boolean alwaysTrustSelected;


    protected CertWarningDialog(final String message, final AccessType accessType, final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        super(message);
        this.file = file;
        this.accessType = accessType;
        this.certVerifier = certVerifier;
        this.securityDelegate = securityDelegate;
        this.alwaysTrustSelected = (accessType == AccessType.VERIFIED);

        runButton = ButtonFactory.createRunButton(() -> AccessWarningResult.YES);
        sandboxButton = ButtonFactory.createSandboxButton(() -> AccessWarningResult.SANDBOX);
        sandboxButton.setEnabled(!alwaysTrustSelected);
        cancelButton = ButtonFactory.createCancelButton(TRANSLATOR.translate("CertWarnCancelTip"), () -> AccessWarningResult.NO);
        advancedOptionsButton = createAdvancedOptionsButton();
    }

    public static CertWarningDialog create(final AccessType accessType, final JNLPFile jnlpFile, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {

        final String message = getMessageFor(accessType);
        return new CertWarningDialog(message, accessType, jnlpFile, certVerifier, securityDelegate);
    }

    @Override
    public String createTitle() {
        // TODO localization
        return accessType == AccessType.VERIFIED ? "Security Approval Required" : "Security Warning";
    }

    @Override
    protected ImageIcon createIcon() {
        switch (accessType) {
            case VERIFIED:
                return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/question.png");
            default:
                return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning.png");
        }
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        try {
            final String name = Optional.ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getTitle)
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("Name"), name, panel, 0);

            Certificate cert = certVerifier.getPublisher(null);
            String publisher = "";
            if (cert instanceof X509Certificate) {
                publisher = SecurityUtil.getCN(((X509Certificate) cert)
                        .getSubjectX500Principal().getName());
            }
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);

            final String from = Optional.ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getHomepage)
                    .map(URL::toString)
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("From"), from, panel, 2);

            addSeparatorRow(false, panel, 3);

            addRow(createAlwaysTrustCheckbox(), panel, 4);

            addRow(createMoreInformationPanel(), panel, 5);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(runButton, sandboxButton, cancelButton);
    }

    private JButton createAdvancedOptionsButton() {
        JButton advancedOptions = new JButton("\u2630");
        advancedOptions.setEnabled(file != null && securityDelegate != null);
        advancedOptions.setToolTipText(TRANSLATOR.translate("CertWarnPolicyTip"));
        return advancedOptions;
    }

    protected JCheckBox createAlwaysTrustCheckbox() {
        JCheckBox alwaysTrustCheckBox = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrustCheckBox.setEnabled(true);
        alwaysTrustCheckBox.setSelected(alwaysTrustSelected);
        alwaysTrustCheckBox.addActionListener(e -> sandboxButton.setEnabled(alwaysTrustSelected = !alwaysTrustCheckBox.isSelected()));
        return alwaysTrustCheckBox;
    }

    private JPanel createMoreInformationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        final String moreInformationText = getMoreInformationText(accessType, certVerifier);
        final JLabel moreInformationLabel = new JLabel(htmlWrap(moreInformationText));
        panel.add(moreInformationLabel);
        JButton moreInfoButton = new JButton(TRANSLATOR.translate("ButMoreInformation"));
        moreInfoButton.addActionListener((e) -> Dialogs.showMoreInfoDialog(certVerifier, file));
        panel.add(moreInfoButton);
        panel.setPreferredSize(new Dimension(600, 100));
        return panel;
    }

    protected String getMoreInformationText(final AccessType accessType, final CertVerifier certVerifier) {
        String moreInformationText = certVerifier.getRootInCaCerts() ?
                TRANSLATOR.translate("STrustedSource") : TRANSLATOR.translate("SUntrustedSource");

        switch (accessType) {
            case UNVERIFIED:
            case SIGNING_ERROR:
                return moreInformationText + " " + TRANSLATOR.translate("SWarnFullPermissionsIgnorePolicy");
            default:
                return moreInformationText;
        }
    }

    private static String getMessageFor(final AccessType accessType) {
        switch (accessType) {
            case VERIFIED:
                return TRANSLATOR.translate("SSigVerified");
            case UNVERIFIED:
                return TRANSLATOR.translate("SSigUnverified");
            case SIGNING_ERROR:
                return TRANSLATOR.translate("SSignatureError");
            default:
                return "";
        }
    }
}
