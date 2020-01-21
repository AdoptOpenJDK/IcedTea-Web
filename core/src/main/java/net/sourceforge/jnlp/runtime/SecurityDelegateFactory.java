package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public interface SecurityDelegateFactory {
    SecurityDelegate create(ApplicationPermissions applicationPermissions, JNLPFile jnlpFile, JarCertVerifier certVerifier);
}
