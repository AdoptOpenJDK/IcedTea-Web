package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.TemporaryInternetFilesPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class CacheSettingsPanelProvider implements ControlPanelProvider {

    private static final String NAME = "TemporaryInternetFilesPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
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
