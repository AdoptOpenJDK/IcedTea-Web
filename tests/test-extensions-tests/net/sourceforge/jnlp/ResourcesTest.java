/* ResourcesTest.java
Copyright (C) 2011-2012 Red Hat, Inc.

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
package net.sourceforge.jnlp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.browsertesting.Browser;
import net.sourceforge.jnlp.browsertesting.BrowserFactory;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.browsers.LinuxBrowser;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class ResourcesTest extends  BrowserTest{


    @Test
    @NeedsDisplay
    public void testNonExisitngBrowserWillNotDeadlock() throws Exception {
        server.setCurrentBrowser(Browsers.none);
        ProcessResult pr = server.executeBrowser("not_existing_url.html");
        Assert.assertNull(pr.process);
        Assert.assertEquals(pr.stderr, "");
        Assert.assertEquals(pr.stdout, "");
        Assert.assertTrue(pr.wasTerminated);
        Assert.assertTrue(pr.returnValue < 0);
        Assert.assertNotNull(pr.deadlyException);
    }

    @Test
    public void testUnexistingProcessWillFailRecognizedly() throws Exception {
        server.setCurrentBrowser(Browsers.none);
        List<String> al=Arrays.asList(new String[] {"definietly_not_Existing_process"});
        ProcessResult pr = server.executeProcess(al);
        Assert.assertNull(pr.process);
        Assert.assertEquals(pr.stderr, "");
        Assert.assertEquals(pr.stdout, "");
        Assert.assertTrue(pr.wasTerminated);
        Assert.assertTrue(pr.returnValue < 0);
        Assert.assertNotNull(pr.deadlyException);
    }

    @Test
    public void testGetUrlUponThisInstance() throws MalformedURLException{
        URL u1=server.getUrlUponThisInstance("simple.jsp");
        URL u2=server.getUrlUponThisInstance("/simple.jsp");
        Assert.assertEquals(u1, u2);
    }

    @Test
    @TestInBrowsers(testIn=Browsers.none)
    public void testNonExisitngBrowserWillNotCauseMess() throws Exception {
        ProcessResult pr = server.executeBrowser("not_existing_url.html");
        Assert.assertNull(pr.process);
        Assert.assertEquals(pr.stderr, "");
        Assert.assertEquals(pr.stdout, "");
        Assert.assertTrue(pr.wasTerminated);
        Assert.assertTrue(pr.returnValue < 0);
        Assert.assertNotNull(pr.deadlyException);
    }

    @Test
    public void testBrowsers2() throws Exception {
        List<Browser> a = BrowserFactory.getFactory().getAllBrowsers();
        Assert.assertNotNull("returned browsers array must not be null", a);
        Assert.assertTrue("at least one browser must be configured", a.size() > 0);
        for (Browser b : a) {
            testBrowser(b);
        }

    }

    @Test
    @TestInBrowsers(testIn = Browsers.all)
    public void testBrowser3() throws Exception {
        testBrowser(server.getCurrentBrowser());
    }

    @Test
    public void testBrowsers1() throws Exception {
        BrowserFactory bf = new BrowserFactory(null);
        int expected = 0;
        Assert.assertTrue("Created from null there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        bf = new BrowserFactory("");
        expected = 0;
        Assert.assertTrue("Created from empty there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        String s = "dsgrdg";
        bf = new BrowserFactory(s);
        expected = 0;
        Assert.assertTrue("Created from nonsense " + s + " there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = "sgrg/jkik";
        bf = new BrowserFactory(s);
        expected = 0;
        Assert.assertTrue("Created from nonsense " + s + " there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox + "/jkik";
        bf = new BrowserFactory(s);
        expected = 0;
        Assert.assertTrue("Created from nonsense " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = "sgrg/jkik:sege";
        bf = new BrowserFactory(s);
        expected = 0;
        Assert.assertTrue("Created from  two nonsenses " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox.toExec() + ":" + Browsers.firefox;
        bf = new BrowserFactory(s);
        expected = 2;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox.toExec();
        bf = new BrowserFactory(s);
        expected = 1;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = "something/somewhere/" + Browsers.firefox.toExec();
        bf = new BrowserFactory(s);
        expected = 1;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = "something/somewhere/" + Browsers.firefox.toExec() + ":" + "something/somewhere/" + Browsers.opera.toExec();
        bf = new BrowserFactory(s);
        expected = 2;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = "something/somewhere/" + Browsers.firefox.toExec() + ":" + "something/somewhere/" + Browsers.opera.toExec() + ":" + Browsers.chromiumBrowser;
        bf = new BrowserFactory(s);
        expected = 3;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox.toExec() + ":" + "vfdgf" + ":" + Browsers.googleChrome.toExec();
        bf = new BrowserFactory(s);
        expected = 2;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox.toExec() + ":" + Browsers.chromiumBrowser + ":" + Browsers.googleChrome.toExec() + ":" + Browsers.opera + ":" + Browsers.epiphany + ":" + Browsers.midori;
        bf = new BrowserFactory(s);
        expected = 6;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);
        testFullFactory(bf);

        s = "fgfd/" + Browsers.firefox.toExec() + ":" + "/fgfd/" + Browsers.chromiumBrowser + ":" + "fgfd/dfsdf/" + Browsers.googleChrome.toExec() + ":" + "/g/fgfd/" + Browsers.opera + ":" + Browsers.epiphany + ":" + Browsers.midori;
        bf = new BrowserFactory(s);
        expected = 6;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);
        testFullFactory(bf);

        s = Browsers.firefox.toExec() + ":" + ":" + Browsers.googleChrome.toExec() + ":" + Browsers.opera;
        bf = new BrowserFactory(s);
        expected = 3;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = Browsers.firefox.toExec() + ":" + ":" + ":" + Browsers.opera;
        bf = new BrowserFactory(s);
        expected = 2;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = ":" + ":" + Browsers.googleChrome.toExec() + ":";
        bf = new BrowserFactory(s);
        expected = 1;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);

        s = ":" + Browsers.firefox.toExec() + ":bfgbfg/fddf/" + Browsers.googleChrome.toExec() + ":";
        bf = new BrowserFactory(s);
        expected = 2;
        Assert.assertTrue("Created from  " + s + "there must be " + expected + " browsers in factory. Is" + bf.getAllBrowsers().size(), bf.getAllBrowsers().size() == expected);
    }

    @Test
    public void testResourcesExists() throws Exception {
        File[] simpleContent = server.getDir().listFiles(new FileFilter() {

            public boolean accept(File file) {
                if (!file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        Assert.assertNotNull(simpleContent);
        Assert.assertTrue(simpleContent.length > 5);

        for (int i = 0; i < simpleContent.length; i++) {
            File file = simpleContent[i];
            ServerAccess.logOutputReprint(file.getName());
            //server port have in fact no usage in converting filename to uri-like-filename.
            //But if there is null, instead if some number, then nullpointer exception is thrown (Integer->int).
            //So I'm using "real" currently used port, instead of some random value.
            URI u = new URI((String) null, (String) null, (String) null, server.getPort(), file.getName(), (String) null, null);
            ServerAccess.logOutputReprint(" ("+u.toString()+")");
            String fname = u.toString();
            if (file.getName().toLowerCase().endsWith(".jnlp")) {
                String c = server.getResourceAsString("/" + fname);
                Assert.assertTrue(c.contains("<"));
                Assert.assertTrue(c.contains(">"));
                Assert.assertTrue(c.contains("jnlp"));
                Assert.assertTrue(c.contains("resources"));
                Assert.assertTrue(c.replaceAll("\\s*", "").contains("</jnlp>"));

            } else {
                byte[] c = server.getResourceAsBytes("/" + fname).toByteArray();
                Assert.assertEquals(c.length, file.length());
            }

        }

    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void testListeners() throws Exception {
        final StringBuilder o1 = new StringBuilder();
        final StringBuilder e1 = new StringBuilder();
        final StringBuilder o2 = new StringBuilder();
        final StringBuilder e2 = new StringBuilder();
        final ContentReaderListener lo = new ContentReaderListener() {

            @Override
            public void charReaded(char ch) {
                o1.append(ch);
            }

            @Override
            public void lineReaded(String s) {
                o2.append(s).append("\n");
            }
        };
        ContentReaderListener le = new ContentReaderListener() {

            @Override
            public void charReaded(char ch) {
                e1.append(ch);
            }

            @Override
            public void lineReaded(String s) {
                e2.append(s).append("\n");
            }
        };
       ProcessResult pr = server.executeBrowser("not_existing_url.html",lo,le);
       server.setCurrentBrowser(BrowserFactory.getFactory().getFirst().getID());
       Assert.assertNotNull(server.getCurrentBrowsers());
       Assert.assertNotNull(server.getCurrentBrowser());
       Assert.assertEquals(pr.stdout, o1.toString());
       Assert.assertEquals(pr.stderr, e1.toString());
       //the last \n is mandatory as las tline is flushed also when proces dies
       Assert.assertEquals(pr.stdout.replace("\n", ""), o2.toString().replace("\n", ""));
       Assert.assertEquals(pr.stderr.replace("\n", ""), e2.toString().replace("\n", ""));
    }

    private void testFullFactory(BrowserFactory bf) {
        Assert.assertEquals(bf.getBrowser(Browsers.chromiumBrowser).getID(), Browsers.chromiumBrowser);
        Assert.assertEquals(bf.getBrowser(Browsers.googleChrome).getID(), Browsers.googleChrome);
        Assert.assertEquals(bf.getBrowser(Browsers.firefox).getID(), Browsers.firefox);
        Assert.assertEquals(bf.getBrowser(Browsers.opera).getID(), Browsers.opera);
        Assert.assertEquals(bf.getBrowser(Browsers.epiphany).getID(), Browsers.epiphany);
        Assert.assertEquals(bf.getBrowser(Browsers.midori).getID(), Browsers.midori);
    }

    private void testBrowser(Browser browser) throws IOException {
        File defaultPluginDir = null;
        if (browser.getDefaultPluginExpectedLocation() != null) {
            defaultPluginDir = new File(browser.getDefaultPluginExpectedLocation());
        }
        if (defaultPluginDir != null) {
            Assert.assertTrue("browser's plugins  location should exist " + defaultPluginDir.toString() + " for " + browser.getID().toString(), defaultPluginDir.exists());
        }

        File userPluginDir = null;
        if (browser.getUserDefaultPluginExpectedLocation() != null) {
            userPluginDir = new File(browser.getUserDefaultPluginExpectedLocation());
        }
        // userPluginDir (~/.mozilla/plugins/) may not exist if user has not installed any Firefox plugins.

        File[] defaultPlugins = new File[0];
        if (defaultPluginDir != null && defaultPluginDir.isDirectory()) {
            defaultPlugins = defaultPluginDir.listFiles();
        }

        File[] userPlugins = new File[0];
        if (userPluginDir != null && userPluginDir.isDirectory()) {
            userPlugins = userPluginDir.listFiles();
        }

        Assert.assertTrue("at least one of browser's plugins  directory should contains at least one file didn't. For " + browser.getID().toString(), defaultPlugins.length + userPlugins.length > 0);

        defaultPlugins = new File[0];
        if (defaultPluginDir != null && defaultPluginDir.isDirectory()) {
            defaultPlugins = defaultPluginDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().equals(LinuxBrowser.DEFAULT_PLUGIN_NAME));
                }
            });
        }

        userPlugins = new File[0];
        if (userPluginDir != null && userPluginDir.isDirectory()) {
            userPlugins = userPluginDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().equals(LinuxBrowser.DEFAULT_PLUGIN_NAME));
                }
            });
        }

        Assert.assertTrue("browser's plugins  directories should contains exactly one  " + LinuxBrowser.DEFAULT_PLUGIN_NAME + ", but didnt for " + browser.getID().toString(),
                defaultPlugins.length + userPlugins.length == 1);
        String currentPath = server.getJavawsFile().getParentFile().getParentFile().getAbsolutePath();

        File[] plugins;
        if (defaultPlugins.length == 1) {
            plugins = defaultPlugins;
        } else {
            plugins = userPlugins;
        }
        String s = ServerAccess.getContentOfStream(new FileInputStream(plugins[0]), "ASCII");
        Assert.assertTrue("browser's plugins  shoud points to" + currentPath + ", but  didnt",
                s.contains(s));
    }
}
