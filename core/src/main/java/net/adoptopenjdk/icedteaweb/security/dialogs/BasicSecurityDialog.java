package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.adoptopenjdk.icedteaweb.ui.dialogs.ButtonBasedDialogWithResult;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;

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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public abstract class BasicSecurityDialog<R> extends ButtonBasedDialogWithResult<R> {

    private final static Translator TRANSLATOR = Translator.getInstance();

    private String message;

    public BasicSecurityDialog(String title, String message, DialogButton<R> ...buttons) {
        this(title, message, Arrays.asList(buttons));
    }

    public BasicSecurityDialog(String title, String message, List<DialogButton<R>> buttons) {
        super(title, buttons);
        this.message = message;
    }

    protected abstract JComponent createDetailPaneContent();

    @Override
    protected JPanel createContentPane(final List<DialogButton<R>> buttons) {
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
            final JButton button = new JButton(b.getText());
            if (b.getDescription() != null) {
                button.setToolTipText(b.getDescription());
            }
            button.addActionListener(e -> {
                final R result = b.getOnAction().get();
                close(result);
            });
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.NORTH);
        contentPanel.add(detailPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }

    public static <R> DialogButton<R> createOkButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButOk"), onAction);
    }

    public static <R> DialogButton<R> createCancelButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButCancel"), onAction);
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "Connection failed for URL: https://docs.oracle.com/javase/tutorialJWS/samples/uiswing/AccessibleScrollDemoProject/AccessibleScrollDemo.jnlp." +
                "\n\nDo you want to continue with no proxy or exit the application?";
        final DialogButton<Integer> exitButton = new DialogButton<>("Exit", () -> 0);

        new BasicSecurityDialog<Integer>("Security Warning", msg1, exitButton){
            @Override
            protected JComponent createDetailPaneContent() {
                return new JLabel("huhu");
            }
        }.showAndWait();
    //   new BasicSecurityDialog<>("Title", msg2, new ImageIcon(Images.NETWORK_64_URL), exitButton).showAndWait();
    }

}
