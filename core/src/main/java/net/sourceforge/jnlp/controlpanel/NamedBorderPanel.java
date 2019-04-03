/* NamedBorderPanel.java -- Makes a border which has a name.
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

import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * This class provides the a panel that has a border with the name specified.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class NamedBorderPanel extends JPanel {

    /**
     * Creates a new instance of JPanel with a named border and specified
     * layout.
     * 
     * @param title
     *            Name to be displayed.
     * @param layout
     *            Layout to use with this panel.
     */
    public NamedBorderPanel(String title, LayoutManager layout) {
        this(title);
        setLayout(layout);
    }

    /**
     * Creates a new instance of JPanel with a named border.
     * 
     * @param title
     *            Name to be displayed.
     */
    public NamedBorderPanel(String title) {
        super();
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }
}
