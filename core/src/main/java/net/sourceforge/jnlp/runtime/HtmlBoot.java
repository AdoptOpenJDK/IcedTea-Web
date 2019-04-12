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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.splashscreen.JNLPSplashScreen;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.PluginBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public final class HtmlBoot {

    private final static Logger LOG = LoggerFactory.getLogger(HtmlBoot.class);

    private final CommandLineOptionsParser optionParser;

    public HtmlBoot(CommandLineOptionsParser optionParser) {
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
        try {
            if (!optionParser.hasOption(CommandLineOptions.HEADLESS)) {
                SwingUtils.invokeLater(new Runnable() {

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
            JNLPRuntime.setForksAllowed(false);//needed?
            ParserSettings settings = Boot.init(extra);
            if (settings == null) {
                return false;
            }
            if (splashScreen != null) {
                splashScreen.stopAnimation();
                splashScreen.setVisible(false);
                splashScreen.dispose();
            }
        } catch (final Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            if (splashScreen != null) {
                SwingUtils.invokeLater(new Runnable() {

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
