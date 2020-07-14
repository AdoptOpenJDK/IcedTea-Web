package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class UrlWhiteListUtils {
    private final static Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);
    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HTTP_PORT = "80";
    public static final String HTTPS_PORT = "443";

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
                        final URL wlUrl = new URL(wlUrlStr);
                        return UrlUtils.notNullUrlEquals(url, wlUrl);
                    } else {
                        final URL expandedWlUrl = new URL(UrlWhiteListUtils.expandedWhiteListUrlString(wlUrlStr));
                        final boolean isUrlProtocolEqual = isUrlProtocolEqual(expandedWlUrl, url);
                        final boolean isUrlHostWithWildcardEqual = isUrlHostWithWildcardEqual(expandedWlUrl, url);
                        final boolean isUrlPortEqual = expandedWlUrl.getPort() != -1 ? expandedWlUrl.getPort() == url.getPort() : true;
                        return isUrlProtocolEqual && isUrlHostWithWildcardEqual && isUrlPortEqual;
                    }
                } catch (Exception e) {
                    LOG.debug("Bad white list url: " + wlUrlStr);
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
        try {
            final URL expURL = new URL(expandedUrlStr);
            if (expURL.getPort() == -1) {
                if (expURL.getProtocol().equalsIgnoreCase(HTTP)) {
                    expandedUrlStr = expandedUrlStr + ":" + HTTP_PORT;
                }
                if (expURL.getProtocol().equalsIgnoreCase(HTTPS)) {
                    expandedUrlStr = expandedUrlStr + ":" + HTTPS_PORT;
                }
            }
        } catch (Exception e) {
            final int ind = expandedUrlStr.lastIndexOf(":");
            if (e.getCause() instanceof NumberFormatException && expandedUrlStr.substring(ind+1, expandedUrlStr.length()).equals("*") ) {
                expandedUrlStr =  expandedUrlStr.substring(0,ind) + "";
            }
        }
        return expandedUrlStr;
    }

    public static String expandedWhiteListUrlString(final String wlUrlStr) {
        final String expandedUrlProtocol = expandProtocol(wlUrlStr);
        final String expandedUrlStr = expandPort(expandedUrlProtocol);
        return expandedUrlStr;
    }
}
