package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.LayoutPartsBuilder;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.AccessType;

import javax.swing.JComponent;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

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

    public static AccessWarningDialog create(final AccessType accessType, final JNLPFile jnlpFile, final Object[] extras) {
        return new AccessWarningDialog(jnlpFile, getMessageFor(accessType, extras));
    }

    private static String getMessageFor(final AccessType accessType, final Object[] extras) {

        switch (accessType) {
            case READ_WRITE_FILE:
                return TRANSLATOR.translate("SFileReadWriteAccess", filePath(extras));
            case READ_FILE:
                return TRANSLATOR.translate("SFileReadAccess", filePath(extras));
            case WRITE_FILE:
                return TRANSLATOR.translate("SFileWriteAccess", filePath(extras));
            case CLIPBOARD_READ:
                return TRANSLATOR.translate("SClipboardReadAccess");
            case CLIPBOARD_WRITE:
                return TRANSLATOR.translate("SClipboardWriteAccess");
            case PRINTER:
                return TRANSLATOR.translate("SPrinterAccess");
            case NETWORK:
                return TRANSLATOR.translate("SNetworkAccess", address(extras));
            default:
                return "";
        }
    }

    private static String filePath(Object[] extras) {
        return ofNullable(extras)
                .filter(nonNullExtras -> nonNullExtras.length > 0)
                .map(nonEmptyExtras -> nonEmptyExtras[0])
                .filter(firstObject -> firstObject instanceof String)
                .map(firstObject -> (String) firstObject)
                .map(FileUtils::displayablePath)
                .orElse(TRANSLATOR.translate("AFileOnTheMachine"));
    }

    private static Object address(Object[] extras) {
        return ofNullable(extras)
                .filter(nonNullExtras -> nonNullExtras.length > 0)
                .map(nonEmptyExtras -> nonEmptyExtras[0])
                .orElse("(address here)");
    }
}
