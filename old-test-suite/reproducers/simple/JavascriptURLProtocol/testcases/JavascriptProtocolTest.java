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

import static org.junit.Assert.assertFalse;
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

public class JavascriptProtocolTest extends BrowserTest {

    private static final String END_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    private static void assertContains(String source, String message, String substring) {
        assertTrue(source + " should contain '" + substring + "' but did not!", 
                message.contains(substring));
    }
    private static void assertNotContains(String source, String message, String substring) {
        assertFalse(source + " should not contain '" + substring + "' but did!", 
                message.contains(substring));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1271" })
    public void testJavascriptProtocolFollowed() throws Exception {
        ProcessResult pr = server.executeBrowser("/JavascriptProtocol.html",
                AutoClose.CLOSE_ON_BOTH);
        assertNotContains("stdout", pr.stdout, "HasntRun");
        assertContains("stdout", pr.stdout, "Javascript URL string was evaluated.");
        assertContains("stdout", pr.stdout, "HasRun");
        assertContains("stdout", pr.stdout, END_STRING);
    }

}
