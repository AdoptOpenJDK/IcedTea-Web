package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateCheck;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdatePolicy;
import net.sourceforge.jnlp.cache.UpdateOptions;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class UpdatePolicyDialog extends JDialog {
    private UpdateOptions updateOptions;

    public UpdatePolicyDialog(final UpdateOptions updateOptions) {
        this.updateOptions = updateOptions;
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setAlwaysOnTop(true);
        setTitle("Update Policy");

        final JLabel description = new JLabel();
        description.setText("<html><p>" +
                "A new update is available for your application. " +
                "The update policy for the JNLP application is defined to prompt for a user decision on how to handle this application update before the application is launched." +
                "</p></html>");
        description.setPreferredSize(new Dimension(500, 100));

        JRadioButton downloadAndRunUpdatedButton = new JRadioButton("Download and run the updated version", true);
        downloadAndRunUpdatedButton.addActionListener(e -> {
            updateOptions.setAlwaysDownloadUpdates(downloadAndRunUpdatedButton.isSelected());
        });
        JRadioButton launchCachedVersionButton = new JRadioButton("Launch the cached version");
        launchCachedVersionButton.addItemListener(e -> {
            updateOptions.setLaunchCachedVersion(launchCachedVersionButton.isSelected());
        });
        JRadioButton cancelAndAbortRunningApplicationButton = new JRadioButton("Cancel and abort running the application");
        cancelAndAbortRunningApplicationButton.addActionListener(e -> {
            updateOptions.setCancelAndAbortRunningTheApplication(cancelAndAbortRunningApplicationButton.isSelected());
        });

        ButtonGroup bgroup = new ButtonGroup();
        bgroup.add(downloadAndRunUpdatedButton);
        bgroup.add(launchCachedVersionButton);
        bgroup.add(cancelAndAbortRunningApplicationButton);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(3, 1));
        radioPanel.add(downloadAndRunUpdatedButton);
        radioPanel.add(launchCachedVersionButton);
        radioPanel.add(cancelAndAbortRunningApplicationButton);

        final JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
        });

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());


        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));
        mainPanel.add(description);
        mainPanel.add(radioPanel);


        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actionWrapperPanel.add(okButton);
        actionWrapperPanel.add(cancelButton);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(actionWrapperPanel, BorderLayout.SOUTH);

        add(panel);
    }

    public UpdateOptions showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        return updateOptions;
    }

    public static void main(String[] args) {
        new UpdatePolicyDialog(new UpdateOptions(UpdateCheck.ALWAYS, UpdatePolicy.PROMPT_UPDATE)).showAndWait();
    }
}
