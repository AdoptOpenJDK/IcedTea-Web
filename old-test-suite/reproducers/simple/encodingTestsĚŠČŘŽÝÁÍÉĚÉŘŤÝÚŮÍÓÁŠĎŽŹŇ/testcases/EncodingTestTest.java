/* EncodingTestTest.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;
import org.junit.Test;

public class EncodingTestTest extends BrowserTest {

    List<String> verboseArg = Arrays.asList(new String[]{ServerAccess.VERBOSE_OPTION});
    private static final String arg = "ěščřžýáíé=!@#$%^*()_+ú)ů§.-?:_\"!'(/ěéřťÝúíóášďźžčň;+ĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ1";
    private static final String argEscaped = arg.replace("\"", "&quot;");
    private static final String utf = "UTF8";
    private static final String iso88592 = "ISO88592";
    private FileInputStream is;
    File[] utf8Files = server.getDir().listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.contains(utf);
        }
    });
    File[] iso88592Files = server.getDir().listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.contains(iso88592);
        }
    });

    @Test
    public void iso88592FileCanBeDecodedCorrectly() throws Exception {
        Assert.assertTrue("there must be more then 1 iso file in server's directory", iso88592Files.length > 0);
        for (int i = 0; i < iso88592Files.length; i++) {
            File f = iso88592Files[i];
            is = new FileInputStream(f);
            String ff = ServerAccess.getContentOfStream(is, "ISO-8859-2");
            ServerAccess.logOutputReprint(f.getName());
            ServerAccess.logOutputReprint(ff);
            Assert.assertTrue("file " + f.getName() + " should contain " + arg + " bud didn't", ff.contains(arg) || ff.contains(argEscaped));
        }
    }

    @Test
    public void iso88592FileCanBeDecodedWrongly() throws Exception {
        Assert.assertTrue("there must be more then 1 iso file in server's directory", iso88592Files.length > 0);
        for (int i = 0; i < iso88592Files.length; i++) {
            File f = iso88592Files[i];
            is = new FileInputStream(f);
            String ff = ServerAccess.getContentOfStream(is, "UTF-8");
            ServerAccess.logOutputReprint(f.getName());
            ServerAccess.logOutputReprint(ff);
            Assert.assertFalse("file " + f.getName() + " should NOT contain " + arg + " bud did", ff.contains(arg) || ff.contains(argEscaped));
        }
    }

    @Test
    public void utf8FileCanBeDecodedCorrectly() throws Exception {
        Assert.assertTrue("there must be more then 1 utf file in server's directory", utf8Files.length > 0);
        for (int i = 0; i < utf8Files.length; i++) {
            File f = utf8Files[i];
            is = new FileInputStream(f);
            String ff = ServerAccess.getContentOfStream(is, "UTF-8");
            ServerAccess.logOutputReprint(f.getName());
            ServerAccess.logOutputReprint(ff);
            Assert.assertTrue("file " + f.getName() + " should contain " + arg + " bud didn't", ff.contains(arg) || ff.contains(argEscaped));
        }
    }

    @Test
    public void utf8FileCanBeDecodedWrongly() throws Exception {
        Assert.assertTrue("there must be more then 1 utf file in server's directory", utf8Files.length > 0);
        for (int i = 0; i < utf8Files.length; i++) {
            File f = utf8Files[i];
            is = new FileInputStream(f);
            String ff = ServerAccess.getContentOfStream(is, "ISO-8859-2");
            ServerAccess.logOutputReprint(f.getName());
            ServerAccess.logOutputReprint(ff);
            Assert.assertFalse("file " + f.getName() + " should NOT contain " + arg + " bud did", ff.contains(arg) || ff.contains(argEscaped));
        }
    }

    @Test
    public void testEncodingTest1Utf8() throws Exception {
        testEncodingTest1(utf);
    }

    @Test
    @KnownToFail
    @Bug(id = "PR1108")
    public void testEncodingTest1Iso88592() throws Exception {
        testEncodingTest1(iso88592);
    }

    @Test
    public void testEncodingTest2Utf8() throws Exception {
        testEncodingTest2(utf);
    }

    @Test
    @KnownToFail
    @Bug(id = "PR1108")
    public void testEncodingTest2Iso88592() throws Exception {
        testEncodingTest2(iso88592);
    }

    @Test
    public void testEncodingTest3Utf8() throws Exception {
        testEncodingTest3(utf);
    }

    @Test
    @KnownToFail
    @Bug(id = "PR1108")
    public void testEncodingTest3Iso88592() throws Exception {
        testEncodingTest3(iso88592);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void testEncodingTest4Utf8() throws Exception {
        testEncodingTest4(utf);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void testEncodingTest4Iso88592() throws Exception {
        testEncodingTest4(iso88592);
    }

    @Test
    public void testEncodingTest5Utf8() throws Exception {
        testEncodingTest5(utf);
    }

    @Test
    @Bug(id = "PR1108")
    @KnownToFail
    public void testEncodingTest5Iso88592() throws Exception {
        testEncodingTest5(iso88592);
    }

    /**
     * launching simpletest1.jar from  encoding encoded jnlp
     */
    public void testEncodingTest1(String encoding) throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(verboseArg, "/encodingTest1-" + encoding + ".jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("encodingTest1 (in " + encoding + ") stdout should contain " + s + " bud didn't", pr.stdout.contains(s));
        //javaws in verbose mode is printing out readed jnlp. I'm no sure if the following test is relevant
        Assert.assertTrue("encodingTest1 (in " + encoding + ") stdout should contain " + arg + " bud didn't", pr.stdout.contains(arg));
    }

    /**
     * launching simpletest1.jar fromencoding file with utf8/ISO-8859-2 uncompatible characters
     */
    public void testEncodingTest2(String encoding) throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(verboseArg, "/encodingTest2ĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ-" + encoding + ".jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("encodingTest2ĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ (in " + encoding + ") stdout should contain " + s + " bud didn't", pr.stdout.contains(s));
        //javaws in verbose mode is printing out readed jnlp. I'm no sure if the following test is relevant
        Assert.assertTrue("encodingTest2ĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ (in " + encoding + ") stdout should contain " + arg + " bud didn't", pr.stdout.contains(arg));
    }

    /**
     * launching encodingTestsĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ.jar from encoding file with utf8/ISO-8859-2 uncompatible characters included also in args and jar filename
     */
    public void testEncodingTest3(String encoding) throws Exception {
        //not verbose in this case, this class is printing it's argument out
        ProcessResult pr = server.executeJavawsHeadless("/encodingTest3-" + encoding + ".jnlp");
        String s = "Encoded jar decoded correctly";
        Assert.assertTrue("encodingTest3 (in " + encoding + ") stdout should contain " + s + " bud didn't", pr.stdout.contains(s));
        Assert.assertTrue("encodingTest3 (in " + encoding + ") stdout should contain " + arg + " bud didn't", pr.stdout.contains(arg));
    }

    /**
     * launching encodingTestsĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ.jar from encoding file with utf8/ISO-8859-2 uncompatible characters included also in args and jar filename in browser
     */
    public void testEncodingTest4(String encoding) throws Exception {
        ProcessResult pr = server.executeBrowser("/encodingTest4-" + encoding + ".html");
        String s3 = "applet was initialised";
        Assert.assertTrue("encodingTest4 stdout should contains " + s3 + " bud didn't", pr.stdout.contains(s3));
        String s0 = "applet was started";
        Assert.assertTrue("encodingTest4 stdout should contains " + s0 + " bud didn't", pr.stdout.contains(s3));
        Assert.assertTrue("encodingTest4 (in " + encoding + ") stdout should contain " + arg + " bud didn't", pr.stdout.contains(arg));
    }

    /**
     * launching encodingTestsĚŠČŘŽÝÁÍÉĚÉŘŤÝÚŮÍÓÁŠĎŽŹŇ.jar from encoding file with utf8/ISO-8859-2 uncompatible characters included also in args and jar filename as applet by jnlp
     */
    public void testEncodingTest5(String encoding) throws Exception {
        //not verbose in this case, this class is printing it's argument out
        ProcessResult pr = server.executeJavawsHeadless("/encodingTest5-" + encoding + ".jnlp");
        String s3 = "applet was initialised";
        Assert.assertTrue("encodingTest5 stdout should contains " + s3 + " bud didn't", pr.stdout.contains(s3));
        String s0 = "applet was started";
        Assert.assertTrue("encodingTest5 stdout should contains " + s0 + " bud didn't", pr.stdout.contains(s3));
        Assert.assertTrue("encodingTest5 (in " + encoding + ") stdout should contain " + arg + " bud didn't", pr.stdout.contains(arg));
    }
}
