/* InternalClassloaderWithDownloadedResource.java
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

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class InternalClassloaderWithDownloadedResource extends Applet {

    public static void main(String[] args) throws Exception {
        int port = 44321; //debug default
        String sPort = System.getProperty("serveraccess.port");
        if (sPort != null) {
            port = new Integer(sPort);
        }
        if (args.length != 1) {
            throw new IllegalArgumentException("exactly one argument expected");
        }
        resolveArgument(args[0], port);

    }

    private static void downlaodAndExecuteForeignMethod(int port, int classlaoder) throws SecurityException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, MalformedURLException, IllegalArgumentException {
        URL url = new URL("http://localhost:" + port + "/SimpletestSigned1.jar");
        URLClassLoader ucl = null;
        if (classlaoder == 1) {
            ucl = (URLClassLoader) InternalClassloaderWithDownloadedResource.class.getClassLoader();
            System.out.println("Downloading " + url.toString());
            Method privateStringMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            privateStringMethod.setAccessible(true);
            privateStringMethod.invoke(ucl, url);
        } else if (classlaoder == 2) {
            ucl = new URLClassLoader(new URL[]{url});
        } else {
            throw new IllegalArgumentException("just 1 or 2 classlaoder id expected");
        }
        executeForeignMethod(port, ucl);
    }

    private static void executeForeignMethod(int port, URLClassLoader loader) throws SecurityException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, MalformedURLException, IllegalArgumentException {
        String className = "SimpletestSigned1";
        Class<?> cls = loader.loadClass(className);
        Method m = cls.getMethod("main", new Class[]{new String[0].getClass()});
        System.out.println("executing " + className + "'s main");
        m.invoke(null, (Object) new String[0]);
    }

    private static void resolveArgument(String s, int port) throws Exception {
        if (s == null) {
            throw new IllegalArgumentException("arg was null");
        } else if (s.equalsIgnoreCase("hack")) {
            downlaodAndExecuteForeignMethod(port, 1);
        } else if (s.equalsIgnoreCase("new")) {
            downlaodAndExecuteForeignMethod(port, 2);
        } else {
            throw new IllegalArgumentException("hack or new expected as argument");
        }
    }

    private class Killer extends Thread {

        public int n = 2000;

        @Override
        public void run() {
            try {
                Thread.sleep(n);
                System.out.println("Applet killing himself after " + n + " ms of life");
                System.exit(0);
            } catch (Exception ex) {
            }
        }
    }
    private Killer killer;

    @Override
    public void init() {
        System.out.println("applet was initialised");
        killer = new Killer();
    }

    @Override
    public void start() {
        System.out.println("applet was started");
        killer.start();
        int port = 44321; //debug default
        try {
            File portsFile = new File(System.getProperty("java.io.tmpdir"), "serveraccess.port");
            if (portsFile.exists()) {
                String sPort = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(portsFile), "utf-8"));
                try {
                    sPort = br.readLine();
                } finally {
                    br.close();
                }
                if (sPort != null) {
                    port = new Integer(sPort.trim());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        try {
            resolveArgument(getParameter("arg"), port);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        System.out.println("killer was started");
    }

    @Override
    public void stop() {
        System.out.println("applet was stopped");
    }

    @Override
    public void destroy() {
        System.out.println("applet will be destroyed");
    }
}
