package net.sourceforge.jnlp.signing;

enum ApplicationSigningState {
    FULL, // all jars are signed
    PARTIAL, // some jars are signed
    NONE // no jars are signed
}
