/* SecuritySettingsPanel.java -- Display possible security settings.
Copyright (C) 2010 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package net.sourceforge.jnlp.controlpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This provides a way for the user to modify the security settings through a
 * GUI.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
@SuppressWarnings("serial")
public class SecuritySettingsPanel extends NamedBorderPanel implements ActionListener {

    private DeploymentConfiguration config;

    // NOTE: All the ones listed with "Default" are in Oracle's implementation.
    // Not shown on deployments.properties webpage. Add support for these later!
    /** List of properties used by this panel */
    public static String[] properties = { "deployment.security.askgrantdialog.show",
            "deployment.security.askgrantdialog.notinca",
            "deployment.security.browser.keystore.use", // default TRUE
            "deployment.security.clientauth.keystore.auto", // Default FALSE
            "deployment.security.jsse.hostmismatch.warning",
            "deployment.security.https.warning.show", // Default FALSE
            "deployment.security.sandbox.awtwarningwindow",
            "deployment.security.sandbox.jnlp.enhanced",
            "deployment.security.validation.crl", // Default TRUE
            "deployment.security.validation.ocsp", // Default FALSE
            "deployment.security.pretrust.list", // Default TRUE
            "deployment.security.blacklist.check", // Default TRUE
            "deployment.security.password.cache", // Default TRUE
            "deployment.security.SSLv2Hello", // Default FALSE
            "deployment.security.SSLv3", // Default TRUE
            "deployment.security.TLSv1", // Default TRUE
            //            "deployment.security.mixcode", // Default TRUE
    };

    /**
     * This creates a new instance of the security settings panel.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public SecuritySettingsPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadSecurity"), new BorderLayout());
        this.config = config;

        addComponents();
    }

    /**
     * Add the components to the panel.
     */
    private void addComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel description = new JLabel("<html>" + Translator.R("CPSecurityDescription") + "<hr /></html>");

        JCheckBox[] securityGeneralOptions = { new JCheckBox(Translator.R("SGPAllowUserGrantSigned")),
                new JCheckBox(Translator.R("SGPAllowUserGrantUntrust")),
                new JCheckBox(Translator.R("SGPUseBrowserKeystore")),
                new JCheckBox(Translator.R("SGPUsePersonalCertOneMatch")),
                new JCheckBox(Translator.R("SGPWarnCertHostMismatch")),
                new JCheckBox(Translator.R("SGPShowValid")),
                new JCheckBox(Translator.R("SGPShowSandboxWarning")),
                new JCheckBox(Translator.R("SGPAllowUserAcceptJNLPSecurityRequests")),
                new JCheckBox(Translator.R("SGPCheckCertRevocationList")),
                new JCheckBox(Translator.R("SGPEnableOnlineCertValidate")),
                new JCheckBox(Translator.R("SGPEnableTrustedPublisherList")),
                new JCheckBox(Translator.R("SGPEnableBlacklistRevocation")),
                new JCheckBox(Translator.R("SGPEnableCachingPassword")),
                new JCheckBox(Translator.R("SGPUseSSL2")),
                new JCheckBox(Translator.R("SGPUseSSL3")),
                new JCheckBox(Translator.R("SGPUseTLS1")), };

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.weightx = 1;

        topPanel.add(description, c);

        // Only display the ones with properties that are valid or existent.
        for (int i = 0; i < properties.length; i++) {
            String s = config.getProperty(properties[i]);
            if (s == null) {
                securityGeneralOptions[i] = null;
                continue;
            }
            securityGeneralOptions[i].setSelected(Boolean.parseBoolean(s));
            securityGeneralOptions[i].setActionCommand(properties[i]);
            securityGeneralOptions[i].addActionListener(this);
            c.gridy = i + 1;
            topPanel.add(securityGeneralOptions[i], c);
        }

        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.weighty = 1;
        c.gridy++;
        topPanel.add(filler, c);

        add(topPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        config.setProperty(e.getActionCommand(), String.valueOf(((JCheckBox) e.getSource()).isSelected()));
    }
}
