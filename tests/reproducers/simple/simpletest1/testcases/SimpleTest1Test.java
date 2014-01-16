/* SimpleTest1Test.java
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;

import org.junit.Test;

public class SimpleTest1Test {

    private static ServerAccess server = new ServerAccess();
    private static final List<String> strict = Arrays.asList(new String[]{"-strict", ServerAccess.VERBOSE_OPTION});

    private void checkLaunched(ProcessResult pr) {
        checkLaunched(pr, false);
    }

    private void checkLaunched(ProcessResult pr, boolean negate) {
        String s = "Good simple javaws exapmle";
        if (negate) {
            Assert.assertFalse("testSimpletest1lunchOk stdout should NOT contains " + s + " bud did", pr.stdout.contains(s));
        } else {
            Assert.assertTrue("testSimpletest1lunchOk stdout should contains " + s + " bud didn't", pr.stdout.contains(s));
        }
        String ss = "xception";
        if (negate) {
            Assert.assertTrue("testSimpletest1lunchOk stderr should contains " + ss + " but didn't", pr.stderr.contains(ss));
        } else {
            //disabled, unnecessary exceptions may occure
            //Assert.assertFalse("testSimpletest1lunchOk stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
        }
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    public void testSimpletest1lunchOk() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/simpletest1.jnlp");
        checkLaunched(pr);
    }

    @Test
    public void testSimpletest1lunchNotOkJnlpStrict() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(strict, "/simpletest1.jnlp");
        checkLaunched(pr, true);
    }

    @Test
    public void testSimpletest1lunchOkStrictJnlp() throws Exception {
        String originalResourceName = "simpletest1.jnlp";
        String newResourceName = "simpletest1_strict.jnlp";
        createStrictFile(originalResourceName, newResourceName, server.getUrl(""));
        ProcessResult pr = server.executeJavawsHeadless(null, "/" + newResourceName);
        checkLaunched(pr);
    }

    @Test
    public void testSimpletest1lunchOkStrictJnlpStrict() throws Exception {
        String originalResourceName = "simpletest1.jnlp";
        String newResourceName = "simpletest1_strict.jnlp";
        createStrictFile(originalResourceName, newResourceName, server.getUrl(""));
        ProcessResult pr = server.executeJavawsHeadless(strict, "/" + newResourceName);
        checkLaunched(pr);
    }

    private void createStrictFile(String originalResourceName, String newResourceName, URL codebase) throws MalformedURLException, IOException {
        String originalContent = ServerAccess.getContentOfStream(new FileInputStream(new File(server.getDir(), originalResourceName)));
        String nwContent1 = originalContent.replaceAll("href=\""+originalResourceName+"\"", "href=\""+newResourceName+"\"");
        String nwContent = nwContent1.replaceAll("codebase=\".\"", "codebase=\"" + codebase + "\"");
        ServerAccess.saveFile(nwContent, new File(server.getDir(), newResourceName));
    }
}
