package net.sourceforge.jnlp.proxy.browser;

import java.util.stream.Stream;

public enum FirefoxProxyType {

    FF_PROXY_TYPE_NONE(0),
    FF_PROXY_TYPE_MANUAL(1),
    FF_PROXY_TYPE_PAC(2),
    FF_PROXY_TYPE_AUTO(4),
    FF_PROXY_TYPE_SYSTEM(5);

    private final int configValue;

    FirefoxProxyType(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public static FirefoxProxyType getForConfigValue(final int value) {
        return Stream.of(FirefoxProxyType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .orElse(FF_PROXY_TYPE_NONE);
    }
}
