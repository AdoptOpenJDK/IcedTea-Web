package net.sourceforge.jnlp.proxy;

import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.util.stream.Stream;

public enum ProxyProviderTypes {

    NONE(0),
    MANUAL_HOSTS(1),
    MANUAL_PAC_URL(2),
    FIREFOX(3),
    WINDOWS(4);

    private final int configValue;

    ProxyProviderTypes(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public void checkSupported() {
        if(this == WINDOWS && !OsUtil.isWindows()) {
            throw new IllegalStateException("Windows proxy is only supported on windows os");
        }
        if(this == FIREFOX && OsUtil.isMac()) {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            throw new IllegalStateException("Firefox proxy is not supported on mac os");
        }
    }

    public boolean isSupported() {
        if(this == WINDOWS && !OsUtil.isWindows()) {
            return false;
        }
        if(this == FIREFOX && OsUtil.isMac()) {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            return false;
        }
        return true;
    }

    public static ProxyProviderTypes getForConfigValue(final int value) {
        return Stream.of(ProxyProviderTypes.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No proxy type defined with config value " + value));
    }
}
