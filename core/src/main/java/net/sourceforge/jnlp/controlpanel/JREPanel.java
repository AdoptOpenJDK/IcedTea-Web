/* JREPanel.java - Displays option for changing to another Java Runtime.
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

import javax.swing.JLabel;

import net.sourceforge.jnlp.runtime.Translator;

/**
 * This panel is to allow access to setting the JRE but we currently do not
 * support this.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
public class JREPanel extends NamedBorderPanel {

    /**
     * Creates a new instance of the JRE settings panel. (Currently not
     * supported).
     */
    public JREPanel() {
        super(Translator.R("CPHeadJRESettings"));
        setLayout(new BorderLayout());

        JLabel jreLabel = new JLabel("<html>" + Translator.R("CPJRESupport") + "</html>");
        add(jreLabel, BorderLayout.NORTH);
    }
}
