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
package net.sourceforge.jnlp.awt;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.jnlp.awt.awtactions.KeyboardActions;
import net.sourceforge.jnlp.awt.awtactions.MouseActions;
import net.sourceforge.jnlp.awt.imagesearch.ComponentFinder;
import net.sourceforge.jnlp.awt.imagesearch.ComponentNotFoundException;
import net.sourceforge.jnlp.awt.imagesearch.ImageSeeker;
import net.sourceforge.jnlp.closinglisteners.Rule;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;

public abstract class AWTHelper extends RulesFolowingClosingListener implements Runnable{

    //attributes possibly set by user
    private String initStr = null;        
    private Color appletColor;
    private BufferedImage marker;
    private Point markerPosition;
    private int appletHeight;
    private int appletWidth;
    private int tryKTimes = DEFAULT_K;

    //other 
    protected StringBuilder sb = new StringBuilder();
    private boolean actionStarted = false;
    private Rectangle actionArea;
    private BufferedImage screenshot;
    private Robot robot;
    private boolean appletFound = false;
    private boolean appletColorGiven = false; //impossible to search for color difference if not given
    private boolean markerGiven = false; //impossible to find the applet if marker not given
    private boolean appletDimensionGiven = false;
    private boolean screenshotTaken = false;
    private int defaultWaitForApplet = 1000;
    
    //default number of times the screen is captured and the applet is searched for
    //in the screenshot
    public static final int DEFAULT_K = 3;
   
    
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
        } catch (AWTException e) {
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
    public AWTHelper(String initStr){
        this();
        
        this.initStr = initStr;
    }
    
    /**
     * the constructor with icon and its position in applet of given dimension
     * use:
     *   - we want to find and activate the applet first
     *   - the search for applet will be done via searching for icon
     *     of given position(x,y,w,h) inside applet of given width and height
     * 
     * @param icon marker by which the applet will be found
     * @param iconPosition relatively to applet (including icon width and height)
     * @param appletWidth
     * @param appletHeight
     */
   
    public AWTHelper(BufferedImage icon, Point iconPosition, int appletWidth, int appletHeight){
        this();
        this.marker = icon;
        this.markerPosition = iconPosition;
        this.markerGiven = true;
        
        this.appletWidth = appletWidth;
        this.appletHeight = appletHeight;
        this.appletDimensionGiven = true;
    }
    
    public AWTHelper(String initString, BufferedImage icon, Point iconPosition, int appletWidth, int appletHeight) throws AWTException{
        this(icon, iconPosition, appletWidth, appletHeight);
        
        this.initStr = initString;
    }
    
    /**
     * the constructor with applet width and height only - use:
     *  - we want to find the applet by finding the default icon
     *    that is located in the upper left corner of applet
     * 
     * @param appletWidth
     * @param appletHeight
     */
    public AWTHelper(int appletWidth, int appletHeight){
        this();
        
        String test_server_dir_path = System.getProperty("test.server.dir");

        this.marker = ComponentFinder.defaultIcon;
        this.markerPosition = new Point(0,0);
        this.markerGiven = true;
                
        this.appletWidth = appletWidth;
        this.appletHeight = appletHeight;
        this.appletDimensionGiven = true;
    }
    
    public AWTHelper(String initString, int appletWidth, int appletHeight){
        this(appletWidth, appletHeight);
        this.initStr = initString;
    }
    
    /**
     * refers to AWTHelper functioning as RulesFolowingClosingListener
     * 
     * @param strs array of strings to be added as contains rules
     */
    public void addClosingRulesFromStringArray(String [] strs){
        for(String s : strs){
            this.addContainsRule(s);
        }
    }
    
    /**
     * override of method charReaded (from RulesFolowingClosingListener)
     * 
     * waiting for the applet, when applet is ready run action thread
     * (if initStr==null, do not check and do not call run)
     * 
     * when all the wanted strings are in the stdout, applet can be closed
     * 
     * @param ch 
     */
    @Override
    public void charReaded(char ch) {
        sb.append(ch);
        //is applet ready to start clicking?
        //check and run applet only if initStr is not null
        if ((initStr != null) && !actionStarted && appletIsReady(sb.toString())) {
            try{
                actionStarted = true; 
                this.findAndActivateApplet();
                this.run();
            } catch (ComponentNotFoundException e1) {
                throw new RuntimeException("AWTHelper problems finding applet.",e1);
            } catch (AWTFrameworkException e2){
                throw new RuntimeException("AWTHelper problems with unset attributes.",e2);
            }
        }
        //is all the wanted output in stdout?
        super.charReaded(ch);
    }
    
    /**
     * method runAWTHelper - we can call run and declared the action as started
     * without finding out if initStr is in the output, if this method is
     * called
     * 
     */
    public void runAWTHelper(){
        actionStarted = true;
        this.run();
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
    public Rule<String, String> getInitStrAsRule(){
    	if( initStr != null ){
            return new ContainsRule(this.initStr);
    	}else{
    		return new Rule<String, String>(){

				@Override
				public void setRule(String rule) {
				}

				@Override
				public boolean evaluate(String upon) {
					return true;
				}

				@Override
				public String toPassingString() {
					return "nothing to check, initStr is null";
				}

				@Override
				public String toFailingString() {
					return "nothing to check, initStr is null";
				}
    			
    		} ;
    	}
    }
    
    //boolean controls getters
    protected boolean appletIsReady(String content) {
        return this.getInitStrAsRule().evaluate(content);
    }
    
    public boolean isActionStarted() {
        return actionStarted;
    }
    
    public boolean isAppletColorGiven(){
        return appletColorGiven;
    }
    
    public boolean isAppletDimensionGiven(){
        return appletDimensionGiven;
    }
    
    public boolean isMarkerGiven(){
        return markerGiven;
    }
    
    //setters
    /**
     * method setDefaultWaitForApplet sets the time (in ms) for which the method
     * captureScreenAndFindApplet will wait (for the applet to load) before it
     * gets the screenshot the default time is 1000ms
     * 
     * @param defaultWaitForApplet
     */
    public void setDefaultWaitForApplet(int defaultWaitForApplet) {
        this.defaultWaitForApplet = defaultWaitForApplet;
    }

    public void setTryKTimes(int tryKTimes) {
        this.tryKTimes = tryKTimes;
    }

    public void setAppletColor(Color appletColor) {
        this.appletColor = appletColor;
        this.appletColorGiven = true;
    }

    public void setInitStr(String initStr) {
        this.initStr = initStr;
    }
       
    public void setMarker(BufferedImage marker, Point markerPosition) {
        this.marker = marker;
        this.markerPosition = markerPosition;
        this.markerGiven = true;
    }
    
    public void setAppletDimension(int width, int height){
        this.appletWidth = width;
        this.appletHeight = height;
        this.appletDimensionGiven = true;
    }


    //creating screenshots, searching for applet
    /**
     * method captureScreenAndFindAppletByIcon
     * 1. checks that all needed attributes of AWTHelper are given
     *    (marker, its position and applet width and height)
     * 2. captures screen, 
     * 3. finds the rectangle where applet is and saves it to the attribute
     *    actionArea 
     * 4. sets screenCapture indicator to true (after tryKTimes unsuccessfull
     *    tries an exception "ComponentNotFound" will be raised)
     * 
     * @throws AWTException 
     * @throws ComponentNotFoundException 
     * @throws AWTFrameworkException 
     */
    public void captureScreenAndFindAppletByIcon() throws ComponentNotFoundException, AWTFrameworkException {
        if(!appletDimensionGiven || !markerGiven){
            throw new AWTFrameworkException("AWTFramework cannot find applet without dimension or marker!");
        }
        captureScreenAndFindAppletByIconTryKTimes(marker, markerPosition, appletWidth, appletHeight, tryKTimes);
    }

    /**
     ** method captureScreenAndFindAppletByIcon
     * 1. captures screen, 
     * 2. finds the rectangle where applet is and saves it to the attribute
     *    actionArea 
     * 3. sets screenCapture indicator to true (after tryKTimes unsuccessfull
     *    tries an exception "ComponentNotFound" will be raised) 
     * 
     * @param icon
     * @param iconPosition
     * @param width
     * @param height
     * @param K
     * @throws ComponentNotFoundException
     */
    public void captureScreenAndFindAppletByIconTryKTimes(BufferedImage icon, Point iconPosition, int width, int height, int K) throws ComponentNotFoundException {
  
        int count = 0;
        appletFound = false;
        while ((count < K) && !appletFound) {
            robot.delay(defaultWaitForApplet);
            try {
                screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                initialiseOnScreenshot(icon, iconPosition, width, height, screenshot);
            } catch (ComponentNotFoundException ex) {
                //keeping silent and try more-times
            }
            count++;
        }

        if (ImageSeeker.isRectangleValid(actionArea)) {
            appletFound = true;
        } else {
            throw new ComponentNotFoundException("Object not found in the screenshot!");
        }

    }
    
    public void initialiseOnScreenshot(BufferedImage icon, Point iconPosition, int width, int height, BufferedImage screenshot) throws ComponentNotFoundException {
        Rectangle r = ComponentFinder.findWindowByIcon(icon, iconPosition, width, height, screenshot);
        initialiseOnScreenshotAndArea(screenshot, r);
        
    }
    
    public void initialiseOnScreenshotAndArea(BufferedImage screenshot, Rectangle actionArea) throws ComponentNotFoundException {
        this.screenshot = screenshot;
        screenshotTaken = true;
        this.actionArea = actionArea;
        if (ImageSeeker.isRectangleValid(actionArea)) {
            appletFound = true;
        } else {
            throw new ComponentNotFoundException("set invalid area!");
        }
    }

    
    /**
     * auxiliary method writeAppletScreen for writing Buffered image into png
     * 
     * @param appletScreen
     * @param filename
     * @throws IOException
     */
    private void writeAppletScreen(BufferedImage appletScreen, String filename) throws IOException {// into png file
            ImageIO.write(appletScreen, "png", new File(filename+".png"));
    }

    
    /**
     * method findAndActivateApplet finds the applet by icon 
     * and clicks in the middle of applet area
     * 
     * @throws ComponentNotFoundException (applet not found) 
     * @throws AWTFrameworkException 
     */
    public void findAndActivateApplet() throws ComponentNotFoundException, AWTFrameworkException
    {
        captureScreenAndFindAppletByIcon();
        clickInTheMiddleOfApplet();
    }


    //methods for clicking and typing 
    /**
     * method clickInTheMiddleOfApplet focuses the applet by clicking in the
     * middle of its location rectangle
     */
    public void clickInTheMiddleOfApplet() {
        MouseActions.clickInside(this.actionArea, this.robot);
    }
    
    /**
     * Method clickOnIconExact - click in the middle of a rectangle with 
     * given pattern (icon) using specified mouse key.
     * If the applet has not been found yet, the search includes whole screen.
     * 
     * @param icon
     * @param mouseKey
     * @throws ComponentNotFoundException
     */
    public void clickOnIconExact(BufferedImage icon, int mouseKey) throws ComponentNotFoundException{
        Rectangle areaOfSearch;
        if(!appletFound){//searching whole screen, less effective
            areaOfSearch = new Rectangle(0, 0, this.screenshot.getWidth(), this.screenshot.getHeight());
        }else{
            areaOfSearch = this.actionArea;
        }
        Rectangle iconRectangle = ImageSeeker.findExactImage(icon, this.screenshot, areaOfSearch);

        if (ImageSeeker.isRectangleValid(iconRectangle)) {
            MouseActions.clickInside(mouseKey, iconRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Exact icon not found!");
        }
    }

    /**
     * Method clickOnIconBlurred - click in the middle of a rectangle with 
     * given pattern (icon) using specified mouse key.
     * If the applet has not been found yet, the search includes whole screen.
     *  
     * @param icon
     * @param mouseKey
     * @param precision tolerated minimal correlation (see ImageSeeker methods)
     * @throws ComponentNotFoundException
     */
    public void clickOnIconBlurred(BufferedImage icon, int mouseKey, double precision) throws ComponentNotFoundException{
        Rectangle areaOfSearch;
        if(!appletFound){//searching whole screen, less effective
            areaOfSearch = new Rectangle(0, 0, this.screenshot.getWidth(), this.screenshot.getHeight());
        }else{
            areaOfSearch = this.actionArea;
        }    
        Rectangle iconRectangle = ImageSeeker.findBlurredImage(icon, this.screenshot, precision, areaOfSearch);

        if (ImageSeeker.isRectangleValid(iconRectangle)) {
            MouseActions.clickInside(mouseKey, iconRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Blurred icon not found!");
        }
    }
    
    /**
     * Method clickOnColoredRectangle - click in the middle of a rectangle with
     * given color (appletColor must be specified as the background) using
     * specified mouse key.
     * 
     * @param c
     * @param mouseKey
     * @throws ComponentNotFoundException
     * @throws AWTFrameworkException 
     * 
     */
    public void clickOnColoredRectangle(Color c, int mouseKey) throws ComponentNotFoundException, AWTFrameworkException {
        Rectangle buttonRectangle = findColoredRectangle(c);

        if (ImageSeeker.isRectangleValid(buttonRectangle)) {
            MouseActions.clickInside(mouseKey, buttonRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Colored rectangle not found!");
        }
    }

    public void moveToMiddleOfColoredRectangle(Color c) throws ComponentNotFoundException, AWTFrameworkException {

        Rectangle buttonRectangle = findColoredRectangle(c);

        if (ImageSeeker.isRectangleValid(buttonRectangle)) {
            MouseActions.moveMouseToMiddle(buttonRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Colored rectangle not found!");
        }
    }

    public void moveOutsideColoredRectangle(Color c) throws ComponentNotFoundException, AWTFrameworkException {
        Rectangle buttonRectangle = findColoredRectangle(c);

        if (ImageSeeker.isRectangleValid(buttonRectangle)) {
            MouseActions.moveMouseOutside(buttonRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Colored rectangle not found!");
        }
    }

    public void moveInsideColoredRectangle(Color c) throws ComponentNotFoundException, AWTFrameworkException {
        Rectangle buttonRectangle = findColoredRectangle(c);

        if (ImageSeeker.isRectangleValid(buttonRectangle)) {
            MouseActions.moveInsideRectangle(buttonRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Colored rectangle not found!");
        }
    }

    public void dragFromColoredRectangle(Color c) throws ComponentNotFoundException, AWTFrameworkException {
        Rectangle buttonRectangle = findColoredRectangle(c);

        if (ImageSeeker.isRectangleValid(buttonRectangle)) {
            MouseActions.dragFromRectangle(buttonRectangle, this.robot);
        }else{
            throw new ComponentNotFoundException("Colored rectangle not found!");
        }
    }

    public Rectangle findColoredRectangle(Color c) throws AWTFrameworkException {
        if(!appletColorGiven || !appletFound){
            throw new AWTFrameworkException("AWTHelper could not search for colored rectangle, needs appletColor and applet position.");
        }
        
        Rectangle result;

        int gap = 5;
        result = ImageSeeker.findColoredAreaGap(this.screenshot, c, appletColor, this.actionArea.y, this.actionArea.y + this.actionArea.height, gap);

        return result;
    }

    /**
     * method writeText writes string containing small letters and numbers and
     * spaces like the keyboard input (using KeyboardActions so delays are
     * inserted)
     * 
     * @param text
     */
    public void writeText(String text) {
        KeyboardActions.writeText(this.robot, text);
    }

    /**
     * method typeKey writes one key on the keyboard (again using
     * KeyboardActions)
     * 
     * @param key
     */
    public void typeKey(int key) {
        KeyboardActions.typeKey(this.robot, key);
    }
 }
