/* 
Copyright (C) 2014 Red Hat, Inc.

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

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JavaFxTest {

    private static final String correct = "jnlp-javafx started";
    private static final String done = "jnlp-javafx can be terminated";
    public static final ServerAccess server = new ServerAccess();

    /*
     * Randomly seeing:
     * 
(javaws:13906): Gdk-ERROR **: The program 'javaws' received an X Window System error.
This probably reflects a bug in the program.
The error was 'RenderBadPicture (invalid Picture parameter)'.
  (Details: serial 7754 error_code 141 request_code 138 minor_code 7)
  (Note to programmers: normally, X errors are reported asynchronously;
   that is, you will receive the error a while after causing it.
   To debug your program, run it with the --sync command line
   option to change this behavior. You can then get a meaningful
   backtrace from your debugger if you break on the gdk_x_error() function.)
    
     *  Suprsing is, that awt splasshcreen is visible, and after it also itw error dialogue is visible
     */
    @Test
    @NeedsDisplay
    public void testJavawsJNLP() throws Exception {
        ProcessResult pr = server.executeJavaws(
                Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.NOSEC.option, OptionsDefinitions.OPTIONS.HEADLESS.option}),
                "/JavaFx.jnlp",
                new StringBasedClosingListener(done),
                new StringBasedClosingListener("xceptionxception"));
        System.out.println(pr.stdout);
        System.out.println(pr.stderr);
        Assert.assertTrue("stdout should contain " + correct + ", but it didnt.", pr.stdout.contains(correct));
    }
}
