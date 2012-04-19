/* BasicExceptionDialog.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */

package net.sourceforge.jnlp.util;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A dialog that displays some basic information about an exception
 */
public class BasicExceptionDialog {

    private static String exceptionToString(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Must be invoked from the Swing EDT.
     *
     * @param exception the exception to indicate
     */
    public static void show(Exception exception) {
        String detailsText = exceptionToString(exception);

        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JOptionPane optionPane = new JOptionPane(mainPanel, JOptionPane.ERROR_MESSAGE);
        final JDialog errorDialog = optionPane.createDialog(R("Error"));
        errorDialog.setIconImages(ImageResources.INSTANCE.getApplicationImages());

        final JPanel quickInfoPanel = new JPanel();
        BoxLayout layout = new BoxLayout(quickInfoPanel, BoxLayout.Y_AXIS);
        quickInfoPanel.setLayout(layout);
        mainPanel.add(quickInfoPanel, BorderLayout.PAGE_START);

        JLabel errorLabel = new JLabel(exception.getMessage());
        errorLabel.setAlignmentY(JComponent.LEFT_ALIGNMENT);
        quickInfoPanel.add(errorLabel);

        final JButton viewDetails = new JButton(R("ButShowDetails"));
        viewDetails.setAlignmentY(JComponent.LEFT_ALIGNMENT);
        viewDetails.setActionCommand("show");
        quickInfoPanel.add(viewDetails);

        JTextArea textArea = new JTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textArea.setEditable(false);
        textArea.setText(detailsText);
        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(100, 200));

        viewDetails.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (viewDetails.getActionCommand().equals("show")) {
                    mainPanel.add(scrollPane, BorderLayout.CENTER);
                    viewDetails.setActionCommand("hide");
                    viewDetails.setText(R("ButHideDetails"));
                    errorDialog.pack();
                } else {
                    mainPanel.remove(scrollPane);
                    viewDetails.setActionCommand("show");
                    viewDetails.setText(R("ButShowDetails"));
                    errorDialog.pack();
                }
            }
        });

        errorDialog.pack();
        errorDialog.setResizable(true);
        errorDialog.setVisible(true);
        errorDialog.dispose();
    }
}
