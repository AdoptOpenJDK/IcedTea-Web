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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * this class intentionally NOT cache any content, but always load data. Its
 * original use case was to to implement http proxy logging. And there reloads
 * really matters.
 *
 */
public class HtmlBrowserPanel extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(HtmlBrowserPanel.class);

    private void fireDocumentChanged(String current) {
        for (DocumentChangedListener documentChangedListener : documentChangedListeners) {
            documentChangedListener.documentChanged(current);
        }
    }

    private void fireAddressChanged(String url) {
        for (DocumentChangedListener documentChangedListener : documentChangedListeners) {
            documentChangedListener.addressChanged(url);
        }
    }

    private void fireAddressChanged(URL url) {
        for (DocumentChangedListener documentChangedListener : documentChangedListeners) {
            documentChangedListener.addressChanged(url);
        }
    }

    public void addDocumentChangedListener(DocumentChangedListener i) {
        documentChangedListeners.add(i);
    }
    List<DocumentChangedListener> documentChangedListeners = new ArrayList<>();

    public URL getUrl() {
        if (current == null) {
            return null;
        }
        return current.url;

    }

    private static class State {

        private static String removeMeta(String string) {
            return string.replaceAll("(?i)<\\s*meta.*?>", ""); //any meta chars confuse 3.2 html jeditorpane heavily
        }

        final URL url;
        final String source;
        final String all;

        private State(URL url, String all, String html) {
            this.url = url;
            this.all = all;
            this.source = html;
        }

        private String getSource() {
            if (all == null || all.trim().isEmpty()) {
                return source;
            }
            return all;
        }

        private String getHtml() {
            if (source == null || source.trim().isEmpty()) {
                return removeMeta(all);
            } else {
                return removeMeta(source);
            }
        }
    }

    private class History {

        private final List<URL> visited = new ArrayList();
        private final List<URL> backed = new ArrayList();

        private URL back() {
            if (visited.isEmpty()) {
                return null;
            }
            URL q = visited.get(0);
            visited.remove(0);
            put(current, backed);
            URL u = loadCatched(q); //override current
            createTooltips();
            return q;
        }

        private URL fwd() {
            if (backed.isEmpty()) {
                return null;
            }
            URL q = backed.get(0);
            backed.remove(0);
            put(current, visited);
            URL u = loadCatched(q); //override current
            createTooltips();
            return q;
        }

        private void put(URL url, List<URL> where) {
            if (url != null) {
                if (where.isEmpty()) {
                    where.add(0, url);
                } else if (!where.get(0).equals(url)) {
                    where.add(0, url);
                }
            }

        }

        private void put(State current, List<URL> where) {
            if (current != null) {
                put(current.url, where);
            }
        }

        private void visit(String text) {
            put(current, visited);
            URL u = loadCatched(text); //override current
            createTooltips();
        }

        private void visit(URL url) {
            put(current, visited);
            URL u = loadCatched(url); //override current
            createTooltips();
        }

        private void createTooltips() {
            createTooltip(visited, backButton, Translator.R("BUTback"));
            createTooltip(backed, fwdButton, Translator.R("BUTforward"));
        }

        private void createTooltip(List<URL> visited, JButton b, String title) {
            StringBuilder sb = new StringBuilder("<html><h3>" + title + "</h3><ol>");
            JPopupMenu p = new JPopupMenu();
            for (final URL url : visited) {
                sb.append("<li>").append(url.toExternalForm()).append("</li>");
                JMenuItem jim = new JMenuItem(url.toExternalForm());
                jim.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        visit(url);
                    }
                });
                p.add(jim);
            }
            b.removeAll();;
            b.setComponentPopupMenu(p);
            sb.append("</ol></html>");
            b.setToolTipText(sb.toString());

        }

    }

    private final JPanel customUrl = new JPanel(new BorderLayout());
    private final JPanel mainButtons = new JPanel(new GridLayout(1, 0));
    private final JPanel tools = new JPanel(new BorderLayout());
    private final JTextField goTo = new JTextField();
    private final JButton gotoButton = new JButton(Translator.R("BrowserGoTo"));
    private final JButton backButton = new JButton("<<<");
    private final JButton reloadButton = new JButton(Translator.R("BUTreload"));
    private final JButton fwdButton = new JButton(">>>");
    private final JToggleButton viewSourceButton = new JToggleButton(Translator.R("BrowserSource"));
    private final JCheckBox socketCheckbox = new JCheckBox(Translator.R("BrowserSocket"));
    private final JComboBox<Charset> encodingBox = new JComboBox<>(new Charset[]{
        null,
        US_ASCII,
        UTF_8,
        ISO_8859_1,
        UTF_16,
        UTF_16BE,
        UTF_16LE

});

    private static final String TEXTPLAIN = "text/plain";
    private static final String TEXTHTML = "text/html";

    //because of various reloadings, those are always recreated
    private JEditorPane currentHtml;
    private JScrollPane currentScrollHtml;

    private State current;
    private boolean source = false;
    private boolean useSocket = false;
    private final History history = new History();

    public HtmlBrowserPanel(final URL url) {
        this(url, false);
    }

    public HtmlBrowserPanel(final String url) {
        this(url, false);
    }

    public HtmlBrowserPanel(final URL url, boolean socket) {
        super(new BorderLayout());
        setUseSocket(socket);
        crateGui();
        URL u = loadCatched(url);
    }

    public HtmlBrowserPanel(final String url, boolean socket) {
        super(new BorderLayout());
        setUseSocket(socket);
        crateGui();
        URL u = loadCatched(url);
    }

    private URL loadCatched(String url) {
        try {
            URL u = new URL(url);
            load(u);
            return u;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            if (!JNLPRuntime.isHeadless()) {
                JOptionPane.showMessageDialog(null, ex);
            }
            return null;
        }
    }

    private URL loadCatched(URL url) {
        try {
            load(url);
            return url;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            if (!JNLPRuntime.isHeadless()) {
                JOptionPane.showMessageDialog(null, ex);
            }
            return null;
        }
    }

    private void load(URL url) throws IOException {
        goTo.setText(url.toExternalForm());
        fireAddressChanged(url);
        fireAddressChanged(url.toExternalForm());
        //url connection is checking response code.It can be used as 511 is unimplemented
        String[] result;
        if (isUseSocket()) {
            LOG.debug("Using socket connection");
            Charset ch = (Charset)(encodingBox.getSelectedItem());
            if (ch == null) {
                result = UrlUtils.loadUrlWithInvalidHeader(url);
            } else {
                result = UrlUtils.loadUrlWithInvalidHeader(url, ch);
            }
        } else {
            LOG.debug("Using URLconnection");
            String s;
            Charset ch = (Charset)(encodingBox.getSelectedItem());
            if (ch == null) {
                s = UrlUtils.loadUrl(url);
            } else {
                s = UrlUtils.loadUrl(url, ch);
            }
            result = new String[]{s, s, s};
        }
        LOG.debug(result[0]);
        if (result[2].trim().isEmpty()) {
            result[2] = result[1];
        }
        current = new State(url, result[0], result[2]);
        if (source) {
            currentHtml = new JEditorPane(TEXTPLAIN, current.getSource());
            if (encodingBox.getSelectedItem()!=null){
                currentHtml.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            }
        } else {
            currentHtml = new JEditorPane(TEXTHTML, current.getHtml());
            if (encodingBox.getSelectedItem()!=null){
                currentHtml.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            }
            ((HTMLDocument) currentHtml.getDocument()).setBase(current.url);
        }
        fireDocumentChanged(getCurrentSource());
        currentHtml.setEditable(false);//otherwise hyperlinks don't work
        currentHtml.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL u = e.getURL();
                    history.visit(u);
                }
            }
        });
        if (currentScrollHtml != null) {
            this.remove(currentScrollHtml);
        }
        currentScrollHtml = new JScrollPane(currentHtml);
        this.add(currentScrollHtml);

        this.validate();
    }

    private void crateGui() {

        mainButtons.add(backButton);
        backButton.setToolTipText(Translator.R("BUTback"));
        mainButtons.add(reloadButton);
        mainButtons.add(fwdButton);
        fwdButton.setToolTipText(Translator.R("BUTforward"));
        mainButtons.add(viewSourceButton);
        mainButtons.add(socketCheckbox);
        socketCheckbox.setSelected(isUseSocket());
        socketCheckbox.setToolTipText(Translator.R("BrowserSocketHelp"));
        customUrl.add(gotoButton, BorderLayout.WEST);
        customUrl.add(goTo);
        customUrl.add(encodingBox, BorderLayout.EAST);
        tools.add(customUrl, BorderLayout.SOUTH);
        tools.add(mainButtons, BorderLayout.NORTH);
        gotoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoUrl(goTo.getText());
            }
        });
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URL u = loadCatched(goTo.getText());
            }
        });
        goTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoUrl(goTo.getText());
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URL u = history.back();

            }
        });
        fwdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URL u = history.fwd();
            }
        });
        socketCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUseSocket(socketCheckbox.isSelected());
            }
        });
        viewSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                source = !source;
                if (current == null || currentHtml == null) {
                    return;
                }
                showBySource();
            }

        });

        this.add(tools, BorderLayout.NORTH);
    }

    private void showBySource() {
        if (source) {
            currentHtml.setContentType(TEXTPLAIN);
            currentHtml.setText(current.getSource());
        } else {
            currentHtml.setContentType(TEXTHTML);
            currentHtml.setText(current.getHtml());
            ((HTMLDocument) currentHtml.getDocument()).setBase(current.url);
        }
    }

    private void setUseSocket(boolean b) {
        useSocket = b;
        if (socketCheckbox != null) {
            socketCheckbox.setSelected(b);
        }
    }

    public boolean isUseSocket() {
        return useSocket;
    }

    public static void warn() {
        LOG.info("WARNING this is html 3.2 comatible browser, not intended for casual web browsing!");
        LOG.info("Provided without any warranty!");
    }

    public static void showStandAloneWindow(String url, boolean socket) {
        warn();
        JFrame f = new JFrame();
        f.add(new HtmlBrowserPanel(url, socket));
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    public String getCurrentSource() {
        if (current == null) {
            return "";
        }
        return current.getSource();
    }

    public void gotoUrl(String text) {
        history.visit(text);
    }

}
