/* CacheViewer.java -- Display the GUI for viewing and deleting cache files.
Copyright (C) 2013 Red Hat

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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.ScreenFinder;

/**
 * This class will provide a visual way of viewing cache.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class CacheViewer extends JDialog {

    private boolean initialized = false;
    private static final String dialogTitle = Translator.R("CVCPDialogTitle");
    private final DeploymentConfiguration config; // Configuration file which contains all the settings.
    CachePane topPanel;

    /**
     * Creates a new instance of the cache viewer.
     *
     * @param config Deployment configuration file.
     */
    public CacheViewer(DeploymentConfiguration config) {
        super((Frame) null, dialogTitle, true); // Don't need a parent.
        this.config = config;
        if (config == null) {
            throw new IllegalArgumentException("config: " + config);
        }
        setIconImages(ImageResources.INSTANCE.getApplicationImages());
        /* Prepare for adding components to dialog box */
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        topPanel = new CachePane(this, this.config);
        contentPane.add(topPanel, c);

        pack();
        this.topPanel.invokeLaterPopulateTable();

        /* Set focus to default button when first activated */
        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    topPanel.focusOnDefaultButton();
                    gotFocus = true;
                }
            }
        };
        addWindowFocusListener(adapter);

        // Add a KeyEventDispatcher to dispatch events when this CacheViewer has focus
        final CacheViewer cacheViewer = this;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            /**
             * Dispatches mainly the {@code KeyEvent.VK_ESCAPE} key event to
             * close the {@code CacheViewer} dialog.
             * @return {@code true} after an {@link KeyEvent#VK_ESCAPE
             * VK_ESCAPE} has been processed, otherwise {@code false}
             * @see KeyEventDispatcher
             */
            public boolean dispatchKeyEvent(final KeyEvent keyEvent) {
                // Check if Esc key has been pressed
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE &&
                    keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                    // Exclude this key event from further processing
                    keyEvent.consume();
                    // Remove this low-level KeyEventDispatcher
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                    // Post close event to CacheViewer dialog
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                            new WindowEvent(cacheViewer, WindowEvent.WINDOW_CLOSING));
                    return true;
                }
                return false;
            }
        });

        initialized = true;
    }

    /**
     * Display the cache viewer.
     *
     * @param config Configuration file.
     */
    public static void showCacheDialog(final DeploymentConfiguration config) {
        CacheViewer psd = new CacheViewer(config);
        psd.setResizable(true);
        psd.centerDialog();
        psd.setVisible(true);
        psd.dispose();
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
        ScreenFinder.centerWindowsToCurrentScreen(this);
    }
}
