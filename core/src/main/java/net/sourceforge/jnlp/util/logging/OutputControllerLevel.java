package net.sourceforge.jnlp.util.logging;

/**
 * Log levels for ITW internal logging.
 */
public enum OutputControllerLevel {

/*
    debug   ->  MESSAGE_DEBUG
    info    ->  MESSAGE_ALL
    warn    ->  WARNING_ALL
    error   ->  ERROR_ALL

                    color   stdout   stderr   verbose
    -------------------------------------------------
    MESSAGE_ALL     black       x
    MESSAGE_DEBUG   black       x               x
    WARNING_ALL     yellow      x       x
    WARNING_DEBUG   yellow      x       x       x
    ERROR_ALL       red                 x
    ERROR_DEBUG     red                 x       x

 */

    MESSAGE_ALL,
    MESSAGE_DEBUG,
    WARNING_ALL,
    @Deprecated WARNING_DEBUG,
    ERROR_ALL,
    @Deprecated ERROR_DEBUG,

    ;

    public boolean printToOutStream() {
        return this == MESSAGE_ALL
                || this == MESSAGE_DEBUG
                || this == WARNING_ALL
                || this == WARNING_DEBUG;
    }

    public boolean printToErrStream() {
        return this == ERROR_ALL
                || this == ERROR_DEBUG
                || this == WARNING_ALL
                || this == WARNING_DEBUG;
    }

    public boolean isWarning() {
        return this == WARNING_ALL
                || this == WARNING_DEBUG;
    }

    public boolean isDebug() {
        return this == ERROR_DEBUG
                || this == MESSAGE_DEBUG
                || this == WARNING_DEBUG;
    }

    public boolean isInfo() {
        return this == ERROR_ALL
                || this == WARNING_ALL
                || this == MESSAGE_ALL;
    }
    
    public boolean isCrucial() {
        return this == ERROR_ALL
                || this == WARNING_ALL;
    }
}
