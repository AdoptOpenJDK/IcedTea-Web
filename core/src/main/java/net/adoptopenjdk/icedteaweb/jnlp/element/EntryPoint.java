package net.adoptopenjdk.icedteaweb.jnlp.element;

/**
 * Provides access to the main entry point where to start a program's execution.
 * <p/>
 * Name inspired by https://en.wikipedia.org/wiki/Entry_point
 */
public interface EntryPoint {
    /**
     * For Java applications this method returns the name of the class containing the public static
     * void main(String[]) method. The name and/or meaning may vary as is appropriate for other types
     * of applications.
     * <p/>
     * For Java this attribute can be omitted if the main class can be found from the Main-Class manifest entry
     * in the main JAR file.
     *
     * @return the fully qualified name of the main entry point where to start a program's execution
     */
    String getMainClass();
}
