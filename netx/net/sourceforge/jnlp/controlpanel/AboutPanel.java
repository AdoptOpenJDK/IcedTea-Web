/* AboutPanel.java -- Display information about the control panel and icedtea-web.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JLabel;

import net.sourceforge.jnlp.runtime.Translator;

/**
 * This class provides a GUI interface which shows some basic information on
 * this project.
 *
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 *
 */
public class AboutPanel extends NamedBorderPanel {

    public AboutPanel() {
        super(Translator.R("CPHeadAbout"), new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel logo = new JLabel();
        JLabel aboutLabel = new JLabel("<html>" + Translator.R("CPAboutInfo") + "</html>");

        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.gridx = 0;
        c.weighty = 0;
        c.weightx = 0;
        add(logo, c);
        c.gridx = 1;
        c.weightx = 1;
        add(aboutLabel, c);

        /* Keep all the elements at the top of the panel (Extra padding) */
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.weighty = 1;
        c.gridy++;
        add(filler, c);
    }
}
