/* 
 Copyright (C) 2010 Red Hat, Inc.

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.sourceforge.jnlp.security.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.security.dialogresults.BasicDialogValue;
import net.sourceforge.jnlp.security.dialogresults.NamePassword;
import net.sourceforge.jnlp.security.dialogresults.YesNo;
import net.sourceforge.jnlp.security.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SecurityDialogsTest {

    private static boolean wasHeadless;
    private static boolean wasTrustAll;
    private static boolean wasTrustNone;
    private static String prompt;
    private static URL url;
    private static File appletSecurityBackup;
    private static String seclevel;
    private static final String urlstr1 = "http://must.not.be.in/";
    private static final String urlstr2 = ".appletSecurity";
    private static final String urlstr = urlstr1 + urlstr2;

    private JNLPFile crtJnlpF() throws MalformedURLException {
        return new DummyJnlpWithTitleAndUrls();
    }

    private static class DummyJnlpWithTitleAndUrls extends DummyJNLPFileWithJar {

        public DummyJnlpWithTitleAndUrls() throws MalformedURLException {
            super(new File("/some/path/blah.jar"));
        }

        @Override
        public InformationDesc getInformation() {
            return new InformationDesc(null, false) {

                @Override
                public String getTitle() {
                    return "Demo App";
                }

            };
        }

        @Override
        public URL getCodeBase() {
            return url;
        }

        @Override
        public URL getSourceLocation() {
            return url;
        }

    };

    private static class ExpectedResults {

        public static ExpectedResults PositiveResults = new ExpectedResults(BasicDialogValue.Primitive.YES, YesNo.yes(), null, true);
        public static ExpectedResults NegativeResults = new ExpectedResults(BasicDialogValue.Primitive.NO, YesNo.no(), null, false);
        public final BasicDialogValue.Primitive p;
        public final YesNo ea;
        public final NamePassword np;
        public final boolean b;

        public ExpectedResults(BasicDialogValue.Primitive p, YesNo ea, NamePassword np, boolean b) {
            this.p = p;
            this.ea = ea;
            this.np = np;
            this.b = b;
        }

    }

    @BeforeClass
    public static void initUrl() throws MalformedURLException {
        url = new URL(urlstr);
    }

    @BeforeClass
    public static void backupAppletSecurity() throws IOException {
        if (PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().exists()) {
            appletSecurityBackup = File.createTempFile("appletSecurity", "itwTestBackup");
            FirefoxProfilesOperator.copyFile(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), appletSecurityBackup);
        }
    }

    @Before
    public void removeAppletSecurity() throws IOException {
        removeAppletSecurityImpl();
    }

    public static void removeAppletSecurityImpl() throws IOException {
        if (appletSecurityBackup != null && appletSecurityBackup.exists()) {
            PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete();
        }
    }

    @AfterClass
    public static void restoreAppletSecurity() throws IOException {
        if (appletSecurityBackup != null && appletSecurityBackup.exists()) {
            removeAppletSecurityImpl();
            FirefoxProfilesOperator.copyFile(appletSecurityBackup, PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
            appletSecurityBackup.delete();
        }
    }

    @BeforeClass
    public static void saveJnlpRuntime() {
        wasHeadless = JNLPRuntime.isHeadless();
        wasTrustAll = JNLPRuntime.isTrustAll();
        //trutNone is not used in dialogues, its considered as default
        //but is ussed in Unsigned... dialogs family
        wasTrustNone = JNLPRuntime.isTrustNone();
        prompt = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER);
        seclevel = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL);
    }

    @After
    public void restoreJnlpRuntime() throws Exception {
        restoreJnlpRuntimeFinally();
    }

    private static void setPrompt(String p) {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER, p);
    }

    private static void setPrompt(boolean p) {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER, String.valueOf(p));
    }

    private static void setAS(AppletSecurityLevel as) {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, String.valueOf(as.toChars()));
    }

    @AfterClass
    public static void restoreJnlpRuntimeFinally() throws Exception {
        JNLPRuntime.setHeadless(wasHeadless);
        JNLPRuntime.setTrustAll(wasTrustAll);
        JNLPRuntime.setTrustNone(wasTrustNone);
        setPrompt(prompt);
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, seclevel);
    }

    @Test(timeout = 1000)//if gui pops up
    public void testDialogsHeadlessTrustAllPrompt() throws Exception {
        JNLPRuntime.setHeadless(true);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setTrustNone(false); //ignored
        setPrompt(true); //should not metter becasue is headless
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        testAllDialogs(ExpectedResults.PositiveResults);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.DENY_ALL);
        checkUnsignedActing(false, true);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        checkUnsignedActing(false, true);
    }

    @Test(timeout = 1000)//if gui pops up
    public void testDialogsHeadlessTrustNonePrompt() throws Exception {
        JNLPRuntime.setHeadless(true);
        JNLPRuntime.setTrustAll(false);
        JNLPRuntime.setTrustNone(false); //used by Unsigne
        setPrompt(true); //should not metter becasue is headless
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        testAllDialogs(ExpectedResults.NegativeResults);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedActing(false);
        setAS(AppletSecurityLevel.DENY_ALL);
        checkUnsignedActing(false);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        checkUnsignedActing(false);
    }

    @Test(timeout = 1000)//if gui pops up
    public void testDialogsNotHeadlessTrustAllDontPrompt() throws Exception {
        JNLPRuntime.setHeadless(false); //should not metter as is nto asking
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setTrustNone(false); //ignored
        setPrompt(false);
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        testAllDialogs(ExpectedResults.PositiveResults);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.DENY_ALL);
        checkUnsignedActing(false, true);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        checkUnsignedActing(false, true);
    }

    @Test(timeout = 1000)//if gui pops up
    public void testDialogsNotHeadlessTrustNoneDontPrompt() throws Exception {
        JNLPRuntime.setHeadless(false); //should not metter as is nto asking
        JNLPRuntime.setTrustAll(false);
        JNLPRuntime.setTrustNone(false); //ignored
        setPrompt(false);
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        testAllDialogs(ExpectedResults.NegativeResults);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedActing(false);
        setAS(AppletSecurityLevel.DENY_ALL);
        checkUnsignedActing(false);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        checkUnsignedActing(false);
    }

    private void testAllDialogs(ExpectedResults r) throws MalformedURLException {
        //anything but  shoertcut
        AccessWarningPaneComplexReturn r1 = SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.PRINTER, null, null);
        Assert.assertEquals(r.p, r1.getRegularReturn().getValue());
        //shortcut
        AccessWarningPaneComplexReturn r2 = SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.CREATE_DESTKOP_SHORTCUT, null, null);
        Assert.assertEquals(r.p, r2.getRegularReturn().getValue());
        YesNo r3 = SecurityDialogs.showUnsignedWarningDialog(null);
        Assert.assertEquals(r.ea, r3);
        YesNoSandbox r4 = SecurityDialogs.showCertWarningDialog(SecurityDialogs.AccessType.UNVERIFIED, null, null, null);
        Assert.assertEquals(r.p, r4.getValue());
        YesNo r5 = SecurityDialogs.showPartiallySignedWarningDialog(null, null, null);
        Assert.assertEquals(r.ea, r5);
        NamePassword r6 = SecurityDialogs.showAuthenicationPrompt(null, 123456, null, null);
        Assert.assertEquals(r.np, r6);
        boolean r7 = SecurityDialogs.showMissingALACAttributePanel(null, null, null);
        Assert.assertEquals(r.b, r7);
        boolean r8 = SecurityDialogs.showMatchingALACAttributePanel(crtJnlpF(), url, new HashSet<URL>());
        Assert.assertEquals(r.b, r8);
        boolean r9 = SecurityDialogs.showMissingPermissionsAttributeDialogue(null);
        Assert.assertEquals(r.b, r9);
    }

    private void checkUnsignedActing(boolean b) throws MalformedURLException {
        checkUnsignedActing(b, b);
    }

    /*
     *  testPartiallySignedBehaviour(); needs security delegate to set sandbox, so somtetimes results are strange
     */
    private void checkUnsignedActing(boolean b1, boolean b2) throws MalformedURLException {
        boolean r10 = testUnsignedBehaviour();
        Assert.assertEquals(b1, r10);
        boolean r11 = testPartiallySignedBehaviour();
        Assert.assertEquals(b2, r11);
    }

    private boolean testUnsignedBehaviour() throws MalformedURLException {
        try {
            UnsignedAppletTrustConfirmation.checkUnsignedWithUserIfRequired(crtJnlpF());
            return true;
        } catch (LaunchException ex) {
            return false;
        }
    }

    private boolean testPartiallySignedBehaviour() throws MalformedURLException {
        try {
            UnsignedAppletTrustConfirmation.checkPartiallySignedWithUserIfRequired(null, crtJnlpF(), null);
            return true;
        } catch (LaunchException ex) {
            return false;
        }
    }
    //SPOILER ALERT
    //all test below, are executing gui mode
    //however, they should never popup becasue jnlpruntime should never be initialized in this test
    //so posting to non existing queue leads to  NPE
    //if this logic will ever be  changed, the testswill need fixing
    //msot terrible thing which may happen is, that gui will be really shown
    //then each test must check if it have X, if odnt, pass with message "nothing tested|
    //if it have X, it have to show gui, terminate itself, and then verify that gui was really running

    private void countNPES() throws MalformedURLException {
        countNPES(0);
    }

    private void countNPES(int allowedRuns) throws MalformedURLException {
        int npecounter = 0;
        int metcounter = 0;
        try {
            metcounter++;
            //anything but  shoertcut
            SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.PRINTER, crtJnlpF(), null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            //shortcut
            SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.CREATE_DESTKOP_SHORTCUT, crtJnlpF(), null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showUnsignedWarningDialog(crtJnlpF());
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showCertWarningDialog(SecurityDialogs.AccessType.UNVERIFIED, crtJnlpF(), null, null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showPartiallySignedWarningDialog(crtJnlpF(), null, null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showAuthenicationPrompt(null, 123456, null, null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showMissingALACAttributePanel(crtJnlpF(), null, null);
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showMatchingALACAttributePanel(crtJnlpF(), url, new HashSet<URL>());
        } catch (NullPointerException ex) {
            npecounter++;
        }
        try {
            metcounter++;
            SecurityDialogs.showMissingPermissionsAttributeDialogue(crtJnlpF());
        } catch (NullPointerException ex) {
            npecounter++;
        }
        Assert.assertEquals(metcounter, npecounter + allowedRuns);
    }

    private void checkUnsignedNPE(boolean b) throws MalformedURLException {
        checkUnsignedNPE(b, b);
    }

    /*
     testPartiallySignedBehaviour(); needs security delegate to set sandbox, so somtetimes results are strange
     */
    private void checkUnsignedNPE(boolean b1, boolean b2) throws MalformedURLException {
        int metcounter = 0;
        boolean ex1 = false;
        boolean ex2 = false;
        try {
            metcounter++;
            testPartiallySignedBehaviour();
        } catch (NullPointerException ex) {
            ex1 = true;
        }
        try {
            metcounter++;
            testUnsignedBehaviour();
        } catch (NullPointerException ex) {
            ex2 = true;
        }
        Assert.assertEquals(2, metcounter);
        Assert.assertEquals(b1, ex1);
        Assert.assertEquals(b2, ex2);
    }

    @Test
    public void testDialogsNotHeadlessTrustNonePrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(false);//should notmetter
        JNLPRuntime.setTrustNone(false); //ignored
        setPrompt(true);
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        countNPES();
        checkUnsignedNPE(false);
    }

    @Test
    public void testNormaDialogsNotHeadlessTrustAllPrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setTrustNone(false);
        setPrompt(true);
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        countNPES();
    }

    @Test
    public void testUnsignedDialogsNotHeadlessTrustAllPrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setTrustNone(false);
        setPrompt(true); //ignored
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.DENY_ALL);
        checkUnsignedActing(false, true);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        checkUnsignedActing(false, true);
    }

    @Test
    public void testUnsignedDialogsNotHeadlessTrustNonePrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(false);
        JNLPRuntime.setTrustNone(true);
        setPrompt(true); //ignored
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        boolean r10 = testUnsignedBehaviour();
        Assert.assertEquals(false, r10);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        boolean r11 = testUnsignedBehaviour();
        Assert.assertEquals(false, r11);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.DENY_ALL);
        boolean r12 = testUnsignedBehaviour();
        Assert.assertEquals(false, r12);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        boolean r13 = testUnsignedBehaviour();
        Assert.assertEquals(false, r13);
        checkUnsignedNPE(true, false);
    }

    @Test
    public void testUnsignedDialogsNotHeadlessTrustNoneTrustAllPrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(true);
        JNLPRuntime.setTrustNone(true);
        setPrompt(true); //ignored
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        boolean a = testUnsignedBehaviour();
        Assert.assertFalse(a);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        boolean r10 = testUnsignedBehaviour();
        Assert.assertEquals(false, r10);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.DENY_ALL);
        boolean r11 = testUnsignedBehaviour();
        Assert.assertEquals(false, r11);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        boolean r12 = testUnsignedBehaviour();
        Assert.assertEquals(false, r12);
        checkUnsignedNPE(true, false);
    }

    @Test
    public void testUnsignedDialogsNotHeadlessPrompt() throws Exception {
        JNLPRuntime.setHeadless(false);
        JNLPRuntime.setTrustAll(false);
        JNLPRuntime.setTrustNone(false);
        setPrompt(true); //ignored
        setAS(AppletSecurityLevel.ALLOW_UNSIGNED);
        checkUnsignedActing(true);
        setAS(AppletSecurityLevel.ASK_UNSIGNED);
        checkUnsignedNPE(true, true);
        setAS(AppletSecurityLevel.DENY_ALL);
        boolean r11 = testUnsignedBehaviour();
        Assert.assertEquals(false, r11);
        checkUnsignedNPE(true, false);
        setAS(AppletSecurityLevel.DENY_UNSIGNED);
        boolean r12 = testUnsignedBehaviour();
        Assert.assertEquals(false, r12);
        checkUnsignedNPE(true, false);
    }

    //ending/mising spaces are important separators!
    //if new rememberable interface implementation is added, then tests using this sentence should start to fail, 
    //and so this sentence should be updated for it
    private static final String appletSecurityContent
            = "MissingALACAttributePanel:A{YES};"
            + "MatchingALACAttributePanel:A{YES};"
            + "UnsignedAppletTrustWarningPanel:A{YES};"
            + "AccessWarningPane:A{YES};"
            + "MissingPermissionsAttributePanel:A{YES};"
            + "PartiallySignedAppTrustWarningPanel:A{YES}; "
            + "1434098834574 "
            + ".* \\Q" + urlstr + "\\E ";

    private void runRememeberableClasses(ExpectedResults r) throws MalformedURLException {
        boolean r7 = SecurityDialogs.showMissingALACAttributePanel(crtJnlpF(), null, new HashSet<URL>());
        Assert.assertEquals(r.b, r7);
        boolean r8 = SecurityDialogs.showMatchingALACAttributePanel(crtJnlpF(), url, new HashSet<URL>());
        Assert.assertEquals(r.b, r8);
        boolean r9 = testUnsignedBehaviour();
        Assert.assertEquals(r.b, r9);
        //skiping this one, ahrd to mock certVerifier
        // boolean r5 = testPartiallySignedBehaviour();
        //Assert.assertEquals(r.b, r5);
        boolean r6 = SecurityDialogs.showMissingPermissionsAttributeDialogue(crtJnlpF());
        Assert.assertEquals(r.b, r6);
        AccessWarningPaneComplexReturn r1 = SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.PRINTER, crtJnlpF(), null);
        Assert.assertEquals(r.p, r1.getRegularReturn().getValue());
        AccessWarningPaneComplexReturn r2 = SecurityDialogs.showAccessWarningDialog(SecurityDialogs.AccessType.CREATE_DESTKOP_SHORTCUT, crtJnlpF(), null);
        Assert.assertEquals(r.p, r2.getRegularReturn().getValue());

    }

    @Test
    public void testRemeberBehaviour() throws Exception {
        File f = PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile();
        try {
            JNLPRuntime.setHeadless(false);
            JNLPRuntime.setTrustAll(false);
            JNLPRuntime.setTrustNone(false);
            setPrompt(true); //ignored
            setAS(AppletSecurityLevel.ASK_UNSIGNED);
            /*Everything is on default, which means ask always everywhere*/
            countNPES();
            checkUnsignedNPE(true);
            //no we fake queue
            fakeQueue();
            //file exists our 6 rememberable dialogues should pass
            FileUtils.saveFile(appletSecurityContent, f);
            runRememeberableClasses(ExpectedResults.PositiveResults);
            FileUtils.saveFile(appletSecurityContent.replace("{YES}", "{NO}"), f);
            runRememeberableClasses(ExpectedResults.NegativeResults);
        } finally {
            resetQueue();
            f.delete();
        }

    }

    private void fakeQueue() throws Exception {
        Field field = JNLPRuntime.class.getDeclaredField("securityDialogMessageHandler");
        field.setAccessible(true);
        SecurityDialogMessageHandler sd = new SecurityDialogMessageHandler() {
            private SecurityDialogMessage currentMessage;

            @Override
            protected void handleMessage(SecurityDialogMessage message) {
                this.currentMessage = message;
                super.handleMessage(message); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        super.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        unlockMessagesClient(currentMessage);
                    }
                }
            }

        };
        field.set(null, sd);
        Thread t = new Thread(sd);
        t.setDaemon(true);
        t.start();
    }

    private void resetQueue() throws Exception {
        Field field = JNLPRuntime.class.getDeclaredField("securityDialogMessageHandler");
        field.setAccessible(true);
        field.set(null, null);
    }

}
