package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.MenuDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <img src="doc-files/CreateShortcutDialog.png"></img>
 */
public class CreateShortcutDialog extends BasicSecurityDialog<Optional<CreateShortcutResult>> {
    private static final Logger LOG = LoggerFactory.getLogger(CreateShortcutDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<Optional<CreateShortcutResult>> createButton;
    private final DialogButton<Optional<CreateShortcutResult>> cancelButton;
    private JCheckBox desktopCheckBox;
    private JCheckBox menuCheckBox;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    private CreateShortcutDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        createButton = ButtonFactory.createCreateButton(() -> Optional.of(new CreateShortcutResult(AllowDeny.valueOf(desktopCheckBox), AllowDeny.valueOf(menuCheckBox), rememberUserDecisionPanel.getResult())));
        cancelButton = ButtonFactory.createCancelButton(Optional::empty);
    }

    public static CreateShortcutDialog create(final JNLPFile jnlpFile) {
        return new CreateShortcutDialog(jnlpFile, TRANSLATOR.translate("SDesktopShortcut"));
    }

    private JCheckBox createDesktopCheckBox() {
        final Boolean applicationRequested = Optional.ofNullable(file.getInformation())
                .map(InformationDesc::getShortcut)
                .map(ShortcutDesc::onDesktop)
                .orElse(false);

        final String textKey = applicationRequested ? "EXAWdesktopWants" : "EXAWdesktopDontWants";

        return new JCheckBox(TRANSLATOR.translate(textKey), applicationRequested);
    }

    private JCheckBox createMenuCheckBox() {

        final Boolean includeInMenuRequested = Optional.ofNullable(file.getInformation())
                .map(InformationDesc::getShortcut)
                .map(ShortcutDesc::toMenu)
                .orElse(false);

        final Optional<String> subMenu = Optional.ofNullable(file.getInformation())
                .map(InformationDesc::getShortcut)
                .map(ShortcutDesc::getMenu)
                .map(MenuDesc::getSubMenu);

        if (includeInMenuRequested) {
            final String text;
            if (subMenu.isPresent()) {
                text = TRANSLATOR.translate("EXAWsubmenu", subMenu.get());
            } else {
                text = TRANSLATOR.translate("EXAWmenuWants");
            }
            return new JCheckBox(text, true);

        } else {
            return new JCheckBox(TRANSLATOR.translate("EXAWmenuDontWants"), false);
        }
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

            addSeparatorRow(false, panel, 3);

            desktopCheckBox = createDesktopCheckBox();
            addRow(desktopCheckBox, panel, 4);
            menuCheckBox = createMenuCheckBox();
            addRow(menuCheckBox, panel, 5);

            addSeparatorRow(false, panel, 6);

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            addRow(rememberUserDecisionPanel, panel, 7);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<Optional<CreateShortcutResult>>> createButtons() {
        return Arrays.asList(createButton, cancelButton);
    }
}
