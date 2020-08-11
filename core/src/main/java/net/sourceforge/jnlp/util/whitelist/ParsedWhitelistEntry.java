package net.sourceforge.jnlp.util.whitelist;

import java.net.URL;

/**
 * An entry where protocol, host and port were parsed.
 */
class ParsedWhitelistEntry implements WhitelistEntry {

    private final String rawWhitelistEntry;
    private final WhitelistEntryProtocol protocol;
    private final WhitelistEntryHost host;
    private final WhitelistEntryPort port;

    private final boolean valid;

    ParsedWhitelistEntry(final String raw, final WhitelistEntryProtocol protocol, final WhitelistEntryHost host, final WhitelistEntryPort port) {
        this.rawWhitelistEntry = raw;
        this.protocol = protocol;
        this.host = host;
        this.port = port;

        this.valid = protocol.isValid() && host.isValid() && port.isValid();
    }

    @Override
    public String getRawWhitelistEntry() {
        return rawWhitelistEntry;
    }

    @Override
    public String getEffectiveWhitelistEntry() {
        if (!valid) {
            return "";
        }
        return protocol.effective() + "://" + host.effective() + ":" + port.effective();
    }

    @Override
    public String getErrorMessage() {
        if (valid) {
            return "";
        }
        if (!protocol.isValid()) {
            return protocol.error();
        }
        if (!host.isValid()) {
            return host.error();
        }
        return port.error();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean matches(final URL url) {
        if (!isValid() || url == null) {
            // ignore invalid url or whitelist entries
            return false;
        } else {
            return protocol.matches(url) && host.matches(url) && port.matches(url);
        }
    }
}
