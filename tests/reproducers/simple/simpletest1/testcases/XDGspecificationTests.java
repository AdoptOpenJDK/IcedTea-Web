/* 
 Copyright (C) 2011 Red Hat, Inc.

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.browsertesting.browsers.firefox.FirefoxProfilesOperator;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import org.junit.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@Bug(id = "RH947647")
public class XDGspecificationTests extends BrowserTest {

    private static File backupMainDir;

    private static class Backup {

        public final File from;
        public final File to;

        public Backup(File from, File to) {
            this.from = from;
            this.to = to;
        }
    }
    //intentionaly not using constants from itw to check itw
    private static final File oldRoot = new File(System.getProperty("user.home"), ".icedtea");
    private static final File realCache;
    private static final File realConfig;
    private static final File homeCache = new File(System.getProperty("user.home") + File.separator + ".cache" + File.separator + "icedtea-web");
    private static final File homeConfig = new File(System.getProperty("user.home") + File.separator + ".config" + File.separator + "icedtea-web");
    private static List<Backup> hollyBackup;

    static {
        String configHome = System.getProperty("user.home") + File.separator + ".config";
        String cacheHome = System.getProperty("user.home") + File.separator + ".cache";

        String XDG_CONFIG_HOME_value = System.getenv(PathsAndFiles.XDG_CONFIG_HOME_VAR);
        String XDG_CACHE_HOME_value = System.getenv(PathsAndFiles.XDG_CACHE_HOME_VAR);
        if (XDG_CONFIG_HOME_value != null) {
            configHome = XDG_CONFIG_HOME_value;
        }
        if (XDG_CACHE_HOME_value != null) {
            cacheHome = XDG_CACHE_HOME_value;
        }
        realConfig = new File(configHome + File.separator + "icedtea-web");
        realCache = new File(cacheHome + File.separator + "icedtea-web");
    }

    // When current root is backuped,
    // also new files, which legacy impl is not aware about
    // cold be copied. Remove them in fake root
    // if they are not, then firstrun cleanup fails, and so second prints unexpected warnings
    public static void removeUnsupportedLegacyFiles() {
        File gjp = new File(oldRoot, "generated_jnlps");
        File icons = new File(oldRoot, "icons");
        if (gjp.exists()) {
            deleteRecursively(gjp);
        }
        if (icons.exists()) {
            deleteRecursively(icons);
        }
        Assert.assertFalse(gjp.exists());
        Assert.assertFalse(icons.exists());
    }

    @BeforeClass
    public static void backup() throws IOException {
        File base = tmpDir();
        backupMainDir = base;
        hollyBackup = backupRealSettingsAndClear(base);
    }

    @AfterClass
    public static void restore() throws IOException {
        cleanRealSettings();
        restoreSettings(hollyBackup);
        deleteRecursively(backupMainDir);
    }

    private static void mv(File oldRoot, File base, List<Backup> l) {
        if (oldRoot.exists()) {
            ServerAccess.logOutputReprint("moving of " + oldRoot + " to " + base);
            File dest = new File(base, oldRoot.getName());
            boolean a = oldRoot.renameTo(dest);
            if (!a) {
                ServerAccess.logErrorReprint("moving of " + oldRoot + " to " + base + " failed");
            } else {
                ServerAccess.logOutputReprint("sucess");
            }
            if (l != null) {
                l.add(new Backup(oldRoot, dest));
            }
        } else {
            ServerAccess.logOutputReprint("Can not move " + oldRoot + " to " + base + " the source (the first) is misisng");
        }
    }

    public static File tmpDir() throws IOException {
        //creating in home, not in tmp, as we need to be sure the backup is on same device
        File f = File.createTempFile("itwConfigCache", "tmpDir", new File(System.getProperty("user.home")));
        f.delete();
        f.mkdir();
        return f;
    }

    private static List<Backup> backupRealSettingsAndClear(File base) throws IOException {
        File config = new File(base, "config");
        config.mkdirs();
        File cache = new File(base, "cache");
        cache.mkdirs();
        List<Backup> l = new ArrayList<>();
        mv(oldRoot, base, l);
        mv(realCache, cache, l);
        mv(realConfig, config, l);
        return l;
    }

    private static void restoreSettings(List<Backup> col) throws IOException {
        for (Backup l : col) {
            mv(l.to, l.from.getParentFile(), null);
        }
    }

    public static void deleteRecursively(File f) {
        if (f.exists()) {
            ServerAccess.logOutputReprint("removing " + f);
            try {
                FirefoxProfilesOperator.deleteRecursively(f);
            } catch (IOException ex) {
                ServerAccess.logException(ex);
            }
        } else {
            ServerAccess.logOutputReprint("removal of " + f + " failed, do not exists");
        }
    }

    private static void cleanRealSettings() {
        deleteRecursively(oldRoot);
        deleteRecursively(realCache);
        deleteRecursively(realConfig);
    }

    private static void cleanHomeSettings() {
        deleteRecursively(oldRoot);
        deleteRecursively(homeCache);
        deleteRecursively(homeConfig);
    }

    private String[] removeXdgVAlues() {
        Map<String, String> p = System.getenv();
        Set<Entry<String, String>> r = p.entrySet();
        List<Entry<String, String>> rr = new ArrayList<>(r);
        Collections.sort(rr, new Comparator<Entry<String, String>>() {

            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        List<String> l = new ArrayList<>(p.size());
        int i = 0;
        int c = 0;
        for (Iterator<Entry<String, String>> it = rr.iterator(); it.hasNext(); i++) {
            Entry<String, String> entry = it.next();
            String v = entry.getValue();
            String s = entry.getKey() + "=" + v;
            if (entry.getKey().equals(PathsAndFiles.XDG_CACHE_HOME_VAR) || entry.getKey().equals(PathsAndFiles.XDG_CONFIG_HOME_VAR)) {
                ServerAccess.logOutputReprint("ignoring " + s);
                c++;
            } else {
                l.add(s);
            }

        }
        if (c == 0) {
            ServerAccess.logOutputReprint("no XDG defined, no change in variables ");
        }
        return l.toArray(new String[l.size()]);
    }

    private static String[] setXdgVAlues(File fakeRoot) {
        return setXdgVAlues(new File(fakeRoot.getAbsolutePath() + File.separator + "customCache"), new File(fakeRoot.getAbsolutePath() + File.separator + "customConfig"));
    }

    private static String[] setXdgVAlues(File cacheF, File configF) {
        boolean cache = false;
        boolean config = false;
        Map<String, String> p = System.getenv();
        Set<Entry<String, String>> r = p.entrySet();
        List<Entry<String, String>> rr = new ArrayList<>(r);
        Collections.sort(rr, new Comparator<Entry<String, String>>() {

            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        List<String> l = new ArrayList<>(p.size() + 2);
        int i = 0;
        for (Iterator<Entry<String, String>> it = rr.iterator(); it.hasNext(); i++) {
            Entry<String, String> entry = it.next();
            String v = entry.getValue();
            String s = entry.getKey() + "=" + v;
            switch (entry.getKey()) {
                case PathsAndFiles.XDG_CACHE_HOME_VAR:
                    ServerAccess.logOutputReprint(entry.getKey() + " was " + v);
                    v = cacheF.getAbsolutePath();
                    ServerAccess.logOutputReprint("set " + v);
                    cache = true;
                    break;
                case PathsAndFiles.XDG_CONFIG_HOME_VAR:
                    ServerAccess.logOutputReprint(entry.getKey() + " was " + v);
                    v = configF.getAbsolutePath();
                    ServerAccess.logOutputReprint("set " + v);
                    config = true;
                    break;
            }
            s = entry.getKey() + "=" + v;
            l.add(s);
        }
        if (!cache) {
            ServerAccess.logOutputReprint("was no cache");
            String v = cacheF.getAbsolutePath();
            ServerAccess.logOutputReprint("set " + v);
            String s = PathsAndFiles.XDG_CACHE_HOME_VAR + "=" + v;
            l.add(s);
        }
        if (!config) {
            ServerAccess.logOutputReprint("was no config");
            String v = configF.getAbsolutePath();
            ServerAccess.logOutputReprint("set " + v);
            String s = PathsAndFiles.XDG_CONFIG_HOME_VAR + "=" + v;
            l.add(s);
        }

        return l.toArray(new String[l.size()]);
    }

    private static void createFakeOldHomeCache() throws Exception {
        File tmp = tmpDir();
        fakeExtendedSecurity(new File(tmp, PathsAndFiles.DEPLOYMENT_SUBDIR_DIR));
        try {
            ProcessWrapper pw = new ProcessWrapper(
                    server.getJavawsLocation(),
                    Arrays.asList(new String[]{ServerAccess.HEADLES_OPTION}),
                    server.getUrl("simpletest2.jnlp"),
                    (ContentReaderListener) null,
                    null,
                    setXdgVAlues(tmp, tmp));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(simpletests2Run.toPassingString(), simpletests2Run.evaluate(pr.stderr));
            File currentConfigCache = new File(tmp, PathsAndFiles.DEPLOYMENT_SUBDIR_DIR);
            File oldIcedTea = new File(new File(System.getProperty("user.home")) + File.separator + ".icedtea");
            boolean a = currentConfigCache.renameTo(oldIcedTea);
            Assert.assertTrue("creation of old cache by renaming " + currentConfigCache + " to " + oldIcedTea + " failed", a);
            assertOldMainFilesInHome(false, true, false);
            assertNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(tmp);
        }

    }

    private static void createFakeOldHomeConfig() throws Exception {
        File tmp = tmpDir();
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw1.setVars(setXdgVAlues(tmp, tmp));
            ProcessResult pr1 = pw1.execute();

            ProcessWrapper pw2 = new ProcessWrapper();
            pw2.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "oldBaf", "differentOldBaf"
                    }));
            pw2.setVars(setXdgVAlues(tmp, tmp));
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr1.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr2.stdout));
            File currentConfigCache = new File(tmp, PathsAndFiles.DEPLOYMENT_SUBDIR_DIR);
            File oldIcedTea = new File(new File(System.getProperty("user.home")) + File.separator + ".icedtea");
            boolean a = currentConfigCache.renameTo(oldIcedTea);
            Assert.assertTrue("creation of old config by renaming " + currentConfigCache + " to " + oldIcedTea + " failed", a);
            assertOldConfigFilesInHome(true, true, true);
            assertNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
            deleteRecursively(tmp);
        }

    }

    @After
    @Before
    public void cleanHome() {
        cleanHomeSettings();
    }

    @After
    @Before
    public void cleanReal() {
        cleanRealSettings();
    }

    private static List<File> getContentOfDirectory(File f) {
        List<File> result = new ArrayList<>();
        if (f == null || !f.exists() || !f.isDirectory()) {
            return result;
        }
        File[] files = f.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getContentOfDirectory(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }

    private static String listToString(List<File>... l) {
        StringBuilder sb = new StringBuilder();
        for (List<File> list : l) {
            for (File s : list) {
                sb.append(s.getAbsolutePath()).append('\n');
            }
        }

        return sb.toString();
    }

    private static void assertConfigFiles(String s, boolean certs, boolean trust, boolean props) {
        if (certs) {
            Assert.assertTrue(trustedCertsInside.toPassingString(), trustedCertsInside.evaluate(s));
        }
        if (trust) {
            Assert.assertTrue(appletTrustInside.toPassingString(), appletTrustInside.evaluate(s));
        }
        if (props) {
            Assert.assertTrue(propsInside.toPassingString(), propsInside.evaluate(s));
        }
        Assert.assertTrue(icedteaHostInside.toPassingString(), icedteaHostInside.evaluate(s));
    }

    private static void assertMainFiles(String s, boolean s1, boolean s2, boolean a1) {
        if (a1) {
            Assert.assertTrue(appletJarInside.toPassingString(), appletJarInside.evaluate(s));
        }
        if (s1) {
            Assert.assertTrue(jnlpInside1.toPassingString(), jnlpInside1.evaluate(s));
            Assert.assertTrue(jarInside1.toPassingString(), jarInside1.evaluate(s));
        }
        if (s2) {
            Assert.assertTrue(jnlpInside2.toPassingString(), jnlpInside2.evaluate(s));
            Assert.assertTrue(jarInside2.toPassingString(), jarInside2.evaluate(s));
        }
        Assert.assertTrue(localHostInside.toPassingString(), localHostInside.evaluate(s));
        Assert.assertTrue(cacheHostInside.toPassingString(), cacheHostInside.evaluate(s));
        Assert.assertTrue(icedteaHostInside.toPassingString(), icedteaHostInside.evaluate(s));
        Assert.assertTrue(securityHostInside.toPassingString(), securityHostInside.evaluate(s));
        Assert.assertTrue(trustedHostInside.toPassingString(), trustedHostInside.evaluate(s));
    }

    private static void assertNotConfigFiles(String s, boolean certs, boolean trust, boolean props) {
        if (certs) {
            Assert.assertFalse(trustedCertsInside.toFailingString(), trustedCertsInside.evaluate(s));
        }
        if (trust) {
            Assert.assertFalse(appletTrustInside.toFailingString(), appletTrustInside.evaluate(s));
        }
        if (props) {
            Assert.assertFalse(propsInside.toFailingString(), propsInside.evaluate(s));
        }
        Assert.assertFalse(icedteaHostInside.toFailingString(), icedteaHostInside.evaluate(s));
    }

    private static void assertNotMainFiles(String s, boolean s1, boolean s2, boolean a1) {
        if (a1) {
            Assert.assertFalse(appletJarInside.toFailingString(), appletJarInside.evaluate(s));
        }
        if (s1) {
            Assert.assertFalse(jnlpInside1.toFailingString(), jnlpInside1.evaluate(s));
            Assert.assertFalse(jarInside1.toFailingString(), jarInside1.evaluate(s));
        }
        if (s2) {
            Assert.assertFalse(jnlpInside2.toFailingString(), jnlpInside2.evaluate(s));
            Assert.assertFalse(jarInside2.toFailingString(), jarInside2.evaluate(s));
        }
        Assert.assertFalse(localHostInside.toFailingString(), localHostInside.evaluate(s));
        Assert.assertFalse(cacheHostInside.toFailingString(), cacheHostInside.evaluate(s));
        Assert.assertFalse(icedteaHostInside.toFailingString(), icedteaHostInside.evaluate(s));
        Assert.assertFalse(securityHostInside.toFailingString(), securityHostInside.evaluate(s));
        Assert.assertFalse(trustedHostInside.toFailingString(), trustedHostInside.evaluate(s));
    }

    private static void assertMainFilesInHome(boolean s1, boolean s2, boolean a1) {
        String configHome = System.getProperty("user.home") + File.separator + ".config" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        String cacheHome = System.getProperty("user.home") + File.separator + ".cache" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        assertMainFiles(
                listToString(getContentOfDirectory(new File(configHome))) + "\n"
                + listToString(getContentOfDirectory(new File(cacheHome))), s1, s2, a1);
    }

    private static void assertConfigFilesInHome(boolean certs, boolean trust, boolean props) {
        String configHome = System.getProperty("user.home") + File.separator + ".config" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        String cacheHome = System.getProperty("user.home") + File.separator + ".cache" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        assertConfigFiles(
                listToString(getContentOfDirectory(new File(configHome))) + "\n"
                + listToString(getContentOfDirectory(new File(cacheHome))), certs, trust, props);
    }

    private static void assertNotMainFilesInHome(boolean s1, boolean s2, boolean a1) {
        String configHome = System.getProperty("user.home") + File.separator + ".config" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        String cacheHome = System.getProperty("user.home") + File.separator + ".cache" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        assertNotMainFiles(
                listToString(getContentOfDirectory(new File(configHome))) + "\n"
                + listToString(getContentOfDirectory(new File(cacheHome))), s1, s2, a1);
    }

    private static void assertNotConfigFilesInHome(boolean certs, boolean trust, boolean props) {
        String configHome = System.getProperty("user.home") + File.separator + ".config" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        String cacheHome = System.getProperty("user.home") + File.separator + ".cache" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR;
        assertNotConfigFiles(
                listToString(getContentOfDirectory(new File(configHome))) + "\n"
                + listToString(getContentOfDirectory(new File(cacheHome))), certs, trust, props);
    }
    //runs
    private static final RulesFolowingClosingListener.ContainsRule simpletests1Run = new RulesFolowingClosingListener.ContainsRule("Good simple javaws exapmle");
    private static final RulesFolowingClosingListener.ContainsRule simpletests2Run = new RulesFolowingClosingListener.ContainsRule("Correct exception");
    private static final RulesFolowingClosingListener.ContainsRule moving = new RulesFolowingClosingListener.ContainsRule(DeploymentConfiguration.TRANSFER_TITLE);
    private static final RulesFolowingClosingListener.NotContainsRule notMoving = new RulesFolowingClosingListener.NotContainsRule(DeploymentConfiguration.TRANSFER_TITLE);
    private static final RulesFolowingClosingListener.ContainsRule unknownProperty = new RulesFolowingClosingListener.ContainsRule("WARNING: Unknown property name");
    private static final RulesFolowingClosingListener.ContainsRule applet1Run = new RulesFolowingClosingListener.ContainsRule("applet was started");
    //javaws/plugin files
    private static final RulesFolowingClosingListener.ContainsRule jnlpInside1 = new RulesFolowingClosingListener.ContainsRule("/simpletest1.jnlp");
    private static final RulesFolowingClosingListener.ContainsRule jarInside1 = new RulesFolowingClosingListener.ContainsRule("/simpletest1.jar");
    private static final RulesFolowingClosingListener.ContainsRule jnlpInside2 = new RulesFolowingClosingListener.ContainsRule("/simpletest2.jnlp");
    private static final RulesFolowingClosingListener.ContainsRule jarInside2 = new RulesFolowingClosingListener.ContainsRule("/simpletest2.jar");
    private static final RulesFolowingClosingListener.ContainsRule appletJarInside = new RulesFolowingClosingListener.ContainsRule("AppletTest.jar");
    //private static final RulesFolowingClosingListener.ContainsRule appletHtmlInside = new RulesFolowingClosingListener.ContainsRule("appletAutoTests2.html"); not caching htmls
    //common files            
    private static final RulesFolowingClosingListener.ContainsRule localHostInside = new RulesFolowingClosingListener.ContainsRule("/localhost/");
    private static final RulesFolowingClosingListener.ContainsRule cacheHostInside = new RulesFolowingClosingListener.ContainsRule("/cache/");
    private static final RulesFolowingClosingListener.ContainsRule icedteaHostInside = new RulesFolowingClosingListener.ContainsRule("/icedtea-web/");
    private static final RulesFolowingClosingListener.ContainsRule oldIcedteaHostInside = new RulesFolowingClosingListener.ContainsRule("/.icedtea/");
    private static final RulesFolowingClosingListener.ContainsRule securityHostInside = new RulesFolowingClosingListener.ContainsRule("/security/");
    private static final RulesFolowingClosingListener.ContainsRule trustedHostInside = new RulesFolowingClosingListener.ContainsRule("/trusted");
    //config files
    private static final RulesFolowingClosingListener.ContainsRule trustedCertsInside = new RulesFolowingClosingListener.ContainsRule("trusted.cacerts");
    private static final RulesFolowingClosingListener.ContainsRule appletTrustInside = new RulesFolowingClosingListener.ContainsRule(".appletTrustSettings");
    private static final RulesFolowingClosingListener.ContainsRule propsInside = new RulesFolowingClosingListener.ContainsRule("deployment.properties");

    /*
     *JAVAWS - NO OLD CONFIG
     */
    @Test
    public void runJavawsInCleanSystemWithNoXdg() throws Exception {
        assertNotMainFilesInHome(true, true, true);
        assertOldNotMainFilesInHome(true, true, true);
        //we need fake security and manifests
        File ff = new File(PathsAndFiles.USER_CONFIG_HOME);
        try {
            fakeExtendedSecurity(ff);
            ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, removeXdgVAlues());
            ProcessResult pr = pw.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            itwDoesNotComplainAboutOldConfig(pr); //no old config
            assertMainFilesInHome(true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
            ProcessResult pr2 = pw.execute();
            itwDoesNotComplainAboutOldConfig(pr2);
            assertMainFilesInHome(true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(ff);
        }
    }

    @Test
    public void runJavawsInCleanSystemWithXdg() throws Exception {
        File f = tmpDir();
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            //we need fake security and manifests
            File ff = new File(f, "customConfig/" + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR);
            fakeExtendedSecurity(ff);
            ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            itwDoesNotComplainAboutOldConfig(pr); //no old config
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
            ProcessResult pr2 = pw.execute();
            itwDoesNotComplainAboutOldConfig(pr2); //no old config
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr2.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(f);
        }
    }

    @Test
    public void runJavawsInCleanSystemWithXdgAndNoParent() throws Exception {
        File f = tmpDir();
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            f.delete();
            //we need fake security and manifests
            File ff = new File(f, "customConfig/" + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR);
            fakeExtendedSecurity(ff);
            ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            itwDoesNotComplainAboutOldConfig(pr);
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
            ProcessResult pr3 = pw.execute();
            itwDoesNotComplainAboutOldConfig(pr3);
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr3.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr3.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, false, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(f);
        }
    }

    private static void itwDoesNotComplainAboutOldConfig(ProcessResult pr) {
        itwDoesNotComplainAboutOldConfig(pr.stdout);
    }
    private static void itwDoesNotComplainAboutOldConfig(String stdout) {
        Assert.assertFalse(stdout.contains(DeploymentConfiguration.TRANSFER_TITLE));
    }
    
    private static void itwDoesComplainAboutOldConfig(ProcessResult pr) {
        itwDoesComplainAboutOldConfig(pr.stdout);
    }
    private static void itwDoesComplainAboutOldConfig(String stdout) {
        Assert.assertTrue(stdout.contains(DeploymentConfiguration.TRANSFER_TITLE));
    }
    
    private static void assertOldMainFiles(String s, boolean s1, boolean s2, boolean a1) {
        if (a1) {
            Assert.assertTrue(appletJarInside.toPassingString(), appletJarInside.evaluate(s));
        }
        if (s1) {
            Assert.assertTrue(jnlpInside1.toPassingString(), jnlpInside1.evaluate(s));
            Assert.assertTrue(jarInside1.toPassingString(), jarInside1.evaluate(s));
        }
        if (s2) {
            Assert.assertTrue(jnlpInside2.toPassingString(), jnlpInside2.evaluate(s));
            Assert.assertTrue(jarInside2.toPassingString(), jarInside2.evaluate(s));
        }
        Assert.assertTrue(localHostInside.toPassingString(), localHostInside.evaluate(s));
        Assert.assertTrue(cacheHostInside.toPassingString(), cacheHostInside.evaluate(s));
        Assert.assertTrue(oldIcedteaHostInside.toPassingString(), oldIcedteaHostInside.evaluate(s));
        Assert.assertTrue(securityHostInside.toPassingString(), securityHostInside.evaluate(s));
        Assert.assertTrue(trustedHostInside.toPassingString(), trustedHostInside.evaluate(s));
    }

    private static void assertOldConfigFiles(String s, boolean certs, boolean trust, boolean props) {
        if (certs) {
            Assert.assertTrue(trustedCertsInside.toPassingString(), trustedCertsInside.evaluate(s));
        }
        if (trust) {
            Assert.assertTrue(appletTrustInside.toPassingString(), appletTrustInside.evaluate(s));
        }
        if (props) {
            Assert.assertTrue(propsInside.toPassingString(), propsInside.evaluate(s));
        }
        Assert.assertTrue(oldIcedteaHostInside.toPassingString(), oldIcedteaHostInside.evaluate(s));
    }

    private static void assertOldNotMainFiles(String s, boolean s1, boolean s2, boolean a1) {
        if (a1) {
            Assert.assertFalse(appletJarInside.toFailingString(), appletJarInside.evaluate(s));
        }
        if (s1) {
            Assert.assertFalse(jnlpInside1.toFailingString(), jnlpInside1.evaluate(s));
            Assert.assertFalse(jarInside1.toFailingString(), jarInside1.evaluate(s));
        }
        if (s2) {
            Assert.assertFalse(jnlpInside2.toFailingString(), jnlpInside2.evaluate(s));
            Assert.assertFalse(jarInside2.toFailingString(), jarInside2.evaluate(s));
        }
        Assert.assertFalse(localHostInside.toFailingString(), localHostInside.evaluate(s));
        Assert.assertFalse(cacheHostInside.toFailingString(), cacheHostInside.evaluate(s));
        Assert.assertFalse(oldIcedteaHostInside.toFailingString(), oldIcedteaHostInside.evaluate(s));
        Assert.assertFalse(securityHostInside.toFailingString(), securityHostInside.evaluate(s));
        Assert.assertFalse(trustedHostInside.toFailingString(), trustedHostInside.evaluate(s));
    }

    private static void assertOldNotConfigFiles(String s, boolean certs, boolean trust, boolean props) {
        if (certs) {
            Assert.assertFalse(trustedCertsInside.toFailingString(), trustedCertsInside.evaluate(s));
        }
        if (trust) {
            Assert.assertFalse(appletTrustInside.toFailingString(), appletTrustInside.evaluate(s));
        }
        if (props) {
            Assert.assertFalse(propsInside.toFailingString(), propsInside.evaluate(s));
        }
        Assert.assertFalse(oldIcedteaHostInside.toFailingString(), oldIcedteaHostInside.evaluate(s));
    }

    private static void assertOldMainFilesInHome(boolean s1, boolean s2, boolean a1) {
        String oldHome = System.getProperty("user.home") + File.separator + ".icedtea";
        assertOldMainFiles(listToString(getContentOfDirectory(new File(oldHome))), s1, s2, a1);
    }

    private static void assertOldNotMainFilesInHome(boolean s1, boolean s2, boolean a1) {
        String oldHome = System.getProperty("user.home") + File.separator + ".icedtea";
        assertOldNotMainFiles(listToString(getContentOfDirectory(new File(oldHome))), s1, s2, a1);
    }

    private static void assertOldConfigFilesInHome(boolean certs, boolean trust, boolean props) {
        String oldHome = System.getProperty("user.home") + File.separator + ".icedtea";
        assertOldConfigFiles(listToString(getContentOfDirectory(new File(oldHome))), certs, trust, props);
    }

    private static void assertOldNotConfigFilesInHome(boolean certs, boolean trust, boolean props) {
        String oldHome = System.getProperty("user.home") + File.separator + ".icedtea";
        assertOldNotConfigFiles(listToString(getContentOfDirectory(new File(oldHome))), certs, trust, props);
    }

    /*
     *JAVAWS - OLD CONFIG EXISTS
     */
    @Test
    public void runJavawsWithNoXdg_oldIcedTeaConfigExisted() throws Exception {
        File ff = new File(PathsAndFiles.USER_CONFIG_HOME);
        try {
            assertNotMainFilesInHome(true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            createFakeOldHomeCache();
            removeUnsupportedLegacyFiles();
            ProcessWrapper pw1 = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, removeXdgVAlues());
            ProcessResult pr1 = pw1.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr1.stdout));
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr1.stdout));
            assertMainFilesInHome(true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
            fakeExtendedSecurity(ff);
            ProcessWrapper pw2 = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr2.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertMainFilesInHome(true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(ff);
        }
    }

    @Test
    public void runJavawsWithXdg_oldIcedTeaConfigExisted() throws Exception {
        File f = tmpDir();
        File ff = new File(PathsAndFiles.USER_CONFIG_HOME);
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            createFakeOldHomeCache();
            removeUnsupportedLegacyFiles();
            ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr.stdout));
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
            fakeExtendedSecurity(ff);
            ProcessWrapper pw2 = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr2.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(f);
            deleteRecursively(ff);
        }
    }

    @Test
    public void runJavawsWithXdgAndNoParent_oldIcedTeaConfigExisted() throws Exception {
        File f = tmpDir();
        File ff = new File(PathsAndFiles.USER_CONFIG_HOME);
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            createFakeOldHomeCache();
            f.delete();
            removeUnsupportedLegacyFiles();
            ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr.stdout));
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
            fakeExtendedSecurity(ff);
            ProcessWrapper pw2 = new ProcessWrapper(server.getJavawsLocation(), null, server.getUrl("simpletest1.jnlp"), (ContentReaderListener) null, null, removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(simpletests1Run.toPassingString(), simpletests1Run.evaluate(pr2.stdout));
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), true, true, false);
            assertOldNotMainFilesInHome(true, true, true);
        } finally {
            deleteRecursively(f);
            deleteRecursively(ff);
        }
    }

    /*
     *ITW-SETTINGS gui - NO OLD CONFIG
     */
    @Test
    public void runItwGuiInCleanSystemWithNoXdg() throws Exception {
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw.setVars(removeXdgVAlues());
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
        }
    }

    @Test
    public void runItwGuiInCleanSystemWithXdg() throws Exception {
        File f = tmpDir();
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw.setVars(setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
            deleteRecursively(f);
        }
    }
    /*
     *ITW-SETTINGS gui- OLD CONFIG EXISTS
     */

    @Test
    public void runItwGuiWithNoXdg_oldIcedTeaConfigExisted() throws Exception {
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            createFakeOldHomeConfig();
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw1.setVars(removeXdgVAlues());
            ProcessResult pr1 = pw1.execute();
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr1.stdout));
            assertConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);

            ProcessWrapper pw2 = new ProcessWrapper();
            pw2.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw2.setVars(removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
        }

    }

    @Test
    public void runItwGuiWithXdg_oldIcedTeaConfigExisted() throws Exception {
        File f = tmpDir();
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            createFakeOldHomeConfig();
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw1.setVars(setXdgVAlues(f));
            ProcessResult pr = pw1.execute();
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw2 = new ProcessWrapper();
            pw2.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath()
                    }));
            pw2.setVars(removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
            deleteRecursively(f);
        }
    }
    /*
     *ITW-SETTINGS commandline - NO OLD CONFIG
     */

    @Test
    public void runItwCmdInCleanSystemWithNoXdg() throws Exception {
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "blah", "differentBlah"
                    }));
            pw.setVars(removeXdgVAlues());
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr.stdout));
            assertConfigFilesInHome(false, false, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
        }
    }

    @Test
    public void runItwCmdInCleanSystemWithXdg() throws Exception {
        File f = tmpDir();
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "blah", "differentBlah"
                    }));
            pw.setVars(setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), false, false, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
            deleteRecursively(f);
        }
    }
    /*
     *ITW-SETTINGS commandline- OLD CONFIG EXISTS
     */

    @Test
    public void runItwCmdWithNoXdg_oldIcedTeaConfigExisted() throws Exception {
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            createFakeOldHomeConfig();
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "blah", "differentBlah"
                    }));
            pw1.setVars(removeXdgVAlues());
            ProcessResult pr1 = pw1.execute();
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr1.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr1.stdout));
            assertConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);

            ProcessWrapper pw2 = new ProcessWrapper();
            pw2.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "baf", "differentBaf"
                    }));
            pw2.setVars(removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr2.stdout));
            assertConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
        }

    }

    @Test
    public void runItwCmdWithXdg_oldIcedTeaConfigExisted() throws Exception {
        File f = tmpDir();
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            createFakeOldHomeConfig();
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "blah", "differentBlah"
                    }));
            pw1.setVars(setXdgVAlues(f));
            ProcessResult pr = pw1.execute();
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw2 = new ProcessWrapper();
            pw2.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "baf", "differentBaf"
                    }));
            pw2.setVars(removeXdgVAlues());
            ProcessResult pr2 = pw2.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr2.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr2.stdout));
            assertConfigFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
            deleteRecursively(f);
        }
    }

    private static void fakeExtendedSecurity(File file) throws IOException {
        if (!file.exists()) {
            boolean a = file.mkdirs();
            Assert.assertTrue("creation of directories for " + file + " failed", a);
        }
        File f = new File(file, PathsAndFiles.USER_DEPLOYMENT_FILE.getDefaultFile().getName());
        ServerAccess.saveFile("deployment.security.level=ALLOW_UNSIGNED\ndeployment.manifest.attributes.check=NONE", f);
    }

    /*
     *PLUGIN - NO OLD CONFIG
     */
    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void runAppletInCleanSystemWithNoXdg() throws Exception {
        assertNotMainFilesInHome(true, true, true);
        assertOldNotMainFilesInHome(true, true, true);
        //intentionally hardoced default
        fakeExtendedSecurity(new File(System.getProperty("user.home") + File.separator + ".config" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR));
        ProcessWrapper pw = new ProcessWrapper();
        pw.setArgs(Arrays.asList(
                new String[]{
                    server.getCurrentBrowser().getBin(),
                    server.getUrl("appletAutoTests2.html").toString()
                }));
        pw.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
        pw.setVars(removeXdgVAlues());
        ProcessResult pr = pw.execute();
        Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr.stdout));
        Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
        assertMainFilesInHome(false, false, true);
        assertOldNotMainFilesInHome(true, true, true);
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void runAppletsInCleanSystemWithXdg() throws Exception {
        File f = tmpDir();
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            fakeExtendedSecurity(new File(f.getAbsolutePath() + File.separator + "customConfig" + File.separator + PathsAndFiles.DEPLOYMENT_SUBDIR_DIR));
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        server.getCurrentBrowser().getBin(),
                        server.getUrl("appletAutoTests2.html").toString()
                    }));
            pw.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
            pw.setVars(setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), false, false, true);
            assertOldNotMainFilesInHome(true, true, true);
            /*do alst, we need to check the migration, not applet lunching itself*/
            Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr.stdout));
        } finally {
            deleteRecursively(f);
        }
    }
    /*
     *PLUGIN - OLD CONFIG EXISTS
     */

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void runAppletInCleanSystemWithNoXdg_oldIcedTeaConfigExisted() throws Exception {
        assertNotMainFilesInHome(true, true, true);
        assertOldNotMainFilesInHome(true, true, true);
        fakeExtendedSecurity(new File(System.getProperty("user.home") + File.separator + ".icedtea"));
        ProcessWrapper pw1 = new ProcessWrapper();
        pw1.setArgs(Arrays.asList(
                new String[]{
                    server.getCurrentBrowser().getBin(),
                    server.getUrl("appletAutoTests2.html").toString()
                }));
        pw1.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
        pw1.setVars(removeXdgVAlues());
        ProcessResult pr1 = pw1.execute();
        Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr1.stdout));
        Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr1.stdout));
        assertMainFilesInHome(false, false, true);
        assertOldNotMainFilesInHome(true, true, true);
        ProcessWrapper pw = new ProcessWrapper();
        pw.setArgs(Arrays.asList(
                new String[]{
                    server.getCurrentBrowser().getBin(),
                    server.getUrl("appletAutoTests2.html").toString()
                }));
        pw.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
        pw.setVars(removeXdgVAlues());
        ProcessResult pr = pw.execute();
        Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr.stdout));
        Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
        assertMainFilesInHome(false, false, true);
        assertOldNotMainFilesInHome(true, true, true);
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void runAppletsInCleanSystemWithXdg_oldIcedTeaConfigExisted() throws Exception {
        File f = tmpDir();
        try {
            assertNotMainFiles(listToString(getContentOfDirectory(f)), true, true, true);
            assertOldNotMainFilesInHome(true, true, true);
            fakeExtendedSecurity(new File(System.getProperty("user.home") + File.separator + ".icedtea"));
            ProcessWrapper pw1 = new ProcessWrapper();
            pw1.setArgs(Arrays.asList(
                    new String[]{
                        server.getCurrentBrowser().getBin(),
                        server.getUrl("appletAutoTests2.html").toString()
                    }));
            pw1.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
            pw1.setVars(setXdgVAlues(f));
            ProcessResult pr1 = pw1.execute();
            Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr1.stdout));
            Assert.assertTrue(moving.toPassingString(), moving.evaluate(pr1.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), false, false, true);
            assertOldNotMainFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        server.getCurrentBrowser().getBin(),
                        server.getUrl("appletAutoTests2.html").toString()
                    }));
            pw.addStdOutListener(new RulesFolowingClosingListener(applet1Run));
            pw.setVars(setXdgVAlues(f));
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            assertMainFiles(listToString(getContentOfDirectory(f)), false, false, true);
            assertOldNotMainFilesInHome(true, true, true);
            /*do last, we need to check the migration, not applet lunching itself*/
            Assert.assertTrue(applet1Run.toPassingString(), applet1Run.evaluate(pr.stdout));
        } finally {
            deleteRecursively(f);
        }
    }

    @Test
    //this test is unrelated to XDG, bus it testing issue in new option parser.
    //when this test was under fixing, it was found, that parser is unable to handle two same params in set
    //this is reproducing it. 
    @Bug(id = "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2015-March/031049.html")
    public void runItwCmdInCleanSystemWithNoXdgAndWithTwoSameValuesInCMD() throws Exception {
        long t = ServerAccess.PROCESS_TIMEOUT;
        ServerAccess.PROCESS_TIMEOUT = 5000;
        try {
            assertNotConfigFilesInHome(true, true, true);
            assertOldNotConfigFilesInHome(true, true, true);
            ProcessWrapper pw = new ProcessWrapper();
            pw.setArgs(Arrays.asList(
                    new String[]{
                        new File(server.getJavawsFile().getParentFile(), "itweb-settings").getAbsolutePath(),
                        //one impl of new parser was unable to handle duplicates
                        "set", "blah", "blah"
                    }));
            pw.setVars(removeXdgVAlues());
            ProcessResult pr = pw.execute();
            Assert.assertTrue(notMoving.toPassingString(), notMoving.evaluate(pr.stdout));
            Assert.assertTrue(unknownProperty.toPassingString(), unknownProperty.evaluate(pr.stdout));
            assertConfigFilesInHome(false, false, true);
            assertOldNotConfigFilesInHome(true, true, true);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = t;
        }
    }
}
