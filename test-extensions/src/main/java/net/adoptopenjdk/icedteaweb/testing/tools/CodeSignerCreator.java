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

package net.adoptopenjdk.icedteaweb.testing.tools;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.CodeSigner;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;


public class CodeSignerCreator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Create a self-signed X509 Certificate signed using SHA256withRSA with a 2048 bit key.
     *
     * @param dname     Domain Name to represent the certificate
     * @param notBefore The date by which the certificate starts being valid. Cannot be null.
     * @param validity  The number of days the certificate is valid after notBefore.
     * @return An X509 certificate setup with properties using the specified parameters.
     * @throws Exception
     */
    public static X509Certificate createCert(final String dname, final Date notBefore, final int validity)
            throws Exception {
        if (dname == null) {
            throw new Exception("Required DN is null. Please specify cert Domain Name via dname");
        }
        if (notBefore == null) {
            throw new Exception("Required start date is null. Please specify the date at which the cert is valid via notBefore");
        }
        if (validity < 0) {
            throw new Exception("Required validity is negative. Please specify the number of days for which the cert is valid after the start date.");
        }

        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair keyPair = generator.generateKeyPair();

        final X500Name name = new X500Name(dname);
        final Date notAfter = new Date(notBefore.getTime() + validity * 24L * 60L * 60L * 1000L);
        final BigInteger serial = BigInteger.valueOf(RANDOM.nextInt() & 0x7fffffff);

        return new JcaX509CertificateConverter().getCertificate(
                new JcaX509v3CertificateBuilder(name, serial, notBefore, notAfter, name, keyPair.getPublic())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate())));
    }

    /**
     * Create a new code signer with the specified information.
     *
     * @param domainName Domain Name to represent the certificate
     * @param notBefore  The date by which the certificate starts being valid. Cannot be null.
     * @param validity   The number of days the certificate is valid after notBefore.
     * @return A code signer with the properties passed through its parameters.
     */
    public static CodeSigner getOneCodeSigner(final String domainName, final Date notBefore, final int validity)
            throws Exception {
        final X509Certificate jarEntryCert = createCert(domainName, notBefore, validity);

        final ArrayList<X509Certificate> certs = new ArrayList<>(1);
        certs.add(jarEntryCert);

        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final CertPath certPath = cf.generateCertPath(certs);
        final Timestamp certTimestamp = new Timestamp(jarEntryCert.getNotBefore(), certPath);
        return new CodeSigner(certPath, certTimestamp);
    }
}
