package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.IpUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

public class UrlWhiteListUtils {
    private static final String WILDCARD = "*";
    private static final String HOST_PART_SEP = ".";
    private static final String HOST_PART_REGEX = "\\.";
    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    private static final String PROTOCOL_SEPARATOR = "://";

    private static final Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);

    private static List<WhitelistEntry> applicationUrlWhiteList;
    private static final Lock whiteListLock = new ReentrantLock();

    public static List<WhitelistEntry> getApplicationUrlWhiteList() {
        whiteListLock.lock();
        try {
            if (applicationUrlWhiteList == null) {
                applicationUrlWhiteList = JNLPRuntime.getConfiguration().getPropertyAsList(KEY_SECURITY_SERVER_WHITELIST)
                        .stream()
                        .filter(s -> !StringUtils.isBlank(s))
                        .map(UrlWhiteListUtils::validateWhitelistUrl)
                        .collect(Collectors.toList());
            }
            return applicationUrlWhiteList;
        } finally {
            whiteListLock.unlock();
        }
    }

    public static boolean isUrlInApplicationUrlWhitelist(final URL url) {
        return isUrlInWhitelist(url, getApplicationUrlWhiteList(), true);
    }

    static boolean isUrlInWhitelist(final URL url, final List<WhitelistEntry> whiteList, final boolean allowLocalhost) {
        Assert.requireNonNull(url, "url");
        Assert.requireNonNull(whiteList, "whiteList");

        if (whiteList.isEmpty()) {
            return true; // empty whitelist == allow all connections
        }

        // is it localhost or loopback
        if (allowLocalhost && IpUtil.isLocalhostOrLoopback(url)) {
            return true; // local server need not be in whitelist
        }

        final boolean result = whiteList.stream().anyMatch(wlEntry -> {
            try {
                if (wlEntry.isValid()) { // ignore invalid whitelist entries
                    final URL wlUrl = new URL(wlEntry.getValidatedWhitelistEntry());
                    return isUrlProtocolMatching(wlUrl, url) && isUrlHostMatching(wlUrl, url) && isUrlPortMatching(wlUrl, url);
                } else {
                    return false;
                }
            } catch (Exception e) {
                LOG.warn("Bad white list url: " + wlEntry.getValidatedWhitelistEntry());
                return false;
            }
        });
        return result;
    }

    private static boolean isUrlProtocolMatching(URL url1, URL url2) {
        Assert.requireNonNull(url1, "url1");
        Assert.requireNonNull(url2, "url2");
        return Objects.equals(url1.getProtocol(), url2.getProtocol());
    }

    private static boolean isUrlHostMatching(URL wlUrl, URL url) {
        Assert.requireNonNull(wlUrl, "wlUrl");
        Assert.requireNonNull(url, "url");

        // proto://*:port
        if (Objects.equals(wlUrl.getHost(), WILDCARD)) {
            return true;
        }

        final String[] wlUrlHostParts = wlUrl.getHost().split(HOST_PART_REGEX);
        final String[] urlHostParts = url.getHost().split(HOST_PART_REGEX);

        if (wlUrlHostParts.length != urlHostParts.length) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < wlUrlHostParts.length; i++) {
            // hostparts are equal if whitelist url has * or they are same
            result = result && (Objects.equals(wlUrlHostParts[i], WILDCARD) || Objects.equals(wlUrlHostParts[i], urlHostParts[i]));
        }
        return result;
    }

    private static boolean isUrlPortMatching(URL wlUrl, URL url) {
        Assert.requireNonNull(wlUrl, "wlUrl");
        Assert.requireNonNull(url, "url");

        if (wlUrl.getPort() != -1) {
            // url does not have port then force default port as we do the same for whitelist url
            if (url.getPort() == -1) {
                return wlUrl.getPort() == url.getDefaultPort();
            } else {
                return wlUrl.getPort() == url.getPort();
            }
        }
        return true;
    }

    private static String validateWhitelistUrlProtocol(final String wlUrlStr) throws MalformedURLException {
        final String[] splitProtocol = wlUrlStr.split(PROTOCOL_SEPARATOR);
        if (splitProtocol.length == 1) {
            final char firstChar = wlUrlStr.charAt(0);
            if (PROTOCOL_SEPARATOR.indexOf(firstChar) == -1) { // firstChar is not / or :
                return HTTPS + PROTOCOL_SEPARATOR + wlUrlStr;
            }
        }
        try {
            new URL(wlUrlStr);
        } catch (Exception e) {
            if (e.getMessage().contains("protocol")) {
                throw e;
            }
        }
        return wlUrlStr;
    }

    private static String validateWhitelistUrlPort(final String wlUrlStr) throws MalformedURLException {
        try {
            final URL wlUrl = new URL(wlUrlStr);
            // if port is missing then take it as default port for the protocol
            if (wlUrl.getPort() == -1) {
                return wlUrl.getProtocol() + PROTOCOL_SEPARATOR + wlUrl.getHost() + ":" + wlUrl.getDefaultPort();
            }
        } catch (Exception e) {
            // if port is illegal due to * then replace * with ""
            final int ind = wlUrlStr.lastIndexOf(":");
            if (e.getCause() instanceof NumberFormatException && Objects.equals(wlUrlStr.substring(ind + 1), WILDCARD)) {
                return wlUrlStr.substring(0, ind);
            }
            throw e;
        }
        return wlUrlStr;
    }

    // IP Address => all digits, 4 parts, *, -
    private static boolean isIP(final String wlUrlStr) {
        final boolean hasValidChars = wlUrlStr.replace(HOST_PART_SEP.charAt(0), '0').chars().allMatch(c -> Character.isDigit(c) || c == WILDCARD.charAt(0) || c == '-');
        final String[] ipParts = wlUrlStr.split(HOST_PART_REGEX);
        return hasValidChars && ipParts.length == 4;
    }

    private static void validateIPPart(final String ipPart) throws Exception {
        if (ipPart.contains(WILDCARD) || ipPart.contains("-")) {
            throw new Exception(R("SWPINVALIDIPHOST"));
        }
        try {
            final int ipPartInt = Integer.parseInt(ipPart);
            if (ipPartInt < 0 || ipPartInt > 255) {
                throw new Exception(R("SWPINVALIDIPHOST"));
            }
        } catch (NumberFormatException nfe) {
            throw new Exception(R("SWPINVALIDIPHOST"));
        }
    }

    private static void validateWhitelistUrlHost(final String wlUrlStr) throws Exception {
        final URL wlURL = new URL(wlUrlStr);
        final String hostStr = wlURL.getHost();

        // Whitelist Host is *
        if (Objects.equals(hostStr, WILDCARD)) {
            return;
        }

        final boolean isIPHost = isIP(hostStr);
        final String[] hostParts = hostStr.split(HOST_PART_REGEX);
        for (int i = 0; i < hostParts.length; i++) {
            if (isIPHost) {
                validateIPPart(hostParts[i]);
            } else { // non IP host
                // * is allowed only in first part and it should be the only char
                if (hostParts[i].contains(WILDCARD) && (hostParts[i].length() > 1 || i != 0)) {
                    throw new Exception(R("SWPVALIDATEHOST"));
                }
            }
        }
    }

    static WhitelistEntry validateWhitelistUrl(final String wlUrlStr) {
        Assert.requireNonNull(wlUrlStr, "wlUrlStr");
        try {
            final String validatedWLUrlProtocol = validateWhitelistUrlProtocol(wlUrlStr);
            final String validatedWLUrlStr = validateWhitelistUrlPort(validatedWLUrlProtocol);
            validateWhitelistUrlHost(validatedWLUrlStr);
            return WhitelistEntry.validWhitelistEntry(wlUrlStr, validatedWLUrlStr);
        } catch (Exception e) {
            return WhitelistEntry.invalidWhitelistEntry(wlUrlStr, e.getMessage());
        }
    }

    public static class WhitelistEntry {
        private final String whitelistEntry;
        private final String validatedWhitelistEntry;
        private final String errorMessage;

        public String getWhitelistEntry() {
            return whitelistEntry;
        }

        public String getValidatedWhitelistEntry() {
            return validatedWhitelistEntry;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        private WhitelistEntry(final String whitelistEntry, final String validatedWhitelistEntry, final String errorMessage) {
            this.whitelistEntry = whitelistEntry;
            this.validatedWhitelistEntry = validatedWhitelistEntry;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return errorMessage == null;
        }

        public static WhitelistEntry validWhitelistEntry(final String wlEntry, final String validatedEntry) {
            return new WhitelistEntry(wlEntry, validatedEntry, null);
        }

        public static WhitelistEntry invalidWhitelistEntry(final String wlEntry, final String errorMessage) {
            return new WhitelistEntry(wlEntry, null, errorMessage);
        }
    }
}
