package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.NewDialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class HttpsCertTrustDialog extends CertWarningDialog {
    private final static Logger LOG = LoggerFactory.getLogger(HttpsCertTrustDialog.class);
    private final static Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> yesButton;
    private final DialogButton<AccessWarningResult> noButton;



    private HttpsCertTrustDialog(final String message, final AccessType accessType, final JNLPFile file, final HttpsCertVerifier certVerifier) {
        super(message, accessType, file, certVerifier, null);

        yesButton = ButtonFactory.createYesButton(() -> null);
        noButton = ButtonFactory.createNoButton(() -> null);
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
            }
            else {
                name = file.getInformation().getTitle();
            }
            addRow(TRANSLATOR.translate("Name"), name, panel, 0);
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(yesButton, noButton);
    }

    public static HttpsCertTrustDialog create(final AccessType accessType, final JNLPFile jnlpFile, final HttpsCertVerifier certVerifier) {
        final String message = TRANSLATOR.translate("SHttpsUnverified") + " " + TRANSLATOR.translate("Continue");
        return new HttpsCertTrustDialog(message, accessType, jnlpFile, certVerifier);
    }

    public static void main(String[] args) throws Exception {
        final JNLPFile file = new JNLPFileFactory().create(new URL("file:///Users/andreasehret/Desktop/version-check.jnlp"));
        final Object[] extras = {"extra item 1"};

        new NewDialogFactory().showCertWarningDialog(AccessType.UNVERIFIED, file, new HttpsCertVerifier(new X509Certificate[0], true, true, "hostname"), null);
    }
}
