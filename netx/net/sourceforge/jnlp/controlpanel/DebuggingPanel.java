/* DebuggingPanel.java -- Displays and sets options for debugging.
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This displays the options related to debugging.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class DebuggingPanel extends NamedBorderPanel implements ItemListener {

    /** List of properties used by this panel */
    public static String[] properties = { "deployment.trace", // Debugging
            "deployment.log", // Debugging
            "deployment.console.startup.mode", // Java Console
    };
    private DeploymentConfiguration config;

    /**
     * Create a new instance of the debugging panel.
     * 
     * @param config
     *            loaded DeploymentConfiguration file.
     */
    public DebuggingPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadDebugging"), new GridBagLayout());

        this.config = config;

        addComponents();
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();

        JLabel debuggingDescription = new JLabel("<html>" + Translator.R("CPDebuggingDescription") + "<hr /><br /></html>");

        JCheckBox[] debuggingOptions = { new JCheckBox(Translator.R("DPEnableTracing")),
                new JCheckBox(Translator.R("DPEnableLogging")), };

        ComboItem[] javaConsoleItems = { new ComboItem(Translator.R("DPDisable"), "DISABLE"),
                new ComboItem(Translator.R("DPHide"), "HIDE"),
                new ComboItem(Translator.R("DPShow"), "SHOW"), };

        JLabel consoleLabel = new JLabel(Translator.R("DPJavaConsole"));
        JComboBox consoleComboBox = new JComboBox();
        consoleComboBox.setActionCommand("deployment.console.startup.mode"); // The property this comboBox affects.

        JPanel consolePanel = new JPanel();
        consolePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        consolePanel.add(consoleLabel);
        consolePanel.add(consoleComboBox);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        add(debuggingDescription, c);

        /*
         * Add the items to the panel unless we can not get the values for them.
         */
        for (int i = 0; i < properties.length; i++) {
            try {
                String s = config.getProperty(properties[i]);
                c.gridy = i + 1;

                switch (i) {
                    case 0:
                    case 1:
                        debuggingOptions[i].setSelected(Boolean.parseBoolean(s));
                        debuggingOptions[i].setActionCommand(properties[i]);
                        debuggingOptions[i].addItemListener(this);
                        add(debuggingOptions[i], c);
                        break;
                    case 2:
                        for (int j = 0; j < javaConsoleItems.length; j++) {
                            consoleComboBox.addItem(javaConsoleItems[j]);
                            if (config.getProperty("deployment.console.startup.mode").equals(javaConsoleItems[j].getValue()))
                                consoleComboBox.setSelectedIndex(j);
                        }
                        consoleComboBox.addItemListener(this);
                        add(consolePanel, c);
                }

            } catch (Exception e) {
                debuggingOptions[i] = null;
            }
        }

        // pack the bottom so that it doesn't change size if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        add(filler, c);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        Object o = e.getSource();

        if (o instanceof JCheckBox) {
            JCheckBox jcb = (JCheckBox) o;
            config.setProperty(jcb.getActionCommand(), String.valueOf(jcb.isSelected()));
        } else if (o instanceof JComboBox) {
            JComboBox jcb = (JComboBox) o;
            ComboItem c = (ComboItem) e.getItem();
            config.setProperty(jcb.getActionCommand(), c.getValue());
        }

    }
}
