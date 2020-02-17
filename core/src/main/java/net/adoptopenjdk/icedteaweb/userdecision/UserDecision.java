package net.adoptopenjdk.icedteaweb.userdecision;

public class UserDecision<T extends Enum<T>> {
    private Key key;
    private T value;

    public enum Key {
        CREATE_DESKTOP_SHORTCUT,
        CREATE_MENU_SHORTCUT,
        ESTABLISH_NETWORK_CONNECTION,
        READ_FILE,
        WRITE_FILE,
        READ_WRITE_FILE,
        READ_CLIPBOARD,
        WRITE_CLIPBOARD,
        USE_PRINTER
    }

    private UserDecision(final Key key, final T value) {
        this.key = key;
        this.value = value;
    }

    public static <T extends Enum<T>> UserDecision<T> of(final Key key, final T value) {
        return new UserDecision<T>(key, value);
    }

    public Key getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
