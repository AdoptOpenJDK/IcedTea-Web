package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.AboutPanel;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class AboutPanelProvider implements ControlPanelProvider {

    public static final String NAME = "AboutPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return "About Panel";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new AboutPanel();
    }
}
