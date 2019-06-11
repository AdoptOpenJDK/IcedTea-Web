/*   Copyright (C) 2016 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataListener;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This dialog provides way to manage rememberable dialogues
 *
 *
 */
public class RememberableDialogueEditor extends JDialog {

    private final List<Class<? extends RememberableDialog>> allClasses;
    private final AppletSecurityActions actions;

    private AppletSecurityActions result;
    private final RememberableDialogueEditor self;
    private final JFrame frame;

    RememberableDialogueEditor(JFrame jFrame, boolean modal, Object dialogs) {
        super(jFrame, modal);
        frame=jFrame;
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        self = this;
        actions = (AppletSecurityActions) dialogs;
        allClasses = ClassFinder.findAllMatchingTypes(RememberableDialog.class);
        recreateGui();
        this.setLocationRelativeTo(jFrame);
    }

    private void recreateGui() {
        final Collection<Map.Entry<String, SavedRememberAction>> entries = actions.getEntries();
        final JDialog d = this;
        getContentPane().removeAll();
        d.setLayout(new GridLayout(0, 4));

        final List<Class<? extends RememberableDialog>> addableClasses = new ArrayList<>(allClasses);
        for (final Map.Entry<String, SavedRememberAction> entry : entries) {
            final String dialog = entry.getKey();
            for (int i = 0; i < addableClasses.size(); i++) {
                final Class<? extends RememberableDialog> get = addableClasses.get(i);
                String s = get.getSimpleName();
                if (s.equals(dialog)) {
                    JButton bb = new JButton("-");
                    bb.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            actions.removeAction(get);
                            recreateGui();
                        }
                    });
                    d.add(bb);
                    d.add(new JLabel(entry.getKey()));
                    final JComboBox<ExecuteAppletAction> cbb = new JComboBox<>(ExecuteAppletAction.values());
                    cbb.setSelectedItem(entry.getValue().getAction());
                    cbb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            actions.setAction(entry.getKey(), createRemember((ExecuteAppletAction) cbb.getSelectedItem(), entry));
                        }

                        private SavedRememberAction createRemember(ExecuteAppletAction nwValue, Map.Entry<String, SavedRememberAction> entry) {
                            return new SavedRememberAction(nwValue, entry.getValue().getSavedValue());
                        }
                    });
                    d.add(cbb);
                    JButton expertButton = new JButton(Translator.R("EPEexpert"));
                    expertButton.setToolTipText(Translator.R("EPEexpertHelp"));
                    expertButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Object result = JOptionPane.showInputDialog(self.frame, entry.getValue().getSavedValue(), Translator.R("EPEexpertHint"), JOptionPane.ERROR_MESSAGE, null, null, entry.getValue().getSavedValue());
                            if (result != null) {
                                String s = result.toString();
                                if (!s.trim().isEmpty()){
                                    actions.setAction(entry.getKey(), createRemember(entry.getValue().getAction(), s));
                                }
                            }
                        }
                        private SavedRememberAction createRemember(ExecuteAppletAction action, String newValue) {
                            return new SavedRememberAction(action, newValue);
                        }
                    });
                    d.add(expertButton);
                    addableClasses.remove(i);
                    i--;
                }
            }

        }

        ComboBoxModel<String> model = new ComboBoxModel<String>() {

            Object selected = null;

            @Override
            public void setSelectedItem(Object anItem) {
                for (int i = 0; i < addableClasses.size(); i++) {
                    Class<? extends RememberableDialog> get = addableClasses.get(i);
                    if (get.getSimpleName().equals(anItem)) {
                        selected = get.getSimpleName();
                    }

                }
            }

            @Override
            public Object getSelectedItem() {
                return selected;
            }

            @Override
            public int getSize() {
                return addableClasses.size();
            }

            @Override
            public String getElementAt(int index) {
                return addableClasses.get(index).getSimpleName();
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        };
        for (int x = 0; x < 4; x++) {
            Button b = new Button(Translator.R("EPEhelp" + (x + 1)));
            b.setEnabled(false);
            d.add(b);
        }
        final JComboBox<String> cb = new JComboBox<>(model);
        JButton b = new JButton("+");
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb.getSelectedIndex() < 0) {
                    return;
                }
                actions.setAction(addableClasses.get(cb.getSelectedIndex()), new SavedRememberAction(ExecuteAppletAction.NEVER, ExecuteAppletAction.NEVER.toChar()));
                recreateGui();
            }
        });
        d.add(b);
        d.add(cb);
        JButton save = new JButton(Translator.R("EPEsave"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = actions;
                self.setVisible(false);
            }
        });
        d.add(save);
        JButton cancel = new JButton(Translator.R("EPEcancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = null;
                self.setVisible(false);
            }
        });
        d.add(cancel);
        d.pack();

    }

    public AppletSecurityActions getResult() {
        return result;
    }

}
