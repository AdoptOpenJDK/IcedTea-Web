package net.sourceforge.jnlp.proxy.browser;

import java.util.stream.Stream;

public enum BrowserProxyType {

    BROWSER_PROXY_TYPE_NONE(0),
    BROWSER_PROXY_TYPE_MANUAL(1),
    BROWSER_PROXY_TYPE_PAC(2),
    BROWSER_PROXY_TYPE_AUTO(4),
    BROWSER_PROXY_TYPE_SYSTEM(5);

    private final int configValue;

    BrowserProxyType(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public static BrowserProxyType getForConfigValue(final int value) {
        return Stream.of(BrowserProxyType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .orElse(BROWSER_PROXY_TYPE_NONE);
    }
}
