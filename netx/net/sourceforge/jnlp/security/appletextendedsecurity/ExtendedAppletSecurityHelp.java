/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;

public class ExtendedAppletSecurityHelp extends javax.swing.JDialog implements HyperlinkListener {

    public ExtendedAppletSecurityHelp(java.awt.Frame parent, boolean modal, String reference) {
        this(parent, modal);
        mainHtmlPane.scrollToReference(reference);

    }

    public ExtendedAppletSecurityHelp(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Dimension d = new Dimension(600, 400);
        setPreferredSize(d);
        setSize(d);
        initComponents();
        mainHtmlPane.setText(Translator.R("APPEXTSEChelp"));
        mainHtmlPane.addHyperlinkListener(ExtendedAppletSecurityHelp.this);
        mainHtmlPane.setCaretPosition(1);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                if (event.getURL() == null) {
                    String s = event.getDescription().replace("#", "");
                    mainHtmlPane.scrollToReference(s);
                } else {
                    mainHtmlPane.setPage(event.getURL());
                }
            } catch (IOException ioe) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ioe);
            }
        }
    }

    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        mainHtmlPane = new javax.swing.JEditorPane();
        mainPanel = new javax.swing.JPanel();
        niceSeparator = new javax.swing.JSeparator();
        mainButtonsPanel = new javax.swing.JPanel();
        navigationPanel = new javax.swing.JPanel();
        homeButton = new javax.swing.JButton();
        homeAndDialogueButton = new javax.swing.JButton();
        closePanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        mainHtmlPane.setContentType("text/html");
        mainHtmlPane.setEditable(false);
        scrollPane.setViewportView(mainHtmlPane);
        getContentPane().add(scrollPane);
        javax.swing.GroupLayout mainLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);
        mainLayout.setHorizontalGroup(
                mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 485, Short.MAX_VALUE)
                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainLayout.createSequentialGroup()
                .addGap(0, 217, Short.MAX_VALUE)
                .addComponent(niceSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 218, Short.MAX_VALUE))));
        mainLayout.setVerticalGroup(
                mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 10, Short.MAX_VALUE)
                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(niceSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))));

        getContentPane().add(mainPanel);
        mainButtonsPanel.setLayout(new javax.swing.BoxLayout(mainButtonsPanel, javax.swing.BoxLayout.LINE_AXIS));
        navigationPanel.setLayout(new javax.swing.BoxLayout(navigationPanel, javax.swing.BoxLayout.LINE_AXIS));
        homeButton.setText(Translator.R("SPLASHHome"));
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToIntroSection(evt);
            }
        });
        navigationPanel.add(homeButton);
        homeAndDialogueButton.setText(Translator.R("APPEXTSEChelpHomeDialogue"));
        homeAndDialogueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToDialogueSection(evt);
            }
        });
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExtendedAppletSecurityHelp.this.dispose();
            }
        });
        navigationPanel.add(homeAndDialogueButton);
        mainButtonsPanel.add(navigationPanel);
        closeButton.setText(Translator.R("ButClose"));
        closePanel.add(closeButton);
        mainButtonsPanel.add(closePanel);
        getContentPane().add(mainButtonsPanel);

        pack();
    }

    private void goToIntroSection(java.awt.event.ActionEvent evt) {
        mainHtmlPane.setText(Translator.R("APPEXTSEChelp"));
        mainHtmlPane.setCaretPosition(1);
    }

    private void goToDialogueSection(java.awt.event.ActionEvent evt) {
        mainHtmlPane.setText(Translator.R("APPEXTSEChelp"));
        mainHtmlPane.scrollToReference("dialogue");
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ExtendedAppletSecurityHelp dialog = new ExtendedAppletSecurityHelp(null, false);
                dialog.setVisible(true);
            }
        });
    }
    private JButton homeAndDialogueButton;
    private JButton homeButton;
    private JButton closeButton;
    private JEditorPane mainHtmlPane;
    private JPanel mainButtonsPanel;
    private JPanel navigationPanel;
    private JPanel closePanel;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JSeparator niceSeparator;
}
