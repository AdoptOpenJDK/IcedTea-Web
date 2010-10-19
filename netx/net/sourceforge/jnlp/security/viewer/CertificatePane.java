/* CertificatePane.java
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

package net.sourceforge.jnlp.security.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.security.SecurityWarningDialog;
import net.sourceforge.jnlp.tools.KeyTool;

public class CertificatePane extends JPanel {

        /**
         * The certificates stored in the user's trusted.certs file.
         */
        private ArrayList<X509Certificate> certs = null;

        /**
         * "Issued To" and "Issued By" string pairs for certs.
         */
        private String[][] issuedToAndBy = null;
        private final String[] columnNames = { "Issued To", "Issued By" };

        private JTable table;

        private JDialog parent;

        private JComponent defaultFocusComponent = null;

        /**
         * The KeyStore associated with the user's trusted.certs file.
         */
        private KeyStore keyStore = null;

        public CertificatePane(JDialog parent) {
                super();
                this.parent = parent;
                initializeKeyStore();
                addComponents();
        }

        /**
         * Reads the user's trusted.cacerts keystore.
         */
        private void initializeKeyStore() {
                try {
                        keyStore = SecurityUtil.getUserKeyStore();
                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }

        //create the GUI here.
        protected void addComponents() {
                readKeyStore();

                JPanel main = new JPanel(new BorderLayout());

                JPanel tablePanel = new JPanel(new BorderLayout());

                //Table
                DefaultTableModel tableModel
                        = new DefaultTableModel(issuedToAndBy, columnNames);
                table = new JTable(tableModel);
                table.getTableHeader().setReorderingAllowed(false);
                table.setFillsViewportHeight(true);
                JScrollPane tablePane = new JScrollPane(table);
                tablePane.setPreferredSize(new Dimension(500,200));
                tablePane.setSize(new Dimension(500,200));
                tablePane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.addTab("User", tablePane);
                JPanel buttonPanel = new JPanel(new FlowLayout());

                String[] buttonNames = {"Import", "Export", "Remove", "Details"};
                char[] buttonMnemonics = {      KeyEvent.VK_I,
                                                                        KeyEvent.VK_E,
                                                                        KeyEvent.VK_M,
                                                                        KeyEvent.VK_D};
                ActionListener[] listeners = {  new ImportButtonListener(),
                                                                                new ExportButtonListener(),
                                                                                new RemoveButtonListener(),
                                                                                new DetailsButtonListener() };
                JButton button;

                //get the max width
                int maxWidth = 0;
                for (int i = 0; i < buttonNames.length; i++) {
                        button = new JButton(buttonNames[i]);
                        maxWidth = Math.max(maxWidth, button.getMinimumSize().width);
                }

                for (int i = 0; i < buttonNames.length; i++) {
                        button = new JButton(buttonNames[i]);
                        button.setMnemonic(buttonMnemonics[i]);
                        button.addActionListener(listeners[i]);
                        button.setSize(maxWidth, button.getSize().height);
                        buttonPanel.add(button);
                }

                tablePanel.add(tabbedPane, BorderLayout.CENTER);
                tablePanel.add(buttonPanel, BorderLayout.SOUTH);

                JPanel closePanel = new JPanel(new BorderLayout());
                closePanel.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(new CloseButtonListener());
                defaultFocusComponent = closeButton;
                closePanel.add(closeButton, BorderLayout.EAST);

                main.add(tablePanel, BorderLayout.CENTER);
                main.add(closePanel, BorderLayout.SOUTH);

                add(main);

        }

        /**
         * Read in the optionPane's keystore to issuedToAndBy.
         */
        private void readKeyStore() {

                Enumeration<String> aliases = null;
                certs = new ArrayList<X509Certificate>();
                try {

                        //Get all of the X509Certificates and put them into an ArrayList
                        aliases = keyStore.aliases();
                        while (aliases.hasMoreElements()) {
                                Certificate c = keyStore.getCertificate(aliases.nextElement());
                                if (c instanceof X509Certificate)
                                        certs.add((X509Certificate)c);
                        }

                        //get the publisher and root information
                        issuedToAndBy = new String[certs.size()][2];
                        for (int i = 0; i < certs.size(); i++) {
                    X509Certificate c = certs.get(i);
                                issuedToAndBy[i][0] =
                                        SecurityUtil.getCN(c.getSubjectX500Principal().getName());
                                issuedToAndBy[i][1] =
                                        SecurityUtil.getCN(c.getIssuerX500Principal().getName());
                }
                } catch (Exception e) {
                        //TODO
                }
        }

        /**
         * Re-reads the certs file and repopulates the JTable. This is typically
         * called after a certificate was deleted from the keystore.
         */
        private void repopulateTable() {
                initializeKeyStore();
                readKeyStore();
                DefaultTableModel tableModel
                        = new DefaultTableModel(issuedToAndBy, columnNames);

                table.setModel(tableModel);
                repaint();
        }

        public void focusOnDefaultButton() {
            if (defaultFocusComponent != null) {
                defaultFocusComponent.requestFocusInWindow();
            }
        }

        private class ImportButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(parent);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                                KeyTool kt = new KeyTool();
                                kt.importCert(chooser.getSelectedFile());
                                repopulateTable();
                        } catch (Exception ex) {
                                // TODO: handle exception
                                ex.printStackTrace();
                        }
                }
        }
    }

        private class ExportButtonListener implements ActionListener {
                public void actionPerformed(ActionEvent e) {
                        //For now, let's just export in -rfc mode as keytool does.
                        //we'll write to a file the exported certificate.


                        try {
                                int selectedRow = table.getSelectedRow();
                                if (selectedRow != -1) {
                                JFileChooser chooser = new JFileChooser();
                                int returnVal = chooser.showOpenDialog(parent);
                                if(returnVal == JFileChooser.APPROVE_OPTION) {
                                        String alias = keyStore.getCertificateAlias(certs
                                                        .get(selectedRow));
                                        if (alias != null) {
                                                Certificate c = keyStore.getCertificate(alias);
                                                PrintStream ps = new PrintStream(chooser.getSelectedFile().getAbsolutePath());
                                                KeyTool.dumpCert(c, ps);
                                                repopulateTable();
                                        }
                                }
                                }
                        } catch (Exception ex) {
                                // TODO
                                ex.printStackTrace();
                        }
                }
        }

        private class RemoveButtonListener implements ActionListener {

                /**
                 * Removes a certificate from the keyStore and writes changes to disk.
                 */
        public void actionPerformed(ActionEvent e) {

                try {
                        int selectedRow = table.getSelectedRow();

                        if (selectedRow != -1){
                                String alias = keyStore.getCertificateAlias(certs.get(selectedRow));
                                if (alias != null) {

                                        int i = JOptionPane.showConfirmDialog(parent,
                                                        "Are you sure you want to remove the selected certificate?",
                                                        "Confirmation - Remove Certificate?",
                                                        JOptionPane.YES_NO_OPTION);
                                        if (i == 0) {
                                                keyStore.deleteEntry(alias);
                                                FileOutputStream fos = new FileOutputStream(
                                                        SecurityUtil.getTrustedCertsFilename());
                                                keyStore.store(fos, SecurityUtil.getTrustedCertsPassword());
                                                fos.close();
                                        }
                                }
                                repopulateTable();
                        }
                } catch (Exception ex) {
                        // TODO
                                ex.printStackTrace();
                }

        }
    }

        private class DetailsButtonListener implements ActionListener {

                /**
                 * Shows the details of a trusted certificate.
                 */
        public void actionPerformed(ActionEvent e) {

                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1 && selectedRow >= 0) {
                        X509Certificate c = certs.get(selectedRow);
                        SecurityWarningDialog.showSingleCertInfoDialog(c, parent);
                }
        }
    }

        private class CloseButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
            }
        }

}
