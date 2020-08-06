package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.util.Objects;

/**
 * ...
 */
public class WhitelistEntry {
    private static final String WILDCARD = "*";
    private static final String HOST_PART_REGEX = "\\.";

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistEntry.class);

    private final String rawWhitelistEntry;
    private final String effectiveWhitelistEntry;
    private final String errorMessage;

    static WhitelistEntry validWhitelistEntry(final String wlEntry, final String validatedEntry) {
        return new WhitelistEntry(wlEntry, validatedEntry, null);
    }

    static WhitelistEntry invalidWhitelistEntry(final String wlEntry, final String errorMessage) {
        return new WhitelistEntry(wlEntry, null, errorMessage);
    }

    private WhitelistEntry(final String rawWhitelistEntry, final String effectiveWhitelistEntry, final String errorMessage) {
        this.rawWhitelistEntry = rawWhitelistEntry;
        this.effectiveWhitelistEntry = effectiveWhitelistEntry;
        this.errorMessage = errorMessage;
    }

    public String getRawWhitelistEntry() {
        return rawWhitelistEntry;
    }

    public String getEffectiveWhitelistEntry() {
        return effectiveWhitelistEntry;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isValid() {
        return errorMessage == null;
    }

    public boolean matches(URL url) {
        try {
            if (isValid()) { // ignore invalid whitelist entries
                final URL wlUrl = new URL(getEffectiveWhitelistEntry());
                return isUrlProtocolMatching(wlUrl, url) && isUrlHostMatching(wlUrl, url) && isUrlPortMatching(wlUrl, url);
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.warn("Bad white list url: " + getEffectiveWhitelistEntry());
            return false;
        }
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
}
