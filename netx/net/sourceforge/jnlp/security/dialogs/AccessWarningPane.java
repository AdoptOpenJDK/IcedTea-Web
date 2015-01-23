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

package net.sourceforge.jnlp.security.dialogs;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.ShortcutDesc;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.XDesktopEntry;

/**
 * Provides a panel to show inside a SecurityDialog. These dialogs are
 * used to warn the user when either signed code (with or without signing
 * issues) is going to be run, or when service permission (file, clipboard,
 * printer, etc) is needed with unsigned code.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class AccessWarningPane extends SecurityDialogPanel {

    private Object[] extras;
    private JCheckBox desktopCheck;
    private JCheckBox menuCheck;
    HtmlShortcutPanel htmlPanelDesktop;
    HtmlShortcutPanel htmlPanelMenu;
    RememberPanel rememberPanel;
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
            publisher = file.getInformation().getVendor() != null ? 
                    file.getInformation().getVendor() + " " + R("SUnverified") : 
                    R("SNoAssociatedCertificate");
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

        ImageIcon icon = new ImageIcon((new sun.misc.Launcher()).getClassLoader().getResource("net/sourceforge/jnlp/resources/question.png"));
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


        final JButton run = new JButton(R("ButAllow"));
        final JButton cancel = new JButton(R("ButCancel"));
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        infoPanel.add(nameLabel, c);
        c.gridy++;
        infoPanel.add(publisherLabel, c);
        c.gridy++;
        infoPanel.add(fromLabel,c);
        c.gridy++;
        if (type == AccessType.CREATE_DESTKOP_SHORTCUT) {
            if (file.getInformation() != null &&  file.getInformation().getShortcut() != null && file.getInformation().getShortcut().onDesktop()) {
                desktopCheck = new JCheckBox(R("EXAWdesktopWants"));
                desktopCheck.setSelected(true);
            } else {
                desktopCheck = new JCheckBox(R("EXAWdesktopDontWants"));
                desktopCheck.setSelected(false);
            }

            if (file.getInformation() != null && file.getInformation().getShortcut() !=null && file.getInformation().getShortcut().toMenu()) {
                if (file.getInformation().getShortcut() != null && file.getInformation().getShortcut().getMenu() != null && file.getInformation().getShortcut().getMenu().getSubMenu() != null) {
                    menuCheck = new JCheckBox(R("EXAWsubmenu",file.getInformation().getShortcut().getMenu().getSubMenu()));
                } else {
                    menuCheck = new JCheckBox(R("EXAWmenuWants"));
                }
                menuCheck.setSelected(true);
            } else {
                menuCheck = new JCheckBox(R("EXAWmenuDontWants"));
                menuCheck.setSelected(false);
            }
            infoPanel.add(new JLabel("<html>___________________________________________________</html>"),c);
            c.gridy++;
            infoPanel.add(desktopCheck,c);
            c.gridy++;
            if (!JNLPRuntime.isWebstartApplication()) {
                htmlPanelDesktop = new HtmlShortcutPanel();
                infoPanel.add(htmlPanelDesktop, c);
                htmlPanelDesktop.setVisible(false);
                c.gridy++;
            }
            ActionListener al = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (desktopCheck.isSelected() || menuCheck.isSelected()){
                        run.setEnabled(true);
                        cancel.setEnabled(false);
                    } else {
                        run.setEnabled(false);
                        cancel.setEnabled(true);
                    }
                    
                }
            };
            desktopCheck.addActionListener(al);
            menuCheck.addActionListener(al);
            al.actionPerformed(null);
            infoPanel.add(menuCheck,c);
            c.gridy++;
            if (!JNLPRuntime.isWebstartApplication()) {
                htmlPanelMenu = new HtmlShortcutPanel();
                infoPanel.add(htmlPanelMenu, c);
                htmlPanelMenu.setVisible(false);
                c.gridy++;
            }
            infoPanel.add(new JLabel(R("EXAWsettingsInfo",
                    ShortcutDesc.deploymentJavawsShortcutToString(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_CREATE_DESKTOP_SHORTCUT)),
                    R("CPTabDesktopIntegration"))),c);
            c.gridy++;
            infoPanel.add(new JLabel(R("EXAWsettingsManage", R("CPTabMenuShortcuts"))),c);
            c.gridy++;
            infoPanel.validate();
        }
        rememberPanel = new RememberPanel();
        infoPanel.add(rememberPanel,c);
        c.gridy++;
        infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton showAdvanced = new JButton(R("ButAdvancedOptions"));
        showAdvanced.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                negateVisibility(rememberPanel);
                negateVisibility(htmlPanelDesktop);
                negateVisibility(htmlPanelMenu);
                AccessWarningPane.this.parent.pack();
                
            }

            private void negateVisibility(JComponent a) {
                if (a != null){
                    a.setVisible(!a.isVisible());
                }
            }
        }
        );
        //override the  createSetValueListener mechanism
        //TODO get rid of createSetValueListener completely.
        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setValue(getModifier(0)); //according to  createSetValueListener 0 is ok and 1 cancel
                parent.dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setValue(getModifier(1)); //according to  createSetValueListener 0 is ok and 1 cancel
                parent.dispose();
            }
        });
        initialFocusComponent = cancel;
        buttonPanel.add(run);
        buttonPanel.add(cancel);
        buttonPanel.add(showAdvanced);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);
        
        rememberPanel.setVisible(false);
        this.parent.pack();

    }

    private AccessWarningPaneComplexReturn getModifier(int button) {
        AccessWarningPaneComplexReturn ar = new AccessWarningPaneComplexReturn(button);
        if (desktopCheck != null) {
            if (htmlPanelDesktop != null) {
                //html
                ar.setDekstop(htmlPanelDesktop.getShortcutResult(desktopCheck.isSelected()));
            } else {
                //jnlp
                ar.setDekstop(new AccessWarningPaneComplexReturn.ShortcutResult(desktopCheck.isSelected()));
            }
        }
        if (menuCheck != null) {
            if (htmlPanelMenu != null) {
                //html
                ar.setMenu(htmlPanelMenu.getShortcutResult(menuCheck.isSelected()));
            } else {
                //jnlp
                ar.setMenu(new AccessWarningPaneComplexReturn.ShortcutResult(menuCheck.isSelected()));
            }
        }
        if (rememberPanel != null) {
            ar.setRember(rememberPanel.getShortcutResult());
        }
        return ar;
    }

    private class RememberPanel extends JPanel {
        // TODO: somehow tell the ApplicationInstance
        // to stop asking for permission
        // will be implemented likeALACAcanrember decission 
        // must be encoded in similar way as AWP is doing

        final JRadioButton byApp = new JRadioButton(R("EXAWrememberByApp"));
        final JRadioButton byPage = new JRadioButton(R("EXAWrememberByPage"));
        final JRadioButton dont = new JRadioButton(R("EXAWdontRemember"), true);

        public RememberPanel() {
            super(new FlowLayout(FlowLayout.CENTER, 1, 5));
            this.setBorder(new EmptyBorder(0, 0, 0, 0));
            this.add(byApp);
            this.add(byPage);
            this.add(dont);
            byApp.setToolTipText(R("EXAWrememberByAppTooltip"));
            byPage.setToolTipText(R("EXAWrememberByPageTooltip"));
            dont.setToolTipText(R("EXAWdontRememberTooltip"));
            final ButtonGroup bg = new ButtonGroup();
            bg.add(byApp);
            bg.add(byPage);
            bg.add(dont);
            this.validate();

        }

        public AccessWarningPaneComplexReturn.RemeberType getShortcutResult() {
            if (byApp.isSelected()) {
                return AccessWarningPaneComplexReturn.RemeberType.REMEMBER_BY_APP;
            } else if (byPage.isSelected()) {
                return AccessWarningPaneComplexReturn.RemeberType.REMEMBER_BY_DOMAIN;
            } else if (dont.isSelected()) {
                return AccessWarningPaneComplexReturn.RemeberType.REMEMBER_DONT;
            } else {
                return AccessWarningPaneComplexReturn.RemeberType.REMEMBER_DONT;
            }
        }

    }

    private class HtmlShortcutPanel extends JPanel {

        final JRadioButton browser = new JRadioButton(R("EXAWbrowser"), true);
        final JComboBox<String> browsers = new JComboBox<>(XDesktopEntry.BROWSERS);
        final JRadioButton jnlpGen = new JRadioButton(R("EXAWgenjnlp"));
        final JRadioButton jnlpHref = new JRadioButton(R("EXAWjnlphref"));
        final JRadioButton javawsHtml = new JRadioButton(R("EXAWhtml"));
        final JCheckBox fix = new JCheckBox(R("EXAWfixhref"));
        final ActionListener modifySecondaryControls = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (browser.isSelected()) {
                    browsers.setEnabled(true);
                } else {
                    browsers.setEnabled(false);
                }
                if (jnlpHref.isSelected()) {
                    fix.setEnabled(true);
                    fix.setSelected(true);
                } else {
                    fix.setEnabled(false);
                    fix.setSelected(false);
                }
            }
        };

        public HtmlShortcutPanel() {
            super(new FlowLayout(FlowLayout.CENTER, 1, 5));
            this.setBorder(new EmptyBorder(0, 0, 0, 0));
            addMainComponents();
            setTooltips();
            ButtonGroup bg = createRadiosGroup();
            // init checkbox
            modifySecondaryControls.actionPerformed(null);
            this.validate();

        }

        public AccessWarningPaneComplexReturn.ShortcutResult getShortcutResult(boolean mainResolution) {
            AccessWarningPaneComplexReturn.ShortcutResult r = new AccessWarningPaneComplexReturn.ShortcutResult(mainResolution);
            r.setBrowser((String) browsers.getSelectedItem());
            r.setFixHref(fix.isSelected());
            if (browser.isSelected()) {
                r.setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.BROWSER);
            } else if (jnlpGen.isSelected()) {
                r.setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.GENERATED_JNLP);
            } else if (jnlpHref.isSelected()) {
                r.setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JNLP_HREF);
            } else if (javawsHtml.isSelected()) {
                r.setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JAVAWS_HTML);
            }
            return r;
        }

        private ButtonGroup createRadiosGroup() {
            ButtonGroup bg = new ButtonGroup();
            bg.add(browser);
            bg.add(jnlpGen);
            bg.add(jnlpHref);
            bg.add(javawsHtml);
            setCheckboxModifierListener();
            return bg;
        }

        private void setCheckboxModifierListener() {
            browser.addActionListener(modifySecondaryControls);
            jnlpGen.addActionListener(modifySecondaryControls);
            jnlpHref.addActionListener(modifySecondaryControls);
            javawsHtml.addActionListener(modifySecondaryControls);
        }

        private void addMainComponents() {
            this.add(browser);
            browsers.setEditable(true);
            browsers.setSelectedItem(XDesktopEntry.getBrowserBin());
            this.add(browsers);
            this.add(jnlpGen);
            this.add(jnlpHref);
            this.add(javawsHtml);
            this.add(fix);
            if (parent.getFile() instanceof PluginBridge && ((PluginBridge)(parent.getFile())).haveDebugJnlp()){
                jnlpHref.setEnabled(true);
            } else {
                jnlpHref.setEnabled(false);
            }
        }

        private void setTooltips() {
            browser.setToolTipText(R("EXAWbrowserTolltip"));
            browsers.setToolTipText(R("EXAWbrowsersTolltip"));
            jnlpGen.setToolTipText(R("EXAWgeneratedTolltip"));
            jnlpHref.setToolTipText(R("EXAWhrefTolltip"));
            javawsHtml.setToolTipText(R("EXAWhtmlTolltip"));
            fix.setToolTipText(R("EXAWfixTolltip"));
        }

    }

}
