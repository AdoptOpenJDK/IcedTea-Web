package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.tools.JarCertVerifier;

class DefaultSecurityDelegateFactory implements SecurityDelegateFactory {
    @Override
    public SecurityDelegate create(ApplicationPermissions applicationPermissions, JNLPFile jnlpFile, JarCertVerifier certVerifier) {
        return new SecurityDelegateNew(applicationPermissions, jnlpFile, certVerifier);
    }
}
