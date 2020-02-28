package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.security.auth.x500.X500Principal;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HttpsCertTrustDialog extends CertWarningDialog {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsCertTrustDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> yesButton;
    private final DialogButton<AccessWarningResult> noButton;
    private final JNLPFile file;
    private final Certificate certificate;
    private final boolean rootInCaCerts;


    private HttpsCertTrustDialog(final String message, final JNLPFile file, final Certificate certificate, final boolean rootInCaCerts, final List<? extends Certificate> certificates, final List<String> certIssues) {
        super(message, file, certificates, certIssues, false);
        this.file = file;
        this.certificate = certificate;
        this.rootInCaCerts = rootInCaCerts;

        this.yesButton = ButtonFactory.createYesButton(() -> AccessWarningResult.YES);
        this.noButton = ButtonFactory.createNoButton(() -> AccessWarningResult.NO);
    }

    public static HttpsCertTrustDialog create(final JNLPFile jnlpFile, final Certificate certificate, final boolean rootInCaCerts, final List<? extends Certificate> certificates, final List<String> certIssues) {
        final String message = TRANSLATOR.translate("SHttpsUnverified") + " " + TRANSLATOR.translate("Continue");
        return new HttpsCertTrustDialog(message, jnlpFile, certificate, rootInCaCerts, certificates, certIssues);
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            String name;
            String publisher = "";
            if (certificate instanceof X509Certificate) {
                name = SecurityUtil.getCN(Optional.ofNullable(certificate)
                        .map(cert -> (X509Certificate) certificate)
                        .map(X509Certificate::getSubjectX500Principal)
                        .map(X500Principal::getName)
                .orElse(""));
                publisher = name;
            } else {
                name = file.getInformation().getTitle();
            }
            gridBuilder.addKeyValueRow(TRANSLATOR.translate("Name"), name);
            gridBuilder.addKeyValueRow(TRANSLATOR.translate("Publisher"), publisher);

            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(createMoreInformationPanel());

            gridBuilder.addHorizontalSpacer();


            gridBuilder.addComponentRow(createAlwaysTrustCheckbox());

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for HttpsCertWarningDialog!", e);
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
        return ImageGallery.WARNING.asImageIcon();
    }
}
