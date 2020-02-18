package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

public class CertWarningDetailsDialog extends DialogWithResult<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(CertWarningDetailsDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final JNLPFile file;
    private final List<String> details;
    private final DialogButton<Void> closeButton;

    CertWarningDetailsDialog(final JNLPFile file, final CertVerifier certVerifier) {
        this.file = file;
        details = new ArrayList<>(certVerifier.getDetails(null));

        // Show signed JNLP warning if the signed main jar does not have a
        // signed JNLP file and the launching JNLP file contains special properties
        if (file != null && file.requiresSignedJNLPWarning()) {
            details.add(TRANSLATOR.translate("SJNLPFileIsNotSigned"));
        }

        this.closeButton = ButtonFactory.createNoButton(() -> null);
    }

    @Override
    protected String createTitle() {
        // TODO localization
        return "More Information";
    }

    private List<DialogButton<Void>> createButtons() {
        return Arrays.asList(closeButton);
    }

    private JComponent createDetailPaneContent() {
        int numLabels = details.size();
        JPanel errorPanel = new JPanel(new GridLayout(numLabels, 1));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        errorPanel.setPreferredSize(new Dimension(400, 50 * (numLabels)));

        for (int i = 0; i < numLabels; i++) {
            ImageIcon icon = null;
            if (details.get(i).equals(TRANSLATOR.translate("STrustedCertificate"))) {
                icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/info-small.png");
            } else {
                icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warning-small.png");
            }

            errorPanel.add(new JLabel(htmlWrap(details.get(i)), icon, SwingConstants.LEFT));
        }
        return errorPanel;
    }

    @Override
    protected JPanel createContentPane() {
        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detailPanel.add(createDetailPaneContent());

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());

        final List<DialogButton<Void>> buttons = createButtons();
        buttons.forEach(b -> {
            final JButton button = b.createButton(this::close);
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.add(detailPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }
}
