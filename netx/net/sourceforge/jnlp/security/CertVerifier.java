/* CertVerifier.java
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
import java.security.cert.Certificate;
import java.util.List;

/**
 * An interface that provides various details about certificates of an app.
 */

public interface CertVerifier {

    /**
     * @return  if the publisher is already trusted
     */
    public boolean getAlreadyTrustPublisher();

    /**
     * @return  if the root is in CA certs
     */
    public boolean getRootInCacerts();

    /**
     * @return  if there are signing issues with the certificate being verified
     * @param certPath to be validated
     */
    public boolean hasSigningIssues(CertPath certPath);

    /**
     * @return  the details regarding issue with this certificate
     * @param certPath certificate
     */
    public List<String> getDetails(CertPath certPath);

    /**
     * @return  a valid certificate path to this certificate being verified
     * @param certPath to be read
     */
    public CertPath getCertPath(CertPath certPath);

    /**
     * @return the application's publisher's certificate.
     * @param certPath to be read for publisher
     */
    public abstract Certificate getPublisher(CertPath certPath);

    /**
     * @return  the application's root's certificate. This
     * may return the same certificate as getPublisher(CertPath certPath) in
     * the event that the application is self signed.
     * @param certPath certificate
     *  
     */
    public abstract Certificate getRoot(CertPath certPath);
}
