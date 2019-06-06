/* 
 Copyright (C) 2012 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.browser.LinkingBrowser;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesCancelSkip;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class InetSecurity511Panel extends SecurityDialogPanel {

    private final static Logger LOG = LoggerFactory.getLogger(InetSecurity511Panel.class);

    private static final String INFO_LINK = "https://tools.ietf.org/html/rfc6585#section-6";
    private static boolean skip = false;
    private final LinkingBrowser tabes;

    public static boolean isSkip() {
        return skip;
    }

    public InetSecurity511Panel(final SecurityDialog sd, final URL url) {
        super(sd);
        if (sd != null) {
            //for testing purposes
            sd.setValue(YesCancelSkip.yes());
        }
        tabes = new LinkingBrowser(url, false);
        this.add(tabes);
        JPanel menu = new JPanel();
        JButton done = new JButton(Translator.R("ButDone"));
        done.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (sd != null) {
                    sd.setValue(YesCancelSkip.yes());
                    parent.getViwableDialog().dispose();
                }
            }
        });
        JButton noExit = new JButton(Translator.R("Exit511"));
        noExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (sd != null) {
                    sd.setValue(YesCancelSkip.cancel());
                    parent.getViwableDialog().dispose();
                }
            }
        });
        menu.add(done);
        menu.add(noExit);

        final JCheckBox ignoreInSession = new JCheckBox(Translator.R("Ignore511"));
        ignoreInSession.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                skip = ignoreInSession.isSelected();
            }
        });
        menu.add(ignoreInSession);
        this.add(menu, BorderLayout.SOUTH);
        JLabel title = new JLabel(htmlWrap(Translator.R("Header511", INFO_LINK)));
        title.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    tabes.getBrowser().gotoUrl(INFO_LINK);
                } else if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(INFO_LINK));
                    } catch (Exception ex) {
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                        if (!JNLPRuntime.isHeadless()) {
                            JOptionPane.showMessageDialog(null, ex);
                        }
                    }
                }
            }

        });
        this.add(title, BorderLayout.NORTH);
        if (sd != null) {
            //for testing purposes
            sd.getViwableDialog().pack();
        }
    }

    @Override
    public DialogResult getDefaultNegativeAnswer() {
        return YesCancelSkip.cancel();
    }

    @Override
    public DialogResult getDefaultPositiveAnswer() {
        return YesCancelSkip.yes();
    }

    @Override
    //skip is only for commandline to pass skip to the static field
    public DialogResult readFromStdIn(String what) {
        YesCancelSkip l = YesCancelSkip.readValue(what);
        if (l.compareValue(Primitive.SKIP)) {
            skip = true;
            l = YesCancelSkip.yes();
        }
        return l;
    }

    @Override
    public String helpToStdIn() {
        return YesCancelSkip.yes().getAllowedValues().toString();
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(Translator.R("Headless511line0")).append("\n");
        List<String> usrls = tabes.getLinksPanel().getAllUrls();
        for (String usrl : usrls) {
            sb.append(usrl).append("\n");
        }
        sb.append(super.getText()).append("\n");
        sb.append(Translator.R("Headless511line1")).append("\n");
        sb.append(Translator.R("Headless511line2")).append("\n");
        sb.append(Translator.R("Headless511line3", CommandLineOptions.BROWSER.getOption())).append("\n");

        return sb.toString();
    }

}
