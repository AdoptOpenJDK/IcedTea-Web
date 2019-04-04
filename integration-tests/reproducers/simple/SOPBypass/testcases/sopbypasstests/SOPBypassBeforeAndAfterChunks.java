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
package sopbypasstests;

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.AbstractMap;

public class SOPBypassBeforeAndAfterChunks extends BrowserTest {

    public static ServerLauncher serverA;
    public static ServerLauncher serverB;
    public static ServerLauncher serverC;
    private static final DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier mod = new DeploymentPropertiesModifier.MultipleDeploymentPropertiesModifier(
            new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.name()),
            new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.name()),
            //we need verbose output to catch PermissionDenied itw is printing
            new AbstractMap.SimpleEntry(DeploymentConfiguration.KEY_ENABLE_LOGGING, "true")
    );
    private static final DeploymentPropertiesModifier mod2 = new DeploymentPropertiesModifier();

    
    @Test
    public void SOPBypassBeforeAndAfterChunks(){
        //each testcase class must have test
    }
            
    @BeforeClass
    public static void setup() throws Exception {
        serverA = ServerAccess.getIndependentInstance();
        serverB = ServerAccess.getIndependentInstance();
        serverC = ServerAccess.getIndependentInstance();
        serverA.setServerNaming(ServerLauncher.ServerNaming.HOSTNAME);
        serverB.setServerNaming(ServerLauncher.ServerNaming.HOSTNAME);
        serverC.setServerNaming(ServerLauncher.ServerNaming.HOSTNAME);
        ServerAccess.getInstance().setServerNaming(ServerLauncher.ServerNaming.HOSTNAME);
        File file = mod.src.getFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        mod.setProperties();
    }

    @AfterClass
    public static void teardown() throws Exception {
        ServerAccess.getInstance().setServerNaming(ServerLauncher.ServerNaming.LOCALHOST); //must always!
        mod.restoreProperties();
        serverA.stop();
        serverB.stop();
        serverC.stop();

    }

    public static String getUnrelatedServer() throws MalformedURLException {
        return unrelatedInstance().getUrl().toExternalForm();

    }

    static ServerLauncher unrelatedInstance() {
        return serverA;
    }

    static ServerLauncher serverInstance() {
        //should be same as server from  BrowserTest
        return ServerAccess.getInstance();
    }
}
