/* HTMLPanel.java
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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.sourceforge.jnlp.util.logging.OutputController;

public class HTMLPanel extends JPanel {

    private String id;

    public HTMLPanel(URL url, String identifier) {
        super(new BorderLayout());
        id = identifier;
        JEditorPane pane = new JEditorPane();
        try {
             pane = new JEditorPane(url);
        } catch (IOException ex) {
            //no need to have invalid url fatal
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, ex);
        }
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.addHyperlinkListener(new UrlHyperlinkListener());
        JScrollPane scroller = new JScrollPane(pane);
        this.add(scroller, BorderLayout.CENTER);
    }

    public String getIdentifier() {
        return id;
    }

    private class UrlHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (Desktop.isDesktopSupported() && event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (URISyntaxException ex) {
                } catch (IOException ex) {
                }
            }
        }
    }

}
