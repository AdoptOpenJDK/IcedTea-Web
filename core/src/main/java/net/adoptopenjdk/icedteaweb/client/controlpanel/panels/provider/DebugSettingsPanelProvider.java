package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.DebuggingPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class DebugSettingsPanelProvider implements ControlPanelProvider {

    @Override
    public String getName() {
        return Translator.R("CPTabDebugging");
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new DebuggingPanel(config);
    }
}
