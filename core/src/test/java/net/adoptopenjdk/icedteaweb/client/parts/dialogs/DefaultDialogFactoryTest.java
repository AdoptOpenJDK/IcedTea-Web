package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.MissingPermissionsAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.MatchingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.ResourceFactory;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.SecurityDelegateNew;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.HttpsCertVerifier;
import net.sourceforge.jnlp.signing.JarCertVerifier;
import net.sourceforge.jnlp.util.UrlUtils;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to start dialogs without starting ITW.
 */
public class DefaultDialogFactoryTest {

    private final JNLPFile file;
    private final HttpsCertVerifier httpsCertVerifier;
    private final JarCertVerifier jarCertVerifier;
    private final DefaultDialogFactory dialogFactory;

    public DefaultDialogFactoryTest() throws Exception {
        JNLPRuntime.initialize();
        file = new JNLPFileFactory().create(getClass().getResource("/net/sourceforge/jnlp/basic.jnlp"));
        httpsCertVerifier = new HttpsCertVerifier(new X509Certificate[0], true, true, "hostname");
        jarCertVerifier = new JarCertVerifier();
        dialogFactory = new DefaultDialogFactory();
    }

    public static void main(String[] args) throws Exception {
        // new DefaultDialogFactoryTest().showPartiallySignedWarning();
        // new DefaultDialogFactoryTest().showCertInfoDialog();
        // new DefaultDialogFactoryTest().showMoreInfoDialog();

        // new DefaultDialogFactoryTest().showMissingALACAttributePanel();
        // new DefaultDialogFactoryTest().showMatchingALACAttributePanel();
         new DefaultDialogFactoryTest().showMissingPermissionsAttributeDialogue();
        //new DefaultDialogFactoryTest().show511Dialog();

    }

    private void showCertInfoDialog() {
        new DefaultDialogFactory().showCertInfoDialog(httpsCertVerifier, null);
    }

    private void showAccessWarning() {
        dialogFactory.showAccessWarningDialog(AccessType.CREATE_DESKTOP_SHORTCUT, file, new Object[]{"test"});
    }

    private void showUnsignedWarning() {
        dialogFactory.showUnsignedWarningDialog(file);
    }

    private void showCertWarning() {
        dialogFactory.showCertWarningDialog(AccessType.UNVERIFIED, file, jarCertVerifier, null);
        dialogFactory.showCertWarningDialog(AccessType.UNVERIFIED, file, httpsCertVerifier, null);
    }

    private void showPartiallySignedWarning() {
        dialogFactory.showPartiallySignedWarningDialog(file, jarCertVerifier, new SecurityDelegateNew(null, file, null));
    }

    private void showMissingALACAttributePanel() throws MalformedURLException {
        Set<URL> s = new HashSet<>();
        s.add(new URL("http:/blah.com/blah"));
        s.add(new URL("http:/blah.com/blah/blah"));
        MissingALACAttributePanel w = new MissingALACAttributePanel(null, "HelloWorld", "http://nbblah.url", UrlUtils.setOfUrlsToHtmlList(s));
        JFrame f = new JFrame();
        f.setSize(600, 400);
        f.add(w, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private void showMatchingALACAttributePanel() throws MalformedURLException {
        Set<URL> s = new HashSet<>();
        s.add(new URL("http:/blah.com/blah"));
        s.add(new URL("http:/blah.com/blah/blah"));
        MatchingALACAttributePanel w = new MatchingALACAttributePanel(null, file, "http://nbblah.url", UrlUtils.setOfUrlsToHtmlList(s));
        JFrame f = new JFrame();
        f.setSize(600, 400);
        f.add(w, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }


    private void showMissingPermissionsAttributeDialogue() {
        MissingPermissionsAttributePanel w = new MissingPermissionsAttributePanel(null, "HelloWorld", "http://nbblah.url");
        JFrame f = new JFrame();
        f.setSize(400, 400);
        f.add(w, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private void show511Dialog() throws MalformedURLException {
        Resource resource = ResourceFactory.createResource(new URL("http://example.com/test.jar"), VersionString.fromString("1.0"), null, UpdatePolicy.ALWAYS);

        dialogFactory.show511Dialogue(resource);
    }

    private void showMoreInfoDialog() throws IOException, ParseException {
        new DefaultDialogFactory().showMoreInfoDialog(new JarCertVerifier(), file);
    }
}
