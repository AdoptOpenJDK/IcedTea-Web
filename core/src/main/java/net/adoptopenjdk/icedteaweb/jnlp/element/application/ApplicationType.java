package net.adoptopenjdk.icedteaweb.jnlp.element.application;

/**
 * The type of application supported by the JNLP Client.
 * <p/>
 * If given the type attribute value is not supported by the JNLP Client, the launch should be aborted.
 * If a JNLP Client supports other types of applications (such as "JavaFX", or "JRuby"), The meaning and/or
 * use of the other application-desc attributes (main-class and progress-class) and sub-elements (argument
 * and param) may vary as is appropriate for that type of application.
 * <p/>
 *
 * @implSpec See <b>JSR-56, Section 3.7.1 Application Descriptor for an Application</b>
 * for a detailed specification of this class.
 */
public enum ApplicationType {
    /**
     * A value (default) indicates the application is a Java application.
     */
    JAVA,
    /**
     * A value indicates the application is a JavaFX application.
     */
    JAVAFX
}
