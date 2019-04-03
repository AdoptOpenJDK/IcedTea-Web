/* JavawsAWTRobotFindsButtonTest.java
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

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.awt.AWTFrameworkException;
import net.sourceforge.jnlp.awt.AWTHelper;
import net.sourceforge.jnlp.awt.imagesearch.ComponentFinder;
import net.sourceforge.jnlp.awt.imagesearch.ComponentNotFoundException;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.closinglisteners.Rule;

import org.junit.Assert;
import org.junit.Test;

public class JavawsAWTRobotFindsButtonTest {

    public static final ServerAccess server = new ServerAccess();

    private final String initStr = "JavawsAWTRobotFindsButton is ready for awt tests!";
    private static final Color APPLET_COLOR = new Color(230, 230, 250); // lavender
    private static final Color BUTTON_COLOR1 = new Color(32, 178, 170); // light sea green

    private static final BufferedImage buttonIcon;

    static{
        try {
            buttonIcon = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("buttonA.png"));
        } catch (IOException e) {
            throw new RuntimeException("Problem initializing buttonIcon",e);
        }
    }
    
    private class AWTHelperImpl_ClickButtonIcon extends AWTHelper{
        
        public AWTHelperImpl_ClickButtonIcon() {
            super(initStr, 400, 400);
            
            this.setAppletColor(APPLET_COLOR);
        }
        
        @Override
        public void run() {
            // move mouse into the button area and out
            try {
                clickOnIconExact(buttonIcon, InputEvent.BUTTON1_MASK);
            } catch (ComponentNotFoundException e) {
                Assert.fail("Button icon not found: "+e.getMessage());
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


    private void appletAWTMouseTest(String url, AWTHelper helper) throws Exception {

        String strURL = "/" + url;
        
       try {
            ServerAccess.PROCESS_TIMEOUT = 40 * 1000;// ms
            ProcessResult pr = server.executeJavaws(strURL, helper, helper);
            evaluateStdoutContents(pr, helper);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000;// ms
        }
    }

    @Test
    @NeedsDisplay
    public void findAndClickButtonByIcon_Test() throws Exception {
        // display the page, activate applet, click on button
        AWTHelper helper = new AWTHelperImpl_ClickButtonIcon();
        helper.addClosingRulesFromStringArray(new String[] { "Mouse clicked button A." });
        appletAWTMouseTest("javaws-awtrobot-finds-button.jnlp", helper);
    }

    @Test
    public void iconFileLoaded_Test() throws IOException {
        Assert.assertNotNull("buttonIcon should not be null", buttonIcon);
    }

}
