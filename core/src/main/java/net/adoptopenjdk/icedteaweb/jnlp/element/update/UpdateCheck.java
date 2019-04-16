package net.adoptopenjdk.icedteaweb.jnlp.element.update;

/**
 * The check attribute indicates the preference for when the JNLP Client should check for updates, and can have
 * one of the three values: "always", "timeout", and "background".
 * <p/>
 * @implSpec See <b>JSR-56, Section 3.6 Application Update</b>
 * for a detailed specification of this class.
 */
public enum UpdateCheck {
    /**
     * A value of "always" means to always check for updates before launching the application.
     */
    ALWAYS,

    /**
     * A value of "timeout" (default) means to check for updates until timeout before launching the
     * application. If the update check is not completed before the timeout, the application is
     * launched, and the update check will continue in the background.
     */
    TIMEOUT,

    /**
     * A value of "background" means to launch the application while checking for updates in the background.
     */
    BACKGROUND
}
