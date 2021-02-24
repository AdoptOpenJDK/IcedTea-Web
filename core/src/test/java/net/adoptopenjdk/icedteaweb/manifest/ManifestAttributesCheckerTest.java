/* 
 Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.manifest;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.DummySecurityDelegate;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.classloader.SecurityDelegate;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class ManifestAttributesCheckerTest {

    @Test
    public void stripDocbaseTest() throws Exception {
        tryTest("http://aaa.bb/ccc/file.html", "http://aaa.bb/ccc/");
        tryTest("http://aaa.bb/ccc/file.html/", "http://aaa.bb/ccc/file.html/");
        tryTest("http://aaa.bb/ccc/dir/", "http://aaa.bb/ccc/dir/");
        tryTest("http://aaa.bb/ccc/dir", "http://aaa.bb/ccc/");
        tryTest("http://aaa.bb/ccc/", "http://aaa.bb/ccc/");
        tryTest("http://aaa.bb/ccc", "http://aaa.bb/");
        tryTest("http://aaa.bb/", "http://aaa.bb/");
        tryTest("http://aaa.bb", "http://aaa.bb");
    }

    @Test
    public void checkAllCheckAlacTest() throws LaunchException, MalformedURLException {
        // can't check failing cases as ManifestAttributesChecker intertwines logic & GUI
        URL codebase = new URL("http://aaa/bb/");
        URL jar1 = new URL("http://aaa/bb/a.jar");
        URL jar2 = new URL("http://aaa/bb/lib/a.jar");
        JNLPFile file = new DummyJNLPFileWithJar(codebase, jar1, jar2);
        SecurityDesc security = new SecurityDesc(file, AppletPermissionLevel.ALL,SecurityDesc.ALL_PERMISSIONS, codebase);
        SecurityDelegate securityDelegate = new DummySecurityDelegate();
        ManifestAttributesChecker checker = new ManifestAttributesChecker(security, file, JNLPClassLoader.SigningState.FULL, securityDelegate);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALAC.name());
        checker.checkAll();
    }

    @Test
    public void checkAllPermissionsNoJarsTest() throws LaunchException, MalformedURLException {
        // checking permissions on a JNLP that contains no jars should not fail 
        URL codebase = new URL("http://aaa/bb/");
        JNLPFile file = new DummyJNLPFileWithJar(codebase);
        SecurityDesc security = new SecurityDesc(file, AppletPermissionLevel.ALL,SecurityDesc.ALL_PERMISSIONS, codebase);
        SecurityDelegate securityDelegate = new DummySecurityDelegate();
        ManifestAttributesChecker checker = new ManifestAttributesChecker(security, file, JNLPClassLoader.SigningState.FULL, securityDelegate);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.name());
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.DENY_UNSIGNED.name());
        checker.checkAll();
    }
    
    @Test(expected = LaunchException.class)
    public void checkAllPermissionsFailTest() throws LaunchException, MalformedURLException {
    	// checking permissions on a JNLP that contains a jar without the permissions attribute when ALL_PERMISSIONS is requested should fail
    	URL codebase = new URL("http://aaa/bb/");
    	URL jar1 = new URL("http://aaa/bb/a.jar");
    	JNLPFile file = new DummyJNLPFileWithJar(codebase, jar1);
    	SecurityDesc security = new SecurityDesc(file, AppletPermissionLevel.ALL,SecurityDesc.ALL_PERMISSIONS, codebase);
    	SecurityDelegate securityDelegate = new DummySecurityDelegate();
    	ManifestAttributesChecker checker = new ManifestAttributesChecker(security, file, JNLPClassLoader.SigningState.FULL, securityDelegate);
    	JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.name());
    	JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.DENY_UNSIGNED.name());
    	checker.checkAll();
    }
        
    private static void tryTest(String src, String expected) throws MalformedURLException {
        URL s = new URL(src);
        URL q = ManifestAttributesChecker.stripDocbase(s);
        //junit is failing for me on url.equals(url)...
        Assert.assertEquals(expected, q.toExternalForm());
    }

}
