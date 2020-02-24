package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.ComponentRow;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagRow;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.KeyValueRow;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.font.TextAttribute;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class BasicSecurityDialog<R> extends DialogWithResult<R> {

    private static final Translator TRANSLATOR = Translator.getInstance();

    private String message;

    public BasicSecurityDialog(String message) {
        super();
        this.message = message;
    }

    protected ImageIcon createIcon() {
        return SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/question.png");
    }

    protected abstract List<DialogButton<R>> createButtons();

    protected abstract JComponent createDetailPaneContent();

    @Override
    protected JPanel createContentPane() {
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createBanner(), BorderLayout.NORTH);
        contentPanel.add(createDetails(), BorderLayout.CENTER);
        contentPanel.add(createActionButtons(), BorderLayout.SOUTH);
        return contentPanel;
    }

    private JPanel createBanner() {
        final JPanel bannerPanel = new JPanel();
        bannerPanel.setLayout(new BorderLayout(15, 0));
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        bannerPanel.setBackground(Color.WHITE);

        bannerPanel.add(createBannerImage(), BorderLayout.WEST);
        bannerPanel.add(createBannerMessage(), BorderLayout.CENTER);
        return bannerPanel;
    }

    private JPanel createBannerImage() {
        JPanel alignHelperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        alignHelperPanel.setBackground(null);
        final JLabel iconLabel = new JLabel(createIcon());
        alignHelperPanel.add(iconLabel);
        return alignHelperPanel;
    }

    private JTextArea createBannerMessage() {
        final JTextArea messageLabel = new JTextArea(message);
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        messageLabel.setColumns(50);
        messageLabel.setFont(messageLabel.getFont().deriveFont(14f));

        return messageLabel;
    }

    private JPanel createDetails() {
        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detailPanel.add(createDetailPaneContent());
        return detailPanel;
    }

    private JPanel createActionButtons() {
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());

        final List<DialogButton<R>> buttons = createButtons();
        buttons.forEach(b -> {
            final JButton button = b.createButton(this::close);
            buttonPanel.add(button);
        });
        return buttonPanel;
    }

    protected static List<GridBagRow> getApplicationDetails(JNLPFile file) {
        final List<GridBagRow> rows = new ArrayList<>();

        final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        final JLabel applicationLabel = new JLabel(TRANSLATOR.translate("Application"));
        final Map<TextAttribute, Object> underlineAttributes = new HashMap<>(applicationLabel.getFont().getAttributes());
        underlineAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        applicationLabel.setFont(applicationLabel.getFont().deriveFont(underlineAttributes));
        titlePanel.add(applicationLabel);

        if (file.isUnsigend()) {
            final JLabel warningLabel = new JLabel(TRANSLATOR.translate("SUnverifiedJnlp"));
            final Map<TextAttribute, Object> boldAttributes = new HashMap<>(warningLabel.getFont().getAttributes());
            boldAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_ULTRABOLD);
            warningLabel.setFont(warningLabel.getFont().deriveFont(boldAttributes));
            titlePanel.add(warningLabel);
        }

        rows.add(new ComponentRow(titlePanel));

        final String name = ofNullable(file)
                .map(JNLPFile::getInformation)
                .map(InformationDesc::getTitle)
                .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
        rows.add(new KeyValueRow(TRANSLATOR.translate("Name"), name));

        final String publisher = ofNullable(file)
                .map(JNLPFile::getInformation)
                .map(InformationDesc::getVendor)
                .map(v -> v + " " + TRANSLATOR.translate("SUnverified"))
                .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
        rows.add(new KeyValueRow(TRANSLATOR.translate("Publisher"), publisher));


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
        rows.add(new KeyValueRow(TRANSLATOR.translate("From"), from));
        return rows;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "This is a small text line." +
                "\n\nDo you want to continue with no proxy or exit the application?";

        final DialogButton<Integer> exitButton = new DialogButton<>("BasicSecurityDialog 1 Title", () -> 0);

        new BasicSecurityDialog<Integer>(msg1) {
            @Override
            public String createTitle() {
                return "Security Warning 1";
            }

            @Override
            protected List<DialogButton<Integer>> createButtons() {
                return Collections.singletonList(exitButton);
            }

            @Override
            protected JComponent createDetailPaneContent() {
                return new JLabel("Detail pane content");
            }
        }.showAndWait();

        new BasicSecurityDialog<Integer>(msg2) {
            @Override
            public String createTitle() {
                return "Security Warning 2";
            }

            @Override
            protected List<DialogButton<Integer>> createButtons() {
                return Collections.singletonList(exitButton);
            }

            @Override
            protected JComponent createDetailPaneContent() {
                return new JLabel("Detail pane content");
            }

        }.showAndWait();
    }
}
