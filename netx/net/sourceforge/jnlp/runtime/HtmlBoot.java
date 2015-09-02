/*   Copyright (C) 2011 Red Hat, Inc.

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
 exception
 */
package net.sourceforge.jnlp.runtime;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sourceforge.jnlp.JNLPSplashScreen;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.html.AppletExtractor;
import net.sourceforge.jnlp.runtime.html.AppletParser;
import net.sourceforge.jnlp.runtime.html.AppletsFilter;
import net.sourceforge.jnlp.util.ScreenFinder;
import net.sourceforge.jnlp.util.logging.OutputController;

import static net.sourceforge.jnlp.runtime.Translator.R;
import net.sourceforge.jnlp.util.optionparser.OptionParser;

public final class HtmlBoot {

    private final OptionParser optionParser;

    public HtmlBoot(OptionParser optionParser) {
        this.optionParser = optionParser;
    }

    private JFrame invokePluginMain(PluginBridge pb, URL html) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class c = Class.forName("sun.applet.PluginMain");
        Method m = c.getMethod("javawsHtmlMain", PluginBridge.class, URL.class);
        return (JFrame) m.invoke(null, pb, html);
    }

    private static void changeMovement(int[] move) {
        if (move[0] > 0) {
            if (move[0] % 2 == 0) {
                move[1] += 100;
            } else {
                move[2] += 100;
            }
        }
    }

    private Point changeMovementSigns(int[] move) {
        int x = move[1];
        int y = move[2];
        switch (move[0] % 4) {
            case 0:
                x = -x;
                y = y;
                break;
            case 1:
                x = x;
                y = y;
                break;
            case 2:
                x = x;
                y = -y;
                break;
            case 3:
                x = -x;
                y = -y;
                break;
        }
        return new Point(x, y);
    }

    private JNLPSplashScreen splashScreen;

    boolean run(Map<String, List<String>> extra) {
        if (!optionParser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS)) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    splashScreen = new JNLPSplashScreen(null, null);
                    splashScreen.setSplashImageURL(null);
                    splashScreen.setVisible(true);
                }
            ;
        }
        );
        }
        List<String> vars = optionParser.getParams(OptionsDefinitions.OPTIONS.HTML);
        JNLPRuntime.setForksAllowed(false);//needed?
        ParserSettings settings = Boot.init(extra);
        if (settings == null) {
            return false;
        }
        try {
            OutputController.getLogger().log("Proceeding with html");
            final URL html = Boot.getFileLocation();
            AppletExtractor axe = new AppletExtractor(html, settings);
            AppletsFilter filtered = new AppletsFilter(axe.findAppletsOnPage(), html, vars.subList(1, vars.size()));
            List<AppletParser> applets = filtered.getApplets();
            // this hack was needed in early phases of the patch.   Now it sees to be not neede. Keeping inside to remove after much more testing
            // will be replaced by regular JNLPRuntime is initialised
//                System.setSecurityManager(new SecurityManager() {
//
//                    @Override
//                    public void checkPermission(Permission perm) {
//                        //
//                    }
//
//                });
            final int[] move = new int[]{0, 0, 0};
            for (AppletParser appletParser : applets) {
                //place the applets correctly over screen
                changeMovement(move);
                final PluginBridge pb = appletParser.toPluginBridge();
                if (splashScreen != null) {
                    splashScreen.setFile(pb);
                }
                final JFrame f = invokePluginMain(pb, html);
                //close all applets in time
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Point movement = changeMovementSigns(move);
                        f.pack();
                        ScreenFinder.centerWindowsToCurrentScreen(f);
                        Rectangle r = f.getBounds();
                        r.x += movement.x;
                        r.y += movement.y;
                        f.setBounds(r);
                        f.setVisible(true);
                    }
                });
                move[0]++;
            }
            if (splashScreen != null) {
                splashScreen.stopAnimation();
                splashScreen.setVisible(false);
                splashScreen.dispose();
            }
        } catch (final Exception ex) {
            OutputController.getLogger().log(ex);
            if (splashScreen != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        splashScreen.setErrorSplash(ex);
                        splashScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        splashScreen.addWindowListener(new WindowAdapter() {

                            @Override
                            public void windowClosed(WindowEvent e) {
                                Boot.fatalError(R("RUnexpected", ex.toString(), ex.getStackTrace()[0]));
                            }

                        });
                    }
                });
            } else {
                Boot.fatalError(R("RUnexpected", ex.toString(), ex.getStackTrace()[0]));
            }
        }
        return true;
    }
}
