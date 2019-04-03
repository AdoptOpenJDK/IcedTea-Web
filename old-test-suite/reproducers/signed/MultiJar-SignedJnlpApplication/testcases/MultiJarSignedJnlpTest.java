/*  MultiJarSignedJnlpTest.java
Copyright (C) 2012 Red Hat, Inc.

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
import java.util.Collections;
import java.util.List;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class MultiJarSignedJnlpTest {

    private static ServerAccess server = new ServerAccess();
    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[] { "-Xtrustall" }));
    private final String signedJnlpException = "net.sourceforge.jnlp.LaunchException: Fatal: Application Error: "
            + "The signed JNLP file did not match the launching JNLP file. Missing Resource: Signed Application "
            + "did not match launching JNLP File";

    @Test
    public void checkingForRequiredResources() throws Exception {
        //MainJarWithoutSignedJnlp.jnlp includes all three required jars
        String s = "Ending application without a Signed Jnlp";
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithoutSignedJnlp.jnlp");
        Assert.assertTrue("Could not locate the required resources required to run this test", pr.stdout.contains(s));
    }

    @Test
    public void mainJarMatchingSignedJnlpTemplate() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithMatchingSignedJnlpTemplate.jnlp");
        String s = "Ending application with signed application_template jnlp";
        Assert.assertTrue("Could not locate SignedJnlpTemplate class within MultiJar-SignedJnlpTemplate.jar", pr.stdout.contains(s));
        Assert.assertFalse(pr.stderr.contains(signedJnlpException));
    }

    @Test
    public void mainJarMatchingSignedJnlpApplication() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithMatchingSignedJnlpApplication.jnlp");
        String s = "Ending application with signed application jnlp";
        Assert.assertTrue("Could not locate SignedJnlpApplication class within MultiJar-SignedJnlpApplication.jar", pr.stdout.contains(s));
        Assert.assertFalse(pr.stderr.contains(signedJnlpException));
    }

    @Test
    public void mainJarWithUnmatchingSignedJnlpApplication() throws Exception {
        String s = "Ending application with signed application jnlp";
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithUnmatchingSignedJnlpApplication.jnlp");
        Assert.assertTrue("Stdout should contains " + signedJnlpException + " but did not", pr.stderr.contains(signedJnlpException));
        Assert.assertFalse( pr.stdout.contains(s));
    }

    @Test
    public void mainJarWithUnmatchingSignedJnlpTemplate() throws Exception {
        String s = "Ending application with signed application_template jnlp";
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithUnmatchingSignedJnlpTemplate.jnlp");
        Assert.assertTrue("Stdout should contains " + signedJnlpException + " but did not", pr.stderr.contains(signedJnlpException));
        Assert.assertFalse(pr.stdout.contains(s));
    }

    @Test
    public void mainJarWithoutSignedJnlp() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/MainJarWithoutSignedJnlp.jnlp");
        String s = "Ending application without a Signed Jnlp";
        Assert.assertTrue("Stdout should contains " + s + " but did not", pr.stdout.contains(s));
    }

}
