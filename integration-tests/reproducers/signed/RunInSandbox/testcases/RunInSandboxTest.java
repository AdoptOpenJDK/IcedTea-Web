/* RunInSandboxTest.java
 Copyright (C) 2014 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received mainCert copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making mainCert combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As mainCert special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is mainCert module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.PathsAndFiles;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RunInSandboxTest extends BrowserTest {

    private final List<String> TRUSTALL = Collections.unmodifiableList(Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.TRUSTALL.option}));
    private final List<String> TRUSTNONE = Collections.unmodifiableList(Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.TRUSTNONE.option}));
    private final List<String> TRUSTALLHTML = Collections.unmodifiableList(Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.TRUSTALL.option, OptionsDefinitions.OPTIONS.HTML.option}));
    private final List<String> TRUSTNONEHTML = Collections.unmodifiableList(Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.TRUSTNONE.option, OptionsDefinitions.OPTIONS.HTML.option}));
    private final List<String> HEADLESS = Collections.unmodifiableList(Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}));
    private static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    private static final String ItwAlias = "icedteaweb_signed";
    private static final char[] kpass = "changeit".toCharArray();
    private static Certificate mainCert;

    @BeforeClass
    public static void exportCertificate() throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        if (PathsAndFiles.USER_CERTS.getFile().exists()) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathsAndFiles.USER_CERTS.getFile()), null);
            mainCert = ks.getCertificate(ItwAlias);
        }
    }
    
    
    public static void deleteCertificate() throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        if (mainCert != null) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathsAndFiles.USER_CERTS.getFile()), null);
            Certificate isThere = ks.getCertificate(ItwAlias);
            if (isThere != null) {
                ks.deleteEntry(ItwAlias);
                ks.store(new FileOutputStream(PathsAndFiles.USER_CERTS.getFile()), kpass);
            }
        }
    }


    @AfterClass
    public static void restoreCertificate() throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        if (mainCert != null) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathsAndFiles.USER_CERTS.getFile()), null);
            ks.setCertificateEntry(ItwAlias, mainCert);
            ks.store(new FileOutputStream(PathsAndFiles.USER_CERTS.getFile()), kpass);
        }
    }

    
    //those tests must have NO certificate .. ItwAlias ... in keystore
    @Test
    public void testTrustAllJnlpAppletLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTALL, "RunInSandboxApplet.jnlp");
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    @Test
    public void testTrustNoneJnlpAppletLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTNONE, "RunInSandboxApplet.jnlp");
        assertNotReadProperty(pr);
        assertAccessControlException(pr);
        assertProperClose(pr);
    }

    @Test
    public void testTrustAllStandardJnlpApplicationLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTALL, "RunInSandboxApplication.jnlp");
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    @Test
    public void testTrustNoneJnlpApplicationLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTNONE, "RunInSandboxApplication.jnlp");
        assertNotReadProperty(pr);
        assertAccessControlException(pr);
        assertProperClose(pr);
    }
    
    @Test
    public void testTrustAlltHtmlJavawsLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTALLHTML, "RunInSandbox.html", new AutoOkClosingListener(), new AutoErrorClosingListener(), null);
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    

    @Test
    public void testTrustNoneHtmlJavawsLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTNONEHTML, "RunInSandbox.html", new AutoOkClosingListener(), new AutoErrorClosingListener(), null);
        assertNotReadProperty(pr);
        assertAccessControlException(pr);
        assertProperClose(pr);
    }
    
    
     @Test
    public void testTrustAlltHtmlHrefJavawsLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTALLHTML, "RunInSandboxJnlpHref.html", new AutoOkClosingListener(), new AutoErrorClosingListener(), null);
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    

    @Test
    public void testTrustNoneHtmlHrefJavawsLaunch() throws Exception {
        deleteCertificate();
        ProcessResult pr = server.executeJavawsHeadless(TRUSTNONEHTML, "RunInSandboxJnlpHref.html", new AutoOkClosingListener(), new AutoErrorClosingListener(), null);
        assertNotReadProperty(pr);
        assertAccessControlException(pr);
        assertProperClose(pr);
    }
    
    
    ///end of must NOT be certificate
    
    //those MUST have certificate in sotre (see different result)
     @Test
    public void testHaveCErtJnlpAppletLaunch() throws Exception {
        restoreCertificate();
        ProcessResult pr = server.executeJavawsHeadless("RunInSandboxApplet.jnlp");
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    

    @Test
    public void testHaveCertStandardJnlpApplicationLaunch() throws Exception {
        restoreCertificate();
        ProcessResult pr = server.executeJavawsHeadless("RunInSandboxApplication.jnlp");
        assertReadProperty(pr);
        assertProperClose(pr);
    }
    
     @Test
     @TestInBrowsers(testIn = Browsers.all)
    public void testHaveCertHtmlAppletLaunch() throws Exception {
        restoreCertificate();
        ProcessResult pr = server.executeBrowser("RunInSandbox.html", ServerAccess.AutoClose.CLOSE_ON_BOTH);
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    

    @Test
    @TestInBrowsers(testIn = Browsers.all)
    public void testHaveCertStandardHtmlHrefApplicationLaunch() throws Exception {
        restoreCertificate();
        ProcessResult pr = server.executeBrowser("RunInSandboxJnlpHref.html", ServerAccess.AutoClose.CLOSE_ON_BOTH);
        assertReadProperty(pr);
        assertProperClose(pr);
    }

    
    
    //end of must HAVE cert
    
    //and those must NOT have cert, and are trying to answer dialogues
    
    @Test
    public void testStandardJnlpApplicationLaunchWithAnswerYes() throws Exception {
        deleteCertificate();
        ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), HEADLESS, server.getUrl("RunInSandboxApplication.jnlp"));
        pw.setWriter("YES\n");
        ProcessResult pr = pw.execute();
        assertReadProperty(pr);
        assertProperClose(pr);
    }
    
    @Test
    public void testStandardJnlpApplicationLaunchWithAnswerSandbox() throws Exception {
        deleteCertificate();
        ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), HEADLESS, server.getUrl("RunInSandboxApplication.jnlp"));
        pw.setWriter("SANDBOX\n");
        ProcessResult pr = pw.execute();
        assertNotReadProperty(pr);
        assertAccessControlException(pr);
        assertProperClose(pr);
    }
    
    @Test
    public void testStandardJnlpApplicationLaunchWithAnswerNo() throws Exception {
        deleteCertificate();
        ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), HEADLESS, server.getUrl("RunInSandboxApplication.jnlp"));
        pw.setWriter("NO\n");
        ProcessResult pr = pw.execute();
        assertNotReadProperty(pr);
        assertNotAccessControlException(pr);
        assertNotProperClose(pr);
        assertLaunchException(pr);
    }
    
      @Test
    public void testStandardJnlpApplicationLaunchWithAnswerEOF() throws Exception {
        deleteCertificate();
        ProcessWrapper pw =  new ProcessWrapper(server.getJavawsLocation(), HEADLESS, server.getUrl("RunInSandboxApplication.jnlp"));
        pw.setWriter("");
        ProcessResult pr = pw.execute();
        assertNotReadProperty(pr);
        assertNotAccessControlException(pr);
        assertNotProperClose(pr);
        assertLaunchException(pr);
    }
    
    //end of tests

    private void assertProperClose(ProcessResult pr) {
        assertTrue("applet should have closed correctly", pr.stdout.contains(appletCloseString));
    }
    
    private void assertNotProperClose(ProcessResult pr) {
        assertFalse("applet must not have closed correctly", pr.stdout.contains(appletCloseString));
    }

    private void assertReadProperty(ProcessResult pr) {
        assertTrue("applet should have been able to read user.home", pr.stdout.contains(System.getProperty("user.home")));
    }

    private void assertNotReadProperty(ProcessResult pr) {
        assertFalse("applet should NOT been able to read user.home", pr.stdout.contains(System.getProperty("user.home")));
    }

    private void assertAccessControlException(ProcessResult pr) {
        String ace = "java.security.AccessControlException: access denied (\"java.util.PropertyPermission\" \"user.home\" \"read\")";
        assertTrue("applet should have throw AccessControlException", pr.stdout.contains(ace));
    }
    
    private void assertNotAccessControlException(ProcessResult pr) {
        String ace = "java.security.AccessControlException: access denied (\"java.util.PropertyPermission\" \"user.home\" \"read\")";
        assertFalse("applet must not throw AccessControlException", pr.stdout.contains(ace));
    }
     
    private void assertLaunchException(ProcessResult pr) {
        String ace = "LaunchException";
        assertTrue("applet should have throw LaunchException", pr.stderr.contains(ace));
    }

}
