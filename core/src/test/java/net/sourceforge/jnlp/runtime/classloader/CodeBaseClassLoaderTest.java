/* CodeBaseClassLoaderTest.java
 Copyright (C) 2012 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.sourceforge.jnlp.runtime.classloader;

import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Ignore;

@NotThreadSafe
@Ignore
public class CodeBaseClassLoaderTest extends NoStdOutErrTest {

    //TODO: How to ahndle old Classloader tests?

   /* private static AppletSecurityLevel level;
    private static String macStatus;

    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        macStatus = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.toString());
        
    }

    @AfterClass
    public static void resetPermissions() {
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, level.toChars());
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, macStatus);
    }

    static void setStaticField(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        field.set(null, newValue);
    }

    private void setWSA() throws Exception {
    }

    private void setApplet() throws Exception {
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Bug(id = {"PR895",
        "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017626.html",
        "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017667.html"})
    @Test
    @Remote
    public void testClassResourceLoadSuccessCachingApplication() throws Exception {
        setWSA();
        //we are testing new resource not in cache
        testResourceCaching("net/sourceforge/jnlp/about/Main.class");
    }

    @Test
    @Remote
    @Ignore // fails from time to time...
    public void testClassResourceLoadSuccessCachingApplet() throws Exception {
        setApplet();
        //so new resource again not in cache
        testResourceCaching("net/sourceforge/jnlp/about/Main.class");
    }

    @Test
    @Remote
    @Ignore
    public void testResourceLoadSuccessCachingApplication() throws Exception {
        setWSA();
        //we are testing new resource not in cache
        testResourceCaching("net/sourceforge/jnlp/about/resources/about.html");
    }

    @Test
    @Remote
    @Ignore
    public void testResourceLoadSuccessCachingApplet() throws Exception {
        setApplet();
        //so new resource again not in cache
        testResourceCaching("net/sourceforge/jnlp/about/resources/about.html");
    }

    public void testResourceCaching(String r) throws Exception {
        testResourceCaching(r, true);
    }

    public void testResourceCaching(String r, boolean shouldExists) throws Exception {
        JNLPFile dummyJnlpFile = new DummyJNLPFile();

        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null);
        CodeBaseClassLoader classLoader = new CodeBaseClassLoader(new URL[]{DummyJNLPFile.JAR_URL, DummyJNLPFile.CODEBASE_URL}, parent);

        double level = 10;
        if (shouldExists) {
            //for found the "caching" is by internal logic.Always faster, but who knows how...
            //to keep the test stable keep the difference minimal
            level = 0.9;
        }
        long startTime, stopTime;

        startTime = System.nanoTime();
        URL u1 = classLoader.findResource(r);
        if (shouldExists) {
            Assert.assertNotNull(u1);
        } else {
            Assert.assertNull(u1);
        }
        stopTime = System.nanoTime();
        long timeOnFirstTry = stopTime - startTime;
        ServerAccess.logErrorReprint("" + timeOnFirstTry);

        startTime = System.nanoTime();
        URL u2 = classLoader.findResource(r);
        if (shouldExists) {
            Assert.assertNotNull(u1);
        } else {
            Assert.assertNull(u2);
        }
        stopTime = System.nanoTime();
        long timeOnSecondTry = stopTime - startTime;
        ServerAccess.logErrorReprint("" + timeOnSecondTry);

        assertTrue(timeOnSecondTry < (timeOnFirstTry / level));
    }

    @Bug(id = {"PR895",
        "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017626.html",
        "http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-March/017667.html"})
    @Test
    @Remote
    public void testResourceLoadFailureCachingApplication() throws Exception {
        setWSA();
        testResourceCaching("net/sourceforge/jnlp/about/Main_FOO_.class", false);
    }

    @Test
    public void testResourceLoadFailureCachingApplet() throws Exception {
        setApplet();
        testResourceCaching("net/sourceforge/jnlp/about/Main_FOO_.class", false);
    }

    @Test
    @Remote
    public void testParentClassLoaderIsAskedForClassesApplication() throws Exception {
        setWSA();
        testParentClassLoaderIsAskedForClasses();
    }

    @Test
    @Remote
    public void testParentClassLoaderIsAskedForClassesApplet() throws Exception {
        setApplet();
        testParentClassLoaderIsAskedForClasses();
    }

    public void testParentClassLoaderIsAskedForClasses() throws Exception {
        JNLPFile dummyJnlpFile = new DummyJNLPFile();

        final boolean[] parentWasInvoked = new boolean[1];

        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                parentWasInvoked[0] = true;
                throw new ClassNotFoundException(name);
            }
        };
        CodeBaseClassLoader classLoader = new CodeBaseClassLoader(new URL[]{DummyJNLPFile.JAR_URL, DummyJNLPFile.CODEBASE_URL}, parent);
        try {
            classLoader.findClass("foo");
            assertFalse("should not happen", true);
        } catch (ClassNotFoundException cnfe) { *//* ignore *//* }

        assertTrue(parentWasInvoked[0]);
    }

    @Test
    public void testNullFileSecurityDescApplication() throws Exception {
        setWSA();
        Exception ex = null;
        try {
            testNullFileSecurityDesc();
        } catch (Exception exx) {
            ex = exx;
        }
        Assert.assertTrue("was expected exception", ex != null);
        Assert.assertTrue("was expected " + NullJnlpFileException.class.getName(), ex instanceof NullJnlpFileException);
    }

    @Test
    @Remote
    public void testNullFileSecurityDescApplet() throws Exception {
        setApplet();
        Exception ex = null;
        try {
            testNullFileSecurityDesc();
        } catch (Exception exx) {
            ex = exx;
        }
        Assert.assertTrue("was expected exception", ex != null);
        Assert.assertTrue("was expected " + NullJnlpFileException.class.getName(), ex instanceof NullJnlpFileException);
    }

    public void testNullFileSecurityDesc() throws Exception {
        JNLPFile dummyJnlpFile = new DummyJNLPFile() {
            @Override
            public SecurityDesc getSecurity() {
                return new SecurityDesc(null, ApplicationEnvironment.SANDBOX, null);
            }
        };
        JNLPClassLoader parent = new JNLPClassLoader(dummyJnlpFile, null);

    }*/
}
