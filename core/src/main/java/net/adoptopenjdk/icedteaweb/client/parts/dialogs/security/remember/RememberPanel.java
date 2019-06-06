/*
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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogPanel;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class RememberPanel extends JPanel implements RememberActionProvider {

    private final static Logger LOG = LoggerFactory.getLogger(RememberPanel.class);

    protected JCheckBox permanencyCheckBox;
    protected JRadioButton applyToAppletButton;
    protected JRadioButton applyToCodeBaseButton;
    private final URL codebase;

    public RememberPanel(URL codebase) {
        this((codebase == null) ? null : codebase.toExternalForm());

    }

    public RememberPanel(String codebase) {
        super(new GridLayout(2 /*rows*/, 1 /*column*/));
        this.codebase = initCodebase(codebase);
        this.add(createCheckBoxPanel(), BorderLayout.WEST);
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        this.add(createMatchOptionsPanel());
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (this.codebase == null) {
            applyToCodeBaseButton.setVisible(false);
        }

    }

    private JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = new JPanel(new BorderLayout());

        permanencyCheckBox = new JCheckBox(SecurityDialogPanel.htmlWrap(R("SRememberOption")));
        permanencyCheckBox.addActionListener(permanencyListener());
        checkBoxPanel.add(permanencyCheckBox, BorderLayout.SOUTH);

        return checkBoxPanel;
    }

    private JPanel createMatchOptionsPanel() {
        JPanel matchOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        ButtonGroup group = new ButtonGroup();
        applyToAppletButton = new JRadioButton(R("SRememberAppletOnly"));
        applyToAppletButton.setSelected(true);
        applyToAppletButton.setEnabled(false); // Start disabled until 'Remember this NumberOfArguments' is selected

        applyToCodeBaseButton = new JRadioButton(SecurityDialogPanel.htmlWrap(R("SRememberCodebase", codebase)));
        applyToCodeBaseButton.setEnabled(false);

        group.add(applyToAppletButton);
        group.add(applyToCodeBaseButton);

        matchOptionsPanel.add(applyToAppletButton);
        matchOptionsPanel.add(applyToCodeBaseButton);

        return matchOptionsPanel;
    }

  
    // Toggles whether 'match applet' or 'match codebase' options are greyed out
    protected ActionListener permanencyListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyToAppletButton.setEnabled(permanencyCheckBox.isSelected());
                applyToCodeBaseButton.setEnabled(permanencyCheckBox.isSelected());
            }
        };
    }

    public boolean isAlwaysSelected() {
        return permanencyCheckBox.isSelected();
    }

    public boolean isCodebaseSelected() {
        return applyToCodeBaseButton.isSelected();
    }


    private URL initCodebase(String codebase) {
        if (codebase != null) {
            try {
                return new URL(codebase);
            } catch (MalformedURLException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
        }
        return null;
    }

    @Override
    public RememberPanelResult getRememberAction() {
        return new RememberPanelResult(permanencyCheckBox.isSelected(), applyToCodeBaseButton.isSelected());
    }

}
