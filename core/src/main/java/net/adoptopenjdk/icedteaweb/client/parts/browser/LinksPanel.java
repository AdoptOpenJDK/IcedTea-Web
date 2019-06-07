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
package net.adoptopenjdk.icedteaweb.client.parts.browser;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksPanel extends JPanel implements DocumentChangedListener {

    private final static Logger LOG = LoggerFactory.getLogger(LinksPanel.class);

    private URL baseUrl;

    private final Set<String> added = new HashSet<>();
    private HtmlBrowserPanel browser;

    public void setInternalBrowser(HtmlBrowserPanel browser) {
        this.browser = browser;
    }

    private static int counter = 0;

    private class RowHolder extends JPanel {

        //should go from text state
        private final String url;

        public RowHolder(final String urll) {
            super(new BorderLayout());
            counter++;
            Color bg = this.getBackground();
            if (counter % 2 == 1) {
                bg = new Color(Math.max(0, bg.getRed() - 20), Math.min(255, bg.getGreen() + 20), Math.max(0, bg.getBlue() - 20));
            }
            this.url = urll;
            JLabel l = new JLabel(url);
            l.setBackground(bg);
            this.add(l);
            JPanel buttonsPanel = new JPanel(new GridLayout(1, 0));
            buttonsPanel.setBackground(bg);
            JButton b1 = new JButton(Translator.R("BrowserOpenExternal"));
            b1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                        if (!JNLPRuntime.isHeadless()) {
                            JOptionPane.showMessageDialog(null, ex);
                        }
                    }
                }
            });
            buttonsPanel.add(b1);
            //if (browser != null) {
            JButton b2 = new JButton(Translator.R("BrowserOpeninternal"));
            b2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    browser.gotoUrl(url);
                }
            });
            buttonsPanel.add(b2);
            //}
            JButton b3 = new JButton(Translator.R("BrowserCopyUrlToClip"));
            b3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringSelection selection = new StringSelection(url);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }
            });
            buttonsPanel.add(b3);
            this.setBackground(bg);
            this.add(buttonsPanel, BorderLayout.EAST);

        }

    }

    public LinksPanel() {
        super(new GridLayout(0, 1));
        addClearButton();
    }

    private void addClearButton() {

        JButton clear = new JButton(Translator.R("BrowserClearAll"));
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinksPanel.this.removeAll();
                added.clear();
                addClearButton();
            }
        });
        this.add(clear);
    }

    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static final String URL_REGEX = "\\(?\\b(https://|http://|www[.])[-A-Za-z0-9+&;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    public static final Pattern URL_REGEX_PATTERN = Pattern.compile(URL_REGEX);

    public List<String> pullAbsoluteLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = URL_REGEX_PATTERN.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links.add(urlStr);
        }
        return links;
    }

    private static final String HTML_QUOTING_REGEX = "(\"|')";
    public static final String HREF_REGEX_START = "(?i)href\\s*=\\s*" + HTML_QUOTING_REGEX;
    private static final String HREF_REGEX_END = HTML_QUOTING_REGEX;
    public static final String HREF_REGEX = HREF_REGEX_START + ".*?" + HREF_REGEX_END;
    public static final Pattern HREF_REGEX_PATTERN = Pattern.compile(HREF_REGEX);

    public List<String> pullHrefs(String text) {
        List<String> links = new ArrayList<>();

        Matcher m = HREF_REGEX_PATTERN.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            urlStr = urlStr.replaceFirst(HREF_REGEX_START, "");
            urlStr = urlStr.substring(0, urlStr.length() - 1);
            links.add(urlStr);
        }
        return links;
    }

    public void parseAndAdd(String currentSource) {
        List<String> l = pullAbsoluteLinks(currentSource);
        for (String string : l) {
            addLink(string);
        }
        l = pullHrefs(currentSource);
        for (String string : l) {
            if (added.add(string)) {
                //not added =>  relative url
                addLink(absolutize(string));
            }
        }
    }

    private String absolutize(String string) {
        if (baseUrl == null) {
            return string;
        }
        return UrlUtils.ensureSlashTail(UrlUtils.removeFileName(baseUrl)).toExternalForm() + string;
    }

    @Override
    public void documentChanged(String current) {
        parseAndAdd(current);
    }

    private void addLink(String string) {
        LOG.info(string);
        if (added.add(string)) {
            this.add(new RowHolder(string));
        }
    }

    @Override
    public void addressChanged(String url) {
        addLink(url);
    }

    @Override
    public void addressChanged(URL url) {
        if (url != null) {
            baseUrl = url;
            addLink(url.toExternalForm());
        }
    }

    public List<String> getAllUrls() {
        return new ArrayList<>(added);
    }

}
