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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CreateShortcutDialog extends BasicSecurityDialog<Optional<ShortcutResult>> {
    private static final Logger LOG = LoggerFactory.getLogger(CreateShortcutDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    DialogButton<Optional<ShortcutResult>> createButton;
    DialogButton<Optional<ShortcutResult>> cancelButton;
    private JCheckBox desktopCheckBox;
    private JCheckBox menuCheckBox;

    private CreateShortcutDialog(final JNLPFile file, final String message) {
        super(message);
        this.file = file;
        createButton = ButtonFactory.createCreateButton(() -> Optional.of(new ShortcutResult(AllowDeny.valueOf(desktopCheckBox), AllowDeny.valueOf(menuCheckBox), null)));
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

            desktopCheckBox = createDesktopCheckBox();
            addRow(desktopCheckBox, panel, 3);

            menuCheckBox = createMenuCheckBox();
            addRow(menuCheckBox, panel, 4);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for Access warning dialog!", e);
        }
        return panel;
    }

    @Override
    protected List<DialogButton<Optional<ShortcutResult>>> createButtons() {
        return Arrays.asList(createButton, cancelButton);
    }

    private void addRow(String key, String value, JPanel panel, int row) {
        final JLabel keyLabel = new JLabel(key + ":");
        keyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints keyLabelConstraints = new GridBagConstraints();
        keyLabelConstraints.gridx = 0;
        keyLabelConstraints.gridy = row;
        keyLabelConstraints.ipady = 8;
        keyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(keyLabel, keyLabelConstraints);

        final JPanel seperatorPanel = new JPanel();
        seperatorPanel.setSize(8, 0);
        GridBagConstraints seperatorPanelConstraints = new GridBagConstraints();
        keyLabelConstraints.gridx = 1;
        keyLabelConstraints.gridy = row;
        keyLabelConstraints.ipady = 8;
        keyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(seperatorPanel, seperatorPanelConstraints);

        final JLabel valueLabel = new JLabel(value);
        GridBagConstraints valueLabelConstraints = new GridBagConstraints();
        valueLabelConstraints.gridx = 2;
        valueLabelConstraints.gridy = row;
        valueLabelConstraints.ipady = 8;
        valueLabelConstraints.weightx = 1;
        valueLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, valueLabelConstraints);
    }

    protected void addRow(JComponent child, JPanel panel, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.ipady = 8;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(child, constraints);
    }
}
