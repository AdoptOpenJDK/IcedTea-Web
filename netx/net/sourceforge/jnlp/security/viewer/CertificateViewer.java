/* CertificateViewer.java
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.UIManager;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

public class CertificateViewer extends JDialog {

    private boolean initialized = false;
    private static final String dialogTitle = R("CVCertificateViewer");

    CertificatePane panel;

    public CertificateViewer() {
        super((Frame) null, dialogTitle, true);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        panel = new CertificatePane(this);

        add(panel);

        pack();

        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    panel.focusOnDefaultButton();
                    gotFocus = true;
                }
            }
        };
        addWindowFocusListener(adapter);

        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void centerDialog() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();

        setLocation((screen.width - dialogSize.width) / 2,
                        (screen.height - dialogSize.height) / 2);
    }

    public static void showCertificateViewer() throws Exception {
        JNLPRuntime.initialize(true);
        setSystemLookAndFeel();

        CertificateViewer cv = new CertificateViewer();
        cv.setResizable(true);
        cv.centerDialog();
        cv.setVisible(true);
        cv.dispose();
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // don't worry if we can't.
        }
    }

    public static void main(String[] args) throws Exception {
        CertificateViewer.showCertificateViewer();
    }
}
