/*
 Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import net.sourceforge.jnlp.util.ClasspathMatcher.ClasspathMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ClasspathMatcherTest {


    @Test
    public void splitOnFirstTest() {
        String[] r;
        r = ClasspathMatcher.splitOnFirst("aa:bb:cc", ":");
        Assert.assertEquals("aa", r[0]);
        Assert.assertEquals("bb:cc", r[1]);

        r = ClasspathMatcher.splitOnFirst("bb:cc", "b");
        Assert.assertEquals("", r[0]);
        Assert.assertEquals("b:cc", r[1]);

        r = ClasspathMatcher.splitOnFirst("bb:cc", "c");
        Assert.assertEquals("bb:", r[0]);
        Assert.assertEquals("c", r[1]);

        r = ClasspathMatcher.splitOnFirst("cc:d", "d");
        Assert.assertEquals("cc:", r[0]);
        Assert.assertEquals("", r[1]);


    }

    @Test
    public void haveProtocolTest() {
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some.correct.url:5050/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some.correct.url:5050"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some.correct.url/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some.url/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some:5050/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://some"));
        Assert.assertTrue(ClasspathMatcher.hasProtocol("http://aa.cz"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("some.correct.url:5050/full/path"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("some.correct.url/full/path"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("some.correct.url"));
        //traps
        Assert.assertFalse(ClasspathMatcher.hasProtocol("httpsome.correct.url:5050://full/path"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("httpsome.corr://ect.url:5050/full/path"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("httpsome.corr://ect.url"));
        Assert.assertFalse(ClasspathMatcher.hasProtocol("httpsome/ful://l/path"));
    }

    @Test
    public void extractProtocolTest() {
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some.correct.url:5050/full/path"));
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some.correct.url:5050"));
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some.correct.url/full/path"));
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some.url/full/path"));
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some:5050/full/path"));
        Assert.assertEquals("http", ClasspathMatcher.extractProtocol("http://some"));
        //no :// at all
        Exception ex = null;
        try {
            ClasspathMatcher.extractProtocol("some.correct.url:5050/full/path");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            ClasspathMatcher.extractProtocol("some.correct.url/full/path");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            ClasspathMatcher.extractProtocol("some.correct.url");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        //wrongly palced :// - is catched by hasProtocol
        Assert.assertFalse("http".equals(ClasspathMatcher.extractProtocol("httpsome.correct.url:5050://full/path")));
        Assert.assertFalse("http".equals(ClasspathMatcher.extractProtocol("httpsome.corr://ect.url:5050/full/path")));
        Assert.assertFalse("http".equals(ClasspathMatcher.extractProtocol("httpsome.corr://ect.url")));
        Assert.assertFalse("http".equals(ClasspathMatcher.extractProtocol("httpsome/ful://l/path")));
    }

    @Test
    public void removeProtocolTest() {
        Assert.assertEquals("some.correct.url:5050/full/path", ClasspathMatcher.removeProtocol("http://some.correct.url:5050/full/path"));
        Assert.assertEquals("some.correct.url/full/path", ClasspathMatcher.removeProtocol("http://some.correct.url/full/path"));
        Assert.assertEquals("some.url/full/path", ClasspathMatcher.removeProtocol("http://some.url/full/path"));
        Assert.assertEquals("some:5050/full/path", ClasspathMatcher.removeProtocol("http://some:5050/full/path"));
        Assert.assertEquals("some", ClasspathMatcher.removeProtocol("http://some"));
        //no :// at all
        Exception ex = null;
        try {
            ClasspathMatcher.removeProtocol("some.correct.url:5050/full/path");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            ClasspathMatcher.removeProtocol("some.correct.url/full/path");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            ClasspathMatcher.removeProtocol("some.correct.url");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        //wrongly palced :// - is catched by hasProtocol
        Assert.assertFalse("some.correct.url:5050://full/path".equals(ClasspathMatcher.removeProtocol("httpsome.correct.url:5050://full/path")));
        Assert.assertFalse("some.corr://ect.url:5050/full/path".equals(ClasspathMatcher.removeProtocol("httpsome.corr://ect.url:5050/full/path")));
        Assert.assertFalse("some.corr://ect.url".equals(ClasspathMatcher.removeProtocol("httpsome.corr://ect.url")));
        Assert.assertFalse("some/ful://l/path".equals(ClasspathMatcher.removeProtocol("httpsome/ful://l/path")));
    }

    @Test
    public void havePathTest() {
        Assert.assertTrue(ClasspathMatcher.hasPath("some.correct.url:5050/full/path"));
        Assert.assertFalse(ClasspathMatcher.hasPath("some.correct.url:5050"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some.correct.url/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some.url/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some:5050/full/path"));
        Assert.assertFalse(ClasspathMatcher.hasPath("some"));
        //incorrect, but hard to solve
        Assert.assertTrue(ClasspathMatcher.hasPath("some.correct.url:5050://full/path"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some.corr://ect.url:5050/full/path"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some.corr://ect.url"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some/ful://l/path"));
        //traps
        Assert.assertTrue(ClasspathMatcher.hasPath("some.url/full/path/"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some:5050/full/path/"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some.url/"));
        Assert.assertTrue(ClasspathMatcher.hasPath("some:5050/"));
        Assert.assertFalse(ClasspathMatcher.hasPath("some.url"));
        Assert.assertFalse(ClasspathMatcher.hasPath("some:5050"));
    }

    @Test
    public void extractPathTest() {
        Assert.assertEquals("full/path", ClasspathMatcher.extractPath("some.correct.url:5050/full/path"));
        Exception ex = null;
        try {
            ClasspathMatcher.extractPath("some.correct.url:5050");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        Assert.assertEquals("full/path", ClasspathMatcher.extractPath("some.correct.url/full/path"));
        Assert.assertEquals("full/path", ClasspathMatcher.extractPath("some.url/full/path"));
        Assert.assertEquals("full/path", ClasspathMatcher.extractPath("some:5050/full/path"));
        try {
            ClasspathMatcher.extractPath("some");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        //correct!
        Assert.assertEquals("//", ClasspathMatcher.extractPath("some.correct.url:5050///"));
        //incorrect, but hard to solve
        Assert.assertEquals("/full/path", ClasspathMatcher.extractPath("some.correct.url:5050://full/path"));
        Assert.assertEquals("/ect.url:5050/full/path", ClasspathMatcher.extractPath("some.corr://ect.url:5050/full/path"));
        Assert.assertEquals("/ect.url", ClasspathMatcher.extractPath("some.corr://ect.url"));
        Assert.assertEquals("ful://l/path", ClasspathMatcher.extractPath("some/ful://l/path"));
        //traps
        Assert.assertEquals("full/path/", ClasspathMatcher.extractPath("some.url/full/path/"));
        Assert.assertEquals("full/path/", ClasspathMatcher.extractPath("some:5050/full/path/"));
        Assert.assertEquals("", ClasspathMatcher.extractPath("some.url/"));
        Assert.assertEquals("", ClasspathMatcher.extractPath("some:5050/"));
        try {
            ClasspathMatcher.extractPath("some.url");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            ClasspathMatcher.extractPath("some:5050");
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
    }

    @Test
    public void splitToPartsTest1() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("*");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("*", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest2() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("https://*.example.com");
        Assert.assertEquals("https", p.protocol);
        Assert.assertEquals("*.example.com", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest3() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("www.example.com");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("www.example.com", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest4() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("www.example.com:8085");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("www.example.com", p.domain);
        Assert.assertEquals("8085", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest5() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("*.example.com");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("*.example.com", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest6() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("127.0.0.1");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("127.0.0.1", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTest7() {
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts("127.0.0.1:8080");
        Assert.assertEquals("*", p.protocol);
        Assert.assertEquals("127.0.0.1", p.domain);
        Assert.assertEquals("8080", p.port);
        Assert.assertEquals("*", p.path);

        p.compilePartsToPatterns();
    }

    @Test
    public void splitToPartsTestCorners() {
        ClasspathMatcher.Parts p1 = ClasspathMatcher.splitToParts("aa://bb.cz:1234/path/x.jnlp");
        Assert.assertEquals("aa", p1.protocol);
        Assert.assertEquals("bb.cz", p1.domain);
        Assert.assertEquals("1234", p1.port);
        Assert.assertEquals("path/x.jnlp", p1.path);

        p1.compilePartsToPatterns();

        ClasspathMatcher.Parts p2 = ClasspathMatcher.splitToParts("://*:/");
        Assert.assertEquals("", p2.protocol);//yah, protocol, if :// is presented,  should be defined
        Assert.assertEquals("*", p2.domain);
        Assert.assertEquals("*", p2.port);
        Assert.assertEquals("*", p2.path);

        p2.compilePartsToPatterns();

        ClasspathMatcher.Parts p3 = ClasspathMatcher.splitToParts("*://*:/:aa//");
        Assert.assertEquals("*", p3.protocol);
        Assert.assertEquals("*", p3.domain);
        Assert.assertEquals("*", p3.port);
        Assert.assertEquals(":aa//", p3.path);

        p3.compilePartsToPatterns();
    }

    @Test
    public void sourceToRegExStringTest() {
        Assert.assertEquals(".*", ClasspathMatcher.sourceToRegExString("*"));
        Assert.assertEquals("^.*\\Q\\E.*$", ClasspathMatcher.sourceToRegExString("**"));
        Assert.assertEquals("^\\Qabcd\\E$", ClasspathMatcher.sourceToRegExString("abcd"));
        Assert.assertEquals("^.*\\Qabcd\\E$", ClasspathMatcher.sourceToRegExString("*abcd"));
        Assert.assertEquals("^\\Qabcd\\E.*$", ClasspathMatcher.sourceToRegExString("abcd*"));
    }
    //http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
    //examples
    private static final URL[] urls = {
        silentUrl("https://a.example.com"), /*0*/
        silentUrl("https://a.b.example.com"),
        silentUrl("http://a.example.com"),
        silentUrl("http://a.b.example.com"),/*3*/
        silentUrl("https://www.example.com"),
        silentUrl("http://www.example.com "),
        silentUrl("http://example.com"),/*6*/
        silentUrl("http://example.net"),
        silentUrl("https://www.example.com:8085"),
        silentUrl("http://www.example.com:8085"),/*9*/
        silentUrl("http://www.example.com"),
        silentUrl("https://a.example.com"),
        silentUrl("http://a.example.com"),/*12*/
        silentUrl("https://a.b.example.com"),
        silentUrl("http://a.b.example.com"),
        silentUrl("https://example.com"),/*15*/
        silentUrl("http://example.com"),
        silentUrl("http://example.net"),
        silentUrl("http://127.0.0.1"),/*18*/
        silentUrl("http://127.0.0.1:8080"),
        silentUrl("http://127.0.0.1:80"),
        silentUrl("http://localhost"),/*21*/
        silentUrl("http://127.0.0.1:8080"),
        silentUrl("http://127.0.0.1"),
        silentUrl("http://127.0.0.1:80 ")/*24*/};

    private static URL silentUrl(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void matchTest1() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("*");
        Assert.assertTrue(p.match(new URL("http://any.strange/url/path")));
        for (URL url : urls) {
            Assert.assertTrue(p.match(url));
        }
    }

    @Test
    public void matchTest2() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("https://*.example.com");
        Assert.assertTrue(p.match(urls[0]));
        Assert.assertTrue(p.match(urls[1]));
        Assert.assertFalse(p.match(urls[2]));
        Assert.assertFalse(p.match(urls[3]));
    }

    @Test
    public void matchTest3() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("www.example.com");
        Assert.assertTrue(p.match(urls[4]));
        Assert.assertTrue(p.match(urls[5]));
        Assert.assertFalse(p.match(urls[6]));
        Assert.assertFalse(p.match(urls[7]));
    }

    @Test
    public void matchTest4() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("www.example.com:8085");
        Assert.assertTrue(p.match(urls[8]));
        Assert.assertTrue(p.match(urls[9]));
        Assert.assertFalse(p.match(urls[10]));
    }

    @Test
    public void matchTest5() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("*.example.com");
        Assert.assertTrue(p.match(urls[11]));
        Assert.assertTrue(p.match(urls[12]));
        Assert.assertTrue(p.match(urls[13]));
        Assert.assertTrue(p.match(urls[14]));
        //those represent the "dot" issue 
        Assert.assertTrue(p.match(urls[15]));
        Assert.assertTrue(p.match(urls[16]));
        Assert.assertFalse(p.match(urls[17]));
        //reasons for alowing "dot" issue
        Assert.assertTrue(p.match(new URL("http://www.example.com")));
        Assert.assertTrue(p.match(new URL("http://example.com")));
        //reason for restricting dost issue
        Assert.assertFalse(p.match(new URL("http://aaaexample.com")));
    }

    @Test
    public void wildCardSubdomainDoesNotMatchParentDomainPaths() throws MalformedURLException {
        ClasspathMatchers p1 = ClasspathMatchers.compile("*.example.com*/*.abc.cde*", true);
        Assert.assertFalse(p1.matches(new URL("http://aaaexample.com/xyz.abc.cde")));

        Assert.assertTrue(p1.matches(new  URL("http://www.example.com/.abc.cde")));
        Assert.assertTrue(p1.matches(new  URL("http://www.example.com/xyz.abc.cde")));
        Assert.assertFalse(p1.matches(new URL("http://www.example.com/abc.cde")));
        Assert.assertTrue(p1.matches(new  URL("http://example.com/xyz.abc.cdeefg")));
        Assert.assertTrue(p1.matches(new  URL("http://example.com/xyz.abc.cde.efg")));
        Assert.assertFalse(p1.matches(new URL("http://example.com/abc.cde.efg")));
        Assert.assertFalse(p1.matches(new URL("http://example.com")));


        ClasspathMatchers p = ClasspathMatchers.compile("*.example.com*/*.abc.cde*", false);
        Assert.assertFalse(p.matches(new URL("http://aaaexample.com/xyz.abc.cde")));

        Assert.assertTrue(p.matches(new URL("http://www.example.com/.abc.cde")));
        Assert.assertTrue(p.matches(new URL("http://www.example.com/xyz.abc.cde")));
        Assert.assertTrue(p.matches(new URL("http://www.example.com/abc.cde")));
        Assert.assertTrue(p.matches(new URL("http://example.com/xyz.abc.cdeefg")));
        Assert.assertTrue(p.matches(new URL("http://example.com/xyz.abc.cde.efg")));
        Assert.assertTrue(p.matches(new URL("http://example.com/abc.cde.efg")));
        Assert.assertTrue(p.matches(new URL("http://example.com")));
    }

    @Test
    public void matchTest6() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("127.0.0.1");
        Assert.assertTrue(p.match(urls[18]));
        Assert.assertTrue(p.match(urls[19]));
        Assert.assertTrue(p.match(urls[20]));
        Assert.assertFalse(p.match(urls[21]));
    }

    @Test
    public void matchTest7() throws MalformedURLException {
        ClasspathMatcher p = ClasspathMatcher.compile("127.0.0.1:8080");
        Assert.assertTrue(p.match(urls[22]));
        Assert.assertFalse(p.match(urls[23]));
        Assert.assertFalse(p.match(urls[24]));
    }

    //nasty url tests
    @Test
    public void googleQueryTest() throws MalformedURLException {
        String googleQuery = "https://www.google.cz/search?q=icdtea+web&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a#q=icedtea+web&rls=org.mozilla:en-US:official&safe=off";
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts(googleQuery);
        Assert.assertEquals("https", p.protocol);
        Assert.assertEquals("www.google.cz", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("search?q=icdtea+web&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a#q=icedtea+web&rls=org.mozilla:en-US:official&safe=off", p.path);

        ClasspathMatcher cm = ClasspathMatcher.compile(googleQuery);
        Assert.assertTrue(cm.match(new URL("https://www.google.cz:5050")));
        Assert.assertFalse(cm.match(new URL("https://google.cz:5050")));
        Assert.assertFalse(cm.match(new URL("http://www.google.cz:5050")));
        Assert.assertTrue(cm.match(new URL("https://www.google.cz")));
    }

    @Test
    public void gitHubQueryTest() throws MalformedURLException {
        String gitHubQuery = "https://github.com/user-user/what-1.2.3-an-hell/commit/b8ee66ad265002c51f86152e06fbe2e6124b62a3#diff-36dcc9a4b486abb6bffe062260d6d82dR626";
        ClasspathMatcher.Parts p = ClasspathMatcher.splitToParts(gitHubQuery);
        Assert.assertEquals("https", p.protocol);
        Assert.assertEquals("github.com", p.domain);
        Assert.assertEquals("*", p.port);
        Assert.assertEquals("user-user/what-1.2.3-an-hell/commit/b8ee66ad265002c51f86152e06fbe2e6124b62a3#diff-36dcc9a4b486abb6bffe062260d6d82dR626", p.path);

        ClasspathMatcher cm = ClasspathMatcher.compile(gitHubQuery);
        Assert.assertTrue(cm.match(new URL("https://github.com:0123")));
        Assert.assertFalse(cm.match(new URL("https://www.github.com")));
        Assert.assertFalse(cm.match(new URL("http://github.com")));
        Assert.assertTrue(cm.match(new URL("https://github.com")));
    }

    @Test
    public void doubleSlashesTests() throws MalformedURLException {
        String s1 = "http://some.url//weird/path";
        ClasspathMatcher.Parts p1 = ClasspathMatcher.splitToParts(s1);
        Assert.assertEquals("http", p1.protocol);
        Assert.assertEquals("some.url", p1.domain);
        Assert.assertEquals("*", p1.port);
        Assert.assertEquals("/weird/path", p1.path);

        ClasspathMatcher cm1 = ClasspathMatcher.compile(s1);
        Assert.assertTrue(cm1.match(new URL("http://some.url//weird/path")));
        Assert.assertTrue(cm1.matchWithPath(new URL("http://some.url//weird/path")));
        //path is not ocunted
        Assert.assertTrue(cm1.match(new URL("http://some.url/weird/path")));
        Assert.assertFalse(cm1.matchWithPath(new URL("http://some.url/weird/path")));

        String s2 = "https://some.url/weird//path/";

        ClasspathMatcher.Parts p2 = ClasspathMatcher.splitToParts(s2);
        Assert.assertEquals("https", p2.protocol);
        Assert.assertEquals("some.url", p2.domain);
        Assert.assertEquals("*", p2.port);
        Assert.assertEquals("weird//path/", p2.path);
    }

    //total trap url
    @Test
    public void madUrls() throws MalformedURLException {
        String trapUrl1 = "*://:&:://%%20";
        ClasspathMatcher.Parts p1 = ClasspathMatcher.splitToParts(trapUrl1);
        Assert.assertEquals("*", p1.protocol);
        Assert.assertEquals("", p1.domain);
        Assert.assertEquals("&::", p1.port);
        Assert.assertEquals("/%%20", p1.path);
        ClasspathMatcher cm1 = ClasspathMatcher.compile(trapUrl1);
        //no valid url can match this
        Assert.assertFalse(cm1.match(new URL("ftp://:0//whatever")));
        Assert.assertFalse(cm1.matchWithPath(new URL("ftp://:0//%%20")));
          
        String trapUrl2 = "*://:0//%%20";
        ClasspathMatcher.Parts p2 = ClasspathMatcher.splitToParts(trapUrl2);
        Assert.assertEquals("*", p2.protocol);
        Assert.assertEquals("", p2.domain);
        Assert.assertEquals("0", p2.port);
        Assert.assertEquals("/%%20", p2.path);
        ClasspathMatcher cm2 = ClasspathMatcher.compile(trapUrl2);
        Assert.assertTrue(cm2.match(new URL("ftp://:0//whatever")));
        Assert.assertFalse(cm2.matchWithPath(new URL("ftp://:0//whatever")));
        Assert.assertTrue(cm2.matchWithPath(new URL("ftp://:0//%%20")));
        
        String trapUrl3 = ":0//%%20";
        ClasspathMatcher.Parts p3 = ClasspathMatcher.splitToParts(trapUrl3);
        Assert.assertEquals("*", p3.protocol);
        Assert.assertEquals("", p3.domain);
        Assert.assertEquals("0", p3.port);
        Assert.assertEquals("/%%20", p3.path);
        ClasspathMatcher cm3 = ClasspathMatcher.compile(trapUrl3);
        Assert.assertTrue(cm3.match(new URL("ftp://:0//whatever")));
        Assert.assertFalse(cm3.matchWithPath(new URL("ftp://:0//whatever")));
        Assert.assertTrue(cm3.match(new URL("ftp://:0//%%20")));
    }
    
    
    @Test
    public void matchersTest() throws MalformedURLException {
        ClasspathMatchers cps1  = ClasspathMatcher.ClasspathMatchers.compile("    aa bb     cc     ");
        ArrayList<ClasspathMatcher> q = cps1.getMatchers();
        Assert.assertEquals(3, q.size());
        Assert.assertEquals("aa", q.get(0).getParts().domain);
        Assert.assertEquals("bb", q.get(1).getParts().domain);
        Assert.assertEquals("cc", q.get(2).getParts().domain);
        
        ClasspathMatchers cps2  = ClasspathMatcher.ClasspathMatchers.compile("http://aa.cz ftp://*bb.cz/");
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/aa")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        Assert.assertTrue(cps2.matches(new URL("http://aa.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        
    }
    
    @Test
    public void testStar() throws MalformedURLException {
        ClasspathMatchers cps1  = ClasspathMatcher.ClasspathMatchers.compile("*");
        Assert.assertTrue(cps1.matches(new URL("http://whatever.anywher/something/at.some")));
        Assert.assertTrue(cps1.matches(new URL("http://whatever.anywher/something/at")));
        Assert.assertTrue(cps1.matches(new URL("http://whatever.anywher/")));
        Assert.assertTrue(cps1.matches(new URL("http://whatever.anywher")));
    }
    
     @Test
    public void matchersTestWithPathsNix() throws MalformedURLException {
        ClasspathMatchers cps1 = ClasspathMatcher.ClasspathMatchers.compile("    aa bb     cc     ", true);
        ArrayList<ClasspathMatcher> q = cps1.getMatchers();
        Assert.assertEquals(3, q.size());
        Assert.assertEquals("aa", q.get(0).getParts().domain);
        Assert.assertEquals("bb", q.get(1).getParts().domain);
        Assert.assertEquals("cc", q.get(2).getParts().domain);


        ClasspathMatchers cps2 = ClasspathMatcher.ClasspathMatchers.compile("http://aa.cz/xyz  ftp://*bb.cz/bcq/dfg/aa*", true);
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/aa")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        //star
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq/aa-test.html")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg-aa-test.html")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg/aa-test.html")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg/aa")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg/aa/")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg/aa-files/aaa.jar")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg\\aa-files\\aaa.jar")));
        //double quotes may harm
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz//bcq/dfg/aa-files/aaa.jar")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz//bcq/dfg/aa-files//aaa.jar")));
        //no star
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz")));
        Assert.assertTrue(cps2.matches(new URL("http://aa.cz/xyz")));
        //double quotes may harm again
        Assert.assertTrue(cps2.matches(new URL("http://aa.cz/xyz/")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz//xyz")));



    }

    @Test
    public void matchersTestWithPathsWin() throws MalformedURLException {
        ClasspathMatchers cps1 = ClasspathMatcher.ClasspathMatchers.compile("    aa bb     cc     ", true);
        ArrayList<ClasspathMatcher> q = cps1.getMatchers();
        Assert.assertEquals(3, q.size());
        Assert.assertEquals("aa", q.get(0).getParts().domain);
        Assert.assertEquals("bb", q.get(1).getParts().domain);
        Assert.assertEquals("cc", q.get(2).getParts().domain);


        ClasspathMatchers cps2 = ClasspathMatcher.ClasspathMatchers.compile("http://aa.cz/xyz  ftp://*bb.cz/bcq\\dfg\\aa*", true);
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/aa")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        //star
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq\\aa-test.html")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg-aa-test.html")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg\\aa-test.html")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg\\aa")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg\\aa\\")));
        Assert.assertTrue(cps2.matches(new URL("ftp://123.bb.cz/bcq\\dfg\\aa-files\\aaa.jar")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz/bcq/dfg/aa-files/aaa.jar")));
        //double quotes may harm
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz//bcq\\dfg\\aa-files\\aaa.jar")));
        Assert.assertFalse(cps2.matches(new URL("ftp://123.bb.cz//bcq\\dfg\\aa-files\\aaa.jar")));
        //no star
        Assert.assertFalse(cps2.matches(new URL("http://bb.cz")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz")));
        Assert.assertTrue(cps2.matches(new URL("http://aa.cz/xyz")));
        //double quotes may harm again
        Assert.assertTrue(cps2.matches(new URL("http://aa.cz/xyz\\")));
        Assert.assertFalse(cps2.matches(new URL("http://aa.cz//xyz")));



    }

    @Test
    public void trickyPathsMatchTes() throws MalformedURLException {
        ClasspathMatchers cps1 = ClasspathMatcher.ClasspathMatchers.compile("http://aaa.com/some/path", true);
        ClasspathMatchers cps11 = ClasspathMatcher.ClasspathMatchers.compile("http://aaa.com/some/path", false);
        ClasspathMatchers cps2 = ClasspathMatcher.ClasspathMatchers.compile("http://aaa.com/some/path/", true);
        ClasspathMatchers cps22 = ClasspathMatcher.ClasspathMatchers.compile("http://aaa.com/some/path/", false);

        Assert.assertTrue(cps1.matches(new URL("http://aaa.com/some/path")));
        Assert.assertTrue(cps1.matches(new URL("http://aaa.com/some/path/")));

        Assert.assertFalse(cps2.matches(new URL("http://aaa.com/some/path")));
        Assert.assertTrue(cps2.matches(new URL("http://aaa.com/some/path/")));


        Assert.assertTrue(cps11.matches(new URL("http://aaa.com/some/path")));
        Assert.assertTrue(cps11.matches(new URL("http://aaa.com/some/path/")));

        Assert.assertTrue(cps22.matches(new URL("http://aaa.com/some/path")));
        Assert.assertTrue(cps22.matches(new URL("http://aaa.com/some/path/")));

    }
}
