/* AppletErrorTest.java
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
 *
 */

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AppletErrorTest extends JApplet {

    private class Killer extends Thread {

        public int n = 20000;

        @Override
        public void run() {
            try {
                Thread.sleep(n);
                System.out.println("Error Applet killing himself after " + n + " ms of life");
                System.exit(0);
            } catch (Exception ex) {
            }
        }
    }
    private volatile boolean waiting = true;
    private boolean isApplet = true;
    private Killer killer;
    private final String IN_GUI_THREAD = "IN_GUI_THREAD";
    private final String BEHIND_GUI_THREAD = "BEHIND_GUI_THREAD";
    private final String IN_GUI = "IN_GUI";
    private final String IN_INIT = "IN_INIT";
    private final String IN_START = "IN_START";
    private final String IN_STOP = "IN_STOP";
    private final String IN_DESTROY = "IN_DESTROY";
    private String levelOfDeath = BEHIND_GUI_THREAD;

    @Override
    public void init() {
        if (isApplet) {
            String s = getParameter("levelOfDeath");
            if (s != null) {
                levelOfDeath = s;
            }
        }
        System.out.println("Error applet was initialised");
        killer = new Killer();
        if (levelOfDeath.equals(IN_INIT)) {
            throw new RuntimeException("Intentional exception from init");
        }
    }

    public static void main(String[] args) {
        final JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(899, 600);
        f.setLayout(new BorderLayout());
        AppletErrorTest ae = new AppletErrorTest();
        ae.isApplet=false;
        ae.init();
        f.add(ae);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        });
        ae.start();


    }

    @Override
    public void start() {
        final AppletErrorTest aSelf = this;
        final JPanel self = new JPanel();
        aSelf.setLayout(new BorderLayout());
        aSelf.add(self);
        self.setLayout(new GridLayout(0, 4));
        final Random r = new Random();
        new Thread(new Runnable() {

            @Override
            public void run() {
                new Colorer(self, r).run();
            }
        }).start();


        System.out.println("Error applet was started");
        killer.start();
        System.out.println("killer was started");
        if (levelOfDeath.equals(IN_GUI_THREAD) || levelOfDeath.equals(IN_GUI) || levelOfDeath.equals(BEHIND_GUI_THREAD)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        for (int i = 0; i < 15; i++) {
                            try {
                                System.out.println("Rainbow is shining");
                                new GuiRainbow(self, r, i).run();
                                if (levelOfDeath.equals(BEHIND_GUI_THREAD) && i >= 12) {
                                    throw new RuntimeException("Intentional error from start (gui is running)- " + levelOfDeath);
                                }
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    } finally {
                        waiting = false;
                    }



                }
            }).start();
        }
        if (!isApplet) {
            if (levelOfDeath.equals(IN_GUI)) {
                while (waiting) {
                    try {
                        Thread.sleep(100);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                aSelf.repaint();
                                aSelf.validate();
                                aSelf.repaint();
                            }
                        });

                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                throw new RuntimeException("Intentional error from start (gui was running)- " + levelOfDeath);
            }
        }
        if (levelOfDeath.equals(IN_START)) {
            throw new RuntimeException("Intentional error from start (gui was not running)- " + levelOfDeath);
        }
    }

    @Override
    public void stop() {
        System.out.println("Error applet was stopped");
        if (levelOfDeath.equals(IN_STOP)) {
            throw new RuntimeException("Intentional exception from stop" + levelOfDeath);
        }
    }

    @Override
    public void destroy() {
        System.out.println("Error applet will be destroyed");
        if (levelOfDeath.equals(IN_DESTROY)) {
            throw new RuntimeException("Intentional exception from destroy" + levelOfDeath);
        }
    }

    private class GuiRainbow implements Runnable {

        private final JComponent self;
        private final Random r;
        private final int i;

        public GuiRainbow(JComponent self, Random r, int i) {
            this.self = self;
            this.r = r;
            this.i = i;
        }

        @Override
        public void run() {
            if (self.getComponentCount() > 1 && r.nextInt(2) == 0) {
                int x = r.nextInt(self.getComponentCount());
                self.remove(x);
                self.validate();
            } else {
                JLabel ll=new JLabel("Hi, its error applet here " + i);
                self.add(ll);
                self.validate();
                ll.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        throw new RuntimeException("Intentional exception by click to "+i);
                    }
                });
            }
            System.out.println("Components are handled");
            if (levelOfDeath.equals(IN_GUI_THREAD) && i >= 8) {
                throw new RuntimeException("Intentional error from swing thread (gui is running)- " + levelOfDeath);
            }

        }
    }

    class Colorer implements Runnable {

        private final JComponent self;
        private final Random r;

        public Colorer(JComponent self, Random r) {
            this.self = self;
            this.r = r;
        }

        @Override
        public void run() {
            int i = 0;
            while (true) {
                i++;
                try {
                    self.setBackground(new Color(r.nextInt()));
                    System.out.println("Applet is coloring " + i);
                    Thread.sleep(200);
                } catch (Exception ex) {
                    //intentionally silenced
                }
            }
        }
    }
}
