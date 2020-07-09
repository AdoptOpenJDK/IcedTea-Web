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

    public boolean printToOutStream() {
        return this == INFO || this == DEBUG || this == WARN;
    }

    public boolean printToErrStream() {
        return this == ERROR || this == WARN;
    }
}
