/* MixedSigningAppletSigned.java
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

package com.redhat.mixedsigning.signed;
import java.applet.Applet;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/* See also simple/MixedSigningApplet */
public class MixedSigningAppletSigned extends Applet {

    @Override
    public void init() {
        System.out.println("MixedSigningAppletSigned applet started. testName: " + getParameter("testName"));
        Method m = null;
        try {
            m = this.getClass().getMethod(getParameter("testName"));
            final String result = (String) m.invoke(this);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("*** APPLET FINISHED ***");
        }
    }

    public String testNonPrivilegedAction() {
        return new HelperMethodCall<String>().method("help").call();
    }

    // Should succeed
    public String testSignedReadProperties() {
        return System.getProperty("user.home");
    }

    // Should result in AccessControlException
    public String testUnsignedReadProperties() {
        return new HelperMethodCall<String>().type(String.class).method("getProperty").arg("user.home").call();
    }

    // Should result in AccessControlException
    public String testSignedExportPropertiesToUnsigned() {
        return new HelperMethodCall<String>().type(String.class).method("getPropertyFromSignedJar").arg("user.home").call();
    }

    // Should result in AccessControlException
    public String testUnsignedAttacksSigned() {
        return new HelperMethodCall<String>().method("attack").call();
    }

    // Should result in InvocationTargetException (due to AccessControlException)
    public String testUnsignedReflectionAttack() {
        return new HelperMethodCall<String>().method("reflectiveAttack").call();
    }

    public String calledByReflection() {
        return System.getProperty("user.home");
    }

    public static String getProperty(String prop) {
        return System.getProperty(prop);
    }

    private static class HelperMethodCall<T> {

        private String methodName;
        private final List<Class<?>> methodSignature;
        private final List<String> args;

        public HelperMethodCall() {
            methodSignature = new ArrayList<Class<?>>();
            args = new ArrayList<String>();
        }

        public HelperMethodCall<T> method(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public HelperMethodCall<T> type(Class<?> methodSignature) {
            this.methodSignature.add(methodSignature);
            return this;
        }

        public HelperMethodCall<T> arg(String arg) {
            this.args.add(arg);
            return this;
        }

        public T call() {
            try {
                Class<?> helper = Class.forName("com.redhat.mixedsigning.helper.MixedSigningAppletHelper");
                Method m;
                if (this.methodSignature == null) {
                    m = helper.getMethod(this.methodName);
                } else {
                    m = helper.getMethod(this.methodName, this.methodSignature.toArray(new Class<?>[methodSignature.size()]));
                }
                Object[] params = args.toArray(new String[args.size()]);
                @SuppressWarnings("unchecked")
                T result = (T) m.invoke(null, params);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
