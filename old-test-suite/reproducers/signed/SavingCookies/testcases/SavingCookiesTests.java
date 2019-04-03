/* SavingCookieTests.java
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
import java.util.Collections;
import java.util.List;

import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;

import org.junit.Test;

public class SavingCookiesTests extends BrowserTest {

    static final String ENTERING_CHECK = "Entered CheckingCookies";
    static final String CHECKING_COMPLETION = "Finished CheckingCookies";
    static final String SAVING_COMPLETION = "Finished SavingCookies";
    private final static List<String> TRUSTALL = Collections.unmodifiableList(Arrays.asList(new String[] { "-Xtrustall" }));

    static class ParallelRun extends Thread {

        ParallelRun(String url, String completionString) {
            this.url = url;
            this.completionString = completionString;
            this.completed = false;
        }

        ProcessResult pr;
        private String url;
        private String completionString;
        volatile boolean completed;

        @Override
        public void run() {
            try {
                final ContentReaderListener stdoutListener = new ContentReaderListener() {
                    @Override
                    public void charReaded(char ch) {
                    }

                    @Override
                    public synchronized void lineReaded(String s) {
                        if (completionString != null && s.contains(completionString)) {
                            completed = true;
                        }
                    }
                };
                if (url.endsWith(".html")) {
                    pr = server.executeBrowser(url, stdoutListener, null);
                } else if (url.endsWith(".jnlp")) {
                    pr = server.executeJavawsHeadless(TRUSTALL, url, stdoutListener, null, null);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                completed = true;
            }
        }
    }

    final String COOKIE_SESSION_CHECK = "Found cookie: TEST=session";
    final String COOKIE_PERSISTENT_CHECK = "Found cookie: TEST=persistent";

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    public void AppletCheckCookieIsntSet() throws Exception {
        final String COOKIE_SANITY_CHECK = "Found cookie: TEST=";
        ProcessResult pr = server.executeBrowser("/CheckCookie.html");

        Assert.assertFalse("stdout should NOT contain '" + COOKIE_SANITY_CHECK + "' but did.", pr.stdout.contains(COOKIE_SANITY_CHECK));
        Assert.assertTrue("stdout should contain '" + CHECKING_COMPLETION + "' but did not.", pr.stdout.contains(CHECKING_COMPLETION));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @Bug(id = "PR588")
    public void AppletSessionCookieShowDoc() throws Exception {
        ProcessResult pr = server.executeBrowser("/SaveSessionCookieAndGotoCheck.html");

        Assert.assertTrue("stdout should contain '" + ENTERING_CHECK + "' but did not.", pr.stdout.contains(ENTERING_CHECK));
        Assert.assertTrue("stdout should contain '" + COOKIE_SESSION_CHECK + "' but did not.", pr.stdout.contains(COOKIE_SESSION_CHECK));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @Bug(id = "PR588")
    public void AppletSessionCookieParallel() throws Exception {
        ParallelRun save = new ParallelRun("/SaveSessionCookie.html", SAVING_COMPLETION);
        save.start();
        while (!save.completed) {
            Thread.sleep(100);
        }

        ProcessResult check = server.executeBrowser("/CheckCookie.html");
        save.join();

        Assert.assertTrue("stdout should contain '" + ENTERING_CHECK + "' but did not.", save.pr.stdout.contains(ENTERING_CHECK));
        //XXX: It is necessary to check save.pr's stdout, because it does not show up in 'check.stdout' for some reason
        Assert.assertTrue("stdout should contain '" + COOKIE_SESSION_CHECK + "' but did not.", save.pr.stdout.contains(COOKIE_SESSION_CHECK));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @Bug(id = "PR588")
    public void AppletSessionCookieSequential() throws Exception {
        ProcessResult save = server.executeBrowser("/SaveSessionCookie.html");
        ProcessResult check = server.executeBrowser("/CheckCookie.html");
        Assert.assertTrue("stdout should contain '" + ENTERING_CHECK + "' but did not.", check.stdout.contains(ENTERING_CHECK));
        //Session cookies should NOT be intact upon browser close and re-open
        Assert.assertFalse("stdout should NOT contain '" + COOKIE_SESSION_CHECK + "' but did.", check.stdout.contains(COOKIE_SESSION_CHECK));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @Bug(id = "PR588")
    public void AppletPersistentCookieShowDoc() throws Exception {
        ProcessResult pr = server.executeBrowser("/SavePersistentCookieAndGotoCheck.html");

        Assert.assertTrue("stdout should contain '" + ENTERING_CHECK + "' but did not.", pr.stdout.contains(ENTERING_CHECK));
        Assert.assertTrue("stdout should contain '" + COOKIE_PERSISTENT_CHECK + "' but did not.", pr.stdout.contains(COOKIE_PERSISTENT_CHECK));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @Bug(id = "PR588")
    public void AppletPersistentCookieSequential() throws Exception {
        ProcessResult save = server.executeBrowser("/SavePersistentCookie.html");
        //Use show doc to clear cookie afterwards
        ProcessResult check = server.executeBrowser("/CheckCookieAndGotoClear.html");
        Assert.assertTrue("stdout should contain '" + ENTERING_CHECK + "' but did not.", check.stdout.contains(ENTERING_CHECK));
        //Persistent cookies should be stored past this point
        Assert.assertTrue("stdout should contain '" + COOKIE_PERSISTENT_CHECK + "' but did not.", check.stdout.contains(COOKIE_PERSISTENT_CHECK));
    }

}
