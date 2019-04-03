
import java.applet.Applet;
import java.util.Arrays;

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
public class MixedSigningAndTrustedOnlyClass2 extends Applet {

    private static final String ID_THIS = "MixedSigningAndTrustedOnlyClass2";
    private static final String ID_REMOTE = "MixedSigningAndTrustedOnlyClass1";

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
            System.out.println(ID_THIS + " running");
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
                        case ID_THIS + "_Normal":
                            doNormalLocal();
                            break;
                        case ID_THIS + "_Restricted":
                            doRestrictedActionLocal();
                            break;
                        case ID_REMOTE + "_Normal":
                            MixedSigningAndTrustedOnlyClass1.doNormalRemote();
                            break;
                        case ID_REMOTE + "_Restricted":
                            MixedSigningAndTrustedOnlyClass1.doRestrictedActionlRemote();
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

    private static void doRestrictedAction() {
        String a = System.getProperty("user.home");
        System.out.println(ID_THIS + " Property read");
        System.out.println(a);
        System.out.flush();
    }

    private  static void doNormal() {
        System.out.println(ID_THIS + " confirmed");
        System.out.flush();
    }
    
    private static final String REMOTE_CALL = "RemoteCall - ";
    private static final String LOCAL_CALL = "LocalCall - ";
    
    public static void doNormalRemote() {
        System.out.print(REMOTE_CALL);
        doNormal();
        System.out.flush();
    }
    
    private static void doNormalLocal() {
        System.out.print(LOCAL_CALL);
        doNormal();
        System.out.flush();
    }
    
     public static void doRestrictedActionlRemote() {
        System.out.print(REMOTE_CALL);
        doRestrictedAction();
        System.out.flush();
    }
    
    private static void doRestrictedActionLocal() {
        System.out.print(LOCAL_CALL);
        doRestrictedAction();
        System.out.flush();
    }

}
