/* SingleCertInfoPane.java
   Copyright (C) 2008 Red Hat, Inc.

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
statement from your version.
*/

package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * @deprecated will be replaced by new security dialogs
 */
@Deprecated
public class SingleCertInfoPane extends CertsInfoPane {

    public SingleCertInfoPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
    }

    @Override
    protected void buildTree() {
        X509Certificate cert = parent.getCert();
        String subjectString =
                        SecurityUtil.getCN(cert.getSubjectX500Principal().getName());
        String issuerString =
                        SecurityUtil.getCN(cert.getIssuerX500Principal().getName());

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(subjectString
                                + " (" + issuerString + ")");

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionHandler());
    }

    @Override
    protected void populateTable() {
        X509Certificate c = parent.getCert();
        certNames = new String[1];
        certsData = new ArrayList<String[][]>();
        certsData.add(parseCert(c));
        certNames[0] = SecurityUtil.getCN(c.getSubjectX500Principal().getName())
                + " (" + SecurityUtil.getCN(c.getIssuerX500Principal().getName()) + ")";
    }
}
