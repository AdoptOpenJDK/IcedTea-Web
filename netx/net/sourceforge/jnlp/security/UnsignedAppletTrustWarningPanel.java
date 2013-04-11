/* Copyright (C) 2013 Red Hat, Inc.

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
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteUnsignedApplet;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;

public class UnsignedAppletTrustWarningPanel extends JPanel {

    /*
     * Details of decided action.
     */
    public static class UnsignedWarningAction {
        private ExecuteUnsignedApplet action;
        private boolean applyToCodeBase;

        public UnsignedWarningAction(ExecuteUnsignedApplet action,
                boolean applyToCodeBase) {
            this.action = action;
            this.applyToCodeBase = applyToCodeBase;
        }

        public ExecuteUnsignedApplet getAction() {
            return action;
        }
        public boolean rememberForCodeBase() {
            return applyToCodeBase;
        }
    }

    /*
     * Callback for when action is decided.
     */
    public static interface ActionChoiceListener {
        void actionChosen(UnsignedWarningAction action);
    }

    private final int PANE_WIDTH = 500;

    private final int TOP_PANEL_HEIGHT = 60;
    private final int INFO_PANEL_HEIGHT = 100;
    private final int INFO_PANEL_HINT_HEIGHT = 25;
    private final int QUESTION_PANEL_HEIGHT = 35;

    private JButton allowButton;
    private JButton rejectButton;
    private JCheckBox permanencyCheckBox;
    private JRadioButton applyToAppletButton;
    private JRadioButton applyToCodeBaseButton;

    private PluginBridge file;

    private ActionChoiceListener actionChoiceListener;

    public UnsignedAppletTrustWarningPanel(PluginBridge file, ActionChoiceListener actionChoiceListener) {

        this.file = file;
        this.actionChoiceListener = actionChoiceListener;

        addComponents();
    }
    
    public JButton getAllowButton() {
        return allowButton;
    }

    public JButton getRejectButton() {
        return rejectButton;
    }

    private String htmlWrap(String text) {
        return "<html>" + text + "</html>";
    }

    private ImageIcon infoImage() {
        final String location = "net/sourceforge/jnlp/resources/info-small.png";
        final ClassLoader appLoader = new sun.misc.Launcher().getClassLoader();
        return new ImageIcon(appLoader.getResource(location));
    }

    private void setupTopPanel() {
        final String topLabelText = R("SUnsignedSummary");

        JLabel topLabel = new JLabel(htmlWrap(topLabelText), infoImage(),
                SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(), Font.BOLD, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(PANE_WIDTH, TOP_PANEL_HEIGHT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(topPanel);
    }

    private void setupInfoPanel() {
        String infoLabelText = R("SUnsignedDetail", file.getCodeBase());
        ExecuteUnsignedApplet rememberedAction = UnsignedAppletTrustConfirmation.getStoredAction(file);
        int panelHeight = INFO_PANEL_HEIGHT;
        if (rememberedAction == ExecuteUnsignedApplet.YES) {
            infoLabelText += "<br>" + R("SUnsignedAllowedBefore");
            panelHeight += INFO_PANEL_HINT_HEIGHT;
        } else if (rememberedAction == ExecuteUnsignedApplet.NO) {
            infoLabelText += "<br>" + R("SUnsignedRejectedBefore");
            panelHeight += INFO_PANEL_HINT_HEIGHT;
        }

        JLabel infoLabel = new JLabel(htmlWrap(infoLabelText));
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(PANE_WIDTH, panelHeight));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(infoPanel);
    }

    private void setupQuestionsPanel() {
        JPanel questionPanel = new JPanel(new BorderLayout());

        questionPanel.add(new JLabel(htmlWrap(R("SUnsignedQuestion"))), BorderLayout.EAST);

        questionPanel.setPreferredSize(new Dimension(PANE_WIDTH, QUESTION_PANEL_HEIGHT));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(questionPanel);
    }

    private JPanel createMatchOptionsPanel() {
        JPanel matchOptionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        ButtonGroup group = new ButtonGroup();
        applyToAppletButton = new JRadioButton(R("SRememberAppletOnly"));
        applyToAppletButton.setSelected(true);
        applyToAppletButton.setEnabled(false); // Start disabled until 'Remember this option' is selected

        applyToCodeBaseButton = new JRadioButton(R("SRememberCodebase"));
        applyToCodeBaseButton.setEnabled(false);

        group.add(applyToAppletButton);
        group.add(applyToCodeBaseButton);

        matchOptionsPanel.add(applyToAppletButton);
        matchOptionsPanel.add(applyToCodeBaseButton);

        return matchOptionsPanel;
    }

    private JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        permanencyCheckBox = new JCheckBox(htmlWrap(R("SRememberOption")));
        permanencyCheckBox.addActionListener(permanencyListener());
        checkBoxPanel.add(permanencyCheckBox);

        return checkBoxPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        allowButton = new JButton(R("ButProceed"));
        rejectButton = new JButton(R("ButCancel"));

        allowButton.addActionListener(chosenActionSetter(true));
        rejectButton.addActionListener(chosenActionSetter(false));

        buttonPanel.add(allowButton);
        buttonPanel.add(rejectButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return buttonPanel;
    }

    // Set up 'Remember Option' checkbox & Proceed/Cancel buttons
    private void setupButtonAndCheckBoxPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel rememberPanel = new JPanel(new GridLayout(2 /*rows*/, 1 /*column*/));
        rememberPanel.add(createCheckBoxPanel());
        rememberPanel.add(createMatchOptionsPanel());
        rememberPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        outerPanel.add(rememberPanel, BorderLayout.WEST);
        outerPanel.add(createButtonPanel(), BorderLayout.EAST);

        add(outerPanel);
    }

    /**
     * Creates the actual GUI components, and adds it to this panel
     */
    private void addComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setupTopPanel();
        setupInfoPanel();
        setupQuestionsPanel();
        setupButtonAndCheckBoxPanel();
    }

    // Toggles whether 'match applet' or 'match codebase' options are greyed out
    private ActionListener permanencyListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyToAppletButton.setEnabled(permanencyCheckBox.isSelected());
                applyToCodeBaseButton.setEnabled(permanencyCheckBox.isSelected());
            }
        };
    }
    // Sets action depending on allowApplet + checkbox state
    private ActionListener chosenActionSetter(final boolean allowApplet) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExecuteUnsignedApplet action;

                if (allowApplet) {
                    action = permanencyCheckBox.isSelected() ? ExecuteUnsignedApplet.ALWAYS : ExecuteUnsignedApplet.YES;
                } else {
                    action = permanencyCheckBox.isSelected() ? ExecuteUnsignedApplet.NEVER : ExecuteUnsignedApplet.NO;
                }

                boolean applyToCodeBase = applyToCodeBaseButton.isSelected();
                actionChoiceListener.actionChosen(new UnsignedWarningAction(action, applyToCodeBase));
            }
        };
    }
}