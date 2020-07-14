/* UrlWhiteListUtils.java
 Copyright (C) 2011 Red Hat, Inc.

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
statement from your version.
*/
package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class UrlWhiteListUtils {
    private final static Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);

    public static boolean isUrlProtocolEqual(URL url1, URL url2) {
        Assert.requireNonNull(url1, "url");
        Assert.requireNonNull(url2, "url");
        return url1.getProtocol().equals(url2.getProtocol());
    }

    public static boolean isUrlHostWithWildcardEqual(URL urlWithWildcard, URL url) {
        Assert.requireNonNull(urlWithWildcard, "url");
        Assert.requireNonNull(url, "url");
        String[] wlUrlHostParts = urlWithWildcard.getHost().split("\\.");
        String[] urlHostParts = url.getHost().split("\\.");
        // proto://*:port
        if (wlUrlHostParts.length == 1 && wlUrlHostParts[0].length() ==1 && wlUrlHostParts[0].charAt(0) == '*') {
            return true;
        }
        if (wlUrlHostParts.length != urlHostParts.length) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < wlUrlHostParts.length; i++) {
            result = result && (wlUrlHostParts[i].charAt(0) == '*' || wlUrlHostParts[i].equals(urlHostParts[i]));
        }
        return result;
    }

    public static boolean isUrlInWhitelist(final URL url, final List<String> whiteList, final boolean allowLocalhost, final boolean exactMatch) {
        Assert.requireNonNull(url, "url");
        Assert.requireNonNull(whiteList, "whiteList");

        if (whiteList.isEmpty()) {
            return true; // empty whitelist == allow all connections
        }

        // if host is null or "" or it is localhost or loopback
        if (allowLocalhost && IpUtil.isLocalhostOrLoopback(url)) {
            return true; // local server need not be in whitelist
        }

        boolean result = whiteList.stream().anyMatch(wlUrlStr -> {
                try {
                    if (exactMatch) {
                        URL wlUrl = new URL(wlUrlStr);
                        return UrlUtils.notNullUrlEquals(url, wlUrl);
                    } else {
                        URL expandedWlUrl = new URL(UrlWhiteListUtils.expandedWhiteListUrlString(wlUrlStr));
                        return (isUrlProtocolEqual(expandedWlUrl, url) && isUrlHostWithWildcardEqual(expandedWlUrl, url)
                            && (expandedWlUrl.getPort() != -1 ? expandedWlUrl.getPort() == url.getPort() : true));
                    }
                } catch (Exception e) {
                    return false;
                }
        });
        return result;
    }

    private static String expandProtocol(final String wlUrlStr)  {
        String expandedUrlStr = wlUrlStr;
        try {
            new URL(expandedUrlStr);
        } catch (Exception e) {
            if (e instanceof MalformedURLException && e.getMessage().contains("no protocol")) {
                expandedUrlStr = "https://" + expandedUrlStr;
            }
        }
        return expandedUrlStr;
    }

    private static String expandPort(final String wlUrlStr) {
        String expandedUrlStr = wlUrlStr;
        URL expURL = null;
        try {
            expURL = new URL(expandedUrlStr);
            if (expURL.getPort() == -1) {
                if (expURL.getProtocol().equalsIgnoreCase(("http"))) {
                    expandedUrlStr = expandedUrlStr + ":80";
                }
                if (expURL.getProtocol().equalsIgnoreCase(("https"))) {
                    expandedUrlStr = expandedUrlStr + ":443";
                }
            }
        } catch (Exception e) {
            int ind = expandedUrlStr.lastIndexOf(":");
            if (e.getCause() instanceof NumberFormatException && expandedUrlStr.substring(ind+1, expandedUrlStr.length()).equals("*") ) {
                expandedUrlStr =  expandedUrlStr.substring(0,ind) + "";
            }
        }
        return expandedUrlStr;
    }

    public static String expandedWhiteListUrlString(final String wlUrlStr) {
        String expandedUrlStr = expandProtocol(wlUrlStr);
        expandedUrlStr = expandPort(expandedUrlStr);
        return expandedUrlStr;
    }
}
