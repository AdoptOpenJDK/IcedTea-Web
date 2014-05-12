/* 
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
package net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.runtime.Translator;
import static net.sourceforge.jnlp.runtime.Translator.R;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityActions;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.sourceforge.jnlp.util.UrlUtils;

/**
 * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#app_library
 */
public class MatchingALACAttributePanel extends AppTrustWarningPanel {

    private final String title;
    private final String codebase;
    private final String remoteUrls;

    public MatchingALACAttributePanel(SecurityDialog x, JNLPFile file, String codebase, String remoteUrls, ActionChoiceListener actionChoiceListener) {
        super(file, actionChoiceListener);
        this.title = super.getAppletTitle();
        this.codebase = codebase;
        this.remoteUrls = remoteUrls;
        TOP_PANEL_HEIGHT = 250;
        addComponents();
        if (x != null) {
            x.setMinimumSize(new Dimension(600, 400));
        }
    }

    @Override
    protected ImageIcon getInfoImage() {
        final String location = "net/sourceforge/jnlp/resources/question.png";
        return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(location));
    }

    @Override
    protected String getTopPanelText() {
        return htmlWrap(Translator.R("ALACAMatchingMainTitle", title, codebase, remoteUrls));
    }

    @Override
    protected String getInfoPanelText() {
        String r = Translator.R("ALACAMatchingInfo");
        UnsignedAppletActionEntry rememberedEntry = UnsignedAppletTrustConfirmation.getStoredEntry(file, AppletSecurityActions.MATCHING_ALACA_ACTION);
        if (rememberedEntry != null) {
            ExecuteAppletAction rememberedAction = rememberedEntry.getAppletSecurityActions().getMatchingAlacaAction();
            if (rememberedAction == ExecuteAppletAction.YES) {
                r += "<br>" + R("SUnsignedAllowedBefore", rememberedEntry.getLocalisedTimeStamp());
            } else if (rememberedAction == ExecuteAppletAction.NO) {
                r += "<br>" + R("SUnsignedRejectedBefore", rememberedEntry.getLocalisedTimeStamp());
            }
        }
        return r;
    }

    @Override
    protected String getQuestionPanelText() {
        return "";//htmlWrap(Translator.R("SRememberOption"));
    }

    @Override
    public String getAppletTitle() {
        return "";
    }

    public static void main(String[] args) throws MalformedURLException, IOException, ParseException {
        Set<URL> s = new HashSet<URL>();
        s.add(new URL("http:/blah.com/blah"));
        s.add(new URL("http:/blah.com/blah/blah"));
        MatchingALACAttributePanel w = new MatchingALACAttributePanel(null, new JNLPFile(new URL("http://www.geogebra.org/webstart/geogebra.jnlp")), "http://nbblah.url", UrlUtils.setOfUrlsToHtmlList(s), new ActionChoiceListener() {

            @Override
            public void actionChosen(AppSigningWarningAction action) {

            }
        });
        JFrame f = new JFrame();
        f.setSize(600, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(w, BorderLayout.CENTER);
        f.setVisible(true);
    }
}
