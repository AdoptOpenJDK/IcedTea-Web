/* SecurityDialogPanel.java
Copyright (C) 2008-2010 Red Hat, Inc.

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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityDialog;

/**
 * Provides a JPanel for use in JNLP warning dialogs.
 */
public abstract class SecurityDialogPanel extends JPanel {

    protected SecurityDialog parent;

    protected JComponent initialFocusComponent = null;

    CertVerifier certVerifier = null;

    public SecurityDialogPanel(SecurityDialog dialog, CertVerifier certVerifier) {
        this.parent = dialog;
        this.certVerifier = certVerifier;
        this.setLayout(new BorderLayout());
    }

    public SecurityDialogPanel(SecurityDialog dialog) {
        this.parent = dialog;
        this.setLayout(new BorderLayout());
    }

    /**
     * Needed to get word wrap working in JLabels.
     */
    protected String htmlWrap(String s) {
        return "<html>" + s + "</html>";
    }

    /**
     * Create an ActionListener suitable for use with buttons. When this {@link ActionListener}
     * is invoked, it will set the value of the {@link SecurityDialog} and then dispossed.
     *
     * @param buttonIndex the index of the button. By convention 0 = Yes. 1 = No, 2 = Cancel
     * @return the ActionListener instance.
     */
    protected ActionListener createSetValueListener(SecurityDialog dialog, int buttonIndex) {
        return new SetValueHandler(dialog, buttonIndex);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        requestFocusOnDefaultButton();
    }

    public void requestFocusOnDefaultButton() {
        if (initialFocusComponent != null) {
            initialFocusComponent.requestFocusInWindow();
        }
    }

    /**
     * Creates a handler that sets a dialog's value and then disposes it when activated
     *
     */
    private static class SetValueHandler implements ActionListener {

        Integer buttonIndex;
        SecurityDialog dialog;

        public SetValueHandler(SecurityDialog dialog, int buttonIndex) {
            this.dialog = dialog;
            this.buttonIndex = buttonIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.setValue(buttonIndex);
            dialog.dispose();
        }
    }
}
