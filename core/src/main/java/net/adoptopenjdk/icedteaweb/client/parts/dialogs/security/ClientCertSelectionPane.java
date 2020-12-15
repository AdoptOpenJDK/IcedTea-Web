package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

/* ClientCertSelectionPane.java -- requests client cert selection from users

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */

import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Modal non-minimizable dialog to request client cert selection
 */
public class ClientCertSelectionPane extends SecurityDialogPanel {

 public ClientCertSelectionPane(SecurityDialog parent, Object[] extras) {
     super(parent);
     setLayout(new GridBagLayout());
     JLabel jlInfo = new JLabel("<html>" + R("CVCertificateViewer")  + "</html>");

     @SuppressWarnings("unchecked") LinkedHashMap<String, X509Certificate> aliasesMap = (LinkedHashMap<String, X509Certificate>)extras[0];
     ArrayList<String> displayedNames = new ArrayList<String>();
     for (Entry<String, X509Certificate> entry : aliasesMap.entrySet()) {
        String subject = SecurityUtil.getCN(entry.getValue().getSubjectX500Principal().getName());
        String issuer = SecurityUtil.getCN(entry.getValue().getIssuerX500Principal().getName());
        int pos = entry.getKey().lastIndexOf(" (from ");
        String source = (pos!=-1)?entry.getKey().substring(pos):"";
        displayedNames.add(subject + ":" + issuer + source);
     }
     
     JList<String> jlist = new JList<String>(displayedNames.toArray(new String[0]));
     jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     jlist.setSelectedIndex(0);
     JScrollPane scrollPane = new JScrollPane(jlist);

     JButton jbOK = new JButton(R("ButOk"));
     JButton jbCancel = new JButton(R("ButCancel"));
     jbOK.setPreferredSize(jbCancel.getPreferredSize());
     JButton jbDetails = new JButton(R("ButShowDetails"));
     jbDetails.setBorderPainted(false);
     jbDetails.setContentAreaFilled(false);
     jbDetails.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     jbDetails.setMargin(new Insets(0, 0, 0, 0));

     GridBagConstraints c = new GridBagConstraints();
     c.anchor = GridBagConstraints.NORTHWEST;
     c.gridx = 0;
     c.gridy = 0;
     c.weightx = 1.0;
     c.insets = new Insets(10, 5, 3, 3);
     add(jlInfo, c);

     c = new GridBagConstraints();
     c.fill = GridBagConstraints.BOTH;
     c.gridx = 0;
     c.gridy = 1;
     c.weightx = 1.0;
     c.weighty = 1.0;
     c.insets = new Insets(10, 5, 3, 3);
     add(scrollPane, c);

     c = new GridBagConstraints();
     c.anchor = GridBagConstraints.SOUTHEAST;
     c.gridx = 0;
     c.gridy = 2;
     c.weightx = 1.0;
     c.insets = new Insets(5, 5, 3, 3);
     add(jbDetails, c);
     
     c = new GridBagConstraints();
     c.anchor = GridBagConstraints.SOUTHEAST;
     c.gridx = 0;
     c.gridy = 3;
     c.weightx = 1.0;
     c.insets = new Insets(3, 3, 10, 10);
     
     JPanel okCancelPane = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
     okCancelPane.add(jbOK);
     okCancelPane.add(Box.createHorizontalStrut(10));
     okCancelPane.add(jbCancel);
     add(okCancelPane, c);
     
     if (parent!=null) {
         parent.getViwableDialog().setMinimumSize(new Dimension(500, 300));
         parent.getViwableDialog().setLocationRelativeTo(null);
         parent.getViwableDialog().pack();
     }

     initialFocusComponent = scrollPane;

     // click on OK
     jbOK.addActionListener(new ActionListener() {
         @Override public void actionPerformed(ActionEvent e) {
             certSelected(parent, jlist);
         }
     });
     // double-click on selection
     jlist.addMouseListener(new MouseAdapter() {
         @Override public void mouseClicked(MouseEvent me) {
             if (me.getClickCount() == 2)
                 certSelected(parent, jlist);
         }
     });
     // enter on selection
     jlist.addKeyListener(new KeyAdapter() {
         @Override public void keyReleased(KeyEvent ke) {
             if(ke.getKeyCode() == KeyEvent.VK_ENTER)
                 certSelected(parent, jlist);
         }
     });
     // open certificate details
     jbDetails.addActionListener(new ActionListener() {
         @Override public void actionPerformed(ActionEvent e) {
             SecurityDialog.showSingleCertInfoDialog(aliasesMap.values().toArray(new X509Certificate[0])[jlist.getSelectedIndex()],
                 ClientCertSelectionPane.this);
         }
     });
     // click on Cancel
     jbCancel.addActionListener(new ActionListener() {
         @Override public void actionPerformed(ActionEvent e) {
             parent.setValue(null);
             parent.getViwableDialog().dispose();
         }
     });
 }

 @Override
 public DialogResult getDefaultNegativeAnswer() {
     return null;
 }

 @Override
 public DialogResult getDefaultPositiveAnswer() {
     return null;
 }

 @Override
 public DialogResult readFromStdIn(String what) {
     return null;
 }

 @Override
 public String helpToStdIn() {
     return "";
 }
 
 private void certSelected(SecurityDialog parent, JList<String> jlist) {
     parent.setValue(new DialogResult() {
         @Override public int getButtonIndex() {
            return jlist.getSelectedIndex();
         }
         @Override public boolean toBoolean() {
            throw new UnsupportedOperationException("Not supported yet.");
         }
         @Override public String writeValue() {
            throw new UnsupportedOperationException("Not supported yet.");
         }                
       });
     parent.getViwableDialog().dispose();
 }

 public static void main(String[] args) throws Exception {
     KeyStore ks = KeyStore.getInstance("Windows-MY");
     ks.load(null, null);
     Enumeration<String >aliases = ks.aliases();
     LinkedHashMap<String, X509Certificate> aliasesMap = new LinkedHashMap<String, X509Certificate>();
     while (aliases.hasMoreElements()) {
         String alias = aliases.nextElement();
         Certificate c = ks.getCertificate(alias);
         if (c instanceof X509Certificate)
            aliasesMap.put(alias + " (from browser keystore)", (X509Certificate)c);
     }
     JFrame f = new JFrame();
     f.setMinimumSize(new Dimension(500, 300));
     f.setSize(700, 300);
     f.add(new ClientCertSelectionPane(null, new Object[] { aliasesMap }), BorderLayout.CENTER);
     f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     f.pack();
     f.setVisible(true);
 }
}