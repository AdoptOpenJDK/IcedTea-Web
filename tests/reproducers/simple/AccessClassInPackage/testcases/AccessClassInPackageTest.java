/* AccessClassInPackageTest.java
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

import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.ProcessResult;
import org.junit.Assert;

import org.junit.Test;

public class AccessClassInPackageTest {

    private static ServerAccess server = new ServerAccess();
    private String[] files = {
        "AccessClassInPackageJAVAXJNLP.jnlp",
        "AccessClassInPackageSELF.jnlp",
        "AccessClassInPackageNETSF.jnlp",
        "AccessClassInPackageSUNSEC.jnlp"
    };
    private String[] filesSigned = {
        "AccessClassInPackageSignedJAVAXJNLP.jnlp",
        "AccessClassInPackageSignedSELF.jnlp",
        "AccessClassInPackageSignedNETSF.jnlp",
        "AccessClassInPackageSignedSUNSEC.jnlp"
    };
    private String[] badExceptions = {
        "accessClassInPackage.javax.jnlp.ServiceManager",
        "accessClassInPackage.AccessClassInPackage",
        "accessClassInPackage.net.sourceforge.jnlp",
        "accessClassInPackage.sun.security.internal.spec"
    };
    private String[] pass = {
        "javax.jnlp.ServiceManager",
        "AccessClassInPackage",
        "net.sourceforge.jnlp.Parser",
        "sun.security.internal.spec.TlsKeyMaterialSpec"
    };
    private static final List<String> xta = Arrays.asList(new String[]{"-Xtrustall"});

    private void testShouldFail(ServerAccess.ProcessResult pr, String s) {
        String c = "(?s).*java.security.AccessControlException.{0,5}access denied.{0,5}java.lang.RuntimePermission.{0,5}" + s + ".*";
        Assert.assertTrue("stderr should match `" + c + "`, but didn't ", pr.stderr.matches(c));
    }

    private void testShouldNOTFail(ServerAccess.ProcessResult pr, String s) {
        String c = "(?s).*java.security.AccessControlException.{0,5}access denied.{0,5}java.lang.RuntimePermission.{0,5}" + s + ".*";
        Assert.assertFalse("stderr should NOT match `" + c + "`, but did ", pr.stderr.matches(c));
    }

    private void commonPitfall(ProcessResult pr) {
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("AccessClassInPackageTestLunch1 should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    private void testShouldPass(ServerAccess.ProcessResult pr, String s) {
        String c = "Class was obtained: " + s;
        Assert.assertTrue("stdout should contains `" + c + "`, but didn't ", pr.stdout.contains(c));
    }

    private void testShouldNOTPass(ServerAccess.ProcessResult pr, String s) {
        String c = "Class was obtained: " + s;
        Assert.assertFalse("stdout should not contains `" + c + "`, but did ", pr.stdout.contains(c));
    }

    @Test
    public void AccessClassInPackageJAVAXJNLP() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + files[0]);
        commonPitfall(pr);
        testShouldPass(pr, pass[0]);
        testShouldNOTFail(pr, badExceptions[0]);
    }

    @Test
    public void AccessClassInPackageSELF() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + files[1]);
        commonPitfall(pr);
        testShouldPass(pr, pass[1]);
        testShouldNOTFail(pr, badExceptions[1]);
    }

    @Test
    public void AccessClassInPackageNETSF() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + files[2]);
        commonPitfall(pr);
        testShouldFail(pr, badExceptions[2]);
        testShouldNOTPass(pr, pass[2]);
    }

    @Test
    public void AccessClassInPackageSUNSEC() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + files[3]);
        commonPitfall(pr);
        commonPitfall(pr);
        testShouldFail(pr, badExceptions[3]);
        testShouldNOTPass(pr, pass[3]);
    }

    //now signed vaiants
    @Test
    public void AccessClassInPackageSignedJAVAXJNLP() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(xta, "/" + filesSigned[0]);
        commonPitfall(pr);
        testShouldPass(pr, pass[0]);
        testShouldNOTFail(pr, badExceptions[0]);
    }

    @Test
    public void AccessClassInPackageSignedSELF() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(xta, "/" + filesSigned[1]);
        commonPitfall(pr);
        testShouldPass(pr, pass[1]);
        testShouldNOTFail(pr, badExceptions[1]);
    }

    @Test
    public void AccessClassInPackageSignedNETSF() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(xta, "/" + filesSigned[2]);
        commonPitfall(pr);
        testShouldPass(pr, pass[2]);
        testShouldNOTFail(pr, badExceptions[2]);
    }

    @Test
    public void AccessClassInPackageSignedSUNSEC() throws Exception {
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(xta, "/" + filesSigned[3]);
        commonPitfall(pr);
        testShouldPass(pr, pass[3]);
        testShouldNOTFail(pr, badExceptions[3]);
    }

}
