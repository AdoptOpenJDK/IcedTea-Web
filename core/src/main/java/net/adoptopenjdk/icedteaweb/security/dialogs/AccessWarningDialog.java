package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.AccessType;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AccessWarningDialog extends BasicSecurityDialog<AccessWarningResult> {
    private static final Logger LOG = LoggerFactory.getLogger(AccessWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    DialogButton<AccessWarningResult> okButton;
    DialogButton<AccessWarningResult> cancelButton;

    private AccessWarningDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        okButton = ButtonFactory.createOkButton(() -> null);
        cancelButton = ButtonFactory.createCancelButton(() -> null);
    }

    @Override
    public String createTitle() {
        return "Security Warning";
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


            final String publisher = Optional.ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getVendor)
                    .map(v -> v + " " + TRANSLATOR.translate("SUnverified"))
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);


            final String fromFallback = Optional.ofNullable(file)
                    .map(JNLPFile::getSourceLocation)
                    .map(URL::getAuthority)
                    .orElse("");

            final String from = Optional.ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getHomepage)
                    .map(URL::toString)
                    .map(i -> !StringUtils.isBlank(i) ? i : null)
                    .orElse(fromFallback);
            addRow(TRANSLATOR.translate("From"), from, panel, 2);
        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AccessWarningResult>> createButtons() {
        return Arrays.asList(okButton, cancelButton);
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

    public static AccessWarningDialog create(final AccessType accessType, final JNLPFile jnlpFile, final Object[] extras) {
        return new AccessWarningDialog(jnlpFile, getMessageFor(accessType, extras));
    }

    private static String getMessageFor(final AccessType accessType, final Object[] extras) {
        switch (accessType) {
            case READ_WRITE_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileReadWriteAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileReadWriteAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case READ_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileReadAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileReadAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case WRITE_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileWriteAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileWriteAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case CREATE_DESKTOP_SHORTCUT:
                return TRANSLATOR.translate("SDesktopShortcut");
            case CLIPBOARD_READ:
                return TRANSLATOR.translate("SClipboardReadAccess");
            case CLIPBOARD_WRITE:
                return TRANSLATOR.translate("SClipboardWriteAccess");
            case PRINTER:
                return TRANSLATOR.translate("SPrinterAccess");
            case NETWORK:
                if (extras != null && extras.length >= 0) {
                    return TRANSLATOR.translate("SNetworkAccess", extras[0]);
                } else {
                    return TRANSLATOR.translate("SNetworkAccess", "(address here)");
                }
            default:
                return "";
        }
    }
}
