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
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
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
import java.util.Arrays;
import java.util.List;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_ASKGRANTDIALOG_NOTINCA;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_PROMPT_USER;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_PROMPT_USER_FOR_JNLP;

/**
 * This provides a way for the user to modify the security settings through a
 * GUI.
 *
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 */
@SuppressWarnings("serial")
public class SecuritySettingsPanel extends NamedBorderPanel {

    /**
     * This creates a new instance of the security settings panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public SecuritySettingsPanel(final DeploymentConfiguration config) {
        super(Translator.R("CPHeadSecurity"), new BorderLayout());

        final List<PropertyToText> properties = Arrays.asList(
                new PropertyToText(KEY_SECURITY_PROMPT_USER, "SGPAllowUserGrantSigned"),
                new PropertyToText(KEY_SECURITY_ASKGRANTDIALOG_NOTINCA, "SGPAllowUserGrantUntrust"),
                new PropertyToText(KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING, "SGPWarnCertHostMismatch"),
                new PropertyToText(KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING, "SGPShowSandboxWarning"),
                new PropertyToText(KEY_SECURITY_PROMPT_USER_FOR_JNLP, "SGPAllowUserAcceptJNLPSecurityRequests"),
                new PropertyToText(KEY_HTTPS_DONT_ENFORCE, "security.panel.notEnforceHttps"),
                new PropertyToText(KEY_ASSUME_FILE_STEM_IN_CODEBASE, "security.panel.asumeFilesystemInCodebase")
        );

        final JPanel topPanel = new JPanel(new GridBagLayout());

        final UiLock uiLock = new UiLock(config);

        properties.stream().forEach(p -> {
            final JCheckBox checkBox = new JCheckBox(Translator.R(p.getPropertyTitleKey()));
            final String propertyName = p.getPropertyName();
            final String value = config.getProperty(propertyName);

            checkBox.setSelected(Boolean.parseBoolean(value));
            checkBox.addActionListener(e -> {
                config.setProperty(propertyName, String.valueOf(checkBox.isSelected()));
            });
            uiLock.update(propertyName, checkBox);

            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.weightx = 1;
            constraints.gridy = properties.indexOf(p);
            topPanel.add(checkBox, constraints);
        });

        final Component filler = Box.createRigidArea(new Dimension(1, 1));
        final GridBagConstraints fillerConstraints = new GridBagConstraints();
        fillerConstraints.fill = GridBagConstraints.BOTH;
        fillerConstraints.gridx = 0;
        fillerConstraints.weightx = 1;
        fillerConstraints.weighty = 1;
        fillerConstraints.gridy = properties.size() + 1;
        topPanel.add(filler, fillerConstraints);

        add(topPanel, BorderLayout.CENTER);
    }

    private class PropertyToText {

        private final String propertyName;

        private final String propertyTitleKey;

        public PropertyToText(final String propertyName, final String propertyTitleKey) {
            this.propertyName = propertyName;
            this.propertyTitleKey = propertyTitleKey;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getPropertyTitleKey() {
            return propertyTitleKey;
        }
    }
}
