package net.adoptopenjdk.icedteaweb.client.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.util.Optional;

/**
 * Helper class that can be used to check if a UI element should be locked / disabled in the UI
 */
public class UiLock {

    private final DeploymentConfiguration deploymentConfiguration;

    private final static String LOCK_SUFFIX = ".locked";

    public UiLock(final DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");
    }

    public void update(final String propertyName, final Component component) {
        final boolean isLocked = Optional.ofNullable(deploymentConfiguration.getProperty(propertyName + LOCK_SUFFIX))
                .map(s -> Boolean.parseBoolean(s))
                .orElse(false);
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

    public void addLock(final String propertyName) {
        deploymentConfiguration.setProperty(propertyName + LOCK_SUFFIX, Boolean.TRUE.toString());
    }
}
