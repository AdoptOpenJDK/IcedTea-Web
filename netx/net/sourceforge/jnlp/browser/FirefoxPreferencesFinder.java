/* FirefoxPreferencesFinder.java
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

package net.sourceforge.jnlp.browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Finds the file corresponding to firefox's (default) preferences file
 */
public class FirefoxPreferencesFinder {

    /**
     * Returns a file object representing firefox's preferences file
     *
     * @return a File object representing the preferences file.
     * @throws FileNotFoundException if the preferences file could not be found
     * @throws IOException if an exception occurs while trying to identify the
     * location of the preferences file.
     */
    public static File find() throws IOException {

        String configPath = System.getProperty("user.home") + File.separator + ".mozilla"
                + File.separator + "firefox" + File.separator;

        String profilesPath = configPath + "profiles.ini";

        if (!(new File(profilesPath).isFile())) {
            throw new FileNotFoundException(profilesPath);
        }

        OutputController.getLogger().log("Using firefox's profiles file: " + profilesPath);

        BufferedReader reader = new BufferedReader(new FileReader(profilesPath));

        List<String> linesInSection = new ArrayList<String>();
        boolean foundDefaultSection = false;

        /*
         * The profiles.ini file is an ini file. This is a quick hack to read
         * it. It is very likely to break given anything strange.
         */

        // find the section with an entry Default=1
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (line.startsWith("[Profile") && line.endsWith("]")) {
                    if (foundDefaultSection) {
                        break;
                    }
                    // new section
                    linesInSection = new ArrayList<String>();
                } else {
                    linesInSection.add(line);
                    int equalSignPos = line.indexOf('=');
                    if (equalSignPos > 0) {
                        String key = line.substring(0, equalSignPos).trim();
                        String value = line.substring(equalSignPos+1).trim();
                        if (key.toLowerCase().equals("default") && value.equals("1")) {
                            foundDefaultSection = true;
                        }
                    }
                }
            }
        } finally {
            reader.close();
        }

        if (!foundDefaultSection && linesInSection.size() == 0) {
            throw new FileNotFoundException("preferences file");
        }

        String path = null;
        for (String line : linesInSection) {
            if (line.startsWith("Path=")) {
                path = line.substring("Path=".length());
            }
        }

        if (path == null) {
            throw new FileNotFoundException("preferences file");
        } else {
            String fullPath = configPath + path + File.separator + "prefs.js";
            OutputController.getLogger().log("Found preferences file: " + fullPath);
            return new File(fullPath);
        }
    }

}
