/* AccessWarningPane.java
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * Provides a panel to show inside a SecurityDialog. These dialogs are
 * used to warn the user when either signed code (with or without signing
 * issues) is going to be run, or when service permission (file, clipboard,
 * printer, etc) is needed with unsigned code.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class AccessWarningPane extends SecurityDialogPanel {

    JCheckBox alwaysAllow;
    Object[] extras;

    public AccessWarningPane(SecurityDialog x, CertVerifier certVerifier) {
        super(x, certVerifier);
        addComponents();
    }

    public AccessWarningPane(SecurityDialog x, Object[] extras, CertVerifier certVerifier) {
        super(x, certVerifier);
        this.extras = extras;
        addComponents();
    }

    /**
     * Creates the actual GUI components, and adds it to this panel
     */
    private void addComponents() {
        AccessType type = parent.getAccessType();
        JNLPFile file = parent.getFile();

        String name = "";
        String publisher = "";
        String from = "";

        //We don't worry about exceptions when trying to fill in
        //these strings -- we just want to fill in as many as possible.
        try {
            name = file.getInformation().getTitle() != null ? file.getInformation().getTitle() : R("SNoAssociatedCertificate");
        } catch (Exception e) {
        }

        try {
            publisher = file.getInformation().getVendor() != null ? file.getInformation().getVendor() : R("SNoAssociatedCertificate");
        } catch (Exception e) {
        }

        try {
            from = !file.getInformation().getHomepage().toString().equals("") ? file.getInformation().getHomepage().toString() : file.getSourceLocation().getAuthority();
        } catch (Exception e) {
            from = file.getSourceLocation().getAuthority();
        }

        //Top label
        String topLabelText = "";
        switch (type) {
            case READ_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    topLabelText = R("SFileReadAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    topLabelText = R("SFileReadAccess", R("AFileOnTheMachine"));
                }
                break;
            case WRITE_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    topLabelText = R("SFileWriteAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    topLabelText = R("SFileWriteAccess", R("AFileOnTheMachine"));
                }
                break;
            case CREATE_DESTKOP_SHORTCUT:
                topLabelText = R("SDesktopShortcut");
                break;
            case CLIPBOARD_READ:
                topLabelText = R("SClipboardReadAccess");
                break;
            case CLIPBOARD_WRITE:
                topLabelText = R("SClipboardWriteAccess");
                break;
            case PRINTER:
                topLabelText = R("SPrinterAccess");
                break;
            case NETWORK:
                if (extras != null && extras.length >= 0)
                    topLabelText = R("SNetworkAccess", extras[0]);
                else
                    topLabelText = R("SNetworkAccess", "(address here)");
        }

        ImageIcon icon = new ImageIcon((new sun.misc.Launcher()).getClassLoader().getResource("net/sourceforge/jnlp/resources/warning.png"));
        JLabel topLabel = new JLabel(htmlWrap(topLabelText), icon, SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                        Font.BOLD, 12));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(450, 100));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //application info
        JLabel nameLabel = new JLabel(R("Name") + ":   " + name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel publisherLabel = new JLabel(R("Publisher") + ": " + publisher);
        publisherLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel fromLabel = new JLabel(R("From") + ":   " + from);
        fromLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        alwaysAllow = new JCheckBox(R("AlwaysAllowAction"));
        alwaysAllow.setEnabled(false);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.add(nameLabel);
        infoPanel.add(publisherLabel);
        infoPanel.add(fromLabel);
        infoPanel.add(alwaysAllow);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton run = new JButton(R("ButAllow"));
        JButton cancel = new JButton(R("ButCancel"));
        run.addActionListener(createSetValueListener(parent, 0));
        run.addActionListener(new CheckBoxListener());
        cancel.addActionListener(createSetValueListener(parent, 1));
        initialFocusComponent = cancel;
        buttonPanel.add(run);
        buttonPanel.add(cancel);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);

    }

    private class CheckBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (alwaysAllow != null && alwaysAllow.isSelected()) {
                // TODO: somehow tell the ApplicationInstance
                // to stop asking for permission
            }
        }
    }

}
