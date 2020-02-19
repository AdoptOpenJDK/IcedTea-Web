// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.adoptopenjdk.icedteaweb.security.dialog.panel;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.security.SecurityUtil;
import sun.security.x509.CertificateValidity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This panel displays data from X509Certificate(s) used in jar signing.
 */
public class CertificateDetailsPanel extends JPanel {
    private final static Logger LOG = LoggerFactory.getLogger(CertificateDetailsPanel.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private JTree tree;

    private static String[] columnNames = {TRANSLATOR.translate("Field"), TRANSLATOR.translate("Value")};

    private List<? extends Certificate> certificates;
    private List<String[][]> certsData;


    public CertificateDetailsPanel(final CertPath certPath) {
        certificates = certPath != null ? certPath.getCertificates() : Collections.emptyList();
        certsData = new ArrayList<>();

        if (certPath != null) {
            for (int i = 0; i < certPath.getCertificates().size(); i++) {
                X509Certificate c = (X509Certificate) certificates.get(i);
                certsData.add(parseCert(c));
            }
        }

        createContent();
    }

    private void createContent() {
        this.setLayout(new BorderLayout());

        final JTextArea value = createValueTextArea();
        final JTable table = createCertDetailsTable(value);
        tree = createCertPathTree(table);

        final JScrollPane treePane = new JScrollPane(tree);
        final JScrollPane tablePane = new JScrollPane(table);
        final JScrollPane valuePane =  new JScrollPane(value);

        JSplitPane tableToValueSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, valuePane);
        tableToValueSplitPane.setDividerLocation(0.70);
        tableToValueSplitPane.setResizeWeight(0.70);

        JSplitPane treeToDetailsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, tableToValueSplitPane);
        treeToDetailsSplitPane.setPreferredSize(new Dimension(800, 300));
        treeToDetailsSplitPane.setDividerLocation(0.30);
        treeToDetailsSplitPane.setResizeWeight(0.30);

        add(treeToDetailsSplitPane, BorderLayout.CENTER);
    }

    public void copyToClipboard() {
        copyToClipboard(tree);
    }

    private JTextArea createValueTextArea() {
        final JTextArea valueTextArea = new JTextArea();
        valueTextArea.setEditable(false);

        return valueTextArea;
    }

    private JTree createCertPathTree(final JTable table) {
        JTree tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        if (!certificates.isEmpty()) {
            X509Certificate firstCert = ((X509Certificate) certificates.get(0));
            String subjectString = SecurityUtil.getCN(firstCert.getSubjectX500Principal().getName());
            String issuerString = SecurityUtil.getCN(firstCert.getIssuerX500Principal().getName());

            DefaultMutableTreeNode top = new DefaultMutableTreeNode(subjectString + " (" + issuerString + ")");

            //not self signed
            if (!firstCert.getSubjectDN().equals(firstCert.getIssuerDN()) && (certificates.size() > 1)) {
                X509Certificate secondCert = ((X509Certificate) certificates.get(1));
                subjectString = SecurityUtil.getCN(secondCert.getSubjectX500Principal().getName());
                issuerString = SecurityUtil.getCN(secondCert.getIssuerX500Principal().getName());
                top.add(new DefaultMutableTreeNode(subjectString + " (" + issuerString + ")"));
            }
            tree.setModel(new DefaultTreeModel(top, false));

            tree.addTreeSelectionListener(e -> {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (certsData.isEmpty() || node == null) {
                    return;
                }
                final int certIndex = node.isLeaf() ? 1 : 0;

                if (certsData.size() > certIndex) {
                    table.setModel(new DefaultTableModel(certsData.get(certIndex), columnNames));
                }
            });
        }
        return tree;
    }

    private void copyToClipboard(final JTree tree) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (certsData.isEmpty() || node == null) {
            return;
        }

        final int certIndex = node.isLeaf() ? 1 : 0;

        if (certsData.size() > certIndex) {
            final String[][] cert = certsData.get(certIndex);
            final int rows = cert.length;
            final int cols = cert[0].length;

            String certString = "";
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    certString += cert[i][j];
                    certString += " ";
                }
                certString += "\n";
            }

            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(certString.toString()), null);
        }
    }

    private JTable createCertDetailsTable(final JTextArea valueTextArea) {
        final Object[][] data = certsData.isEmpty() ? new Object[0][0] : certsData.get(0);
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);

        final ListSelectionModel tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(e -> {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();

            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    valueTextArea.setText((String) table.getValueAt(i, 1));
                }
            }
        });
        table.setFillsViewportHeight(true);

        return table;
    }


    private static String[][] parseCert(X509Certificate c) {
        String version = "" + c.getVersion();
        String serialNumber = c.getSerialNumber().toString();
        String signatureAlg = c.getSigAlgName();
        String issuer = c.getIssuerX500Principal().toString();
        String validity = new CertificateValidity(c.getNotBefore(),
                c.getNotAfter()).toString();
        String subject = c.getSubjectX500Principal().toString();

        String signature = jdkIndependentHexEncoder(c.getSignature());

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

        return new String[][]{{TRANSLATOR.translate("Version"), version},
                {TRANSLATOR.translate("SSerial"), serialNumber},
                {TRANSLATOR.translate("SSignatureAlgorithm"), signatureAlg},
                {TRANSLATOR.translate("SIssuer"), issuer},
                {TRANSLATOR.translate("SValidity"), validity},
                {TRANSLATOR.translate("SSubject"), subject},
                {TRANSLATOR.translate("SSignature"), signature},
                {TRANSLATOR.translate("SMD5Fingerprint"), md5Hash},
                {TRANSLATOR.translate("SSHA1Fingerprint"), sha1Hash}
        };
    }

    private static String jdkIndependentHexEncoder(byte[] signature) {
        try {
            return jdkIndependentHexEncoderImpl(signature);
        } catch (Exception ex) {
            String s = "Failed to encode signature: " + ex.toString();
            LOG.error("Failed to encode signature", ex);
            return s;
        }
    }

    private static String jdkIndependentHexEncoderImpl(byte[] signature) throws Exception {
        try {
            LOG.debug("trying jdk9's HexDumpEncoder");
            Class clazz = Class.forName("sun.security.util.HexDumpEncoder");
            Object encoder = clazz.newInstance();
            Method m = clazz.getDeclaredMethod("encodeBuffer", byte[].class);
            //convert our signature into a nice human-readable form.
            return (String) m.invoke(encoder, signature);
        } catch (Exception ex) {
            LOG.debug("trying jdk8's HexDumpEncoder");
            Class clazz = Class.forName("sun.misc.HexDumpEncoder");
            Object encoder = clazz.newInstance();
            Method m = clazz.getMethod("encode", byte[].class);
            //convert our signature into a nice human-readable form.
            return (String) m.invoke(encoder, signature);
        }
    }

    /**
     * Makes a human readable hash fingerprint.
     * For example: 11:22:33:44:AA:BB:CC:DD:EE:FF.
     */
    private static String makeFingerprint(byte[] hash) {
        String fingerprint = "";
        for (final byte b : hash) {
            if (!fingerprint.equals("")) {
                fingerprint += ":";
            }
            fingerprint += Integer.toHexString(
                    ((b & 0xFF) | 0x100)).substring(1, 3);
        }
        return fingerprint.toUpperCase();
    }
}
