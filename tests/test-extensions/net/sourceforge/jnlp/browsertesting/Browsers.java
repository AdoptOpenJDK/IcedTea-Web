/* Browsers.java
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

package net.sourceforge.jnlp.browsertesting;

import java.io.File;
import net.sourceforge.jnlp.browsertesting.browsers.LinuxBrowser;

/**
 * When all represent all configured browser, one represens one random
 * (the first found) configured browser. Each other represents inidivdual browsers
 * 
 */
public enum Browsers {

   none, all, one, opera, googleChrome, chromiumBrowser, firefox, midori,epiphany;

    public static final String CHROMIUM;

    static {
        final String def = "chromium";
        final String alt = "chromium-browser";
        if (new File(LinuxBrowser.DEFAULT_BIN_PATH, alt).exists()) {
            CHROMIUM = alt;
        } else {
            CHROMIUM = def;
        }
    }

    public String toExec() {
        switch (this) {
            case opera:
                return "opera";
            case googleChrome:
                return "google-chrome";
            case chromiumBrowser:
                return CHROMIUM;
            case firefox:
                return "firefox";
            case midori:
                return "midori";
            case epiphany:
                return "epiphany";
            default:
                return null;

        }
    }

    @Override
     public String toString() {
        if (toExec()!=null) return  toExec();
        switch (this) {
            case all:
                return "all";
            case one:
                return "one";
             case none:
                return "unset_browser";
           default:  return "unknown";

        }
    }
}
