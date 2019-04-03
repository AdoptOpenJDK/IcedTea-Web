/* 
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

import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.TestInBrowsers;

import net.sourceforge.jnlp.annotations.Bug;

import org.junit.Assert;

import net.sourceforge.jnlp.ServerAccess.AutoClose;

import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import net.sourceforge.jnlp.ProcessResult;

import org.junit.Test;

public class AppContextHasJNLPClassLoaderTest extends BrowserTest {

    private static final String MAIN_APP_CONTEXT_CLASSLOADER = "main-thread: app context classloader == JNLPClassLoader";
    private static final String MAIN_THREAD_CONTEXT_CLASSLOADER = "main-thread: thread context classloader == JNLPClassLoader";

    private static final String SWING_APP_CONTEXT_CLASSLOADER = "swing-thread: app context classloader == JNLPClassLoader";
    private static final String SWING_THREAD_CONTEXT_CLASSLOADER = "swing-thread: thread context classloader == JNLPClassLoader";

    private void assertHasJNLPClassLoaderAsContextClassloader(ProcessResult pr) {
        // This shouldn't fail even with PR1251
        // If the main thread does not have the right context classloader, something is quite wrong
        Assert.assertTrue("stdout should contains '" + MAIN_THREAD_CONTEXT_CLASSLOADER + "', but did not", pr.stdout.contains(MAIN_THREAD_CONTEXT_CLASSLOADER));

        // PR1251
        Assert.assertTrue("stdout should contains '" + MAIN_APP_CONTEXT_CLASSLOADER + "', but did not", pr.stdout.contains(MAIN_APP_CONTEXT_CLASSLOADER));
        Assert.assertTrue("stdout should contains '" + SWING_APP_CONTEXT_CLASSLOADER + "', but did not", pr.stdout.contains(SWING_APP_CONTEXT_CLASSLOADER));
        Assert.assertTrue("stdout should contains '" + SWING_THREAD_CONTEXT_CLASSLOADER + "', but did not", pr.stdout.contains(SWING_THREAD_CONTEXT_CLASSLOADER));
    }

    @Test
    @KnownToFail
    @Bug(id="PR1251")
    public void testJNLPApplicationAppContext() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless("/AppContextHasJNLPClassLoader.jnlp");
        assertHasJNLPClassLoaderAsContextClassloader(pr);
    }

    @Test
    @KnownToFail // EventQueue.invokeAndWait is broken in JNLP applets, see PR1253
    @Bug(id={"PR1251","PR1253"})
    public void testJNLPAppletAppContext() throws Exception {
        ProcessResult pr = server.executeJavaws("/AppContextHasJNLPClassLoaderForJNLPApplet.jnlp");
        assertHasJNLPClassLoaderAsContextClassloader(pr);
    }

    @Test
    @TestInBrowsers(testIn={Browsers.one})
    @KnownToFail
    @Bug(id="PR1251")
    public void testAppletAppContext() throws Exception {
        ProcessResult pr = server.executeBrowser("/AppContextHasJNLPClassLoader.html", AutoClose.CLOSE_ON_CORRECT_END);
        assertHasJNLPClassLoaderAsContextClassloader(pr);
    }
}
