package net.sourceforge.jnlp.util.whitelist;

import java.net.URL;

/**
 * A single entry in the URL whitelist.
 */
public interface WhitelistEntry {

    static WhitelistEntry parse(String rawEntry) {
        return WhiteListEntryParser.parse(rawEntry);
    }

    boolean matches(URL url);

    boolean isValid();

    String getRawWhitelistEntry();

    String getEffectiveWhitelistEntry();

    String getErrorMessage();
}
