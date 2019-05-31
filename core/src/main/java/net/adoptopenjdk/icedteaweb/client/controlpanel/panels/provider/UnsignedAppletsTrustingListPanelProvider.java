package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.controlpanel.UnsignedAppletsTrustingListPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;

import javax.swing.JComponent;

public class UnsignedAppletsTrustingListPanelProvider implements ControlPanelProvider {
    @Override
    public String getName() {
        return Translator.R("APPEXTSECControlPanelExtendedAppletSecurityTitle");
    }

    @Override
    public int getOrder() {
        return 90;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new UnsignedAppletsTrustingListPanel(PathsAndFiles.APPLET_TRUST_SETTINGS_SYS.getFile(), PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), config);
    }
}
