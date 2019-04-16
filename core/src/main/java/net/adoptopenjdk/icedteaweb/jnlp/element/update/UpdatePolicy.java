package net.adoptopenjdk.icedteaweb.jnlp.element.update;

/**
 * The policy attribute indicates the preference for how the JNLP Client should handle an application
 * update when it is known an update is available before the application is launched, and can have
 * one of the following three values: "always", "prompt-update", and "prompt-run".
 * <p/>
 * see JSR-56, Chapter 3.6 Application Update
 */
public enum UpdatePolicy {
    /**
     * A value of "always" (default) means to always download updates without any prompt.
     */
    ALWAYS,

    /**
     * A value of "prompt-update" means to ask the user if he/she wants to download and run the
     * updated version, or launch the cached version.
     */
    PROMPT_UPDATE,

    /**
     * A value of "prompt-run" means to ask the user if he/she wants to download and run the updated
     * version, or cancel and abort running the application.
     */
    PROMPT_RUN,
}
