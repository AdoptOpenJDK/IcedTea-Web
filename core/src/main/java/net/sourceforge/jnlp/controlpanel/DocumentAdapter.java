/* DocumentAdapter.java -- Updates properties.
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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Updates the property as it happens.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 *
 */
public class DocumentAdapter implements DocumentListener {

    String[] fields;
    int index;
    String property;
    DeploymentConfiguration config;
    int mode;

    /**
     * This creates a new instance of DocumentAdapter.
     * 
     * @param fields The list of property.
     * @param index Location of property to modify.
     */
    public DocumentAdapter(String[] fields, int index) {
        this.fields = fields;
        this.index = index;
        mode = 1;
    }

    /**
     * This creates a new instance of DocumentAdapter. This allows modifying 
     * the configuration directly.
     * 
     * @param config ConfigurationFile containing the properties.
     * @param property Name of property to modify.
     */
    public DocumentAdapter(DeploymentConfiguration config, String property) {
        this.property = property;
        this.config = config;
        mode = 2;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    /**
     * Update the property as on the appropriate items.
     * 
     * @param e The event that caused the call.
     */
    private void update(DocumentEvent e) {
        Document d = e.getDocument();
        try {
            String value = d.getText(0, d.getLength()).trim();
            value = (value.length() == 0) ? null : value;
            if (mode == 1) {
                fields[index] = value;
            } else if (mode == 2) {
                config.setProperty(property, value);
            }
        } catch (BadLocationException e1) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e1);
        }
    }

}
