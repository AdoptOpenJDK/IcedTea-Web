package net.sourceforge.jnlp.util.logging;

/**
 * Log levels for ITW internal logging.
 */
public enum OutputControllerLevel {

    ERROR,
    WARN,
    INFO,
    DEBUG,

    ;

    private final String displayName = (name() + "   ").substring(0,5);

    public boolean printToOutStream() {
        return this == INFO || this == DEBUG || this == WARN;
    }

    public boolean printToErrStream() {
        return this == ERROR || this == WARN;
    }

    public String display() {
        return displayName;
    }
}
