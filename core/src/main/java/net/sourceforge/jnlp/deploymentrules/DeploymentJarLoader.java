package net.sourceforge.jnlp.deploymentrules;

import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.security.JNLPAppVerifier;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public class DeploymentJarLoader {

    final AppVerifier verifier = new DeploymentRuleSetJarVerifier();

    JarCertVerifier jcv = new JarCertVerifier(verifier);

    public boolean isJarSignedFully() {
    	return jcv.isFullySigned();
    }
}
