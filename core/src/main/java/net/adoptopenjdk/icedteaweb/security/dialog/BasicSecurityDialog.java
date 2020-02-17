package net.adoptopenjdk.icedteaweb.security.dialog;

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
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.List;

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
        JLabel iconComponent = new JLabel("", createIcon(), SwingConstants.LEFT);
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

        final List<DialogButton<R>> buttons = createButtons();
        buttons.forEach(b -> {
            final JButton button = b.createButton(this::close);
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.NORTH);
        contentPanel.add(detailPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }

    protected void addRow(String key, String value, JPanel panel, int row) {
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

    protected void addSeparatorRow(boolean hasLine, JPanel panel, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.ipady = 4;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hasLine ? new JSeparator() : new JPanel(), constraints);
    }

    protected void addRow(JComponent child, JPanel panel, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.ipady = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(child, constraints);
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line. " +
                "This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "Connection failed for URL: https://docs.oracle.com/javase/tutorialJWS/samples/uiswing/AccessibleScrollDemoProject/AccessibleScrollDemo.jnlp." +
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
