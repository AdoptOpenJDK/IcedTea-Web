package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.ServerWhitelistPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class ServerWhitelistPanelProvider implements ControlPanelProvider {

    public static final String NAME = "ServerWhitelistPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabServerWhitelist");
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new ServerWhitelistPanel(config);
    }
}
