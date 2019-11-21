package net.adoptopenjdk.icedteaweb.ie;

import java.util.prefs.Preferences;

public enum RegistryScope {
    LOCAL_MACHINE(0x80000002),
    CURRENT_USER(0x80000001);

    private final int rawValue;

    RegistryScope(final int rawValue) {
        this.rawValue = rawValue;
    }

    public Preferences getPreferences() {
        if (this == LOCAL_MACHINE) {
            return Preferences.systemRoot();
        }
        return Preferences.userRoot();
    }

    public int getRawValue() {
        return rawValue;
    }
}
