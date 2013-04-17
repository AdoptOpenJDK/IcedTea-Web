/* AppletTestTests.java
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.jnlp.ClosingListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.InvalidJarHeaderException;
import net.sourceforge.jnlp.util.JarFile;
import org.junit.Assert;
import org.junit.Test;

public class GifarTestcases extends BrowserTest {

    List<String> trustIgnore = Arrays.asList(new String[]{ServerAccess.HEADLES_OPTION, "-Xtrustall", "-Xignoreheaders"});
    List<String> trust = Arrays.asList(new String[]{ServerAccess.HEADLES_OPTION, "-Xtrustall"});
    RulesFolowingClosingListener.ContainsRule exceptionRule = new RulesFolowingClosingListener.ContainsRule(InvalidJarHeaderException.class.getName());
    RulesFolowingClosingListener.ContainsRule okRule = new RulesFolowingClosingListener.ContainsRule("Image loaded");
    RulesFolowingClosingListener.ContainsRule sucideRule = new RulesFolowingClosingListener.ContainsRule("gifar killing himself");

    private ClosingListener getExceptionClosingListener() {
        return new RulesFolowingClosingListener(exceptionRule);
    }

    private ClosingListener getOkClosingListener() {
        return new RulesFolowingClosingListener(okRule, sucideRule);
    }
    File okJar = new File(server.getDir(), "GifarBase.jar");
    File hackedJar = new File(server.getDir(), "Gifar.jar");
    File okImage = new File(server.getDir(), "happyNonAnimated.gif");
    File hackedImage = new File(server.getDir(), "Gifar.gif");

    @Test
    public void unittest_verify_okJar() throws IOException {
        JNLPRuntime.setIgnoreHeaders(false);
        JarFile j1 = new JarFile(okJar);
        Assert.assertNotNull(j1);
        JNLPRuntime.setIgnoreHeaders(true);
        JarFile j2 = new JarFile(okJar);
        Assert.assertNotNull(j2);

    }
    
     @Test
    public void unittest_verify_badJar() throws IOException {
        JNLPRuntime.setIgnoreHeaders(false);
        Exception ex=null;
        JarFile j1=null;
        try{
        j1 = new JarFile(hackedJar);
        }catch(InvalidJarHeaderException e){
            ex=e;
        }
        Assert.assertNull(j1);
        Assert.assertNotNull(ex);
        Assert.assertEquals(InvalidJarHeaderException.class, ex.getClass());
        JNLPRuntime.setIgnoreHeaders(true);
        JarFile j2 = new JarFile(hackedJar);
        Assert.assertNotNull(j2);

    }
     
      
     @Test
    public void unittest_verify_badImageAsJar() throws IOException {
        JNLPRuntime.setIgnoreHeaders(false);
        Exception ex=null;
        JarFile j1=null;
        try{
        j1 = new JarFile(hackedImage);
        }catch(InvalidJarHeaderException e){
            ex=e;
        }
        Assert.assertNull(j1);
        Assert.assertNotNull(ex);
        Assert.assertEquals(InvalidJarHeaderException.class, ex.getClass());
        JNLPRuntime.setIgnoreHeaders(true);
        JarFile j2 = new JarFile(hackedImage);
        Assert.assertNotNull(j2);

    }
     
       @Test
    public void unittest_verify_okImage() throws IOException {
        JNLPRuntime.setIgnoreHeaders(false);
        BufferedImage j1 = ImageIO.read(okImage);
        Assert.assertNotNull(j1);
        JNLPRuntime.setIgnoreHeaders(true);
        BufferedImage j2 = ImageIO.read(okImage);
        Assert.assertNotNull(j2);

    }
    
     @Test
    public void unittest_verify_badImaqe() throws IOException {
          JNLPRuntime.setIgnoreHeaders(false);
        BufferedImage j1 = ImageIO.read(hackedImage);
        Assert.assertNotNull(j1);
        JNLPRuntime.setIgnoreHeaders(true);
        BufferedImage j2 = ImageIO.read(hackedImage);
        Assert.assertNotNull(j2);

    }
    @Test
    @NeedsDisplay
    public void GifarViaJnlp_application() throws Exception {
        ProcessResult pr = server.executeJavaws(trust, "gifar_application.jnlp");
        Assert.assertEquals((Integer) 0, pr.returnValue);
        Assert.assertFalse("stdout " + okRule.toFailingString() + " but did", okRule.evaluate(pr.stdout));
        Assert.assertTrue("stderr " + exceptionRule.toPassingString() + " but did'nt", exceptionRule.evaluate(pr.stderr));
    }

    @Test
    @NeedsDisplay
    public void GifarViaJnlp_application_ignoreHeaders() throws Exception {
        ProcessResult pr = server.executeJavaws(trustIgnore, "gifar_application.jnlp");
        Assert.assertEquals((Integer) 0, pr.returnValue);
        Assert.assertTrue("stdout " + okRule.toPassingString() + " but didn't", okRule.evaluate(pr.stdout));
        Assert.assertFalse("stderr " + exceptionRule.toFailingString() + " but did", exceptionRule.evaluate(pr.stderr));
    }

    @Test
    @NeedsDisplay
    public void GifarViaJnlp_applet() throws Exception {
        ProcessResult pr = server.executeJavaws(trust, "gifar_applet.jnlp");
        Assert.assertEquals((Integer) 0, pr.returnValue);
        Assert.assertFalse("stdout " + okRule.toFailingString() + " but did", okRule.evaluate(pr.stdout));
        Assert.assertTrue("stderr " + exceptionRule.toPassingString() + " but didn't", exceptionRule.evaluate(pr.stderr));
    }

    @Test
    @NeedsDisplay
    public void GifarViaJnlp_applet_ignoreHeaders() throws Exception {
        ProcessResult pr = server.executeJavaws(trustIgnore, "gifar_applet.jnlp");
        Assert.assertEquals((Integer) 0, pr.returnValue);
        Assert.assertTrue("stdout " + okRule.toPassingString() + " but didn't", okRule.evaluate(pr.stdout));
        Assert.assertFalse("stderr " + exceptionRule.toFailingString() + " but did", exceptionRule.evaluate(pr.stderr));
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.all})
    @NeedsDisplay
    public void GifarViaBrowser_hacked() throws Exception {
        ProcessResult pr = server.executeBrowser("gifarView_hacked.html", getOkClosingListener(), getExceptionClosingListener());
        Assert.assertFalse("stdout " + okRule.toFailingString() + " but did", okRule.evaluate(pr.stdout));
        Assert.assertTrue("stderr " + exceptionRule.toPassingString() + " but didn't", exceptionRule.evaluate(pr.stderr));


    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void GifarViaBrowser_ok() throws Exception {
        ProcessResult pr = server.executeBrowser("gifarView_ok.html", getOkClosingListener(), getExceptionClosingListener());
        Assert.assertTrue("stdout " + okRule.toPassingString() + " but didn't", okRule.evaluate(pr.stdout));
        Assert.assertFalse("stderr " + exceptionRule.toFailingString() + " but did", exceptionRule.evaluate(pr.stderr));


    }
}
