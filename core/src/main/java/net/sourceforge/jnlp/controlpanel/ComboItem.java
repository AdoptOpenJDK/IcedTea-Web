/* ComboItem.java -- Allow storage of an item whose name differs from its value.
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

/**
 * This is to be used with combobox items. Allows storing a value which differs
 * from the key.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class ComboItem {
    String text = null;
    private String value; // Value to be compared with.

    /**
     * Create a new instance of combobox items.
     * 
     * @param text
     *            Text to be displayed by JComboBox
     * @param value
     *            Value associated with this item.
     */
    public ComboItem(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String toString() {
        return this.text;
    }

    /**
     * Get the value associated with this item.
     * 
     * @return Associated value.
     */
    public String getValue() {
        return this.value;
    }
}
