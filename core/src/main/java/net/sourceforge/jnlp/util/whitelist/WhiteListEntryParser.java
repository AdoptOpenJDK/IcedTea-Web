package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Whitelist entry parser.
 * <p>
 * Its main goal is to extract protocol, host and port from the raw input string.
 */
class WhiteListEntryParser {

    private static final String PROTOCOL = "(\\w+)";
    private static final String PORT = "([0-9*]+)";
    private static final String HOST = "([^:/?]+)";

    private static final String OPTIONAL_PROTOCOL = "(?:" + PROTOCOL + "://)?";
    private static final String OPTIONAL_PORT = "(?::" + PORT + ")?";
    private static final String OPTIONAL_PATH_AND_QUERY = "(?:[/?].*)?";

    private static final Pattern ENTRY_PATTERN = Pattern.compile("^" + OPTIONAL_PROTOCOL + HOST + OPTIONAL_PORT + OPTIONAL_PATH_AND_QUERY + "$");

    static WhitelistEntry parse(final String rawEntry) {
        if (StringUtils.isBlank(rawEntry)) {
            return new UnparsableWhitelistEntry(rawEntry);
        }

        final Matcher matcher = ENTRY_PATTERN.matcher(rawEntry);

        if (!matcher.matches()) {
            return new UnparsableWhitelistEntry(rawEntry);
        }

        final String protocolCandidate = matcher.group(1);
        final String hostCandidate = matcher.group(2);
        final String portCandidate = matcher.group(3);

        final WhitelistEntryProtocol protocol = WhitelistEntryProtocol.get(protocolCandidate);
        final WhitelistEntryHost host = WhitelistEntryHost.get(hostCandidate);
        final WhitelistEntryPort port = WhitelistEntryPort.get(portCandidate, protocol);

        return new ParsedWhitelistEntry(rawEntry, protocol, host, port);
    }
}
