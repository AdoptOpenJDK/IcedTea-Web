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
package net.sourceforge.jnlp.splashscreen;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jnlp.runtime.AppletEnvironment;
import net.sourceforge.jnlp.runtime.AppletInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.splashscreen.impls.*;
import org.junit.Assert;
import org.junit.Test;

public class SplashUtilsTest {



    @Test
    public void determineCallerTest() {
        modifyRuntime(false);
        SplashPanel p1 = SplashUtils.getSplashScreen(100, 100);
        Assert.assertEquals(SplashUtils.SplashReason.APPLET, p1.getSplashReason());
        modifyRuntime(true);
        SplashPanel p2 = SplashUtils.getSplashScreen(100, 100);
        Assert.assertEquals(SplashUtils.SplashReason.JAVAWS, p2.getSplashReason());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getEnvironment() throws Exception {
        Class<?>[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class<?> cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                return map;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void fakeEnvironment(Map<String, String> newenv) throws Exception {
        Class<?>[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class<?> cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.clear();
                map.putAll(newenv);
            }
        }
    }

    @Test
    public void testGetSplashScreen1() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sa.getSplashReason() == SplashUtils.SplashReason.APPLET);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sw.getSplashReason() == SplashUtils.SplashReason.JAVAWS);
        } finally {
            fakeEnvironment(original);
        }



    }

    @Test
    public void testGetSplashScreen2() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        fake1.put(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.DEFAULT);
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sa.getSplashReason() == SplashUtils.SplashReason.APPLET);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sw.getSplashReason() == SplashUtils.SplashReason.JAVAWS);
        } finally {
            fakeEnvironment(original);
        }


    }

    @Test
    public void testGetSplashScreen3() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.NONE);
        fake1.put(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.DEFAULT);
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sa.getSplashReason() == SplashUtils.SplashReason.APPLET);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw == null);
        } finally {
            fakeEnvironment(original);
        }



    }

    @Test
    public void testGetSplashScreen4() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        fake1.put(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.NONE);
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa == null);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sw.getSplashReason() == SplashUtils.SplashReason.JAVAWS);
        } finally {
            fakeEnvironment(original);
        }



    }

    @Test
    public void testGetSplashScreen5() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.NONE);
        fake1.put(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, SplashUtils.NONE);
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa == null);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw == null);
        } finally {
            fakeEnvironment(original);
        }

    }

    @Test
    public void testGetSplashScreen6() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, SplashUtils.DEFAULT);
        fake1.put(SplashUtils.ICEDTEA_WEB_PLUGIN_SPLASH, "fgdthyfjtuk");
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sa.getSplashReason() == SplashUtils.SplashReason.APPLET);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sw.getSplashReason() == SplashUtils.SplashReason.JAVAWS);
        } finally {
            fakeEnvironment(original);
        }


    }

    @Test
    public void testGetSplashScreen7() throws Exception {
        Map<String,String> fake1 = new HashMap<String,String>();
        fake1.put(SplashUtils.ICEDTEA_WEB_SPLASH, "egtrutkyukl");
        Map<String,String> original = getEnvironment();
        Assert.assertNotNull(original);
        try {
            fakeEnvironment(fake1);
            SplashPanel sa = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.APPLET);
            Assert.assertTrue(sa instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sa.getSplashReason() == SplashUtils.SplashReason.APPLET);
            SplashPanel sw = SplashUtils.getSplashScreen(100, 100, SplashUtils.SplashReason.JAVAWS);
            Assert.assertTrue(sw instanceof DefaultSplashScreen2012);
            Assert.assertTrue(sw.getSplashReason() == SplashUtils.SplashReason.JAVAWS);
        } finally {
            fakeEnvironment(original);
        }


    }

     static void modifyRuntime(boolean b) {
        try{
        setStatic(JNLPRuntime.class.getDeclaredField("isWebstartApplication"), b);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
     static void setStatic(Field field, Object newValue) throws Exception {
      field.setAccessible(true);
      field.set(null, newValue);
   }
     
    @Test
    public void assertNulsAreOkInShow() {
        SplashUtils.showError(null, (AppletEnvironment)null);
        SplashUtils.showError(null, (AppletInstance)null);
        SplashUtils.showError(null, (SplashController)null);
    }
     

}
