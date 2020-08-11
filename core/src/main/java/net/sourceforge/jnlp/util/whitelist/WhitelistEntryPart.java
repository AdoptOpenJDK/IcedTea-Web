package net.sourceforge.jnlp.util.whitelist;

import java.net.URL;

/**
 * A part of a parsed whitelist entry.
 */
abstract class WhitelistEntryPart {

    private final boolean valid;
    private final String effective;
    private final String error;

    protected WhitelistEntryPart(final boolean valid, final String effective, final String error) {
        this.valid = valid;
        this.effective = effective;
        this.error = error;
    }

    public abstract boolean matches(URL url);

    boolean isValid() {
        return valid;
    }

    String effective() {
        return effective;
    }

    String error() {
        return error;
    }
}
