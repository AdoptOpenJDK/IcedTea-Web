

/* MultipleSignaturesPerJarMain.java
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
package somecrazytestpackage;

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultipleSignaturesPerJarMain extends Applet {

    public static void main(String[] args) {
        executeForeignMethodCaught();
    }

    public static void executeForeignMethodCaught() {
        try {
            executeForeignMethod();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void executeForeignMethod() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class<?> clazz = Class.forName("ReadPropertiesSigned");
        Method mainMethod = clazz.getDeclaredMethod("main", String[].class);
        mainMethod.invoke(clazz.newInstance(), (Object)new String[] {"user.home"});
        System.out.println("Test has finished.");
    }
    private class Killer extends Thread {

        public int n = 2000;

        @Override
        public void run() {
            try {
                Thread.sleep(n);
                System.exit(0);
            } catch (Exception ex) {
            }
        }
    }
    private Killer killer;

    @Override
    public void init() {
        killer = new Killer();
    }

    @Override
    public void start() {
        main(null);
        System.out.println("*** APPLET FINISHED ***");
    }
}
