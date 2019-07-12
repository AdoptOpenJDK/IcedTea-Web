package net.adoptopenjdk.icedteaweb;

public interface IcedTeaWebConstants {

    /**
     * Used as default message when logging exception.
     * Should be removed in future since each logger call should have a message that describes the error.
     */
    @Deprecated
    String DEFAULT_ERROR_MESSAGE = "ERROR";

    String DOUBLE_QUOTE = "\"";

    String JAVAWS = "javaws";
    String JAVAWS_JAR = "javaws.jar";
}
