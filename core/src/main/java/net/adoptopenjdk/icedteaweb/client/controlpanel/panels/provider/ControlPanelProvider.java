package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public interface ControlPanelProvider {

    String getName();

    int getOrder();

    JComponent createPanel(DeploymentConfiguration config);

    default boolean isActive() {
        return true;
    }
}
