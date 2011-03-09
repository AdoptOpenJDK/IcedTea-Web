/* NotAllSignedWarningPane.java
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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class NotAllSignedWarningPane extends SecurityDialogPanel {

    public NotAllSignedWarningPane(SecurityDialog x) {
        super(x);
        addComponents();
    }

    /**
     * Creates the actual GUI components, and adds it to this panel
     */
    private void addComponents() {

        String topLabelText = R("SNotAllSignedSummary");
        String infoLabelText = R("SNotAllSignedDetail");
        String questionLabelText = R("SNotAllSignedQuestion");

        ImageIcon icon = new ImageIcon((new sun.misc.Launcher()).getClassLoader().getResource("net/sourceforge/jnlp/resources/warning.png"));
        JLabel topLabel = new JLabel(htmlWrap(topLabelText), icon, SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                        Font.BOLD, 12));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(500, 80));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel(htmlWrap(infoLabelText));
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(500, 100));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel questionLabel = new JLabel(htmlWrap(questionLabelText));
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        questionPanel.setPreferredSize(new Dimension(500, 100));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton run = new JButton(R("ButProceed"));
        JButton cancel = new JButton(R("ButCancel"));
        run.addActionListener(createSetValueListener(parent, 0));
        cancel.addActionListener(createSetValueListener(parent, 1));
        initialFocusComponent = cancel;
        buttonPanel.add(run);
        buttonPanel.add(cancel);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(questionPanel);
        add(buttonPanel);

    }
}
