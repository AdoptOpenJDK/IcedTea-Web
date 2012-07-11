/* CheckServices.java
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

import javax.jnlp.ServiceManager;
import javax.jnlp.BasicService;
import java.applet.Applet;

public class CheckServices extends Applet {

    public CheckServices() {
        System.out.println("Applet constructor reached.");
        checkSetup("constructor"); 
    }

    public void checkSetup(String method) {
        try {
            BasicService basicService =
                (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
            // getCodeBase() will return null if ServiceManager does not 
            // have access to ApplicationInstance.
            String codebase = basicService.getCodeBase().toString();
            System.out.println("Codebase for applet was found in " + method
                + ": " + codebase);
        } catch (NullPointerException npe) {
            System.err.println("Exception occurred with null codebase in " + method);
            npe.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Exception occurred (probably with ServiceManager).");
          ex.printStackTrace();
        }
    }

    @Override
    public void init() {
        System.out.println("Applet is initializing.");
        checkSetup("init()"); 
    }

    @Override
    public void start() {
        System.out.println("Applet is starting.");
        checkSetup("start()"); 
        // FIXME: Instead of killing the thread, use the AWT robot to close
        // the applet window, signaling the event that runs stop/destroy.
        System.out.println("Killer thread is starting.");
        Thread killer = new Thread() {
            public int n = 2000;
        
            @Override
            public void run() {
                try {
                        Thread.sleep(n);
                        System.out.println("Applet killing itself after " + n + " ms of life");
                        System.exit(0);
                } catch (Exception ex) {
                }   
            }
        };
        killer.start(); 
    }

   
    @Override
    public void stop() {
        System.out.println("Applet is stopping.");
        checkSetup("stop()"); 
    }   

    @Override
    public void destroy() {
        System.out.println("Applet is destorying itself.");
        checkSetup("destroy()"); 
    } 
    
}
