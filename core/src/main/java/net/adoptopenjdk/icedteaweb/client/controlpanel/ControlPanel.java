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

package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.JVMPanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.JVMPanel.JvmValidationResult;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ImageResources;

import javax.naming.ConfigurationException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * This is the control panel for Java. It provides a GUI for modifying the
 * deployments.properties file.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class ControlPanel extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(ControlPanel.class);

    private final DeploymentConfiguration config;

    /**
     * Creates a new instance of the ControlPanel.
     * 
     * @param config
     *            Loaded DeploymentsConfiguration file.
     * 
     */
    private ControlPanel(DeploymentConfiguration config) {
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
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
        image.setIcon(new ImageIcon(ImageResources.INSTANCE.getApplicationImages().get(0)));


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getColor("TextPane.background"));
        topPanel.add(descriptionPanel, BorderLayout.LINE_START);
        topPanel.add(image, BorderLayout.LINE_END);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return topPanel;
    }
    
    private int validateJdk() {
        String s = ControlPanel.this.config.getProperty(ConfigurationConstants.KEY_JRE_DIR);
        JvmValidationResult validationResult = JVMPanel.validateJvm(s);
        if (validationResult.id == JvmValidationResult.STATE.NOT_DIR
                || validationResult.id == JvmValidationResult.STATE.NOT_VALID_DIR
                || validationResult.id == JvmValidationResult.STATE.NOT_VALID_JDK) {
            return JOptionPane.showConfirmDialog(ControlPanel.this,
                    "<html>"+Translator.R("CPJVMNotokMessage1", s)+"<br/>"
                    + validationResult.formattedText+"<br/>"
                    + Translator.R("CPJVMNotokMessage2", ConfigurationConstants.KEY_JRE_DIR, PathsAndFiles.USER_DEPLOYMENT_FILE.getFullPath(config))+"</html>",
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

        List<JButton> buttons = new ArrayList<>();

        JButton okButton = new JButton(Translator.R("ButOk"));
        okButton.addActionListener(e -> {
            // TODO: validation of config before saving
            ControlPanel.this.saveConfiguration();
            JNLPRuntime.exit(0);
        });
        buttons.add(okButton);

        JButton applyButton = new JButton(Translator.R("ButApply"));
        applyButton.addActionListener(e -> {
            // TODO: validation of config before saving
            ControlPanel.this.saveConfiguration();
        });
        buttons.add(applyButton);

        JButton cancelButton = new JButton(Translator.R("ButCancel"));
        cancelButton.addActionListener(e -> JNLPRuntime.exit(0));
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
        final Map<String, ControlPanelProvider> providers = new HashMap<>();
        final ServiceLoader<ControlPanelProvider> serviceLoader = ServiceLoader.load(ControlPanelProvider.class);
        serviceLoader.iterator().forEachRemaining(p -> {
            final String name = p.getName();
            if (p.isActive()) {
                if (providers.containsKey(name)) {
                    throw new IllegalStateException("More than 1 active view provider for control panel with name " + name + " found!");
                }
                LOG.debug("Adding view {} to control panel", name);
                providers.put(p.getName(), p);
            } else {
                LOG.debug("Won't add view {} to control panel since it is deactivated", name);
            }
        });

        final Map<String, JComponent> panels = providers.values().stream()
                .sorted(Comparator.comparingInt(ControlPanelProvider::getOrder))
                .collect(Collectors.toMap(ControlPanelProvider::getName, p -> p.createPanel(config), (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, LinkedHashMap::new));

        final CardLayout cardLayout = new CardLayout();
        final JPanel settingsPanel = new JPanel(cardLayout);
        final Dimension minDimension = panels.values().stream()
                .map(JComponent::getMinimumSize)
                .reduce((a, b) -> new Dimension(Math.max(a.width, b.width), Math.max(a.height, b.height)))
                .orElse(new Dimension());
        panels.forEach((name, component) -> {
            component.setPreferredSize(minDimension);
            settingsPanel.add(component, name);
            cardLayout.addLayoutComponent(component, name);
        });
        final JPanel settingsDetailPanel = new JPanel();
        settingsDetailPanel.setLayout(new BorderLayout());
        settingsDetailPanel.add(settingsPanel, BorderLayout.CENTER);
        settingsDetailPanel.setBorder(new EmptyBorder(0, 5, -3, 0));


        final JList<String> settingsList = new JList<>(panels.keySet().toArray(new String[0]));
        settingsList.addListSelectionListener(e -> cardLayout.show(settingsPanel, settingsList.getSelectedValue()));
        settingsList.setSelectedIndex(0);
        final JScrollPane settingsListScrollPane = new JScrollPane(settingsList);
        settingsListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(settingsListScrollPane, BorderLayout.LINE_START);
        mainPanel.add(settingsDetailPanel, BorderLayout.CENTER);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        return mainPanel;
    }

    /**
     * Save the configuration changes.
     */
    private void saveConfiguration() {
        try {
            config.save();
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            JOptionPane.showMessageDialog(this, e);
        }
    }

    public static void main(String[] args) {
        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (ConfigurationException e) {
            // FIXME inform user about this and exit properly
            // the only known condition under which this can happen is when a
            // required system configuration file is not found

            // if configuration is not loaded, we will get NullPointerExceptions
            // everywhere
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore; not a big deal
        }

        SwingUtils.invokeLater(() -> {
            final ControlPanel editor = new ControlPanel(config);
            editor.setVisible(true);
        });
    }
}
