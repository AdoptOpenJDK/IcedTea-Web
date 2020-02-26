package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogWithResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Collections;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

public abstract class BasicSecurityDialog<R> extends DialogWithResult<R> {
    private String message;

    public BasicSecurityDialog(String message) {
        super();
        this.message = message;
    }

    protected ImageIcon createIcon() {
        return ImageGallery.QUESTION.asImageIcon();
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
        final JPanel bannerPanel = new JPanel(new BorderLayout(15, 0));
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

    private JLabel createBannerMessage() {
        final JLabel bannerText = new JLabel(htmlWrap(message), SwingConstants.CENTER);
        bannerText.setIconTextGap(10);
        bannerText.setBackground(null);
        bannerText.setFont(bannerText.getFont().deriveFont(16f));
        return bannerText;
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
