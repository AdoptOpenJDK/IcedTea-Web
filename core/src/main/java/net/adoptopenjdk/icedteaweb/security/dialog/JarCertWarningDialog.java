package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
public class JarCertWarningDialog extends CertWarningDialog {
    private static final Logger LOG = LoggerFactory.getLogger(JarCertWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> runButton;
    private final DialogButton<AccessWarningResult> sandboxButton;
    private final DialogButton<AccessWarningResult> cancelButton;
    private final JButton advancedOptionsButton;

    private final boolean rootInCaCerts;
    private final AccessType accessType;
    private final SecurityDelegate securityDelegate;
    private final JNLPFile file;
    private final Certificate certificate;
    private boolean alwaysTrustSelected;

    protected JarCertWarningDialog(final String message, final AccessType accessType, final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        super(message, file,certVerifier, accessType == AccessType.VERIFIED);
        this.file = file;
        this.certificate = certVerifier.getPublisher(null);
        this.accessType = accessType;
        this.securityDelegate = securityDelegate;
        this.alwaysTrustSelected = (accessType == AccessType.VERIFIED);
        this.rootInCaCerts = certVerifier.getRootInCaCerts();

        runButton = ButtonFactory.createRunButton(() -> AccessWarningResult.YES);
        sandboxButton = ButtonFactory.createSandboxButton(() -> AccessWarningResult.SANDBOX);
        sandboxButton.setEnabled(!alwaysTrustSelected);
        cancelButton = ButtonFactory.createCancelButton(TRANSLATOR.translate("CertWarnCancelTip"), () -> AccessWarningResult.NO);
        advancedOptionsButton = createAdvancedOptionsButton();
    }

    public static JarCertWarningDialog create(final AccessType accessType, final JNLPFile jnlpFile, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {

        final String message = getMessageFor(accessType);
        return new JarCertWarningDialog(message, accessType, jnlpFile, certVerifier, securityDelegate);
    }

    @Override
    protected ImageIcon createIcon() {
        if (alwaysTrustSelected) {
            return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/question.png");
        }
        return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning.png");
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {

            gridBuilder.addRows(getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();
            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(createMoreInformationPanel());

            gridBuilder.addComponentRow(createAlwaysTrustCheckbox());

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return gridBuilder.createGrid();
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
        JCheckBox alwaysTrustCheckBox = super.createAlwaysTrustCheckbox();
        alwaysTrustCheckBox.addActionListener(e -> sandboxButton.setEnabled(alwaysTrustSelected = !alwaysTrustCheckBox.isSelected()));
        return alwaysTrustCheckBox;
    }

    protected String getMoreInformationText() {
        String moreInformationText = rootInCaCerts ?
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
