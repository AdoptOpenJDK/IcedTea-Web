/* DesktopShortcutPanel.java -- Display option for adding desktop shortcut.
Copyright (C) 2015 Red Hat

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

import net.adoptopenjdk.icedteaweb.client.controlpanel.ComboItem;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.desktopintegrationeditor.FreeDesktopIntegrationEditorFrame;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This class provides the panel that allows the user to set whether they want
 * to create a desktop shortcut for javaws.
 */
public class DesktopShortcutPanel extends NamedBorderPanel implements ItemListener {

    private final DeploymentConfiguration config;
    private FreeDesktopIntegrationEditorFrame integrationManagement;

    /**
     * Create a new instance of the desktop shortcut settings panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public DesktopShortcutPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadDesktopIntegration"), new GridBagLayout());
        this.config = config;

        addComponents();
    }

    public static ComboItem deploymentJavawsShortcutToComboItem(String i) {
        return new ComboItem(ShortcutDesc.deploymentJavawsShortcutToString(i), i);
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();
        JLabel description = new JLabel("<html>" + Translator.R("CPDesktopIntegrationDescription") + "<hr /></html>");
        JComboBox<ComboItem> shortcutComboOptions = new JComboBox<>();
        JButton manageIntegrationsButton = new JButton(Translator.R("CPDesktopIntegrationShowIntegrations"));
        if (OsUtil.isWindows()) {
            manageIntegrationsButton.setToolTipText(Translator.R("CPDesktopIntegrationLinuxOnly"));
            manageIntegrationsButton.setEnabled(false);
        }
        manageIntegrationsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtils.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (integrationManagement == null) {
                            integrationManagement = new FreeDesktopIntegrationEditorFrame();
                        }
                        integrationManagement.setVisible(true);
                    }
                });
            }
        });
        ComboItem[] items = {deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_NEVER),
            deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ALWAYS),
            deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ASK_USER),
            deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ASK_USER_IF_HINTED),
            deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ALWAYS_IF_HINTED)};

        shortcutComboOptions.setActionCommand(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT); // The configuration property this combobox affects.
        for (int j = 0; j < items.length; j++) {
            shortcutComboOptions.addItem(items[j]);
            if (config.getProperty(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT).equals(items[j].getValue())) {
                shortcutComboOptions.setSelectedIndex(j);
            }
        }

        shortcutComboOptions.addItemListener(this);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        add(description, c);
        c.gridy = 1;
        add(shortcutComboOptions, c);
        c.gridy = 2;
        add(manageIntegrationsButton, c);

        // This is to keep it from expanding vertically if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        add(filler, c);
    }

    @SuppressWarnings("unchecked")
    public void itemStateChanged(ItemEvent e) {
        ComboItem c = (ComboItem) e.getItem();
        config.setProperty(((JComboBox<ComboItem>) e.getSource()).getActionCommand(), c.getValue());
    }
}
