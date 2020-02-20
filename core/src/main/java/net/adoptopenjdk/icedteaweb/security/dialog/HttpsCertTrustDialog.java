package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class HttpsCertTrustDialog extends CertWarningDialog {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsCertTrustDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> yesButton;
    private final DialogButton<AccessWarningResult> noButton;
    private final JNLPFile file;
    private final Certificate certificate;
    private boolean rootInCaCerts;


    private HttpsCertTrustDialog(final String message, final JNLPFile file, final CertVerifier certVerifier) {
        super(message, file, certVerifier, false);
        this.file = file;
        this.certificate = certVerifier.getPublisher(null);
        this.rootInCaCerts = certVerifier.getRootInCaCerts();

        this.yesButton = ButtonFactory.createYesButton(() -> null);
        this.noButton = ButtonFactory.createNoButton(() -> null);
    }

    public static HttpsCertTrustDialog create(final JNLPFile jnlpFile, final CertVerifier certVerifier) {
        final String message = TRANSLATOR.translate("SHttpsUnverified") + " " + TRANSLATOR.translate("Continue");
        return new HttpsCertTrustDialog(message, jnlpFile, certVerifier);
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            String name;
            String publisher = "";
            if (certificate instanceof X509Certificate) {
                name = SecurityUtil.getCN(((X509Certificate) certificate).getSubjectX500Principal().getName());
                publisher = name;
            } else {
                name = file.getInformation().getTitle();
            }
            gridBuilder.addRow(TRANSLATOR.translate("Name"), name);
            gridBuilder.addRow(TRANSLATOR.translate("Publisher"), publisher);

            gridBuilder.addSeparatorRow(false);

            gridBuilder.addRow(createAlwaysTrustCheckbox());

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(yesButton, noButton);
    }

    @Override
    protected String getMoreInformationText() {
        return rootInCaCerts ? TRANSLATOR.translate("STrustedSource") : TRANSLATOR.translate("SUntrustedSource");
    }

    @Override
    protected ImageIcon createIcon() {
        return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning.png");
    }
}
