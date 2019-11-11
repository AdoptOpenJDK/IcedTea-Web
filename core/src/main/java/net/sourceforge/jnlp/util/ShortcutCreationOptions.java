package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public enum ShortcutCreationOptions {

    CREATE_NEVER(ShortcutDesc.CREATE_NEVER),
    CREATE_ALWAYS(ShortcutDesc.CREATE_ALWAYS),
    CREATE_ASK_USER(ShortcutDesc.CREATE_ASK_USER),
    CREATE_ASK_USER_IF_HINTED(ShortcutDesc.CREATE_ASK_USER_IF_HINTED),
    CREATE_ALWAYS_IF_HINTED(ShortcutDesc.CREATE_ALWAYS_IF_HINTED);


    private final String configName;

    ShortcutCreationOptions(final String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public static Optional<ShortcutCreationOptions> forConfigName(final String configName) {
        return Stream.of(values())
                .filter(v -> Objects.equals(configName, v.getConfigName()))
                .findFirst();
    }
}
