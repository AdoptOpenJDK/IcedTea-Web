/* SplashUtils.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.adoptopenjdk.icedteaweb.client.parts.splashscreen;

import net.adoptopenjdk.icedteaweb.client.parts.splashscreen.impls.DefaultErrorSplashScreen2012;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static net.adoptopenjdk.icedteaweb.client.parts.splashscreen.SplashUtils.SplashReason.APPLET;
import static net.adoptopenjdk.icedteaweb.client.parts.splashscreen.SplashUtils.SplashReason.JAVAWS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ErrorSplashUtilsTest {

    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @Before
    public void setUp() {
        envVars.clear(System.getenv().keySet().toArray(new String[0]));
    }

    @Test
    public void determineCallerTest() {
        assertErrorSplashReason(false, APPLET);
        assertErrorSplashReason(true, JAVAWS);
    }

    private void assertErrorSplashReason(boolean isWebstartApplication, SplashUtils.SplashReason reason) {
        SplashUtilsTest.modifyIsWebstartApplicationRuntime(isWebstartApplication);
        final SplashPanel p2 = SplashUtils.getErrorSplashScreen(100, 100, null);
        assertThat(p2.getSplashReason(), is(reason));
    }

    @Test
    public void testGetErrorSplashScreen1() {
        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, null);
        assertThat(sa, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sa.getSplashReason(), is(APPLET));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, new Exception("oj"));
        assertThat(sw, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sw.getSplashReason(), is(JAVAWS));
    }

    @Test
    public void testGetErrorSplashScreen2() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        envVars.set(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.DEFAULT);

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, new Exception("oj"));
        assertThat(sa, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sa.getSplashReason(), is(APPLET));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, null);
        assertThat(sw, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sw.getSplashReason(), is(JAVAWS));
    }

    @Test
    public void testGetErrorSplashScreen3() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.NONE);
        envVars.set(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.DEFAULT);

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, null);
        assertThat(sa, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sa.getSplashReason(), is(APPLET));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, new Exception("oj"));
        assertThat(sw, is(nullValue()));
    }

    @Test
    public void testGetErrorSplashScreen4() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        envVars.set(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.NONE);

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, new Exception("oj"));
        assertThat(sa, is(nullValue()));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, new Exception("oj"));
        assertThat(sw, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sw.getSplashReason(), is(JAVAWS));
    }

    @Test
    public void testGetErrorSplashScreen5() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.NONE);
        envVars.set(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.NONE);

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, null);
        assertThat(sa, is(nullValue()));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, null);
        assertThat(sw, is(nullValue()));
    }

    @Test
    public void testGetErrorSplashScreen6() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        envVars.set(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, "fgdthyfjtuk");

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, new Exception("oj"));
        assertThat(sa, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sa.getSplashReason(), is(APPLET));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, new Exception("oj"));
        assertThat(sw, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sw.getSplashReason(), is(JAVAWS));
    }

    @Test
    public void testGetErrorSplashScreen7() {
        envVars.set(SplashUtils.ICEDTEA_WEB_SPLASH, "egtrutkyukl");

        SplashPanel sa = SplashUtils.getErrorSplashScreen(100, 100, APPLET, null);
        assertThat(sa, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sa.getSplashReason(), is(APPLET));

        SplashPanel sw = SplashUtils.getErrorSplashScreen(100, 100, JAVAWS, null);
        assertThat(sw, is(instanceOf(DefaultErrorSplashScreen2012.class)));
        assertThat(sw.getSplashReason(), is(JAVAWS));
    }
}
