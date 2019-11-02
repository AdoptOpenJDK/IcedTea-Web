package net.adoptopenjdk.icedteaweb.client.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import java.awt.Component;

/**
 * Helper class that can be used to check if a UI element should be locked / disabled in the UI
 */
public class UiLock {

    private final DeploymentConfiguration deploymentConfiguration;

    public UiLock(final DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");
    }

    public void update(final String propertyName, final Component component) {
        final boolean isLocked = deploymentConfiguration.isLocked(propertyName);
        if(isLocked) {
            component.setEnabled(false);

            if(component instanceof JTextComponent) {
                ((JTextComponent) component).setEditable(false);
            }
            if(component instanceof JComboBox) {
                ((JComboBox) component).setEditable(false);
            }
        }
    }
}
