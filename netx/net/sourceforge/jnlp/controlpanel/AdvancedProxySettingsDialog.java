/* AdvancedProxySettingsDialog.java -- Display the dialog for modifying proxy settings.
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.ImageResources;

/**
 * This dialog provides a means for user to edit more of the proxy settings.
 * 
 * @author Andrew Su <asu@redhat.com, andrew.su@utoronto.ca>
 * 
 */
public class AdvancedProxySettingsDialog extends JDialog {

    private boolean initialized = false;
    private static final String dialogTitle = Translator.R("APSDialogTitle");
    private DeploymentConfiguration config; // Configuration file which contains all the settings.

    AdvancedProxySettingsPane topPanel;

    /**
     * Creates a new instance of the proxy settings dialog.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public AdvancedProxySettingsDialog(DeploymentConfiguration config) {
        super((Frame) null, dialogTitle, true); // Don't need a parent.
        setIconImages(ImageResources.INSTANCE.getApplicationImages());

        this.config = config;

        /* Prepare for adding components to dialog box */
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(456, 404));
        setPreferredSize(new Dimension(456, 404));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        topPanel = new AdvancedProxySettingsPane(this, this.config);
        contentPane.add(topPanel, c);

        pack();

        /* Set focus to default button when first activated */
        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    topPanel.focusOnDefaultButton();
                    gotFocus = true;
                }
            }
        };
        addWindowFocusListener(adapter);

        initialized = true;
    }

    /**
     * Check whether the dialog has finished being created.
     * 
     * @return True if dialog is ready to be displayed.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Center the dialog box.
     */
    private void centerDialog() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();

        setLocation((screen.width - dialogSize.width) / 2, (screen.height - dialogSize.height) / 2);
    }

    /**
     * Display the Proxy Settings Dialog.
     * 
     * @param config
     *            A loaded DeploymentConfiguration file.
     */
    public static void showAdvancedProxySettingsDialog(final DeploymentConfiguration config) {
        AdvancedProxySettingsDialog psd = new AdvancedProxySettingsDialog(config);
        psd.setResizable(false);
        psd.centerDialog();
        psd.setVisible(true);
        psd.dispose();
    }
}
