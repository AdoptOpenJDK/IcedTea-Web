package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.ReferencesPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.util.Arrays;
import java.util.List;

public abstract class MissingAttributeDialog extends BasicSecurityDialog<RememberableResult<AllowDeny>> {
    private static final Logger LOG = LoggerFactory.getLogger(MissingAttributeDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<RememberableResult<AllowDeny>> cancelButton;
    private final DialogButton<RememberableResult<AllowDeny>> runButton;
    protected RememberUserDecisionPanel rememberUserDecisionPanel;

    protected JNLPFile file;

    protected MissingAttributeDialog(final String message, final JNLPFile file) {
        super(message);
        this.file = file;
        runButton = ButtonFactory.createRunButton(() -> new RememberableResult<>(AllowDeny.ALLOW, rememberUserDecisionPanel.getResult()));
        cancelButton = ButtonFactory.createCancelButton(() -> new RememberableResult<>(AllowDeny.DENY, rememberUserDecisionPanel.getResult()));
    }

    @Override
    protected ImageIcon createIcon() {
        return ImageGallery.WARNING.asImageIcon();
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(getApplicationDetails(file));
            gridBuilder.addKeyValueRow(TRANSLATOR.translate("Codebase"), file.getNotNullProbableCodeBase().toString());

            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(createMoreInformationPanel());

            gridBuilder.addHorizontalSpacer();

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for MissingAttributeDialog!", e);
        }
        return gridBuilder.createGrid();
    }

    protected abstract ReferencesPanel createMoreInformationPanel();

    @Override
    protected List<DialogButton<RememberableResult<AllowDeny>>> createButtons() {
        return Arrays.asList(runButton, cancelButton);
    }
}
