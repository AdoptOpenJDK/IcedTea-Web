package net.adoptopenjdk.icedteaweb.logging;

/**
 * Simple logger interface based on SLF4J.
 *
 * @see <a href="https://www.slf4j.org/">https://www.slf4j.org/</a>
 */
public interface Logger {

    /**
     * Log a message at the {@code DEBUG} level.
     *
     * @param msg the message to be logged
     */
    void debug(String msg);

    /**
     * Log a message at the {@code DEBUG} level.
     * Replace {@code {}} with the given arguments
     *
     * @param msg       the message to be logged
     * @param arguments a list of 3 or more arguments for replacement
     */
    void debug(String msg, Object... arguments);

    /**
     * Log a message and an exception at the {@code DEBUG} level.
     *
     * @param msg the message to be logged
     * @param t   the exception to be logged
     */
    void debug(String msg, Throwable t);

    /**
     * Log a message at the {@code INFO} level.
     *
     * @param msg the message to be logged
     */
    void info(String msg);

    /**
     * Log a message at the {@code INFO} level.
     * Replace {@code {}} with the given arguments
     *
     * @param msg       the message to be logged
     * @param arguments a list of 3 or more arguments for replacement
     */
    void info(String msg, Object... arguments);

    /**
     * Log a message and an exception at the {@code INFO} level.
     *
     * @param msg the message to be logged
     * @param t   the exception to be logged
     */
    void info(String msg, Throwable t);

    /**
     * Log a message at the {@code WARN} level.
     *
     * @param msg the message to be logged
     */
    void warn(String msg);

    /**
     * Log a message at the {@code WARN} level.
     * Replace {@code {}} with the given arguments
     *
     * @param msg       the message to be logged
     * @param arguments a list of 3 or more arguments for replacement
     */
    void warn(String msg, Object... arguments);

    /**
     * Log a message and an exception at the {@code WARN} level.
     *
     * @param msg the message to be logged
     * @param t   the exception to be logged
     */
    void warn(String msg, Throwable t);

    /**
     * Log a message at the {@code ERROR} level.
     *
     * @param msg the message to be logged
     */
    void error(String msg);

    /**
     * Log a message at the {@code ERROR} level.
     * Replace {@code {}} with the given arguments
     *
     * @param msg       the message to be logged
     * @param arguments a list of 3 or more arguments for replacement
     */
    void error(String msg, Object... arguments);

    /**
     * Log a message and an exception at the {@code ERROR} level.
     *
     * @param msg the message to be logged
     * @param t   the exception to be logged
     */
    void error(String msg, Throwable t);

}
