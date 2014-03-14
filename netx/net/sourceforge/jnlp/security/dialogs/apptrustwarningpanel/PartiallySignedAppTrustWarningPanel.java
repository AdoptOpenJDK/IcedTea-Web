package net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.PluginParameters;
import net.sourceforge.jnlp.security.SecurityDialog;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.DialogType;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public class PartiallySignedAppTrustWarningPanel extends AppTrustWarningPanel {

    private JarCertVerifier jcv;
    private JButton sandboxButton;

    public PartiallySignedAppTrustWarningPanel(JNLPFile file, ActionChoiceListener actionChoiceListener,
            SecurityDialog securityDialog) {
        super(file, actionChoiceListener);
        this.jcv = (JarCertVerifier) securityDialog.getCertVerifier();
        this.INFO_PANEL_HEIGHT = 200;

        sandboxButton = new JButton();
        sandboxButton.setText(R("ButSandbox"));
        sandboxButton.addActionListener(chosenActionSetter(ExecuteAppletAction.SANDBOX));
        buttons.add(1, sandboxButton);
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
                publisher = SecurityUtil.getCN(((X509Certificate) c)
                        .getSubjectX500Principal().getName());
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

        return "<br>" + R("Publisher") + ":  " + publisher
                + "<br>" + R("From") + ": " + from;
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
