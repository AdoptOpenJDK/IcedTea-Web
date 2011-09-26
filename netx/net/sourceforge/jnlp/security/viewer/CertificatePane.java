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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.jnlp.security.CertificateUtils;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.KeyStores.Level;
import net.sourceforge.jnlp.util.FileUtils;

public class CertificatePane extends JPanel {

    /**
     * The certificates stored in the certificates file.
     */
    private ArrayList<X509Certificate> certs = null;

    private static final Dimension TABLE_DIMENSION = new Dimension(500, 200);

    /**
     * "Issued To" and "Issued By" string pairs for certs.
     */
    private String[][] issuedToAndBy = null;
    private final String[] columnNames = { R("CVIssuedTo"), R("CVIssuedBy") };

    private final CertificateType[] certificateTypes = new CertificateType[] {
            new CertificateType(KeyStores.Type.CA_CERTS),
            new CertificateType(KeyStores.Type.JSSE_CA_CERTS),
            new CertificateType(KeyStores.Type.CERTS),
            new CertificateType(KeyStores.Type.JSSE_CERTS),
            new CertificateType(KeyStores.Type.CLIENT_CERTS)
        };

    JTabbedPane tabbedPane;
    private final JTable userTable;
    private final JTable systemTable;
    private JComboBox certificateTypeCombo;
    private KeyStores.Type currentKeyStoreType;
    private KeyStores.Level currentKeyStoreLevel;

    /** JComponents that should be disbled for system store */
    private final List<JComponent> disableForSystem;

    private JDialog parent;
    private JComponent defaultFocusComponent = null;

    /**
     * The Current KeyStore. Only one table/tab is visible for interaction to
     * the user. This KeyStore corresponds to that.
     */
    private KeyStore keyStore = null;

    public CertificatePane(JDialog parent) {
        super();
        this.parent = parent;

        userTable = new JTable(null);
        systemTable = new JTable(null);
        disableForSystem = new ArrayList<JComponent>();

        addComponents();

        currentKeyStoreType = ((CertificateType) (certificateTypeCombo.getSelectedItem())).getType();
        if (tabbedPane.getSelectedIndex() == 0) {
            currentKeyStoreLevel = Level.USER;
        } else {
            currentKeyStoreLevel = Level.SYSTEM;
        }

        repopulateTables();
    }

    /**
     * Reads the user's trusted.cacerts keystore.
     */
    private void initializeKeyStore() {
        try {
            keyStore = KeyStores.getKeyStore(currentKeyStoreLevel, currentKeyStoreType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //create the GUI here.
    protected void addComponents() {

        JPanel main = new JPanel(new BorderLayout());

        JPanel certificateTypePanel = new JPanel(new BorderLayout());
        certificateTypePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel certificateTypeLabel = new JLabel(R("CVCertificateType"));

        certificateTypeCombo = new JComboBox(certificateTypes);
        certificateTypeCombo.addActionListener(new CertificateTypeListener());

        certificateTypePanel.add(certificateTypeLabel, BorderLayout.LINE_START);
        certificateTypePanel.add(certificateTypeCombo, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());

        // User Table
        DefaultTableModel userTableModel = new DefaultTableModel(issuedToAndBy, columnNames);
        userTable.setModel(userTableModel);
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setFillsViewportHeight(true);
        JScrollPane userTablePane = new JScrollPane(userTable);
        userTablePane.setPreferredSize(TABLE_DIMENSION);
        userTablePane.setSize(TABLE_DIMENSION);
        userTablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // System Table
        DefaultTableModel systemTableModel = new DefaultTableModel(issuedToAndBy, columnNames);
        systemTable.setModel(systemTableModel);
        systemTable.getTableHeader().setReorderingAllowed(false);
        systemTable.setFillsViewportHeight(true);
        JScrollPane systemTablePane = new JScrollPane(systemTable);
        systemTablePane.setPreferredSize(TABLE_DIMENSION);
        systemTablePane.setSize(TABLE_DIMENSION);
        systemTablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(R("CVUser"), userTablePane);
        tabbedPane.addTab(R("CVSystem"), systemTablePane);
        tabbedPane.addChangeListener(new TabChangeListener());

        JPanel buttonPanel = new JPanel(new FlowLayout());

        String[] buttonNames = { R("CVImport"), R("CVExport"), R("CVRemove"), R("CVDetails") };
        char[] buttonMnemonics = { KeyEvent.VK_I,
                                                                        KeyEvent.VK_E,
                                                                        KeyEvent.VK_M,
                                                                        KeyEvent.VK_D };
        ActionListener[] listeners = { new ImportButtonListener(),
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
            // import and remove buttons
            if (i == 0 || i == 2) {
                disableForSystem.add(button);
            }
            buttonPanel.add(button);
        }

        tablePanel.add(tabbedPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        main.add(certificateTypePanel, BorderLayout.NORTH);
        main.add(tablePanel, BorderLayout.CENTER);

        if (parent != null) {
            JPanel closePanel = new JPanel(new BorderLayout());
            closePanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
            JButton closeButton = new JButton(R("ButClose"));
            closeButton.addActionListener(new CloseButtonListener());
            defaultFocusComponent = closeButton;
            closePanel.add(closeButton, BorderLayout.EAST);
            main.add(closePanel, BorderLayout.SOUTH);
        }

        setLayout(new GridLayout(0,1));
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
                    certs.add((X509Certificate) c);
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
            e.printStackTrace();
        }
    }

    /**
     * Re-reads the certs file and repopulates the JTable. This is typically
     * called after a certificate was deleted from the keystore.
     */
    private void repopulateTables() {
        initializeKeyStore();
        readKeyStore();
        DefaultTableModel tableModel = new DefaultTableModel(issuedToAndBy, columnNames);

        userTable.setModel(tableModel);

        tableModel = new DefaultTableModel(issuedToAndBy, columnNames);
        systemTable.setModel(tableModel);
    }

    public void focusOnDefaultButton() {
        if (defaultFocusComponent != null) {
            defaultFocusComponent.requestFocusInWindow();
        }
    }

    private char[] getPassword(final String label) {
        JPasswordField jpf = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(parent,
                                new Object[]{label, jpf},  R("CVPasswordTitle"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            return jpf.getPassword();
        else
            return null;
    }

    /** Allows storing KeyStores.Types in a JComponent */
    private static class CertificateType {
        private final KeyStores.Type type;

        public CertificateType(KeyStores.Type type) {
            this.type = type;
        }

        public KeyStores.Type getType() {
            return type;
        }

        public String toString() {
            return KeyStores.toDisplayableString(null, type);
        }
    }

    /** Invoked when a user selects a different certificate type */
    private class CertificateTypeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox source = (JComboBox) e.getSource();
            CertificateType type = (CertificateType) source.getSelectedItem();
            currentKeyStoreType = type.getType();
            repopulateTables();
        }
    }

    /**
     * Invoked when a user selects a different tab (switches from user to system
     * or vice versa). Changes the currentKeyStore Enables or disables buttons.
     */
    private class TabChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JTabbedPane source = (JTabbedPane) e.getSource();
            switch (source.getSelectedIndex()) {
                case 0:
                    currentKeyStoreLevel = Level.USER;
                    for (JComponent component : disableForSystem) {
                        component.setEnabled(true);
                    }
                    break;
                case 1:
                    currentKeyStoreLevel = Level.SYSTEM;
                    for (JComponent component : disableForSystem) {
                        component.setEnabled(false);
                    }
                    break;
            }
            repopulateTables();

        }
    }

    private class ImportButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    KeyStore ks = keyStore;
                    if (currentKeyStoreType == KeyStores.Type.CLIENT_CERTS) {
                        char[] password = getPassword(R("CVImportPasswordMessage"));
                        if (password != null) {
                            CertificateUtils.addPKCS12ToKeyStore(
                                       chooser.getSelectedFile(), ks, password);
                        } else {
                            return;
                        }
                    } else {
                        CertificateUtils.addToKeyStore(chooser.getSelectedFile(), ks);
                    }
                    File keyStoreFile = new File(KeyStores
                                        .getKeyStoreLocation(currentKeyStoreLevel, currentKeyStoreType));
                    if (!keyStoreFile.isFile()) {
                        FileUtils.createRestrictedFile(keyStoreFile, true);
                    }

                    OutputStream os = new FileOutputStream(keyStoreFile);
                    try {
                        ks.store(os, KeyStores.getPassword());
                    } finally {
                        os.close();
                    }
                    repopulateTables();
                } catch (Exception ex) {
                    // TODO: handle exception
                    ex.printStackTrace();
                }
            }
        }
    }

    private class ExportButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            JTable table = null;
            if (currentKeyStoreLevel == Level.USER) {
                table = userTable;
            } else {
                table = systemTable;
            }

            //For now, let's just export in -rfc mode as keytool does.
            //we'll write to a file the exported certificate.

            try {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    JFileChooser chooser = new JFileChooser();
                    int returnVal = chooser.showOpenDialog(parent);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String alias = keyStore.getCertificateAlias(certs
                                                        .get(selectedRow));
                        if (alias != null) {
                            if (currentKeyStoreType == KeyStores.Type.CLIENT_CERTS) {
                                char[] password = getPassword(R("CVExportPasswordMessage"));
                                if (password != null)
                                    CertificateUtils.dumpPKCS12(alias, chooser.getSelectedFile(), keyStore, password);
                            } else {
                                Certificate c = keyStore.getCertificate(alias);
                                PrintStream ps = new PrintStream(chooser.getSelectedFile().getAbsolutePath());
                                CertificateUtils.dump(c, ps);
                            }
                            repopulateTables();
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

            JTable table = null;
            if (currentKeyStoreLevel == Level.USER) {
                table = userTable;
            } else {
                table = systemTable;
            }
            try {
                int selectedRow = table.getSelectedRow();

                if (selectedRow != -1) {
                    String alias = keyStore.getCertificateAlias(certs.get(selectedRow));
                    if (alias != null) {

                        int i = JOptionPane.showConfirmDialog(parent,
                                                        R("CVRemoveConfirmMessage"),
                                                        R("CVRemoveConfirmTitle"),
                                                        JOptionPane.YES_NO_OPTION);
                        if (i == 0) {
                            keyStore.deleteEntry(alias);
                            File keyStoreFile = new File(KeyStores
                                                        .getKeyStoreLocation(currentKeyStoreLevel, currentKeyStoreType));
                            if (!keyStoreFile.isFile()) {
                                FileUtils.createRestrictedFile(keyStoreFile, true);
                            }
                            FileOutputStream fos = new FileOutputStream(keyStoreFile);
                            keyStore.store(fos, KeyStores.getPassword());
                            fos.close();
                        }
                    }
                    repopulateTables();
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

            JTable table = null;
            if (currentKeyStoreLevel == Level.USER) {
                table = userTable;
            } else {
                table = systemTable;
            }

            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1 && selectedRow >= 0) {
                X509Certificate c = certs.get(selectedRow);
                SecurityDialog.showSingleCertInfoDialog(c, parent);
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
