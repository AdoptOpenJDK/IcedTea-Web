package net.sourceforge.jnlp.signing;

import net.sourceforge.jnlp.tools.CertInformation;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JarSigningHolderTest {

    @Test(expected = NullPointerException.class)
    public void testFailOnNullRessource() {

         new JarSigningHolder(null, p -> new CertInformation());
    }

    @Test(expected = NullPointerException.class)
    public void testFailOnNullCertificate() {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("unsigned.jar").getFile();

        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        holder.getState(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFailOnNullCertInformationProvider() {

        final String jarUrl = JarSigningHolderTest.class.getResource("unsigned.jar").getFile();
        new JarSigningHolder(jarUrl, null);
    }

    @Test
    public void testUnsignedJarHasNoCertificates() {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("unsigned.jar").getFile();

        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        assertTrue(holder.getCertificates().isEmpty());
    }

    @Test
    public void testUnsignedJarIsNotSignedByCertificate() throws Exception {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("unsigned.jar").getFile();
        final Certificate certificate = generateTestCertificate();



        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        assertEquals(SigningState.NONE, holder.getState(certificate));
    }

    @Test
    public void testSignedJarHasCertificates() {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("signed.jar").getFile();

        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        assertFalse(holder.getCertificatePaths().isEmpty());
    }

    @Test
    public void testSignedJarIsNotSignedByAnyWrongCertificate() throws Exception {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("signed.jar").getFile();
        final Certificate certificate = generateTestCertificate();



        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        assertEquals(SigningState.NONE, holder.getState(certificate));
    }

    @Test
    public void testSignedJarIsSignedByCertificatePath() {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("signed.jar").getFile();

        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        final CertPath certPath = holder.getCertificatePaths().iterator().next();
        assertEquals(SigningState.FULL, holder.getStateForPath(certPath));
    }

    @Test
    public void testSignedJarIsSignedByCertificate() {

        //given
        final String jarUrl = JarSigningHolderTest.class.getResource("signed.jar").getFile();

        //when
        final JarSigningHolder holder = new JarSigningHolder(jarUrl, p -> new CertInformation());

        //than
        final Set<? extends Certificate> certificates = holder.getCertificatePaths().stream().flatMap(certPath -> certPath.getCertificates().stream()).collect(Collectors.toSet());
        assertFalse(certificates.isEmpty());
        certificates.forEach(c -> assertEquals(SigningState.FULL, holder.getState(c)));
    }

    private Certificate generateTestCertificate() throws Exception {
        /*
        CertAndKeyGen keyGen=new CertAndKeyGen("RSA","SHA1WithRSA",null);
        keyGen.generate(1024);
        return keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long)365*24*3600);
        */

        //Alternative version... (written as PEM)

        //Source: http://www.javased.com/index.php?source_dir=spring-security-oauth/spring-security-oauth/src/test/java/org/springframework/security/oauth/common/signature/TestRSA_SHA1SignatureMethod.java
        final String googleOAuthCert="-----BEGIN CERTIFICATE-----\n" +
                "MIIDBDCCAm2gAwIBAgIJAK8dGINfkSTHMA0GCSqGSIb3DQEBBQUAMGAxCzAJBgNV\n"+
                "BAYTAlVTMQswCQYDVQQIEwJDQTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzETMBEG\n"+
                "A1UEChMKR29vZ2xlIEluYzEXMBUGA1UEAxMOd3d3Lmdvb2dsZS5jb20wHhcNMDgx\n"+
                "MDA4MDEwODMyWhcNMDkxMDA4MDEwODMyWjBgMQswCQYDVQQGEwJVUzELMAkGA1UE\n"+
                "CBMCQ0ExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxEzARBgNVBAoTCkdvb2dsZSBJ\n"+
                "bmMxFzAVBgNVBAMTDnd3dy5nb29nbGUuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GN\n"+
                "ADCBiQKBgQDQUV7ukIfIixbokHONGMW9+ed0E9X4m99I8upPQp3iAtqIvWs7XCbA\n"+
                "bGqzQH1qX9Y00hrQ5RRQj8OI3tRiQs/KfzGWOdvLpIk5oXpdT58tg4FlYh5fbhIo\n"+
                "VoVn4GvtSjKmJFsoM8NRtEJHL1aWd++dXzkQjEsNcBXwQvfDb0YnbQIDAQABo4HF\n"+
                "MIHCMB0GA1UdDgQWBBSm/h1pNY91bNfW08ac9riYzs3cxzCBkgYDVR0jBIGKMIGH\n"+
                "gBSm/h1pNY91bNfW08ac9riYzs3cx6FkpGIwYDELMAkGA1UEBhMCVVMxCzAJBgNV\n"+
                "BAgTAkNBMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRMwEQYDVQQKEwpHb29nbGUg\n"+
                "SW5jMRcwFQYDVQQDEw53d3cuZ29vZ2xlLmNvbYIJAK8dGINfkSTHMAwGA1UdEwQF\n"+
                "MAMBAf8wDQYJKoZIhvcNAQEFBQADgYEAYpHTr3vQNsHHHUm4MkYcDB20a5KvcFoX\n"+
                "gCcYtmdyd8rh/FKeZm2me7eQCXgBfJqQ4dvVLJ4LgIQiU3R5ZDe0WbW7rJ3M9ADQ\n"+
                "FyQoRJP8OIMYW3BoMi0Z4E730KSLRh6kfLq4rK6vw7lkH9oynaHHWZSJLDAp17cP\n"+
                "j+6znWkN9/g=\n"+
                "-----END CERTIFICATE-----";
        return CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(googleOAuthCert.getBytes(StandardCharsets.UTF_8)));

    }

}