/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl;

import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UrlRegEx;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VersionRestrictionTest extends NoStdOutErrTest {

    private static File testFile;
    private static final SavedRememberAction sra = new SavedRememberAction(ExecuteAppletAction.ALWAYS, "NO");
    private static final AppletSecurityActions asa = AppletSecurityActions.fromAction(cN.class, sra);
    private static final UrlRegEx urx = UrlRegEx.quote("http://aa.bb/");
    private static final List<String> archs = Arrays.asList("res.jar");
    private static final UnsignedAppletActionEntry aq = new UnsignedAppletActionEntry(asa, new Date(1l), urx, urx, archs);

    private abstract static class cN implements RememberableDialog {
    };

    @Before
    public void prepareNewTestFile() throws IOException {
        testFile = File.createTempFile("itwAES", "testFile");
        testFile.deleteOnExit();
    }

    @After
    public void removeAllPossibleBackupFiles() throws IOException {
        File[] f = getBackupFiles();
        for (File file : f) {
            file.deleteOnExit();
        }
        for (File file : f) {
            file.delete();
        }
        checkBackupFile(false);
    }

    private File[] getBackupFiles() {
        File[] f = testFile.getParentFile().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testFile.getName() + "\\.[0123456789]+" + UnsignedAppletActionStorageImpl.BACKUP_SUFFIX);
            }
        });
        return f;
    }

    private void checkBackupFile(boolean created) throws IOException {
        checkBackupFile(created, 0);
    }

    private void checkBackupFile(boolean created, int expectedVersion) throws IOException {
        File[] f = getBackupFiles();
        if (!created) {
            Assert.assertEquals("no backup should exists", 0, f.length);
        } else {
            Assert.assertEquals("there should be exactly one backup", 1, f.length);
            Assert.assertTrue(f[0].getName().endsWith("." + expectedVersion + UnsignedAppletActionStorageImpl.BACKUP_SUFFIX));
            String s = FileUtils.loadFileAsString(f[0]);
            String l[] = s.split("\\n");
            int hc = 0;
            for (String string : l) {
                string = string.trim();
                if (string.startsWith(UnsignedAppletActionStorageImpl.versionPrefix)) {
                    hc++;
                    if (hc == 1) {
                        Assert.assertTrue("first header must contains warning", string.contains("!WARNING!"));
                    } else {
                        Assert.assertFalse("only first header can contains warning", string.contains("!WARNING!"));
                    }
                }
            }
            Assert.assertTrue("at least one header must be in backup", hc > 0);
        }
    }

    @Test
    public void numberFormatExceptionInOnInLoad1() throws IOException {
        ServerAccess.saveFile("#VERSION X\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 0);
    }

    @Test
    public void numberFormatExceptionInOnInLoad2() throws IOException {
        ServerAccess.saveFile("#VERSION\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 0);
    }

    @Test
    public void numberFormatExceptionInOnInLoad3() throws IOException {
        ServerAccess.saveFile("#VERSION \n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 0);
    }

    @Test
    public void numberFormatExceptionInOnInLoad4() throws IOException {
        ServerAccess.saveFile("#VERSION                \n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 0);
    }

    @Test
    public void correctLoad() throws IOException {
        ServerAccess.saveFile("#VERSION 2\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(2, i1.items.size());
        checkBackupFile(false);
    }

    @Test
    public void correctLoad2() throws IOException {
        ServerAccess.saveFile("#VERSION 2"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(2, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(3, i1.items.size());
        checkBackupFile(false);
    }

    @Test
    public void correctLoad3() throws IOException {
        ServerAccess.saveFile("\n"
                + "\n"
                + "#VERSION 2"
                + "\n"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(2, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(3, i1.items.size());
        checkBackupFile(false);
    }

    @Test
    public void firstVersionValidOnlyOK() throws IOException {
        ServerAccess.saveFile("\n"
                + "\n"
                + "#VERSION 2"
                + "\n"
                + "#VERSION 1"
                + "\n"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(2, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(3, i1.items.size());
        checkBackupFile(false);
    }

    @Test
    public void firstVersionValidOnlyBad() throws IOException {
        ServerAccess.saveFile("\n"
                + "\n"
                + "#VERSION 1"
                + "\n"
                + "#VERSION 2"
                + "\n"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 1);
    }

    @Test
    public void laterVersionIgnored() throws IOException {
        ServerAccess.saveFile("\n"
                + "\n"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "#VERSION 2\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true);
    }

    @Test
    public void incorrectLoad() throws IOException {
        ServerAccess.saveFile("#VERSION 1\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 1);
    }

    @Test
    public void incorrectLoad1() throws IOException {
        ServerAccess.saveFile("#VERSION2\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 0);
    }

    @Test
    public void incorrectLoad2() throws IOException {
        ServerAccess.saveFile("#VERSION 1"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true, 1);
    }

    @Test
    public void noVersionNoLoad() throws IOException {
        ServerAccess.saveFile("\n"
                + "cN:N{YES}; 1 \\Qhttp://some.url/\\E \\Qhttp://some.url/\\E jar.jar"
                + "\n"
                + "cN:N{YES}; 1 \\Qhttp://some2.url/\\E \\Qhttp://some2.url/\\E jar.jar", testFile);
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(testFile);
        i1.readContents();
        Assert.assertEquals(0, i1.items.size());
        i1.add(aq);
        i1.readContents();
        Assert.assertEquals(1, i1.items.size());
        checkBackupFile(true);
    }
}
