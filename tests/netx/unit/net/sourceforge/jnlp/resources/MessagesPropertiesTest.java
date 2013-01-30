/* Copyright (C) 2012 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 IcedTea is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
 exception statement from your version. */
package net.sourceforge.jnlp.resources;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import net.sourceforge.jnlp.ServerAccess;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessagesPropertiesTest {

    public static final String mainFile = "Messages";
    public static final String[] secondaryCountries = new String[]{"cs"};
    public static final String[] secondaryLanguages = new String[]{"CZ"};
    public static ResourceBundle main;
    public static ResourceBundle[] secondary = new ResourceBundle[secondaryCountries.length];

    @BeforeClass
    public static void loadResourceBoundels() {
        assertTrue("length of countries and languages must be same", secondaryCountries.length == secondaryLanguages.length);
        //get default by non existing language and country
        main = ResourceBundle.getBundle("net.sourceforge.jnlp.resources." + mainFile, new Locale("dfgdfg", "gvff"));
        secondary = new ResourceBundle[secondaryCountries.length];
        assertNotNull(main);
        for (int i = 0; i < secondaryCountries.length; i++) {
            String country = secondaryCountries[i];
            String language = secondaryLanguages[i];
            secondary[i] = ResourceBundle.getBundle("net.sourceforge.jnlp.resources." + mainFile, new Locale(country, language));
            assertNotNull(secondary[i]);
        }
    }

    @Test
    public void allResourcesAreReallyDifferent() {
        List<String> ids = new ArrayList<String>(secondary.length + 1);
        ids.add("default");
        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>(secondary.length + 1);
        bundles.add(main);
        int errors = 0;
        for (int i = 0; i < secondaryCountries.length; i++) {
            String country = secondaryCountries[i];
            String language = secondaryLanguages[i];
            ids.add(country + "_" + language);
            bundles.add(secondary[i]);

        }
        for (int i = 0; i < bundles.size(); i++) {
            ResourceBundle resourceBundle1 = bundles.get(i);
            String id1 = ids.get(i);
            Enumeration<String> keys1 = resourceBundle1.getKeys();
            for (int j = 0; j < bundles.size(); j++) {
                if (i == j) {
                    break;
                }
                ResourceBundle resourceBundle2 = bundles.get(j);
                String id2 = ids.get(j);
                outLog("Checking for same items between " + resourceBundle1.getLocale() + " x " + resourceBundle2.getLocale() + " (should be " + id1 + " x " + id2 + ")");
                errLog("Checking for same items between " + resourceBundle1.getLocale() + " x " + resourceBundle2.getLocale() + " (should be " + id1 + " x " + id2 + ")");
                int localErrors=0;
                while (keys1.hasMoreElements()) {
                    String key = (String) keys1.nextElement();
                    String val1 = getMissingResourceAsEmpty(resourceBundle1, key);
                    String val2 = getMissingResourceAsEmpty(resourceBundle2, key);
                    outLog("\""+val1+"\" x \""+val2);
                    if (val1.trim().equalsIgnoreCase(val2.trim())) {
                        if (val1.trim().length() <= 5 /*"ok", "", ...*/ || val1.toLowerCase().contains("://") /*urls...*/) {
                            errLog("Warning! Items equals for: " + key + " = " + val1 + " but are in allowed subset");
                        } else {
                            errors++;
                            localErrors++;
                            errLog("Error! Items equals for: " + key + " = " + val1);
                        }
                    }
                }
                errLog(localErrors+" errors allResourcesAreReallyDifferent fo "+id2+" x "+id1);

            }
        }
        assertTrue("Several - " + errors + " - items are same in  bundles. See error logs for details", errors == 0);
    }

    private String getMissingResourceAsEmpty(ResourceBundle res, String key) {
        try {
            return res.getString(key);
        } catch (MissingResourceException ex) {
            return "";
        }
    }

    @Test
    //it is not critical that some localisations are missing, however good to know    
    public void warnForNotLocalisedStrings() {
        int errors = 0;
        Enumeration<String> keys = main.getKeys();
        for (int i = 0; i < secondary.length; i++) {
            int localErrors = 0;
            ResourceBundle sec = secondary[i];
            String country = secondaryCountries[i];
            String language = secondaryLanguages[i];
            String id = country + "_" + language;
            outLog("Checking for missing  strings in " + sec.getLocale() + " (should be " + id + ") compared with default");
            errLog("Checking for missing  strings in " + sec.getLocale() + " (should be " + id + ") compared with default");
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String val1 = getMissingResourceAsEmpty(main, key);
                String val2 = getMissingResourceAsEmpty(sec, key);
                outLog("\""+val1+"\" x \""+val2);
                if (val1.trim().isEmpty()) {
                } else {
                    if (val2.trim().isEmpty()){
                    errors++;
                    localErrors++;
                    errLog("Error! There is value for default: " + key + ", but for " + id+" is missing");
                    }

                }
            }
            errLog(localErrors+" warnForNotLocalisedStrings errors for "+id);

        }
        assertTrue("Several - " + errors + " - items have missing localization. See error logs for details", errors == 0);
    }
    

    
    @Test
    public void noEmptyResources() {
        List<String> ids = new ArrayList<String>(secondary.length + 1);
        ids.add("default");
        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>(secondary.length + 1);
        bundles.add(main);
        int errors = 0;
        for (int i = 0; i < secondaryCountries.length; i++) {
            String country = secondaryCountries[i];
            String language = secondaryLanguages[i];
            ids.add(country + "_" + language);
            bundles.add(secondary[i]);

        }
        for (int i = 0; i < bundles.size(); i++) {
            ResourceBundle resourceBundle = bundles.get(i);
            String id = ids.get(i);
            Enumeration<String> keys = resourceBundle.getKeys();
                outLog("Checking for empty items in " + resourceBundle.getLocale() + "  (should be " + id + ")");
                errLog("Checking for empty items in " + resourceBundle.getLocale() + "  (should be " + id + ")");
                int localErrors=0;
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String val = getMissingResourceAsEmpty(resourceBundle, key);
                    outLog("\""+key+"\" = \""+val);
                        if (val.trim().isEmpty()) {
                            errors++;
                            localErrors++;
                            errLog("Error! Key: " + key + " have no vlue");
                        }
                    
                }
                errLog(localErrors+" noEmptyResources errors for "+id);

        }
        assertTrue("Several - " + errors + " - items  have no values", errors == 0);
    }
    
    
    @Test
    public void findKeysWhichAreInLocalisedButNotInDefault() {
        int errors = 0;
        for (int i = 0; i < secondary.length; i++) {
            int localErrors = 0;
            ResourceBundle sec = secondary[i];
            Enumeration<String> keys = sec.getKeys();
            String country = secondaryCountries[i];
            String language = secondaryLanguages[i];
            String id = country + "_" + language;
            outLog("Checking for redundant keys in " + sec.getLocale() + " (should be " + id + ") compared with default");
            errLog("Checking for redundant keys in " + sec.getLocale() + " (should be " + id + ") compared with default");
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String val2 = getMissingResourceAsEmpty(main, key);
                String val1 = getMissingResourceAsEmpty(sec, key);
                outLog("\""+val1+"\" x \""+val2);
                    if (val2.trim().isEmpty() && !val1.trim().isEmpty()){
                    errors++;
                    localErrors++;
                    errLog("Error! There is value for "+id+", key " + key + ", but for default is missing");
                    }

            }
            errLog(localErrors+" findKeysWhichAreInLocalisedButNotInDefault errors for "+id);

        }
        assertTrue("Several - " + errors + " - items  have value in localized version but not in default one", errors == 0);
    }
    


    private void errLog(String string) {
        ServerAccess.logErrorReprint(string);
    }

    private void outLog(String string) {
        ServerAccess.logOutputReprint(string);
    }
}
