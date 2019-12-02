package net.sourceforge.jnlp.proxy.browser;

import java.io.IOException;
import java.util.Map;

/**
 * ...
 */
interface PreferencesParser {
    void parse() throws IOException;
    Map<String, String> getPreferences();
}
