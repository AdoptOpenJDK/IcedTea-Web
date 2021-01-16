package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.LayoutPartsBuilder;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JComponent;
import java.util.Arrays;
import java.util.List;

/**
 * <img src="doc-files/AccessWarningDialog.png"></img>
 */
public class AccessWarningDialog extends BasicSecurityDialog<RememberableResult<AllowDeny>> {
    private static final Logger LOG = LoggerFactory.getLogger(AccessWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<RememberableResult<AllowDeny>> allowButton;
    private final DialogButton<RememberableResult<AllowDeny>> denyButton;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    private AccessWarningDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        allowButton = ButtonFactory.createAllowButton(() -> new RememberableResult<>(AllowDeny.ALLOW, rememberUserDecisionPanel.getResult()));
        denyButton = ButtonFactory.createDenyButton(() -> new RememberableResult<>(AllowDeny.DENY, rememberUserDecisionPanel.getResult()));
    }

    public static AccessWarningDialog create(final JNLPFile jnlpFile, final String message) {
        return new AccessWarningDialog(jnlpFile, message);
    }

    @Override
    public String createTitle() {
        return "Security Warning";
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(LayoutPartsBuilder.getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<RememberableResult<AllowDeny>>> createButtons() {
        return Arrays.asList(allowButton, denyButton);
    }
}
