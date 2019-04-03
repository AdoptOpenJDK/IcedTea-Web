/* MultipleSignaturesTestTests.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;

import org.junit.Test;

@Bug(id = {"PR822"})
public class MultipleSignaturesTestTests  extends BrowserTest{

    public static final String GSJE = "Good simple javaws exapmle";
    public static final String launchExcDiffCerts =  "Fatal: Application Error: The JNLP application is not fully signed by a single cert.";
    public static final String accExcString = "java.security.AccessControlException: access denied";

    @Test
    @NeedsDisplay
    public void multipleSignaturesTestJnlpApplet() throws Exception {
        ProcessResult pr = server.executeJavaws("/MultipleSignaturesTest2.jnlp");
        String s = GSJE;
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn=Browsers.one)
    public void multipleSignaturesTestHtmlApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/MultipleSignaturesTest.html", AutoClose.CLOSE_ON_CORRECT_END);
        String s = GSJE;
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
        Assert.assertFalse("stderr should NOT contains `" + accExcString + "`, but did", pr.stderr.contains(accExcString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn=Browsers.one)
    @Bug(id={"PR822"})
    public void multipleSignaturesTestHtmlAppletUsesPermissions() throws Exception {
        ProcessResult pr = server.executeBrowser("/MultipleSignaturesTestUsesPermissions.html", AutoClose.CLOSE_ON_CORRECT_END);
        // This calls ReadPropertiesSigned with user.home, it is not easy to think of a pattern to match this
        // Instead we make sure _something_ was printed
        Assert.assertFalse("stdout should NOT be empty, but was", pr.stdout.isEmpty());
        Assert.assertFalse("stderr should NOT contains `" + accExcString + "`, but did", pr.stderr.contains(accExcString));
    }


    @Test
    public void multipleSignaturesTestJnlpApplication() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("/MultipleSignaturesTest1.jnlp");
        //well this is questionable - application is signed but is not requesting
        // permissions, but still usage of foreign code is allowed.
        String s = GSJE;
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
    }

    @Test
    public void multipleSignaturesTestJnlpApplicationRequesting() throws Exception {
        // This jar is fully signed - however a JNLP application requires that one of the signers signs everything
        ProcessResult pr = server.executeJavawsHeadless("/MultipleSignaturesTest1_requesting.jnlp");
        String s = GSJE;
        Assert.assertFalse("stdout should NOT contain `" + s + "`, but did", pr.stdout.contains(s));
        Assert.assertTrue("stderr should contain `" + launchExcDiffCerts + "`, but did not", pr.stderr.contains(launchExcDiffCerts));
    }
}
