package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.NewDialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.signing.JarCertVerifier;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class CertWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private final static Logger LOG = LoggerFactory.getLogger(CertWarningDialog.class);
    private final static Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> runButton;
    private final DialogButton<AccessWarningResult> advancedButton;
    private final DialogButton<AccessWarningResult> sandboxButton;
    private final DialogButton<AccessWarningResult> cancelButton;

    private AccessType accessType;
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

        runButton = ButtonFactory.createRunButton(() -> null);
        advancedButton = ButtonFactory.createAdvancedOptionsButton(() -> null);
        sandboxButton = ButtonFactory.createSandboxButton(() -> null);
        cancelButton = ButtonFactory.createCancelButton(TRANSLATOR.translate("CertWarnCancelTip"), () -> null);

    }

    @Override
    public String getTitle() {
        // TODO localization
        return accessType == AccessType.VERIFIED ? "Security Approval Required" : "Security Warning";
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        try {
            final String name = Optional.ofNullable(file)
                    .map(f -> f.getInformation())
                    .map(i -> i.getTitle())
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
                    .map(f -> f.getInformation())
                    .map(i -> i.getHomepage())
                    .map(u -> u.toString())
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("From"), from, panel, 2);

            addAlwaysTrustCheckbox(panel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for CertWarningDialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(runButton, sandboxButton, advancedButton, cancelButton);
    }

    private void addAlwaysTrustCheckbox(JPanel panel) {
        JCheckBox alwaysTrustCheckBox = new JCheckBox(R("SAlwaysTrustPublisher"));
        alwaysTrustCheckBox.setEnabled(true);
        alwaysTrustCheckBox.setSelected(alwaysTrustSelected);
        alwaysTrustCheckBox.addActionListener(e -> sandboxButton.setEnabled(!alwaysTrustCheckBox.isSelected()));
        panel.add(alwaysTrustCheckBox);
    }

    protected void addRow(String key, String value, JPanel panel, int row) {
        final JLabel keyLabel = new JLabel(key + ":");
        keyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints keyLabelConstraints = new GridBagConstraints();
        keyLabelConstraints.gridx = 0;
        keyLabelConstraints.gridy = row;
        keyLabelConstraints.ipady = 8;
        keyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(keyLabel, keyLabelConstraints);

        final JPanel seperatorPanel = new JPanel();
        seperatorPanel.setSize(8, 0);
        GridBagConstraints seperatorPanelConstraints = new GridBagConstraints();
        keyLabelConstraints.gridx = 1;
        keyLabelConstraints.gridy = row;
        keyLabelConstraints.ipady = 8;
        keyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(seperatorPanel, seperatorPanelConstraints);

        final JLabel valueLabel = new JLabel(value);
        GridBagConstraints valueLabelConstraints = new GridBagConstraints();
        valueLabelConstraints.gridx = 2;
        valueLabelConstraints.gridy = row;
        valueLabelConstraints.ipady = 8;
        valueLabelConstraints.weightx = 1;
        valueLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, valueLabelConstraints);
    }

    public static CertWarningDialog create(final AccessType accessType, final JNLPFile jnlpFile, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {

        final String message = getMessageFor(accessType);
        return new CertWarningDialog(message, accessType, jnlpFile, certVerifier, securityDelegate);
    }

    private static String getMessageFor(final AccessType accessType) {
        switch (accessType) {
            case VERIFIED:
                return R("SSigVerified");
            case UNVERIFIED:
                return R("SSigUnverified");
            case SIGNING_ERROR:
                return R("SSignatureError");
            default:
                return "";
        }
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

    public static void main(String[] args) throws Exception {
        final JNLPFile file = new JNLPFileFactory().create(new URL("file:///Users/andreasehret/Desktop/version-check.jnlp"));
        final Object[] extras = {"extra item 1"};

        new NewDialogFactory().showCertWarningDialog(AccessType.VERIFIED, file, new JarCertVerifier(), null);
    }

}
