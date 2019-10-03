package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.JVMPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class JvmSettingsPanelProvider implements ControlPanelProvider {

    private static final String NAME = "JVMPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabJVMSettings");
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new JVMPanel(config);
    }
}
