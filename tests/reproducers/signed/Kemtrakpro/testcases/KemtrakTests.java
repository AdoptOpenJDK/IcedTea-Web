/* 
Copyright (C) 20121 Red Hat, Inc.

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

import java.util.AbstractMap;
import java.util.Arrays;
import net.sourceforge.jnlp.browsertesting.BrowserTest;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;

import org.junit.Assert;

import org.junit.Test;

@Bug(id = {"PR3350"})
/**
 * The issue is visible only in manifest attributes on, so this is manual
 * reproducer, automated in head
 */
public class KemtrakTests extends BrowserTest {

    private static final ServerAccess SERVER = new ServerAccess();

    @Bug(id = "PR3350")
    @Test
    public void KemtrakTest1() throws Exception {
        DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier dm
                = new DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier(
                        new AbstractMap.SimpleEntry<>(
                                DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK,
                                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL.name()),
                        new AbstractMap.SimpleEntry<>(
                                DeploymentConfiguration.KEY_SECURITY_LEVEL,
                                AppletSecurityLevel.ASK_UNSIGNED.name()));
        try {
            dm.setProperties();
            ProcessWrapper pw = new ProcessWrapper(SERVER.getJavawsLocation(),Arrays.asList(new String[]{ServerAccess.HEADLES_OPTION}), SERVER.getUrl("Kemtrak.jnlp"));
            pw.setWriter("YES\nYES\n");
            pw.addStdOutListener(new StringBasedClosingListener("jcalendar2"));
            ProcessResult pr = pw.execute();
            Assert.assertTrue("Stdout should contain Kemtrak1 but did not", pr.stdout.contains("Kemtrak1"));
            Assert.assertTrue("Stdout should contain Kemtrak2 but did not", pr.stdout.contains("Kemtrak2"));
            Assert.assertTrue("Stdout should contain jcalendar2 but did not", pr.stdout.contains("jcalendar2"));
        } finally {
            dm.restoreProperties();
        }
    }

}
