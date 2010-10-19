/* PasswordAuthenticationDialog -- requests authentication information from users
   Copyright (C) 2009  Red Hat

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

package sun.applet;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.PasswordAuthentication;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Modal non-minimizable dialog to request http authentication credentials
 */

public class PasswordAuthenticationDialog extends JDialog {
    
    private JLabel jlInfo = new JLabel("");
    private JTextField jtfUserName = new JTextField();
    private JPasswordField jpfPassword = new JPasswordField();
    private boolean userCancelled;

    public PasswordAuthenticationDialog() {
        initialize();
    }

    /**
     * Initialized the dialog components
     */
    
    public void initialize() {

        setTitle("IcedTea Java Plugin - Authorization needed to proceed");

        setLayout(new GridBagLayout());

        JLabel jlUserName = new JLabel("Username: ");
        JLabel jlPassword = new JLabel("Password: ");
        JButton jbOK = new JButton("OK");
        JButton jbCancel = new JButton("Cancel");

        jtfUserName.setSize(20, 10);
        jpfPassword.setSize(20, 10);

        GridBagConstraints c;
        
        c = new GridBagConstraints();
        c.fill = c.HORIZONTAL;
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
        c.fill = c.HORIZONTAL;
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
        c.fill = c.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(5, 5, 3, 3);
        c.weightx = 1.0;
        add(jpfPassword, c);

        c = new GridBagConstraints();
        c.anchor = c.SOUTHEAST;
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(5, 5, 3, 70);
        c.weightx = 0.0;
        add(jbCancel, c);
        
        c = new GridBagConstraints();
        c.anchor = c.SOUTHEAST;
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(5, 5, 3, 3);
        c.weightx = 0.0;
        add(jbOK, c);
        
        setMinimumSize(new Dimension(400,150));
        setMaximumSize(new Dimension(1024,150));
        setAlwaysOnTop(true);
        
        setSize(400,150);
        setLocationRelativeTo(null);

        // OK => read supplied info and pass it on
        jbOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userCancelled = false;
                dispose();
            }
        });
        
        // Cancel => discard supplied info and pass on an empty auth
        jbCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userCancelled = true;
                dispose();
            }
        });
        
        // "return" key in either user or password field => OK

        jtfUserName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userCancelled = false;
                dispose();
            }
        });
        
        jpfPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userCancelled = false;
                dispose();
            }
        });
    }

    /**
     * Present a dialog to the user asking them for authentication information
     * 
     * @param hostThe host for with authentication is needed
     * @param port The port being accessed
     * @param prompt The prompt (realm) as presented by the server
     * @param type The type of server (proxy/web)
     * @return PasswordAuthentication containing the credentials (empty credentials if user cancelled)
     */
    protected PasswordAuthentication askUser(String host, int port, String prompt, String type) {
        PasswordAuthentication auth = null;

        host += port != -1 ? ":" + port : "";

        // This frame is reusable. So reset everything first.
        userCancelled = true;
        jlInfo.setText("<html>The " + type + " server at " + host + " is requesting authentication. It says \"" + prompt + "\"</html>");

        try {
            SwingUtilities.invokeAndWait( new Runnable() {
                public void run() {
                    // show dialog to user
                    setVisible(true);
                }
            });
        
            PluginDebug.debug("password dialog shown");
            
            // wait until dialog is gone
            while (this.isShowing()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                }
            }
            
            PluginDebug.debug("password dialog closed");

            if (!userCancelled) {
                auth = new PasswordAuthentication(jtfUserName.getText(), jpfPassword.getText().toCharArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            // Nothing else we can do. Empty auth will be returned
        }

        return auth;
    }

    public static void main(String[] args) {
        PasswordAuthenticationDialog frame = new PasswordAuthenticationDialog();

        PasswordAuthentication auth = frame.askUser("127.0.0.1", 3128, "Password for local proxy", "proxy");

        System.err.println("Auth info: " + auth.getUserName() + ":" + new String(auth.getPassword()));
        System.exit(0);
    }
}
