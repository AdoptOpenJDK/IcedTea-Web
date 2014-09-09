/* Main.java
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

package net.sourceforge.jnlp.about;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.sourceforge.jnlp.util.ScreenFinder;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.HtmlFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;

public final class AboutDialog extends JPanel implements Runnable, ActionListener {

    private static final String about_url_stub = "/net/sourceforge/jnlp/resources/about";
    private static final String authors_url = "/net/sourceforge/jnlp/resources/AUTHORS.html";
    private static final String changelog_url = "/net/sourceforge/jnlp/resources/ChangeLog.html";
    private static final String copying_url = "/net/sourceforge/jnlp/resources/COPYING.html";
    private static final String news_url = "/net/sourceforge/jnlp/resources/NEWS.html";

    private final  String app;

    private final JDialog frame;
    private JPanel contentPane;
    //singletons to not laod/generate them all around
    private static HTMLPanel aboutPanel, authorsPanel, newsPanel, changelogPanel, copyingPanel, helpPanel;
    private final JButton aboutButton, authorsButton, newsButton, changelogButton, copyingButton, helpButton;

    private final URL res_authors = getClass().getResource(authors_url);
    private final URL res_news = getClass().getResource(news_url);
    private final URL res_changelog = getClass().getResource(changelog_url);
    private final URL res_copying = getClass().getResource(copying_url);

    public static enum ShowPage{
        ABOUT /*default*/,
        AUTHORS,
        NEWS,
        CHANGELOG,
        LICENSE,
        HELP
        
    }
    private  AboutDialog(boolean modal, String app, ShowPage showPage) {
        super(new GridBagLayout());
        this.app = app;
        frame = new JDialog((Frame) null, R("AboutDialogueTabAbout") + " IcedTea-Web", modal);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


        aboutButton = new JButton( R("AboutDialogueTabAbout"));
        aboutButton.addActionListener(this);

        authorsButton = new JButton(R("AboutDialogueTabAuthors"));
        authorsButton.addActionListener(this);

        newsButton = new JButton( R("AboutDialogueTabNews"));
        newsButton.addActionListener(this);

        changelogButton = new JButton(R("AboutDialogueTabChangelog"));
        changelogButton.addActionListener(this);

        copyingButton = new JButton(R("AboutDialogueTabGPLv2"));
        copyingButton.addActionListener(this);
        
        helpButton = new JButton(R("APPEXTSECguiPanelHelpButton"));
        helpButton.addActionListener(this);

        
        switch (showPage) {
            case ABOUT:
                actionPerformed(new ActionEvent(aboutButton, 0, ""));
                break;
            case AUTHORS:
                actionPerformed(new ActionEvent(authorsButton, 0, ""));
                break;
            case CHANGELOG:
                actionPerformed(new ActionEvent(changelogButton, 0, ""));
                break;
            case HELP:
                actionPerformed(new ActionEvent(helpButton, 0, ""));
                break;
            case LICENSE:
                actionPerformed(new ActionEvent(copyingButton, 0, ""));
                break;
            case NEWS:
                actionPerformed(new ActionEvent(newsButton, 0, ""));
                break;

            default:
                actionPerformed(new ActionEvent(aboutButton, 0, ""));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object action = e.getSource();

        if (action.equals(aboutButton)) {
            if (aboutPanel == null) {
                String lang = Locale.getDefault().getLanguage();
                URL about_lang;
                try {
                    about_lang = getClass().getResource(about_url_stub + "_" + lang + ".html");
                    about_lang.openStream().close();
                } catch (Exception ex) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, ex);
                    //probably unknown locale, switch to english
                    about_lang = getClass().getResource(about_url_stub + "_en.html");
                }
                aboutPanel = new HTMLPanel(about_lang);
            }
            contentPane = aboutPanel;
        } else if (action.equals(authorsButton)) {
            if (authorsPanel == null) {
                authorsPanel = new HTMLPanel(res_authors);
            }
            contentPane = authorsPanel;
        } else if (action.equals(newsButton)) {
            if (newsPanel == null) {
                newsPanel = new HTMLPanel(res_news);
            }
            contentPane = newsPanel;
        } else if (action.equals(changelogButton)) {
            if (changelogPanel == null) {
                changelogPanel = new HTMLPanel(res_changelog);
            }
            contentPane = changelogPanel;
        } else if (action.equals(copyingButton)) {
            if (copyingPanel == null) {
                copyingPanel = new HTMLPanel(res_copying);
            }
            contentPane = copyingPanel;
        } else if (action.equals(helpButton)) {
            if (helpPanel == null) {
                //copy logo and generate resources to tmp dir
                try {
                    File f = File.createTempFile("icedtea-web", "help");
                    f.delete();
                    f.mkdir();
                    f.deleteOnExit();
                    TextsProvider.generateRuntimeHtmlTexts(f);
                    //detect running application
                    File target = new File(f, TextsProvider.ITW + "." + HtmlFormatter.SUFFIX);
                    if (app != null) {
                        target = new File(f, app + "." + HtmlFormatter.SUFFIX);
                    }
                    helpPanel = new InternalHTMLPanel(target.toURI().toURL());
                } catch (IOException ex) {
                    OutputController.getLogger().log(ex);
                }
            }
            contentPane = helpPanel;
        }

        layoutWindow();
    }

    private void layoutWindow() {
        this.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        this.add(contentPane, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.ipady = 16;
        this.add(aboutButton, gbc);

        gbc.gridx = 1;
        this.add(authorsButton, gbc);

        gbc.gridx = 2;
        this.add(newsButton, gbc);

        gbc.gridx = 3;
        this.add(changelogButton, gbc);

        gbc.gridx = 4;
        this.add(copyingButton, gbc);
        
        gbc.gridx = 5;
        this.add(helpButton, gbc);

        Dimension contentSize = new Dimension(640, 480);
        contentPane.setMinimumSize(contentSize);
        contentPane.setPreferredSize(contentSize);
        contentPane.setBorder(new EmptyBorder(0, 0, 8, 0));

        this.setBorder(new EmptyBorder(8, 8, 8, 8));

        frame.pack();
    }

    @Override
    public void run() {
        layoutWindow();
        ScreenFinder.centerWindowsToCurrentScreen(frame);
        frame.setVisible(true);
    }

    public static void display(String app) {
        display(false, app);
    }

    public static void display(boolean modal, String app) {
        display(modal, app, ShowPage.ABOUT);
    }
    
    public static void display(boolean modal, String app, ShowPage showPage) {
        SwingUtilities.invokeLater(new AboutDialog(modal, app, showPage));
    }
}

 