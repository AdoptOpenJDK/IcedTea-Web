
import java.applet.Applet;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/* CountingAppletSigned.java
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
public class CountingAppletSigned extends Applet {

    public static void main(String[] args) throws InterruptedException {
        Integer counter = null;
        if (args.length > 0) {
            counter = new Integer(args[0]);
            ;
        }
        int i = 0;
        while (true) {
            System.out.println("counting... " + i);
            if (counter != null && i == counter.intValue()) {
                System.exit(-i);
            }
            i++;
            Thread.sleep(1000);
        }
    }

    @Override
    public void init() {
        System.out.println("applet was initialised");
        final CountingAppletSigned self = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                self.setLayout(new BorderLayout());
                self.add(new JLabel("S"));
                self.validateTree();
                self.repaint();
            }
        });
    }

    @Override
    public void start() {
        System.out.println("applet was started");
        String s = getParameter("kill");
        final String[] params;
        if (s != null) {
            params = new String[]{s};
        } else {
            params = new String[0];
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    main(params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
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
