package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.PolicyPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class PolicySettingsPanelProvider implements ControlPanelProvider {

    @Override
    public String getName() {
        return Translator.R("CPTabPolicy");
    }

    @Override
    public int getOrder() {
        return 80;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new PolicyPanel(config);
    }
}
