/* DesktopShortcutPanel.java -- Display option for adding desktop shortcut.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This class provides the panel that allows the user to set whether they want
 * to create a desktop shortcut for javaws.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class DesktopShortcutPanel extends NamedBorderPanel implements ItemListener {

    private DeploymentConfiguration config;

    /**
     * Create a new instance of the desktop shortcut settings panel.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public DesktopShortcutPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadDesktopIntegration"), new GridBagLayout());
        this.config = config;

        addComponents();
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();
        JLabel description = new JLabel("<html>" + Translator.R("CPDesktopIntegrationDescription") + "<hr /></html>");
        JComboBox shortcutComboOptions = new JComboBox();
        ComboItem[] items = { new ComboItem(Translator.R("DSPNeverCreate"), "NEVER"),
                new ComboItem(Translator.R("DSPAlwaysAllow"), "ALWAYS"),
                new ComboItem(Translator.R("DSPAskUser"), "ASK_USER"),
                new ComboItem(Translator.R("DSPAskIfHinted"), "ASK_IF_HINTED"),
                new ComboItem(Translator.R("DSPAlwaysIfHinted"), "ALWAYS_IF_HINTED") };

        shortcutComboOptions.setActionCommand("deployment.javaws.shortcut"); // The configuration property this combobox affects.
        for (int j = 0; j < items.length; j++) {
            shortcutComboOptions.addItem(items[j]);
            if (config.getProperty("deployment.javaws.shortcut").equals(items[j].getValue())) {
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

        // This is to keep it from expanding vertically if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        add(filler, c);
    }

    public void itemStateChanged(ItemEvent e) {
        ComboItem c = (ComboItem) e.getItem();
        config.setProperty(((JComboBox) e.getSource()).getActionCommand(), c.getValue());
    }
}
