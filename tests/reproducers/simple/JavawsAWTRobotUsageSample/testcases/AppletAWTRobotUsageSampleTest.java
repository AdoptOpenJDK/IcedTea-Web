/* AppletAWTRobotUsageSampleTest.java
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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.awt.AWTFrameworkException;
import net.sourceforge.jnlp.awt.AWTHelper;
import net.sourceforge.jnlp.awt.imagesearch.ComponentNotFoundException;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.Rule;

import org.junit.Assert;
import org.junit.Test;

public class AppletAWTRobotUsageSampleTest extends BrowserTest {

    private final String initStr = "JavawsAWTRobotUsageSample is ready for awt tests!";

    private static final Color APPLET_COLOR = new Color(230, 230, 250); // lavender
    private static final Color BUTTON_COLOR1 = new Color(32, 178, 170); // light sea green

    private abstract class AWTHelperImpl extends AWTHelper{
        
        public AWTHelperImpl() {
            super(initStr, 400, 400);
            
            this.setAppletColor(APPLET_COLOR);
        }
        
    }
   
    private class AWTHelperImpl_EnterExit extends AWTHelperImpl {

        @Override
        public void run() {
                // move mouse into the button area and out
                try {
                    moveToMiddleOfColoredRectangle(BUTTON_COLOR1);
                    moveOutsideColoredRectangle(BUTTON_COLOR1);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }
    
    private class AWTHelperImpl_MouseClick1 extends AWTHelperImpl{

        @Override
           public void run() {
                // click in the middle of the button
            
                try {
                    clickOnColoredRectangle(BUTTON_COLOR1, InputEvent.BUTTON1_MASK);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }

    private class AWTHelperImpl_MouseClick2 extends AWTHelperImpl{
        @Override
        public void run() {
                // move mouse in the middle of the button and click 2nd
                // button
                try {
                    clickOnColoredRectangle(BUTTON_COLOR1, InputEvent.BUTTON2_MASK);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }
    
    private class AWTHelperImpl_MouseClick3 extends AWTHelperImpl{
        @Override
        public void run() {
                // move mouse in the middle of the button and click 3rd
                // button
                try {
                    clickOnColoredRectangle(BUTTON_COLOR1, InputEvent.BUTTON3_MASK);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }
    
    private class AWTHelperImpl_MouseDrag extends AWTHelperImpl{
        @Override
        public void run() {
                // move into the rectangle, press 1st button, drag out
                try {
                    dragFromColoredRectangle(BUTTON_COLOR1);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }
    
    private class AWTHelperImpl_MouseMove extends AWTHelperImpl{
        @Override
        public void run() {
                clickInTheMiddleOfApplet();
                try {
                    moveInsideColoredRectangle(BUTTON_COLOR1);
                } catch (ComponentNotFoundException e) {
                    Assert.fail("Button not found: "+e.getMessage());
                } catch (AWTFrameworkException e2){
                    Assert.fail("AWTFrameworkException: "+e2.getMessage());
                }
        }
    }


    private void evaluateStdoutContents(ProcessResult pr, AWTHelper helper) {

        // Assert that the applet was initialized.
        Rule i = helper.getInitStrAsRule();
        Assert.assertTrue(i.toPassingString(), i.evaluate(initStr));
        
        // Assert there are all the test messages from applet
        for (Rule r : helper.getRules() ) {
            Assert.assertTrue(r.toPassingString(), r.evaluate(pr.stdout));
        }

    }


    private void appletAWTMouseTest(String url, AWTHelper helper)
            throws Exception {

        String strURL = "/" + url;
        
        ProcessResult pr = server.executeBrowser(strURL, helper, helper);
        evaluateStdoutContents(pr, helper);
    }
    
    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_EnterAndExit_Test() throws Exception {

        // display the page, activate applet, move over the button
        AWTHelper helper = new AWTHelperImpl_EnterExit();
        helper.addClosingRulesFromStringArray(new String[] { "mouseEntered", "mouseExited"});
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_ClickButton1_Test() throws Exception {

        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_MouseClick1();
        helper.addClosingRulesFromStringArray(new String[] { "mousePressedButton1", "mouseReleasedButton1", "mouseClickedButton1" });
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_ClickButton2_Test() throws Exception {

        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_MouseClick2();
        helper.addClosingRulesFromStringArray(new String[] { "mousePressedButton2", "mouseReleasedButton2", "mouseClickedButton2" });
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_ClickButton3_Test() throws Exception {

        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_MouseClick3();
        helper.addClosingRulesFromStringArray(new String[] { "mousePressedButton3", "mouseReleasedButton3", "mouseClickedButton3" });
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_Drag_Test() throws Exception {

        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_MouseDrag();
        helper.addClosingRulesFromStringArray(new String[] { "mouseDragged" });
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    @NeedsDisplay
    public void AppletAWTMouse_Move_Test() throws Exception {

        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_MouseMove();
        helper.addClosingRulesFromStringArray(new String[] { "mouseMoved" });
        appletAWTMouseTest("AppletAWTRobotUsageSample.html", helper);
    }
}
