package net.adoptopenjdk.icedteaweb.jnlp.version;

/**
 * Enum of all separators as defined for {@link VersionId}s and {@link VersionString}s as defined
 * by JSR-56 Specification, Appendix A.
 */
public enum VersionSeparator {
    DOT("."),
    MINUS("-"),
    UNDERSCORE("_"),
    SPACE(" ");

    private String symbol;

    VersionSeparator(final String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
