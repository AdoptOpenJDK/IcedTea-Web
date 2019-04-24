/*
 Copyright (C) 2016 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.sourceforge.jnlp.util.ImageResources;
import net.adoptopenjdk.icedteaweb.ui.swing.ScreenFinder;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.JDialog;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class encapsulate viwable part of SecurityDialog, so it do not need to
 * extend it.
 * 
 * It is accepting commons setters for jdialog, but actually applying them right before it is created.
 * Obviously it do not have getters, but jdialog itself  should not be keeper of any information. SecurityPanel is.
 */
public class ViwableDialog {

    private JDialog jd = null;
    List<Runnable> operations = new ArrayList<Runnable>();

    public ViwableDialog() {
    }

    private JDialog createJDialog() {
        jd = new JDialog();
        jd.setName("ViwableDialog");
        SwingUtils.info(jd);
        jd.setIconImages(ImageResources.INSTANCE.getApplicationImages());
        
        for (Runnable operation : operations) {
            operation.run();
        }
        // prune operations. May throw NPE if operations used after createJDialog()
        operations = null;
        return jd;
    }

    public void setMinimumSize(final Dimension minimumSize) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.setMinimumSize(minimumSize);
            }
        });
    }

    public void pack() {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.pack();
            }
        });
    }

    public void setLocationRelativeTo(final Component c) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.setLocationRelativeTo(c);
            }
        });
    }

    public void show() {
        SwingUtils.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                if (jd == null) {
                    jd = createJDialog();
                }
                jd.setVisible(true);
            }
        });
    }

    /**
     * Called when the SecurityDialog is hidden - either because the user made a
     * choice (Ok, Cancel, etc) or closed the window
     */
    public void dispose() {
        // avoid reentrance:
        if (jd != null) {
            notifySelectionMade();

            jd.dispose();
            // recycle:
            jd = null;
        }
    }

    private final List<ActionListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Notify all the listeners that the user has made a decision using this
     * security dialog.
     */
    private void notifySelectionMade() {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(null);
        }
    }

    /**
     * Adds an {@link ActionListener} which will be notified if the user makes a
     * choice using this SecurityDialog. The listener should use
     * getValue() to actually get the user's response.
     *
     * @param listener another action listener to be listen to
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void add(final SecurityDialogPanel panel, final String constraints) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.add(panel, constraints);
            }
        });
    }

    public void setModalityType(final Dialog.ModalityType modalityType) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.setModalityType(modalityType);
            }
        });
    }

    public void setTitle(final String title) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.setTitle(title);
            }
        });
    }

    public void setDefaultCloseOperation(final int op) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.setDefaultCloseOperation(op);
            }
        });
    }

    private static void centerDialog(JDialog dialog) {
        ScreenFinder.centerWindowsToCurrentScreen(dialog);
    }

    public void centerDialog() {
        operations.add(new Runnable() {
            @Override
            public void run() {
                centerDialog(jd);
            }
        });
    }

    public void setResizable(final boolean b) {
        // not deferred: called when alive
        if (jd != null) {
            jd.setResizable(b);
        }
    }

    public void addWindowListener(final WindowAdapter adapter) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.addWindowListener(adapter);
            }
        });
    }

    public void addWindowFocusListener(final WindowAdapter adapter) {
        operations.add(new Runnable() {
            @Override
            public void run() {
                jd.addWindowFocusListener(adapter);
            }
        });
    }

}
