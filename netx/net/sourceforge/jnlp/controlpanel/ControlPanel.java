/* ControlPanel.java -- Display the control panel for modifying deployment settings.
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.naming.ConfigurationException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.jnlp.runtime.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.security.viewer.CertificatePane;

/**
 * This is the control panel for Java. It provides a GUI for modifying the
 * deployments.properties file.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class ControlPanel extends JFrame {

    /**
     * Class for keeping track of the panels and their associated text.
     * 
     * @author @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
     * 
     */
    private class SettingsPanel {
        final String value;
        final JPanel panel;

        public SettingsPanel(String value, JPanel panel) {
            this.value = value;
            this.panel = panel;
        }

        public JPanel getPanel() {
            return panel;
        }

        public String toString() {
            return value;
        }
    }

    private DeploymentConfiguration config = null;

    /*
     * actual configuration options
     */
    private String configBrowserCommand = null;

    /**
     * Creates a new instance of the ControlPanel.
     * 
     * @param config
     *            Loaded DeploymentsConfiguration file.
     * 
     */
    public ControlPanel(DeploymentConfiguration config) {
        super();
        setTitle(Translator.R("CPHead"));

        this.config = config;

        JPanel mainPanel = createMainSettingsPanel();
        JPanel buttonPanel = createButtonPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(getPreferredSize());
        setResizable(false);
    }

    /**
     * Creates the "ok" "apply" and "cancel" buttons.
     * 
     * @return A panel with the "ok" "apply" and "cancel" button.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        List<JButton> buttons = new ArrayList<JButton>();

        JButton okButton = new JButton(Translator.R("ButOk"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ControlPanel.this.saveConfiguration();
                ControlPanel.this.dispose();
            }
        });
        buttons.add(okButton);

        JButton applyButton = new JButton(Translator.R("ButApply"));
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ControlPanel.this.saveConfiguration();
            }
        });
        buttons.add(applyButton);

        JButton cancelButton = new JButton(Translator.R("ButCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ControlPanel.this.dispose();
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
        int wantedHeight = maxHeight + 2;
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(wantedWidth, wantedHeight));
            buttonPanel.add(button);
        }

        return buttonPanel;
    }

    /**
     * Add the different settings panels to the GUI.
     * 
     * @return A panel with all the components in place.
     */
    private JPanel createMainSettingsPanel() {

        loadConfiguration();

        SettingsPanel[] panels = new SettingsPanel[] { new SettingsPanel(Translator.R("CPTabAbout"), createAboutPanel()),
                new SettingsPanel(Translator.R("CPTabCache"), createCacheSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabCertificate"), createCertificatesSettingsPanel()),
                //                new SettingsPanel(Translator.R("CPTabClassLoader"), createClassLoaderSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabDebugging"), createDebugSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabDesktopIntegration"), createDesktopSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabNetwork"), createNetworkSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabRuntimes"), createRuntimesSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabSecurity"), createSecuritySettingsPanel()), };

        // Add panels.
        final JPanel settingsPanel = new JPanel(new CardLayout());
        for (SettingsPanel panel : panels) {
            JPanel p = panel.getPanel();
            p.setPreferredSize(new Dimension(530, 360));
            settingsPanel.add(p, panel.toString());
        }

        final JList settingsList = new JList(panels);
        settingsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JList list = (JList) e.getSource();
                SettingsPanel panel = (SettingsPanel) list.getSelectedValue();
                CardLayout cl = (CardLayout) settingsPanel.getLayout();
                cl.show(settingsPanel, panel.toString());
            }
        });
        JScrollPane settingsListScrollPane = new JScrollPane(settingsList);
        settingsListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final JPanel settingsDetailPanel = new JPanel();
        settingsDetailPanel.setLayout(new BorderLayout());
        settingsDetailPanel.add(settingsPanel, BorderLayout.CENTER);
        settingsDetailPanel.setBorder(new EmptyBorder(0, 5, -3, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(settingsListScrollPane, BorderLayout.LINE_START);
        mainPanel.add(settingsDetailPanel, BorderLayout.CENTER);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        settingsList.setSelectedIndex(0);

        return mainPanel;
    }

    private JPanel createAboutPanel() {
        return new AboutPanel();
    }

    private JPanel createCacheSettingsPanel() {
        return new TemporaryInternetFilesPanel(this.config);
    }

    private JPanel createCertificatesSettingsPanel() {
        JPanel p = new NamedBorderPanel(Translator.R("CPHeadCertificates"), new BorderLayout());
        p.add(new CertificatePane(null), BorderLayout.CENTER);
        return p;
    }

    private JPanel createClassLoaderSettingsPanel() {
        return createNotImplementedPanel();
    }

    private JPanel createDebugSettingsPanel() {
        return new DebuggingPanel(this.config);
    }

    private JPanel createDesktopSettingsPanel() {
        return new DesktopShortcutPanel(this.config);
    }

    private JPanel createNetworkSettingsPanel() {
        return new NetworkSettingsPanel(this.config);
    }

    private JPanel createRuntimesSettingsPanel() {
        return new JREPanel();
    }

    private JPanel createSecuritySettingsPanel() {
        return new SecuritySettingsPanel(this.config);
    }

    /**
     * This is a placeholder panel.
     * 
     * @return
     */
    private JPanel createNotImplementedPanel() {

        JPanel notImplementedPanel = new NamedBorderPanel("Unimplemented");
        notImplementedPanel.setLayout(new BorderLayout());

        URL imgUrl = getClass().getClassLoader().getResource("net/sourceforge/jnlp/resources/warning.png");
        Image img;
        try {
            img = ImageIO.read(imgUrl);
            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel("Not Implemented", icon, SwingConstants.CENTER);
            notImplementedPanel.add(label);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notImplementedPanel;
    }

    /**
     * Get the location of the browser.
     */
    private void loadConfiguration() {
        configBrowserCommand = config.getProperty("deployment.browser.path");
        if (configBrowserCommand == null) {
            configBrowserCommand = "";
        }
    }

    /**
     * Save the configuration changes.
     */
    private void saveConfiguration() {
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        JNLPRuntime.initialize(true);
        final DeploymentConfiguration config = JNLPRuntime.getConfiguration();
        try {
            config.load();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ControlPanel editor = new ControlPanel(config);
                editor.setVisible(true);
            }
        });
    }
}
