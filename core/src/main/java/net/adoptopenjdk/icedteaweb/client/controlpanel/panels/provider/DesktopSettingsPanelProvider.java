package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.DesktopShortcutPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class DesktopSettingsPanelProvider implements ControlPanelProvider {

    public static final String NAME = "DesktopShortcutPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabDesktopIntegration");
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new DesktopShortcutPanel(config);
    }
}
