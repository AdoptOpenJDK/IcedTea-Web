package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URL;

/**
 * The port information of a parsed whitelist entry.
 */
abstract class WhitelistEntryPort extends WhitelistEntryPart {

    private static final String WILDCARD = "*";

    static WhitelistEntryPort parse(final String candidate, final WhitelistEntryProtocol protocol) {
        if (StringUtils.isBlank(candidate)) {
            return new ExactPort(protocol.getDefaultPort());
        }
        if (WILDCARD.equalsIgnoreCase(candidate)) {
            return Wildcard.INSTANCE;
        }

        try {
            final int port = Integer.parseInt(candidate);
            return new ExactPort(port);
        } catch (NumberFormatException ignored) {
            return InvalidPort.INSTANCE;
        }
    }

    private WhitelistEntryPort(final boolean valid, final String effective, final String error) {
        super(valid, effective, error);
    }

    /**
     * Exact port given.
     */
    private static class ExactPort extends WhitelistEntryPort {
        private final int port;

        private ExactPort(final int port) {
            super(true, Integer.toString(port), null);
            this.port = port;
        }

        @Override
        public boolean matches(final URL url) {
            return port == getPort(url);
        }

        private int getPort(final URL url) {
            final int port = url.getPort();
            if (port > 0) {
                return port;
            }
            return url.getDefaultPort();
        }
    }

    /**
     * Wildcard. Matches any port.
     */
    private static class Wildcard extends WhitelistEntryPort {
        private static final Wildcard INSTANCE = new Wildcard();

        private Wildcard() {
            super(true, WILDCARD, null);
        }

        @Override
        public boolean matches(final URL url) {
            return true;
        }
    }

    /**
     * Invalid port.
     */
    private static class InvalidPort extends WhitelistEntryPort {
        private static final InvalidPort INSTANCE = new InvalidPort();

        private InvalidPort() {
            super(false, null, "Invalid port. Must be a number or '*'");
        }

        @Override
        public boolean matches(final URL url) {
            return false;
        }
    }
}
