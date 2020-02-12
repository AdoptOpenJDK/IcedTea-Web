package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

public abstract class BasicSecurityDialog<R> extends DialogWithResult<R> {

    private final static Translator TRANSLATOR = Translator.getInstance();

    private String message;

    public BasicSecurityDialog(String title, String message) {
        super(title);
        this.message = message;
    }

    protected abstract List<DialogButton<R>> createButtons();

    protected abstract JComponent createDetailPaneContent();

    @Override
    protected JPanel createContentPane() {
        final List<DialogButton<R>> buttons = createButtons();

        final ImageIcon icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/question.png");

        JLabel iconComponent = new JLabel("", icon, SwingConstants.LEFT);
        final JTextArea messageLabel = new JTextArea(message);
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        messageLabel.setColumns(50);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(new BorderLayout(12, 12));
        messageWrapperPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        messageWrapperPanel.setBackground(Color.WHITE);
        messageWrapperPanel.add(iconComponent, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detailPanel.add(createDetailPaneContent());

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());

        buttons.forEach(b -> {
            final JButton button = b.createButton(r -> close(r));
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.NORTH);
        contentPanel.add(detailPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "Connection failed for URL: https://docs.oracle.com/javase/tutorialJWS/samples/uiswing/AccessibleScrollDemoProject/AccessibleScrollDemo.jnlp." +
                "\n\nDo you want to continue with no proxy or exit the application?";

        final DialogButton<Integer> exitButton = new DialogButton<>("BasicSecurityDialog 1 Title", () -> 0);

        new BasicSecurityDialog<Integer>("Security Warning", msg1){
            @Override
            protected List<DialogButton<Integer>> createButtons() {
                return Collections.singletonList(exitButton);
            }

            @Override
            protected JComponent createDetailPaneContent() {
                return new JLabel("Detail pane content");
            }
        }.showAndWait();

        new BasicSecurityDialog<Integer>("BasicSecurityDialog 2 Title", msg2) {
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
