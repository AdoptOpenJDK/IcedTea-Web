/* AppVerifier.java
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

import java.security.cert.CertPath;
import java.util.Map;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.tools.JarCertVerifier;

/**
 * An interface that provides various details about an app's signers.
 */
public interface AppVerifier {

    /**
     * Checks if the app has already found trust in its publisher(s).
     * @param certs The certs to search through and their cert information
     * @param signedJars A map of all the jars of this app and the number of
     * signed entries each one has.
     * @return True if the app trusts its publishers.
     */
    public boolean hasAlreadyTrustedPublisher(
            Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars);

    /**
     * Checks if the app has signer(s) whose certs along their chains are in CA certs.
     * @param certs The certs to search through and their cert information
     * @param signedJars A map of all the jars of this app and the number of
     * signed entries each one has.
     * @return True if the app has a root in the CA certs store.
     */
    public boolean hasRootInCacerts(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars);

    /**
     * Checks if the app's jars are covered by the provided certificates, enough
     * to consider the app fully signed.
     * @param certs Any possible signer and their respective information regarding this app.
     * @param signedJars A map of all the jars of this app and the number of
     * signed entries each one has.
     * @return true if jar is fully signed
     */
    public boolean isFullySigned(Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars);

    /**
     * Prompt the user with requests for trusting the certificates used by this app
     * @param securityDelegate parental security
     * @param jcv jar verifier
     * @param file jnlp fiel to provide information
     * @throws LaunchException if it fails to verify
     */
    public void checkTrustWithUser(SecurityDelegate securityDelegate, JarCertVerifier jcv, JNLPFile file)
            throws LaunchException;
}
