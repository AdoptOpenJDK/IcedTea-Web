/* JeditorPaneBasedExceptionDialog.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.sourceforge.jnlp.splashscreen.parts;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.about.AboutDialog;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.BasicExceptionDialog;
import net.sourceforge.jnlp.util.logging.OutputController;

public class JEditorPaneBasedExceptionDialog extends JDialog implements HyperlinkListener {

    // components
    private JButton closeButton;
    private JButton closeAndCopyButton;
    private JButton homeButton;
    private JButton aboutButton;
    private JButton consoleButton;
    private JButton cacheButton;
    private JEditorPane htmlErrorAndHelpPanel;
    private JLabel exceptionLabel;
    private JLabel iconLabel;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JScrollPane htmlPaneScroller;
    // End of components declaration
    private final String message;
    private final Throwable exception;
    private final Date shown;
    private final String anotherInfo;

    /** Creates new form JEditorPaneBasedExceptionDialog */
    public JEditorPaneBasedExceptionDialog(java.awt.Frame parent, boolean modal, Throwable ex, InformationElement information, String anotherInfo) {
        super(parent, modal);
        shown = new Date();
        initComponents();
        htmlErrorAndHelpPanel.setContentType("text/html");
        htmlErrorAndHelpPanel.setEditable(false);
        this.anotherInfo=anotherInfo;
        List<String> l = infoElementToList(information);
        this.message = getText(ex, l, anotherInfo, shown);
        this.exception = ex;
        if (exception == null) {
            closeAndCopyButton.setVisible(false);
        }
        htmlErrorAndHelpPanel.setText(message);
        //htmlPaneScroller.getVerticalScrollBar().setValue(1);
        htmlErrorAndHelpPanel.setCaretPosition(0);
        try {
            Icon icon = new ImageIcon(this.getClass().getResource("/net/sourceforge/jnlp/resources/warning.png"));
            iconLabel.setIcon(icon);
        } catch (Exception lex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, lex);
        }
        htmlErrorAndHelpPanel.addHyperlinkListener(this);
        homeButton.setVisible(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


    }

    static List<String> infoElementToList(InformationElement information) {
        List<String> l = null;
        if (information != null) {
            l = information.getHeader();
            InfoItem ii = information.getLongestDescriptionForSplash();
            if (ii != null) {
                l.add(ii.toNiceString());
            }
        }
        return l;
    }

    private void initComponents() {

        topPanel = new JPanel();
        closeButton = new JButton();
        closeAndCopyButton = new JButton();
        mainPanel = new JPanel();
        exceptionLabel = new JLabel();
        iconLabel = new JLabel();
        bottomPanel = new JPanel();
        htmlPaneScroller = new JScrollPane();
        htmlErrorAndHelpPanel = new JEditorPane();
        homeButton = new JButton();
        aboutButton = new JButton();
        consoleButton = BasicExceptionDialog.getShowButton(JEditorPaneBasedExceptionDialog.this);
        cacheButton = BasicExceptionDialog.getClearCacheButton(JEditorPaneBasedExceptionDialog.this);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        closeButton.setText(Translator.R(InfoItem.SPLASH + "Close"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWindowButtonActionPerformed(evt);
            }
        });

        closeAndCopyButton.setText(Translator.R(InfoItem.SPLASH + "closewAndCopyException"));
        closeAndCopyButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyAndCloseButtonActionPerformed(evt);
            }
        });

        GroupLayout jPanel2Layout = new GroupLayout(topPanel);
        topPanel.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(closeButton)
                        .addContainerGap()
                        .addComponent(aboutButton)
                        .addContainerGap()
                        .addComponent(cacheButton)
                        .addContainerGap()
                        .addComponent(consoleButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 314, Short.MAX_VALUE)
                        .addComponent(closeAndCopyButton)
                        .addContainerGap()));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap(24, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(closeButton)
                        .addComponent(aboutButton)
                        .addComponent(cacheButton)
                        .addComponent(consoleButton)
                        .addComponent(closeAndCopyButton))
                    .addContainerGap()));

        exceptionLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        exceptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        exceptionLabel.setText(Translator.R(InfoItem.SPLASH + "exOccured"));

        bottomPanel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bottomPanel.setLayout(new java.awt.BorderLayout());

        htmlPaneScroller.setViewportView(htmlErrorAndHelpPanel);

        bottomPanel.add(htmlPaneScroller, java.awt.BorderLayout.CENTER);

        homeButton.setText(Translator.R(InfoItem.SPLASH + "Home"));
        homeButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeButtonActionPerformed(evt);
            }
        });


        aboutButton.setText(Translator.R("AboutDialogueTabAbout"));
        aboutButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try{
               AboutDialog.display(true);
            }catch(Exception ex){
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
                JOptionPane.showConfirmDialog(JEditorPaneBasedExceptionDialog.this, ex);
            }
            }
        });
        
        GroupLayout jPanel1Layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(exceptionLabel, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(homeButton, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE).addContainerGap()).addComponent(bottomPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(exceptionLabel, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE).addComponent(homeButton, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(bottomPanel, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)));
                
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(mainPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(topPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(topPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap()));

        pack();
    }

    private void copyAndCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (exception != null) {
            try {
                StringSelection data = new StringSelection(anotherInfo+"\n"+shown.toString()+"\n"+getExceptionStackTraceAsString(exception)+addPlainChain());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
            } catch (Exception ex) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
                JOptionPane.showMessageDialog(this, Translator.R(InfoItem.SPLASH + "cantCopyEx"));
            }
        } else {
            JOptionPane.showMessageDialog(this, Translator.R(InfoItem.SPLASH + "noExRecorded"));
        }
        close();
    }

    private void homeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        htmlErrorAndHelpPanel.setText(message);
        homeButton.setVisible(false);
    }

    private void closeWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {
        close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                Exception ex = new RuntimeException("dsgsfdg");
                JEditorPaneBasedExceptionDialog dialog = new JEditorPaneBasedExceptionDialog(new JFrame(), true, ex, null, "uaaa: aaa\nwqdeweq:sdsds");
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    static String getText(Throwable ex, List<String> l, String anotherInfo,Date shown) {
        StringBuilder s = new StringBuilder("<html><body>");
        String info = "<p>"
                + Translator.R(InfoItem.SPLASH + "mainL1", createLink())
                + " </p> \n" +
                "<p>"
                + Translator.R(InfoItem.SPLASH + "mainL2", createLink())
                + " </p> \n";
        String t = "<p>"
                + Translator.R(InfoItem.SPLASH + "mainL3")
                + "</p> \n"
                + info + formatListInfoList(l) + formatInfo(anotherInfo);
        Object[] options = new String[2];
        options[0] = Translator.R(InfoItem.SPLASH + "Close");
        options[1] = Translator.R(InfoItem.SPLASH + "closeAndCopyShorter");
        if (ex != null) {
            t = "<p>"
                    + Translator.R(InfoItem.SPLASH + "mainL4")
                    + " </p>\n"
                    + info + formatListInfoList(l) + formatInfo(anotherInfo)
                    +"<br/>"+DateFormat.getInstance().format(shown)+"<br/>"
                    + "<p>"
                    + Translator.R(InfoItem.SPLASH + "exWas")
                    + " <br/>\n" + "<pre>" + getExceptionStackTraceAsString(ex) + "</pre>"
                    + addChain();


        } else {
            t += formatListInfoList(l);
        }
        s.append(t);
        s.append("</body></html>");
        return s.toString();
    }

    public static String getExceptionStackTraceAsString(Throwable exception) {
        if (exception == null) {
            return "";
        }
        return OutputController.exceptionToString(exception);
    }

    public static String[] getExceptionStackTraceAsStrings(Throwable exception) {
        if (exception == null) {
            return new String[0];
        }
        return OutputController.exceptionToString(exception).split("\n");
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                htmlErrorAndHelpPanel.setPage(event.getURL());
                homeButton.setVisible(true);

            } catch (Exception ioe) {
                JOptionPane.showMessageDialog(this, Translator.R(InfoItem.SPLASH + "cfl") + " "
                        + event.getURL().toExternalForm() + ": " + ioe);
            }
        }
    }

    private void close() {
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    static String formatListInfoList(List<String> l) {
        if (l == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<p>");
        sb.append("<h3>").
                append(Translator.R(InfoItem.SPLASH + "vendorsInfo")).append(":</h3>");
        sb.append("<pre>");
        for (int i = 0; i < l.size(); i++) {
            String string = l.get(i);
            sb.append(string).append("\n");
        }
        sb.append("</pre>");
        sb.append("</p>");
        return sb.toString();
    }

    static String formatInfo(String l) {
        if (l == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<p>");
        sb.append("<h3>").
                append(Translator.R(InfoItem.SPLASH + "anotherInfo")).append(": </h3>");
        sb.append("<pre>");
        sb.append(l);
        sb.append("</pre>");
        sb.append("</p>");
        return sb.toString();
    }

    Throwable getException() {
        return exception;
    }

    String getMessage() {
        return message;
    }

    private static String createLink() {
        return "<a href=\"" + Translator.R(InfoItem.SPLASH + "url") + "\">" +
                Translator.R(InfoItem.SPLASH + "urlLooks") + "</a>";
    }


    private static String addChain() {
        if (LaunchException.getLaunchExceptionChain().isEmpty()) {
            return "";
        }
        return Translator.R(InfoItem.SPLASH + "chainWas")
                + " <br/>\n" + "<pre>" + getChainAsString(true) + "</pre>";

    }

    private static String addPlainChain() {
        if (LaunchException.getLaunchExceptionChain().isEmpty()) {
            return "";
        }
        return "\n Chain: \n" + getChainAsString(false);

    }

    private static String getChainAsString(boolean formatTime) {
        return getChainAsString(LaunchException.getLaunchExceptionChain(), formatTime);
    }

    private static String getChainAsString(List<LaunchException.LaunchExceptionWithStamp> launchExceptionChain, boolean formatTime) {
        String s = "";
        if (launchExceptionChain != null) {
            for (int i = 0; i < launchExceptionChain.size(); i++) {
                LaunchException.LaunchExceptionWithStamp launchException = launchExceptionChain.get(i);
                s = s + (i+1) + ") at " + formatTime(launchException.getStamp(), formatTime) + "\n" + getExceptionStackTraceAsString(launchException.getEx());
            }
        }
        return s;
    }

    private static String formatTime(Date dateTime, boolean formatTime) {
        if (dateTime == null) {
            return "unknown time";
        }
        if (formatTime) {
            return DateFormat.getInstance().format(dateTime);
        } else {
            return dateTime.toString();
        }
    }
}
