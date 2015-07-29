/*
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

import java.applet.Applet;
import java.util.Arrays;

public class MixedSigningAndTrustedOnlyClass1 extends Applet {

    private static final String ID1 = "MixedSigningAndTrustedOnlyClass1";
    private static final String ID2 = "MixedSigningAndTrustedOnlyClass2";

    public static void main(String[] args) {
        runBody(args);
    }

    @Override
    public void start() {
        String c = getParameter("command");
        String[] cc = c.split(" ");
        runBody(cc);
    }

    private static void runBody(String... commands) {
        try {
            System.out.println(ID1 + " running");
            System.out.println("params: " + Arrays.toString(commands));
            boolean canDie = true;
            for (String command : commands) {
                try {
                    switch (command) {
                        case "canDie":
                            canDie = true;
                            break;
                        case "cantDie":
                            canDie = false;
                            break;
                        case ID1 + "_Normal":
                            doNormal();
                            break;
                        case ID1 + "_Restricted":
                            doRestrictedAction();
                            break;
                        case ID2 + "_Normal":
                            MixedSigningAndTrustedOnlyClass2.doNormal();
                            break;
                        case ID2 + "_Restricted":
                            MixedSigningAndTrustedOnlyClass2.doRestrictedAction();
                            break;

                    }
                } catch (Exception ex) {
                    if (canDie) {
                        throw ex;
                    } else {
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            System.out.println("*** APPLET FINISHED ***");
            System.out.flush();
            System.out.println("some garbage forcing to flush");
            System.out.flush();
        }
    }

    public static void doRestrictedAction() {
        System.out.println(System.getProperty("user.home"));
        System.out.println(ID1 + " Property read");
    }

    public static void doNormal() {
        System.out.println(ID1 + " confirmed");
    }

}
