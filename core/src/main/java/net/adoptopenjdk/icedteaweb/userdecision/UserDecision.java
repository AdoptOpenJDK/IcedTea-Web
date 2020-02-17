package net.adoptopenjdk.icedteaweb.userdecision;

import net.sourceforge.jnlp.security.AccessType;

import java.util.stream.Stream;

public class UserDecision<T extends Enum<T>> {
    private Key key;
    private T value;

    public enum Key {
        CREATE_DESKTOP_SHORTCUT(null),
        CREATE_MENU_SHORTCUT(null),
        ESTABLISH_NETWORK_CONNECTION(AccessType.NETWORK),
        READ_FILE(AccessType.READ_FILE),
        WRITE_FILE(AccessType.WRITE_FILE),
        READ_WRITE_FILE(AccessType.READ_WRITE_FILE),
        READ_CLIPBOARD(AccessType.CLIPBOARD_READ),
        WRITE_CLIPBOARD(AccessType.CLIPBOARD_WRITE),
        USE_PRINTER(AccessType.PRINTER);

        private final AccessType accessType;

        Key(AccessType accessType) {
            this.accessType = accessType;
        }

        public static Key valueOf(AccessType accessType) {
            return Stream.of(Key.values())
                    .filter(k -> k.accessType == accessType)
                    .findFirst()
                    .orElseThrow(()->new IllegalArgumentException("Could not find key for access type " + accessType));

        }
    }

    private UserDecision(final Key key, final T value) {
        this.key = key;
        this.value = value;
    }

    public static <T extends Enum<T>> UserDecision<T> of(final Key key, final T value) {
        return new UserDecision<T>(key, value);
    }

    public static <T extends Enum<T>> UserDecision<T> of(final AccessType accessType, final T value) {
        return new UserDecision<>(Key.valueOf(accessType), value);
    }

    public Key getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
