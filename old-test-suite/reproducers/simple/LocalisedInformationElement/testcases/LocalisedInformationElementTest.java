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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import org.junit.Assert;
import org.junit.Test;

public class LocalisedInformationElementTest {

    private static ServerAccess server = new ServerAccess();

    /**
     * this will prepare new set of varibales with wanted locale, which
     * can be then passed to subprocess
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
        int i = -1;
        for (Iterator<Entry<String, String>> it = rr.iterator(); it.hasNext();) {
            i++;
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

    public static ProcessResult evaluateLocalisedInformationElementTest(String id, String[] variables, boolean verbose) throws Exception {
        ProcessResult pr = executeJavaws(verbose, variables, id);
        String s = "LocalisedInformationElement launched";
        Assert.assertTrue(id + " stdout should contains " + s + " bud didn't", pr.stdout.contains(s));
        String locMatch = "(?s).*default locale: \\w{2}.*";
        Assert.assertTrue(id + " stdout should match " + locMatch + " bud didn't", pr.stdout.matches(locMatch));
        return pr;
    }

    public static ProcessResult evaluateLocalisedInformationElementTestNotLaunched(String id, String[] variables, boolean verbose) throws Exception {
        ProcessResult pr = executeJavaws(verbose, variables, id);
        String s = "LocalisedInformationElement launched";
        Assert.assertFalse(id + " stdout should not contains " + s + " bud didn't", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertTrue(id + " stderr should contains " + ss + " but didn't", pr.stderr.contains(ss));
        String locMatch = "(?s).*default locale: \\w{2}.*";
        Assert.assertFalse(id + " stdout should not match " + locMatch + " bud didn't", pr.stdout.matches(locMatch));
        String sss = "MissingVendorException";
        Assert.assertTrue(id + " stderr should contains " + sss + " but didn't", pr.stderr.contains(sss));
        return pr;
    }

    private static ProcessResult executeJavaws(boolean verbose, String[] variables, String id) throws Exception {
        List<String> oa = new ArrayList<String>(1);
        if (verbose) {
            oa.add("-verbose");
        }
        final ProcessResult pr;
        if (variables == null) {
            pr = server.executeJavawsHeadless(oa, "/" + id + ".jnlp");
        } else {
            pr = server.executeJavawsHeadless(oa, "/" + id + ".jnlp", variables);
        }
        return pr;
    }


    //the description checkis disabled for all PR955, so it is representing just
    //PR955 issue. Tests with enable description check are introduced later
    private final boolean w1=false;

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation1() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_US.UTF-8");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l, true);
        assertTiVeDe(pr, "localisedJnlp1.jnlp1", "IcedTea", "LocalisedInformationElement1.jnlp", w1);
    }

//LANG variable do not 'accept' nationales without regions :(
//    @Test
//    @Bug(id = "PR955")
//    public void testLocalisedInformationElementLaunchWithLocalisedInformation2() throws Exception {
//        String[] l = getChangeLocalesForSubproces("cs");
//        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l,true);
//        assertTiVeDe(pr,"LocalisedInformationElement1.jnlp po cesky","IcedTea CZ","Muj vlastni LocalisedInformationElement1.jnlp",w1);
//    }
    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation22() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp po cesky", "IcedTea CZ", "Muj vlastni LocalisedInformationElement1.jnlp", w1);
    }

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_1() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_BE");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee BE", "IcedTea", "La LocalisedInformationElement1.jnlp", w1);
    }

//   java is ignoring set encoding :(
//    @Test
//    @Bug(id = "PR955")
//    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_2() throws Exception {
//        String[] l = getChangeLocalesForSubproces("fr_BE.iso88591");
//        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l, true);
//        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee BE iso88591", "IcedTea", "La LocalisedInformationElement1.jnlp",false);
//    }
    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_CH");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement1", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee", "IcedTea", "La LocalisedInformationElement1.jnlp", w1);
    }

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation1_withPieceMissing() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_US.UTF-8");
        ProcessResult pr = evaluateLocalisedInformationElementTestNotLaunched("LocalisedInformationElement2", l, true);

    }

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation22_withPieceMissing() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ");
        ProcessResult pr = evaluateLocalisedInformationElementTestNotLaunched("LocalisedInformationElement2", l, true);
    }

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_1_withPieceMissing() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_BE");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement2", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee BE", "IcedTea", "La LocalisedInformationElement1.jnlp", w1);
    }

    @Test
    @Bug(id = "PR955")
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_withPieceMissing() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_CH");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement2", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee", "IcedTea", "La LocalisedInformationElement1.jnlp", w1);
    }

    //thsoe 11 methods are jsut for printing of locales passed to javaws
    //so actually testing the LOCALE hack
    @Test
    public void printLocales() throws Exception {
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", null, false);
    }

    @Test
    public void printLocalesChanged1() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ.UTF-8");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertTrue(pr.stdout.contains("cs_CZ"));
    }
// the following four have acepted iso encoding, but not used it

    @Test
    public void printLocalesChanged2() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.ISO-8859-1");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertTrue(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged22() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.ISO88591");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertTrue(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged2222() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.iso-8859-1");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertTrue(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged3() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.UTF-8");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertTrue(pr.stdout.contains("en_AU"));
    }

    // the following five have NOTacepted iso encoding at all
    @Test
    public void printLocalesChanged2_X() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.ISO-8859-2");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertFalse(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged22_X() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.ISO88592");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertFalse(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged2222_X() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.iso-8859-2");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertFalse(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged3_X() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.UTF-16");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertFalse(pr.stdout.contains("en_AU"));
    }

    @Test
    public void printLocalesChanged4_X() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_AU.jklukl56489jkyk");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement_noLoc", l, false);
        Assert.assertFalse(pr.stdout.contains("en_AU"));
    }
    private static final String DEFAULT_HOMEPAGE = "http://icedtea.classpath.org/wiki/IcedTea-Web#Testing_IcedTea-Web";

    public static void assertTiVeDe(ProcessResult pr, String title, String vendor, String description, boolean descTests) {
        assertTiHpVeDe(pr, title, DEFAULT_HOMEPAGE, vendor, description, descTests);
    }

    public static void assertTiHpVeDe(ProcessResult pr, String title, String homepage, String vendor, String description, boolean descTests) {
        Assert.assertTrue("call shuld evaluate homepage as: " + homepage + " but did not", pr.stdout.contains("Homepage: " + homepage));
        Assert.assertTrue("call shuld evaluate title as: " + title + " but did not", pr.stdout.contains("Acceptable title tag found, contains: " + title));
        Assert.assertTrue("call shuld evaluate vendor as: " + " but did not", pr.stdout.contains("Acceptable vendor tag found, contains: " + vendor));
        if (descTests) {
            Assert.assertTrue("call shuld evaluate description as: " + description + " but did not", pr.stdout.contains("Description: " + description));
        }
    }


    //following tests are testing also localisation of description
    private final boolean w2=true;

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation1_withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("en_US.UTF-8");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement3", l, true);
        assertTiVeDe(pr, "localisedJnlp1.jnlp1", "IcedTea", "D_DEF", w2);
    }

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation22_withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("cs_CZ");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement3", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp po cesky", "IcedTea CZ", "D_DEF_CS", w2);
    }

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_1_withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_BE");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement3", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee BE", "IcedTea", "D_FR_BE", w2);
    }

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33__withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_CH");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement3", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee", "IcedTea", "D_DEF_FR", w2);
    }

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_1_withPieceMissing_withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_BE");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement4", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee BE", "IcedTea", "D_DEF_FR", w2);
    }

    @Test
    public void testLocalisedInformationElementLaunchWithLocalisedInformation33_withPieceMissing_withDescription() throws Exception {
        String[] l = getChangeLocalesForSubproces("fr_CH");
        ProcessResult pr = evaluateLocalisedInformationElementTest("LocalisedInformationElement4", l, true);
        assertTiVeDe(pr, "LocalisedInformationElement1.jnlp la francee", "IcedTea", "D_DEF_FR", w2);
    }

    //following tests are testing localisation of homepage
    //to lazy to do...
}
