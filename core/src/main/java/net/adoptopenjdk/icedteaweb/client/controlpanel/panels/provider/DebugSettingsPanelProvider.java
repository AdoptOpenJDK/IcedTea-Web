package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.DebuggingPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class DebugSettingsPanelProvider implements ControlPanelProvider {

    private static final String NAME = "DebuggingPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
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
