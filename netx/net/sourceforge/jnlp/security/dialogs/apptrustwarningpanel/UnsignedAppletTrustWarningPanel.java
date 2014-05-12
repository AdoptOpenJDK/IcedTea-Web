/* Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import static net.sourceforge.jnlp.runtime.Translator.R;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityActions;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;


public class UnsignedAppletTrustWarningPanel extends AppTrustWarningPanel {

    public UnsignedAppletTrustWarningPanel(SecurityDialog securityDialog, final JNLPFile file, final ActionChoiceListener listener) {
        super(file, listener);
        this.INFO_PANEL_HEIGHT = 250;
        addComponents();
        if (securityDialog != null) {
            securityDialog.setMinimumSize(new Dimension(600, 400));
        }
    }

    @Override
    protected ImageIcon getInfoImage() {
        final String location = "net/sourceforge/jnlp/resources/info-small.png";
        return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(location));
    }

    protected static String getTopPanelTextKey() {
        return "SUnsignedSummary";
    }

    protected static String getInfoPanelTextKey() {
        return "SUnsignedDetail";
    }

    protected static String getQuestionPanelTextKey() {
        return "SUnsignedQuestion";
    }

    @Override
    protected String getTopPanelText() {
        return htmlWrap(R(getTopPanelTextKey()));
    }

    @Override
    protected String getInfoPanelText() {
        String text = R(getInfoPanelTextKey(), file.getCodeBase(), file.getSourceLocation());
        UnsignedAppletActionEntry rememberedEntry = UnsignedAppletTrustConfirmation.getStoredEntry(file, AppletSecurityActions.UNSIGNED_APPLET_ACTION);
        if (rememberedEntry != null) {
            ExecuteAppletAction rememberedAction = rememberedEntry.getAppletSecurityActions().getUnsignedAppletAction();
            if (rememberedAction == ExecuteAppletAction.YES) {
                text += "<br>" + R("SUnsignedAllowedBefore", rememberedEntry.getLocalisedTimeStamp());
            } else if (rememberedAction == ExecuteAppletAction.NO) {
                text += "<br>" + R("SUnsignedRejectedBefore", rememberedEntry.getLocalisedTimeStamp());
            }
        }
        return htmlWrap(text);
    }

    @Override
    protected String getQuestionPanelText() {
        return htmlWrap(R(getQuestionPanelTextKey()));
    }
    
    public static void main(String[] args) throws Exception {
        UnsignedAppletTrustWarningPanel w = new UnsignedAppletTrustWarningPanel(null, new JNLPFile(new URL("http://www.geogebra.org/webstart/geogebra.jnlp")), null);
        JFrame f = new JFrame();
        f.setSize(600, 400);
        f.add(w, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

}
