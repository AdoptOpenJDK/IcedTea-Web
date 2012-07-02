/* ServerAccessTest.java
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
package net.sourceforge.jnlp;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * This class provides access to virtual server and stuff around.
 * It can find unoccupied port, start server, provides its singleton instantiation, lunch parallel instantiations,
 * read location of installed (tested javaws) see javaws.build.bin java property,
 * location of server www root on file system (see test.server.dir java property),
 * stubs for lunching javaws and for locating resources and read resources.
 *
 * It can also execute processes with timeout (@see PROCESS_TIMEOUT) (used during lunching javaws)
 * Some protected apis are exported because public classes in this package are put to be tested by makefile.
 *
 * There are included test cases which show some basic usages.
 *
 *
 */
public class ServerAccessTest {

    ServerAccess serverAccess = new ServerAccess();

    @Test
    public void testsProcessResultFiltering() throws Exception {
        ProcessResult pn = new ProcessResult(null, null, null, true, 0, null);
        Assert.assertNull(pn.notFilteredStdout);
        Assert.assertNull(pn.stdout);
        Assert.assertNull(pn.stderr);
        String fakeOut2 =
                "EMMA: processing instrumentation path ...\n"
                + "EMMA: package [net.sourceforge.filebrowser] contains classes [ArrayOfString] without full debug info\n"
                + "EMMA: instrumentation path processed in 1407 ms\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "EMMA: The intruder!\n"
                + "test stage 3\n"
                + "EMMA: [45 class(es) instrumented, 13 resource(s) copied]\n"
                + "EMMA: metadata merged into [icedtea-web/inc] {in 105 ms}\n"
                + "EMMA: processing instrumentation path ...";
        String filteredOut2 =
                "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n";
        ProcessResult p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0, null);
        Assert.assertEquals(p2.notFilteredStdout, fakeOut2);
        Assert.assertEquals(p2.stdout, filteredOut2);
        Assert.assertEquals(p2.stderr, fakeOut2);
        fakeOut2 += "\n";
        p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0, null);
        Assert.assertEquals(p2.notFilteredStdout, fakeOut2);
        Assert.assertEquals(p2.stdout, filteredOut2);
        Assert.assertEquals(p2.stderr, fakeOut2);
        String fakeOut =
                "test string\n"
                + "EMMA: processing instrumentation path ...\n"
                + "EMMA: package [net.sourceforge.filebrowser] contains classes [ArrayOfString] without full debug info\n"
                + "EMMA: instrumentation path processed in 1407 ms\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n"
                + "EMMA: [45 class(es) instrumented, 13 resource(s) copied]\n"
                + "EMMA: metadata merged into [icedtea-web/inc] {in 105 ms}\n"
                + "EMMA: processing instrumentation path ...\n"
                + "test ends";
        String filteredOut =
                "test string\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n"
                + "test ends";
        ProcessResult p = new ProcessResult(fakeOut, fakeOut, null, true, 0, null);
        Assert.assertEquals(p.notFilteredStdout, fakeOut);
        Assert.assertEquals(p.stdout, filteredOut);
        Assert.assertEquals(p.stderr, fakeOut);
        fakeOut += "\n";
        filteredOut += "\n";
        p = new ProcessResult(fakeOut, fakeOut, null, true, 0, null);
        Assert.assertEquals(p.notFilteredStdout, fakeOut);
        Assert.assertEquals(p.stdout, filteredOut);
        Assert.assertEquals(p.stderr, fakeOut);
    }

    @Test
    public void ensureJavaws() throws Exception {
        String javawsValue = serverAccess.getJavawsLocation();
        Assert.assertNotNull(javawsValue);
        Assert.assertTrue(javawsValue.trim().length() > 2);
        File javawsFile = serverAccess.getJavawsFile();
        Assert.assertTrue(javawsFile.exists());
        Assert.assertFalse(javawsFile.isDirectory());
    }

    @Test
    public void ensureServer() throws Exception {
        ServerLauncher server = ServerAccess.getInstance();
        Assert.assertNotNull(server.getPort());
        Assert.assertNotNull(server.getDir());
        Assert.assertTrue(server.getPort() > 999);
        Assert.assertTrue(server.getDir().toString().trim().length() > 2);

        Assert.assertTrue(server.getDir().exists());
        Assert.assertTrue(server.getDir().isDirectory());

        File portFile = new File(server.getDir(), "server.port");
        File dirFile = new File(server.getDir(), "server.dir");

        ServerAccess.saveFile(server.getDir().getAbsolutePath(), dirFile);
        ServerAccess.saveFile(server.getPort().toString(), portFile);
        ServerAccess.saveFile(server.getPort().toString(), portFile);

        Assert.assertTrue(portFile.exists());
        Assert.assertTrue(dirFile.exists());
        Assert.assertTrue(server.getDir().listFiles().length > 1);

        String portFileContent = ServerAccess.getContentOfStream(new FileInputStream(portFile));
        String dirFileContent = ServerAccess.getContentOfStream(new FileInputStream(dirFile));

        URL portUrl = new URL("http", "localhost", server.getPort(), "/server.port");
        URL dirUrl = new URL("http", "localhost", server.getPort(), "/server.dir");

        String portUrlContent = ServerAccess.getContentOfStream(portUrl.openConnection().getInputStream());
        String dirUrlContent = ServerAccess.getContentOfStream(dirUrl.openConnection().getInputStream());

        Assert.assertEquals(portUrlContent.trim(), portFileContent.trim());
        Assert.assertEquals(dirUrlContent.trim(), dirFileContent.trim());
        Assert.assertEquals(new File(dirUrlContent.trim()), server.getDir());
        Assert.assertEquals(new Integer(portUrlContent.trim()), server.getPort());

        URL fastUrl = new URL("http", "localhost", server.getPort(), "/simpletest1.jnlp");
        URL slowUrl = new URL("http", "localhost", server.getPort(), "/XslowXsimpletest1.jnlp");

        String fastUrlcontent = ServerAccess.getContentOfStream(fastUrl.openConnection().getInputStream());
        String slowUrlContent = ServerAccess.getContentOfStream(slowUrl.openConnection().getInputStream());
        Assert.assertEquals(fastUrlcontent, slowUrlContent);

    }

    @Test
    public void splitArrayTest0() throws Exception {
        byte[] b = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        byte[][] bb = TinyHttpdImpl.splitArray(b, 3);
        //printArrays(bb);
        byte[] b1 = {1, 2, 3, 4, 5};
        byte[] b2 = {6, 7, 8, 9, 10};
        byte[] b3 = {11, 12, 13, 14};
        Assert.assertEquals(3, bb.length);
        Assert.assertArrayEquals(b1, bb[0]);
        Assert.assertArrayEquals(b2, bb[1]);
        Assert.assertArrayEquals(b3, bb[2]);
    }

    @Test
    public void splitArrayTest1() throws Exception {
        byte[] b = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        byte[][] bb = TinyHttpdImpl.splitArray(b, 3);
        //printArrays(bb);
        byte[] b1 = {1, 2, 3, 4, 5};
        byte[] b2 = {6, 7, 8, 9, 10};
        byte[] b3 = {11, 12, 13};
        Assert.assertEquals(3, bb.length);
        Assert.assertArrayEquals(b1, bb[0]);
        Assert.assertArrayEquals(b2, bb[1]);
        Assert.assertArrayEquals(b3, bb[2]);
    }

    @Test
    public void splitArrayTest2() throws Exception {
        byte[] b = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        byte[][] bb = TinyHttpdImpl.splitArray(b, 3);
        //printArrays(bb);
        byte[] b1 = {1, 2, 3, 4};
        byte[] b2 = {5, 6, 7, 8};
        byte[] b3 = {9, 10, 11, 12};
        Assert.assertEquals(3, bb.length);
        Assert.assertArrayEquals(b1, bb[0]);
        Assert.assertArrayEquals(b2, bb[1]);
        Assert.assertArrayEquals(b3, bb[2]);
    }

    private void printArrays(byte[][] bb) {
        System.out.println("[][] l=" + bb.length);
        for (int i = 0; i < bb.length; i++) {
            byte[] bs = bb[i];
            System.out.println(i + ": l=" + bs.length);
            for (int j = 0; j < bs.length; j++) {
                byte c = bs[j];
                System.out.print(" " + j + ":" + c + " ");
            }
            System.out.println("");
        }
    }
}
