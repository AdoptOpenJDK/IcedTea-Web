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
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.sourceforge.jnlp.util.ScreenFinder;

public class AboutDialog extends JPanel implements Runnable, ActionListener {

    private static final String about_url = "/net/sourceforge/jnlp/resources/about.html";
    private static final String authors_url = "/net/sourceforge/jnlp/resources/AUTHORS.html";
    private static final String changelog_url = "/net/sourceforge/jnlp/resources/ChangeLog.html";
    private static final String copying_url = "/net/sourceforge/jnlp/resources/COPYING.html";
    private static final String news_url = "/net/sourceforge/jnlp/resources/NEWS.html";

    private JDialog frame;
    private JPanel contentPane;
    private HTMLPanel aboutPanel, authorsPanel, newsPanel, changelogPanel, copyingPanel;
    private JButton aboutButton, authorsButton, newsButton, changelogButton, copyingButton;

    public AboutDialog(boolean modal) {
        super(new GridBagLayout());

        frame = new JDialog((Frame)null, R("AboutDialogueTabAbout") + " IcedTea-Web", modal);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        URL res_about = getClass().getResource(about_url);
        URL res_authors = getClass().getResource(authors_url);
        URL res_news = getClass().getResource(news_url);
        URL res_changelog = getClass().getResource(changelog_url);
        URL res_copying = getClass().getResource(copying_url);

        aboutPanel = new HTMLPanel(res_about, R("AboutDialogueTabAbout"));
        authorsPanel = new HTMLPanel(res_authors, R("AboutDialogueTabAuthors"));
        newsPanel = new HTMLPanel(res_news, R("AboutDialogueTabNews"));
        changelogPanel = new HTMLPanel(res_changelog, R("AboutDialogueTabChangelog"));
        copyingPanel = new HTMLPanel(res_copying, R("AboutDialogueTabGPLv2"));

        aboutButton = new JButton(aboutPanel.getIdentifier());
        aboutButton.setActionCommand(aboutPanel.getIdentifier());
        aboutButton.addActionListener(this);

        authorsButton = new JButton(authorsPanel.getIdentifier());
        authorsButton.setActionCommand(authorsPanel.getIdentifier());
        authorsButton.addActionListener(this);

        newsButton = new JButton(newsPanel.getIdentifier());
        newsButton.setActionCommand(newsPanel.getIdentifier());
        newsButton.addActionListener(this);

        changelogButton = new JButton(changelogPanel.getIdentifier());
        changelogButton.setActionCommand(changelogPanel.getIdentifier());
        changelogButton.addActionListener(this);

        copyingButton = new JButton(copyingPanel.getIdentifier());
        copyingButton.setActionCommand(copyingPanel.getIdentifier());
        copyingButton.addActionListener(this);

        contentPane = aboutPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(((HTMLPanel) contentPane).getIdentifier()))
            return;

        if (action.equals(aboutPanel.getIdentifier())) {
            contentPane = aboutPanel;
        } else if (action.equals(authorsPanel.getIdentifier())) {
            contentPane = authorsPanel;
        } else if (action.equals(newsPanel.getIdentifier())) {
            contentPane = newsPanel;
        } else if (action.equals(changelogPanel.getIdentifier())) {
            contentPane = changelogPanel;
        } else if (action.equals(copyingPanel.getIdentifier())) {
            contentPane = copyingPanel;
        }

        layoutWindow();
    }

    private void layoutWindow() {
        this.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
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

    public static void display() {
        display(false);
    }

    public static void display(boolean modal) {
        SwingUtilities.invokeLater(new AboutDialog(modal));
    }

}
