/* 
Copyright (C) 2017 Red Hat, Inc.

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
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;

public class Kemtrak extends Applet {

    private class Killer extends Thread {

        public int n = 1000;

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

    public static void main(String[] args) throws  IOException {
        System.out.println("Kemtrak2");
        if (args.length == 2 && args[0].equals("closeJar")) {
            String cbase = args[1];
            System.out.println("Closing Jar!");
            URL localURL = new java.net.URL("jar", "", cbase + "jcalendar.jar!/");
            JarURLConnection localObject3 = (java.net.JarURLConnection) localURL.openConnection();
            java.util.jar.JarFile localJarFile = ((java.net.JarURLConnection) localObject3).getJarFile();
            String str9 = localJarFile.getName();
            int i3 = localJarFile.size();
            localJarFile.close();
            System.out.println("jcalendar " + localURL + "   " + str9 + ", entrie: " + i3);
            //if one call inisde  jcalendar.jar (jcalendar1() and/or jcalendar2) *BEFORE* closing, issue is NOT hit
            jcalendar2();
        } else {
            jcalendar1();
        }
        System.out.println("kemtrak finished");
    }

    @Override
    public void init() {
        try {
            System.out.println("Kemtrak1");
            String cj = this.getParameter("closeJar");
            if ("closeJar".equals(cj)) {
                Kemtrak.main(new String[]{"closeJar", getCodeBase().toExternalForm()});
            } else {
                Kemtrak.main(new String[0]);
            }
        } catch (IOException u) {
            throw new RuntimeException(u);
        } finally {
            killer = new Killer();
            killer.start();
        }
    }
    
    
    //we use reflection only to avoid jcalendar.jar on classpath

    public static void jcalendar1() {
        try {
            Class<?> signedAppletClass = Class.forName("jcalendar");
            Method m = signedAppletClass.getMethod("main1", String[].class);
            m.invoke(null, (Object) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void jcalendar2() {
        try {
            Class<?> signedAppletClass = Class.forName("jcalendar");
            Method m = signedAppletClass.getMethod("main2", String[].class);
            m.invoke(null, (Object) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
