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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sourceforge.jnlp.config.Defaults;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.LogConfig;

/**
 * This displays the options related to debugging.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class DebuggingPanel extends NamedBorderPanel implements ItemListener {

    /** List of properties used by checkboxes in this panel */
    public static String[] properties = {
            DeploymentConfiguration.KEY_ENABLE_LOGGING,
            DeploymentConfiguration.KEY_ENABLE_LOGGING_HEADERS,
            DeploymentConfiguration.KEY_ENABLE_LOGGING_TOFILE,
            DeploymentConfiguration.KEY_ENABLE_LOGGING_TOSTREAMS,
            DeploymentConfiguration.KEY_ENABLE_LOGGING_TOSYSTEMLOG
            
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


        final JLabel debuggingDescription = new JLabel("<html>" + Translator.R("CPDebuggingDescription") + "<hr /><br /></html>");
        final JLabel logsDestinationTitle = new JLabel(Translator.R("CPFilesLogsDestDir")+": ");
        final JTextField logsDestination = new JTextField(config.getProperty(DeploymentConfiguration.KEY_USER_LOG_DIR));
        logsDestination.getDocument().addDocumentListener(new DocumentListener() {


            @Override
            public void insertUpdate(DocumentEvent e) {
                 save();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                 save();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                save();

            }

            private void save() {
                config.setProperty(DeploymentConfiguration.KEY_USER_LOG_DIR, logsDestination.getText());
            }
        });
        final JButton logsDestinationReset = new JButton(Translator.R("CPFilesLogsDestDirResert"));
        logsDestinationReset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logsDestination.setText(Defaults.getDefaults().get(DeploymentConfiguration.KEY_USER_LOG_DIR).getDefaultValue());
            }
        });

        JCheckBox[] debuggingOptions = { 
                new JCheckBox(Translator.R("DPEnableLogging")),
                new JCheckBox(Translator.R("DPEnableHeaders")),
                new JCheckBox(Translator.R("DPEnableFile")),
                new JCheckBox(Translator.R("DPEnableStds")),
                new JCheckBox(Translator.R("DPEnableSyslog"))
        };
        String[] hints = { 
                (Translator.R("DPEnableLoggingHint")),
                (Translator.R("DPEnableHeadersHint")),
                (Translator.R("DPEnableFileHint", LogConfig.getLogConfig().getIcedteaLogDir())),
                (Translator.R("DPEnableStdsHint")),
                (Translator.R("DPEnableSyslogHint"))
        };

        ComboItem[] javaConsoleItems = { new ComboItem(Translator.R("DPDisable"), DeploymentConfiguration.CONSOLE_DISABLE),
                new ComboItem(Translator.R("DPHide"), DeploymentConfiguration.CONSOLE_HIDE),
                new ComboItem(Translator.R("DPShow"), DeploymentConfiguration.CONSOLE_SHOW), 
                new ComboItem(Translator.R("DPShowPluginOnly"), DeploymentConfiguration.CONSOLE_SHOW_PLUGIN), 
                new ComboItem(Translator.R("DPShowJavawsOnly"), DeploymentConfiguration.CONSOLE_SHOW_JAVAWS) };

        JLabel consoleLabel = new JLabel(Translator.R("DPJavaConsole"));
        JComboBox consoleComboBox = new JComboBox();
        consoleComboBox.setActionCommand(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE); // The property this comboBox affects.

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
            String s = config.getProperty(properties[i]);
            c.gridy++;
            if (i == 2) {
                JLabel space = new JLabel("<html>" + Translator.R("CPDebuggingPossibilites") + ":</html>");
                add(space, c);
                c.gridy++;
            }

            debuggingOptions[i].setSelected(Boolean.parseBoolean(s));
            debuggingOptions[i].setActionCommand(properties[i]);
            debuggingOptions[i].setToolTipText(hints[i]);
            debuggingOptions[i].addItemListener(this);
            add(debuggingOptions[i], c);

              if (i == 2) {
                 c.gridx++;
                add(logsDestinationTitle, c);
                c.gridx++;
                add(logsDestination, c);
                c.gridx++;
                add(logsDestinationReset, c);
                c.gridx-=3;
            }
        }


        for (int j = 0; j < javaConsoleItems.length; j++) {
            consoleComboBox.addItem(javaConsoleItems[j]);
            if (config.getProperty(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE).equals(javaConsoleItems[j].getValue())) {
                consoleComboBox.setSelectedIndex(j);
            }
        }
        c.gridy++;
        consoleComboBox.addItemListener(this);
        add(consolePanel, c);
        
        // pack the bottom so that it doesn't change size if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        add(filler, c);
    }

    @Override
    @SuppressWarnings("unchecked")
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
