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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.naming.ConfigurationException;
import net.sourceforge.jnlp.PluginBridgeTest;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;

import org.junit.Test;

public class DeploymentConfigurationTest extends NoStdOutErrTest{

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

        assertTrue(target.size() != 0);
    }
    
    @Test
    public void testPersistedComments() throws ConfigurationException, IOException {
        final File f = File.createTempFile("proeprties", "withComments");
        f.deleteOnExit();
        FileUtils.saveFile("#commented1=val1\nproeprty2=val2\n#commented3=val3\nproeprty4=val4", f);
        DeploymentConfiguration dc = new DeploymentConfiguration(new InfrastructureFileDescriptor(){
            
            @Override
            public String getFullPath() {
                return f.getAbsolutePath();
            }
            
        });
        dc.load();
        Assert.assertEquals("val2", dc.getProperty("proeprty2"));
        Assert.assertEquals("val4", dc.getProperty("proeprty4"));
        Assert.assertEquals(null, dc.getProperty("commented1"));
        Assert.assertEquals(null, dc.getProperty("commented3"));
        
        dc.save();
        
        String s = FileUtils.loadFileAsString(f);
        Assert.assertTrue(s.contains("#"+DeploymentConfiguration.DEPLOYMENT_COMMENT));
        String date = new Date().toString().substring(0, 10); //every propertiews file have header and date by default
        Assert.assertTrue(s.contains("#"+date)); //check day part of date...
        Assert.assertTrue(s.contains("#commented1"));
        Assert.assertTrue(s.contains("proeprty2"));
        Assert.assertTrue(s.contains("#commented3"));
        Assert.assertTrue(s.contains("proeprty4"));
        Assert.assertTrue(s.contains("val1"));
        Assert.assertTrue(s.contains("val2"));
        Assert.assertTrue(s.contains("val3"));
        Assert.assertTrue(s.contains("val4"));
      
        }
    
    
    
    @Test
    public void testEnsurePersistedCommentsDoNotMultiplyHeaderAndDate() throws ConfigurationException, IOException {
        final File f = File.createTempFile("proeprties", "withComments");
        f.deleteOnExit();
        FileUtils.saveFile("#commented1=val1\nproeprty2=val2\n#commented3=val3\nproeprty4=val4", f);
        DeploymentConfiguration dc = new DeploymentConfiguration(new InfrastructureFileDescriptor() {

            @Override
            public String getFullPath() {
                return f.getAbsolutePath();
            }

        });
        String s = null;
        for (int x = 0; x < 10; x++) {
            dc.load();
            Assert.assertEquals("val2", dc.getProperty("proeprty2"));
            Assert.assertEquals("val4", dc.getProperty("proeprty4"));
            Assert.assertEquals(null, dc.getProperty("commented1"));
            Assert.assertEquals(null, dc.getProperty("commented3"));

            dc.save();

            s = FileUtils.loadFileAsString(f);
            for (int y = 0; x < x; x++) {
                //ensure salt
                Assert.assertTrue(s.contains("#id" + y + "id"));
            }
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, DeploymentConfiguration.DEPLOYMENT_COMMENT));
            String date = new Date().toString().substring(0, 10); //every propertiews file have header and date by default
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, date)); //check day part of date...
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "#commented1"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "proeprty2"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "#commented3"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "proeprty4"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "val1"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "val2"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "val3"));
            Assert.assertEquals(1, PluginBridgeTest.countOccurences(s, "val4"));
            //insert some salt to check if it really iterates
            FileUtils.saveFile(s + "\n#id" + x + "id", f);
        }
        //System.out.println(s);
    }

}
