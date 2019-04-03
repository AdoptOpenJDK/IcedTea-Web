/*
 * Copyright 2012 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sourceforge.jnlp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.replacements.BASE64Encoder;

import org.junit.AfterClass;
import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class PluginBridgeTest extends NoStdOutErrTest{
    private class MockJNLPCreator extends JNLPCreator {

        private URL JNLPHref;

        public URL getJNLPHref() {
            return JNLPHref;
        }

        @Override
        public JNLPFile create(URL location, Version version, ParserSettings settings,
                UpdatePolicy policy, URL forceCodebase) throws IOException, ParseException {
            JNLPHref = location;
            return new MockJNLPFile();
        }
    }

    private class MockJNLPFile extends JNLPFile {
        @Override
        public AppletDesc getApplet() {
            return new AppletDesc(null, null, null, 0, 0, new HashMap<String, String>());
        }

        @Override
        public ResourcesDesc getResources() {
            return new ResourcesDesc(null, null, null, null);
        }
    }

    static private PluginParameters createValidParamObject() {
        Map<String, String> params = new HashMap<>();
        params.put("code", ""); // Avoids an exception being thrown
        return new PluginParameters(params);
    }

    private static String originalCacheDir;

    @BeforeClass
    public static void setup() {
        originalCacheDir = PathsAndFiles.CACHE_DIR.getFullPath();
        PathsAndFiles.CACHE_DIR.setValue(System.getProperty("java.io.tmpdir") + File.separator + "tempcache");
    }

    @AfterClass
    public static void teardown() {
        CacheUtil.clearCache();
        PathsAndFiles.CACHE_DIR.setValue(originalCacheDir);
    }

    @Test
    public void testAbsoluteJNLPHref() throws MalformedURLException, Exception {
        URL codeBase = new URL("http://undesired.absolute.codebase.com");
        String absoluteLocation = "http://absolute.href.com/test.jnlp";
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", absoluteLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(absoluteLocation, mockCreator.getJNLPHref().toExternalForm());
    }

    @Test
    public void testRelativeJNLPHref() throws MalformedURLException, Exception {
        URL codeBase = new URL("http://desired.absolute.codebase.com/");
        String relativeLocation = "sub/dir/test.jnlp";
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(codeBase.toExternalForm() + relativeLocation,
                mockCreator.getJNLPHref().toExternalForm());
    }

    @Test
    public void testNoSubDirInCodeBase() throws MalformedURLException, Exception {
        String desiredDomain = "http://desired.absolute.codebase.com";
        URL codeBase = new URL(desiredDomain + "/undesired/sub/dir");
        String relativeLocation = "/app/test/test.jnlp";
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(desiredDomain + relativeLocation,
                mockCreator.getJNLPHref().toExternalForm());
    }
    
    @Test
    public void testGetRequestedPermissionLevel() throws MalformedURLException, Exception {
        String desiredDomain = "http://desired.absolute.codebase.com";
        URL codeBase = new URL(desiredDomain + "/undesired/sub/dir");
        String relativeLocation = "/app/test/test.jnlp";
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(pb.getRequestedPermissionLevel(), SecurityDesc.RequestedPermissionLevel.NONE);
        
        params.put(SecurityDesc.RequestedPermissionLevel.PERMISSIONS_NAME,SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString());
        pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(pb.getRequestedPermissionLevel(), SecurityDesc.RequestedPermissionLevel.ALL);
        
        //unknown for applets!
        params.put(SecurityDesc.RequestedPermissionLevel.PERMISSIONS_NAME, SecurityDesc.RequestedPermissionLevel.J2EE.toJnlpString());
        pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(pb.getRequestedPermissionLevel(), SecurityDesc.RequestedPermissionLevel.NONE);
        
        params.put(SecurityDesc.RequestedPermissionLevel.PERMISSIONS_NAME, SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString());
        pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(pb.getRequestedPermissionLevel(), SecurityDesc.RequestedPermissionLevel.SANDBOX);
        
        params.put(SecurityDesc.RequestedPermissionLevel.PERMISSIONS_NAME, SecurityDesc.RequestedPermissionLevel.DEFAULT.toHtmlString());
        pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(pb.getRequestedPermissionLevel(), SecurityDesc.RequestedPermissionLevel.NONE);
    }

    @Test
    public void testBase64StringDecoding() throws Exception {
        String actualFile = "This is a sample string that will be encoded to" +
                "a Base64 string and then decoded using PluginBridge's" +
                "decoding method and compared.";

        BASE64Encoder encoder = new BASE64Encoder();
        String encodedFile = encoder.encodeBuffer(actualFile.getBytes());

        byte[] decodedBytes = PluginBridge.decodeBase64String(encodedFile);
        String decodedString = new String(decodedBytes);
        Assert.assertEquals(actualFile, decodedString);
    }

    @Test
    public void testEmbeddedJnlpWithValidCodebase() throws Exception {
        URL codeBase = new URL("http://icedtea.classpath.org");
        String relativeLocation = "/EmbeddedJnlpFile.jnlp";

        //Codebase within jnlp file is VALID
        /**
        <?xml version="1.0"?>
            <jnlp spec="1.5+"
              href="EmbeddedJnlpFile.jnlp"
              codebase="http://www.redhat.com"
            >

            <information>
                <title>Sample Test</title>
                <vendor>RedHat</vendor>
                <offline-allowed/>
            </information>

            <resources>
                <j2se version='1.6+' />
                <jar href='EmbeddedJnlpJarOne.jar' main='true' />
                <jar href='EmbeddedJnlpJarTwo.jar' main='true' />
            </resources>

            <applet-desc
                documentBase="."
                name="redhat.embeddedjnlp"
                main-class="redhat.embeddedjnlp"
                width="0"
                height="0"
            />
           </jnlp>
         **/

        String jnlpFileEncoded = "ICAgICAgICA8P3htbCB2ZXJzaW9uPSIxLjAiPz4NCiAgICAgICAgICAgIDxqbmxwIHNwZWM9IjEu" +
                "NSsiIA0KICAgICAgICAgICAgICBocmVmPSJFbWJlZGRlZEpubHBGaWxlLmpubHAiIA0KICAgICAg" +
                "ICAgICAgICBjb2RlYmFzZT0iaHR0cDovL3d3dy5yZWRoYXQuY29tIiAgICANCiAgICAgICAgICAg" +
                "ID4NCg0KICAgICAgICAgICAgPGluZm9ybWF0aW9uPg0KICAgICAgICAgICAgICAgIDx0aXRsZT5T" +
                "YW1wbGUgVGVzdDwvdGl0bGU+DQogICAgICAgICAgICAgICAgPHZlbmRvcj5SZWRIYXQ8L3ZlbmRv" +
                "cj4NCiAgICAgICAgICAgICAgICA8b2ZmbGluZS1hbGxvd2VkLz4NCiAgICAgICAgICAgIDwvaW5m" +
                "b3JtYXRpb24+DQoNCiAgICAgICAgICAgIDxyZXNvdXJjZXM+DQogICAgICAgICAgICAgICAgPGoy" +
                "c2UgdmVyc2lvbj0nMS42KycgLz4NCiAgICAgICAgICAgICAgICA8amFyIGhyZWY9J0VtYmVkZGVk" +
                "Sm5scEphck9uZS5qYXInIG1haW49J3RydWUnIC8+DQogICAgICAgICAgICAgICAgPGphciBocmVm" +
                "PSdFbWJlZGRlZEpubHBKYXJUd28uamFyJyBtYWluPSd0cnVlJyAvPg0KICAgICAgICAgICAgPC9y" +
                "ZXNvdXJjZXM+DQoNCiAgICAgICAgICAgIDxhcHBsZXQtZGVzYw0KICAgICAgICAgICAgICAgIGRv" +
                "Y3VtZW50QmFzZT0iLiINCiAgICAgICAgICAgICAgICBuYW1lPSJyZWRoYXQuZW1iZWRkZWRqbmxw" +
                "Ig0KICAgICAgICAgICAgICAgIG1haW4tY2xhc3M9InJlZGhhdC5lbWJlZGRlZGpubHAiDQogICAg" +
                "ICAgICAgICAgICAgd2lkdGg9IjAiDQogICAgICAgICAgICAgICAgaGVpZ2h0PSIwIg0KICAgICAg" +
                "ICAgICAgLz4NCiAgICAgICAgICAgIDwvam5scD4=";

        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        params.put("jnlp_embedded", jnlpFileEncoded);

        String jnlpCodebase = "http://www.redhat.com";
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        JARDesc[] jars = pb.getResources().getJARs();

        //Check if there are two jars cached
        Assert.assertTrue(jars.length == 2);

        //Resource can be in any order
        List<String> resourceLocations = new ArrayList<>();
        resourceLocations.add(jars[0].getLocation().toExternalForm());
        resourceLocations.add(jars[1].getLocation().toExternalForm());

        //Check URLs of jars
        Assert.assertTrue(resourceLocations.contains(jnlpCodebase + "/EmbeddedJnlpJarOne.jar"));
        Assert.assertTrue((resourceLocations.contains(jnlpCodebase + "/EmbeddedJnlpJarTwo.jar")));
    }

    @Test
    //http://docs.oracle.com/javase/6/docs/technotes/guides/jweb/applet/codebase_determination.html
    //example 3
    public void testEmbeddedJnlpWithInvalidCodebase() throws Exception {
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");
        String relativeLocation = "/EmbeddedJnlpFile.jnlp";

        //Codebase within jnlp file is INVALID
        /**
        <?xml version="1.0"?>
            <jnlp spec="1.5+"
              href="EmbeddedJnlpFile.jnlp"
              codebase="invalidPath"
            >

            <information>
                <title>Sample Test</title>
                <vendor>RedHat</vendor>
                <offline-allowed/>
            </information>

            <resources>
                <j2se version='1.6+' />
                <jar href='EmbeddedJnlpJarOne.jar' main='true' />
                <jar href='EmbeddedJnlpJarTwo.jar' main='true' />
            </resources>

            <applet-desc
                documentBase="."
                name="redhat.embeddedjnlp"
                main-class="redhat.embeddedjnlp"
                width="0"
                height="0"
            />
            </jnlp>
         **/

        String jnlpFileEncoded = "ICAgICAgICA8P3htbCB2ZXJzaW9uPSIxLjAiPz4NCiAgICAgICAgICAgIDxqbmxwIHNwZWM9IjEu" +
                "NSsiIA0KICAgICAgICAgICAgICBocmVmPSJFbWJlZGRlZEpubHBGaWxlLmpubHAiIA0KICAgICAg" +
                "ICAgICAgICBjb2RlYmFzZT0iaW52YWxpZFBhdGgiICAgIA0KICAgICAgICAgICAgPg0KDQogICAg" +
                "ICAgICAgICA8aW5mb3JtYXRpb24+DQogICAgICAgICAgICAgICAgPHRpdGxlPlNhbXBsZSBUZXN0" +
                "PC90aXRsZT4NCiAgICAgICAgICAgICAgICA8dmVuZG9yPlJlZEhhdDwvdmVuZG9yPg0KICAgICAg" +
                "ICAgICAgICAgIDxvZmZsaW5lLWFsbG93ZWQvPg0KICAgICAgICAgICAgPC9pbmZvcm1hdGlvbj4N" +
                "Cg0KICAgICAgICAgICAgPHJlc291cmNlcz4NCiAgICAgICAgICAgICAgICA8ajJzZSB2ZXJzaW9u" +
                "PScxLjYrJyAvPg0KICAgICAgICAgICAgICAgIDxqYXIgaHJlZj0nRW1iZWRkZWRKbmxwSmFyT25l" +
                "LmphcicgbWFpbj0ndHJ1ZScgLz4NCiAgICAgICAgICAgICAgICA8amFyIGhyZWY9J0VtYmVkZGVk" +
                "Sm5scEphclR3by5qYXInIG1haW49J3RydWUnIC8+DQogICAgICAgICAgICA8L3Jlc291cmNlcz4N" +
                "Cg0KICAgICAgICAgICAgPGFwcGxldC1kZXNjDQogICAgICAgICAgICAgICAgZG9jdW1lbnRCYXNl" +
                "PSIuIg0KICAgICAgICAgICAgICAgIG5hbWU9InJlZGhhdC5lbWJlZGRlZGpubHAiDQogICAgICAg" +
                "ICAgICAgICAgbWFpbi1jbGFzcz0icmVkaGF0LmVtYmVkZGVkam5scCINCiAgICAgICAgICAgICAg" +
                "ICB3aWR0aD0iMCINCiAgICAgICAgICAgICAgICBoZWlnaHQ9IjAiDQogICAgICAgICAgICAvPg0K" +
                "ICAgICAgICAgICAgPC9qbmxwPg==";

        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        params.put("jnlp_embedded", jnlpFileEncoded);

        PluginBridge pb = new PluginBridge(overwrittenCodebase, null, "", "", 0, 0, params, mockCreator);
        JARDesc[] jars = pb.getResources().getJARs();

        //Check if there are two jars cached
        Assert.assertTrue(jars.length == 2);

        //Resource can be in any order
        List<String> resourceLocations = new ArrayList<>();
        resourceLocations.add(jars[0].getLocation().toExternalForm());
        resourceLocations.add(jars[1].getLocation().toExternalForm());

        //Check URLs of jars
        Assert.assertTrue(resourceLocations.contains(overwrittenCodebase + "/EmbeddedJnlpJarOne.jar"));
        Assert.assertTrue((resourceLocations.contains(overwrittenCodebase + "/EmbeddedJnlpJarTwo.jar")));
    }

    @Test
    public void testInvalidEmbeddedJnlp() throws Exception {
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");
        String relativeLocation = "/EmbeddedJnlpFile.jnlp";

        //Embedded jnlp is invalid
        String jnlpFileEncoded = "thisContextIsInvalid";

        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        params.put("jnlp_embedded", jnlpFileEncoded);

        try {
            new PluginBridge(overwrittenCodebase, null, "", "", 0, 0, params, mockCreator);
        } catch (Exception e) {
            return;
        }
        Assert.fail("PluginBridge was successfully created with an invalid embedded jnlp value");
    }
    
     @Test
    public void stripClassNoClass() throws Exception {
         Assert.assertEquals("blah.class.someclass", PluginBridge.strippClass("blah.class.someclass"));
    }
    
    @Test
    public void stripClassClass() throws Exception {
         Assert.assertEquals("blah.class.someclass", PluginBridge.strippClass("blah.class.someclass.class"));
    }
    
    
    private static final String CV="cbVal";
    
    private static String fixCommonIssues(String input, boolean needsSecurity) {
        return PluginBridge.fixCommonIsuses(needsSecurity, input, CV, "titTets", "ventest");
    }

    ;
    
    public static int countOccurences(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    private void checkIssuesFixed(String input, boolean security, boolean defaultCB) {
        assertEquals(1, countOccurences(input, "<jnlp"));
        if (defaultCB) {
            assertEquals(1, countOccurences(input, CV));
        } else {
            assertEquals(0, countOccurences(input, CV));
        }
        assertEquals(1, countOccurences(input, "codebase"));
        if (security) {
            assertEquals(2, countOccurences(input, "security"));
            assertEquals(1, countOccurences(input, "<security"));
            assertEquals(1, countOccurences(input, "all-permissions"));
        } else {
            assertEquals(0, countOccurences(input, "all-permissions"));
        }
        assertEquals(1, countOccurences(input, "<title>"));
        assertEquals(1, countOccurences(input, "<vendor>"));
    }
    ;
            
    @Test
    public void testElementMatch() throws Exception {
        Assert.assertTrue("<sandbox>".matches(PluginBridge.toMatcher(PluginBridge.SANDBOX_REGEX)));
        Assert.assertTrue("  <  sandbox  >  ".matches(PluginBridge.toMatcher(PluginBridge.SANDBOX_REGEX)));
        Assert.assertTrue("  <  sandbox  >  \n".matches(PluginBridge.toMatcher(PluginBridge.SANDBOX_REGEX)));
        Assert.assertTrue("\n  <  SANDBOX  >  \n".matches(PluginBridge.toMatcher(PluginBridge.SANDBOX_REGEX)));
        Assert.assertEquals("  XX  ", "  <  sAnDbOx  >  ".replaceAll(PluginBridge.SANDBOX_REGEX,"XX"));
        Assert.assertEquals(" A \n XX \n B ", " A \n <sandbox> \n B ".replaceAll(PluginBridge.SANDBOX_REGEX,"XX"));
        
        
    }
     @Test
     public void testClosingElementMatch() throws Exception {
        Assert.assertTrue("</information>".matches(PluginBridge.toMatcher(PluginBridge.CLOSE_INFORMATION_REGEX)));
        Assert.assertTrue("  <  /information  >  ".matches(PluginBridge.toMatcher(PluginBridge.CLOSE_INFORMATION_REGEX)));
        Assert.assertTrue("  < / information  >  \n".matches(PluginBridge.toMatcher(PluginBridge.CLOSE_INFORMATION_REGEX)));
        Assert.assertTrue("\n  </  INFORMATION  >  \n".matches(PluginBridge.toMatcher(PluginBridge.CLOSE_INFORMATION_REGEX)));
        Assert.assertEquals("  XX  ", "  </ InFoRmatIon  >  ".replaceAll(PluginBridge.CLOSE_INFORMATION_REGEX,"XX"));
        Assert.assertEquals(" A \n XX \n B ", " A \n </information> \n B ".replaceAll(PluginBridge.CLOSE_INFORMATION_REGEX,"XX"));
    }
     
    @Test
    public void testCodeBaseMatches() throws Exception {
        Assert.assertFalse("zzz codebase zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));
        Assert.assertTrue("zzz codebase='someVal' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));
        Assert.assertTrue("zzz codebase='' \nzzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));
        Assert.assertTrue("zzz codebase=\"\" zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));
        Assert.assertTrue("zzz \ncodebase='.' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));
        Assert.assertTrue("zzz codebase=\".\" zzz\n".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX1)));

        Assert.assertTrue("zzz codebase='' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertTrue("zzz codebase=\"\" zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertTrue("zzz codebase='.' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertTrue("zzz codebase=\".\" zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertTrue("zzz codebase=\".\">".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertFalse("zzz codebase=\".\"X".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));

        Assert.assertFalse("zzz codebase='x' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertFalse("zzz codebase=\"..\" zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        
        Assert.assertFalse("zzz codebase zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
        Assert.assertFalse("zzz codebase='someVal' zzz".matches(PluginBridge.toMatcher(PluginBridge.CODEBASE_REGEX2)));
    }
    
    private static final String jnlpTempalte = "<jnlp @CB@ > "
            + " @INFO@ \n"
            + " @SEC@ \n "
            + "<resources>\n"
            + "<jar href='blah.jar' main='true'/>     \n "
            + "<jar href='blah2.jar'/>      \n"
            + "<extension name='blah_blah' href='blah.jnlp' />\n"
            + "</resources>\n"
            + "<applet-desc       name='notNecessary'\n"
            + "main-class='nope'      width'200'       height='200'>  \n"
            + "</applet-desc>\n"
            + " </jnlp>";
    
    private static String prepareTemplate(String codebase, String information, String security ){
        return jnlpTempalte.replace("@CB@", codebase).replace("@INFO@", information).replace("@SEC@", security);
    }
    

    @Test
    public void fixCommonIssuesNothingUnsigned() throws Exception {
        String source = prepareTemplate("","", "");
        String fixed = fixCommonIssues(source, false);
        checkIssuesFixed(fixed, false, true);
    }
    
    @Test
    public void fixCommonNothingSigned() throws Exception {
        String source = prepareTemplate("","", "");
        String fixed = fixCommonIssues(source, true);
        checkIssuesFixed(fixed, true, true);
    }
    
    @Test
    public void fixCommonIssuesEmptyInformationUnsigned() throws Exception {
        String source = prepareTemplate("","<information></information>", "");
        String fixed = fixCommonIssues(source, false);
        checkIssuesFixed(fixed, false, true);
    }
    
    @Test
    public void fixCommonIssuesEmptyInformationSigned() throws Exception {
        String source = prepareTemplate("","<information></information>", "");
        String fixed = fixCommonIssues(source, true);
        checkIssuesFixed(fixed, true, true);
    }
    @Test
    public void fixCommonIssuesInformationUnsigned() throws Exception {
        String source = prepareTemplate("","<information><vendor>blah</vendor><title>argh</title></information>", "");
        String fixed = fixCommonIssues(source, false);
        checkIssuesFixed(fixed, false, true);
    }
    
    @Test
    public void fixCommonIssuesInformationSigned() throws Exception {
        String source = prepareTemplate("","<information><vendor>blah</vendor><title>argh</title></information>", "");
        String fixed = fixCommonIssues(source, true);
        checkIssuesFixed(fixed, true, true);
    }
    
     @Test
    public void fixCommonIssuesInformationCodebaseDotUnsigned() throws Exception {
        String source = prepareTemplate("codebase='.'", "<information><vendor>blah</vendor><title>argh</title></information>", "");
        String fixed = fixCommonIssues(source, false);
        checkIssuesFixed(fixed, false, true);
    }
    
     @Test
    public void fixCommonIssuesInformationCodebaseDotUnsigned2() throws Exception {
        String source = prepareTemplate("codebase=\"\"", "<information><vendor>blah</vendor><title>argh</title></information>", "");
        String fixed = fixCommonIssues(source, false);
        checkIssuesFixed(fixed, false, true);
    }
    
    @Test
    public void fixCommonIssuesInformationCodebaseSigned() throws Exception {
        String source = prepareTemplate("codebase='customOne'","<information><vendor>blah</vendor><title>argh</title></information>", "");
        String fixed = fixCommonIssues(source, true);
        checkIssuesFixed(fixed, true, false);
    }

}
