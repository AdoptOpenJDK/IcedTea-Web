package net.sourceforge.jnlp.security.policyeditor;

import net.sourceforge.jnlp.util.logging.OutputController;

import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.KeyStroke;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URISyntaxException;

import static net.sourceforge.jnlp.runtime.Translator.R;

public class PolicyEditorAboutDialog extends JFrame {
    private final String content;
    private final JScrollPane scrollPane = new JScrollPane();
    private final JTextPane textArea = new JTextPane();
    private final JPanel noWrapPanel = new JPanel();
    private final JButton closeButton = new JButton(R("ButClose"));
    private final ActionListener closeButtonAction;
    private final KeyListener closeKeyListener;

    public PolicyEditorAboutDialog(final String title, final String content) {
        super(title);
        this.content = content;
        setupLayout();

        closeButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                close();
            }
        };
        closeButton.addActionListener(closeButtonAction);

        closeKeyListener = new CloseKeyListener();
        scrollPane.addKeyListener(closeKeyListener);
        textArea.addKeyListener(closeKeyListener);
        noWrapPanel.addKeyListener(closeKeyListener);
        closeButton.addKeyListener(closeKeyListener);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());

        textArea.setEditorKit(new HTMLEditorKit());
        textArea.setContentType("text/html");
        textArea.setText(content);
        textArea.setEditable(false);
        textArea.addHyperlinkListener(new UrlHyperlinkListener());

        noWrapPanel.setLayout(new BorderLayout());
        noWrapPanel.add(textArea, BorderLayout.CENTER);
        scrollPane.setViewportView(textArea);
        textArea.setCaretPosition(0);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = 3;
        panelConstraints.weighty = 1.0;
        panelConstraints.fill = GridBagConstraints.BOTH;
        final EmptyBorder scrollPaneBorder = new EmptyBorder(5, 5, 5, 5);
        scrollPane.setBorder(scrollPaneBorder);
        final EmptyBorder textAreaBorder = new EmptyBorder(0, 10, 10, 10);
        textArea.setBorder(textAreaBorder);
        add(scrollPane, panelConstraints);

        final GridBagConstraints closeButtonConstraints = new GridBagConstraints();
        closeButtonConstraints.weightx = 1.0;
        closeButtonConstraints.weighty = 0.0;
        closeButtonConstraints.gridx = 1;
        closeButtonConstraints.gridy = 1;
        closeButtonConstraints.insets = new Insets(0, 0, 5, 0);
        add(closeButton, closeButtonConstraints);

        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(getMinimumSize());
        pack();
    }

    private class CloseKeyListener implements KeyListener {
        @Override
        public void keyTyped(final KeyEvent e) {
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            if (e.getExtendedKeyCode() == KeyStroke.getKeyStroke(R("PEAboutPolicyEditorCloseAccelerator")).getKeyCode()
                    || e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
                close();
            }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
        }
    }

    private static class UrlHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (Desktop.isDesktopSupported() && event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (final URISyntaxException | IOException ex) {
                    OutputController.getLogger().log(ex);
                }
            }
        }
    }

    public static void main(final String[] args) {
        final PolicyEditorAboutDialog dialog = new PolicyEditorAboutDialog(R("PEHelpDialogTitle"), R("PEHelpDialogContent"));
        dialog.setVisible(true);
    }

}
