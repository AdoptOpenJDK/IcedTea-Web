package net.sourceforge.jnlp.proxy;

import java.util.stream.Stream;

public enum ProxyType {

    PROXY_TYPE_UNKNOWN(-1),
    PROXY_TYPE_NONE(0),
    PROXY_TYPE_MANUAL(1),
    PROXY_TYPE_AUTO(2),
    PROXY_TYPE_BROWSER(3),
    PROXY_TYPE_SYSTEM(4),
    ;

    private final int configValue;

    ProxyType(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public static ProxyType getForConfigValue(final int value) {
        final ProxyType result = Stream.of(ProxyType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .orElse(PROXY_TYPE_UNKNOWN);

        if (result == PROXY_TYPE_SYSTEM) {
            return PROXY_TYPE_UNKNOWN; // fallback until system is supported
        }

        return result;
    }
}
