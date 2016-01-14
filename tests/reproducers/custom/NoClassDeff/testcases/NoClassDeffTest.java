/* NoClassDeffTest.java
 Copyright (C) 2013 Red Hat, Inc.

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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import net.sourceforge.jnlp.util.FileUtils;

import org.junit.Assert;
import org.junit.Test;

@Bug(id = "PR2219")
/**
 *
 * When NoClassDefFound is thrown from ITW. Current behaviour: javaws app
 * correctly dies browsers and -html correctly dies
 *
 * javaws applet SURVIVES init and start throwing this. But correctly dies in
 * paint
 *
 *
 */
public class NoClassDeffTest extends BrowserTest {

    private class NoClassDefFoundErrorClosingListener extends StringBasedClosingListener {

        public NoClassDefFoundErrorClosingListener() {
            super(NoClassDefFoundError.class.getSimpleName());
        }

    }

    private static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    private static final String[] HTMLA = new String[]{OptionsDefinitions.OPTIONS.HTML.option};
    private static final List<String> HTMLL = Arrays.asList(HTMLA);

    private static final String JNLPAPP = "NoClassDeffApp.jnlp";
    private static final String JNLPAPPLET = "NoClassDeffApplet.jnlp";
    private static final String HTML = "NoClassDeff.html";
    private static final String HTMLHREF = "NoClassDeffJnlpHref.html";

    //jnlp app OK run
    @Test
    @NeedsDisplay
    public void noClassDeffTestWorksJnlp1() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("main1"));
        Assert.assertTrue(pr.stdout.contains("main2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
        Assert.assertFalse(pr.stdout.contains("EX: "));
    }

    //jnlp bad run 1
    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowJnlp1() throws Exception {
        prepare("main", false);
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("main1"));
        Assert.assertFalse(pr.stdout.contains("main2"));
        Assert.assertFalse(pr.stdout.contains(appletCloseString));
        Assert.assertFalse(pr.stdout.contains("EX: "));
    }

    //jnlp bad run 2
    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowCatchJnlp1() throws Exception {
        prepare("main", true);
        ProcessResult pr = server.executeJavaws(JNLPAPP, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("main1"));
        Assert.assertTrue(pr.stdout.contains("main2"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    //applets  OK run
    //opera should go correctly  up to destroy WITHOUT paint
    //epiphany only start and init
    //midori up to paint
    //firefox, no data
    //-html and jnlp applet get init, start, paint
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestWorksHtml1() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestWorksHtml2() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestWorksJnlp2() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestWorksJavawsHtml1() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTML, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestWorksJavawsHtml2() throws Exception {
        prepare("okRun", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertFalse(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    //jnlp applets crashes
    //they behave a bit differently form browser ones
    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsInitJnlp2() throws Exception {
        prepare("init", false);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertFalse(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertFalse(pr.stdout.contains("init2"));
        Assert.assertFalse(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsStartJnlp2() throws Exception {
        prepare("start", false);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertFalse(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsPaintJnlp2() throws Exception {
        prepare("paint", false);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertFalse(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertFalse(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsCatchInitJnlp2() throws Exception {
        prepare("init", true);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsCatchStartJnlp2() throws Exception {
        prepare("start", true);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    public void noClassDeffTestThrowsCatchPaintJnlp2() throws Exception {
        prepare("paint", true);
        ProcessResult pr = server.executeJavaws(JNLPAPPLET, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
        Assert.assertTrue(pr.stdout.contains("paint1"));
        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    //-html and browser crashes
    //
    //applets  crash init 1
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsInitHtml1() throws Exception {
        prepare("init", false);
        ProcessResult pr = server.executeBrowser(HTML, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertFalse(pr.stdout.contains("init2"));
        Assert.assertFalse(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsInitHtml2() throws Exception {
        prepare("init", false);
        ProcessResult pr = server.executeBrowser(HTMLHREF, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertFalse(pr.stdout.contains("init2"));
        Assert.assertFalse(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsInitJavawsHtml1() throws Exception {
        prepare("init", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTML, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertFalse(pr.stdout.contains("init2"));
        Assert.assertFalse(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsInitJavawsHtml2() throws Exception {
        prepare("init", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTMLHREF, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertFalse(pr.stdout.contains("init2"));
        Assert.assertFalse(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertFalse(pr.stdout.contains(appletCloseString));
    }

    //applets  crash start 1
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsStartHtml1() throws Exception {
        prepare("start", false);
        ProcessResult pr = server.executeBrowser(HTML, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsStartHtml2() throws Exception {
        prepare("start", false);
        ProcessResult pr = server.executeBrowser(HTMLHREF, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsStartJavawsHtml1() throws Exception {
        prepare("start", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTML, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsStartJavawsHtml2() throws Exception {
        prepare("start", false);
        ProcessResult pr = server.executeJavaws(HTMLL, HTMLHREF, null, new NoClassDefFoundErrorClosingListener());
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertFalse(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertFalse(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertFalse(pr.stdout.contains(appletCloseString));
    }

    private void prepare(String when, boolean catchError) throws IOException {
        File dir = ServerAccess.getInstance().getDir();
        String[] files = new String[]{"NoClassDeffApp.jnlp", "NoClassDeff.html", "NoClassDeffApplet.jnlp", "NoClassDeffJnlpHref.html"};
        for (String file : files) {
            String s = FileUtils.loadFileAsString(new File(dir, file + ".in"));
            s = s.replaceAll("DIE_ON_STAGE", when);
            s = s.replaceAll("CATCH_ERROR", String.valueOf(catchError));
            FileUtils.saveFile(s, new File(dir, file));
        }
    }

    //applets  crash init 2
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchInitHtml1() throws Exception {
        prepare("init", true);
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchInitHtml2() throws Exception {
        prepare("init", true);
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchInitJavawsHtml1() throws Exception {
        prepare("init", true);
        ProcessResult pr = server.executeJavaws(HTMLL, HTML, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchInitJavawsHtml2() throws Exception {
        prepare("init", true);
        ProcessResult pr = server.executeJavaws(HTMLL, HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    //applets  crash start 2
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchStartHtml1() throws Exception {
        prepare("start", true);
        ProcessResult pr = server.executeBrowser(HTML, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchStartHtml2() throws Exception {
        prepare("start", true);
        ProcessResult pr = server.executeBrowser(HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
//        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchStartJavawsHtml1() throws Exception {
        prepare("start", true);
        ProcessResult pr = server.executeJavaws(HTMLL, HTML, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    public void noClassDeffTestThrowsCatchStartJavawsHtml2() throws Exception {
        prepare("start", true);
        ProcessResult pr = server.executeJavaws(HTMLL, HTMLHREF, new AutoOkClosingListener(), null);
        Assert.assertTrue(pr.stdout.contains("Loading LostClass"));
        Assert.assertTrue(pr.stdout.contains("EX: "));
        Assert.assertTrue(pr.stderr.contains(NoClassDefFoundError.class.getSimpleName()));
        Assert.assertTrue(pr.stdout.contains("init1"));
        Assert.assertTrue(pr.stdout.contains("init2"));
        Assert.assertTrue(pr.stdout.contains("start1"));
        Assert.assertTrue(pr.stdout.contains("start2"));
//        Assert.assertTrue(pr.stdout.contains("paint1"));
//        Assert.assertTrue(pr.stdout.contains("paint2"));
//        Assert.assertTrue(pr.stdout.contains("stop1"));
//        Assert.assertTrue(pr.stdout.contains("stop2"));
//        Assert.assertTrue(pr.stdout.contains("destroy1"));
//        Assert.assertTrue(pr.stdout.contains("destroy2"));
        Assert.assertTrue(pr.stdout.contains(appletCloseString));
    }

  

}
