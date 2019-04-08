/* KeyboardActions.java
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

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyboardActions {
    
    private static final int defaultDelay = 250;

    /**
     * method writeText for simulating typing the 
     * given String text by a user with delays 
     * allowed characters in the text: 0-9, a-z, the space
     * between the keystrokes
     *  
     * @param robot
     * @param text
     * @param delayMs
     */
    public static void writeText(Robot robot, String text, int delayMs){
        for (int i = 0; i < text.length(); i++){
            char c = text.charAt(i);        
            typeKey(robot, keyFromChar(c), delayMs);    
        }
    }
    
    public static void writeText(Robot robot, String text){
        writeText(robot,text, defaultDelay);
    }
    
    /**
     * method typeKey for pressing and releasing given key
     * with a reasonable delay 
     *
     * @param robot
     * @param key
     * @param delayMs
     */
     
    public static void typeKey(Robot robot, int key, int delayMs){
        robot.delay(delayMs);
        robot.keyPress(key);
        robot.delay(delayMs);
        robot.keyRelease(key);
    }
    
    public static void typeKey(Robot robot, int key){
        typeKey(robot, key, defaultDelay);
    }
    
    /**
     * method returning the KeyInput event int
     * if the character is not from a-b, 0-9, the returned value is
     * KeyEvent.VK_SPACE
     * 
     * @param ch
     * @return
     */
    public static int keyFromChar(char ch){
        int key;
        
        if( ('0' <= ch) && ('9' >= ch) ){
            key = (ch - '0') + KeyEvent.VK_0; 
        }else if( ( 'a' <= ch) && ('z' >= ch) ){
            key = (ch - 'a') + KeyEvent.VK_A;
        }else{
            key = KeyEvent.VK_SPACE;
        }
        
        return key;
    }
    
}
