/* BrowserFactory.java
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

package net.adoptopenjdk.icedteaweb.testing.browsertesting;

import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.annotations.TestInBrowsers;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Chrome;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Chromium;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Epiphany;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Firefox;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Midory;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.Opera;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USED_BROWSERS;

public class BrowserFactory {

    private static final BrowserFactory factory = new BrowserFactory(System.getProperty(USED_BROWSERS));
    private final List<Browser> configuredBrowsers;
    private final Random oneGenerator = new Random();

    public static BrowserFactory getFactory() {
        return factory;
    }

    /**
     * This is public just for testing purposes!
     */
    public BrowserFactory(final String browsers) {
        if (browsers == null) {
            configuredBrowsers = new ArrayList<>(0);
        } else {
            final String[] s = browsers.split(File.pathSeparator);
            configuredBrowsers = new ArrayList<>(s.length);
            for (final String value : s) {
                String string = value;
                String[] p = string.split("/");
                if (p.length > 1) {
                    string = p[p.length - 1];
                }
                if (string.equals(Browsers.chromiumBrowser.toString())) {
                    configuredBrowsers.add(new Chromium(value));
                }
                if (string.equals(Browsers.googleChrome.toString())) {
                    configuredBrowsers.add(new Chrome(value));
                }
                if (string.equals(Browsers.opera.toString())) {
                    configuredBrowsers.add(new Opera(value));
                }
                if (string.equals(Browsers.firefox.toString())) {
                    configuredBrowsers.add(new Firefox(value));
                }
                if (string.equals(Browsers.epiphany.toString())) {
                    configuredBrowsers.add(new Epiphany(value));
                }
                if (string.equals(Browsers.midori.toString())) {
                    configuredBrowsers.add(new Midory(value));
                }
            }
        }

    }

    public Browser getBrowser(final Browsers id) {
        for (final Browser browser : configuredBrowsers) {
            if (browser.getID() == id) {
                return browser;
            }

        }
        return null;

    }

    public Browser getFirst() {
        return configuredBrowsers.stream()
                .findFirst()
                .orElse(null);
    }

    private Browser getRandom() {
        if (configuredBrowsers.isEmpty()){
            return null;
        }
        return configuredBrowsers.get(oneGenerator.nextInt(configuredBrowsers.size()));
    }

    public List<Browser> getAllBrowsers() {
        return Collections.unmodifiableList(configuredBrowsers);
    }

    public List<Browsers> getBrowsers(final TestInBrowsers tib) {
        return getBrowsers(tib.testIn());
    }

    public List<Browsers> getBrowsers(final Browsers[] testIn) {
         final List<Browser> q = translateAnnotationSilently(testIn);
         if (q==null || q.isEmpty()){
             final List<Browsers> qq = new ArrayList<>(0);
               qq.add(Browsers.none);
               return qq;
            }
         final List<Browsers> qq = new ArrayList<>(q.size());
         for (final Browser browser : q) {
             qq.add(browser.getID());
         }
         return qq;

     }
    /**
     *
     * @param testIn Bbrowsers which should be transformed to list of Browser
     * @return all matching browser, if browser do not exists, this is ignored and run is silently continued
     */
    private List<Browser> translateAnnotationSilently(final Browsers[] testIn) {
        if (testIn==null) {
            return null;
        }
        final List<Browser> r = new ArrayList<>(configuredBrowsers.size());
        for (final Browsers b : testIn) {
            if (b == Browsers.all) {
                if (getAllBrowsers().isEmpty()) {
                    ServerAccess.logErrorReprint("You try to add all browsers, but there is none");
                } else {
                    r.addAll(getAllBrowsers());
                }
            } else if (b == Browsers.one) {
                final Browser bb = getRandom();
                if (bb == null) {
                    ServerAccess.logErrorReprint("You try to add random browser, but there is none");
                } else {
                    r.add(bb);
                }
            } else {
                final Browser bb = getBrowser(b);
                if (bb == null) {
                    ServerAccess.logErrorReprint("You try to add " + b.toString() + " browser, but it do not exists");
                } else {
                    r.add(bb);
                }

            }
        }

        return r;

    }
}
