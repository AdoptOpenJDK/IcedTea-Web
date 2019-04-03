/*
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

import static org.junit.Assert.assertTrue;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;

import org.junit.Test;

public class JSObjectFromEvalTest extends BrowserTest {

    private static final String END_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    private static final String JAVA_CREATE = "Java create\n";
    private static final String JS_CREATE = "JS create\n";
    private static final String JAVA_SET = "Java set\n";
    private static final String CORRECT_VALUE = "obj.test = 0";

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1198" })
    public void testJSObjectSetMemberIsSet() throws Exception {
        ProcessResult pr = server.executeBrowser("/JSObjectFromEval.html",
                AutoClose.CLOSE_ON_BOTH);

        String expectedJSCreateOutput = JS_CREATE + JAVA_SET + CORRECT_VALUE;
        String expectedJavaCreateOutput = JAVA_CREATE + JAVA_SET
                + CORRECT_VALUE;

        // No reason JS create should fail, this is mostly a sanity check:
        assertTrue("stdout should contain 'JS create [...] " + CORRECT_VALUE
                + "' but did not.", pr.stdout.contains(expectedJSCreateOutput));

        // Demonstrates PR1198:
        assertTrue("stdout should contain 'Java create [...] " + CORRECT_VALUE
                + "' but did not.",
                pr.stdout.contains(expectedJavaCreateOutput));

        // Make sure we got to the end of the script
        assertTrue("stdout should contain '" + END_STRING + "' but did not.",
                pr.stdout.contains(END_STRING));
    }

}
