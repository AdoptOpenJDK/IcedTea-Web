/* AppletReadsInvalidJarTests.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

public class AppletReadsInvalidJarTests extends BrowserTest{

  
    static final String CORRECT_EXECUTION = "Program Executed Correctly.";
    static final String JNLP_EXPECTED_EXCEPTION = "ZipException";

    /*This SHOULD NOT execute the applet!*/
    @Test
    public void AppletJNLPTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("/AppletReadsInvalidJar.jnlp");

        Assert.assertFalse("AppletReadsInvalidJar stdout should NOT contain '" + CORRECT_EXECUTION + "', but did (applet should not have ran!).", pr.stdout.contains(CORRECT_EXECUTION));
        Assert.assertTrue("AppletReadsInvalidJar stderr should contain 'ZipException', but did not.", pr.stderr.contains(JNLP_EXPECTED_EXCEPTION));
    }

    /*This SHOULD execute the applet!*/
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void AppletInFirefoxTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/AppletReadsInvalidJar.html");

        Assert.assertTrue("AppletReadsInvalidJar stdout should contain '" + CORRECT_EXECUTION + "' but did not.", pr.stdout.contains(CORRECT_EXECUTION));
    }
}
