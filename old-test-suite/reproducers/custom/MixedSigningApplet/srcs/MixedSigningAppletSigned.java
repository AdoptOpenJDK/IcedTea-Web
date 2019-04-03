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

package signed;
import helper.MixedSigningAppletHelper;
import java.applet.Applet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.security.AccessController;
import java.security.PrivilegedAction;

/* See also simple/MixedSigningApplet */
public class MixedSigningAppletSigned extends Applet {

    public static void main(String[] args) {
        MixedSigningAppletSigned applet = new MixedSigningAppletSigned();
        applet.jnlpStart(args[0].replaceAll("\"", ""));
    }

    public void jnlpStart(String testName) {
        try {
            Method m = this.getClass().getMethod(testName);
            final String result = (String) m.invoke(this);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method m = this.getClass().getMethod(testName + "Reflect");
            final String result = (String) m.invoke(this);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("*** APPLET FINISHED ***");
        System.exit(0);
    }

    @Override
    public void start() {
        jnlpStart(getParameter("testName"));
    }

    public String testNonPrivilegedActionReflect() {
        return new HelperMethodCall<String>().method("help").call();
    }

    public String testNonPrivilegedAction() {
        return MixedSigningAppletHelper.help();
    }

    public String testNonPrivilegedActionDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testNonPrivilegedActionReflect();
            }
        });
    }

    public String testNonPrivilegedActionDoPrivileged() {
        return testNonPrivilegedActionDoPrivileged();
    }

    public String testNonPrivilegedActionDoPrivileged2Reflect() {
        return new HelperMethodCall<String>().method("helpDoPrivileged").call();
    }

    public String testNonPrivilegedActionDoPrivileged2() {
        return MixedSigningAppletHelper.helpDoPrivileged();
    }

    // Should succeed
    public String testSignedReadProperties() {
        return getProperty("user.home");
    }

    // Should just be the same as above. It doesn't make much sense to make a reflective version here
    public String testSignedReadPropertiesReflect() {
        return testSignedReadProperties();
    }

    public String testSignedReadPropertiesDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testSignedReadProperties();
            }
        });
    }

    public String testSignedReadPropertiesDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testSignedReadPropertiesReflect();
            }
        });
    }

    // Should result in AccessControlException
    public String testUnsignedReadPropertiesReflect() {
        return new HelperMethodCall<String>().type(String.class).method("getProperty").arg("user.home").call();
    }

    public String testUnsignedReadProperties() {
        return MixedSigningAppletHelper.getProperty("user.home");
    }

    public String testUnsignedReadPropertiesDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedReadProperties();
            }
        });
    }

    public String testUnsignedReadPropertiesDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedReadPropertiesReflect();
            }
        });
    }

    public String testUnsignedReadPropertiesDoPrivileged2Reflect() {
        return new HelperMethodCall<String>().type(String.class).method("getPropertyDoPrivileged").arg("user.home").call();
    }

    public String testUnsignedReadPropertiesDoPrivileged2() {
        return MixedSigningAppletHelper.getPropertyDoPrivileged("user.home");
    }

    // Should result in AccessControlException
    public String testSignedExportPropertiesToUnsignedReflect() {
        return new HelperMethodCall<String>().type(String.class).method("getPropertyFromSignedJar").arg("user.home").call();
    }

    public String testSignedExportPropertiesToUnsigned() {
        return MixedSigningAppletHelper.getPropertyFromSignedJar("user.home");
    }

    public String testSignedExportPropertiesToUnsignedDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testSignedExportPropertiesToUnsignedReflect();
            }
        });
    }

    public String testSignedExportPropertiesToUnsignedDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testSignedExportPropertiesToUnsigned();
            }
        });
    }

    public String testSignedExportPropertiesToUnsignedDoPrivileged2Reflect() {
        return new HelperMethodCall<String>().type(String.class).method("getPropertyFromSignedJarDoPrivileged").arg("user.home").call();
    }

    public String testSignedExportPropertiesToUnsignedDoPrivileged2() {
        return MixedSigningAppletHelper.getPropertyFromSignedJarDoPrivileged("user.home");
    }

    // Should result in AccessControlException
    public String testUnsignedAttacksSignedReflect() {
        return new HelperMethodCall<String>().method("attack").call();
    }

    public String testUnsignedAttacksSigned() {
        return MixedSigningAppletHelper.attack();
    }

    public String testUnsignedAttacksSignedDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedAttacksSignedReflect();
            }
        });
    }

    public String testUnsignedAttacksSignedDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedAttacksSigned();
            }
        });
    }

    public String testUnsignedAttacksSignedDoPrivileged2Reflect() {
        return new HelperMethodCall<String>().method("attackDoPrivileged").call();
    }

    public String testUnsignedAttacksSignedDoPrivileged2() {
        return MixedSigningAppletHelper.attackDoPrivileged();
    }

    // Should result in InvocationTargetException (due to AccessControlException)
    public String testUnsignedReflectionAttackReflect() {
        return new HelperMethodCall<String>().method("reflectiveAttack").call();
    }

    public String testUnsignedReflectionAttack() {
        return MixedSigningAppletHelper.reflectiveAttack();
    }

    public String testUnsignedReflectionAttackDoPrivilegedReflect() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedReflectionAttackReflect();
            }
        });
    }

    public String testUnsignedReflectionAttackDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return testUnsignedReflectionAttack();
            }
        });
    }

    public String testUnsignedReflectionAttackDoPrivileged2Reflect() {
        return new HelperMethodCall<String>().method("reflectiveAttackDoPrivileged").call();
    }

    public String testUnsignedReflectionAttackDoPrivileged2() {
        return MixedSigningAppletHelper.reflectiveAttackDoPrivileged();
    }

    public String calledByReflection() {
        return System.getProperty("user.home");
    }

    public String calledByReflectionDoPrivileged() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return calledByReflection();
            }
        });
    }

    public static String getProperty(String prop) {
        return System.getProperty(prop);
    }

    public static String getPropertyDoPrivileged(String prop) {
        final String fProp = prop;
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty(fProp);
            }
        });
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
                Class<?> helper = Class.forName("helper.MixedSigningAppletHelper");
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
