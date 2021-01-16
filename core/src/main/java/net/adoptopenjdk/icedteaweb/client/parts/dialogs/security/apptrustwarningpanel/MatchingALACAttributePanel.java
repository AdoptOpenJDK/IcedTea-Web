/* 
 Copyright (C) 2008 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.ImageIcon;
import java.awt.Dimension;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

/**
 * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#app_library
 *
 * @deprecated will be replaced by new security dialogs
 */
@Deprecated
public class MatchingALACAttributePanel extends AppTrustWarningPanel {

    private final String title;
    private final String codebase;
    private final String remoteUrls;

    public MatchingALACAttributePanel(SecurityDialog securityDialog, JNLPFile file, String codebase, String remoteUrls) {
        super(file, securityDialog);
        this.title = super.getAppletTitle();
        this.codebase = codebase;
        this.remoteUrls = remoteUrls;
        TOP_PANEL_HEIGHT = 250;
        addComponents();
        if (securityDialog != null) {
            securityDialog.getViewableDialog().setMinimumSize(new Dimension(600, 400));
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
        UnsignedAppletActionEntry rememberedEntry = UnsignedAppletTrustConfirmation.getStoredEntry(file, this.getClass());
        if (rememberedEntry != null) {
            ExecuteAppletAction rememberedAction = rememberedEntry.getAppletSecurityActions().getAction(this.getClass());
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
}
