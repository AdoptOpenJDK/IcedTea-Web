/* Copyright (C) 2014 Red Hat, Inc.

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
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SetValueHandler;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.TemporaryPermissionsButton;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.signing.JarCertVerifier;
import net.sourceforge.jnlp.tools.CertInformation;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Dimension;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils.htmlWrap;

/**
 * @deprecated will be replaced by new security dialogs
 */
@Deprecated
public class PartiallySignedAppTrustWarningPanel extends AppTrustWarningPanel {

    private final JarCertVerifier jcv;
    private final JButton sandboxButton;
    private final JButton advancedOptionsButton;

    public PartiallySignedAppTrustWarningPanel(JNLPFile file, SecurityDialog securityDialog, SecurityDelegate securityDelegate) {
        super(file, securityDialog);
        this.jcv = (JarCertVerifier) securityDialog.getCertVerifier();
        this.INFO_PANEL_HEIGHT = 250;

        sandboxButton = new JButton();
        sandboxButton.setText(R("ButSandbox"));
        sandboxButton.addActionListener(SetValueHandler.createSetValueListener(parent,
                YesNoSandbox.sandbox()));
        advancedOptionsButton = new TemporaryPermissionsButton(file, securityDelegate, sandboxButton);

        buttons.add(1, sandboxButton);
        buttons.add(2, advancedOptionsButton);

        addComponents();
        securityDialog.getViewableDialog().setMinimumSize(new Dimension(600, 400));
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
            from = file.getInformation().getHomepage().toExternalForm();
        } catch (Exception e) {
        }

        return "<br>" + R("Publisher") + ":  " + publisher + "<br>" + R("From") + ": <a href='" + from + "'>" + from + "</a>";
    }

    private String getSigningInfo() {
        CertInformation info = jcv.getCertInformation(jcv.getCertPath());

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
        return ImageGallery.WARNING.asImageIcon();
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
        UnsignedAppletActionEntry rememberedEntry = UnsignedAppletTrustConfirmation.getStoredEntry(file, this.getClass());
        if (rememberedEntry != null) {
            ExecuteAppletAction rememberedAction = rememberedEntry.getAppletSecurityActions().getAction(this.getClass());
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

    @Override
    public DialogResult readValue(String s) {
        return YesNoSandbox.readValue(s);
    }

    @Override
    public DialogResult getDefaultNegativeAnswer() {
        return YesNoSandbox.sandbox();
    }

    @Override
    public DialogResult getDefaultPositiveAnswer() {
        return YesNoSandbox.yes();
    }

    @Override
    public DialogResult readFromStdIn(String what) {
        return YesNoSandbox.readValue(what);
    }

    @Override
    public String helpToStdIn() {
        return YesNoSandbox.sandbox().getAllowedValues().toString();
    }

}
