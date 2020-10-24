package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.net.URL;

/**
 * The protocol information of a parsed whitelist entry.
 */
abstract class WhitelistEntryProtocol extends WhitelistEntryPart {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    static WhitelistEntryProtocol parse(final String candidate) {
        if (StringUtils.isBlank(candidate) || HTTPS.equalsIgnoreCase(candidate)) {
            return Https.INSTANCE;
        }
        if (HTTP.equalsIgnoreCase(candidate)) {
            return Http.INSTANCE;
        }
        return InvalidProtocol.INSTANCE;
    }

    private WhitelistEntryProtocol(final boolean valid, final String effective, final String error) {
        super(valid, effective, error);
    }

    abstract int getDefaultPort();

    @Override
    public boolean matches(final URL url) {
        return effective().equalsIgnoreCase(url.getProtocol());
    }

    /**
     * HTTP.
     */
    private static class Http extends WhitelistEntryProtocol {
        private static final Http INSTANCE = new Http();

        private Http() {
            super(true, HTTP, null);
        }

        @Override
        int getDefaultPort() {
            return 80;
        }
    }

    /**
     * HTTPS.
     */
    private static class Https extends WhitelistEntryProtocol {
        private static final Https INSTANCE = new Https();

        private Https() {
            super(true, HTTPS, null);
        }

        @Override
        int getDefaultPort() {
            return 443;
        }
    }

    /**
     * Invalid protocol.
     */
    private static class InvalidProtocol extends WhitelistEntryProtocol {
        private static final InvalidProtocol INSTANCE = new InvalidProtocol();

        private InvalidProtocol() {
            super(false, null, Translator.R("WleInvalidProtocol"));
        }

        @Override
        public boolean matches(final URL url) {
            return false;
        }

        @Override
        int getDefaultPort() {
            return -2;
        }
    }
}
