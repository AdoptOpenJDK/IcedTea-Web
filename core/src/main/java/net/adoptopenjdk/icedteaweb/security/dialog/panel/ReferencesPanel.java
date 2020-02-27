package net.adoptopenjdk.icedteaweb.security.dialog.panel;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.util.html.HtmlUtil;
import net.adoptopenjdk.icedteaweb.client.util.html.NamedUrl;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class ReferencesPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(ReferencesPanel.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    public ReferencesPanel(final String htmlList) {
        super(new BorderLayout());
        createContent(null, htmlList);
    }

    public ReferencesPanel(String title, Set<URL> urls) {
        super(new BorderLayout());
        createContent(title, HtmlUtil.unorderedListOf(urls, 4));
    }

    public ReferencesPanel(String title, List<NamedUrl> namedUrls) {
        super(new BorderLayout());
        createContent(title, HtmlUtil.unorderedListOf(namedUrls, 4));
    }

    private void createContent(final String listTitle, final String htmlList) {
        final String html = StringUtils.isBlank(listTitle) ? htmlList : listTitle + "<br/>" + htmlList;

        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setEditable(false);
        editorPane.setFont(new Font(Font.SANS_SERIF, getFont().getStyle(), getFont().getSize()));
        editorPane.setBackground(null);
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                try {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                } catch (IOException | URISyntaxException ex) {
                    LOG.error("Error while trying to open hyperlink from dialog.", e);
                }
            }
        });
        add(editorPane, BorderLayout.CENTER);
    }
}
