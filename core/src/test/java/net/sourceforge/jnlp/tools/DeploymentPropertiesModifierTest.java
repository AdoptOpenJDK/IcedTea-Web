/*
   Copyright (C) 2015 Red Hat, Inc.

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

package net.sourceforge.jnlp.tools;

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.adoptopenjdk.icedteaweb.testing.tools.DeploymentPropertiesModifier;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;

import static org.junit.Assert.assertEquals;

public class DeploymentPropertiesModifierTest {

    private File deploymentFile;

    @Test
    public void testSetProperties() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        String properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(0, properties.length());

        DeploymentPropertiesModifier dpm = new DeploymentPropertiesModifier(deploymentInfrastructure);
        dpm.setProperties(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        String setProperty = ConfigurationConstants.KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.toChars() + "\n";

        properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(setProperty, properties);

    }

    @Test
    public void testRestoreProperties() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        String properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(0, properties.length());

        DeploymentPropertiesModifier dpm = new DeploymentPropertiesModifier(deploymentInfrastructure);
        dpm.setProperties(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());

        String setProperty = ConfigurationConstants.KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.toChars() + "\n";
        properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(setProperty, properties);

        dpm.restoreProperties();
        properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(0, properties.length());

    }

    @Test(expected = IllegalStateException.class)
    public void testRestorePropertiesRequiresPropertiesSetFirst() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        DeploymentPropertiesModifier dpm = new DeploymentPropertiesModifier(deploymentInfrastructure);

        dpm.restoreProperties();
        String properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(0, properties.length());

    }

    @Test (expected = IllegalStateException.class)
    public void testUsingSameDeploymentPropertiesModifierThrowsException() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        DeploymentPropertiesModifier dpm = new DeploymentPropertiesModifier(deploymentInfrastructure);

        dpm.setProperties(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        try {
            dpm.setProperties(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString());
        } catch (IllegalStateException ise) {
            dpm.restoreProperties();
            throw new IllegalStateException();
        }

    }

    @Test
    public void testUsingDifferentDeploymentPropertiesModifier() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        DeploymentPropertiesModifier dpm1 = new DeploymentPropertiesModifier(deploymentInfrastructure);
        DeploymentPropertiesModifier dpm2 = new DeploymentPropertiesModifier(deploymentInfrastructure);

        dpm1.setProperties(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        dpm2.setProperties(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString());

        String contents = ConfigurationConstants.KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.toChars() + "\n" + ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK + "=" + ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString() + "\n";
        String properties = FileUtils.loadFileAsString(deploymentFile);
        assertEquals(contents, properties);

        dpm2.restoreProperties();
        dpm1.restoreProperties();
    }
    
     @Test
    public void testMultipleDeploymentPropertiesModifier() throws IOException {
        File tempUserFile = File.createTempFile("userDeploy", "");
        tempUserFile.deleteOnExit();

        String content = "a.b=12\nc.d=34\ne.f=56\ng.h=78\ni.j=90";
        BasicFileUtils.saveFile(content, tempUserFile);
        deploymentFile = tempUserFile;
        DummyInfrastructureFileDescriptor deploymentInfrastructure = new DummyInfrastructureFileDescriptor(deploymentFile);

        DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier dpm1
                = new DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier(deploymentInfrastructure,
                        new AbstractMap.SimpleEntry<>("c.d", "22"),
                        new AbstractMap.SimpleEntry<>("i.j", "44")
                );
        dpm1.setProperties();
        String propertiesChanged = FileUtils.loadFileAsString(deploymentFile);
        Assert.assertNotEquals(content, propertiesChanged);
        Assert.assertTrue(propertiesChanged.contains("12"));
        Assert.assertFalse(propertiesChanged.contains("34"));
        Assert.assertTrue(propertiesChanged.contains("22"));
        Assert.assertTrue(propertiesChanged.contains("56"));
        Assert.assertTrue(propertiesChanged.contains("78"));
        Assert.assertFalse(propertiesChanged.contains("90"));
        Assert.assertTrue(propertiesChanged.contains("44"));
        Assert.assertTrue(propertiesChanged.contains("a.b"));
        Assert.assertTrue(propertiesChanged.contains("c.d"));
        Assert.assertTrue(propertiesChanged.contains("e.f"));
        Assert.assertTrue(propertiesChanged.contains("g.h"));
        Assert.assertTrue(propertiesChanged.contains("i.j"));
        
        dpm1.restoreProperties();
        String propertiesRestored = FileUtils.loadFileAsString(deploymentFile);
        
        Assert.assertNotEquals(content, propertiesChanged);
        Assert.assertTrue(propertiesRestored.contains("12"));
        Assert.assertTrue(propertiesRestored.contains("34"));
        Assert.assertFalse(propertiesRestored.contains("22"));
        Assert.assertTrue(propertiesRestored.contains("56"));
        Assert.assertTrue(propertiesRestored.contains("78"));
        Assert.assertTrue(propertiesRestored.contains("90"));
        Assert.assertFalse(propertiesRestored.contains("44"));
        Assert.assertTrue(propertiesRestored.contains("a.b"));
        Assert.assertTrue(propertiesRestored.contains("c.d"));
        Assert.assertTrue(propertiesRestored.contains("e.f"));
        Assert.assertTrue(propertiesRestored.contains("g.h"));
        Assert.assertTrue(propertiesRestored.contains("i.j"));
        
        // /n at the end of last line may not matter
        assertEquals(content.trim(), propertiesRestored.trim());
    }

    private static class DummyInfrastructureFileDescriptor extends InfrastructureFileDescriptor {
        private final File file;

        private DummyInfrastructureFileDescriptor(File file) {
            super();
            this.file = file;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public String getFullPath() {
            return file.getAbsolutePath();
        }

    }
}
