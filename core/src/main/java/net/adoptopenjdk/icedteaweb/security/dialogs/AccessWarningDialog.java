package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.NewDialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.security.AccessType;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.Optional;

public class AccessWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private final static Logger LOG = LoggerFactory.getLogger(AccessWarningDialog.class);
    private final static Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;

    private AccessWarningDialog(final JNLPFile file, final String title, final String message, final DialogButton<AccessWarningResult>... buttons) {
        super(title, message, buttons);
        this.file = file;
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


            final String publisher = Optional.ofNullable(file)
                    .map(f -> f.getInformation())
                    .map(i -> i.getVendor())
                    .map(v -> v + " " + TRANSLATOR.translate("SUnverified"))
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);


            final String fromFallback = Optional.ofNullable(file)
                    .map(f -> f.getSourceLocation())
                    .map(s -> s.getAuthority())
                    .orElse("");

            final String from = Optional.ofNullable(file)
                    .map(f -> f.getInformation())
                    .map(i -> i.getHomepage())
                    .map(u -> u.toString())
                    .map(i -> !StringUtils.isBlank(i) ? i : null)
                    .orElse(fromFallback);
            addRow(TRANSLATOR.translate("From"), from, panel, 2);
        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    private void addRow(String key, String value, JPanel panel, int row) {
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

    public static AccessWarningDialog create(String title, String message, final JNLPFile jnlpFile, final Object item) {
        DialogButton<AccessWarningResult> okButton = BasicSecurityDialog.createOkButton(() -> null);
        DialogButton<AccessWarningResult> cancelButton = BasicSecurityDialog.createCancelButton(() -> null);
        return new AccessWarningDialog(jnlpFile, title, message, okButton, cancelButton);
    }

    public static void main(String[] args) throws Exception {
        final JNLPFile file = new JNLPFileFactory().create(new URL("file:///Users/andreasehret/Desktop/version-check.jnlp"));
        final Object[] extras = {"extra item 1"};

        new NewDialogFactory().showAccessWarningDialog(AccessType.NETWORK, file, extras);
    }
}
