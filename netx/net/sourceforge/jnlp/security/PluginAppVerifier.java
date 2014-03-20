/* PluginAppVerifier.java
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.AppletAction;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;

public class PluginAppVerifier implements AppVerifier {

    @Override
    public boolean hasAlreadyTrustedPublisher(
            Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {

        boolean allPublishersTrusted = true;

        for(String jarName : signedJars.keySet()) {
            int numbSignableEntries = signedJars.get(jarName);
            boolean publisherTrusted = false;

            for (CertInformation certInfo : certs.values()) {
                if(certInfo.isSignerOfJar(jarName)
                        && numbSignableEntries == certInfo.getNumJarEntriesSigned(jarName)
                        && certInfo.isPublisherAlreadyTrusted()) {
                    publisherTrusted = true;
                    break;
                }
            }

            allPublishersTrusted &= publisherTrusted;
        }
        return allPublishersTrusted;
    }

    @Override
    public boolean hasRootInCacerts(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {

        boolean allRootCAsTrusted = true;

        for(String jarName : signedJars.keySet()) {
            int numbSignableEntries = signedJars.get(jarName);
            boolean rootCATrusted = false;

            for (CertInformation certInfo : certs.values()) {
                if(certInfo.isSignerOfJar(jarName)
                        && numbSignableEntries == certInfo.getNumJarEntriesSigned(jarName)
                        && certInfo.isRootInCacerts()) {
                    rootCATrusted = true;
                    break;
                }
            }

            allRootCAsTrusted &= rootCATrusted;
        }
        return allRootCAsTrusted;
    }

    @Override
    public boolean isFullySigned(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {

        boolean isFullySigned = true;

        for(String jarName : signedJars.keySet()) {
            int numbSignableEntries = signedJars.get(jarName);
            boolean isSigned = false;

            for (CertInformation certInfo : certs.values()) {
                if(certInfo.isSignerOfJar(jarName)
                        && numbSignableEntries == certInfo.getNumJarEntriesSigned(jarName)) {
                    isSigned = true;
                    break;
                }
            }

            isFullySigned &= isSigned;
        }

        return isFullySigned;
    }

    @Override
    public void checkTrustWithUser(SecurityDelegate securityDelegate, JarCertVerifier jcv, JNLPFile file)
            throws LaunchException {
        List<CertPath> certPaths = buildCertPathsList(jcv);
        List<CertPath> alreadyApprovedByUser = new ArrayList<CertPath>();
        for (String jarName : jcv.getJarSignableEntries().keySet()) {
            boolean trustFoundOrApproved = false;
            for (CertPath cPathApproved : alreadyApprovedByUser) {
                jcv.setCurrentlyUsedCertPath(cPathApproved);
                CertInformation info = jcv.getCertInformation(cPathApproved);
                if (info.isSignerOfJar(jarName)
                        && alreadyApprovedByUser.contains(cPathApproved)) {
                    trustFoundOrApproved = true;
                    break;
                }
            }

            if (trustFoundOrApproved) {
                continue;
            }

            for (CertPath cPath : certPaths) {
                jcv.setCurrentlyUsedCertPath(cPath);
                CertInformation info = jcv.getCertInformation(cPath);
                if (info.isSignerOfJar(jarName)) {
                    if (info.isPublisherAlreadyTrusted()) {
                        trustFoundOrApproved = true;
                        alreadyApprovedByUser.add(cPath);
                        break;
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
                        alreadyApprovedByUser.add(cPath);
                        trustFoundOrApproved = true;
                        break;
                    }
                }
            }
            if (!trustFoundOrApproved) {
                throw new LaunchException(null, null, R("LSFatal"),
                    R("LCLaunching"), R("LCancelOnUserRequest"), "");
            }
        }
    }

    /**
     * Build a list of all the CertPaths that were detected in the provided
     * JCV, placing them in the most trusted possible order.
     * @param jcv The verifier containing the CertPaths to examine.
     * @return A list of CertPaths sorted in the following order: Signers with
     *   1. Already trusted publishers
     *   2. Roots in the CA store and have no signing issues
     *   3. Roots in the CA store but have signing issues
     *   4. Everything else
     */
    public List<CertPath> buildCertPathsList(JarCertVerifier jcv) {
        List<CertPath> certPathsList = jcv.getCertsList();
        List<CertPath> returnList = new ArrayList<CertPath>();

        for (CertPath cPath : certPathsList) {
            if (!returnList.contains(cPath)
                    && jcv.getCertInformation(cPath).isPublisherAlreadyTrusted())
                returnList.add(cPath);
        }

        for (CertPath cPath : certPathsList) {
            if (!returnList.contains(cPath)
                    && jcv.getCertInformation(cPath).isRootInCacerts()
                    && !jcv.getCertInformation(cPath).hasSigningIssues())
                returnList.add(cPath);
        }

        for (CertPath cPath : certPathsList) {
            if (!returnList.contains(cPath)
                    && jcv.getCertInformation(cPath).isRootInCacerts()
                    && jcv.getCertInformation(cPath).hasSigningIssues())
                returnList.add(cPath);
        }

        for (CertPath cPath : certPathsList) {
            if (!returnList.contains(cPath))
                returnList.add(cPath);
        }

        return returnList;
    }
}
