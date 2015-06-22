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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import net.sourceforge.jnlp.security.dialogs.remember.ExecuteAppletAction;
import net.sourceforge.jnlp.security.dialogs.remember.SavedRememberAction;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import org.junit.Test;

public class UnsignedAppletTrustConfirmationTest {

    private static final String surl1 = "http://codeba.se/app";
    private static final String url41 = "http://my.url/app/";
    private static final String url42 = "resource.jar";
    private static URL url;
    private static URL url4;
    private static InfrastructureFileDescriptor APPLET_TRUST_SETTINGS_USER_BACKUP;

    private static class DummyJnlpWithTitleAndUrls extends DummyJNLPFileWithJar {

        public DummyJnlpWithTitleAndUrls(URL u) throws MalformedURLException {
            super(u);
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
    
    
    @BeforeClass
    public static void backupAppTrsSets() {
        APPLET_TRUST_SETTINGS_USER_BACKUP = PathsAndFiles.APPLET_TRUST_SETTINGS_USER;
    }
    
    @After
    public  void restoreAppTrsSets() throws Exception {
        fakeAppTrsSets(APPLET_TRUST_SETTINGS_USER_BACKUP);
    }
    
    private static void fakeAppTrsSets(final File f) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        fakeAppTrsSets(new InfrastructureFileDescriptor() {

            @Override
            public String getFullPath() {
                return f.getAbsolutePath();
            }

        });
    }
    private static void fakeAppTrsSets(InfrastructureFileDescriptor fake) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = PathsAndFiles.class.getDeclaredField("APPLET_TRUST_SETTINGS_USER");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, fake);
    }


    @Test
    public void updateAppletActionTest1() throws Exception {
        File f = File.createTempFile("appletExtendedSecurity", "itwUnittest");
        f.deleteOnExit();
        fakeAppTrsSets(f);
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(url4),
                new SavedRememberAction(ExecuteAppletAction.ALWAYS, "YES"),
                Boolean.FALSE,
                UnsignedAppletTrustWarningPanel.class);
        String s = FileUtils.loadFileAsString(f);
        Assert.assertTrue(s.contains("UnsignedAppletTrustWarningPanel:A{YES}"));
        Assert.assertTrue(s.contains(url41+url42));
        Assert.assertTrue(s.contains(surl1));
        UnsignedAppletTrustConfirmation.updateAppletAction(
                new DummyJnlpWithTitleAndUrls(url4),
                new SavedRememberAction(ExecuteAppletAction.NEVER, "NO"),
                Boolean.TRUE,
                UnsignedAppletTrustWarningPanel.class);
        s = FileUtils.loadFileAsString(f);
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
}
