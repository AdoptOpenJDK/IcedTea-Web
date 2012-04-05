/* MoreInfoPane.java
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides the panel for the More Info dialog. This dialog shows details about an
 * application's signing status.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class MoreInfoPane extends SecurityDialogPanel {

    private boolean showSignedJNLPWarning;

    public MoreInfoPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
        showSignedJNLPWarning= x.requiresSignedJNLPWarning();
        addComponents();
    }

    /**
     * Constructs the GUI components of this panel
     */
    private void addComponents() {
        ArrayList<String> details = certVerifier.getDetails();

        // Show signed JNLP warning if the signed main jar does not have a
        // signed JNLP file and the launching JNLP file contains special properties
        if(showSignedJNLPWarning)
            details.add(R("SJNLPFileIsNotSigned"));
            
        int numLabels = details.size();
        JPanel errorPanel = new JPanel(new GridLayout(numLabels, 1));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        errorPanel.setPreferredSize(new Dimension(400, 70 * (numLabels)));

        for (int i = 0; i < numLabels; i++) {
            ImageIcon icon = null;
            if (details.get(i).equals(R("STrustedCertificate")))
                icon = new ImageIcon((new sun.misc.Launcher())
                                                .getClassLoader().getResource("net/sourceforge/jnlp/resources/info-small.png"));
            else
                icon = new ImageIcon((new sun.misc.Launcher())
                                                .getClassLoader().getResource("net/sourceforge/jnlp/resources/warning-small.png"));

            errorPanel.add(new JLabel(htmlWrap(details.get(i)), icon, SwingConstants.LEFT));
        }
        
        // Removes signed JNLP warning after it has been used. This will avoid
        // any alteration to certVerifier.
        if(showSignedJNLPWarning)
            details.remove(details.size()-1);

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        JButton certDetails = new JButton(R("SCertificateDetails"));
        certDetails.addActionListener(new CertInfoButtonListener());
        JButton close = new JButton(R("ButClose"));
        close.addActionListener(createSetValueListener(parent, 0));
        buttonsPanel.add(certDetails, BorderLayout.WEST);
        buttonsPanel.add(close, BorderLayout.EAST);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(errorPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.SOUTH);

    }

    private class CertInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SecurityDialog.showCertInfoDialog(parent.getCertVerifier(),
                                parent);
        }
    }
}
