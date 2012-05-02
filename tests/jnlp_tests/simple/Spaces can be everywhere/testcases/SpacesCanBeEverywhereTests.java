/* SpacesCanBeEverywhereTests.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import org.junit.Assert;

import org.junit.Test;

@Bug(id={"http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2011-October/016127.html","PR804","PR811"})
public class SpacesCanBeEverywhereTests {

    private static ServerAccess server = new ServerAccess();

    @Bug(id={"PR811","http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2011-October/016144.html"})
    @Test
    public void SpacesCanBeEverywhereRemoteTests1() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereRemoteTests1 request");
        System.err.println("connecting SpacesCanBeEverywhereRemoteTests1 request");
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere1.jnlp");
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests2() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereRemoteTests2 request");
        System.err.println("connecting SpacesCanBeEverywhereRemoteTests2 request");
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere2.jnlp");
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s="Spaces can be everywhere.jsr was launched correctly";
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests3() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereRemoteTests3 request");
        System.err.println("connecting SpacesCanBeEverywhereRemoteTests3 request");
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/SpacesCanBeEverywhere1.jnlp");
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s="Spaces can be everywhere.jsr was launched correctly";
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }


    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests1() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereLocalTests1 request");
        System.err.println("connecting SpacesCanBeEverywhereLocalTests1 request");
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("Spaces can be everywhere1.jnlp");
        ServerAccess.ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests2() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereLocalTests2 request");
        System.err.println("connecting SpacesCanBeEverywhereLocalTests2 request");
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("Spaces can be everywhere2.jnlp");
        ServerAccess.ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s="Spaces can be everywhere.jsr was launched correctly";
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests3() throws Exception {
        System.out.println("connecting SpacesCanBeEverywhereLocalTests3 request");
        System.err.println("connecting SpacesCanBeEverywhereLocalTests3 request");
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("SpacesCanBeEverywhere1.jnlp");
        ServerAccess.ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String s="Spaces can be everywhere.jsr was launched correctly";
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }
}
