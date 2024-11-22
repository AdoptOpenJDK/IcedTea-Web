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

package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.client.controlpanel.ComboItem;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.logging.LogConfig;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This displays the options related to debugging.
 *
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 *
 */
public class DebuggingPanel extends NamedBorderPanel implements ItemListener {

    /** List of properties used by checkboxes in this panel */
    public static String[] properties = {
            ConfigurationConstants.KEY_ENABLE_DEBUG_LOGGING,
            ConfigurationConstants.KEY_ENABLE_LOGGING_HEADERS,
            ConfigurationConstants.KEY_ENABLE_LOGGING_OF_JNLP_FILE_CONTENT,
            ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE,
            ConfigurationConstants.KEY_ENABLE_LEGACY_LOGBASEDFILELOG,
            ConfigurationConstants.KEY_ENABLE_APPLICATION_LOGGING_TOFILE,
            ConfigurationConstants.KEY_ENABLE_APPLICATION_LOGGING_TOCONSOLE,
            ConfigurationConstants.KEY_ENABLE_LOGGING_TOSTREAMS,
            ConfigurationConstants.KEY_ENABLE_LOGGING_TOSYSTEMLOG

    };

     private final DeploymentConfiguration config;

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


    private void fileLoggingAct(JCheckBox source, JCheckBox... targets) {
        if (source.isSelected()) {
            for (JCheckBox target : targets) {
                target.setEnabled(true);
            }
        } else {
            for (JCheckBox target : targets) {
                target.setEnabled(false);
            }
        }
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();


        final JLabel debuggingDescription = new JLabel("<html>" + Translator.R("CPDebuggingDescription") + "<hr /><br /></html>");
        final JLabel logsDestinationTitle = new JLabel(Translator.R("CPFilesLogsDestDir")+": ");
        final JTextField logsDestination = new JTextField(PathsAndFiles.LOG_DIR.getFullPath(config));
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
                PathsAndFiles.LOG_DIR.setValue(logsDestination.getText(), config);
            }
        });
        final JButton logsDestinationReset = new JButton(Translator.R("CPFilesLogsDestDirResert"));
        logsDestinationReset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logsDestination.setText(PathsAndFiles.LOG_DIR.getDefaultFullPath());
            }
        });

        final JCheckBox[] debuggingOptions = {
                new JCheckBox(Translator.R("DPEnableLogging")),
                new JCheckBox(Translator.R("DPEnableHeaders")),
                new JCheckBox(Translator.R("DPEnableJnlpContentLogging")),
                new JCheckBox(Translator.R("DPEnableFile")),
                new JCheckBox(Translator.R("DPEnableLegacyFileLog")),
                new JCheckBox(Translator.R("DPEnableClientAppFileLogging")),
                new JCheckBox(Translator.R("DPEnableClientAppConsoleLogging")),
                new JCheckBox(Translator.R("DPEnableStds")),
                new JCheckBox(Translator.R("DPEnableSyslog"))
        };

        debuggingOptions[3].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileLoggingAct(debuggingOptions[3], debuggingOptions[4], debuggingOptions[5]);
            }

        });
        final String[] hints = {
                (Translator.R("DPEnableLoggingHint")),
                (Translator.R("DPEnableHeadersHint")),
                (Translator.R("DPEnableJnlpContentLoggingHint")),
                (Translator.R("DPEnableFileHint", LogConfig.getLogConfig().getIcedteaLogDir())),
                (Translator.R("DPEnableLegacyFileLogHint")),
                (Translator.R("DPEnableClientAppFileLoggingHint")),
                (Translator.R("DPEnableClientAppConsoleLoggingHint")),
                (Translator.R("DPEnableStdsHint")),
                (Translator.R("DPEnableSyslogHint"))
        };

        final ComboItem[] javaConsoleItems = { new ComboItem(Translator.R("DPDisable"), ConfigurationConstants.CONSOLE_DISABLE),
                new ComboItem(Translator.R("DPHide"), ConfigurationConstants.CONSOLE_HIDE),
                new ComboItem(Translator.R("DPShow"), ConfigurationConstants.CONSOLE_SHOW),
                new ComboItem(Translator.R("DPShowPluginOnly"), ConfigurationConstants.CONSOLE_SHOW_PLUGIN),
                new ComboItem(Translator.R("DPShowJavawsOnly"), ConfigurationConstants.CONSOLE_SHOW_JAVAWS) };

        JLabel consoleLabel = new JLabel(Translator.R("DPJavaConsole"));
        JComboBox<ComboItem> consoleComboBox = new JComboBox<>();
        consoleComboBox.setActionCommand(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE); // The property this comboBox affects.

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
            if (i == 3) {
                JLabel space = new JLabel("<html>" + Translator.R("CPDebuggingPossibilites") + ":</html>");
                add(space, c);
                c.gridy++;
            }

            //move  5th and 6th checkbox below  logsDestination
            if (i == 4 || i == 5) {
                c.gridx += 1;
                if (i == 5) {
                    c.gridy--;
                }
            } else {
                c.gridx = 0;
            }
            debuggingOptions[i].setSelected(Boolean.parseBoolean(s));
            debuggingOptions[i].setActionCommand(properties[i]);
            debuggingOptions[i].setToolTipText(hints[i]);
            debuggingOptions[i].addItemListener(this);
            add(debuggingOptions[i], c);

            if (i == 3) {
                c.gridx++;
                JPanel resetTitlePanel = new JPanel(new BorderLayout(10, 0));
                resetTitlePanel.add(logsDestinationReset, BorderLayout.LINE_START);
                resetTitlePanel.add(logsDestinationTitle, BorderLayout.LINE_END);
                add(resetTitlePanel, c);
                c.gridx++;
                add(logsDestination, c);
                c.gridx -= 2;
            }
        }


        for (int j = 0; j < javaConsoleItems.length; j++) {
            consoleComboBox.addItem(javaConsoleItems[j]);
            if (config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE).equals(javaConsoleItems[j].getValue())) {
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
        fileLoggingAct(debuggingOptions[3], debuggingOptions[4], debuggingOptions[5]);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        Object o = e.getSource();

        if (o instanceof JCheckBox) {
            JCheckBox jcb = (JCheckBox) o;
            config.setProperty(jcb.getActionCommand(), String.valueOf(jcb.isSelected()));
        } else if (o instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<ComboItem> jcb = (JComboBox<ComboItem>) o;
            ComboItem c = (ComboItem) e.getItem();
            config.setProperty(jcb.getActionCommand(), c.getValue());
        }

    }
}
