package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

public class UrlWhiteListUtils {
    private static List<String> expandedWhitelist;
    private static final Lock whiteListLock = new ReentrantLock();

    private final static Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);
    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HTTP_PORT = "80";
    public static final String HTTPS_PORT = "443";

    public static List<String> getExpandedWhiteList() {
        whiteListLock.lock();
        try {
            if (expandedWhitelist == null) {
                expandedWhitelist = JNLPRuntime.getConfiguration().getPropertyAsList(KEY_SECURITY_SERVER_WHITELIST)
                        .stream()
                        .filter(s -> !StringUtils.isBlank(s))
                        .map(s -> UrlWhiteListUtils.expandWhiteListUrlString(s))
                        .collect(Collectors.toList());

            }
            return expandedWhitelist;
        } finally {
            whiteListLock.unlock();
        }
    }

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
        if (wlUrlHostParts.length == 1 && wlUrlHostParts[0].length() == 1 && wlUrlHostParts[0].charAt(0) == '*') {
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

        final boolean result = whiteList.stream().anyMatch(wlUrlStr -> {
            try {
                if (exactMatch) {
                    final URL wlUrl = new URL(wlUrlStr);
                    return UrlUtils.notNullUrlEquals(url, wlUrl);
                } else {
//                        final URL expandedWlUrl = new URL(UrlWhiteListUtils.expandWhiteListUrlString(wlUrlStr));
                    final URL expandedWlUrl = new URL(wlUrlStr);
                    final boolean isUrlProtocolEqual = isUrlProtocolEqual(expandedWlUrl, url);
                    final boolean isUrlHostWithWildcardEqual = isUrlHostWithWildcardEqual(expandedWlUrl, url);
                    final boolean isUrlPortEqual = expandedWlUrl.getPort() != -1 ? expandedWlUrl.getPort() == url.getPort() : true;
                    return isUrlProtocolEqual && isUrlHostWithWildcardEqual && isUrlPortEqual;
                }
            } catch (Exception e) {
                LOG.warn("Bad white list url: " + wlUrlStr);
                return false;
            }
        });
        return result;
    }

    private static String expandProtocol(final String wlUrlStr) {
        try {
            new URL(wlUrlStr);
        } catch (Exception e) {
            // If protocol is missing then assume it is https
            if (e instanceof MalformedURLException && e.getMessage().contains("no protocol")) {
                return HTTPS + "://" + wlUrlStr;
            }
        }
        return wlUrlStr;
    }

    private static String expandPort(final String wlUrlStr) {
        try {
            final URL expURL = new URL(wlUrlStr);
            // if port is missing then take it as default port for the protocol
            if (expURL.getPort() == -1) {
                if (expURL.getProtocol().equalsIgnoreCase(HTTP)) {
                    return wlUrlStr + ":" + HTTP_PORT;
                }
                if (expURL.getProtocol().equalsIgnoreCase(HTTPS)) {
                    return wlUrlStr + ":" + HTTPS_PORT;
                }
            }
        } catch (Exception e) {
            // if port is illegal due to * then replace * with ""
            final int ind = wlUrlStr.lastIndexOf(":");
            if (e.getCause() instanceof NumberFormatException && wlUrlStr.substring(ind + 1, wlUrlStr.length()).equals("*")) {
                return wlUrlStr.substring(0, ind) + "";
            }
        }
        return wlUrlStr;
    }

    public static String expandWhiteListUrlString(final String wlUrlStr) {
        final String expandedUrlProtocol = expandProtocol(wlUrlStr);
        final String expandedUrlStr = expandPort(expandedUrlProtocol);
        return expandedUrlStr;
    }
}
