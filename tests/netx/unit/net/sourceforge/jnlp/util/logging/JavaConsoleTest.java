/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.jnlp.util.logging;

import java.util.Date;
import net.sourceforge.jnlp.util.logging.headers.PluginMessage;
import org.junit.Assert;
import org.junit.Test;


public class JavaConsoleTest {


     String s1 = "plugindebug 1384850630162925 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1204] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0:   PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80";
     String s2 = "plugindebugX 1384850630162954 [jvanek][ITW-Cplugindebug 1384850630163008 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1124] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: parts[0]=plugin, parts[1]=PluginProxyInfo, reference, parts[3]=1, parts[4]=http://www.walter-fendt.de:80 -- decoded_url=http://www.walter-fendt.de:80";
     String s3 = "preinit_pluginerror 1384850630163298 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1134] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: Proxy info: plugin PluginProxyInfo reference 1 DIRECT";

    @Test
    public void CreatePluginHeaderTestOK() throws Exception{
        PluginMessage p1 = new PluginMessage(s1);
        PluginMessage p3 = new PluginMessage(s3);
        Assert.assertFalse(p1.wasError);
        Assert.assertFalse(p3.wasError);
        Assert.assertTrue(p1.header.isC);
        Assert.assertTrue(p3.header.isC);
        Assert.assertEquals(OutputController.Level.MESSAGE_DEBUG,p1.header.level);
        Assert.assertEquals(OutputController.Level.ERROR_ALL,p3.header.level);
        Assert.assertTrue(p1.header.date.toString().contains("Tue Nov 19 09:43:50") && p1.header.date.toString().contains("2013"));
        Assert.assertTrue(p3.header.date.toString().contains("Tue Nov 19 09:43:50") && p3.header.date.toString().contains("2013"));
        Assert.assertTrue(p1.header.caller.contains("/home/jvanek"));
        Assert.assertTrue(p3.header.caller.contains("/home/jvanek"));
        Assert.assertTrue(p1.header.user.equals("jvanek"));
        Assert.assertTrue(p3.header.user.equals("jvanek"));
        Assert.assertTrue(p1.header.thread1.equals("140513434003264"));
        Assert.assertTrue(p1.header.thread2.equals("0x7fcbd531f8c0"));
        Assert.assertTrue(p3.header.thread1.equals("140513434003264"));
        Assert.assertTrue(p3.header.thread2.equals("0x7fcbd531f8c0"));
        Assert.assertTrue(p1.restOfMessage.equals("  PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80"));
        Assert.assertTrue(p3.restOfMessage.equals("Proxy info: plugin PluginProxyInfo reference 1 DIRECT"));

    }
    @Test
    public void CreatePluginHeaderTestNotOK()throws Exception{
        PluginMessage p2 = new PluginMessage(s2);
        Assert.assertTrue(p2.wasError);
        Assert.assertTrue(p2.header.isC);
        Assert.assertEquals(OutputController.Level.WARNING_ALL,p2.header.level);
        Assert.assertTrue(p2.header.date.toString().contains(new Date().toString().substring(0,16))); //means no Tue Nov 19 09:43:50 :)
        Assert.assertTrue(p2.header.user.equals("jvanek"));
        Assert.assertTrue(p2.header.thread1.equals("unknown"));
        Assert.assertTrue(p2.header.thread2.equals("unknown"));


    }
}
