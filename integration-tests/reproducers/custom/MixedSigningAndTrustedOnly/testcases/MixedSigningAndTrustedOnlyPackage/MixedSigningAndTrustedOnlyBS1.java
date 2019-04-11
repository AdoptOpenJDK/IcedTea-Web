/* 
 Copyright (C) 2013 Red Hat, Inc.

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
//package MixedSigningAndTrustedOnlyPackage;
package MixedSigningAndTrustedOnlyPackage;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static MixedSigningAndTrustedOnlyPackage.MixedSigningAndTrustedOnly.*;

/**
 *
 * Very simple tests. Basic behavior. jnlp and html with single signed jar, no
 * trusted-only manifest note - this file is declared to be in package, but
 * directory is wrong. Itw reproducers engine needs it like it, but your ide may
 * complain. Try to live with. Sorry
 */
public class MixedSigningAndTrustedOnlyBS1 extends BrowserTest {

    private static DeploymentPropertiesModifier q;

    @BeforeClass
    public static void setDeploymentProperties() throws IOException {
        q = setDeploymentPropertiesImpl();
    }

    @AfterClass
    public static void resetDeploymentProperties() throws IOException {
        q.restoreProperties();
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    public void htmlC1AllCommandsBS() throws Exception {
        String file = prepareFile(FileType.HTML, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeBrowser(file, AutoClose.CLOSE_ON_BOTH);
        assertAllOkC1(pr);
    }

    @NeedsDisplay
    @Test
    public void jnlpHtmlC1AllCommandsBS() throws Exception {
        String file = prepareFile(FileType.HTML, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(HTML, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC1(pr);
    }

    @Test
    public void jnlpAppC1AllCommandsBSnosec() throws Exception {
        String file = prepareFile(FileType.JNLP_APP, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(HEADLESS, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllButRestrictedC1(pr);
        assertLaunchException(pr);
        assertAccessDenied(pr);
    }

    @Test
    @NeedsDisplay
    public void jnlpAppletC1AllCommandsBSnosec() throws Exception {
        String file = prepareFile(FileType.JNLP_APPLET, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(verbose, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllButRestrictedC1(pr);
        assertAccessControlException(pr);
        assertAccessDenied(pr);
    }

    @Test
    public void jnlpAppC1AllCommandsBSsec() throws Exception {
        String file = prepareFile(FileType.JNLP_APP, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, true);
        ProcessResult pr = server.executeJavaws(HEADLESS, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC1(pr);
    }

    @Test
    @NeedsDisplay
    public void jnlpAppletC1AllCommandsBSsec() throws Exception {
        String file = prepareFile(FileType.JNLP_APPLET, C1, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, true);
        ProcessResult pr = server.executeJavaws(file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC1(pr);
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    public void htmlCc2llCommandsBS() throws Exception {
        String file = prepareFile(FileType.HTML, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeBrowser(file, AutoClose.CLOSE_ON_BOTH);
        assertAllOkC2(pr);
    }

    @NeedsDisplay
    @Test
    public void jnlphtmlCc2llCommandsBS() throws Exception {
        String file = prepareFile(FileType.HTML, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(HTML, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC2(pr);
    }

    @Test
    public void jnlpAppC2AllCommandsBSnosec() throws Exception {
        String file = prepareFile(FileType.JNLP_APP, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(HEADLESS, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllButRestrictedC2(pr);
        assertLaunchException(pr);
        assertAccessDenied(pr);
    }

    @Test
    @NeedsDisplay
    public void jnlpAppletC2AllCommandsBSnosec() throws Exception {
        String file = prepareFile(FileType.JNLP_APPLET, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, false);
        ProcessResult pr = server.executeJavaws(verbose, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllButRestrictedC2(pr);
        assertAccessControlException(pr);
        assertAccessDenied(pr);
    }

    @Test
    public void jnlpAppC2AllCommandsBSsec() throws Exception {
        String file = prepareFile(FileType.JNLP_APP, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, true);
        ProcessResult pr = server.executeJavaws(HEADLESS, file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC2(pr);
    }

    @Test
    @NeedsDisplay
    public void jnlpAppletC2AllCommandsBSsec() throws Exception {
        String file = prepareFile(FileType.JNLP_APPLET, C2, new Archives[]{BS},
                new String[]{COMMAND_C1_NORMAL, COMMAND_C2_NORMAL, COMMAND_C1_RESTRICT, COMMAND_C2_RESTRICT}, true);
        ProcessResult pr = server.executeJavaws(file, new AutoOkClosingListener(), new AutoErrorClosingListener());
        assertAllOkC2(pr);
    }

}
