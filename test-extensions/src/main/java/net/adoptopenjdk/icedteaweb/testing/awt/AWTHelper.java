/* AWTHelper.java
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
package net.adoptopenjdk.icedteaweb.testing.awt;

import net.adoptopenjdk.icedteaweb.testing.closinglisteners.Rule;
import net.adoptopenjdk.icedteaweb.testing.closinglisteners.RulesFollowingClosingListener;

import java.awt.AWTException;
import java.awt.Robot;

public abstract class AWTHelper extends RulesFollowingClosingListener implements Runnable{

    //attributes possibly set by user
    private String initStr = null;

    //other 
    private final StringBuilder sb = new StringBuilder();
    private boolean actionStarted = false;
    private final Robot robot;

   
    
    //several constructors
    /**
     * the minimal constructor - use:
     *  - if we do not want to find the bounds of applet area first
     *  - searching for buttons and other components is then done in the whole
     *    screen, confusion with other icons on display is then possible
     *  - less effective, deprecated (better bound the area first) 
     */
    @Deprecated
    public AWTHelper() {
        try {
            this.robot = new Robot();
        } catch (final AWTException e) {
            throw new RuntimeException("AWTHelper could not create its Robot instance.",e);
        }
    }
    
    /**
     * the minimal constructor with initStr - use:
     *  - we want to know from stdout that the applet (or sth else) is ready
     *  - if we do not want to find the bounds of applet area first
     *  - searching for buttons and other components is then done in the whole
     *    screen, confusion with other icons on display is then possible
     *  - less effective, deprecated (better bound the area first) 
     */
    @Deprecated
    public AWTHelper(final String initStr){
        this();
        
        this.initStr = initStr;
    }

    /**
     * override of method charRead (from RulesFollowingClosingListener)
     * 
     * waiting for the applet, when applet is ready run action thread
     * (if initStr==null, do not check and do not call run)
     * 
     * when all the wanted strings are in the stdout, applet can be closed
     * 
     * @param ch 
     */
    @Override
    public void charRead(final char ch) {
        sb.append(ch);
        //is applet ready to start clicking?
        //check and run applet only if initStr is not null
        if ((initStr != null) && !actionStarted && appletIsReady(sb.toString())) {
            try{
                actionStarted = true; 
                this.findAndActivateApplet();
                this.run();
            } catch (final AWTFrameworkException e2){
                throw new RuntimeException("AWTHelper problems with unset attributes.",e2);
            }
        }
        //is all the wanted output in stdout?
        super.charRead(ch);
    }
    


    /**
     * implementation of AWTHelper should implement the run method
     */
    public abstract void run();


    /**
     * method getInitStrAsRule returns the initStr in the form
     * of Contains rule that can be evaluated on a string 
     * 
     * @return
     */
    private Rule<String, String> getInitStrAsRule(){
    	if( initStr != null ){
            return new ContainsRule(this.initStr);
    	}else{
    		return v -> true;
    	}
    }
    
    //boolean controls getters
    private boolean appletIsReady(final String content) {
        return this.getInitStrAsRule().evaluate(content);
    }



    //creating screenshots, searching for applet
    /**
     * method captureScreenAndFindAppletByIcon
     * 1. checks that all needed attributes of AWTHelper are given
     *    (marker, its position and applet width and height)
     * 2. captures screen, 
     * 3. finds the rectangle where applet is and saves it to the attribute
     *    actionArea 
     * 4. sets screenCapture indicator to true (after tryKTimes unsuccessful
     *    tries an exception "ComponentNotFound" will be raised)
     * 
     * @throws AWTFrameworkException
     * @throws AWTFrameworkException 
     */
    private void captureScreenAndFindAppletByIcon() throws AWTFrameworkException {
        throw new AWTFrameworkException("AWTFramework cannot find applet without dimension or marker!");
    }

    /**
     * method findAndActivateApplet finds the applet by icon 
     * and clicks in the middle of applet area
     * 
     * @throws AWTFrameworkException
     */
    private void findAndActivateApplet() throws AWTFrameworkException
    {
        captureScreenAndFindAppletByIcon();
        clickInTheMiddleOfApplet();
    }


    //methods for clicking and typing 
    /**
     * method clickInTheMiddleOfApplet focuses the applet by clicking in the
     * middle of its location rectangle
     */
    private void clickInTheMiddleOfApplet() {
        MouseActions.clickInside(null, this.robot);
    }
    

 }
