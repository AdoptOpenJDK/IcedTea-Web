/*   Copyright (C) 2014 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.UrlUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnsignedAppletTrustConfirmationTest {

    private static final String surl1 = "http://codeba.se/app";
    private static final String url41 = "http://my.url/app/";
    private static final String url42 = "resource.jar";
    private static URL url;
    private static URL url4;

    private static class DummyJnlpWithTitleAndUrls extends DummyJNLPFileWithJar {

        public DummyJnlpWithTitleAndUrls(URL u) throws MalformedURLException {
            super(url, u);
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
    
    @BeforeClass
    public static void initUrl() throws MalformedURLException {
        url=new URL(surl1);
        url4=new URL(url41+url42);
    }
   
   private static File backup;

    @BeforeClass
    public static void backupAppTrust() throws IOException{
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().createNewFile();
        backup = File.createTempFile("appletExtendedSecurity", "itwUnittest");
        backup.deleteOnExit();
        FirefoxProfilesOperator.copyFile(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile(), backup);
    }
    
    @AfterClass
    public static void restoreAppTrust() throws IOException{
        FirefoxProfilesOperator.copyFile(backup, PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
    }

    @Test
    public void updateAppletActionTest1() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(url4),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        Assert.assertTrue(s.contains("UnsignedAppletTrustWarningPanel:A{YES}"));
        Assert.assertTrue(s.contains(url41+url42));
        Assert.assertTrue(s.contains(surl1));
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(url4),
                new SavedRememberAction(ExecuteAppletAction.NEVER, "NO"),
                Boolean.TRUE,
                UnsignedAppletTrustWarningPanel.class);
        s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        Assert.assertTrue(s.contains("UnsignedAppletTrustWarningPanel:N{NO}"));
        Assert.assertFalse(s.contains(url41+url42));
        Assert.assertTrue(s.contains(surl1));        
    }


    @Test
    public void testToRelativePaths() throws Exception {
        /* Absolute -> Relative */
        assertEquals(Arrays.asList("test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example.com/test.jar"), "http://example.com/"));

        /* Relative is unchanged */
        assertEquals(Arrays.asList("test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("test.jar"), "http://example.com/"));

        /* Different root URL is unchanged */
        assertEquals(Arrays.asList("http://example2.com/test.jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example2.com/test.jar"), "http://example.com/"));

        /* Path with invalid URL characters is handled */
        assertEquals(Arrays.asList("test .jar"),
                UnsignedAppletTrustConfirmation.toRelativePaths(Arrays.asList("http://example.com/test .jar"), "http://example.com/"));
    }
    
    
    @Test
    public void testStripFile() throws Exception {
        String sample = "http://aa.bb/";
        String result = UrlUtils.stripFile(new URL(sample));
        assertEquals(sample, result);
        sample = "http://aa.bb";
        result = UrlUtils.stripFile(new URL(sample));
        assertEquals(sample + "/", result);
        sample = "http://aa.bb/";
        result = UrlUtils.stripFile(new URL(sample + "cc"));
        assertEquals(sample, result);
        sample = "http://aa.bb/cc/";
        result = UrlUtils.stripFile(new URL(sample));
        assertEquals(sample, result);
        sample = "http://aa.bb/some/complicated/";
        result = UrlUtils.stripFile(new URL(sample + "some"));
        assertEquals(sample, result);
        sample = "http://aa.bb/some/complicated/some/";
        result = UrlUtils.stripFile(new URL(sample));
        assertEquals(sample, result);
        sample = "http://aa.bb/some/";
        result = UrlUtils.stripFile(new URL(sample + "strange?a=b"));
        assertEquals(sample, result);
        sample = "http://aa.bb/some/strange/";
        result = UrlUtils.stripFile(new URL(sample + "?a=b"));
        assertEquals(sample, result);
        
    }
    
    
    private static URL urlX1;
    private static URL urlX2;
    private static URL urlX3;
    
    private static URL urlY1;
    private static URL urlY2;
    private static URL urlY3;
    private static URL urlY4;
    private static URL urlY5;
    private static URL urlY6;
    private static URL urlY7;
    private static URL urlY8;
    
    @BeforeClass
    public static void initUrlsX123() throws MalformedURLException, IOException {
        urlX1 = new URL("http://&#10;does&#32;not&#32;matter&#32;is&#32;ok");
        urlX2 = new URL("http://\ndoes not matter is harmful");
        Properties p = new Properties();
        p.load(new StringReader("key=http:\\u002F\\u002F\\u000Adoes\\u0020not\\u0020matter\\u0020is\\u0020harmful"));
        urlX3=new URL(p.getProperty("key"));
    }

    @BeforeClass
    public static void initUrlsY12345678() throws MalformedURLException, IOException {
        urlY1 = new URL("http://som\\EeUrl.cz/aa");
        urlY2 = new URL("http://some\\QUrl.cz/aa");
        urlY3 = new URL("http://so\\QmeU\\Erl.cz/aa");
        urlY4 = new URL("http://so\\EmeU\\Qrl.cz/aa");

        urlY5 = new URL("http://someUrl.cz/aa\\Ebb/cc");
        urlY6 = new URL("http://someUrl.cz/aa\\Qbb/cc");
        urlY7 = new URL("http://someUrl.cz/aa\\Qbb/cc/dd\\Eee");
        urlY8 = new URL("http://someUrl.cz/aa\\Ebb/cc/dd\\Qee");
    }

    @Test
    public void updateAppletActionTestYQN1234saveAndLoadFine() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY1),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY2),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY3),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY4),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        AppletStartupSecuritySettings securitySettings = AppletStartupSecuritySettings.getInstance();
        UnsignedAppletActionStorageImpl userActionStorage = (UnsignedAppletActionStorageImpl) securitySettings.getUnsignedAppletActionCustomStorage();
        List<UnsignedAppletActionEntry> ll = userActionStorage.getMatchingItems(null, null, null);
        Assert.assertEquals(4, ll.size());
    }

    @Test
    public void updateAppletActionTestYQN5678saveAndLoadFine() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY5),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY6),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY7),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrlsWithOverwrite(urlY8),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        AppletStartupSecuritySettings securitySettings = AppletStartupSecuritySettings.getInstance();
        UnsignedAppletActionStorageImpl userActionStorage = (UnsignedAppletActionStorageImpl) securitySettings.getUnsignedAppletActionCustomStorage();
        List<UnsignedAppletActionEntry> ll = userActionStorage.getMatchingItems(null, null, null);
        Assert.assertEquals(4, ll.size());
    }
    
    @Test
    public void updateAppletActionTestX3() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        try{
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(urlX3),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        //may throw  RuntimeException which is correct, however, wee need to check result
        } catch (Exception ex){
            ServerAccess.logException(ex);
        }
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        Assert.assertFalse(s.contains("harmful"));
    }
    
    @Test
    public void updateAppletActionTestX2() throws Exception {
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        try{
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(urlX2),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        //may throw  RuntimeException which is correct, however, wee need to check result
        } catch (Exception ex){
            ServerAccess.logException(ex);
        }
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        Assert.assertFalse(s.contains("harmful"));
    }
    
     @Test
    public void updateAppletActionTestX1() throws Exception {
        //this case is correct, if html encoded url is passed as URL from javaws, it is kept intact
        PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().delete(); //clean file to examine later
        Exception eex = null;
        try{
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(urlX1),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        //may throw  RuntimeException which is correct, however, wee need to check result
        } catch (Exception ex){
            eex = ex;
            ServerAccess.logException(ex);
        }
        String s = FileUtils.loadFileAsString(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        Assert.assertNull(eex);
        Assert.assertTrue(s.contains("http://&#10;does&#32;not&#32;matter&#32;is&#32;ok"));
    }

    private static class DummyJnlpWithTitleAndUrlsWithOverwrite extends DummyJnlpWithTitleAndUrls {
        private final URL u;

        public DummyJnlpWithTitleAndUrlsWithOverwrite(URL u) throws MalformedURLException {
            super(u);
            this.u  = u;
        }

        @Override
        public URL getCodeBase() {
            return u;
        }

            @Override
            public URL getSourceLocation() {
                return u;
            }
    }
}
