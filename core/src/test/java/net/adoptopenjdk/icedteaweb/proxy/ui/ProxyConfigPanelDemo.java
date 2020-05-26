package net.adoptopenjdk.icedteaweb.proxy.ui;

import net.adoptopenjdk.icedteaweb.ui.swing.FormPanel;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ProxyConfigPanelDemo extends FormPanel {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setContentPane(new ProxyConfigPanel(new DeploymentConfiguration()));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
