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

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.replacements.BASE64Encoder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PluginBridgeTest extends NoStdOutErrTest{

    private class MockJnlpFileFactory extends JNLPFileFactory {

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
        MockJnlpFileFactory mockCreator = new MockJnlpFileFactory();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, params, mockCreator);
        assertEquals(absoluteLocation, mockCreator.getJNLPHref().toExternalForm());
    }

    @Test
    public void testRelativeJNLPHref() throws MalformedURLException, Exception {
        URL codeBase = new URL("http://desired.absolute.codebase.com/");
        String relativeLocation = "sub/dir/test.jnlp";
        PluginParameters params = createValidParamObject();
        params.put("jnlp_href", relativeLocation);
        MockJnlpFileFactory mockCreator = new MockJnlpFileFactory();
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
        MockJnlpFileFactory mockCreator = new MockJnlpFileFactory();
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
        MockJnlpFileFactory mockCreator = new MockJnlpFileFactory();
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
    public void testInvalidEmbeddedJnlp() throws Exception {
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");
        String relativeLocation = "/EmbeddedJnlpFile.jnlp";

        //Embedded jnlp is invalid
        String jnlpFileEncoded = "thisContextIsInvalid";

        MockJnlpFileFactory mockCreator = new MockJnlpFileFactory();
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
