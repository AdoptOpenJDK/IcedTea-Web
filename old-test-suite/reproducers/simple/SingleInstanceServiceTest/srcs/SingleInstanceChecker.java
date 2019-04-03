/* SingleInstanceChecker.java
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

import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

public class SingleInstanceChecker extends Applet implements SingleInstanceListener {

    private SingleInstanceChecker self;
    Killer killer;

    private static class Killer extends Thread {

        private int timeout;
        private String timeoutText;

        public Killer() {
            timeout = 5000;
            timeoutText = Integer.toString(timeout);
        }

        public Killer(int n) {
            timeout = n;
            timeoutText = Integer.toString(timeout);
        }

        public Killer(int n, String s) {
            timeout = n;
            timeoutText = s;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout);
                System.out.println("Applet killing itself after " + timeoutText + " ms of life");
                System.exit(0);
            } catch (Exception ex) {
            }
        }
    }

    public SingleInstanceChecker() {
        self = this;
    }

    private void proceed() {

        try {
            SingleInstanceService testService = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
            System.out.println("SingleInstanceChecker: Adding listener to service.");
            testService.addSingleInstanceListener(this);
            System.out.println("SingleInstanceChecker: Listener added.");
        } catch (UnavailableServiceException use) {
            System.err.println("SingleInstanceChecker: Service lookup failed.");
            use.printStackTrace();
        } finally {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        startKiller(2);
                    }
                }
            }).start();
        }
    }

    //killer is started when params are received, or when application is running to long
    private void startKiller(int a) {
        synchronized (self) {
            if (killer == null) {
                if (a == 2) {
                    killer = new Killer(5000, "10000");
                    killer.start();
                } else {
                    killer = new Killer(5000);
                    killer.start();
                }
            }
        }
    }

    @Override
    public void newActivation(String[] params) {
        String paramsString = "";
        for (String param : params) {
            paramsString += " "+param;
        }
        System.out.println("Parameters received by SingleInstanceChecker:" + paramsString);
        startKiller(1);
    }

    @Override
    public void start() {
        System.out.print("Parameters received by during launch:");
        for (int i=1; i<10; i++ ) {
            String s=getParameter("p"+i);
            if (s!=null){
                System.out.print(" "+s);
            }
        }
        System.out.println();
        proceed();
    }

    public static void main(String[] args) {
        System.out.print("Parameters received by during launch:");
        for (String string : args) {
            System.out.print(" "+string);
        }
        System.out.println();
        new SingleInstanceChecker().proceed();
    }
}
