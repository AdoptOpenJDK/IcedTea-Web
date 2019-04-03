/* JarCertVerifierTest.java
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

package net.sourceforge.jnlp.tools;

import static net.sourceforge.jnlp.runtime.Translator.R;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.CodeSigner;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;

import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.tools.JarCertVerifier.VerifyResult;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JarCertVerifierTest {

    @Test
    public void testIsMetaInfFile() {
        final String METAINF = "META-INF";
        assertFalse(JarCertVerifier.isMetaInfFile("some_dir/" + METAINF + "/filename"));
        assertFalse(JarCertVerifier.isMetaInfFile(METAINF + "filename"));
        assertTrue(JarCertVerifier.isMetaInfFile(METAINF + "/filename"));
    }

    class JarCertVerifierEntry extends JarEntry {
        CodeSigner[] signers;

        public JarCertVerifierEntry(String name, CodeSigner[] codesigners) {
            super(name);
            signers = codesigners;
        }

        public JarCertVerifierEntry(String name) {
            this(name, null);
        }

        public CodeSigner[] getCodeSigners() {
            return signers == null ? null : signers.clone();
        }
    }

    // Empty list to be used with JarCertVerifier constructor.
    private static final List<JARDesc> emptyJARDescList = new Vector<JARDesc>();

    private static final String DNPARTIAL = ", OU=JarCertVerifier Unit Test, O=IcedTea, L=Toronto, ST=Ontario, C=CA";
    private static CodeSigner alphaSigner, betaSigner, charlieSigner,
            expiredSigner, expiringSigner, notYetValidSigner, expiringAndNotYetValidSigner;

    @BeforeClass
    public static void setUp() throws Exception {
        Date currentDate = new Date();
        Date pastDate = new Date(currentDate.getTime() - (1000L * 24L * 60L * 60L) - 1000L); // 1 day and 1 second in the past
        Date futureDate = new Date(currentDate.getTime() + (1000L * 24L * 60L * 60L)); // 1 day in the future
        alphaSigner = CodeSignerCreator.getOneCodeSigner("CN=Alpha Signer" + DNPARTIAL, currentDate, 365);
        betaSigner = CodeSignerCreator.getOneCodeSigner("CN=Beta Signer" + DNPARTIAL, currentDate, 365);
        charlieSigner = CodeSignerCreator.getOneCodeSigner("CN=Charlie Signer" + DNPARTIAL, currentDate, 365);
        expiredSigner = CodeSignerCreator.getOneCodeSigner("CN=Expired Signer" + DNPARTIAL, pastDate, 1);
        expiringSigner = CodeSignerCreator.getOneCodeSigner("CN=Expiring Signer" + DNPARTIAL, currentDate, 1);
        notYetValidSigner = CodeSignerCreator.getOneCodeSigner("CN=Not Yet Valid Signer" + DNPARTIAL, futureDate, 365);
        expiringAndNotYetValidSigner = CodeSignerCreator.getOneCodeSigner("CN=Expiring and Not Yet Valid Signer" + DNPARTIAL, futureDate, 3);
    }

    @Test
    public void testNoManifest() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        VerifyResult result = jcv.verifyJarEntryCerts("", false, null);

        Assert.assertEquals("No manifest should be considered unsigned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("No manifest means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testNoSignableEntries() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("OneDirEntry/"));
        entries.add(new JarCertVerifierEntry("META-INF/MANIFEST.MF"));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("No signable entry (only dirs/manifests) should be considered trivially signed.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("No signable entry (only dirs/manifests) means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testSingleEntryNoSigners() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstEntryWithoutSigner"));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One unsigned entry should be considered unsigned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("One unsigned entry means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testManyEntriesNoSigners() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstEntryWithoutSigner"));
        entries.add(new JarCertVerifierEntry("secondEntryWithoutSigner"));
        entries.add(new JarCertVerifierEntry("thirdEntryWithoutSigner"));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Many unsigned entries should be considered unsigned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("Many unsigned entries means no signers in the verifier.", 0,
                jcv.getCertsList().size());
    }

    @Test
    public void testSingleEntrySingleValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] signers = { alphaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByOne", signers));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One signed entry should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("One signed entry means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("One signed entry means one signer in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesSingleValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] signers = { alphaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByOne", signers));
        entries.add(new JarCertVerifierEntry("secondSignedByOne", signers));
        entries.add(new JarCertVerifierEntry("thirdSignedByOne", signers));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by one signer should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries signed by one signer means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by one signer means one signer in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

    @Test
    public void testSingleEntryMultipleValidSigners() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] signers = { alphaSigner, betaSigner, charlieSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByThree", signers));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by three signers should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("One entry signed by three means three signers in the verifier.",
                3, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by three means three signers in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(betaSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(charlieSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesMultipleValidSigners() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] signers = { alphaSigner, betaSigner, charlieSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByThree", signers));
        entries.add(new JarCertVerifierEntry("secondSignedByThree", signers));
        entries.add(new JarCertVerifierEntry("thirdSignedByThree", signers));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by three signers should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries signed by three means three signers in the verifier.",
                3, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by three means three signers in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(betaSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(charlieSigner.getSignerCertPath()));
    }

    @Test
    public void testOneCommonSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] alphaSigners = { alphaSigner };
        CodeSigner[] betaSigners = { alphaSigner, betaSigner };
        CodeSigner[] charlieSigners = { alphaSigner, charlieSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByOne", alphaSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByTwo", betaSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByTwo", charlieSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by at least one common signer should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries signed completely by only one signer means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed completely by only one signer means one signer in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

    @Test
    public void testNoCommonSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] alphaSigners = { alphaSigner };
        CodeSigner[] betaSigners = { betaSigner };
        CodeSigner[] charlieSigners = { charlieSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByAlpha", alphaSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByBeta", betaSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByCharlie", charlieSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by no common signers should be considered unsigned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("Three entries signed by no common signers means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testFewButNotAllCommonSigners() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] alphaSigners = { alphaSigner };
        CodeSigner[] betaSigners = { betaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByAlpha", alphaSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByAlpha", alphaSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByBeta", betaSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("First two entries signed by alpha signer, third entry signed by beta signer should be considered unisgned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("Three entries signed by some common signers but not all means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testNotAllEntriesSigned() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] alphaSigners = { alphaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByAlpha", alphaSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByAlpha", alphaSigners));
        entries.add(new JarCertVerifierEntry("thirdUnsigned"));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("First two entries signed by alpha signer, third entry not signed, should be considered unisgned.",
                VerifyResult.UNSIGNED, result);
        Assert.assertEquals("First two entries signed by alpha signer, third entry not signed, means no signers in the verifier.",
                0, jcv.getCertsList().size());
    }

    @Test
    public void testSingleEntryExpiredSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] expiredSigners = { expiredSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpired", expiredSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by expired cert, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("One entry signed by expired cert means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by expired cert means one signer in the verifier.",
                jcv.getCertsList().contains(expiredSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesExpiredSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] expiredSigners = { expiredSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpired", expiredSigners));
        entries.add(new JarCertVerifierEntry("secondSignedBExpired", expiredSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByExpired", expiredSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by expired cert, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("Three entries signed by expired cert means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by expired cert means one signer in the verifier.",
                jcv.getCertsList().contains(expiredSigner.getSignerCertPath()));
    }

    @Test
    public void testSingleEntryExpiringSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] expiringSigners = { expiringSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpiring", expiringSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by expiring cert, should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("One entry signed by expiring cert means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by expiring cert means one signer in the verifier.",
                jcv.getCertsList().contains(expiringSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesExpiringSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] expiringSigners = { expiringSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpiring", expiringSigners));
        entries.add(new JarCertVerifierEntry("secondSignedBExpiring", expiringSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByExpiring", expiringSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by expiring cert, should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries signed by expiring cert means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by expiring cert means one signer in the verifier.",
                jcv.getCertsList().contains(expiringSigner.getSignerCertPath()));
    }

    @Test
    public void testSingleEntryNotYetValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] notYetValidSigners = { notYetValidSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByNotYetValid", notYetValidSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by cert that is not yet valid, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("One entry signed by cert that is not yet valid means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by cert that is not yet valid means one signer in the verifier.",
                jcv.getCertsList().contains(notYetValidSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesNotYetValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] notYetValidSigners = { notYetValidSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByNotYetValid", notYetValidSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByNotYetValid", notYetValidSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByNotYetValid", notYetValidSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by cert that is not yet valid, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("Three entries signed by cert that is not yet valid means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by cert that is not yet valid means one signer in the verifier.",
                jcv.getCertsList().contains(notYetValidSigner.getSignerCertPath()));
    }

    @Test
    public void testSingleEntryExpiringAndNotYetValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] expiringAndNotYetValidSigners = { expiringAndNotYetValidSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpiringNotYetValid", expiringAndNotYetValidSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by cert that is not yet valid but also expiring, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("One entry signed by cert that is not yet valid but also expiring means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by cert that is not yet valid but also expiring means one signer in the verifier.",
                jcv.getCertsList().contains(expiringAndNotYetValidSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntryExpiringAndNotYetValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);

        CodeSigner[] expiringAndNotYetValidSigners = { expiringAndNotYetValidSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByExpiringNotYetValid", expiringAndNotYetValidSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByExpiringNotYetValid", expiringAndNotYetValidSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByExpiringNotYetValid", expiringAndNotYetValidSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by cert that is not yet valid but also expiring, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("Three entries signed by cert that is not yet valid but also expiring means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by cert that is not yet valid but also expiring means one signer in the verifier.",
                jcv.getCertsList().contains(expiringAndNotYetValidSigner.getSignerCertPath()));
        Assert.assertTrue("Three entries signed by cert that is not yet valid but also expiring means expiring issue should be in details list.",
                jcv.getDetails(expiringAndNotYetValidSigner.getSignerCertPath()).contains(R("SHasExpiringCert")));
    }

    @Test
    public void testSingleEntryOneExpiredOneValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] oneExpiredOneValidSigner = { expiredSigner, alphaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByTwo", oneExpiredOneValidSigner));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("One entry signed by one expired cert and another valid cert, should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("One entry signed by one expired cert and another valid cert means two signers in the verifier.",
                2, jcv.getCertsList().size());
        Assert.assertTrue("One entry signed by one expired cert and another valid cert means two signers in the verifier.",
                jcv.getCertsList().contains(expiredSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

    @Test
    public void testManyEntriesOneExpiredOneValidSigner() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] oneExpiredOneValidSigner = { expiredSigner, alphaSigner };
        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByTwo", oneExpiredOneValidSigner));
        entries.add(new JarCertVerifierEntry("secondSignedByTwo", oneExpiredOneValidSigner));
        entries.add(new JarCertVerifierEntry("thirdSignedByTwo", oneExpiredOneValidSigner));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries signed by one expired cert and another valid cert, should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries signed by one expired cert and another valid cert means two signers in the verifier.",
                2, jcv.getCertsList().size());
        Assert.assertTrue("Three entries signed by one expired cert and another valid cert means two signers in the verifier.",
                jcv.getCertsList().contains(expiredSigner.getSignerCertPath())
                        && jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

    @Test
    public void testSomeExpiredEntries() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] oneExpiredOneValidSigners = { expiredSigner, alphaSigner };
        CodeSigner[] expiredSigners = { expiredSigner };

        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("firstSignedByTwo", oneExpiredOneValidSigners));
        entries.add(new JarCertVerifierEntry("secondSignedByTwo", oneExpiredOneValidSigners));
        entries.add(new JarCertVerifierEntry("thirdSignedByExpired", expiredSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Two entries signed by one expired and one valid cert, third signed by just expired cert, should be considered signed but not okay.",
                VerifyResult.SIGNED_NOT_OK, result);
        Assert.assertEquals("Two entries signed by one expired and one valid cert, third signed by just expired cert means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Two entries signed by one expired and one valid cert, third signed by just expired cert means one signer in the verifier.",
                jcv.getCertsList().contains(expiredSigner.getSignerCertPath()));
    }

    @Test
    public void testManyInvalidOneValidStillSignedOkay() throws Exception {
        JarCertVerifier jcv = new JarCertVerifier(null);
        CodeSigner[] oneExpiredOneValidSigners = { alphaSigner, expiredSigner };
        CodeSigner[] oneNotYetValidOneValidSigners = { alphaSigner, notYetValidSigner };
        CodeSigner[] oneExpiringSigners = { alphaSigner, expiringSigner };

        Vector<JarEntry> entries = new Vector<JarEntry>();
        entries.add(new JarCertVerifierEntry("META-INF/MANIFEST.MF"));
        entries.add(new JarCertVerifierEntry("firstSigned", oneExpiredOneValidSigners));
        entries.add(new JarCertVerifierEntry("secondSigned", oneNotYetValidOneValidSigners));
        entries.add(new JarCertVerifierEntry("thirdSigned", oneExpiringSigners));
        entries.add(new JarCertVerifierEntry("oneDir/"));
        entries.add(new JarCertVerifierEntry("oneDir/fourthSigned", oneExpiredOneValidSigners));
        VerifyResult result = jcv.verifyJarEntryCerts("", true, entries);

        Assert.assertEquals("Three entries sharing valid cert and others with issues, should be considered signed and okay.",
                VerifyResult.SIGNED_OK, result);
        Assert.assertEquals("Three entries sharing valid cert and others with issues means one signer in the verifier.",
                1, jcv.getCertsList().size());
        Assert.assertTrue("Three entries sharing valid cert and others with issues means one signer in the verifier.",
                jcv.getCertsList().contains(alphaSigner.getSignerCertPath()));
    }

}
