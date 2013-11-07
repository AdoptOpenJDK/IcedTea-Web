/* InformationTitleVendorParserTest.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class InformationTitleVendorParserTest {

    private static ServerAccess server = new ServerAccess();

    public void runTest(String jnlpName, String exception) throws Exception {
        List<String> verbosed = Arrays.asList(new String[] { "-verbose" });
        ProcessResult pr=server.executeJavawsHeadless(verbosed, "/" + jnlpName + ".jnlp");
        String s1 = "Good simple javaws exapmle";
        Assert.assertFalse("test" + jnlpName + " stdout should not contain " + s1 + " but did.", pr.stdout.contains(s1));
        Assert.assertTrue("testForTitle stderr should contain " + exception + " but did not.", pr.stderr.contains(exception));
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer)0, pr.returnValue);
    }

    @Test
    public void testInformationeParser() throws Exception {
        runTest("InformationParser", "net.sourceforge.jnlp.MissingInformationException");
    }

    @Test
    public void testTitleParser() throws Exception {
        runTest("TitleParser", "net.sourceforge.jnlp.MissingTitleException");
    }
    @Test
    public void testVendorParser() throws Exception {
        runTest("VendorParser", "net.sourceforge.jnlp.MissingVendorException");
    }

    @Test
    public void testTitleVendorParser() throws Exception {
        // Note that the title message missing causes an immediate exception, regardless of Vendor.
        runTest("TitleVendorParser", "net.sourceforge.jnlp.MissingTitleException");
    }
}
