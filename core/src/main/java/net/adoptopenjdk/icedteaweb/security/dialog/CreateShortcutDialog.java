package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.MenuDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.CreateShortcutResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.valueOf;

/**
 * <img src="doc-files/CreateShortcutDialog.png"></img>
 */
public class CreateShortcutDialog extends BasicSecurityDialog<Optional<RememberableResult<CreateShortcutResult>>> {
    private static final Logger LOG = LoggerFactory.getLogger(CreateShortcutDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final DialogButton<Optional<RememberableResult<CreateShortcutResult>>> createButton;
    private final DialogButton<Optional<RememberableResult<CreateShortcutResult>>> cancelButton;
    private JCheckBox desktopCheckBox;
    private JCheckBox menuCheckBox;
    private RememberUserDecisionPanel rememberUserDecisionPanel;

    private CreateShortcutDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        createButton = ButtonFactory.createCreateButton(() -> {
            final CreateShortcutResult shortcutResult = new CreateShortcutResult(valueOf(desktopCheckBox), valueOf(menuCheckBox));
            return Optional.of(new RememberableResult<>(shortcutResult, rememberUserDecisionPanel.getResult()));
        });
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
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();

            desktopCheckBox = createDesktopCheckBox();
            gridBuilder.addComponentRow(desktopCheckBox);
            menuCheckBox = createMenuCheckBox();
            gridBuilder.addComponentRow(menuCheckBox);

            gridBuilder.addHorizontalSpacer();

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for create shortcut dialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<Optional<RememberableResult<CreateShortcutResult>>>> createButtons() {
        return Arrays.asList(createButton, cancelButton);
    }
}
