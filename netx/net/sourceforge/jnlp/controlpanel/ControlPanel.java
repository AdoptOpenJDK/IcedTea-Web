/* ControlPanel.java -- Display the control panel for modifying deployment settings.
Copyright (C) 2011 Red Hat

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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.controlpanel.JVMPanel.JvmValidationResult;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.security.viewer.CertificatePane;
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * This is the control panel for Java. It provides a GUI for modifying the
 * deployments.properties file.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class ControlPanel extends JFrame {
    private JVMPanel jvmPanel;

    /**
     * Class for keeping track of the panels and their associated text.
     * 
     * @author @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
     * 
     */
    private static class SettingsPanel {
        final String value;
        final JPanel panel;

        public SettingsPanel(String value, JPanel panel) {
            this.value = value;
            this.panel = panel;
        }

        public JPanel getPanel() {
            return panel;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private DeploymentConfiguration config = null;

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
        setIconImages(ImageResources.INSTANCE.getApplicationImages());

        this.config = config;

        JPanel topPanel = createTopPanel();
        JPanel mainPanel = createMainSettingsPanel();
        JPanel buttonPanel = createButtonPanel();

        add(topPanel, BorderLayout.PAGE_START);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    private JPanel createTopPanel() {
        Font currentFont;
        JLabel about = new JLabel(R("CPMainDescriptionShort"));
        currentFont = about.getFont();
        about.setFont(currentFont.deriveFont(currentFont.getSize2D() + 2));
        currentFont = about.getFont();
        about.setFont(currentFont.deriveFont(Font.BOLD));

        JLabel description = new JLabel(R("CPMainDescriptionLong"));
        description.setBorder(new EmptyBorder(2, 0, 2, 0));

        JPanel descriptionPanel = new JPanel(new GridLayout(0, 1));
        descriptionPanel.setBackground(UIManager.getColor("TextPane.background"));
        descriptionPanel.add(about);
        descriptionPanel.add(description);

        JLabel image = new JLabel();

        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        try {
            URL imgUrl = cl.getResource("net/sourceforge/jnlp/resources/netx-icon.png");
            image.setIcon(new ImageIcon(ImageIO.read(imgUrl)));
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getColor("TextPane.background"));
        topPanel.add(descriptionPanel, BorderLayout.LINE_START);
        topPanel.add(image, BorderLayout.LINE_END);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return topPanel;
    }
    
    private int validateJdk() {
        String s = ControlPanel.this.config.getProperty(DeploymentConfiguration.KEY_JRE_DIR);
        JvmValidationResult validationResult = JVMPanel.validateJvm(s);
        if (validationResult.id == JvmValidationResult.STATE.NOT_DIR
                || validationResult.id == JvmValidationResult.STATE.NOT_VALID_DIR
                || validationResult.id == JvmValidationResult.STATE.NOT_VALID_JDK) {
            return JOptionPane.showConfirmDialog(ControlPanel.this,
                    "<html>"+Translator.R("CPJVMNotokMessage1", s)+"<br/>"
                    + validationResult.formattedText+"<br/>"
                    + Translator.R("CPJVMNotokMessage2", DeploymentConfiguration.KEY_JRE_DIR, PathsAndFiles.USER_DEPLOYMENT_FILE.getFullPath(config))+"</html>",
                    Translator.R("CPJVMconfirmInvalidJdkTitle"),JOptionPane.OK_CANCEL_OPTION);
        }
        return JOptionPane.OK_OPTION;
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
                int validationResult = validateJdk();
                if (validationResult!= JOptionPane.OK_OPTION){
                    return;
                }
                ControlPanel.this.dispose();
            }
        });
        buttons.add(okButton);

        JButton applyButton = new JButton(Translator.R("ButApply"));
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ControlPanel.this.saveConfiguration();
                int validationResult = validateJdk();
                if (validationResult != JOptionPane.OK_OPTION) {
                    int i = JOptionPane.showConfirmDialog(ControlPanel.this,
                            Translator.R("CPJVMconfirmReset"),
                            Translator.R("CPJVMconfirmReset"), JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        jvmPanel.resetTestFieldArgumentsExec();
                    }
                }
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
        jvmPanel =  (JVMPanel) createJVMSettingsPanel();
        SettingsPanel[] panels = new SettingsPanel[] { new SettingsPanel(Translator.R("CPTabAbout"), createAboutPanel()),
                new SettingsPanel(Translator.R("CPTabCache"), createCacheSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabCertificate"), createCertificatesSettingsPanel()),
                // TODO: This is commented out since this is not implemented yet
                // new SettingsPanel(Translator.R("CPTabClassLoader"), createClassLoaderSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabDebugging"), createDebugSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabDesktopIntegration"), createDesktopSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabJVMSettings"),jvmPanel),
                new SettingsPanel(Translator.R("CPTabNetwork"), createNetworkSettingsPanel()),
                // TODO: This is commented out since this is not implemented yet
                // new SettingsPanel(Translator.R("CPTabRuntimes"), createRuntimesSettingsPanel()),
                new SettingsPanel(Translator.R("CPTabSecurity"), createSecuritySettingsPanel()),
                //todo refactor to work with tmp file and apply as asu designed it
                new SettingsPanel(Translator.R("CPTabPolicy"), createPolicySettingsPanel()),
                new SettingsPanel(Translator.R("APPEXTSECControlPanelExtendedAppletSecurityTitle"), new UnsignedAppletsTrustingListPanel(PathsAndFiles.APPLET_TRUST_SETTINGS_SYS.getFile(), PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), this.config))
        };

        // Add panels.
        final JPanel settingsPanel = new JPanel(new CardLayout());

        // Calculate largest minimum size we should use.
        int height = 0;
        int width = 0;
        for (SettingsPanel panel : panels) {
            JPanel p = panel.getPanel();
            Dimension d = p.getMinimumSize();
            if (d.height > height) {
                height = d.height;
            }
            if (d.width > width) {
                width = d.width;
            }
        }
        Dimension dim = new Dimension(width, height);

        for (SettingsPanel panel : panels) {
            JPanel p = panel.getPanel();
            p.setPreferredSize(dim);
            settingsPanel.add(p, panel.toString());
        }

        final JList<SettingsPanel> settingsList = new JList<>(panels);
        settingsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                @SuppressWarnings("unchecked")
                JList<SettingsPanel> list = (JList<SettingsPanel>) e.getSource();
                SettingsPanel panel = list.getSelectedValue();
                CardLayout cl = (CardLayout) settingsPanel.getLayout();
                cl.show(settingsPanel, panel.toString());
            }
        });
        JScrollPane settingsListScrollPane = new JScrollPane(settingsList);
        settingsListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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

    private JPanel createPolicySettingsPanel() {
        return new PolicyPanel(this, this.config);
    }

    private JPanel createJVMSettingsPanel() {
        return new JVMPanel(this.config);
    }

    /**
     * This is a placeholder panel.
     * 
     * @return a placeholder panel
     * @see JPanel
     */
    private JPanel createNotImplementedPanel() {

        JPanel notImplementedPanel = new NamedBorderPanel("Unimplemented");
        notImplementedPanel.setLayout(new BorderLayout());

        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        URL imgUrl = cl.getResource("net/sourceforge/jnlp/resources/warning.png");
        Image img;
        try {
            img = ImageIO.read(imgUrl);
            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel("Not Implemented", icon, SwingConstants.CENTER);
            notImplementedPanel.add(label);
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
        return notImplementedPanel;
    }

    /**
     * Save the configuration changes.
     */
    private void saveConfiguration() {
        try {
            config.save();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            JOptionPane.showMessageDialog(this, e);
        }
    }

    public static void main(String[] args) throws Exception {
        DeploymentConfiguration.move14AndOlderFilesTo15StructureCatched();
        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (ConfigurationException e) {
            // FIXME inform user about this and exit properly
            // the only known condition under which this can happen is when a
            // required system configuration file is not found

            // if configuration is not loaded, we will get NullPointerExceptions
            // everywhere
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore; not a big deal
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
