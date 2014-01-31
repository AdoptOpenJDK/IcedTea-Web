/* AdvancedProxySettingsPane.java -- Provides the panel which can modify proxy settings.
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This is the pane that modifies the proxy settings in more detail.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class AdvancedProxySettingsPane extends JPanel {

    private JDialog parent;
    private DeploymentConfiguration config;

    /** List of properties used by this panel */
    public static String[] properties = { "deployment.proxy.http.host",
            "deployment.proxy.http.port",
            "deployment.proxy.https.host",
            "deployment.proxy.https.port",
            "deployment.proxy.ftp.host",
            "deployment.proxy.ftp.port",
            "deployment.proxy.socks.host",
            "deployment.proxy.socks.port",
            "deployment.proxy.same",
            "deployment.proxy.override.hosts" };
    private String[] fields = new String[properties.length];

    private JComponent defaultFocusComponent = null;

    /**
     * Creates a new instance of the proxy settings panel.
     * 
     * @param parent
     *            JDialog this is associated with.
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public AdvancedProxySettingsPane(JDialog parent, DeploymentConfiguration config) {
        super(new BorderLayout());
        this.parent = parent;
        this.config = config;

        getProperties();
        addComponents();
    }

    /**
     * Place properties into an array, this is so when cancel is hit. We don't
     * overwrite the original values.
     */
    private void getProperties() {
        for (int i = 0; i < fields.length; i++) {
            fields[i] = this.config.getProperty(properties[i]);
        }
    }

    /**
     * Add the components to the panel.
     */
    private void addComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel servers = new NamedBorderPanel(Translator.R("APSServersPanel"));
        servers.setLayout(new GridBagLayout());

        JLabel type = new JLabel(Translator.R("APSProxyTypeLabel"));
        JLabel proxyAddress = new JLabel(Translator.R("APSProxyAddressLabel"));
        JLabel port = new JLabel(Translator.R("APSProxyPortLabel"));

        // This addresses the HTTP proxy settings.
        JLabel http = new JLabel(Translator.R("APSLabelHTTP") + ":");
        final JTextField httpAddressField = new JTextField(fields[0]);
        final JTextField httpPortField = new JTextField();
        httpPortField.setDocument(NetworkSettingsPanel.getPortNumberDocument());
        httpAddressField.getDocument().addDocumentListener(new DocumentAdapter(fields, 0));
        httpPortField.getDocument().addDocumentListener(new DocumentAdapter(fields, 1));
        httpPortField.setText(fields[1]);

        // This addresses the HTTPS proxy settings.
        JLabel secure = new JLabel(Translator.R("APSLabelSecure") + ":");
        final JTextField secureAddressField = new JTextField(fields[2]);
        final JTextField securePortField = new JTextField();
        securePortField.setDocument(NetworkSettingsPanel.getPortNumberDocument());
        secureAddressField.getDocument().addDocumentListener(new DocumentAdapter(fields, 2));
        securePortField.getDocument().addDocumentListener(new DocumentAdapter(fields, 3));
        securePortField.setText(fields[3]);

        // This addresses the FTP proxy settings.
        JLabel ftp = new JLabel(Translator.R("APSLabelFTP") + ":");
        final JTextField ftpAddressField = new JTextField(fields[4]);
        final JTextField ftpPortField = new JTextField();
        ftpPortField.setDocument(NetworkSettingsPanel.getPortNumberDocument());
        ftpAddressField.getDocument().addDocumentListener(new DocumentAdapter(fields, 4));
        ftpPortField.getDocument().addDocumentListener(new DocumentAdapter(fields, 5));
        ftpPortField.setText(fields[5]);

        // This addresses the Socks proxy settings.
        JLabel socks = new JLabel(Translator.R("APSLabelSocks") + ":");
        final JTextField socksAddressField = new JTextField(fields[6]);
        final JTextField socksPortField = new JTextField();
        socksPortField.setDocument(NetworkSettingsPanel.getPortNumberDocument());
        socksAddressField.getDocument().addDocumentListener(new DocumentAdapter(fields, 6));
        socksPortField.getDocument().addDocumentListener(new DocumentAdapter(fields, 7));
        socksPortField.setText(fields[7]);

        JCheckBox sameProxyForAll = new JCheckBox(Translator.R("APSSameProxyForAllProtocols"), Boolean.parseBoolean(fields[8]));
        sameProxyForAll.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                fields[8] = String.valueOf(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        JPanel p = new JPanel();
        BoxLayout bl = new BoxLayout(p, BoxLayout.Y_AXIS);
        p.setLayout(bl);
        p.add(sameProxyForAll);

        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        servers.add(type, c);
        c.gridwidth = 2;
        c.weightx = 1;
        c.gridx = 1;
        servers.add(proxyAddress, c);
        c.gridwidth = 1;
        c.weightx = 1;
        c.gridx = 4;
        servers.add(port, c);

        plant(1, http, httpAddressField, httpPortField, servers, c);
        plant(2, secure, secureAddressField, securePortField, servers, c);
        plant(3, ftp, ftpAddressField, ftpPortField, servers, c);
        plant(4, socks, socksAddressField, socksPortField, servers, c);
        c.gridwidth = 5;
        c.gridx = 0;
        c.gridy = 5;
        servers.add(p, c);

        JPanel exceptions = new NamedBorderPanel(Translator.R("APSExceptionsLabel"));
        exceptions.setLayout(new BorderLayout());
        JLabel exceptionDescription = new JLabel(Translator.R("APSExceptionsDescription"));
        final JTextArea exceptionListArea = new JTextArea();
        exceptionListArea.setLineWrap(true);
        exceptionListArea.setText(fields[9]);
        exceptionListArea.getDocument().addDocumentListener(new DocumentAdapter(fields, 9));

        JLabel exceptionFormat = new JLabel(Translator.R("APSExceptionInstruction"));
        JScrollPane exceptionScroll = new JScrollPane(exceptionListArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        exceptions.add(exceptionDescription, BorderLayout.NORTH);
        exceptions.add(exceptionScroll, BorderLayout.CENTER);
        exceptions.add(exceptionFormat, BorderLayout.SOUTH);

        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = 0;
        topPanel.add(servers, c);
        c.weighty = 1;
        c.gridy = 1;
        topPanel.add(exceptions, c);

        this.add(topPanel);
        this.add(createButtonPanel(), BorderLayout.SOUTH);

    }

    /**
     * Helper method to help make adding component shorter.
     */
    private void plant(int y, JLabel label, JTextField addr, JTextField port, JPanel addTo, GridBagConstraints c) {
        c.gridy = y;

        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        addTo.add(label, c);
        c.gridwidth = 2;
        c.weightx = 1;
        c.gridx = 1;
        addTo.add(addr, c);
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 3;
        addTo.add(new JLabel(":"), c);
        c.gridwidth = 1;
        c.weightx = 0.3;
        c.gridx = 4;
        addTo.add(port, c);
    }

    /**
     * Make the button panel.
     * 
     * @return the button panel created
     * @see JPanel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        List<JButton> buttons = new ArrayList<JButton>();

        JButton okButton = new JButton(Translator.R("ButOk"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < fields.length; i++)
                    config.setProperty(properties[i], fields[i]);

                parent.dispose();
            }
        });
        buttons.add(okButton);

        JButton cancelButton = new JButton(Translator.R("ButCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
            }
        });
        buttons.add(cancelButton);

        int maxWidth = 0;
        int maxHeight = 0;
        for (JButton button : buttons) {
            maxWidth = Math.max(button.getMinimumSize().width, maxWidth);
            maxHeight = Math.max(button.getMinimumSize().height, maxHeight);
        }

        int wantedWidth = maxWidth + 10;
        int wantedHeight = maxHeight;
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(wantedWidth, wantedHeight));
            buttonPanel.add(button);
        }

        return buttonPanel;
    }

    /**
     * Put focus onto default button.
     */
    public void focusOnDefaultButton() {
        if (defaultFocusComponent != null) {
            defaultFocusComponent.requestFocusInWindow();
        }
    }
}
