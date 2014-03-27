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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.sourceforge.jnlp.security.dialogs.TemporaryPermissionsButton;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public class PartiallySignedAppTrustWarningPanel extends AppTrustWarningPanel {

    private final JarCertVerifier jcv;
    private final JButton sandboxButton;
    private final JButton advancedOptionsButton;

    public PartiallySignedAppTrustWarningPanel(JNLPFile file, ActionChoiceListener actionChoiceListener,
            SecurityDialog securityDialog, SecurityDelegate securityDelegate) {
        super(file, actionChoiceListener);
        this.jcv = (JarCertVerifier) securityDialog.getCertVerifier();
        this.INFO_PANEL_HEIGHT = 200;

        sandboxButton = new JButton();
        sandboxButton.setText(R("ButSandbox"));
        sandboxButton.addActionListener(chosenActionSetter(ExecuteAppletAction.SANDBOX));

        advancedOptionsButton = new TemporaryPermissionsButton(file, securityDelegate, sandboxButton);

        buttons.add(1, sandboxButton);
        buttons.add(2, advancedOptionsButton);

        addComponents();
    }

    @Override
    protected String getAppletTitle() {
        String title;
        try {
            if (file instanceof PluginBridge) {
                title = file.getTitle();
            } else {
                title = file.getInformation().getTitle();
            }
        } catch (Exception e) {
            title = "";
        }
        return R("SAppletTitle", title);
    }

    private String getAppletInfo() {
        Certificate c = jcv.getPublisher(null);

        String publisher = "";
        String from = "";

        try {
            if (c instanceof X509Certificate) {
                publisher = SecurityUtil.getCN(((X509Certificate) c).getSubjectX500Principal().getName());
            }
        } catch (Exception e) {
        }

        try {
            if (file instanceof PluginBridge) {
                from = file.getCodeBase().getHost();
            } else {
                from = file.getInformation().getHomepage().toString();
            }
        } catch (Exception e) {
        }

        return "<br>" + R("Publisher") + ":  " + publisher + "<br>" + R("From") + ": " + from;
    }

    private String getSigningInfo() {
        CertInformation info = jcv.getCertInformation(jcv.getCertPath(null));

        if (info != null && info.isRootInCacerts() && !info.hasSigningIssues()) {
            return R("SSigVerified");
        } else if (info != null && info.isRootInCacerts()) {
            return R("SSigUnverified");
        } else {
            return R("SSignatureError");
        }
    }

    @Override
    protected ImageIcon getInfoImage() {
        final String location = "net/sourceforge/jnlp/resources/warning.png";
        return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(location));
    }

    protected static String getTopPanelTextKey() {
        return "SPartiallySignedSummary";
    }

    protected static String getInfoPanelTextKey() {
        return "SPartiallySignedDetail";
    }

    protected static String getQuestionPanelTextKey() {
        return "SPartiallySignedQuestion";
    }

    @Override
    protected String getTopPanelText() {
        return htmlWrap(R(getTopPanelTextKey()));
    }

    @Override
    protected String getInfoPanelText() {
        String text = getAppletInfo();
        text += "<br><br>" + R(getInfoPanelTextKey(), file.getCodeBase(), file.getSourceLocation());
        text += "<br><br>" + getSigningInfo();
        ExecuteAppletAction rememberedAction = UnsignedAppletTrustConfirmation.getStoredAction(file);
        if (rememberedAction == ExecuteAppletAction.YES) {
            text += "<br>" + R("SUnsignedAllowedBefore");
        } else if (rememberedAction == ExecuteAppletAction.NO) {
            text += "<br>" + R("SUnsignedRejectedBefore");
        }
        return htmlWrap(text);
    }

    @Override
    protected String getQuestionPanelText() {
        return htmlWrap(R(getQuestionPanelTextKey()));
    }

}
