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

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

public class UrlWhiteListUtils {
    private static final String WILDCARD = "*";
    private static final String HOSTPARTSEPARATOR = "\\.";
    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HTTP_PORT = "80";
    public static final String HTTPS_PORT = "443";

    private final static Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);

    private static List<ValidatedWhiteListEntry> whiteList;
    private static final Lock whiteListLock = new ReentrantLock();

    public static List<ValidatedWhiteListEntry> getValidatedWhiteList() {
        whiteListLock.lock();
        try {
            if (whiteList == null) {
                whiteList = JNLPRuntime.getConfiguration().getPropertyAsList(KEY_SECURITY_SERVER_WHITELIST)
                        .stream()
                        .filter(s -> !StringUtils.isBlank(s))
                        .map(s -> UrlWhiteListUtils.validateWhiteListUrlString(s))
                        .collect(Collectors.toList());
            }
            return whiteList;
        } finally {
            whiteListLock.unlock();
        }
    }

    private static boolean isUrlProtocolMatching(URL url1, URL url2) {
        Assert.requireNonNull(url1, "url");
        Assert.requireNonNull(url2, "url");
        return url1.getProtocol().equals(url2.getProtocol());
    }

    private static boolean isUrlHostMatching(URL wlUrl, URL url) {
        Assert.requireNonNull(wlUrl, "url");
        Assert.requireNonNull(url, "url");
        String[] wlUrlHostParts = wlUrl.getHost().split(HOSTPARTSEPARATOR);
        String[] urlHostParts = url.getHost().split(HOSTPARTSEPARATOR);
        // proto://*:port
        if (wlUrlHostParts.length == 1 && wlUrlHostParts[0].length() == 1 && wlUrlHostParts[0].charAt(0) == WILDCARD.charAt(0)) {
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

    private static boolean isUrlPortMatching(URL wlUrl, URL url) {
        return wlUrl.getPort() != -1 ? wlUrl.getPort() == url.getPort() : true;
    }

    public static boolean isUrlInWhitelist(final URL url) {
        final List<String> whiteList = getValidatedWhiteList().stream().map(vwl -> vwl.getWhiteListEntry()).collect(Collectors.toList());
        return isUrlInWhitelist(url, whiteList, true, false);
    }

    public static boolean isUrlInWhitelist(final URL url, final List<String> whiteList, final boolean allowLocalhost, final boolean exactMatch) {
        Assert.requireNonNull(url, "url");
        Assert.requireNonNull(whiteList, "whiteList");

        if (whiteList.isEmpty()) {
            return true; // empty whitelist == allow all connections
        }

        // is it localhost or loopback
        if (allowLocalhost && IpUtil.isLocalhostOrLoopback(url)) {
            return true; // local server need not be in whitelist
        }

        final boolean result = whiteList.stream().anyMatch(wlUrlStr -> {
            try {
                final URL wlUrl = new URL(wlUrlStr);
                if (exactMatch) {
                    return UrlUtils.notNullUrlEquals(url, wlUrl);
                } else {
                    return isUrlProtocolMatching(wlUrl, url) && isUrlHostMatching(wlUrl, url) && isUrlPortMatching(wlUrl, url);
                }
            } catch (Exception e) {
                LOG.warn("Bad white list url: " + wlUrlStr);
                return false;
            }
        });
        return result;
    }

    private static String validateWhiteListUrlProtocol(final String wlUrlStr) throws MalformedURLException {
        // TODO Improve detection of no protocol
        try {
            new URL(wlUrlStr);
        } catch (Exception e) {
            // If protocol is missing then assume it is https
            if (e instanceof MalformedURLException && (e.getMessage().contains("no protocol") || e.getMessage().contains("unknown protocol"))) {
                return HTTPS + "://" + wlUrlStr;
            } else if (e.getMessage().contains("protocol")) {
                throw e;
            }
        }
        return wlUrlStr;
    }

    private static String validatewhiteListUrlPort(final String wlUrlStr) throws MalformedURLException {
        // TODO : Decide whether to enforce default port
        try {
            final URL wlUrl = new URL(wlUrlStr);
            // if port is missing then take it as default port for the protocol
            if (wlUrl.getPort() == -1) {
                if (wlUrl.getProtocol().equalsIgnoreCase(HTTP)) {
                    return wlUrlStr + ":" + HTTP_PORT;
                }
                if (wlUrl.getProtocol().equalsIgnoreCase(HTTPS)) {
                    return wlUrlStr + ":" + HTTPS_PORT;
                }
            }
        } catch (Exception e) {
            // if port is illegal due to * then replace * with ""
            final int ind = wlUrlStr.lastIndexOf(":");
            if (e.getCause() instanceof NumberFormatException && wlUrlStr.substring(ind + 1, wlUrlStr.length()).equals(WILDCARD)) {
                return wlUrlStr.substring(0, ind) + "";
            }
            throw e;
        }
        return wlUrlStr;
    }

    private static void validateWhiteListUrlHost(final String wlUrlStr) throws Exception {
        final URL wlURL = new URL(wlUrlStr);
        String host = wlURL.getHost();
        String[] hostParts = host.split(HOSTPARTSEPARATOR);
        // check if a host part contains * then that is the only character
        for (String s : hostParts) {
            if (s.contains(WILDCARD) && s.length() > 1) {
                throw new Exception(R("SWPVALIDATEHOST"));
            }
        }
    }

    static ValidatedWhiteListEntry validateWhiteListUrlString(final String wlUrlStr) {
        try {
            final String validatedWLUrlProtocol = validateWhiteListUrlProtocol(wlUrlStr);
            final String validatedWLUrlStr = validatewhiteListUrlPort(validatedWLUrlProtocol);
            validateWhiteListUrlHost(validatedWLUrlStr);
            return new ValidatedWhiteListEntry(validatedWLUrlStr);
        } catch (Exception e) {
            return new ValidatedWhiteListEntry(wlUrlStr, R("SWPVALIDATEWLURL") + ": " + e.getMessage());
        }
    }

    public static class ValidatedWhiteListEntry {
        private final String whiteListEntry;
        private final String errorMessage;

        public String getWhiteListEntry() {
            return whiteListEntry;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public ValidatedWhiteListEntry(final String whiteListEntry) {
            this(whiteListEntry, "");
        }

        public ValidatedWhiteListEntry(final String whiteListEntry, final String errorMessage) {
            this.whiteListEntry = whiteListEntry;
            this.errorMessage = errorMessage;
        }
    }
}
