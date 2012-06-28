/* CodeBaseClassLoaderTest.java
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

package net.sourceforge.jnlp.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.CodeBaseClassLoader;
import net.sourceforge.jnlp.annotations.Bug;

import org.junit.Test;

public class CodeBaseClassLoaderTest {

    @Bug(id={"PR895",
            "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017626.html",
            "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017667.html"})
    @Test
    public void testResourceLoadSuccessCaching() throws LaunchException, ClassNotFoundException, IOException, ParseException {
        final URL JAR_URL = new URL("http://icedtea.classpath.org/netx/about.jar");
        final URL CODEBASE_URL = new URL("http://icedtea.classpath.org/netx/");

        JNLPFile dummyJnlpFile = new JNLPFile() {
            @Override
            public ResourcesDesc getResources() {
                return new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
            }

            @Override
            public URL getCodeBase() {
                return CODEBASE_URL;
            }

            @Override
            public SecurityDesc getSecurity() {
                return new SecurityDesc(null, SecurityDesc.SANDBOX_PERMISSIONS, null);
            }
        };

        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null);
        CodeBaseClassLoader classLoader = new CodeBaseClassLoader(new URL[] { JAR_URL, CODEBASE_URL }, parent);

        long startTime, stopTime;

        startTime = System.nanoTime();
        classLoader.findResource("net/sourceforge/jnlp/about/Main.class");
        stopTime = System.nanoTime();
        long timeOnFirstTry = stopTime - startTime;
        ServerAccess.logErrorReprint(""+timeOnFirstTry);

        startTime = System.nanoTime();
        classLoader.findResource("net/sourceforge/jnlp/about/Main.class");
        stopTime = System.nanoTime();
        long timeOnSecondTry = stopTime - startTime;
        ServerAccess.logErrorReprint(""+timeOnSecondTry);

        assertTrue(timeOnSecondTry < (timeOnFirstTry / 10));
    }

    @Bug(id={"PR895",
            "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017626.html",
            "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017667.html"})
    @Test
    public void testResourceLoadFailureCaching() throws LaunchException, ClassNotFoundException, IOException, ParseException {
        final URL JAR_URL = new URL("http://icedtea.classpath.org/netx/about.jar");
        final URL CODEBASE_URL = new URL("http://icedtea.classpath.org/netx/");

        JNLPFile dummyJnlpFile = new JNLPFile() {
            @Override
            public ResourcesDesc getResources() {
                return new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
            }

            @Override
            public URL getCodeBase() {
                return CODEBASE_URL;
            }

            @Override
            public SecurityDesc getSecurity() {
                return new SecurityDesc(null, SecurityDesc.SANDBOX_PERMISSIONS, null);
            }
        };

        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null);
        CodeBaseClassLoader classLoader = new CodeBaseClassLoader(new URL[] { JAR_URL, CODEBASE_URL }, parent);

        long startTime, stopTime;

        startTime = System.nanoTime();
        classLoader.findResource("net/sourceforge/jnlp/about/Main_FOO_.class");
        stopTime = System.nanoTime();
        long timeOnFirstTry = stopTime - startTime;
        ServerAccess.logErrorReprint(""+timeOnFirstTry);

        startTime = System.nanoTime();
        classLoader.findResource("net/sourceforge/jnlp/about/Main_FOO_.class");
        stopTime = System.nanoTime();
        long timeOnSecondTry = stopTime - startTime;
        ServerAccess.logErrorReprint(""+timeOnSecondTry);

        assertTrue(timeOnSecondTry < (timeOnFirstTry / 10));
    }

    @Test
    public void testParentClassLoaderIsAskedForClasses() throws MalformedURLException, LaunchException {
        final URL JAR_URL = new URL("http://icedtea.classpath.org/netx/about.jar");
        final URL CODEBASE_URL = new URL("http://icedtea.classpath.org/netx/");

        JNLPFile dummyJnlpFile = new JNLPFile() {
            @Override
            public ResourcesDesc getResources() {
                return new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
            }

            @Override
            public URL getCodeBase() {
                return CODEBASE_URL;
            }

            @Override
            public SecurityDesc getSecurity() {
                return new SecurityDesc(null, SecurityDesc.SANDBOX_PERMISSIONS, null);
            }
        };

        final boolean[] parentWasInvoked = new boolean[1];

        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                parentWasInvoked[0] = true;
                throw new ClassNotFoundException(name);
            }
        };
        CodeBaseClassLoader classLoader = new CodeBaseClassLoader(new URL[] { JAR_URL, CODEBASE_URL }, parent);
        try {
            classLoader.findClass("foo");
            assertFalse("should not happen", true);
        } catch (ClassNotFoundException cnfe) { /* ignore */ }

        assertTrue(parentWasInvoked[0]);
    }
}
