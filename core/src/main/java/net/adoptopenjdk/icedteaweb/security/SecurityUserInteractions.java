package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.tools.CertInformation;

import java.security.cert.CertPath;

/**
 * Interactions with user for concerning security and permission related decisions.
 */
public interface SecurityUserInteractions {

    AllowDeny askUserForPermissionToRunUnsignedApplication(final JNLPFile file);

    AllowDenySandbox askUserHowToRunApplicationWithCertIssues(final JNLPFile file, final CertPath certPath, final CertInformation certInformation);
}
