/* CertsInfoPane.java
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

package net.sourceforge.jnlp.security.dialogs;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.util.ArrayList;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

import sun.misc.HexDumpEncoder;
import sun.security.x509.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.SecurityUtil;

/**
 * Provides the panel for the Certificate Info dialog. This dialog displays data from
 * X509Certificate(s) used in jar signing.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class CertsInfoPane extends SecurityDialogPanel {

    private CertPath certPath;
    protected JTree tree;
    private JTable table;
    private JTextArea output;
    private ListSelectionModel listSelectionModel;
    private ListSelectionModel tableSelectionModel;
    protected String[] certNames;
    private String[] columnNames = { R("Field"), R("Value") };
    protected ArrayList<String[][]> certsData;

    public CertsInfoPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
        addComponents();
    }

    /**
     * Builds the JTree out of CertPaths.
     */
    void buildTree() {
        certPath = parent.getCertVerifier().getCertPath(null);
        X509Certificate firstCert =
                        ((X509Certificate) certPath.getCertificates().get(0));
        String subjectString =
                        SecurityUtil.getCN(firstCert.getSubjectX500Principal().getName());
        String issuerString =
                        SecurityUtil.getCN(firstCert.getIssuerX500Principal().getName());

        DefaultMutableTreeNode top =
                        new DefaultMutableTreeNode(subjectString
                                + " (" + issuerString + ")");

        //not self signed
        if (!firstCert.getSubjectDN().equals(firstCert.getIssuerDN())
                        && (certPath.getCertificates().size() > 1)) {
            X509Certificate secondCert =
                                ((X509Certificate) certPath.getCertificates().get(1));
            subjectString =
                                SecurityUtil.getCN(secondCert.getSubjectX500Principal().getName());
            issuerString =
                                SecurityUtil.getCN(secondCert.getIssuerX500Principal().getName());
            top.add(new DefaultMutableTreeNode(subjectString
                                + " (" + issuerString + ")"));
        }

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionHandler());
    }

    /**
     * Fills in certsNames, certsData with data from the certificates.
     */
    protected void populateTable() {
        certNames = new String[certPath.getCertificates().size()];
        certsData = new ArrayList<String[][]>();

        for (int i = 0; i < certPath.getCertificates().size(); i++) {

            X509Certificate c = (X509Certificate) certPath.getCertificates().get(i);
            certsData.add(parseCert(c));
            certNames[i] = SecurityUtil.getCN(c.getSubjectX500Principal().getName())
                                + " (" + SecurityUtil.getCN(c.getIssuerX500Principal().getName()) + ")";
        }
    }

    protected String[][] parseCert(X509Certificate c) {

        String version = "" + c.getVersion();
        String serialNumber = c.getSerialNumber().toString();
        String signatureAlg = c.getSigAlgName();
        String issuer = c.getIssuerX500Principal().toString();
        String validity = new CertificateValidity(c.getNotBefore(),
                            c.getNotAfter()).toString();
        String subject = c.getSubjectX500Principal().toString();

        //convert our signature into a nice human-readable form.
        HexDumpEncoder encoder = new HexDumpEncoder();
        String signature = encoder.encodeBuffer(c.getSignature());

        String md5Hash = "";
        String sha1Hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(c.getEncoded());
            md5Hash = makeFingerprint(digest.digest());

            digest = MessageDigest.getInstance("SHA-1");
            digest.update(c.getEncoded());
            sha1Hash = makeFingerprint(digest.digest());
        } catch (Exception e) {
            //fail quietly
        }

        String[][] cert = { { R("Version"), version },
                            { R("SSerial"), serialNumber },
                            { R("SSignatureAlgorithm"), signatureAlg },
                            { R("SIssuer"), issuer },
                            { R("SValidity"), validity },
                            { R("SSubject"), subject },
                            { R("SSignature"), signature },
                                                        { R("SMD5Fingerprint"), md5Hash },
                                                        { R("SSHA1Fingerprint"), sha1Hash }
                                                        };
        return cert;
    }

    /**
     * Constructs the GUI components of this panel
     */
    private void addComponents() {
        buildTree();
        populateTable();
        /**
        //List of Certs
        list = new JList(certNames);
        list.setSelectedIndex(0); //assuming there's at least 1 cert
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionHandler());
        JScrollPane listPane = new JScrollPane(list);
        */
        JScrollPane listPane = new JScrollPane(tree);

        //Table of field-value pairs
        DefaultTableModel tableModel = new DefaultTableModel(certsData.get(0),
                                                            columnNames);
        table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(new TableSelectionHandler());
        table.setFillsViewportHeight(true);
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setPreferredSize(new Dimension(500, 200));

        //Text area to display the larger values
        output = new JTextArea();
        output.setEditable(false);
        JScrollPane outputPane = new JScrollPane(output,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputPane.setPreferredSize(new Dimension(500, 200));

        //split pane of the field-value pairs and textbox
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        tablePane, outputPane);
        rightSplitPane.setDividerLocation(0.50);
        rightSplitPane.setResizeWeight(0.50);

        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        listPane, rightSplitPane);
        mainPane.setDividerLocation(0.30);
        mainPane.setResizeWeight(0.30);

        JPanel buttonPane = new JPanel(new BorderLayout());
        JButton close = new JButton(R("ButClose"));
        JButton copyToClipboard = new JButton(R("ButCopy"));
        close.addActionListener(createSetValueListener(parent, 0));
        copyToClipboard.addActionListener(new CopyToClipboardHandler());
        buttonPane.add(close, BorderLayout.EAST);
        buttonPane.add(copyToClipboard, BorderLayout.WEST);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(mainPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }

    /**
     * Copies the currently selected certificate to the system Clipboard.
     */
    private class CopyToClipboardHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            int certIndex = 0;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            if (node.isRoot()) {
                certIndex = 0;
            }
            else if (node.isLeaf()) {
                certIndex = 1;
            }

            String[][] cert = certsData.get(certIndex);
            int rows = cert.length;
            int cols = cert[0].length;

            String certString = "";
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    certString += cert[i][j];
                    certString += " ";
                }
                certString += "\n";
            }

            clipboard.setContents(new StringSelection(certString), null);
        }
    }

    /**
     * Updates the JTable when the JTree selection has changed.
     */
    protected class TreeSelectionHandler implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                tree.getLastSelectedPathComponent();

            if (node == null) {
                return;
            }
            if (node.isRoot()) {
                table.setModel(new DefaultTableModel(certsData.get(0),
                                        columnNames));
            } else if (node.isLeaf()) {
                table.setModel(new DefaultTableModel(certsData.get(1),
                                        columnNames));
            }
        }
    }

    /**
    * Updates the JTable when the selection on the list has changed.
    */
    private class ListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();

            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    table.setModel(new DefaultTableModel(certsData.get(i),
                                                            columnNames));
                }
            }
        }
    }

    /**
     * Updates the JTextArea output when the selection on the JTable
     * has changed.
     */
    private class TableSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();

            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    output.setText((String) table.getValueAt(i, 1));
                }
            }
        }
    }

    /**
     * Makes a human readable hash fingerprint.
     * For example: 11:22:33:44:AA:BB:CC:DD:EE:FF.
     */
    private String makeFingerprint(byte[] hash) {
        String fingerprint = "";
        for (int i = 0; i < hash.length; i++) {
            if (!fingerprint.equals("")) {
                fingerprint += ":";
            }
            fingerprint += Integer.toHexString(
                                ((hash[i] & 0xFF) | 0x100)).substring(1, 3);
        }
        return fingerprint.toUpperCase();
    }
}
