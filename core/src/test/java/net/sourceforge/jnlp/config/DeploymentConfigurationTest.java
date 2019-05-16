/* DeploymentConfigurationTest.java
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
package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.sourceforge.jnlp.PluginBridgeTest;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.adoptopenjdk.icedteaweb.testing.annotations.Remote;
import net.sourceforge.jnlp.config.DeploymentConfiguration.ConfigType;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class DeploymentConfigurationTest extends NoStdOutErrTest {

    @Test
    public void testLoad() throws ConfigurationException {
        DeploymentConfiguration config = new DeploymentConfiguration();

        // FIXME test this more exhaustively
        config.load();

        assertTrue(config.getAllPropertyNames().size() > 0);
    }

    @Test
    public void testInstallProperties() throws ConfigurationException {
        DeploymentConfiguration config = new DeploymentConfiguration();

        config.load();

        Properties target = new Properties();
        config.copyTo(target);

        assertTrue(!target.isEmpty());
    }

    @Test
    public void loadEmptyDeploymentPropertiesFile() throws ConfigurationException, IOException {
        final File f = File.createTempFile("emptyDeployment", "properties");
        f.deleteOnExit();

        final Map<String, Setting<String>> properties = DeploymentConfiguration.loadProperties(ConfigType.SYSTEM, f.toURI().toURL(), false);

        Assert.assertThat(properties.keySet(), is(empty()));
    }

    @Test
    public void loadNonExistentOptionalDeploymentPropertiesFile() throws ConfigurationException, IOException {
        final File f = new File("nonExistentDeployment.properties");
        final Map<String, Setting<String>> properties = DeploymentConfiguration.loadProperties(ConfigType.SYSTEM, f.toURI().toURL(), false);

        Assert.assertThat(properties.keySet(), is(empty()));
    }

    @Test(expected = ConfigurationException.class)
    public void loadNonExistentMandatoryDeploymentPropertiesFile() throws ConfigurationException, IOException {
        final File f = new File("nonExistentDeployment.properties");
        DeploymentConfiguration.loadProperties(ConfigType.SYSTEM, f.toURI().toURL(), true);
    }

    @Test
    public void testPersistedComments() throws ConfigurationException, IOException {
        final File f = File.createTempFile("properties", "withComments");
        f.deleteOnExit();
        BasicFileUtils.saveFile("#commented1=val1\nproperty2=val2\n#commented3=val3\nproperty4=val4", f);
        DeploymentConfiguration dc = new DeploymentConfiguration(new InfrastructureFileDescriptor() {

            @Override
            public String getFullPath() {
                return f.getAbsolutePath();
            }

        });
        dc.load();
        Assert.assertEquals("val2", dc.getProperty("property2"));
        Assert.assertEquals("val4", dc.getProperty("property4"));
        Assert.assertEquals(null, dc.getProperty("commented1"));
        Assert.assertEquals(null, dc.getProperty("commented3"));

        dc.save();

        String s = FileUtils.loadFileAsString(f);
        Assert.assertTrue(s.contains("#" + ConfigurationConstants.DEPLOYMENT_COMMENT));
        String date = new Date().toString().substring(0, 10); //every propertiews file have header and date by default
        Assert.assertTrue(s.contains("#" + date)); //check day part of date...
        Assert.assertTrue(s.contains("#commented1"));
        Assert.assertTrue(s.contains("property2"));
        Assert.assertTrue(s.contains("#commented3"));
        Assert.assertTrue(s.contains("property4"));
        Assert.assertTrue(s.contains("val1"));
        Assert.assertTrue(s.contains("val2"));
        Assert.assertTrue(s.contains("val3"));
        Assert.assertTrue(s.contains("val4"));

    }

    @Test
    @Ignore
    public void testEnsurePersistedCommentsDoNotMultiplyHeaderAndDate() throws ConfigurationException, IOException {
        final File f = File.createTempFile("properties", "withComments");
        f.deleteOnExit();
        BasicFileUtils.saveFile("#commented1=val1\nproperty2=val2\n#commented3=val3\nproperty4=val4", f);
        DeploymentConfiguration dc = new DeploymentConfiguration(new InfrastructureFileDescriptor() {

            @Override
            public String getFullPath() {
                return f.getAbsolutePath();
            }

        });
        String s = null;
        for (int x = 0; x < 10; x++) {
            dc.load();
            Assert.assertEquals("val2", dc.getProperty("property2"));
            Assert.assertEquals("val4", dc.getProperty("property4"));
            Assert.assertEquals(null, dc.getProperty("commented1"));
            Assert.assertEquals(null, dc.getProperty("commented3"));

            dc.save();

            s = FileUtils.loadFileAsString(f);
            for (int y = 0; x < x; x++) {
                //ensure salt
                Assert.assertTrue(s.contains("#id" + y + "id"));
            }
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, ConfigurationConstants.DEPLOYMENT_COMMENT));
            String date = new Date().toString().substring(0, 10); //every propertiews file have header and date by default
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, date)); //check day part of date...
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "#commented1"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "property2"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "#commented3"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "property4"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "val1"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "val2"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "val3"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurrences(s, "val4"));
            //insert some salt to check if it really iterates
            BasicFileUtils.saveFile(s + "\n#id" + x + "id", f);
        }
        //System.out.println(s);
    }

    @Test
    public void testCheckUrlFileOk() throws ConfigurationException, IOException {
        File f = File.createTempFile("itw", "checkUrlTest_ok");
        f.deleteOnExit();
        boolean is = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertTrue("File was supposed to exists", is);
        boolean is2 = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertTrue("File was supposed to exists", is2);
    }

    @Test
    public void testCheckUrlFileNotOk() throws ConfigurationException, IOException {
        File f = new File("/some/not/existing/file");
        boolean is = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertFalse("File was NOT supposed to exists", is);
        boolean is2 = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertFalse("File was NOT supposed to exists", is2);
    }

    @Test
    public void testCheckUrlFileOkNotOk() throws ConfigurationException, IOException {
        File f = File.createTempFile("itw", "checkUrlTest_not_ok");
        f.deleteOnExit();
        boolean is = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertTrue("File was supposed to exists", is);
        f.delete();
        boolean is2 = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertFalse("File was NOT supposed to exists", is2);
        f.createNewFile();
        f.deleteOnExit();
        boolean is3 = DeploymentConfiguration.checkUrl(f.toURI().toURL());
        Assert.assertTrue("File was supposed to exists", is3);
    }

    @Test
    @Ignore //Tests sometimes fails in case of proxy that redirects to a page in case of 404
    public void testCheckUrlRemoteNotOk() throws ConfigurationException, IOException {
        boolean is = DeploymentConfiguration.checkUrl(new URL("http://some.not/surely/existing.file"));
        Assert.assertFalse("File was supposed to not exists", is);
    }

    @Test
    public void testCheckUrlRemoteNotOk404_1() throws ConfigurationException, IOException {
        ServerLauncher server = ServerAccess.getIndependentInstance(System.getProperty(JAVA_IO_TMPDIR), ServerAccess.findFreePort());
        File f = File.createTempFile("itw", "checkUrlTest_404");
        f.delete();
        f.mkdir();
        f.deleteOnExit();
        try {
            URL u = new URL("http://localhost:" + server.getPort() + "/" + f.getName() + "/notexisting.file");
            boolean is = DeploymentConfiguration.checkUrl(u);
            Assert.assertFalse("File was not supposed to exists", is);
        } finally {
            server.stop();
        }
    }
    
    @Test
    @Remote
    public void testCheckUrlRemoteNotOk404_2() throws ConfigurationException, IOException {
        URL u = new URL("https://google.com/some/not/existingitw.file");
        boolean is = DeploymentConfiguration.checkUrl(u);
        Assert.assertFalse("File was not supposed to exists", is);
    }

    @Test
    public void testCheckUrlRemoteOk() throws ConfigurationException, IOException {
        ServerLauncher server = ServerAccess.getIndependentInstance(System.getProperty(JAVA_IO_TMPDIR), ServerAccess.findFreePort());
        try {
            File f = File.createTempFile("itw", "checkUrlTest_remote");
            f.deleteOnExit();
            URL u = new URL("http://localhost:" + server.getPort() + "/" + f.getName());
            boolean is = DeploymentConfiguration.checkUrl(u);
            Assert.assertTrue("File was supposed to exists", is);
            f.delete();
            //404_3
            boolean is2 = DeploymentConfiguration.checkUrl(u);
            Assert.assertFalse("File was NOT supposed to exists", is2);
            f.createNewFile();
            f.deleteOnExit();
            boolean is3 = DeploymentConfiguration.checkUrl(u);
            Assert.assertTrue("File was supposed to exists", is3);
        } finally {
            server.stop();
        }
    }
}
