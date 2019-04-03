
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;

/* AppletTest.java
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
public class GifarMain extends JApplet {

    boolean isApplet = true;
    String defaultPath = "happyNonAnimated.gif";
    String imageName;
    URL path = null;

    private class Killer extends Thread {

        public int n = 3000;

        @Override
        public void run() {
            try {
                Thread.sleep(n);
                System.out.println("gifar killing himself after " + n + " ms of life");
                System.exit(0);
            } catch (Exception ex) {
            }
        }
    }
    private Killer killer;

    @Override
    public void init() {
        System.out.println("gifar was initialised");
        killer = new Killer();
        this.setLayout(new BorderLayout());
        String futurePath = null;
        if (isApplet) {
            futurePath = getParameter("image");
            if ("yes".equals(futurePath)) {
                imageName = defaultPath;
            } else if (futurePath != null) {
                imageName = futurePath;
            }
        }
    }

    @Override
    public void start() {
        System.out.println("gifar is starting");
        String s = "<html>" + System.getProperty("java.vm.version") + "<br>"
                + System.getProperty("java.vm.vendor") + "<br>"
                + System.getProperty("java.vm.name") + "<br>"
                + System.getProperty("java.specification.version") + "<br>"
                + System.getProperty("java.specification.vendor") + "<br>"
                + System.getProperty("java.specification.name") + "</html>";
        JLabel jLabel1 = new JLabel(s);
        this.add(jLabel1, BorderLayout.NORTH);
        System.out.println("Used image: " + imageName);
        if (imageName != null) {
            try {
                path = new URL(getIndependentCodebase(), imageName);
                System.out.println("Loading: "+path.toString());
                JLabel jLabel2 = new JLabel(loadIcon(path));
                System.out.println("Image loaded");
                this.add(jLabel2, BorderLayout.SOUTH);
            } catch (Exception ex) {
                //ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        killer.start();
        System.out.println("is applet: " + isApplet);
        System.out.println("gifar was started");
    }

    Icon loadIcon(URL u) {
        try {
            BufferedImage i = ImageIO.read(u.openStream());
            return new ImageIcon(i);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }

    public URL getIndependentCodebase() {
        try {
            URL u1 = getCodeBaseApplet();
            URL u2 = getCodeBaseJavaws();
            if (u1 != null) {
                return u1;
            }
            if (u2 != null) {
                return u2;
            }
            return new URL("http://localhost:44321/");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public URL getCodeBaseApplet() {
        try {
            URL codebase = getCodeBase();
            if (codebase != null) {
                System.out.println("applet codebase: " + codebase.toString());
                return codebase;
            } else {
                System.out.println("applet codebase: null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("applet codebase: null");
        }
        return null;
    }

    public URL getCodeBaseJavaws() {
        try {
            BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
            URL codebase = bs.getCodeBase();
            if (codebase != null) {
                System.out.println("javaws codebase: " + codebase.toString());
                return codebase;
            } else {
                System.out.println("javaws codebase: null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("javaws codebase: null");
        }
        return null;
    }

    @Override
    public void stop() {
        System.out.println("gifar was stopped");
    }

    @Override
    public void destroy() {
        System.out.println("gifar will be destroyed");
    }

   public static void main(String[] args){
        final JFrame f = new JFrame();
        f.setLayout(new BorderLayout());
        f.setSize(250, 200);
        GifarMain gm = new GifarMain();
        gm.isApplet = false;
        f.add(gm);
        gm.init();
        if (args.length > 0) {
            if ("yes".equals(args[0])) {
                gm.imageName = gm.defaultPath;
            } else {
                gm.imageName = args[0];
            }
        }
        gm.start();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                f.setVisible(true);
            }
        });
    }
}
