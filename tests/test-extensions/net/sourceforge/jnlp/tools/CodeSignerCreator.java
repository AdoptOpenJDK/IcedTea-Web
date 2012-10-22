/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package net.sourceforge.jnlp.tools;

import java.security.CodeSigner;
import java.security.PrivateKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class CodeSignerCreator {

    /**
     * Create an X509 Certificate signed using SHA1withRSA with a 2048 bit key.
     * @param dname Domain Name to represent the certificate
     * @param notBefore The date by which the certificate starts being valid. Cannot be null.
     * @param validity The number of days the certificate is valid after notBefore.
     * @return An X509 certificate setup with properties using the specified parameters.
     * @throws Exception
     */
    public static X509Certificate createCert(String dname, Date notBefore, int validity)
            throws Exception {
        int keysize = 2048;
        String keyAlgName = "RSA";
        String sigAlgName = "SHA1withRSA";

        if (dname == null)
            throw new Exception("Required DN is null. Please specify cert Domain Name via dname");
        if (notBefore == null)
            throw new Exception("Required start date is null. Please specify the date at which the cert is valid via notBefore");
        if (validity < 0)
            throw new Exception("Required validity is negative. Please specify the number of days for which the cert is valid after the start date.");

        // KeyTool#doGenKeyPair
        X500Name x500Name = new X500Name(dname);

        CertAndKeyGen keypair = new CertAndKeyGen(keyAlgName, sigAlgName);

        keypair.generate(keysize);
        PrivateKey privKey = keypair.getPrivateKey();

        X509Certificate oldCert = keypair.getSelfCertificate(x500Name,
                notBefore, validity * 24L * 60L * 60L);

        // KeyTool#doSelfCert
        byte[] encoded = oldCert.getEncoded();
        X509CertImpl certImpl = new X509CertImpl(encoded);
        X509CertInfo certInfo = (X509CertInfo) certImpl.get(X509CertImpl.NAME
                + "." + X509CertImpl.INFO);

        Date notAfter = new Date(notBefore.getTime() + validity*1000L*24L*60L*60L);

        CertificateValidity interval = new CertificateValidity(notBefore,
                notAfter);

        certInfo.set(X509CertInfo.VALIDITY, interval);
        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                    new java.util.Random().nextInt() & 0x7fffffff));
        certInfo.set(X509CertInfo.SUBJECT + "." + CertificateSubjectName.DN_NAME, x500Name);
        certInfo.set(X509CertInfo.ISSUER + "." + CertificateIssuerName.DN_NAME, x500Name);

        // The inner and outer signature algorithms have to match.
        // The way we achieve that is really ugly, but there seems to be no
        // other solution: We first sign the cert, then retrieve the
        // outer sigalg and use it to set the inner sigalg
        X509CertImpl newCert = new X509CertImpl(certInfo);
        newCert.sign(privKey, sigAlgName);
        AlgorithmId sigAlgid = (AlgorithmId)newCert.get(X509CertImpl.SIG_ALG);
        certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, sigAlgid);

        certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));

        // FIXME Figure out extensions
//        CertificateExtensions ext = createV3Extensions(
//                null,
//                (CertificateExtensions)certInfo.get(X509CertInfo.EXTENSIONS),
//                v3ext,
//                oldCert.getPublicKey(),
//                null);
//        certInfo.set(X509CertInfo.EXTENSIONS, ext);

        newCert = new X509CertImpl(certInfo);
        newCert.sign(privKey, sigAlgName);

        return newCert;
    }

    /**
     * Create a new code signer with the specified information.
     * @param domainName Domain Name to represent the certificate
     * @param notBefore The date by which the certificate starts being valid. Cannot be null.
     * @param validity The number of days the certificate is valid after notBefore.
     * @return A code signer with the properties passed through its parameters.
     */
    public static CodeSigner getOneCodeSigner(String domainName, Date notBefore, int validity)
            throws Exception {
        X509Certificate jarEntryCert = createCert(domainName, notBefore, validity);

        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>(1);
        certs.add(jarEntryCert);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath certPath = cf.generateCertPath(certs);
        Timestamp certTimestamp = new Timestamp(jarEntryCert.getNotBefore(), certPath);
        return new CodeSigner(certPath, certTimestamp);
    }
}
