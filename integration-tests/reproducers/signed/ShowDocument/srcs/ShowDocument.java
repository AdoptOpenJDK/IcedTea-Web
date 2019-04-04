
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;


/* SimpleTest2.java
Copyright (C) 2011 Red Hat, Inc.

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
public class ShowDocument extends Applet {

    //this is the only reason why it is isgned
    private static String url = System.getProperty("durl", "http://www-eng-x.llnl.gov/documents/a_document.txt");

    private static class Killer extends Thread {

        public static final int n = 2000;

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

    public static void main(String[] args) throws MalformedURLException, UnavailableServiceException {
        // Lookup the javax.jnlp.BasicService object
        BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        // Invoke the showDocument method
        bs.showDocument(new URL(url));
    }

    public void work1() {
        try {
            boolean ok = true;
            this.getAppletContext().showDocument(new URL(url));
            if (!ok) {
                throw new RuntimeException("document should show");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void work2() {
        try {
            boolean ok = true;
            this.getAppletContext().showDocument(new URL(url), "");
            if (!ok) {
                throw new RuntimeException("document should show");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        System.out.println("applet was initialised");
        killer = new Killer();
    }

    @Override
    public void start() {
        System.out.println("applet was started");
        work1();
        work2();
        killer.start();
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
