package net.adoptopenjdk.icedteaweb;

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
     * @see #isBlank(String)
     */

    public static String requireNonBlank(final String str, final String argumentName) {
        requireNonNull(str, argumentName);
        if (isBlank(str)) {
            throw new IllegalArgumentException(String.format(NOT_EMPTY_MSG_FORMAT, argumentName));
        }
        return str;
    }

    /**
     * <p>Determines whether a given string is <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>false</code>.</p>
     *
     * @param str The string to test.
     * @return <code>true</code> if the string is <code>null</code>, or
     * blank.
     */
    public static boolean isBlank(final String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        for (final char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
}
