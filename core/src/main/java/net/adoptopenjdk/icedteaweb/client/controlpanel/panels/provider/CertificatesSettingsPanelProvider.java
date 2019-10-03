package net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider;

import net.adoptopenjdk.icedteaweb.client.certificateviewer.CertificatePane;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class CertificatesSettingsPanelProvider implements ControlPanelProvider {

    private static final String NAME = "CertificatePane";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPHeadCertificates");
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        JPanel p = new NamedBorderPanel(getName(), new BorderLayout());
        p.add(new CertificatePane(null), BorderLayout.CENTER);
        return p;
    }
}
