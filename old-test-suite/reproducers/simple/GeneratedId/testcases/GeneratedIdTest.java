/* Copyright (C) 2012 Red Hat, Inc.

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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class GeneratedIdTest {

    private static final ServerAccess server = new ServerAccess();
    private static final String okBase = "0 - id: ";
    private static final String someId1 = "SomeId";
    private static final String someId2 = "AnotherId";
    private static final String okBase1 = okBase + someId1;
    private static final String okBase2 = okBase + someId2;
    private static final String baseName1 = "GeneratedId.jnlp";
    private static final String baseName1_noHref = "GeneratedIdNoHref.jnlp";
    private static final String baseName2 = "GeneratedId_1_tmp.jnlp";
    private static final String baseName2_noHref = "GeneratedIdNoHref_1_tmp.jnlp";

    public static File prepareChangedFileWithHref() throws IOException {
        File src = new File(server.getDir(), baseName1);
        File dest = new File(server.getDir(), baseName2);
        String srcJnlp = ServerAccess.getContentOfStream(new FileInputStream(src));
        ServerAccess.saveFile(srcJnlp.replace(someId1, someId2), dest);
        return dest;
    }

    public static File prepareChangedFileNoHref() throws IOException {
        File src = new File(server.getDir(), baseName1);
        File dest = new File(server.getDir(), baseName2_noHref);
        String srcJnlp = ServerAccess.getContentOfStream(new FileInputStream(src));
        ServerAccess.saveFile(srcJnlp.replace(someId1, someId2).replace("href=\"GeneratedId.jnlp\"", ""), dest);
        return dest;
    }

    public static File prepareCopiedFileNoHref() throws IOException {
        File src = new File(server.getDir(), baseName1);
        File dest = new File(server.getDir(), baseName1_noHref);
        String srcJnlp = ServerAccess.getContentOfStream(new FileInputStream(src));
        ServerAccess.saveFile(srcJnlp.replace("href=\"GeneratedId.jnlp\"", ""), dest);
        return dest;
    }

    @Test
    //have href
    //is local
    //should be redownloaded
    //href points to different file
    public void launchLocalChangedFileWithHref() throws Exception {
        File dest = prepareChangedFileWithHref();
        List<String> l = new ArrayList<String>(3);
        l.add(server.getJavawsLocation());
        l.add(ServerAccess.HEADLES_OPTION);
        l.add(dest.getAbsolutePath());
        ProcessResult pr = ServerAccess.executeProcess(l);
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    @Test
    //do not have href
    //is local
    //should NOT be redownloaded
    public void launchLocalChangedFileWithNoHref() throws Exception {
        File dest = prepareChangedFileNoHref();
        List<String> l = new ArrayList<String>(3);
        l.add(server.getJavawsLocation());
        l.add(ServerAccess.HEADLES_OPTION);
        l.add(dest.getAbsolutePath());
        ProcessResult pr = ServerAccess.executeProcess(l);
        Assert.assertTrue("Stdout should contain '" + okBase2 + "', but did not.", pr.stdout.contains(okBase2));
    }

    @Test
    //do have href
    //is local
    //should be redownloaded (how to verify!?!)
    public void launchLocalFileWithHref() throws Exception {
        File dest = new File(server.getDir(), baseName1);
        List<String> l = new ArrayList<String>(3);
        l.add(server.getJavawsLocation());
        l.add(ServerAccess.HEADLES_OPTION);
        l.add(dest.getAbsolutePath());
        ProcessResult pr = ServerAccess.executeProcess(l);
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    @Test
    //do not have href
    //is local
    //should NOT be redownloaded (how to verify!?!)
    public void launchLocalFileNoHref() throws Exception {
        File dest = prepareCopiedFileNoHref();
        List<String> l = new ArrayList<String>(3);
        l.add(server.getJavawsLocation());
        l.add(ServerAccess.HEADLES_OPTION);
        l.add(dest.getAbsolutePath());
        ProcessResult pr = ServerAccess.executeProcess(l);
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    @Test
    //remote
    //have href
    //should not be redownloaded (how to verify!?!)
    //href is same file
    public void launchRemoteFileWithHref() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("/" + baseName1);
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    //remote
    //have href
    //should be redownloaded as href is different file
    @Test
    public void launchRemoteChangedFileWithHref() throws Exception {
        File f = prepareChangedFileWithHref();
        ProcessResult pr = server.executeJavawsHeadless("/" + f.getName());
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    @Test
    //remote
    //have not href
    //should not be redownloaded (how to verify!?!)
    public void launchRemoteFileWithNoHref() throws Exception {
        File f = prepareCopiedFileNoHref();
        ProcessResult pr = server.executeJavawsHeadless("/" + f.getName());
        Assert.assertTrue("Stdout should contain '" + okBase1 + "', but did not.", pr.stdout.contains(okBase1));
    }

    //remote
    //have not href
    //should NOT be redownloaded
    @Test
    public void launchRemoteChangedFileWithNoHref() throws Exception {
        File f = prepareChangedFileNoHref();
        ProcessResult pr = server.executeJavawsHeadless("/" + f.getName());
        Assert.assertTrue("Stdout should contain '" + okBase2 + "', but did not.", pr.stdout.contains(okBase2));
    }
}
