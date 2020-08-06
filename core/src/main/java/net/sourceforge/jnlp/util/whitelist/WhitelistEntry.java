package net.sourceforge.jnlp.util.whitelist;

/**
 * ...
 */
public class WhitelistEntry {
    private final String whitelistEntry;
    private final String validatedWhitelistEntry;
    private final String errorMessage;

    static WhitelistEntry validWhitelistEntry(final String wlEntry, final String validatedEntry) {
        return new WhitelistEntry(wlEntry, validatedEntry, null);
    }

    static WhitelistEntry invalidWhitelistEntry(final String wlEntry, final String errorMessage) {
        return new WhitelistEntry(wlEntry, null, errorMessage);
    }

    private WhitelistEntry(final String whitelistEntry, final String validatedWhitelistEntry, final String errorMessage) {
        this.whitelistEntry = whitelistEntry;
        this.validatedWhitelistEntry = validatedWhitelistEntry;
        this.errorMessage = errorMessage;
    }

    public String getWhitelistEntry() {
        return whitelistEntry;
    }

    public String getValidatedWhitelistEntry() {
        return validatedWhitelistEntry;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isValid() {
        return errorMessage == null;
    }
}
