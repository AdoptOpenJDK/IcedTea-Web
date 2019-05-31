package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.SecuritySettingsPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class SecuritySettingsPanelProvider implements ControlPanelProvider {

    @Override
    public String getName() {
        return Translator.R("CPTabSecurity");
    }

    @Override
    public int getOrder() {
        return 70;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new SecuritySettingsPanel(config);
    }
}
