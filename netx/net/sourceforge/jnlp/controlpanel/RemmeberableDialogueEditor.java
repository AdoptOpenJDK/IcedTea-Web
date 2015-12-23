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
package net.sourceforge.jnlp.controlpanel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.ListDataListener;
import net.sourceforge.jnlp.security.dialogs.remember.AppletSecurityActions;
import net.sourceforge.jnlp.security.dialogs.remember.ExecuteAppletAction;
import net.sourceforge.jnlp.security.dialogs.remember.RememberableDialog;
import net.sourceforge.jnlp.security.dialogs.remember.SavedRememberAction;

/**
 * This dialog provides way to manage rememberable dialogues
 *
 *
 */
public class RemmeberableDialogueEditor extends JDialog {

    private final List<Class<? extends RememberableDialog>> allClasses;
    private final AppletSecurityActions actions;

    RemmeberableDialogueEditor(JFrame jFrame, boolean modal, Object dialogs) {
        super(jFrame, modal);
        actions = (AppletSecurityActions) dialogs;
        allClasses = ClassFinder.findAllMatchingTypes(RememberableDialog.class);
        recreateGui();
    }

    private void recreateGui() {
        final Collection<Map.Entry<String, SavedRememberAction>> entries = actions.getEntries();
        final JDialog d = this;
        getContentPane().removeAll();
        d.setLayout(new GridLayout(0, 4));
        

        final List<Class<? extends RememberableDialog>> addedBleClasses = new ArrayList<>(allClasses);
        for (Map.Entry<String, SavedRememberAction> entry : entries) {
            String dialog = entry.getKey();
            for (int i = 0; i < addedBleClasses.size(); i++) {
                final Class<? extends RememberableDialog> get = addedBleClasses.get(i);
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
                    JComboBox cbb = new JComboBox(ExecuteAppletAction.values());
                    cbb.setSelectedItem(entry.getValue().getAction());
                    d.add(cbb);
                    d.add(new JButton("Expert edit"));
                    addedBleClasses.remove(i);
                    i--;
                }
            }

        }

        ComboBoxModel<String> model = new ComboBoxModel<String>() {

            Object selected = null;

            @Override
            public void setSelectedItem(Object anItem) {
                for (int i = 0; i < addedBleClasses.size(); i++) {
                    Class<? extends RememberableDialog> get = addedBleClasses.get(i);
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
                return addedBleClasses.size();
            }

            @Override
            public String getElementAt(int index) {
                return addedBleClasses.get(index).getSimpleName();
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        };
        final JComboBox<String> cb = new JComboBox<>(model);
        JButton b = new JButton("+");
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                actions.setAction(addedBleClasses.get(cb.getSelectedIndex()), new SavedRememberAction(ExecuteAppletAction.NEVER, ExecuteAppletAction.NEVER.toChar()));
                recreateGui();
            }
        });
        d.add(b);
        d.add(cb);
        d.pack();

    }

}
