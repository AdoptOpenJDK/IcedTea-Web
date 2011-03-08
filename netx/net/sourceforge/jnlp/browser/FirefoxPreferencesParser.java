/* FirefoxPreferencesParser.java
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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * A parser for Firefox's preferences file. It can 'parse' Firefox's
 * preferences file and expose the prefrences in a simple to use format.
 * <p>
 * Sample usage:
 * <pre>
 * FirefoxPreferencesParser p = new FirefoxPreferencesParser(prefsFile);
 * p.parse();
 * Map&lt;String,String&gt; prefs = p.getPreferences();
 * System.out.println("blink allowed: " + prefs.get("browser.blink_allowed"));
 * </pre>
 */
public final class FirefoxPreferencesParser {

    File prefsFile = null;
    Map<String, String> prefs = null;

    /**
     * Creates a new FirefoxPreferencesParser
     * @param preferencesFile
     */
    public FirefoxPreferencesParser(File preferencesFile) {
        prefsFile = preferencesFile;
    }

    /**
     * Parse the prefernces file
     * @throws IOException if an exception ocurrs while reading the
     * preferences file.
     */
    public void parse() throws IOException {
        /*
         * The Firefox preference file is actually in javascript. It does seem
         * to be nicely formatted, so it should be possible to hack reading it.
         * The correct way of course is to use a javascript library and extract
         * the user_pref object
         */
        prefs = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new FileReader(prefsFile));

        try {
            while (true) {
                String line = reader.readLine();
                // end of stream
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (line.startsWith("user_pref")) {

                    /*
                     * each line is of the form: user_pref("key",value); where value
                     * can be a string in double quotes or an integer or float or
                     * boolean
                     */

                    boolean foundKey = false;
                    boolean foundValue = false;

                    // extract everything inside user_pref( and );
                    String pref = line.substring("user_pref(".length(), line.length() - 2);
                    // key and value are separated by a ,
                    int firstCommaPos = pref.indexOf(',');
                    if (firstCommaPos >= 1) {
                        String key = pref.substring(0, firstCommaPos).trim();
                        if (key.startsWith("\"") && key.endsWith("\"")) {
                            key = key.substring(1, key.length() - 1);
                            if (key.trim().length() > 0) {
                                foundKey = true;
                            }
                        }

                        if (pref.length() > firstCommaPos + 1) {
                            String value = pref.substring(firstCommaPos + 1).trim();
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1).trim();
                            }
                            foundValue = true;

                            if (foundKey && foundValue) {
                                // System.out.println("added (\"" + key + "\", \"" + value + "\")");
                                prefs.put(key, value);
                            }
                        }
                    }
                }
            }
        } finally {
            reader.close();
        }
        if (JNLPRuntime.isDebug()) {
            System.out.println("Read " + prefs.size() + " entries from Firefox's preferences");
        }
    }

    /**
     * Get the firefox preferences as a map (key,value pair). Note that
     * all values (including integers and booleans) are stored as a string, so
     * conversion to an appropriate type may be required.
     *
     * @return a map containing firefox' preferences
     */
    public Map<String, String> getPreferences() {
        HashMap<String, String> newMap = new HashMap<String, String>();
        newMap.putAll(prefs);
        return newMap;
    }

}
