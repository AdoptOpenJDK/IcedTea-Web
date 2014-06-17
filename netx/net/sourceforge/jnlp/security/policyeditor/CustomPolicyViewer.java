/*Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.policyeditor;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * This implements a simple list viewer for custom policies, ie policies
 * that do not have a dedicated checkbox in PolicyEditor. It also allows
 * for adding new custom permissions and removing permissions.
 */
public class CustomPolicyViewer extends JFrame {

    private final Collection<CustomPermission> customPermissions = new TreeSet<>();
    private final JScrollPane scrollPane = new JScrollPane();
    private final DefaultListModel<CustomPermission> listModel = new DefaultListModel<>();
    private final JList<CustomPermission> list = new JList<>(listModel);
    private final JButton addButton = new JButton(), removeButton = new JButton(), closeButton = new JButton();
    private final JLabel listLabel = new JLabel();
    private final ActionListener addButtonAction, removeButtonAction, closeButtonAction;
    private final PolicyEditor parent;
    private final String codebase;

    /**
     * @param parent the parent PolicyEditor which created this CustomPolicyViewer
     * @param codebase the codebase for which these custom permissions are enabled
     * @param policyFile the PolicyFileModel
     */
    public CustomPolicyViewer(final PolicyEditor parent, final String codebase) {
        super();
        Objects.requireNonNull(parent);
        Objects.requireNonNull(codebase);
        this.parent = parent;
        this.codebase = codebase;
        setLayout(new GridBagLayout());
        setTitle(R("PECPTitle"));
        customPermissions.addAll(parent.getCustomPermissions(codebase));
        for (final CustomPermission perm : customPermissions) {
            listModel.addElement(perm);
        }

        addButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String prefill = R("PECPType") + " " + R("PECPTarget") + " [" + R("PECPActions") + "]";
                String string = JOptionPane.showInputDialog(CustomPolicyViewer.this, R("PECPPrompt"), prefill);
                if (string == null || string.isEmpty()) {
                    return;
                }
                string = string.replaceAll("\\s+", " ").trim();
                final String[] parts = string.split(" ");
                if (parts.length < 2) {
                    return;
                }
                final String type = parts[0], target = parts[1];
                final String actions;
                if (parts.length > 2) {
                    actions = parts[2];
                } else {
                    actions = "";
                }
                final CustomPermission perm = new CustomPermission(type, target, actions);
                addCustomPermission(perm);
            }
        };
        addButton.setText(R("PECPAddButton"));
        addButton.addActionListener(addButtonAction);

        removeButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final CustomPermission selected = list.getSelectedValue();
                if (selected == null) {
                    return;
                }
                removeCustomPermission(selected);
            }
        };
        removeButton.setText(R("PECPRemoveButton"));
        removeButton.addActionListener(removeButtonAction);

        closeButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                quit();
            }
        };
        closeButton.setText(R("PECPCloseButton"));
        closeButton.addActionListener(closeButtonAction);

        final String codebaseText;
        if (codebase.trim().isEmpty()) {
            codebaseText = R("PEGlobalSettings");
        } else {
            codebaseText = codebase;
        }
        listLabel.setText(R("PECPListLabel", codebaseText));

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        list.setSelectedIndex(0);
        setupLayout();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                quit();
            }
        });
    }

    private void setupLayout() {
        final GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        final EmptyBorder border = new EmptyBorder(2, 2, 2, 2);
        listLabel.setBorder(border);
        add(listLabel, labelConstraints);

        final GridBagConstraints scrollPaneConstraints = new GridBagConstraints();
        scrollPaneConstraints.gridx = 0;
        scrollPaneConstraints.gridy = 1;
        scrollPaneConstraints.weightx = 1;
        scrollPaneConstraints.weighty = 1;
        scrollPaneConstraints.gridwidth = 3;
        scrollPaneConstraints.fill = GridBagConstraints.BOTH;
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(list);
        add(scrollPane, scrollPaneConstraints);

        final GridBagConstraints addButtonConstraints = new GridBagConstraints();
        addButtonConstraints.gridx = 0;
        addButtonConstraints.gridy = scrollPaneConstraints.gridy + 1;
        addButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(addButton, addButtonConstraints);

        final GridBagConstraints removeButtonConstraints = new GridBagConstraints();
        removeButtonConstraints.gridx = addButtonConstraints.gridx + 1;
        removeButtonConstraints.gridy = addButtonConstraints.gridy;
        removeButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(removeButton, removeButtonConstraints);

        final GridBagConstraints closeButtonConstraints = new GridBagConstraints();
        closeButtonConstraints.gridx = removeButtonConstraints.gridx + 1;
        closeButtonConstraints.gridy = removeButtonConstraints.gridy;
        closeButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(closeButton, closeButtonConstraints);

        pack();
        setMinimumSize(getPreferredSize());
    }

    private void quit() {
        parent.customPolicyViewerClosing();
        dispose();
    }

    /**
     * Update the custom permissions map. Used by the Custom Policy Viewer to update its parent
     * PolicyEditor to changes it has made
     * @param codebase the codebase for which changes were made
     * @param permissions the permissions granted to this codebase
     */
    private void updateCustomPermissions() {
        parent.setChangesMade(true);
        parent.clearCustomPermissions(codebase);
        for (final CustomPermission permission : customPermissions) {
            parent.addCustomPermission(codebase, permission);
        }
    }

    void addCustomPermission(final CustomPermission permission) {
        Objects.requireNonNull(permission);
        if (customPermissions.add(permission)) {
            listModel.addElement(permission);
            updateCustomPermissions();
        }
        list.setSelectedValue(permission, true);
    }

    void removeCustomPermission(final CustomPermission permission) {
        Objects.requireNonNull(permission);
        customPermissions.remove(permission);
        listModel.removeElement(permission);
        updateCustomPermissions();
    }

    Collection<CustomPermission> getCopyOfCustomPermissions() {
        return new TreeSet<>(customPermissions);
    }

}
