/* TemporaryInternetFilesPanel.java -- Display and sets cache settings.
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * The actual panel that contains the fields that the user can edit accordingly.
 * This is provided as a pane for inside the Panel itself, can also be used to
 * display as a dialog.
 * TODO: Add functionality:
 *
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 *
 */
@SuppressWarnings("serial")
public class TemporaryInternetFilesPanel extends NamedBorderPanel implements ChangeListener {

    private DeploymentConfiguration config;
    private int minSize = -1;
    private int maxSize = 1000;

    /** List of properties used by this panel */
    public static String[] properties = { "deployment.javapi.cache.enabled", // false == enabled
            "deployment.user.cachedir",
            "deployment.cache.max.size", // Specified in MB
            "deployment.cache.jarcompression", // Allows values 0-9
    };

    private JComponent defaultFocusComponent = null;
    JSpinner spCacheSize;
    JSlider slCacheSize;

    public TemporaryInternetFilesPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadTempInternetFiles"));
        this.config = config;
        setLayout(new BorderLayout());

        addComponents();
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel description = new JLabel("<html>" + Translator.R("CPTempInternetFilesDescription") + "<hr /></html>");

        JCheckBox enableCaching = new JCheckBox(Translator.R("TIFPEnableCache"), !Boolean.parseBoolean(this.config.getProperty(properties[0])));
        enableCaching.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                config.setProperty(properties[0], String.valueOf(!(e.getStateChange() == ItemEvent.SELECTED)));
            }
        });

        // This displays the option for changing location of cache
        // User can NOT edit the text field must do it through dialog.
        JPanel locationPanel = new NamedBorderPanel(Translator.R("TIFPLocation"), new GridBagLayout());
        JLabel locationDescription = new JLabel(Translator.R("TIFPLocationLabel") + ":");
        final JTextField location = new JTextField(this.config.getProperty(properties[1]));
        location.setEditable(false); // Can not c&p into the location field.
        JButton bLocation = new JButton(Translator.R("TIFPChange") + "...");
        bLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    String result = fileChooser.getSelectedFile().getAbsolutePath();
                    location.setText(result);
                    config.setProperty(properties[1], result);
                }
            }
        });

        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 0;
        locationPanel.add(locationDescription, c);
        c.gridwidth = 1;
        c.gridy = 1;
        locationPanel.add(location, c);
        c.gridx = 1;
        c.weightx = 0;
        locationPanel.add(bLocation, c);

        // This section deals with how to use the disk space.
        JPanel diskSpacePanel = new NamedBorderPanel(Translator.R("TIFPDiskSpace"), new GridBagLayout());
        JLabel lCompression = new JLabel(Translator.R("TIFPCompressionLevel")); // Sets compression level for jar files.
        ComboItem[] compressionOptions = { new ComboItem(Translator.R("TIFPNone"), "0"),
                new ComboItem("1", "1"),
                new ComboItem("2", "2"),
                new ComboItem("3", "3"),
                new ComboItem("4", "4"),
                new ComboItem("5", "5"),
                new ComboItem("6", "6"),
                new ComboItem("7", "7"),
                new ComboItem("8", "8"),
                new ComboItem(Translator.R("TIFPMax"), "9"), };
        JComboBox cbCompression = new JComboBox(compressionOptions);
        cbCompression.setSelectedIndex(Integer.parseInt(this.config.getProperty(properties[3])));
        cbCompression.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                config.setProperty(properties[3], ((ComboItem) e.getItem()).getValue());
            }
        });

        JLabel lCacheSize = new JLabel(Translator.R("TIFPCacheSize") + ":");
        slCacheSize = new JSlider(minSize, maxSize, Integer.parseInt(this.config.getProperty(properties[2])));
        slCacheSize.setMinorTickSpacing(50);
        slCacheSize.setPaintTicks(true);
        SpinnerNumberModel snmCacheSize = new SpinnerNumberModel(Integer.parseInt(this.config.getProperty(properties[2])), minSize, maxSize, 1);
        spCacheSize = new JSpinner(snmCacheSize);

        slCacheSize.addChangeListener(this);
        spCacheSize.addChangeListener(this);

        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;
        diskSpacePanel.add(lCompression, c);
        c.gridx = 1;
        c.weightx = 0;
        diskSpacePanel.add(cbCompression, c);
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        diskSpacePanel.add(lCacheSize, c);
        c.gridwidth = 1;
        c.gridy = 2;
        diskSpacePanel.add(slCacheSize, c);
        c.gridx = 1;
        diskSpacePanel.add(spCacheSize, c);

        JPanel buttonDeleteRestore = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        JButton bViewFiles = new JButton(Translator.R("TIFPViewFiles"));
        bViewFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CacheViewer.showCacheDialog(config);
            }
        });
        buttonDeleteRestore.add(bViewFiles);

        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        topPanel.add(enableCaching, c);
        c.gridy = 1;
        topPanel.add(locationPanel, c);
        c.gridy = 2;
        topPanel.add(diskSpacePanel, c);
        c.weighty = 1;
        c.gridy = 3;
        topPanel.add(buttonDeleteRestore, c);
        add(description, BorderLayout.NORTH);
        add(topPanel, BorderLayout.CENTER);
    }

    /**
     * Give focus to the default button.
     */
    public void focusOnDefaultButton() {
        if (defaultFocusComponent != null) {
            defaultFocusComponent.requestFocusInWindow();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof JSlider)
            spCacheSize.setValue(((JSlider) o).getValue());
        else if (o instanceof JSpinner)
            slCacheSize.setValue((Integer) ((JSpinner) o).getValue());

        config.setProperty(properties[2], spCacheSize.getValue().toString());
    }
}
