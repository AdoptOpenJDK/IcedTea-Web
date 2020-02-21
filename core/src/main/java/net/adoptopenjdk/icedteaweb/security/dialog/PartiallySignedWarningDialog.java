package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.util.Arrays;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

public class PartiallySignedWarningDialog extends BasicSecurityDialog<RememberableResult<AllowDenySandbox>> {
    private static final Logger LOG = LoggerFactory.getLogger(PartiallySignedWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<RememberableResult<AllowDenySandbox>> allowButton;
    private final DialogButton<RememberableResult<AllowDenySandbox>> sandboxButton;
    private final DialogButton<RememberableResult<AllowDenySandbox>> denyButton;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    PartiallySignedWarningDialog(final JNLPFile file) {
        super(TRANSLATOR.translate("SUnsignedSummary"));
        this.file = file;
        allowButton = ButtonFactory.createAllowButton(() -> new RememberableResult<>(AllowDenySandbox.ALLOW, rememberUserDecisionPanel.getResult()));
        sandboxButton = ButtonFactory.createSandboxButton(() -> new RememberableResult<>(AllowDenySandbox.SANDBOX, rememberUserDecisionPanel.getResult()));
        denyButton = ButtonFactory.createDenyButton(() -> new RememberableResult<>(AllowDenySandbox.DENY, rememberUserDecisionPanel.getResult()));
    }
    @Override
    protected String createTitle() {
        // TODO localization
        return "Unsigned Application";
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(new JLabel(htmlWrap(TRANSLATOR.translate("SPartiallySignedDetail"))));

            gridBuilder.addHorizontalSpacer();

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for partially signed warning dialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<RememberableResult<AllowDenySandbox>>> createButtons() {
        return Arrays.asList(allowButton, sandboxButton, denyButton);
    }
}
