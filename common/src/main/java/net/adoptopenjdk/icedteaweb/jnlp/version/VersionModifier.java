package net.adoptopenjdk.icedteaweb.jnlp.version;

/**
 * Enum of all modifiers as defined for {@link VersionId}s as defined by JSR-56 Specification, Appendix A.
 */
public enum VersionModifier {
    PLUS("+"),
    ASTERISK("*"),
    AMPERSAND("&");

    private String symbol;

    VersionModifier(final String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
