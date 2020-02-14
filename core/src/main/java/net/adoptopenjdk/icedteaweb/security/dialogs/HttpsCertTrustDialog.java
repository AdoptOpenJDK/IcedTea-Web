package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class HttpsCertTrustDialog extends CertWarningDialog {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsCertTrustDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> yesButton;
    private final DialogButton<AccessWarningResult> noButton;


    private HttpsCertTrustDialog(final String message, final AccessType accessType, final JNLPFile file, final HttpsCertVerifier certVerifier) {
        super(message, accessType, file, certVerifier, null);
        this.alwaysTrustSelected = false;

        this.yesButton = ButtonFactory.createYesButton(() -> null);
        this.noButton = ButtonFactory.createNoButton(() -> null);
    }

    public static HttpsCertTrustDialog create(final AccessType accessType, final JNLPFile jnlpFile, final HttpsCertVerifier certVerifier) {
        final String message = TRANSLATOR.translate("SHttpsUnverified") + " " + TRANSLATOR.translate("Continue");
        return new HttpsCertTrustDialog(message, accessType, jnlpFile, certVerifier);
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        try {
            Certificate cert = certVerifier.getPublisher(null);
            String name;
            String publisher = "";
            if (cert instanceof X509Certificate) {
                name = SecurityUtil.getCN(((X509Certificate) cert).getSubjectX500Principal().getName());
                publisher = name;
            } else {
                name = file.getInformation().getTitle();
            }
            addRow(TRANSLATOR.translate("Name"), name, panel, 0);
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);

            addSeparatorRow(false, panel, 2);

            addRow(createAlwaysTrustCheckbox(), panel, 3);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(yesButton, noButton);
    }

    protected String getMoreInformationText(final AccessType accessType, final CertVerifier certVerifier) {
        return certVerifier.getRootInCaCerts() ? TRANSLATOR.translate("STrustedSource") : TRANSLATOR.translate("SUntrustedSource");
    }

    @Override
    protected ImageIcon createIcon() {
        return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning.png");
    }
}
