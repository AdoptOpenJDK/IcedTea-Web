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

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
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
public class TemporaryInternetFilesPanel extends NamedBorderPanel {

    private static final Long CACHE_UNLIMITED_SIZE = -1l;
    private static final Long CACHE_MIN_SIZE = 0l;
    private static final Long CACHE_MAX_SIZE = (long) Integer.MAX_VALUE;
    private static final Long SPINNER_STEP_SIZE = 10l;
    private final JSpinner cacheSizeSpinner;

    private static final long BYTES_TO_MEGABYTES = 1024l * 1024l;

    private final JCheckBox limitCacheSizeCheckBox;
    private final JLabel cacheSizeWarningLabel;
    private final DeploymentConfiguration config;
    private final JComboBox<ComboItem> cbCompression;
    private final JButton bLocation;
    private final JButton resetLocation;
    private final JTextField location;
    private final JLabel locationDescription;
    private final JLabel lCompression;
    private final JLabel lCacheSize;
    private final JButton bViewFiles;
    private final JPanel diskSpacePanel;

    public TemporaryInternetFilesPanel(final DeploymentConfiguration config) {
        super(Translator.R("CPHeadTempInternetFiles"));
        this.config = config;
        setLayout(new BorderLayout());
        cacheSizeSpinner = new JSpinner();
        limitCacheSizeCheckBox = new JCheckBox(Translator.R("TIFPLimitCacheSize"));
        cacheSizeWarningLabel = new JLabel();
        lCacheSize = new JLabel(Translator.R("TIFPCacheSize") + ":");

        ComboItem[] compressionOptions = {new ComboItem(Translator.R("TIFPNone"), "0"),
                new ComboItem("1", "1"),
                new ComboItem("2", "2"),
                new ComboItem("3", "3"),
                new ComboItem("4", "4"),
                new ComboItem("5", "5"),
                new ComboItem("6", "6"),
                new ComboItem("7", "7"),
                new ComboItem("8", "8"),
                new ComboItem(Translator.R("TIFPMax"), "9"),};
        cbCompression = new JComboBox<>(compressionOptions);
        lCompression = new JLabel(Translator.R("TIFPCompressionLevel") + ":"); // Sets compression level for jar files.

        bLocation = new JButton(Translator.R("TIFPChange") + "...");
        resetLocation = new JButton(Translator.R("CPFilesLogsDestDirResert"));
        location = new JTextField(PathsAndFiles.CACHE_DIR.getFullPath(config));
        locationDescription = new JLabel(Translator.R("TIFPLocationLabel") + ":");
        bViewFiles = new JButton(Translator.R("TIFPViewFiles"));

        diskSpacePanel = new JPanel();
        diskSpacePanel.setLayout(new GridBagLayout());

        addComponents();
        if (limitCacheSizeCheckBox.isSelected()) {
            showCacheSizeSpinnerGUIElements(true);

            if (parseLong(cacheSizeSpinner.getValue().toString()) == 0) {
                showCompressionAndLocationGUIElements(false);
            } else {
                showCompressionAndLocationGUIElements(true);
            }

        } else {
            showCacheSizeSpinnerGUIElements(false);
            showCompressionAndLocationGUIElements(true);
        }
    }

    /**
     * Add components to panel.
     */
    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel description = new JLabel(Translator.R("CPTempInternetFilesDescription"));

        // This section deals with how to use the disk space.
        cbCompression.setSelectedIndex(parseInt(this.config.getProperty(DeploymentConfiguration.KEY_CACHE_COMPRESSION_ENABLED)));
        cbCompression.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                config.setProperty(DeploymentConfiguration.KEY_CACHE_COMPRESSION_ENABLED, ((ComboItem) e.getItem()).getValue());
            }
        });

        //Override getNextValue and getPreviousValue to make it jump to the closest increment/decrement of step size
        final Long configCacheSize = parseLong(this.config.getProperty(DeploymentConfiguration.KEY_CACHE_MAX_SIZE));
        final Long initialCacheSize = configCacheSize < CACHE_MIN_SIZE ? CACHE_MIN_SIZE : configCacheSize;
        final SpinnerNumberModel snmCacheSize = new PowerOfSpinnerNumberModel(initialCacheSize, TemporaryInternetFilesPanel.CACHE_MIN_SIZE, TemporaryInternetFilesPanel.CACHE_MAX_SIZE, TemporaryInternetFilesPanel.SPINNER_STEP_SIZE);
        cacheSizeSpinner.setModel(snmCacheSize);

        final SpinnerChangeListener listener = new SpinnerChangeListener();
        cacheSizeSpinner.addChangeListener(listener);
        cacheSizeSpinner.setToolTipText(Translator.R("TIFPCacheSizeSpinnerTooltip", CACHE_MIN_SIZE, CACHE_MAX_SIZE));

        limitCacheSizeCheckBox.setSelected(configCacheSize >= CACHE_MIN_SIZE);
        limitCacheSizeCheckBox.addItemListener(new CheckboxItemListener());


        c.gridx = 0;
        c.weightx = 1;
        c.gridy = 0;
        diskSpacePanel.add(limitCacheSizeCheckBox, c);
        c.gridy = 1;
        diskSpacePanel.add(lCacheSize, c);
        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        diskSpacePanel.add(cacheSizeSpinner, c);
        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = 0;
        diskSpacePanel.add(cacheSizeWarningLabel, c);

        c.gridx = 0;
        c.gridy = 3;
        diskSpacePanel.add(lCompression, c);
        c.gridx = 1;
        c.gridwidth = 2;
        diskSpacePanel.add(cbCompression, c);
        resetLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                location.setText(PathsAndFiles.CACHE_DIR.getDefaultFullPath());
                //background engine is clever. Will reset for us if it will see default path here
                PathsAndFiles.CACHE_DIR.setValue(PathsAndFiles.CACHE_DIR.getDefaultFullPath(), config); 
                showCacheSizeSpinnerGUIElements(limitCacheSizeCheckBox.isSelected());
            }
        });
        // This displays the option for changing location of cache
        // User can NOT edit the text field must do it through dialog.
        location.setEditable(false); // Can not c&p into the location field.
        bLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(location.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setFileHidingEnabled(false);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setDialogTitle(Translator.R("TIFPLocationLabel"));
                if (fileChooser.showDialog(null, Translator.R("TIFPFileChooserChooseButton")) == JFileChooser.APPROVE_OPTION) {
                    // Check if we have permission to write to that location.
                    String result = fileChooser.getSelectedFile().getAbsolutePath();
                    File dirLocation = new File(result);
                    boolean canWrite = dirLocation.canWrite();
                    while (!canWrite && dirLocation != null) { // File does not exist, or no permission.

                        if (dirLocation.exists()) {
                            JOptionPane.showMessageDialog(null, "No permission to write to this location.");
                            return;
                        }

                        dirLocation = dirLocation.getParentFile();
                        canWrite = dirLocation.canWrite();
                    }

                    if (canWrite) {
                        location.setText(result);
                        PathsAndFiles.CACHE_DIR.setValue(result, config);
                        showCacheSizeSpinnerGUIElements(limitCacheSizeCheckBox.isSelected());
                    }
                }
            }
        });

        bViewFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CacheViewer.showCacheDialog(config);
            }
        });

        c.gridy = 4;
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        diskSpacePanel.add(locationDescription, c);
        c.gridy = 5;
        c.gridwidth = 1;
        diskSpacePanel.add(location, c);
        c.gridx = 1;
        c.weightx = 0.5;
        diskSpacePanel.add(resetLocation, c);
        c.gridx = 2;
        c.weightx = 0.5;
        diskSpacePanel.add(bLocation, c);
        c.gridx = 3;
        diskSpacePanel.add(bViewFiles, c);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(description, BorderLayout.NORTH);
        panel.add(diskSpacePanel, BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent componentEvent) {
                listener.stateChanged(null);
            }
        });

    }

    private long getCurrentUsableSpace() {
        long usableDiskSpace = new File(PathsAndFiles.CACHE_DIR.getFullPath(config)).getUsableSpace() / BYTES_TO_MEGABYTES; // getUsableSpace returns bytes
        return usableDiskSpace;
    }

    private static class PowerOfSpinnerNumberModel extends SpinnerNumberModel {
        private final List<Long> powersOf;

        public PowerOfSpinnerNumberModel(final Long initialCacheSize, final Long cacheMinSize, final Long cacheMaxSize, final Long spinnerStepSize) {
            super(initialCacheSize, cacheMinSize, cacheMaxSize, spinnerStepSize);
            powersOf = new ArrayList<>();
            final int powersListSize = (int) Math.floor(Math.log(cacheMaxSize) / Math.log(spinnerStepSize) + 1);

            for (int i = 0; i < powersListSize; i++) {
                final long powerOfTen = (long) Math.pow(spinnerStepSize, i);
                powersOf.add(powerOfTen);
            }
        }

        @Override
        public Long getNextValue() {
            final Number raw = (Number) super.getValue();
            if (super.getNextValue() == null) {
                return (Long) getMaximum();
            }

            final Long original = raw.longValue();
            final Long result = original - (original % powersOf.get(String.valueOf(original).length() - 1)) + powersOf.get(String.valueOf(original).length() - 1);
            if (result < (Long) getMaximum()) {
                return result;
            }

            return (Long) getMaximum();
        }

        @Override
        public Long getPreviousValue() {
            final Number raw = (Number) super.getValue();
            final Long original = raw.longValue();
            if (super.getPreviousValue() == null) {
                if (original > 0) {
                    return original - 1;
                }

                return (Long) getMinimum();
            }

            final Long result;
            if (powersOf.contains(original)) {
                result = original - powersOf.get(String.valueOf(original).length() - 2);
                return result;
            } else {

                if (original % powersOf.get(String.valueOf(original).length() - 1) == 0) {
                    result = original - powersOf.get(String.valueOf(original).length() - 1);
                } else {
                    result = original - original % powersOf.get(String.valueOf(original).length() - 1);
                }

                if (result > Long.valueOf(0)) {
                    return result;
                }
            }

            return Long.valueOf(0);
        }

    }

    private class SpinnerChangeListener implements ChangeListener {

        @Override
        public void stateChanged(final ChangeEvent e) {
            final long usableDiskSpace = getCurrentUsableSpace();
            final long cacheSizeSpinnerValue = (long) cacheSizeSpinner.getValue();

            if (limitCacheSizeCheckBox.isSelected()) {
                showCompressionAndLocationGUIElements(true);

                if (cacheSizeSpinnerValue > usableDiskSpace) {
                    cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSpinnerValueTooLargeWarning", usableDiskSpace));
                } else if (cacheSizeSpinnerValue == 0) {
                    cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSetToNoCaching"));
                    showCompressionAndLocationGUIElements(false);
                } else {
                    cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSpinnerLargeValueWarning", usableDiskSpace));
                }

                config.setProperty(DeploymentConfiguration.KEY_CACHE_MAX_SIZE, Long.valueOf(cacheSizeSpinnerValue).toString());
            } else {
                showCacheSizeSpinnerGUIElements(false);
                showCompressionAndLocationGUIElements(true);
            }
        }
    }

    private class CheckboxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(final ItemEvent e) {
            final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            showCacheSizeSpinnerGUIElements(selected);

            if (parseLong(cacheSizeSpinner.getValue().toString()) == 0 && selected) {
                showCompressionAndLocationGUIElements(false);
            } else {
                showCompressionAndLocationGUIElements(true);
            }

            if (selected) {
                config.setProperty(DeploymentConfiguration.KEY_CACHE_MAX_SIZE, cacheSizeSpinner.getValue().toString());
            } else {
                config.setProperty(DeploymentConfiguration.KEY_CACHE_MAX_SIZE, Long.toString(CACHE_UNLIMITED_SIZE));
            }

            config.setProperty(DeploymentConfiguration.KEY_CACHE_ENABLED, String.valueOf(!selected));
        }
    }

    private void showCompressionAndLocationGUIElements(boolean bool) {
        cbCompression.setEnabled(bool);
        lCompression.setEnabled(bool);
        resetLocation.setEnabled(bool);
        bLocation.setEnabled(bool);
        location.setEnabled(bool);
        locationDescription.setEnabled(bool);
        bViewFiles.setEnabled(bool);
    }

    private void showCacheSizeSpinnerGUIElements(boolean bool){
        lCacheSize.setEnabled(bool);
        cacheSizeSpinner.setEnabled(bool);
        cacheSizeWarningLabel.setEnabled(bool);
        long usableDiskSpace = getCurrentUsableSpace();
        if(bool == false) {
            cacheSizeSpinner.setToolTipText(null);
            cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSpinnerLargeValueWarning", usableDiskSpace));
        } else {

            cacheSizeSpinner.setToolTipText(Translator.R("TIFPCacheSizeSpinnerTooltip", CACHE_MIN_SIZE, CACHE_MAX_SIZE));
            final long cacheSizeSpinnerValue = (long) cacheSizeSpinner.getValue();
            if(cacheSizeSpinnerValue > usableDiskSpace) {
                cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSpinnerValueTooLargeWarning", usableDiskSpace));
            } else if (cacheSizeSpinnerValue == 0) {
                cacheSizeWarningLabel.setText(Translator.R("TIFPCacheSizeSetToNoCaching"));
            }
        }
    }
}