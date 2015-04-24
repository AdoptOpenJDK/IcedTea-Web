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
import java.util.Arrays;
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

    private final static class LocalesIdentifier {
        
        public static final LocalesIdentifier DEFAULT = new LocalesIdentifier("","");
        public static final LocalesIdentifier CZ = new LocalesIdentifier("cs");
        //public static final LocalesIdentifier CZ_CS = new LocalesIdentifier("CZ","cs");
        public static final LocalesIdentifier DE = new LocalesIdentifier("de");
        public static final LocalesIdentifier PL = new LocalesIdentifier("pl");
        //public static final LocalesIdentifier DE_DE = new LocalesIdentifier("DE","de");
        
        public static final String mainFileName = "Messages";
        public static final String pckg = "net.sourceforge.jnlp.resources";
        private final String country;
        private final String language;
        private final Locale locale;
        private final ResourceBundle bundle;

        public LocalesIdentifier(String country, String language) {
            this.country = country;
            this.language = language;
            if (getCountry().equals("") && getLanguage().equals("")){
                locale = new Locale("unknown_so_default", "unknown_so_default");
            } else {
                //get default by non existing language and country
            locale = new Locale(language, country);
            }
            bundle = ResourceBundle.getBundle(pckg+"." + mainFileName, locale);
        }

        public LocalesIdentifier(String language) {
            this.country = null;
            this.language = language;
            locale = new Locale(language);
            bundle = ResourceBundle.getBundle(pckg+"." + mainFileName, locale);
        }

        public String getCountry() {
            if (country == null) {
                return "";
            }
            return country.trim();
        }

        public String getLanguage() {
            if (language == null) {
                return "";
            }
            return language.trim();
        }

        public ResourceBundle getBundle() {
            return bundle;
        }

        public Locale getLocale() {
            return locale;
        }
        
        

        public String getId() {
            if (getLanguage().equals("")) {
                return getCountry();
            }
            if (getCountry().equals("")) {
                return getLanguage();
            }
            return getLanguage() + "_" + getCountry();
        }
        
          public String getIdentifier() {
            if (getId().equals("")) {
                return "default";
            }
            return getId();
        }

        @Override
        public String toString() {
            return pckg+"."+mainFileName+"_"+getId();
        }


        
     
        
        
    }
   
    private static LocalesIdentifier main;
    private static LocalesIdentifier[] secondary;

    @BeforeClass
    public static void loadResourceBoundels() {
        //get default by non existing language and country
        main = LocalesIdentifier.DEFAULT;
        assertNotNull(main);
        secondary= new LocalesIdentifier[] {LocalesIdentifier.CZ, LocalesIdentifier.DE, LocalesIdentifier.PL};
        assertNotNull(secondary);
        for (LocalesIdentifier secondary1 : secondary) {
            assertNotNull(secondary1);
        }
    }

    @Test
    public void allResourcesAreReallyDifferent() {
        List<LocalesIdentifier> bundles = new ArrayList<>(secondary.length + 1);
        String detailResults="";
        int errors = 0;
        bundles.addAll(Arrays.asList(secondary));
        for (LocalesIdentifier resourceBundle1 : bundles) {
            Enumeration<String> keys1 = resourceBundle1.getBundle().getKeys();
            LocalesIdentifier resourceBundle2 = main;
            allLog("Checking for same items between " + resourceBundle1.getLocale() + " x " + resourceBundle2.getLocale() + " (should be " + resourceBundle1.getIdentifier() + " x " + resourceBundle2.getIdentifier() + ")");
            int localErrors=0;
            while (keys1.hasMoreElements()) {
                String key = keys1.nextElement();
                String val1 = getMissingResourceAsEmpty(resourceBundle1.getBundle(), key);
                if (val1.length() > 1000) {
                    errLog("Skipping check of: " + key + " too long. (" + val1.length() + ")"); 
                    continue;
                }
                String val2 = getMissingResourceAsEmpty(resourceBundle2.getBundle(), key);
                outLog("\""+val1+"\" x \""+val2);
                if (val1.trim().equalsIgnoreCase(val2.trim())) {
                    if (val1.trim().length() <= 5 /* short words like"ok", "", ...*/
                            || val1.toLowerCase().contains("://") /*urls...*/
                            || !val1.trim().contains(" ") /*one word*/
                            || val1.replaceAll("\\{\\d\\}", "").trim().length()<5 /*only vars and short words*/
                            //white list
                            || (val1.trim().equals("std. err"))
                            || (val1.trim().equals("std. out"))
                            || (val1.trim().equals("Policy Editor"))
                            || (val1.trim().equals("Java Reflection"))
                            || (val1.trim().equals("javaws html"))
                            || (val1.trim().matches("Minimum: .* Maximum: .*"))
                            || (val1.trim().equals("jnlp href"))
                            || (val1.trim().equals("GNU Lesser General Public License."))
                                         )
                    {
                        errLog("Warning! Items equals for: " + key + " = " + val1 + " but are in allowed subset");
                    } else {
                        errors++;
                        localErrors++;
                        errLog("Error! Items equals for: " + key + " = " + val1);
                    }
                }
            }
            if (localErrors > 0){
                detailResults+=resourceBundle1.getIdentifier()+" x "+resourceBundle2.getIdentifier()+": "+localErrors+";";
            }
            errLog(localErrors+" errors allResourcesAreReallyDifferent fo "+resourceBundle1.getIdentifier()+" x "+resourceBundle2.getIdentifier());
        }
        assertTrue("Several - " + errors + " - items are same in  bundles. See error logs for details: "+detailResults, errors == 0);
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
    //and actually this test sis covered by allResourcesAreReallyDifferent, because fallback is geting default value for unknnow localisation
    public void warnForNotLocalisedStrings() {
        int errors = 0;
        Enumeration<String> keys = main.getBundle().getKeys();
        for (LocalesIdentifier secondary1 : secondary) {
            int localErrors = 0;
            ResourceBundle sec = secondary1.getBundle();
            String id = secondary1.getIdentifier();
            allLog("Checking for missing  strings in " + sec.getLocale() + " (should be " + id + ") compared with default");
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String val1 = getMissingResourceAsEmpty(main.getBundle(), key);
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
         List<LocalesIdentifier> bundles = new ArrayList<>(secondary.length + 1);
        bundles.add(main);
        int errors = 0;
        bundles.addAll(Arrays.asList(secondary));
        for (LocalesIdentifier bundle : bundles) {
            ResourceBundle resourceBundle = bundle.getBundle();
            String id = bundle.getIdentifier();
            Enumeration<String> keys = resourceBundle.getKeys();
            allLog("Checking for empty items in " + resourceBundle.getLocale() + "  (should be " + id + ")");
            int localErrors=0;
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
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
        for (LocalesIdentifier secondary1 : secondary) {
            int localErrors = 0;
            ResourceBundle sec = secondary1.getBundle();
            Enumeration<String> keys = sec.getKeys();
            String id = secondary1.getId();
            outLog("Checking for redundant keys in " + sec.getLocale() + " (should be " + id + ") compared with default");
            errLog("Checking for redundant keys in " + sec.getLocale() + " (should be " + id + ") compared with default");
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String val2 = getMissingResourceAsEmpty(main.getBundle(), key);
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
    


    private void allLog(String string) {
        outLog(string);
        errLog(string);
    }
    private void errLog(String string) {
        //used quite often :)
        //System.out.println(string);
        ServerAccess.logErrorReprint(string);
    }

    private void outLog(String string) {
        ServerAccess.logOutputReprint(string);
    }
}
