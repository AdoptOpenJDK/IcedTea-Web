package net.sourceforge.jnlp.util.whitelist;

import java.net.URL;

/**
 * An entry where it was not possible to parse protocol, host and port.
 */
class UnparsableWhitelistEntry implements WhitelistEntry {
    private final String rawWhitelistEntry;

    UnparsableWhitelistEntry(final String rawWhitelistEntry) {
        this.rawWhitelistEntry = rawWhitelistEntry;
    }

    @Override
    public boolean matches(final URL url) {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getRawWhitelistEntry() {
        return rawWhitelistEntry;
    }

    @Override
    public String getEffectiveWhitelistEntry() {
        return "";
    }

    @Override
    public String getErrorMessage() {
        return "Invalid entry: must be of the form [<protocol>://]host[:<port>]";
    }
}
