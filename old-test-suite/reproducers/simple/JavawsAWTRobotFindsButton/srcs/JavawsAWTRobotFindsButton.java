/* JavawsAWTRobotFindsButton.java
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
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class JavawsAWTRobotFindsButton extends Applet {

    private static final String initStr = "JavawsAWTRobotFindsButton is ready for awt tests!";
    public static final String iconFile = "marker.png";

    public static final Color APPLET_COLOR = new Color(230, 230, 250); // lavender
    public static final Color BUTTON_COLOR1 = new Color(32, 178, 170); // light sea green

    public Image img;
    public Panel panel;
    
    public void init(){
        img = getImage(getCodeBase(), iconFile);

        createGUI();

        writeAppletInitialized();
    }

    //this method should be called by the extending applet
    //when the whole gui is ready
    public void writeAppletInitialized(){
         System.out.println(initStr);
    }

    //paint the icon in upper left corner
    @Override public void paint(Graphics g){
         int width = 32;
         int height = 32;
         int x = 0;
         int y = 0;
         g.drawImage(img, x, y, width, height, this);
         super.paint(g);
    }

    private Button createButton(String label, Color color) {
        Button b = new Button(label);
        b.setBackground(color);
        b.setPreferredSize(new Dimension(100, 50));
        return b;
    }

    // sets background of the applet 
    // and adds the panel with 2 buttons
    private void createGUI() {
        setBackground(APPLET_COLOR);

        panel = new Panel();
        panel.setBounds(33,33,267,267);

        Button bA = createButton("Button A", BUTTON_COLOR1);


        bA.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                System.out.println("Mouse clicked button A.");
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            
            }
            
        });

        panel.add(bA);


        Button bB = createButton("Button B", BUTTON_COLOR1);


        bB.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                System.out.println("Mouse clicked button B.");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
            }
        });

        panel.add(bB);

        this.add(panel);
    }
}
