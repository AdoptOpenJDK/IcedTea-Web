package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * The host information of a parsed whitelist entry.
 */
abstract class WhitelistEntryHost extends WhitelistEntryPart {

    private static final char WILDCARD_CHAR = '*';
    private static final char SUBDOMAIN_SEPARATOR = '.';

    private static final Pattern ALPHA_CHAR_PATTERN = Pattern.compile(".*[a-z,A-Z].*");


    static WhitelistEntryHost parse(final String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return InvalidHost.MISSING_HOST;
        }

        final int wildcardIndex = candidate.indexOf(WILDCARD_CHAR);

        if (wildcardIndex < 0) {
            return new ExactHost(candidate);
        }

        if (wildcardIndex > 0) {
            return InvalidHost.MISS_PLACED_WILDCARD;
        }

        final String withoutWildcardPrefix = candidate.substring(1);
        if (withoutWildcardPrefix.isEmpty()) {
            return AnyHost.INSTANCE;
        }
        if (withoutWildcardPrefix.indexOf(WILDCARD_CHAR) >= 0) {
            return InvalidHost.MULTIPLE_WILDCARDS;
        }
        if (withoutWildcardPrefix.indexOf(SUBDOMAIN_SEPARATOR) != 0) {
            return InvalidHost.INVALID_WILDCARD_PREFIX;
        }
        if (!ALPHA_CHAR_PATTERN.matcher(withoutWildcardPrefix).matches()) {
            return InvalidHost.IP_WITH_WILDCARD;
        }

        return new WildcardHost(candidate);
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
        private static final InvalidHost MISSING_HOST = new InvalidHost("Invalid Host: host is missing");
        private static final InvalidHost MISS_PLACED_WILDCARD = new InvalidHost("Invalid Host: '*' can only be the very first character of the host");
        private static final InvalidHost MULTIPLE_WILDCARDS = new InvalidHost("Invalid Host: '*' can only be the very first character of the host");
        private static final InvalidHost IP_WITH_WILDCARD = new InvalidHost("Invalid Host: wildcards are not allowed in IP addresses");
        private static final InvalidHost INVALID_WILDCARD_PREFIX = new InvalidHost("Invalid Host: after '*' must follow a '.'");

        private InvalidHost(final String error) {
            super(false, null, error);
        }

        @Override
        public boolean matches(final URL url) {
            return false;
        }
    }
}
