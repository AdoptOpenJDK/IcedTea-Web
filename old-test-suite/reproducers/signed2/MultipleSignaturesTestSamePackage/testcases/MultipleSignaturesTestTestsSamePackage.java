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

import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;

import org.junit.Test;

public class MultipleSignaturesTestTestsSamePackage  extends BrowserTest{

    public static final String secExcRegex =  "(?s).*java.lang.SecurityException: .* signer information does not match signer information of other classes in the same package.*";
    public static final String launchExcDiffCerts =  "Fatal: Application Error: The JNLP application is not fully signed by a single cert.";
    public static final List<String> v = Arrays.asList(new String[] {ServerAccess.VERBOSE_OPTION});
    private static final String GSJE= "Good simple javaws exapmle";

    @Test
    @NeedsDisplay
    public void multipleSignaturesTestSamePackageJnlpApplet() throws Exception {
        ProcessResult pr = server.executeJavaws(v,"/MultipleSignaturesTest2_SamePackage.jnlp");
        String s = GSJE;
        Assert.assertFalse("stdout should NOT contains `"+s+"`, but did",pr.stdout.contains(s));
        String ss = "killer was started";
        Assert.assertTrue("stdout should contains `"+ss+"`, but did not",pr.stdout.contains(ss));
        String sss="Applet killing himself after 2000 ms of life";
        Assert.assertTrue("stdout should contains `"+sss+"`, but did not",pr.stdout.contains(sss));
//Applet in jnlp have exception consumed even in verbose mode. Howevwer at least foreign method is not invoken
//        String cc = "xception";
//        Assert.assertTrue("stderr should contains `" + cc + "`, but did not", pr.stderr.contains(cc));
//        Assert.assertTrue("stderr should match " + secExcRegex + "`, but did not", pr.stderr.matches(secExcRegex));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn=Browsers.one)
    public void multipleSignaturesTestSamePackageHtmlApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/MultipleSignaturesTest_SamePackage.html");
        String s = GSJE;
        Assert.assertFalse("stdout should NOT contains `"+s+"`, but did",pr.stdout.contains(s));
        String cc = "xception";
        Assert.assertTrue("stderr should contains `" + cc + "`, but did not", pr.stderr.contains(cc));
        Assert.assertTrue("stderr should match " + secExcRegex + "`, but did not", pr.stderr.matches(secExcRegex));
        String ss = "killer was started";
        Assert.assertTrue("stdout should contains `"+ss+"`, but did not",pr.stdout.contains(ss));
        String sss="Applet killing himself after 2000 ms of life";
        Assert.assertTrue("stdout should contains `"+sss+"`, but did not",pr.stdout.contains(sss));
    }


    @Test
    public void multipleSignaturesTestSamePackageJnlpApplication() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/MultipleSignaturesTest1_SamePackage.jnlp");
        String s = GSJE;
        Assert.assertFalse("stdout should NOT contains `"+s+"`, but did",pr.stdout.contains(s));
        String cc = "xception";
        Assert.assertTrue("stderr should contains `" + cc + "`, but did not", pr.stderr.contains(cc));
        Assert.assertTrue("stderr should match " + secExcRegex + "`, but did not", pr.stderr.matches(secExcRegex));
    }

    @Test
    public void multipleSignaturesTestSamePackageJnlpApplicationRequesting() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/MultipleSignaturesTest1_SamePackage_requesting.jnlp");
        String s = GSJE;
        Assert.assertFalse("stdout should NOT contain `"+s+"`, but did", pr.stdout.contains(s));
        Assert.assertTrue("stderr should contain `" + launchExcDiffCerts + "`, but did not", pr.stderr.contains(launchExcDiffCerts));
    }
   
}
