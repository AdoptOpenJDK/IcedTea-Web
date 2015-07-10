
import java.io.IOException;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import static net.sourceforge.jnlp.browsertesting.BrowserTest.server;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/* AppletTest.java
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
public class SandboxSignedInvalidTest extends BrowserTest {

        private static final DeploymentPropertiesModifier dpm = new DeploymentPropertiesModifier();
        private static final StringBasedClosingListener aok = new AutoOkClosingListener();
        private static final StringBasedClosingListener aer = new AutoErrorClosingListener();
        private static final  String confirmation = "*** applet running ***";
        
        
        @BeforeClass
        public static void setDeploymentManifestPermissionReadingOnly() throws IOException{
            dpm.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString());
        }
        
        @AfterClass
        public static void restoreDeploymentProeprtiees() throws IOException{
            dpm.restoreProperties();
        }
        
    @Test
    //no security dialog
    //crash (jnlp dont have all-permnissions)
    public void javawsAllPermNoSecurity() throws Exception{
            ProcessResult p = server.executeJavawsHeadless("SandboxSignedInvalid.jnlp");
            Assert.assertFalse(p.stdout.contains(confirmation));
            Assert.assertFalse(p.stdout.contains(aok.getCondition()));
            Assert.assertTrue(p.stderr.contains(aer.getCondition()));
    }
    @Test
    //no security dialog
    //pass
    public void javawsAllPermAllSecurity() throws Exception{
            ProcessResult p = server.executeJavawsHeadless("SandboxSignedInvalid_security.jnlp");
            Assert.assertFalse(p.stdout.contains(confirmation));
            Assert.assertFalse(p.stdout.contains(aok.getCondition()));
            Assert.assertTrue(p.stderr.contains(aer.getCondition()));
    }
    
     @Test
    //no security dialog
    //crash (jnlp dont have all-permnissions)
    public void javawsAppletAllPermNoSecurity() throws Exception{
            ProcessResult p = server.executeJavawsHeadless("SandboxSignedInvalid_applet.jnlp", new AutoOkClosingListener(), new AutoErrorClosingListener());
            Assert.assertFalse(p.stdout.contains(confirmation));
            Assert.assertFalse(p.stdout.contains(aok.getCondition()));
            Assert.assertTrue(p.stderr.contains(aer.getCondition()));
    }
    @Test
    //no security dialog
    //pass
    public void javawsAppletAllPermAllSecurity() throws Exception{
            ProcessResult p = server.executeJavawsHeadless("SandboxSignedInvalid_applet_security.jnlp", new AutoOkClosingListener(), new AutoErrorClosingListener());
            Assert.assertFalse(p.stdout.contains(confirmation));
            Assert.assertFalse(p.stdout.contains(aok.getCondition()));
            Assert.assertTrue(p.stderr.contains(aer.getCondition()));
    }
    
     @Test
     @TestInBrowsers(testIn = Browsers.one)
    //no security dialog
    //pass
    public void appletAllPermAllSecurity() throws Exception{
        server.getBrowserLocation();
            ProcessResult p = server.executeBrowser("SandboxSignedInvalid.html", ServerAccess.AutoClose.CLOSE_ON_BOTH);
            Assert.assertFalse(p.stdout.contains(confirmation));
            Assert.assertFalse(p.stdout.contains(aok.getCondition()));
            Assert.assertTrue(p.stderr.contains(aer.getCondition()));
    }


}
