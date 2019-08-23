package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.config.validators.ValueValidator;

import java.util.List;

/**
 * Provider for Default settings.
 * <p>
 * This can be used by extensions to register their own default settings.
 */
public interface DefaultsProvider {

    /**
     * Default settings from extensions of ITW
     *
     * @return default settings
     * @implNote use {@link Setting#createDefault(String, String, ValueValidator)} to create default settings.
     */
    List<Setting<String>> getDefaults();
}
