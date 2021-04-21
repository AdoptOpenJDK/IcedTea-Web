/* Copyright (C) 2013 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel;


import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogPanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SetValueHandler;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.ExtendedAppletSecurityHelp;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberPanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberPanelResult;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.ScreenFinder;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/*
 * This class is meant to provide a common layout and functionality for warning dialogs
 * that appear when the user needs to confirm the running of applets/applications.
 * Subclasses include UnsignedAppletTrustWarningPanel, for unsigned plugin applets, and
 * PartiallySignedAppTrustWarningPanel, for partially signed JNLP applications as well as
 * plugin applets. New implementations should be added to the unit test at
 * unit/net/sourceforge/jnlp/security/AppTrustWarningPanelTest
 */
public abstract class AppTrustWarningPanel extends SecurityDialogPanel implements RememberableDialog{

    private final static Logger LOG = LoggerFactory.getLogger(AppTrustWarningPanel.class);

    protected int PANE_WIDTH = 500;

    protected int TOP_PANEL_HEIGHT = 60;
    protected int INFO_PANEL_HEIGHT = 160;
    protected int INFO_PANEL_HINT_HEIGHT = 25;
    protected int QUESTION_PANEL_HEIGHT = 35;

    protected List<JButton> buttons;
    protected JButton allowButton;
    protected JButton rejectButton;
    protected JButton helpButton;
    protected RememberPanel rememberPanel;

    protected JNLPFile file;

    /*
     * Subclasses should call addComponents() IMMEDIATELY after calling the super() constructor!
     */
    public AppTrustWarningPanel(JNLPFile file, SecurityDialog securityDialog) {
        super(securityDialog);
        this.file = file;
        this.parent = securityDialog;
        rememberPanel = new RememberPanel(file.getCodeBase());
        this.buttons = new ArrayList<>();

        allowButton = new JButton(R("ButProceed"));
        rejectButton = new JButton(R("ButCancel"));
        helpButton = new JButton(R("APPEXTSECguiPanelHelpButton"));


        allowButton.addActionListener(SetValueHandler.createSetValueListener(parent,
                YesNoSandboxLimited.yes()));
        rejectButton.addActionListener(SetValueHandler.createSetValueListener(parent,
                YesNoSandboxLimited.no()));
        
        helpButton.addActionListener(getHelpButtonAction());

        buttons.add(allowButton);
        buttons.add(rejectButton);
        buttons.add(helpButton);
    }

    /*
     * Provides an image to be displayed near the upper left corner of the dialog.
     */
    protected abstract ImageIcon getInfoImage();

    /*
     * Provides a short description of why the dialog is appearing. The message is expected to be HTML-formatted.
     */
    protected abstract String getTopPanelText();

    /*
     * Provides in-depth information on why the dialog is appearing. The message is expected to be HTML-formatted.
     */
    protected abstract String getInfoPanelText();

    /*
     * This provides the text for the final prompt to the user. The message is expected to be HTML formatted.
     * The user's action is a direct response to this question.
     */
    protected abstract String getQuestionPanelText();

    public final JButton getAllowButton() {
        return allowButton;
    }

    public final JButton getRejectButton() {
        return rejectButton;
    }

    protected ActionListener getHelpButtonAction() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog d = new ExtendedAppletSecurityHelp(null, false, "dialogue");
                ScreenFinder.centerWindowsToCurrentScreen(d);
                d.setVisible(true);
            }
        };
    }

    private void setupTopPanel() {
        final String topLabelText = getTopPanelText();

        JLabel topLabel = new JLabel(topLabelText, getInfoImage(),
                SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(), Font.BOLD, 12));
        topLabel.setForeground(Color.BLACK);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(PANE_WIDTH, TOP_PANEL_HEIGHT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(topPanel);
    }

    protected String getAppletTitle() {
        String title;
        try {
            title = file.getInformation().getTitle();
        } catch (Exception e) {
            title = "";
        }
        return title;
    }

    private void setupInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        String titleText = getAppletTitle();
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));

        String infoLabelText = getInfoPanelText();
        JEditorPane infoLabel = new JEditorPane("text/html", htmlWrap(infoLabelText));
        infoLabel.setBackground(infoPanel.getBackground());
        infoLabel.setEditable(false);
        infoLabel.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                try {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                } catch (IOException | URISyntaxException ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                }
            }
        });

        int panelHeight = titleLabel.getHeight() + INFO_PANEL_HEIGHT + INFO_PANEL_HINT_HEIGHT;
        infoPanel.add(titleLabel, BorderLayout.PAGE_START);
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(PANE_WIDTH, panelHeight));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(infoPanel);
    }

    private void setupQuestionsPanel() {
        JPanel questionPanel = new JPanel(new BorderLayout());

        final String questionPanelText = getQuestionPanelText();
        questionPanel.add(new JLabel(questionPanelText), BorderLayout.EAST);

        questionPanel.setPreferredSize(new Dimension(PANE_WIDTH, QUESTION_PANEL_HEIGHT));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        add(questionPanel);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        for (final JButton button : buttons) {
            buttonPanel.add(button);
        }

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        return buttonPanel;
    }

    // Set up 'Remember Option' checkbox & Proceed/Cancel buttons
    private void setupButtonAndCheckBoxPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(rememberPanel, BorderLayout.SOUTH);
        outerPanel.add(createButtonPanel(), BorderLayout.EAST);

        add(outerPanel);
    }

    /**
     * Creates the actual GUI components, and adds it to this panel. This should
     * be called by all subclasses IMMEDIATELY after calling the super()
     * constructor!
     */
    protected final void addComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setupTopPanel();
        setupInfoPanel();
        setupQuestionsPanel();
        setupButtonAndCheckBoxPanel();
    }

    @Override
    public RememberPanelResult getRememberAction() {
        return rememberPanel.getRememberAction();
    }

    @Override
    public JNLPFile getFile() {
        return file;
    }

    @Override
    public DialogResult getValue() {
        return parent.getValue();
    }
    
    @Override
    public DialogResult readValue(String s) {
        return YesNoSandboxLimited.readValue(s);
    }

    @Override
    public DialogResult getDefaultNegativeAnswer() {
        return YesNoSandboxLimited.no();
    }

    @Override
    public DialogResult getDefaultPositiveAnswer() {
        return YesNoSandboxLimited.yes();
    }

    @Override
    public DialogResult readFromStdIn(String what) {
        return YesNoSandboxLimited.readValue(what);
    }
    
    @Override
    public String helpToStdIn() {
        return YesNoSandboxLimited.yes().getAllowedValues().toString();
    }
    
}
