// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.security.AccessType;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class UserDecision<T extends Enum<T>> {
    private Key key;
    private T value;

    public enum Key {
        CREATE_DESKTOP_SHORTCUT,
        CREATE_MENU_SHORTCUT,
        ESTABLISH_NETWORK_CONNECTION(AccessType.NETWORK),
        READ_FILE(AccessType.READ_FILE),
        WRITE_FILE(AccessType.WRITE_FILE),
        READ_WRITE_FILE(AccessType.READ_WRITE_FILE),
        READ_CLIPBOARD(AccessType.CLIPBOARD_READ),
        WRITE_CLIPBOARD(AccessType.CLIPBOARD_WRITE),
        USE_PRINTER(AccessType.PRINTER),
        RUN_UNSIGNED_APPLICATION,
        RUN_PARTIALLY_APPLICATION,
        RUN_MISSING_PERMISSIONS_APPLICATION,
        RUN_MISSING_ALAC_APPLICATION,
        RUN_MATCHING_ALAC_APPLICATION,
        ;

        private final AccessType accessType;

        Key() {
            this.accessType = null;
        }

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
        this.key = Assert.requireNonNull(key, "key");
        this.value = Assert.requireNonNull(value, "value");
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDecision)) return false;
        final UserDecision<?> that = (UserDecision<?>) o;
        return key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserDecision.class.getSimpleName() + "[", "]")
                .add("key=" + key)
                .add("value=" + value)
                .toString();
    }
}
