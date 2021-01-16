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

import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.DummySecurityDelegate;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALAC;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK;

public class ManifestAttributesCheckerTest {

    @Test
    public void checkAllCheckAlacTest() throws LaunchException, MalformedURLException {
        // can't check failing cases as ManifestAttributesChecker intertwines logic & GUI
        URL codebase = new URL("http://aaa/bb/");
        URL jar1 = new URL("http://aaa/bb/a.jar");
        URL jar2 = new URL("http://aaa/bb/lib/a.jar");
        JNLPFile file = new DummyJNLPFileWithJar(codebase, jar1, jar2);
        SecurityDesc security = new SecurityDesc(ApplicationEnvironment.ALL);
        SecurityDelegate securityDelegate = new DummySecurityDelegate();
        ManifestAttributesChecker checker = new ManifestAttributesChecker(file, true, null);
        JNLPRuntime.getConfiguration().setProperty(KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ALAC.name());
        checker.checkAll();
    }

}
