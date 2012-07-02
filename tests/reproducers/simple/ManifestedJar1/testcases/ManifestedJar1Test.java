/* ManifestedJar1Test.java
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

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import org.junit.Assert;

import org.junit.Test;

@Bug(id="http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-February/017435.html")
public class ManifestedJar1Test {

    private static ServerAccess server = new ServerAccess();
    private static final String nonLunchableMessage = "net.sourceforge.jnlp.LaunchException: Fatal: Application Error: Not a launchable JNLP file. File must be a JNLP application, applet, or installer type.";
    //actually this on eis never printed as stderr will not recieve this message in headless mode :(
    private static final String twoMainException = "net.sourceforge.jnlp.ParseException: Invalid XML document syntax";

    private void assertManifestedJar1(String id, ServerAccess.ProcessResult q) {
        String s = "Hello from ManifestedJar1";
        Assert.assertTrue(id + " stdout should contains `" + s + "`, but didn't ", q.stdout.contains(s));
    }

    private void assertManifestedJar2(String id, ServerAccess.ProcessResult q) {
        String s = "Hello from ManifestedJar2";
        Assert.assertTrue(id + " stdout should contains `" + s + "`, but didn't ", q.stdout.contains(s));
    }

    private void assertNotManifestedJar1(String id, ServerAccess.ProcessResult q) {
        String s = "Hello from ManifestedJar1";
        Assert.assertFalse(id + " stdout should NOT contains `" + s + "`, but didn ", q.stdout.contains(s));
    }
    private void assertAppError(String id, ServerAccess.ProcessResult q) {
        Assert.assertTrue(id + " stderr should contains `" + nonLunchableMessage + "`, but didnn't ", q.stderr.contains(nonLunchableMessage));
    }

    private void assertNotManifestedJar2(String id, ServerAccess.ProcessResult q) {
        String s = "Hello from ManifestedJar2";
        Assert.assertFalse(id + " stdout should NOT contains `" + s + "`, but didn ", q.stdout.contains(s));
    }

    private void assertNotDead(String id, ServerAccess.ProcessResult pr) {
        String cc = "ClassNotFoundException";
        Assert.assertFalse(id + " stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse(id + " should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    /**
     * if two jars with manifest specified, none is main and no main class, then first one is loaded
     */
    public void manifestedJar1nothing2nothingNoAppDesc() throws Exception {
        String id = "ManifestedJar-1nothing2nothingNoAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar1(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *if one jar with manifest, is not main,  and no main class then is lunched
     *
     */
    @Test
    public void manifestedJar1noAppDesc() throws Exception {
        String id = "ManifestedJar-1noAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar1(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *if one jar with manifest, but not marked as main and no main class then is lunched
     *
     */
    @Test
    public void manifestedJar1mainNoAppDesc() throws Exception {
        String id = "ManifestedJar-1mainNoAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar1(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *if one jar with manifest, marked as main and no main class then is lunched
     *
     */
    @Test
    public void ManifestedJar1mainHaveAppDesc() throws Exception {
        String id = "ManifestedJar-1mainHaveAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar2(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *
     * Two jars, both with manifest, First is main, but specified mainclass belongs  to second one, then second one should be lunched
     */
    @Test
    public void ManifestedJar1main2nothingNoAppDesc() throws Exception {
        String id = "ManifestedJar-1main2nothingNoAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar2(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *
     * Two jars, both with manifest, seconds is main, no mainclass, then the one marked as main is lunched
     */
    @Test
    public void manifestedJar1main2nothingNoAppDesc() throws Exception {
        String id = "ManifestedJar-1main2nothingNoAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar2(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *
     * Two jars, both with manifest, sboth with main tag, no app desc
     *
     * thisis passing, SUSPICIOUS, but to lunch at least something is better then to lunch nothing at all.
     * althoug it maybe SHOULD throw twoMainException
     */
    @Test
    public void manifestedJar1main2mainNoAppDesc() throws Exception {
        String id = "ManifestedJar-1main2mainNoAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertManifestedJar1(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *
     * Two jars, both with manifest, sboth with main tag, have app desc
     *
     * corectly failing with twoMainException
     */
    @Test
    public void manifestedJar1main2mainAppDesc() throws Exception {
        String id = "ManifestedJar-1main2mainAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertNotManifestedJar1(id, pr);
        assertNotManifestedJar2(id, pr);
        assertNotDead(id, pr);
    }

    /**
     *
     * Two jars, both with manifest, sboth with main tag, have app desc
     *
     * corectly failing
     */
    @Test
    public void manifestedJar1noAppDescAtAll() throws Exception {
        String id = "ManifestedJar-1noAppDescAtAll";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertNotManifestedJar1(id, pr);
        assertNotManifestedJar2(id, pr);
        assertAppError(id, pr);
        assertNotDead(id, pr);
    }



    /**
     *
     * Two jars, both with manifest, non with main tag, have app desc
     * 
     * this jnlp is NOT lunched, twoMainException thrown - ok
     *
     */
    @Test
    public void manifestedJar1nothing2nothingAppDesc() throws Exception {
        String id = "ManifestedJar-1nothing2nothingAppDesc";
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/" + id + ".jnlp");
        assertNotManifestedJar2(id, pr);
        assertNotManifestedJar1(id, pr);
        assertNotDead(id, pr);
    }

}
