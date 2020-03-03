/* SecurityDialogs.java
 Copyright (C) 2010 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessageHandler;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;

import java.awt.Component;
import java.awt.Window;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * <p>
 * A factory for showing many possible types of security warning to the user.
 * </p>
 * <p>
 * This contains all the public methods that classes outside this package should
 * use instead of using {@link SecurityDialog} directly.
 * </p>
 * <p>
 * All of these methods post a message to the
 * {@link SecurityDialogMessageHandler} and block waiting for a response.
 * </p>
 */
public class Dialogs {

    private static final DialogFactory DEFAULT_DIALOG_FACTORY = new DefaultDialogFactory();

    private static DialogFactory dialogFactory = DEFAULT_DIALOG_FACTORY;

    @FunctionalInterface
    public interface Uninstaller extends AutoCloseable {
        void uninstall();

        @Override
        default void close() {
            uninstall();
        }
    }

    public static synchronized Uninstaller setDialogFactory(final DialogFactory dialogs) {
        Assert.requireNonNull(dialogs, "dialogs");
        dialogFactory = dialogs;
        return () -> dialogFactory = DEFAULT_DIALOG_FACTORY;
    }

    private static DialogFactory getDialogs() {
        return dialogFactory;
    }

    /**
     * see {@link DialogFactory#showAccessWarningDialog(AccessType, JNLPFile, Object[])}.
     */
    public static AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType,
                                                                         final JNLPFile file, final Object[] extras) {
        return getDialogs().showAccessWarningDialog(accessType, file, extras);
    }

    /**
     * see {@link DialogFactory#showCertWarningDialog(AccessType, JNLPFile, CertVerifier, SecurityDelegate)}.
     */
    public static YesNoSandbox showCertWarningDialog(AccessType accessType,
                                                     JNLPFile file, CertVerifier certVerifier, SecurityDelegate securityDelegate) {
        return getDialogs().showCertWarningDialog(accessType, file, certVerifier, securityDelegate);
    }

    /**
     * see {@link DialogFactory#showPartiallySignedWarningDialog(JNLPFile, CertVerifier, SecurityDelegate)}.
     */
    public static YesNoSandbox showPartiallySignedWarningDialog(JNLPFile file, CertVerifier certVerifier,
                                                                SecurityDelegate securityDelegate) {
        return getDialogs().showPartiallySignedWarningDialog(file, certVerifier, securityDelegate);
    }

    /**
     * see {@link DialogFactory#showAuthenticationPrompt(String, int, String, String)}.
     */
    public static NamePassword showAuthenticationPrompt(String host, int port, String prompt, String type) {
        return getDialogs().showAuthenticationPrompt(host, port, prompt, type);
    }

    /**
     * see {@link DialogFactory#showMissingALACAttributePanel(JNLPFile, URL, Set)}
     */
    public static boolean showMissingALACAttributePanel(JNLPFile file, URL codeBase, Set<URL> remoteUrls) {
        return getDialogs().showMissingALACAttributePanel(file, codeBase, remoteUrls);
    }

    /**
     * see {@link DialogFactory#showMatchingALACAttributePanel(JNLPFile, URL, Set)}.
     */
    public static boolean showMatchingALACAttributePanel(JNLPFile file, URL documentBase, Set<URL> remoteUrls) {
        return getDialogs().showMatchingALACAttributePanel(file, documentBase, remoteUrls);
    }

    /**
     * see {@link DialogFactory#showMissingPermissionsAttributeDialogue(JNLPFile)}.
     */
    public static boolean showMissingPermissionsAttributeDialogue(JNLPFile file) {
        return getDialogs().showMissingPermissionsAttributeDialogue(file);
    }

    /**
     * see {@link DialogFactory#show511Dialogue(Resource)}.
     */
    public static boolean show511Dialogue(Resource r) {
        return getDialogs().show511Dialogue(r);
    }

    /**
     * see {@link DialogFactory#showMoreInfoDialog(CertVerifier, JNLPFile)}.
     */
    public static void showMoreInfoDialog(CertVerifier certVerifier, JNLPFile file) {
        getDialogs().showMoreInfoDialog(certVerifier, file);
    }

    /**
     * see {@link DialogFactory#showCertInfoDialog(CertVerifier, Component)}.
     */
    public static void showCertInfoDialog(CertVerifier certVerifier, Component parent) {
        getDialogs().showCertInfoDialog(certVerifier, parent);
    }

    /**
     * see {@link DialogFactory#showSingleCertInfoDialog(X509Certificate, Window)}.
     */
    public static void showSingleCertInfoDialog(X509Certificate c, Window parent) {
        getDialogs().showSingleCertInfoDialog(c, parent);
    }

}
