package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.NetworkSettingsPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class NetworkSettingsPanelProvider implements ControlPanelProvider {

    private static final String NAME = "NetworkSettingsPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabNetwork");
    }

    @Override
    public int getOrder() {
        return 60;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new NetworkSettingsPanel(config);
    }
}
