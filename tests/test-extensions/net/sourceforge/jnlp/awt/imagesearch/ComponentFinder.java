/* ComponentFinder.java
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


package net.sourceforge.jnlp.awt.imagesearch;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ComponentFinder {
    public static final BufferedImage defaultIcon;

    static{
        try {
            defaultIcon = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("net/sourceforge/jnlp/awt/imagesearch/marker.png"));
        } catch (IOException e) {
            throw new RuntimeException("ComponentFinder - problem initializing defaultIcon",e);
        }
    }
    
    /**
     * method findColoredRectangle determines coordinates of a rectangle colored
     * by rectangleColor surrounded by a neighbourhood of surroundingColor
     * 
     * @param rectangleColor
     * @param surroundingColor
     * @param screenshot
     * @return
     */
    public static Rectangle findColoredRectangle(Color rectangleColor, Color surroundingColor, BufferedImage screenshot) {

        Rectangle r = ImageSeeker.findColoredAreaGap(screenshot, rectangleColor, surroundingColor, 0, screenshot.getHeight(), 0);
        if( ImageSeeker.isRectangleValid(r)){
            return r;
        }else{
            return null;
        }
    }

    /**
     * method findColoredRectangle determines coordinates of a rectangle colored
     * by rectangleColor surrounded by a neighbourhood of surroundingColor with
     * possible gap of several pixels
     * 
     * @param rectangleColor
     * @param surroundingColor
     * @param screenshot
     * @param gap
     * @return
     */
    public static Rectangle findColoredRectangle(Color rectangleColor, Color surroundingColor, BufferedImage screenshot, int gap) {

        Rectangle r = ImageSeeker.findColoredAreaGap(screenshot, rectangleColor, surroundingColor, 0, screenshot.getHeight(), gap);
        if( ImageSeeker.isRectangleValid(r)){
            return r;
        }else{
            return null;
        }
    }

    
    /**
     * Method findWindowByIcon finds the application area assuming there is a
     * given icon in given position on the application window 
     * the dimension of the window has to be given.
     * 
     * @param icon
     * @param iconPosition
     * @param appletWidth
     * @param appletHeight
     * @param screenshot
     * @return Rectangle rectangle where the applet resides
     */
    public static Rectangle findWindowByIcon(BufferedImage icon, Point iconPosition, int windowWidth, int windowHeight, BufferedImage screenshot) {
        Rectangle r = ImageSeeker.findExactImage(icon, screenshot);
        if( ImageSeeker.isRectangleValid(r)){
            return windowPositionFromIconPosition(r.getLocation(), iconPosition, windowWidth, windowHeight);
        }else{
            return null;
        }
    }

    public static Rectangle findWindowByIconBlurred(BufferedImage icon, Point iconPosition, int windowWidth, int windowHeight, BufferedImage screenshot, double minCorrelation) {
        Rectangle r = ImageSeeker.findBlurredImage(icon, screenshot, minCorrelation);
        if( ImageSeeker.isRectangleValid(r)){
            return windowPositionFromIconPosition(r.getLocation(), iconPosition, windowWidth, windowHeight);
        }else{
            return null;
        }    
    }
    
    public static Rectangle windowPositionFromIconPosition(Point iconAbsolute, Point iconRelative, int windowWidth, int windowHeight){
        return new Rectangle( iconAbsolute.x - iconRelative.x, iconAbsolute.y - iconRelative.y,
                            windowWidth, windowHeight);
    }
}
