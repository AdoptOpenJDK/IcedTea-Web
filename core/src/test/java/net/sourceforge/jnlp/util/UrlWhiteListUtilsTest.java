/*
   Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */

package net.sourceforge.jnlp.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static net.sourceforge.jnlp.util.UrlWhiteListUtils.isUrlInWhitelist;

public class UrlWhiteListUtilsTest {

    @Test
    public void expandLegalWhitelistUrlString() {
        Assert.assertEquals("http://subdomain.domain.com:8080", UrlWhiteListUtils.expandedWhiteListUrlString("http://subdomain.domain.com:8080"));

        Assert.assertEquals("https://domain.com:443", UrlWhiteListUtils.expandedWhiteListUrlString("domain.com"));
        Assert.assertEquals("https://*.domain.com:443", UrlWhiteListUtils.expandedWhiteListUrlString("*.domain.com"));
        Assert.assertEquals("https://*.domain.com", UrlWhiteListUtils.expandedWhiteListUrlString("*.domain.com:*"));

        Assert.assertEquals("http://subdomain.domain.com:80", UrlWhiteListUtils.expandedWhiteListUrlString("http://subdomain.domain.com"));
        Assert.assertEquals("https://subdomain.domain.com:443", UrlWhiteListUtils.expandedWhiteListUrlString("https://subdomain.domain.com"));

        Assert.assertEquals("http://subdomain.domain.com", UrlWhiteListUtils.expandedWhiteListUrlString("http://subdomain.domain.com:*"));
        Assert.assertEquals("https://subdomain.domain.com", UrlWhiteListUtils.expandedWhiteListUrlString("https://subdomain.domain.com:*"));

        Assert.assertEquals("http://*:80", UrlWhiteListUtils.expandedWhiteListUrlString("http://*:80"));
        Assert.assertEquals("https://*:443", UrlWhiteListUtils.expandedWhiteListUrlString("https://*:443"));

        Assert.assertEquals("http://*", UrlWhiteListUtils.expandedWhiteListUrlString("http://*:*"));
        Assert.assertEquals("https://*", UrlWhiteListUtils.expandedWhiteListUrlString("https://*:*"));
    }

    @Test(expected = MalformedURLException.class)
    public void expandIllegalWhitelistUrlString() throws MalformedURLException {
        URL expURL = new URL(UrlWhiteListUtils.expandedWhiteListUrlString("https://subdomain.domain.com:1*"));
    }

    @Test
    public void urlInWhiteList() throws Exception {
        List<String> wList = Arrays.asList(new String[]{
                "https://rfy.m-b.com",
                "https://*.m-b.com",
                "https://rfy.*.com",
                "https://rfy.m-b.*",
                "https://*.*.*:446",
                "https://*:447",
                "https://*.mydomain.com",
                "http://*.mydomain.com",
                "*.cintra.net",
                "*.dmlr.com"});

        // "https://rfry.m-b.com"
        URL url = new URL("https://rfy.m-b.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); //+ " https://rfy.m-b.com:443/some_URL");

        // "https://rfy.m-b.com:443"
        url = new URL("https://rfy.m-b.com:445/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b.com:445/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfyA.m-b.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b.com:443/some_URL");

        // "https://rfy.*.com"
        url = new URL("https://rfy.m-b1.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b1.com:443/some_URL");

        //  "https://*.m-b.com"
        url = new URL("https://rfy.m-b.org:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy.m-b.org:443/some_URL");

        // "https://*.*.*:446"
        url = new URL("https://rfy1.m-b1.org:446/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*:447"
        url = new URL("https://rfy1.m-b1.org:447/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*.*.*:446"
        url = new URL("https://rfy1.m-b1.com:445/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.com:445/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "https://*.mydomain.com"
        url = new URL("https://abc.mydomain.com:444/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:80/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "http://*.mydomain.com"
        url = new URL("http://abc.mydomain.com:81/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.cintra.net"
        url = new URL("https://abc.cintra.net:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        url = new URL("http://abc.cintra.net:443/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:443/some_URL");
        Assert.assertTrue(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");

        // "*.dmlr.com"
        url = new URL("https://abc.dmlr.com:44/some_URL");
        Assert.assertFalse(isUrlInWhitelist(url, wList, true, false)); // + " https://rfy1.m-b1.org:446/some_URL");
    }
}
