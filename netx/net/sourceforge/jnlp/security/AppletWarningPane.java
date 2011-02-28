/* AppletWarningPane.java
   Copyright (C) 2008 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp.security;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AppletWarningPane extends SecurityDialogPanel {

    public AppletWarningPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
        addComponents();
    }

    protected void addComponents() {

        //Top label
        String topLabelText = "While support for verifying signed code" +
                                " has not been implemented yet, some applets will not run " +
                                "properly under the default restricted security level.";
        String bottomLabelText = "Do you want to run this applet under the " +
                                "restricted security level? (clicking No will run this applet " +
                                "without any security checking, and should only be done if you " +
                                "trust the applet!)";

        JLabel topLabel = new JLabel(htmlWrap(topLabelText));
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                        Font.BOLD, 12));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(400, 80));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel bottomLabel = new JLabel(htmlWrap(bottomLabelText));
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(bottomLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(400, 80));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        JButton cancel = new JButton("Cancel");
        int buttonWidth = cancel.getMinimumSize().width;
        int buttonHeight = cancel.getMinimumSize().height;
        Dimension d = new Dimension(buttonWidth, buttonHeight);
        yes.setPreferredSize(d);
        no.setPreferredSize(d);
        cancel.setPreferredSize(d);
        yes.addActionListener(createSetValueListener(parent, 0));
        no.addActionListener(createSetValueListener(parent, 1));
        cancel.addActionListener(createSetValueListener(parent, 2));
        initialFocusComponent = cancel;
        buttonPanel.add(yes);
        buttonPanel.add(no);
        buttonPanel.add(cancel);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);

    }

}
