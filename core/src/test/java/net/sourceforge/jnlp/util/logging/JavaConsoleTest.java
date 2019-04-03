/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.jnlp.util.logging;

import java.util.Date;
import java.util.TimeZone;

import net.sourceforge.jnlp.util.logging.headers.PluginMessage;

import org.junit.Assert;
import org.junit.Test;


public class JavaConsoleTest {

    //note this time is in EST timezone, and so is expecting the string output below
    private static final String TEST_TIME = "Tue Nov 19 09:43:50 "+TimeZone.getTimeZone("EST").getDisplayName(false, TimeZone.SHORT)+" 2013";
    private static final String S1 = "plugindebug 1384850630162925 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG]["+TEST_TIME+"][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1204] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0:   PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80";
    private static final String S2 = "plugindebugX 1384850630162954 [jvanek][ITW-Cplugindebug 1384850630163008 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG]["+TEST_TIME+"][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1124] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: parts[0]=plugin, parts[1]=PluginProxyInfo, reference, parts[3]=1, parts[4]=http://www.walter-fendt.de:80 -- decoded_url=http://www.walter-fendt.de:80";
    private static final String S3 = "preinit_pluginerror 1384850630163298 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG]["+TEST_TIME+"][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1134] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: Proxy info: plugin PluginProxyInfo reference 1 DIRECT";
    private static final String S4 = "plugindebugX blob [jvanek][ITW-Cplugindebug 1384850630163008 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG]["+TEST_TIME+"][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1124] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: parts[0]=plugin, parts[1]=PluginProxyInfo, reference, parts[3]=1, parts[4]=http://www.walter-fendt.de:80 -- decoded_url=http://www.walter-fendt.de:80";

    @Test
    public void CreatePluginHeaderTestOK() throws Exception{
        PluginMessage p1 = new PluginMessage(S1);
        PluginMessage p2 = new PluginMessage(S2);
        PluginMessage p3 = new PluginMessage(S3);
        Assert.assertFalse(p1.wasError);
        Assert.assertFalse(p2.wasError);
        Assert.assertFalse(p3.wasError);
        Assert.assertTrue(p1.header.isC);
        Assert.assertTrue(p2.header.isC);
        Assert.assertTrue(p3.header.isC);
        Assert.assertEquals(OutputController.Level.MESSAGE_DEBUG, p1.header.level);
        Assert.assertEquals(OutputController.Level.WARNING_ALL, p2.header.level);
        Assert.assertEquals(OutputController.Level.ERROR_ALL, p3.header.level);
        Assert.assertTrue(p1.header.date.toString().contains(TEST_TIME) && p1.header.date.toString().contains("2013"));
        Assert.assertTrue(p2.header.date.toString().contains("ITW-C-PLUGIN"));
        Assert.assertTrue(p3.header.date.toString().contains(TEST_TIME) && p3.header.date.toString().contains("2013"));
        Assert.assertTrue(p1.header.caller.contains("/home/jvanek"));
        Assert.assertTrue(p3.header.caller.contains("/home/jvanek"));
        Assert.assertTrue(p1.header.user.equals("jvanek"));
        Assert.assertTrue(p2.header.user.equals("jvanek"));
        Assert.assertTrue(p3.header.user.equals("jvanek"));
        Assert.assertTrue(p1.header.thread1.equals("140513434003264"));
        Assert.assertTrue(p1.header.thread2.equals("0x7fcbd531f8c0"));
        Assert.assertTrue(p2.header.thread1.equals("19"));
        Assert.assertTrue(p2.header.thread2.equals("43"));
        Assert.assertTrue(p3.header.thread1.equals("140513434003264"));
        Assert.assertTrue(p3.header.thread2.equals("0x7fcbd531f8c0"));
        Assert.assertTrue(p1.restOfMessage.equals("  PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80"));
        Assert.assertEquals("0 EST 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1124] " +
                "ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: parts[0]=plugin, parts[1]=PluginProxyInfo, reference, parts[3]=1, " +
                "parts[4]=http://www.walter-fendt.de:80 -- decoded_url=http://www.walter-fendt.de:80", p2.restOfMessage);
        Assert.assertTrue(p3.restOfMessage.equals("Proxy info: plugin PluginProxyInfo reference 1 DIRECT"));

    }
    @Test
    public void CreatePluginHeaderTestNotOK()throws Exception{
        PluginMessage p4 = new PluginMessage(S4);
        Assert.assertTrue(p4.wasError);
        Assert.assertTrue(p4.header.isC);
        Assert.assertEquals(OutputController.Level.WARNING_ALL, p4.header.level);
        Assert.assertTrue(p4.header.date.toString().contains(new Date().toString().substring(0, 16))); //means no Tue Nov 19 09:43:50 :)
        Assert.assertTrue(p4.header.thread1.equals("unknown"));
        Assert.assertTrue(p4.header.thread2.equals("unknown"));
    }
}
