/* PasswordAuthenticationPane -- requests authentication information from users
   Copyright (C) 2010  Red Hat

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

package net.sourceforge.jnlp.security.dialogs;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import net.sourceforge.jnlp.security.SecurityDialog;

/**
 * Modal non-minimizable dialog to request http authentication credentials
 */

public class PasswordAuthenticationPane extends SecurityDialogPanel {

    private final JTextField jtfUserName = new JTextField();
    private final JPasswordField jpfPassword = new JPasswordField();

    private final String host;
    private final int port;
    private final String prompt;
    private final String type;

    public PasswordAuthenticationPane(SecurityDialog parent, Object[] extras) {
        super(parent);
        host = (String) extras[0];
        port = (Integer) extras[1];
        prompt = (String) extras[2];
        type = (String) extras[3];

        addComponents();
     }

    /**
     * Initialized the dialog components
     */

    public void addComponents() {

        JLabel jlInfo = new JLabel("");
        jlInfo.setText("<html>" + R("SAuthenticationPrompt", type, host, prompt)  + "</html>");

        setLayout(new GridBagLayout());

        JLabel jlUserName = new JLabel(R("Username"));
        JLabel jlPassword = new JLabel(R("Password"));
        JButton jbOK = new JButton(R("ButOk"));
        JButton jbCancel = new JButton(R("ButCancel"));

        jtfUserName.setSize(20, 10);
        jpfPassword.setSize(20, 10);

        GridBagConstraints c;

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(10, 5, 3, 3);
        add(jlInfo, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(10, 5, 3, 3);
        add(jlUserName, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(10, 5, 3, 3);
        c.weightx = 1.0;
        add(jtfUserName, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(5, 5, 3, 3);
        add(jlPassword, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(5, 5, 3, 3);
        c.weightx = 1.0;
        add(jpfPassword, c);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(5, 5, 3, 70);
        c.weightx = 0.0;
        add(jbCancel, c);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(5, 5, 3, 3);
        c.weightx = 0.0;
        add(jbOK, c);

        setMinimumSize(new Dimension(400, 150));
        setMaximumSize(new Dimension(1024, 150));

        setSize(400, 150);
        parent.setLocationRelativeTo(null);
        initialFocusComponent = jtfUserName;

        ActionListener acceptActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setValue(new Object[] { jtfUserName.getText(), jpfPassword.getPassword() });
                parent.dispose();
            }
        };

        ActionListener cancelActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setValue(null);
                parent.dispose();
            }
        };

        // OK => read supplied info and pass it on
        jbOK.addActionListener(acceptActionListener);

        // Cancel => discard supplied info and pass on an empty auth
        jbCancel.addActionListener(cancelActionListener);

        // "return" key in either user or password field => OK
        jtfUserName.addActionListener(acceptActionListener);
        jpfPassword.addActionListener(acceptActionListener);
    }
}
