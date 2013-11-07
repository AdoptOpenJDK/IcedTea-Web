/* LocalisedInformationElementTest.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class LocalesTestTest {

    private static ServerAccess server = new ServerAccess();
    String[] keys = {
        "BOUsage",
        "BOUsage2",
        "BOArg",
        "BOParam",
        "BOProperty",
        "BOLicense",
        "BOVerbose",
        "BOAbout",
        "BONosecurity",
        "BONoupdate",
        "BOHeadless",
        "BOStrict",
        "BOViewer",
        "BXnofork",
        "BXclearcache",
        "BOHelp"};

    /**
     * this will prepare new set of variables with wanted locale, which can be
     * then passed to subprocess
     *
     * @param locale - locale to be set to LANG variable, eg cs_CZ.UTF-8
     */
    public static String[] getChangeLocalesForSubproces(String locale) {
        ServerAccess.logOutputReprint("Setting locales");
        Map<String, String> p = System.getenv();
        Set<Entry<String, String>> r = p.entrySet();
        List<Entry<String, String>> rr = new ArrayList<Entry<String, String>>(r);
        Collections.sort(rr, new Comparator<Entry<String, String>>() {

            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        String[] l = new String[rr.size()];
        int i = 0;
        for (Iterator<Entry<String, String>> it = rr.iterator(); it.hasNext(); i++) {
            Entry<String, String> entry = it.next();
            String v = entry.getValue();
            String s = entry.getKey() + "=" + v;
            //System.out.println(s);
            if (entry.getKey().equals("LANG")) {
                ServerAccess.logOutputReprint("was " + v);
                v = locale;
                ServerAccess.logOutputReprint("set " + v);
            }
            s = entry.getKey() + "=" + v;
            l[i] = s;
        }
        return l;
    }

     private ResourceBundle getPropertiesDe() throws IOException {
        return getProperties("_de");
    }
     
    
    public ResourceBundle getPropertiesCz() throws IOException {
        return getProperties("_cs");

    }

     public ResourceBundle getPropertiesPl() throws IOException {
        return getProperties("_pl");

    }

    public ResourceBundle getPropertiesEn() throws IOException {
        return getProperties("");

    }

    public ResourceBundle getProperties(String s) throws IOException {
        return new PropertyResourceBundle(this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jnlp/resources/Messages" + s + ".properties"));

    }
    //just launching javaws -about to see if messages are corectly localised
    List<String> javaws = Arrays.asList(new String[]{server.getJavawsLocation(), "-help", ServerAccess.HEADLES_OPTION});

    @Test
    public void testLocalesEnUsUtf() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_US.UTF-8");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotDe(pr.stdout);
        assertNotPl(pr.stdout);
    }

    @Test
    public void testLocalesCsCz() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotDe(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxCzCs(pr.stdout);
    }

    @Test
    public void testLocalesCsCzUtf() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ.UTF-8");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotDe(pr.stdout);
        assertCz(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxCzCs(pr.stdout);
    }

     @Test
    public void testLocalesPlPL() throws Exception {
        String[] l = getChangeLocalesForSubproces("pl_PL");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotDe(pr.stdout);
        iteratePropertiesForAproxPl(pr.stdout);
    }

    @Test
    public void testLocalesPlPLUtf() throws Exception {
        String[] l = getChangeLocalesForSubproces("pl_PL.UTF-8");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotDe(pr.stdout);
        assertNotCz(pr.stdout);
        assertPl(pr.stdout);
        iteratePropertiesForAproxPl(pr.stdout);
    }
    
     @Test
    public void testLocalesDeDe() throws Exception {
        String[] l = getChangeLocalesForSubproces("de_DE");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxDe(pr.stdout);
    }

    @Test
    public void testLocalesDeDeUtf() throws Exception {
        String[] l = getChangeLocalesForSubproces("de_DE.UTF-8");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertDe(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxDe(pr.stdout);
    }
   
    
       @Test
    public void testLocalesDe_unknowButValidDeLocale() throws Exception {
        String[] l = getChangeLocalesForSubproces("de_LU");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxDe(pr.stdout);
    }

    @Test
    public void testLocalesDeUtf_unknowButValidDeLocale() throws Exception {
        String[] l = getChangeLocalesForSubproces("de_LU.UTF-8");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertDe(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxDe(pr.stdout);
    }
    
    
    
       @Test
    public void testLocalesDe_globalDe() throws Exception {
        String[] l = getChangeLocalesForSubproces("deutsch");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertNotEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotPl(pr.stdout);
        iteratePropertiesForAproxDe(pr.stdout);
    }

   
    
    
    @Test
    public void testLocalesInvalid() throws Exception {
        String[] l = getChangeLocalesForSubproces("ax_BU");
        ProcessResult pr = ServerAccess.executeProcess(javaws, null, null, l);
        assertEnglish(pr.stdout);
        assertNotCz(pr.stdout);
        assertNotDe(pr.stdout);
        assertNotPl(pr.stdout);
    }

    private void assertEnglish(String s) throws IOException {
        ResourceBundle props = getPropertiesEn();
        iteratePropertiesFor(props, s, true, "english");
    }

    private void assertNotEnglish(String s) throws IOException {
        ResourceBundle props = getPropertiesEn();
        iteratePropertiesFor(props, s, false, "english");
    }

    private void assertCz(String s) throws IOException {
        ResourceBundle props = getPropertiesCz();
        iteratePropertiesFor(props, s, true, "czech");
    }

    private void assertPl(String s) throws IOException {
        ResourceBundle props = getPropertiesPl();
        iteratePropertiesFor(props, s, true, "polish");
    }
    
     private void assertDe(String s) throws IOException {
        ResourceBundle props = getPropertiesDe();
        iteratePropertiesFor(props, s, true, "de");
    }
     

    private void assertNotCz(String s) throws IOException {
        ResourceBundle props = getPropertiesCz();
        iteratePropertiesFor(props, s, false, "czech");
    }

      private void assertNotPl(String s) throws IOException {
        ResourceBundle props = getPropertiesPl();
        iteratePropertiesFor(props, s, false, "polish");
    }
    
     private void assertNotDe(String s) throws IOException {
        ResourceBundle props = getPropertiesDe();
        iteratePropertiesFor(props, s, false, "de");
    }


    /**
     * This method is iterating all keys defined in this class, geting their value in given
     * properties, and then checking if given output have/have not (depends on value of assertTrue)
     * this string contained.
     *
     * @param props
     * @param outputToExamine
     * @param assertTrue
     * @param languageId
     */
    private void iteratePropertiesFor(ResourceBundle props, String outputToExamine, boolean assertTrue, String languageId) {
        int keysFound = 0;
        for (int i = 0; i < keys.length; i++) {
            String string = keys[i];
            String value = props.getString(string);
            if (value == null) {
                continue;
            }
            keysFound++;
            if (assertTrue) {
                Assert.assertTrue("Output must contains " + languageId + " text, failed on " + string, outputToExamine.contains(value));
            } else {
                Assert.assertFalse("Output must NOT contains " + languageId + " text, failed on " + string, outputToExamine.contains(value));

            }
        }
        Assert.assertTrue("At least one key must be found, was not", keysFound > 0);
    }

    /**
     * This method is iterating all keys defined in this class, geting their value in given
     * properties, transforming this to asci-ionly regex and then checking if
     * given output match/matchnot (depends on value of assertTrue) this string,
     *
     * @param outputToBeChecked
     * @param props  bundle with strings
     * @param  reg  regexter with rules how to handle national characters
     * @throws IOException
     */
    private void iteratePropertiesForAprox(String outputToBeChecked, ResourceBundle props, Regexer reg) throws IOException {
        int keysFound = 0;
        for (int i = 0; i < keys.length; i++) {
            String string = keys[i];
            String value = props.getString(string);
            if (value == null) {
                continue;
            }
            value = reg.regexIt(value);
            keysFound++;
            {
                Assert.assertTrue("Output must match "+reg.getId() +" text, failed on " + string, outputToBeChecked.matches(value));
            }
        }
        Assert.assertTrue("At least one key must be found, was not", keysFound > 0);
    }

    private void iteratePropertiesForAproxCzCs(String stdout) throws IOException {
        iteratePropertiesForAprox(stdout, getPropertiesCz(), Regexer.cz);
    }
    private void iteratePropertiesForAproxDe(String stdout) throws IOException {
        iteratePropertiesForAprox(stdout, getPropertiesDe(), Regexer.de);
    }

     private void iteratePropertiesForAproxPl(String stdout) throws IOException {
        iteratePropertiesForAprox(stdout, getPropertiesPl(), Regexer.pl);
    }
    
       
    
    private static final class Regexer {

        private static final String[] czEvil = {
            "á",
            "č",
            "ď",
            "ě",
            "é",
            "í",
            "ň",
            "ó",
            "ř",
            "š",
            "ť",
            "ú",
            "ů",
            "ý",
            "ž",
            "[",
            "]",
            "(",
            ")"};
        private static final String[] deEvil = {
            "ä",
            "ö",
            "ß",
            "ü",
            "[",
            "]",
            "(",
            ")"};

           private static final String[] plEvil = {
            "ó",
            "ą",
            "ę",
            "ó",
            "ł",
            "ć",
            "ś",
            "ź",
            "ż",
            "ń",
            "[",
            "]",
            "(",
            ")"};
        
        private static final Regexer cz = new Regexer(czEvil,"cz");        
        private static final Regexer de = new Regexer(deEvil,"de");
        private static final Regexer pl = new Regexer(plEvil,"pl");
        
        private final String[] map;
        private final String id;

        public Regexer(String[] map, String id) {
            this.map = map;
            this.id = id;
        }

        public String getId() {
            return id;
        }
        
        
        

        /**
         * This method transforms given string to asci-only regex, replacing
         * groups of national characters (defined by array variable) by .+
         *
         * @param value
         * @return
         */
        public String regexIt(String value) {
            for (int i = 0; i < map.length; i++) {
                String string = map[i];
                value = value.replace(string, ".");
                value = value.replace(string.toUpperCase(), ".");
            }

            value = value.replaceAll("\\.+", ".+");
            value = "(?s).*" + value + ".*";
            return value;
        }
    }
}
