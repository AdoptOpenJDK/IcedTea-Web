/* MixedSigningAppletHelper.java
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

package helper;
import signed.MixedSigningAppletSigned;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/* See also signed/MixedSigningAppletSigned */
public class MixedSigningAppletHelper {

    public static String help() {
        return "MixedSigningApplet Applet Running";
    }

    public static String helpDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return "MixedSigningApplet Applet Running";
            }
        });
    }

    public static String getProperty(String prop) {
        return System.getProperty(prop);
    }

    public static String getPropertyDoPrivileged(final String prop) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return getProperty(prop);
            }
        });
    }

    public static String getPropertyFromSignedJar(String prop) {
        try {
            Class<?> signedAppletClass = Class.forName("signed.MixedSigningAppletSigned");
            Method m = signedAppletClass.getMethod("getProperty", String.class);
            String result = (String) m.invoke(null, prop);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static String getPropertyFromSignedJarDoPrivileged(final String prop) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return getPropertyFromSignedJar(prop);
            }
        });
    }

    public static String attack() {
        try {
            Class<?> signedAppletClass = Class.forName("signed.MixedSigningAppletSigned");
            Method m = signedAppletClass.getMethod("getProperty", String.class);
            String result = (String) m.invoke(signedAppletClass.newInstance(), "user.home");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static String attackDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return new MixedSigningAppletSigned().testSignedReadPropertiesDoPrivileged();
            }
        });
    }

    public static String reflectiveAttack() {
        String result = null;
        try {
            Object signedApplet = Class.forName("signed.MixedSigningAppletSigned").newInstance();
            Method getProp = signedApplet.getClass().getMethod("calledByReflection");
            result = (String)getProp.invoke(signedApplet);
        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString();
        }
        return result;
    }

    public static String reflectiveAttackDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return reflectiveAttack();
            }
        });
    }
}
