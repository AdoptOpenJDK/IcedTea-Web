package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * The host information of a parsed whitelist entry.
 */
abstract class WhitelistEntryHost extends WhitelistEntryPart {

    private static final char WILDCARD_CHAR = '*';

    private static final Pattern ALPHA_CHAR_PATTERN = Pattern.compile(".*[a-z,A-Z].*");


    static WhitelistEntryHost get(final String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return new InvalidHost("Invalid Host: host is missing");
        }
        if (candidate.indexOf(WILDCARD_CHAR) == 0) {
            final String withoutStarPrefix = candidate.substring(1);
            if (withoutStarPrefix.isEmpty()) {
                return AnyHost.INSTANCE;
            }
            if (withoutStarPrefix.indexOf(WILDCARD_CHAR) >= 0) {
                return new InvalidHost("Invalid Host: '*' can only be the very first character of the host");
            }
            if (withoutStarPrefix.indexOf('.') != 0) {
                return new InvalidHost("Invalid Host: after '*' must follow a '.'");
            }
            if (ALPHA_CHAR_PATTERN.matcher(withoutStarPrefix).matches()) {
                return new WildcardHost(candidate);
            }
            return new InvalidHost("Invalid Host: wildcards are not allowed in IP addresses");
        }

        if (candidate.indexOf(WILDCARD_CHAR) >= 0) {
            return new InvalidHost("Invalid Host: '*' can only be the very first character of the host");
        }

        return new ExactHost(candidate);
    }

    private WhitelistEntryHost(final boolean valid, final String effective, final String error) {
        super(valid, effective, error);
    }

    /**
     * Exact host. Does not contain a wildcard.
     */
    private static class ExactHost extends WhitelistEntryHost {
        private final String host;

        private ExactHost(final String host) {
            super(true, host, null);
            this.host = host;
        }

        @Override
        public boolean matches(final URL url) {
            return host.equalsIgnoreCase(url.getHost());
        }
    }

    /**
     * Wildcard host. Matches any host with the given postfix.
     */
    private static class WildcardHost extends WhitelistEntryHost {
        private final String hostPostfix;
        private final int hostPostfixLength;

        private WildcardHost(final String hostWithWildcard) {
            super(true, hostWithWildcard, null);
            this.hostPostfix = hostWithWildcard.substring(1);
            this.hostPostfixLength = hostPostfix.length();
        }

        @Override
        public boolean matches(final URL url) {
            final String fromUrl = url.getHost();
            final int fromUrlLength = fromUrl.length();

            if (fromUrlLength <= hostPostfixLength) {
                return false;
            }

            final String postfix = fromUrl.substring(fromUrlLength - hostPostfixLength);
            return hostPostfix.equalsIgnoreCase(postfix);
        }
    }

    /**
     * Any host. Matches each and every host.
     */
    private static class AnyHost extends WhitelistEntryHost {
        private static final AnyHost INSTANCE = new AnyHost();

        private AnyHost() {
            super(true, Character.toString(WILDCARD_CHAR), null);
        }

        @Override
        public boolean matches(final URL url) {
            return true;
        }
    }

    /**
     * Invalid host.
     */
    private static class InvalidHost extends WhitelistEntryHost {
        private InvalidHost(final String error) {
            super(false, null, error);
        }

        @Override
        public boolean matches(final URL url) {
            return false;
        }
    }
}
