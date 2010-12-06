package net.sourceforge.jnlp.controlpanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sourceforge.jnlp.runtime.DeploymentConfiguration;

/**
 * When middle click pastes to the checkboxes it doesn't register it... This is
 * to fix that problem. Not needed in Windows.
 * 
 * @author Andrew Su <asu@redhat.com, andrew.su@utoronto.ca>
 * 
 */
class MiddleClickListener extends MouseAdapter {

    DeploymentConfiguration config;
    private String property;

    /**
     * Creates a new instance of middle-click listener.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     * @param property
     *            the property in configuration file to edit.
     */
    public MiddleClickListener(DeploymentConfiguration config, String property) {
        this.config = config;
        this.property = property;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        String result = null;
        if (obj instanceof JTextField)
            result = ((JTextField) obj).getText();
        else if (obj instanceof JTextArea)
            result = ((JTextArea) obj).getText();

        config.setProperty(property, result);
    }
}
