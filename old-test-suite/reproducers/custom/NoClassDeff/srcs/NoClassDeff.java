/* ExtensionJnlpTestApplet.java
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

import java.applet.*;
import java.awt.Graphics;

public class NoClassDeff extends Applet {

    private static final String appletCloseString = "*** APPLET FINISHED ***";
    private static String stage = null;
    private static boolean catchError = false;

    private static void checkStage() {
        if (stage == null) {
            throw new NullPointerException("satge cant be null");
        }
        System.out.println(stage);
        System.out.println("catchError: " + catchError);
    }

    private static void lostClass() {
        System.out.println("Loading LostClass");
        System.out.flush();
        if (catchError) {
            try {
                LostClass l = new LostClass();
            } catch (Throwable ex) {// vs exception
                System.out.println("EX: " + ex.toString());
                System.out.flush();
                ex.printStackTrace();
            }
        } else {
            try {
                LostClass l = new LostClass();
            } catch (Exception ex) {// vs throwable
                System.out.println("EX: " + ex.toString());
                System.out.flush();
                ex.printStackTrace();
            }
        }
    }

    private static class LostClass {

    }

    public static void main(String[] args) {
        stage = args[0];
        catchError = Boolean.valueOf(args[1]);
        checkStage();
        System.out.println("main1");
        if (stage.equalsIgnoreCase("main")) {
            lostClass();
        }
        System.out.println("main2");
        System.out.println(appletCloseString);
        System.out.flush();
        System.out.println("some garbage");
    }

    @Override
    public void init() {
        stage = getParameter("die");
        catchError = Boolean.valueOf(getParameter("catchError"));
        System.out.println("init1");
        if (stage.equalsIgnoreCase("init")) {
            lostClass();
        }
        System.out.println("init2");
    }

    @Override
    public void start() {
        checkStage();
        System.out.println("start1");
        if (stage.equalsIgnoreCase("start")) {
            lostClass();
        }
        System.out.println("start2");
    }

    @Override
    public void stop() {
        checkStage();
        System.out.println("stop1");
        if (stage.equalsIgnoreCase("stop")) {
            lostClass();
        }
        System.out.println("stop2");
        System.out.flush();
        System.out.println("some garbage");
    }

    @Override
    public void destroy() {
        checkStage();
        System.out.println("destroy1");
        if (stage.equalsIgnoreCase("destroy")) {
            lostClass();
        }
        System.out.println("destroy2");
        System.out.flush();
        System.out.println("some garbage");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        checkStage();
        System.out.println("paint1");
        if (stage.equalsIgnoreCase("paint")) {
            lostClass();
        }
        System.out.println("paint2");
        System.out.println(appletCloseString);
        System.out.flush();
        System.out.println("some garbage");
    }

}
