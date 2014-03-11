/* AppletWarningPane.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.util.logging.OutputController;

public class MissingPermissionsAttributePanel extends SecurityDialogPanel {

    public MissingPermissionsAttributePanel(SecurityDialog x, String title, String codebase) {
        super(x);
        try {
            addComponents(title, codebase);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (x != null) {
            x.setMinimumSize(new Dimension(400, 300));
        }
    }

    protected final void addComponents(String title, String codebase) throws IOException {

        URL imgUrl = this.getClass().getResource("/net/sourceforge/jnlp/resources/warning.png");
        ImageIcon icon = null;
        Image img = ImageIO.read(imgUrl);
        icon = new ImageIcon(img);
        String topLabelText = Translator.R("MissingPermissionsMainTitle", title, codebase);
        String bottomLabelText = Translator.R("MissingPermissionsInfo");

        JLabel topLabel = new JLabel(htmlWrap(topLabelText), icon, SwingConstants.CENTER);
        topLabel.setFont(new Font(topLabel.getFont().toString(),
                Font.BOLD, 12));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(400, 80));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JEditorPane bottomLabel = new JEditorPane("text/html", htmlWrap(bottomLabelText));
        bottomLabel.setEditable(false);
        bottomLabel.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                try {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                } catch (IOException ex) {
                    OutputController.getLogger().log(ex);
                } catch (URISyntaxException ex) {
                    OutputController.getLogger().log(ex);
                }
            }
        });
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(bottomLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(400, 80));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomLabel.setBackground(infoPanel.getBackground());

        //run and cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton yes = new JButton(Translator.R("ButYes"));
        JButton no = new JButton(Translator.R("ButNo"));
        int buttonWidth = yes.getMinimumSize().width;
        int buttonHeight = yes.getMinimumSize().height;
        Dimension d = new Dimension(buttonWidth, buttonHeight);
        yes.setPreferredSize(d);
        no.setPreferredSize(d);
        yes.addActionListener(createSetValueListener(parent, 0));
        no.addActionListener(createSetValueListener(parent, 1));
        initialFocusComponent = no;
        buttonPanel.add(yes);
        buttonPanel.add(no);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //all of the above
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(topPanel);
        add(infoPanel);
        add(buttonPanel);

    }

    public static void main(String[] args) {
        MissingPermissionsAttributePanel w = new MissingPermissionsAttributePanel(null, "HelloWorld", "http://nbblah.url");
        JFrame f = new JFrame();
        f.setSize(400, 300);
        f.add(w, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
