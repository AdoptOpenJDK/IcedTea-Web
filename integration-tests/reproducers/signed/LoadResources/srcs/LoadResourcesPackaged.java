/* 
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
package some.pkg;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
//import javax.swing.JApplet;

/**
 * Intentionally copypasted so LoadResource and LoadResource packed do not call
 * each other or climb outside of its pacakage
 */
//public class LoadResourcesPackaged extends JApplet {
public class LoadResourcesPackaged extends Applet {

    public static final String PASS = "Pass";
    public static final String FAIL = "Fail";

    public static final String CONTEXT = "context";
    public static final String CLASS = "class";
    public static final String SYSTEM = "system";
    public static final String[] STATIC_LOADERS = new String[]{CONTEXT, CLASS, SYSTEM};

    public static final String THIS = "this";
    public static final String[] INSTANCE_LOADERS = new String[]{THIS};

    public static String[] FILES = new String[]{"some.file", "LoadResources.class", "some/pkg/LoadResourcesPackaged.class"};
    private static String phase;

    static {
        System.out.println("LoadResourcesPackaged started");
    }

    public static void main(String[] args) {
        phase = "MAIN";
        checkAllStatic();
    }

    public LoadResourcesPackaged() {
        phase = "CONSTRUCTOR";
        checkAllStatic();
        checkAllInstance();
    }

    @Override
    public void init() {
        phase = "INIT";
        checkAllStatic();
        checkAllInstance();
    }

    @Override
    public void start() {
        phase = "START";
        checkAllStatic();
        checkAllInstance();
        System.out.println("*** APPLET FINISHED ***");
    }

    private static void checkAllStatic() {
        for (String file : FILES) {
            for (String loader : STATIC_LOADERS) {
                checkStaticOnly(loader, file);
            }
        }
    }

    private void checkAllInstance() {
        for (String file : FILES) {
            for (String loader : INSTANCE_LOADERS) {
                checkInstance(loader, file);
            }
        }
    }

    private static void checkStaticOnly(String clType, String resource) {
        title(clType, resource);
        try {
            //we need catch exception both in classloader check and resource check
            ClassLoader cl = getStaticClassLoader(clType);
            String res = read(getResource(cl, resource));
            pass(res);
        } catch (Exception ex) {
            fail(ex);
            ex.printStackTrace();
        }

    }

    private static void fail(Exception ex) {
        System.out.println(FAIL + " - " + ex.getMessage());
    }

    private static void pass(String res) {
        System.out.println(PASS + " - " + res);
    }

    private static void title(String clType, String resource) {
        System.out.print("[" + phase + "]" + clType + "(" + resource + "): ");
        System.err.print("[" + phase + "]" + clType + "(" + resource + "): ");
    }

    private void checkInstance(String clType, String resource) {
        title(clType, resource);
        try {
            //we need catch exception both in classloader check and resource check
            ClassLoader cl = getInstanceClassLoader(clType);
            String res = read(getResource(cl, resource));
            pass(res);
        } catch (Exception ex) {
            fail(ex);
            ex.printStackTrace();
        }

    }

    private static ClassLoader getStaticClassLoader(String type) {
        switch (type) {
            case CONTEXT:
                return Thread.currentThread().getContextClassLoader();

            case CLASS:
                return LoadResourcesPackaged.class
                        .getClassLoader();
            case SYSTEM:
                return ClassLoader.getSystemClassLoader();
        }
        return null;

    }

    private ClassLoader getInstanceClassLoader(String type) {
        switch (type) {
            case THIS:
                return this.getClass().getClassLoader();
        }
        return null;
    }

    private static InputStream getResource(ClassLoader classLoader, String arg) {
        return classLoader.getResourceAsStream(arg);
    }

    private static String read(java.io.InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII))) {
            return br.readLine();
        }
    }
}
