package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialogs.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialogs.results.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialogs.results.AllowDenyRememberResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.AccessType;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

public class AccessWarningDialog extends BasicSecurityDialog<AllowDenyRememberResult> {
    private static final Logger LOG = LoggerFactory.getLogger(AccessWarningDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<AllowDenyRememberResult> allowButton;
    private final DialogButton<AllowDenyRememberResult> denyButton;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    private AccessWarningDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        allowButton = ButtonFactory.createAllowButton(() -> new AllowDenyRememberResult(AllowDeny.ALLOW, rememberUserDecisionPanel.getResult()));
        denyButton = ButtonFactory.createDenyButton(() -> new AllowDenyRememberResult(AllowDeny.DENY, rememberUserDecisionPanel.getResult()));
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
            final String name = ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getTitle)
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("Name"), name, panel, 0);


            final String publisher = ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getVendor)
                    .map(v -> v + " " + TRANSLATOR.translate("SUnverified"))
                    .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
            addRow(TRANSLATOR.translate("Publisher"), publisher, panel, 1);


            final String fromFallback = ofNullable(file)
                    .map(JNLPFile::getSourceLocation)
                    .map(URL::getAuthority)
                    .orElse("");

            final String from = ofNullable(file)
                    .map(JNLPFile::getInformation)
                    .map(InformationDesc::getHomepage)
                    .map(URL::toString)
                    .map(i -> !StringUtils.isBlank(i) ? i : null)
                    .orElse(fromFallback);
            addRow(TRANSLATOR.translate("From"), from, panel, 2);

            addSeparatorRow(false, panel, 3);

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            addRow(rememberUserDecisionPanel, panel, 4);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<AllowDenyRememberResult>> createButtons() {
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
