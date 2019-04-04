/* MouseActions.java
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


package net.sourceforge.jnlp.awt.awtactions;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * class MouseActions 
 * 
 * static methods for manipulating the mouse via AWT robot
 */
public class MouseActions {
    private static final int defaultDelay = 250;
    
    /**
     * method click presses and releases given mouse keys
     * with reasonable delay before the event
     * 
     * @param mouseKeyMask
     * @param robot
     * @param delayMs
     */
    public static void click(int mouseKeyMask, Robot robot, int delayMs){
        robot.delay(delayMs);
        robot.mousePress(mouseKeyMask);
        robot.delay(delayMs);
        robot.mouseRelease(mouseKeyMask);
    }
    
    public static void click(int mouseKeyMask, Robot robot){
        robot.delay(defaultDelay);
        robot.mousePress(mouseKeyMask);
        robot.delay(defaultDelay);
        robot.mouseRelease(mouseKeyMask);
    }
    
    /**
     * method doubleClick presses and releases given mouse keys
     * two times with reasonable delays
     * 
     * @param mouseKeyMask
     * @param robot
     * @param delayMs
     */
     
    public static void doubleClick(int mouseKeyMask, Robot robot, int delayMs){
        click(mouseKeyMask, robot, delayMs);
        click(mouseKeyMask, robot, delayMs);
    }
    
    public static void doubleClick(int mouseKeyMask, Robot robot){
        click(mouseKeyMask, robot, defaultDelay);
        click(mouseKeyMask, robot, defaultDelay);
    }

    /**
     * method drag presses the right mouse key,
     * drags the mouse to a point, and releases the mouse key  
     * with reasonable delays
     * 
     * @param xTo
     * @param yTo
     * @param robot
     * @param delayMs
     */
    public static void drag(int xTo, int yTo, Robot robot, int delayMs){
        robot.delay(delayMs);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(delayMs);
        robot.mouseMove(xTo, yTo);
    }
    
    public static void drag(int xTo, int yTo, Robot robot){
        robot.delay(defaultDelay);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(defaultDelay);
        robot.mouseMove(xTo, yTo);
    }
    
    /**
     * method dragFromRectangle clicks in the middle
     * of the given rectangle and drags the mouse from the rectangle
     * with reasonable delays
     * 
     * @param rectangle
     * @param robot
     * @param delayMs
     */

    public static void dragFromRectangle(Rectangle rectangle, Robot robot, int delayMs){
        int x1 = rectangle.x + rectangle.width/2;
        int y1 = rectangle.y + rectangle.height/2;
        int x2 = x1 + 2*rectangle.width;
        int y2 = y1 + 2*rectangle.height;
        robot.delay(delayMs);
        robot.mouseMove(x1, y1);
        drag(x2,y2, robot);
    }
    
    public static void dragFromRectangle(Rectangle rectangle, Robot robot){
        dragFromRectangle(rectangle, robot, defaultDelay);
    }

    /**
     * method moveInsideRectangle places the mouse in the middle
     * of the given rectangle and moves the mouse inside the rectangle
     * with reasonable delays
     * 
     * @param rectangle
     * @param robot
     * @param delayMs
     */

    public static void moveInsideRectangle(Rectangle rectangle, Robot robot, int delayMs){
        int x1 = rectangle.x + rectangle.width/2;
        int y1 = rectangle.y + rectangle.height/2;
        int x2 = x1 + rectangle.width/4;
        int y2 = y1 + rectangle.height/4;
        robot.delay(delayMs);
        robot.mouseMove(x1, y1);
        robot.delay(delayMs);
        robot.mouseMove(x2, y2);
    }
    
    public static void moveInsideRectangle(Rectangle rectangle, Robot robot){
        moveInsideRectangle(rectangle, robot, defaultDelay);
    }
    
    /**
     * 
     * @param rectangle
     * @param robot
     * @param delayMs
     */
    public static void moveMouseToMiddle(Rectangle rectangle, Robot robot, int delayMs){
        robot.delay(delayMs);
        int x = rectangle.x + (rectangle.width/2);
        int y = rectangle.y + (rectangle.height/2);
        robot.mouseMove(x,y);
    }
    
    public static void moveMouseToMiddle(Rectangle rectangle, Robot robot){
        moveMouseToMiddle(rectangle, robot, defaultDelay);
    }

    /**
     * 
     * @param rectangle
     * @param robot
     * @param delayMs
     */
    public static void moveMouseOutside(Rectangle rectangle, Robot robot, int delayMs){
        robot.delay(delayMs);
        int x = rectangle.x + 2*rectangle.width;
        int y = rectangle.y + 2*rectangle.height;
        robot.mouseMove(x,y);
    }
    
    public static void moveMouseOutside(Rectangle rectangle, Robot robot){
        moveMouseOutside(rectangle, robot, defaultDelay);
    }
    
    
    /**
      * method clickInside moves the mouse in the middle point
     * of a given rectangle and clicks with reasonable delay

     * 
     * @param rectangle
     * @param robot
     * @param delayMs
     */
    public static void clickInside(int mouseKey, Rectangle rectangle, Robot robot, int delayMs){
        moveMouseToMiddle(rectangle, robot, delayMs);
        robot.delay(delayMs);
        click(mouseKey, robot, delayMs);
    }
    
    public static void clickInside(int mouseKey, Rectangle rectangle, Robot robot){
        clickInside(mouseKey, rectangle, robot, defaultDelay);
    }
    
    public static void clickInside(Rectangle rectangle, Robot robot, int delayMs){
        clickInside(InputEvent.BUTTON1_MASK, rectangle, robot, delayMs);
    }
    
    public static void clickInside(Rectangle rectangle, Robot robot){
        clickInside(rectangle, robot, defaultDelay);
    }
}
