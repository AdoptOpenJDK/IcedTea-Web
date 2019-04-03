/* NetworkSettingsPanel.java -- Sets proxy settings for network.
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This is the pane used with creating a JDialog version. This allows changing
 * the network configuration: Proxy
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
@SuppressWarnings("serial")
public class NetworkSettingsPanel extends JPanel implements ActionListener {

    private final DeploymentConfiguration config;

    private JPanel description;
    private final ArrayList<JPanel> proxyPanels = new ArrayList<>(); // The stuff with editable fields

    /** List of properties used by this panel */
    public static String[] properties = {
        DeploymentConfiguration.KEY_PROXY_TYPE,
        DeploymentConfiguration.KEY_PROXY_HTTP_HOST,
        DeploymentConfiguration.KEY_PROXY_HTTP_PORT,
        DeploymentConfiguration.KEY_PROXY_BYPASS_LOCAL,
        DeploymentConfiguration.KEY_PROXY_AUTO_CONFIG_URL
    };

    /**
     * Creates a new instance of the network settings panel.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public NetworkSettingsPanel(DeploymentConfiguration config) {
        super();
        this.config = config;
        setLayout(new BorderLayout());

        addComponents();
    }

    /**
     * This adds the components to the panel.
     */
    protected void addComponents() {
        JPanel settingPanel = new NamedBorderPanel(Translator.R("CPHeadNetworkSettings"));
        settingPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;

        JLabel networkDesc = new JLabel("<html>" + Translator.R("CPNetworkSettingsDescription") + "<hr /></html>");

        JLabel[] description = { new JLabel("<html>" + Translator.R("NSDescription-1") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription0") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription1") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription2") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription3") + "</html>") };

        this.description = new JPanel(new CardLayout());
        for (int i = 0; i < description.length; i++)
            this.description.add(description[i], String.valueOf(i - 1));

        // Settings for selecting Proxy Server
        JPanel proxyServerPanel = new JPanel(new GridLayout(0, 1));
        JPanel proxyLocationPanel = new JPanel(new GridBagLayout());
        JPanel proxyBypassPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        JLabel addressLabel = new JLabel(Translator.R("NSAddress") + ":");
        JLabel portLabel = new JLabel(Translator.R("NSPort") + ":");
        final JTextField addressField = new JTextField(config.getProperty(properties[1]), 10);
        addressField.getDocument().addDocumentListener(new DocumentAdapter(config, properties[1]));

        final JTextField portField = new JTextField(5);
        portField.setDocument(NetworkSettingsPanel.getPortNumberDocument());
        portField.getDocument().addDocumentListener(new DocumentAdapter(config, properties[2]));
        portField.setText(config.getProperty(properties[2]));

        // Create the button which allows setting of other types of proxy.
        JButton advancedProxyButton = new JButton(Translator.R("NSAdvanced") + "...");
        advancedProxyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdvancedProxySettingsDialog.showAdvancedProxySettingsDialog(config);
                addressField.setText(config.getProperty(properties[1]));
                portField.setText(config.getProperty(properties[2]));
            }
        });

        JCheckBox bypassCheckBox = new JCheckBox(Translator.R("NSBypassLocal"), Boolean.parseBoolean(config.getProperty(properties[3])));
        bypassCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                config.setProperty(properties[3], String.valueOf(e.getStateChange() == ItemEvent.SELECTED));
            }
        });
        c.gridy = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 0;
        proxyLocationPanel.add(Box.createHorizontalStrut(20), c);
        proxyLocationPanel.add(addressLabel, c);
        c.weightx = 1;
        proxyLocationPanel.add(addressField, c);
        c.weightx = 0;
        proxyLocationPanel.add(portLabel, c);
        c.weightx = 1;
        proxyLocationPanel.add(portField, c);
        c.weightx = 0;
        proxyLocationPanel.add(advancedProxyButton, c);
        proxyBypassPanel.add(Box.createHorizontalStrut(5));
        proxyBypassPanel.add(bypassCheckBox);

        proxyServerPanel.add(proxyLocationPanel);
        proxyServerPanel.add(proxyBypassPanel);

        JRadioButton directConnection = new JRadioButton(Translator.R("NSDirectConnection"), config.getProperty(properties[0]).equals("0"));
        directConnection.setActionCommand("0");
        directConnection.addActionListener(this);

        JRadioButton useProxyServer = new JRadioButton(Translator.R("NSManualProxy"), config.getProperty(properties[0]).equals("1"));
        useProxyServer.setActionCommand("1");
        useProxyServer.addActionListener(this);

        JRadioButton useAutoProxyConfigScript = new JRadioButton(Translator.R("NSAutoProxy"), config.getProperty(properties[0]).equals("2"));
        useAutoProxyConfigScript.setActionCommand("2");
        useAutoProxyConfigScript.addActionListener(this);

        JRadioButton useBrowserSettings = new JRadioButton(Translator.R("NSBrowserProxy"), config.getProperty(properties[0]).equals("3"));
        useBrowserSettings.setActionCommand("3");
        useBrowserSettings.addActionListener(this);

        ButtonGroup modeSelect = new ButtonGroup();
        modeSelect.add(useBrowserSettings);
        modeSelect.add(useProxyServer);
        modeSelect.add(useAutoProxyConfigScript);
        modeSelect.add(directConnection);

        // Settings for Automatic Proxy Configuration Script
        JPanel proxyAutoPanel = new JPanel(new GridBagLayout());
        JLabel locationLabel = new JLabel(Translator.R("NSScriptLocation") + ":");
        final JTextField locationField = new JTextField(config.getProperty(properties[4]), 20);
        locationField.getDocument().addDocumentListener(new DocumentAdapter(config, properties[4]));

        c.gridx = 0;
        proxyAutoPanel.add(Box.createHorizontalStrut(20), c);
        c.gridx = GridBagConstraints.RELATIVE;
        proxyAutoPanel.add(locationLabel, c);
        c.weightx = 1;
        proxyAutoPanel.add(locationField, c);

        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        settingPanel.add(networkDesc, c);
        c.gridy = 1;
        settingPanel.add(this.description, c);
        c.gridy = 2;
        settingPanel.add(directConnection, c);
        c.gridy = 3;
        settingPanel.add(useBrowserSettings, c);
        c.gridy = 4;
        settingPanel.add(useProxyServer, c);
        c.gridy = 5;
        settingPanel.add(proxyServerPanel, c);
        proxyPanels.add(proxyServerPanel);
        c.gridy = 6;
        settingPanel.add(useAutoProxyConfigScript, c);
        c.gridy = 7;
        settingPanel.add(proxyAutoPanel, c);
        proxyPanels.add(proxyAutoPanel);

        // Filler to pack the bottom of the panel.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        settingPanel.add(filler, c);

        setState(); // depending on default setting we will enable or disable

        add(settingPanel, BorderLayout.CENTER);

    }

    /**
     * Enable/Disable the panel and all its children recursively.
     * 
     * @param panel
     *            JPanel which needs to be enabled or disabled.
     * @param enable
     *            true if the panel and its children are to be enabled, false
     *            otherwise.
     */
    private void enablePanel(JPanel panel, boolean enable) {
        // This will be used to enable all components in this panel recursively.
        // Ridiculously slow if lots of nested panels.
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                enablePanel((JPanel) c, enable);
            }
            c.setEnabled(enable);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        config.setProperty(properties[0], e.getActionCommand());
        setState();
    }

    /**
     * This enables and disables the appropriate panels.
     */
    private void setState() {
        ((CardLayout) this.description.getLayout()).show(this.description, config.getProperty(properties[0]));
        switch (config.getProperty(properties[0])) {
            case "0":
                for (JPanel panel : proxyPanels)
                    enablePanel(panel, false);
                break;
            case "1":
                enablePanel(proxyPanels.get(1), false);
                enablePanel(proxyPanels.get(0), true);
                break;
            case "2":
                enablePanel(proxyPanels.get(0), false);
                enablePanel(proxyPanels.get(1), true);
                break;
            case "3":
                for (JPanel panel : proxyPanels)
                    enablePanel(panel, false);
                break;
        }
    }
    
    /**
     * Creates a PlainDocument that only take numbers if it will create a valid port number.
     * @return PlainDocument which will ensure numeric values only and is a valid port number.
     */
    public static PlainDocument getPortNumberDocument(){
        return new PlainDocument(){
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str != null) {
                    try {
                        Integer.valueOf(str);
                        int val = Integer.valueOf(this.getText(0, this.getLength()) + str);
                        if (val < 1 || val > 65535) { // Invalid port number if true
                            throw new NumberFormatException("Invalid port number");
                        }
                        super.insertString(offs, str, a);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, Translator.R("CPInvalidPort"), Translator.R("CPInvalidPortTitle")
                                , JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        };
    }
}
