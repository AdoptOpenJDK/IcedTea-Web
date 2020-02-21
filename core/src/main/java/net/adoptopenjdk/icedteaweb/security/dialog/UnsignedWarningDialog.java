package net.adoptopenjdk.icedteaweb.security.dialog;

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
import javax.swing.JPanel;
import java.awt.GridBagLayout;
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

    UnsignedWarningDialog(final JNLPFile file) {
        super(TRANSLATOR.translate("SUnsignedSummary"));
        this.file = file;
        allowButton = ButtonFactory.createAllowButton(() -> new RememberableResult<>(AllowDeny.ALLOW, rememberUserDecisionPanel.getResult()));
        denyButton = ButtonFactory.createDenyButton(() -> new RememberableResult<>(AllowDeny.DENY, rememberUserDecisionPanel.getResult()));
    }
    @Override
    protected String createTitle() {
        // TODO localization
        return "Unsigned Application";
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        try {
            final String name = ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getTitle)
                    .orElse("");
            addRow(TRANSLATOR.translate("Name"), name, panel, 0);

            final String codebase = ofNullable(file)
                    .map(JNLPFile::getCodeBase)
                    .map(url -> url.toString())
                    .orElse("");
            addRow(TRANSLATOR.translate("Codebase"), codebase, panel, 1);

            final String sourceLocation = ofNullable(file)
                    .map(JNLPFile::getSourceLocation)
                    .map(url -> url.toString())
                    .orElse("");

            addRow(TRANSLATOR.translate("SourceLocation"), sourceLocation, panel, 2);

            addSeparatorRow(false, panel, 3);

            addRow(new JLabel(htmlWrap(TRANSLATOR.translate("<b>It is recommended you only run applications from sites you trust.</b>"))), panel, 4);

            addSeparatorRow(false, panel, 5);

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            addRow(rememberUserDecisionPanel, panel, 6);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<RememberableResult<AllowDeny>>> createButtons() {
        return Arrays.asList(allowButton, denyButton);
    }
}
