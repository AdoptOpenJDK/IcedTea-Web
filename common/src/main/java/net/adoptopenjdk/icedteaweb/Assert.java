package net.adoptopenjdk.icedteaweb;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

public class Assert {

    private static final String NOT_NULL_MSG_FORMAT = "Argument '%s' may not be null";
    private static final String NOT_EMPTY_MSG_FORMAT = "Argument '%s' may not be empty";

    private Assert() {
        // intentionally private and blank
    }

    /**
     * Checks that the specified {@code value} is null and throws {@link java.lang.NullPointerException} with a customized error message if it is.
     * @param value the value to be checked.
     * @param argumentName the name of the argument to be used in the error message.
     * @param <T> type of the value
     * @return the {@code value}.
     */
    public static <T> T requireNonNull(final T value, final String argumentName) {
        if (argumentName == null) {
            throw new NullPointerException(String.format(NOT_NULL_MSG_FORMAT, "argumentName"));
        }
        if (value == null) {
            throw new NullPointerException(String.format(NOT_NULL_MSG_FORMAT, argumentName));
        }
        return value;
    }

    /**
     * Checks that the specified {@code str} {@code blank}, throws {@link IllegalArgumentException} with a customized error message if it is.
     *
     * @param str          the value to be checked.
     * @param argumentName the name of the argument to be used in the error message.
     * @return the {@code str}.
     * @throws java.lang.NullPointerException     if {@code str} is null.
     * @throws java.lang.IllegalArgumentException if {@code str} is blank.
     * @see #requireNonNull(Object, String)
     * @see StringUtils#isBlank(String)
     */

    public static String requireNonBlank(final String str, final String argumentName) {
        requireNonNull(str, argumentName);
        if (isBlank(str)) {
            throw new IllegalArgumentException(String.format(NOT_EMPTY_MSG_FORMAT, argumentName));
        }
        return str;
    }

}
