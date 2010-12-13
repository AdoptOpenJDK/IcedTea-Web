/* MiddleClickListener.java -- Update configuration when pasting with middle click.
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sourceforge.jnlp.config.DeploymentConfiguration;

/**
 * When middle click pastes to the textboxes it doesn't register it... This is
 * to fix that problem. Not needed in Windows.
 * 
 * @author Andrew Su <asu@redhat.com, andrew.su@utoronto.ca>
 * 
 */
class MiddleClickListener extends MouseAdapter {

    DeploymentConfiguration config;
    private String property;

    /**
     * Creates a new instance of middle-click listener.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     * @param property
     *            the property in configuration file to edit.
     */
    public MiddleClickListener(DeploymentConfiguration config, String property) {
        this.config = config;
        this.property = property;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        String result = null;
        if (obj instanceof JTextField)
            result = ((JTextField) obj).getText();
        else if (obj instanceof JTextArea)
            result = ((JTextArea) obj).getText();

        config.setProperty(property, result);
    }
}
