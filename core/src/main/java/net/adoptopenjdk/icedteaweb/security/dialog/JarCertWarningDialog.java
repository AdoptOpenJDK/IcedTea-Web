package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.LayoutPartsBuilder;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: check if advancedOptions (temporary permissions) should still be supported
 *
 * <img src="doc-files/JarCertWarningDialog.png"></img>
 */
public class JarCertWarningDialog extends CertWarningDialog {
    private static final Logger LOG = LoggerFactory.getLogger(JarCertWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<AccessWarningResult> runButton;
    private final DialogButton<AccessWarningResult> sandboxButton;
    private final DialogButton<AccessWarningResult> cancelButton;

    private final JNLPFile file;
    private boolean alwaysTrustSelected;

    protected JarCertWarningDialog(final String message, final JNLPFile file, final List<? extends Certificate> certificates, final List<String> certIssues, final String moreInformationText, final boolean alwaysTrustSelected) {
        super(message, file, certificates, certIssues, alwaysTrustSelected, moreInformationText);
        this.file = file;
        this.alwaysTrustSelected = alwaysTrustSelected;

        runButton = ButtonFactory.createRunButton(() -> AccessWarningResult.YES);
        sandboxButton = ButtonFactory.createSandboxButton(() -> AccessWarningResult.SANDBOX);
        sandboxButton.setEnabled(!alwaysTrustSelected);
        cancelButton = ButtonFactory.createCancelButton(TRANSLATOR.translate("CertWarnCancelTip"), () -> AccessWarningResult.NO);
    }

    public static JarCertWarningDialog create(final String message, final JNLPFile jnlpFile, final List<? extends Certificate> certificates, final List<String> certIssues, final String moreInformationText, final boolean alwaysTrustSelected) {
        return new JarCertWarningDialog(message, jnlpFile, certificates, certIssues, moreInformationText, alwaysTrustSelected);
    }

    @Override
    protected ImageIcon createIcon() {
        return alwaysTrustSelected ? ImageGallery.QUESTION.asImageIcon() : ImageGallery.WARNING.asImageIcon();
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(LayoutPartsBuilder.getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(createMoreInformationPanel());

            gridBuilder.addHorizontalSpacer();

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

    protected JCheckBox createAlwaysTrustCheckbox() {
        JCheckBox alwaysTrustCheckBox = super.createAlwaysTrustCheckbox();
        alwaysTrustCheckBox.addActionListener(e -> sandboxButton.setEnabled(alwaysTrustSelected = !alwaysTrustCheckBox.isSelected()));
        return alwaysTrustCheckBox;
    }
}
