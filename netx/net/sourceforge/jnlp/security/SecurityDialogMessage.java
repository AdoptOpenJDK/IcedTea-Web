package net.sourceforge.jnlp.security;

import java.security.cert.X509Certificate;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.DialogType;

/**
 * Represents a message to the security framework to show a specific security
 * dialog
 */
final class SecurityDialogMessage {

    /*
     * These fields contain information need to display the correct dialog type
     */

    public DialogType dialogType;
    public AccessType accessType;
    public JNLPFile file;
    public CertVerifier certVerifier;
    public X509Certificate certificate;
    public Object[] extras;

    /*
     * Volatile because this is shared between threads and we dont want threads
     * to use a cached value of this.
     */
    public volatile Object userResponse;

    /*
     * These two fields are used to block/unblock the application or the applet.
     * If either of them is not null, call release() or dispose() on it to allow
     * the application/applet to continue.
     */

    public Semaphore lock;
    public JDialog toDispose;

}
