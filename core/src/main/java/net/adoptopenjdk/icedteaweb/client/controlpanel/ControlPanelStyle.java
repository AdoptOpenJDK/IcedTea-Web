package net.adoptopenjdk.icedteaweb.client.controlpanel;

import javax.swing.JPanel;
import java.awt.Image;
import java.util.List;

/**
 * Provides branding for the control panel dialog
 */
public interface ControlPanelStyle {

    /**
     * Returns the dialog title
     * @return dialog title
     */
    String getDialogTitle();

    /**
     * Returns a new header instance for the dialog
     * @return header instance
     */
    JPanel createHeader();

    /**
     * Returns a list of icons that can be used as dialog icons
     * @return list of icons
     */
    List<? extends Image> getDialogIcons();
}
