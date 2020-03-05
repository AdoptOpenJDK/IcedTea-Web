package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.LayoutPartsBuilder;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.RememberUserDecisionPanel;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

/**
 *  <img src="doc-files/PartiallySignedWarningDialog.png"></img>
 */
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

    public static PartiallySignedWarningDialog create(final JNLPFile file) {
        return new PartiallySignedWarningDialog(file);
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("SPartiallySignedApplication");
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            gridBuilder.addRows(LayoutPartsBuilder.getApplicationDetails(file));
            gridBuilder.addHorizontalSpacer();

            gridBuilder.addComponentRow(createMoreInfoPanel());

            rememberUserDecisionPanel = new RememberUserDecisionPanel();
            gridBuilder.addComponentRow(rememberUserDecisionPanel);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for partially signed warning dialog!", e);
        }
        return gridBuilder.createGrid();
    }

    private JPanel createMoreInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        final JTextArea moreInfoTextArea = new JTextArea(TRANSLATOR.translate("SPartiallySignedDetail"));
        moreInfoTextArea.setBackground(getBackground());
        moreInfoTextArea.setWrapStyleWord(true);
        moreInfoTextArea.setLineWrap(true);
        moreInfoTextArea.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(moreInfoTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBar(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(720, 70));

        return panel;
    }

    @Override
    protected List<DialogButton<RememberableResult<AllowDenySandbox>>> createButtons() {
        return Arrays.asList(allowButton, sandboxButton, denyButton);
    }
}
