/* SplashScreenTest.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.sourceforge.jnlp.splashscreen;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;

import net.sourceforge.jnlp.JNLPSplashScreen;
import net.sourceforge.jnlp.splashscreen.parts.InfoItem;
import net.sourceforge.jnlp.splashscreen.parts.InformationElement;
import org.junit.Assert;
import org.junit.Test;

public class ErrorSplashScreenTest extends JDialog {

    static int width = JNLPSplashScreen.DEF_WIDTH;
    static int height = JNLPSplashScreen.DEF_HEIGHT;
    static SplashErrorPanel panel;

    public ErrorSplashScreenTest() {

        setSize(width - getInsets().left - getInsets().right, height - getInsets().bottom - getInsets().top);
//   Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//    int x = (int) ((dimension.getWidth() - getWidth()) / 2);
//    int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        //setLocation(x, y);
        setLocationRelativeTo(null);
        this.pack();
        Exception ex = new Exception("Hi there");
        panel = SplashUtils.getErrorSplashScreen(width, height, SplashUtils.SplashReason.JAVAWS, ex);
        InformationElement ie = new InformationElement();
        ie.setHomepage("http://someones.org/amazing?page");
        ie.setTitle("Testing information title");
        ie.setvendor("IcedTea-Web team");
        ie.addDescription("Testing null description");
        ie.addDescription("tsting twoline des ...break\ncription of kind short", InfoItem.descriptionKindShort);
        panel.setInformationElement(ie);
        panel.setVersion("1.2-re45fdg");
        setLayout(new BorderLayout());
        getContentPane().add(panel.getSplashComponent(), BorderLayout.CENTER);
        addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                //panel.getSplashComponent().setSize(getWidth(), getHeight());
                //panel.adjustForSize(getWidth(), getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    @Test
    public void splashScreenTestsExists() {
        //to silence junit,and test is that thsi class was instantiated ;)
        Assert.assertTrue(true);
    }

    @Test
    public void splashScreenTestsPaint0() {
        //to silence junit,and test is that thsi class was instantiated ;)
        panel.setSplashWidth(width);
        panel.setSplashHeight(height);
        panel.adjustForSize();
        BufferedImage buf = new BufferedImage(panel.getSplashWidth(), panel.getSplashHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.setPercentage(0);
        panel.paintTo(buf.createGraphics());
        //ImageFontCutterTest.save(buf,"e0");
    }

    @Test
    public void splashScreenTestsPaint50() {
        //to silence junit,and test is that thsi class was instantiated ;)
        panel.setSplashWidth(width);
        panel.setSplashHeight(height);
        panel.adjustForSize();
        BufferedImage buf = new BufferedImage(panel.getSplashWidth(), panel.getSplashHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.setPercentage(50);
        panel.paintTo(buf.createGraphics());
        // ImageFontCutterTest.save(buf,"e50");
    }

    @Test
    public void splashScreenTestsPaint100() {
        //to silence junit,and test is that thsi class was instantiated ;)
        panel.setSplashWidth(width);
        panel.setSplashHeight(height);
        panel.adjustForSize();
        BufferedImage buf = new BufferedImage(panel.getSplashWidth(), panel.getSplashHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.setPercentage(100);
        panel.paintTo(buf.createGraphics());
        //  ImageFontCutterTest.save(buf,"e100");
    }

    public static void main(String args[]) {
        ErrorSplashScreenTest app = new ErrorSplashScreenTest();
        app.setSize(800, 600);
        app.setVisible(true);
        app.addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        panel.setPercentage(30);
        //panel.startAnimation();

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        panel.stopAnimation();


    }
}
