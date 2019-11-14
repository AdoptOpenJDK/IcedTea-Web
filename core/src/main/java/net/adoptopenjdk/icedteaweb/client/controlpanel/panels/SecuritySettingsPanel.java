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
package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;

/**
 * This provides a way for the user to modify the security settings through a
 * GUI.
 *
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 */
@SuppressWarnings("serial")
public class SecuritySettingsPanel extends NamedBorderPanel {

    private final DeploymentConfiguration config;

    /**
     * This creates a new instance of the security settings panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public SecuritySettingsPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadSecurity"), new BorderLayout());
        this.config = config;

        final Map<JCheckBox, String> propertyBasedCheckboxes = new LinkedHashMap<>();

        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("SGPAllowUserGrantSigned")), "deployment.security.askgrantdialog.show");
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("SGPAllowUserGrantUntrust")), "deployment.security.askgrantdialog.notinca");
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("SGPWarnCertHostMismatch")), "deployment.security.jsse.hostmismatch.warning");
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("SGPShowSandboxWarning")), "deployment.security.sandbox.awtwarningwindow");
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("SGPAllowUserAcceptJNLPSecurityRequests")), "deployment.security.sandbox.jnlp.enhanced");
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("security.panel.notEnforceHttps")), KEY_HTTPS_DONT_ENFORCE);
        propertyBasedCheckboxes.put(new JCheckBox(Translator.R("security.panel.asumeFilesystemInCodebase")), KEY_ASSUME_FILE_STEM_IN_CODEBASE);

        JPanel topPanel = new JPanel(new GridBagLayout());

        final List<Map.Entry<JCheckBox, String>> entries = new ArrayList<>(propertyBasedCheckboxes.entrySet());

        IntStream.range(0, entries.size()).forEach(index -> {
            final Map.Entry<JCheckBox, String> entry = entries.get(index);
            final JCheckBox checkBox = entry.getKey();
            final String propertyName = entry.getValue();

            String value = config.getProperty(propertyName);

            checkBox.setSelected(Boolean.parseBoolean(value));
            checkBox.addActionListener(e -> {
                config.setProperty(propertyName, String.valueOf(checkBox.isSelected()));
            });

            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.weightx = 1;
            constraints.gridy = index;
            topPanel.add(checkBox, constraints);
        });

        Component filler = Box.createRigidArea(new Dimension(1, 1));
        GridBagConstraints fillerConstraints = new GridBagConstraints();
        fillerConstraints.fill = GridBagConstraints.BOTH;
        fillerConstraints.gridx = 0;
        fillerConstraints.weightx = 1;
        fillerConstraints.weighty = 1;
        fillerConstraints.gridy = entries.size() + 1;
        topPanel.add(filler, fillerConstraints);

        add(topPanel, BorderLayout.CENTER);
    }
}
