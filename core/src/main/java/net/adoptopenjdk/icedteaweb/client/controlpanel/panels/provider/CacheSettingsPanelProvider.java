package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.TemporaryInternetFilesPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class CacheSettingsPanelProvider implements ControlPanelProvider {

    @Override
    public String getName() {
        return Translator.R("CPTabCache");
    }

    @Override
    public int getOrder() {
        return 15;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new TemporaryInternetFilesPanel(config);
    }
}
