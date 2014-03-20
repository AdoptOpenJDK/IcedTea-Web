/* JNLPAppVerifier.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp.security;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.security.cert.CertPath;
import java.util.Map;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.AppletAction;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public class JNLPAppVerifier implements AppVerifier {

    @Override
    public boolean hasAlreadyTrustedPublisher(
            Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {
        int sumOfSignableEntries = JarCertVerifier.getTotalJarEntries(signedJars);
        for (CertInformation certInfo : certs.values()) {
            Map<String, Integer> certSignedJars = certInfo.getSignedJars();

            if (JarCertVerifier.getTotalJarEntries(certSignedJars) == sumOfSignableEntries
                    && certInfo.isPublisherAlreadyTrusted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRootInCacerts(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {
        int sumOfSignableEntries = JarCertVerifier.getTotalJarEntries(signedJars);
        for (CertInformation certInfo : certs.values()) {
            Map<String, Integer> certSignedJars = certInfo.getSignedJars();

            if (JarCertVerifier.getTotalJarEntries(certSignedJars) == sumOfSignableEntries
                    && certInfo.isRootInCacerts()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFullySigned(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {
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

    @Override
    public void checkTrustWithUser(SecurityDelegate securityDelegate, JarCertVerifier jcv, JNLPFile file)
            throws LaunchException {

        int sumOfSignableEntries = JarCertVerifier.getTotalJarEntries(jcv.getJarSignableEntries());
        for (CertPath cPath : jcv.getCertsList()) {
            jcv.setCurrentlyUsedCertPath(cPath);
            CertInformation info = jcv.getCertInformation(cPath);
            if (hasCompletelySignedApp(info, sumOfSignableEntries)) {
                if (info.isPublisherAlreadyTrusted()) {
                    return;
                }

                AccessType dialogType;
                if (info.isRootInCacerts() && !info.hasSigningIssues()) {
                    dialogType = AccessType.VERIFIED;
                } else if (info.isRootInCacerts()) {
                    dialogType = AccessType.SIGNING_ERROR;
                } else {
                    dialogType = AccessType.UNVERIFIED;
                }

                AppletAction action = SecurityDialogs.showCertWarningDialog(
                        dialogType, file, jcv, securityDelegate);
                if (action != AppletAction.CANCEL) {
                    if (action == AppletAction.SANDBOX) {
                        securityDelegate.setRunInSandbox();
                    }
                    return;
                }
            }
        }

        throw new LaunchException(null, null, R("LSFatal"), R("LCLaunching"),
                R("LCancelOnUserRequest"), "");
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
}
