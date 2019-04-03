/* CheckPluginParamsTests.java
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

import java.io.IOException;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@Bug(id = "RH1273691")
public class CheckPluginParamsTests extends BrowserTest {

    private static DeploymentPropertiesModifier d;
    private static final String ID = "test.custom";

    @BeforeClass
    public static void setup() throws IOException {
        String value
                = " -D" + ID + "1=value1"
                + " -D" + ID + "2\\=value2=value2"
                + " -D" + ID + "3=value3\\=value3"
                + " -D" + ID + "4\\=value4\\\\=value4";
        d = new DeploymentPropertiesModifier();
        d.setProperties(DeploymentConfiguration.KEY_PLUGIN_JVM_ARGUMENTS, value);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        d.restoreProperties();
    }

    public void evaluateApplet(ProcessResult pr) {
        String s = pr.stdout;
        Assert.assertTrue(s.contains(ID + "1: value1"));
        Assert.assertTrue(s.contains(ID + "2: value2=value2"));
        Assert.assertTrue(s.contains(ID + "3: value3=value3"));
        Assert.assertTrue(s.contains(ID + "4: value4\\=value4"));
    }

    @Bug(id = "RH1273691")
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void CheckWebstartServices() throws Exception {
        ProcessResult pr = server.executeBrowser(null, "/CheckPluginParams1.html", new AutoOkClosingListener(), new AutoErrorClosingListener());
        evaluateApplet(pr);
    }
    
    @Bug(id = "RH1273691")
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void CheckPluginJNLPHServices() throws Exception {
        ProcessResult pr = server.executeBrowser(null, "/CheckPluginParams2.html", new AutoOkClosingListener(), new AutoErrorClosingListener());
        evaluateApplet(pr);
    }
}
