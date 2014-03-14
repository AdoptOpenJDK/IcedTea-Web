/* SignedAppletExternalMainClassTest.java
Copyright (C) 2013 Red Hat, Inc.

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
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/* see also signed/SignedAppletCodebaseLoading which is related */
public class SignedAppletExternalMainClassTest extends BrowserTest {

    private static final String RUNNING_STRING = "SignedAppletExternalMainClass Applet Running";
    private static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    @Bug(id="PR1513")
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testSignedAppletWithExternalMainClassLaunch() throws Exception {
        ProcessResult pr = server.executeBrowser("SignedAppletExternalMainClass.html", AutoClose.CLOSE_ON_CORRECT_END);
        assertProperStart(pr);
        assertCloseString(pr);
    }

    private static void assertProperStart(ProcessResult pr) {
        assertTrue("applet did not initialize", pr.stdout.contains(RUNNING_STRING));
    }

    private static void assertCloseString(ProcessResult pr) {
        assertTrue("applet should have closed normally", pr.stdout.contains(CLOSE_STRING));
    }
}
