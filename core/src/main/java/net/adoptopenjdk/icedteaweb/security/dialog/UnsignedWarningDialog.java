package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

public class UnsignedWarningDialog extends BasicSecurityDialog<RememberableResult<AllowDeny>> {
    private static final Logger LOG = LoggerFactory.getLogger(UnsignedWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<RememberableResult<AllowDeny>> allowButton;
    private final DialogButton<RememberableResult<AllowDeny>> denyButton;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    private UnsignedWarningDialog(final JNLPFile file) {
        super(TRANSLATOR.translate("SUnsignedSummary"));
        this.file = file;
        allowButton = ButtonFactory.createAllowButton(() -> new RememberableResult<>(AllowDeny.ALLOW, rememberUserDecisionPanel.getResult()));
        denyButton = ButtonFactory.createDenyButton(() -> new RememberableResult<>(AllowDeny.DENY, rememberUserDecisionPanel.getResult()));
    }

    public static UnsignedWarningDialog create(final JNLPFile file) {
        return new UnsignedWarningDialog(file);
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("SUnsignedApplication");
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            final String name = ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getTitle)
                    .map(s -> s + " " + TRANSLATOR.translate("SUnverified"))
                    .orElse("");
            gridBuilder.addKeyValueRow(TRANSLATOR.translate("Name"), name);

            final String codebase = ofNullable(file)
                    .map(JNLPFile::getCodeBase)
                    .map(url -> url.toString())
                    .orElse("");
            gridBuilder.addKeyValueRow(TRANSLATOR.translate("Codebase"), codebase);

            final String sourceLocation = ofNullable(file)
                    .map(JNLPFile::getSourceLocation)
                    .map(url -> url.toString())
                    .orElse("");

            gridBuilder.addKeyValueRow(TRANSLATOR.translate("SourceLocation"), sourceLocation);

            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(new JLabel(htmlWrap(TRANSLATOR.translate("SUntrustedRecommendation"))));

            gridBuilder.addHorizontalSpacer();

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for unsigned warning dialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<RememberableResult<AllowDeny>>> createButtons() {
        return Arrays.asList(allowButton, denyButton);
    }
}
