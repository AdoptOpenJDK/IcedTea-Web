package net.sourceforge.jnlp.deploymentrules;

import java.security.cert.CertPath;
import java.util.Map;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.classloader.SecurityDelegate;
import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;
/**
 * DeploymentRuleSetJarVerifier object for accessing jar file.
 * 
 * This is class is rarely used yet and can be extended when a 
 * UI component to display the entire rulset.xml file and edit it will be enhanced
 */
public class DeploymentRuleSetJarVerifier implements AppVerifier {

	@Override
	public boolean hasAlreadyTrustedPublisher(Map<CertPath, CertInformation> certs, Map<String, Integer> signedJars) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRootInCacerts(Map<CertPath, CertInformation> certs, Map<String, Integer> signedJars) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFullySigned(Map<CertPath, CertInformation> certs, Map<String, Integer> signedJars) {
       
      int sumOfSignableEntries = JarCertVerifier.getTotalJarEntries(signedJars);
      for (CertPath cPath : certs.keySet()) {
          // If this cert has signed everything, return true
          if (hasCompletelySignedApp(certs.get(cPath), sumOfSignableEntries)) {
              return true;
          }
      }

      // No cert found that signed all entries. Return false.
      return false;

	}
	
	  /**
     * Find out if the CertPath with the given info has fully signed the app.
     * @param info The information regarding the CertPath in question
     * @param sumOfSignableEntries The total number of signable entries in the app.
     * @return True if the signer has fully signed this app.
     */
    public boolean hasCompletelySignedApp(CertInformation info, int sumOfSignableEntries) {
        return JarCertVerifier.getTotalJarEntries(info.getSignedJars()) == sumOfSignableEntries;
    }

	@Override
	public void checkTrustWithUser(SecurityDelegate securityDelegate, JarCertVerifier jcv, JNLPFile file)
			throws LaunchException {
		// TODO Auto-generated method stub
		
	}

}
