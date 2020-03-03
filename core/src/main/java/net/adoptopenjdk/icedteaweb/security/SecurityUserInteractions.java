package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.sourceforge.jnlp.JNLPFile;

/**
 * Interactions with user for concerning security and permission related decisions.
 */
public interface SecurityUserInteractions {

    AllowDeny askUserForPermissionToRunUnsignedApplication(final JNLPFile file);
}
