package net.adoptopenjdk.icedteaweb.proxy;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

public enum ProxyProviderType {

    OPERATION_SYSTEM;

    private static final Logger LOG = LoggerFactory.getLogger(ProxyProviderType.class);


    private final int configValue = 3;


    public int getConfigValue() {
        return configValue;
    }

    public ProxyProvider createProvider(final DeploymentConfiguration config, ExecutorService ioExecutor) throws Exception {
        throw new RuntimeException("this method must be overridden by every instance of the enum");
    }

    public void checkSupported() {
        // subclasses implement specific behavior
    }

    public boolean isSupported() {
        // subclasses implement specific behavior
        return true;
    }

    public static ProxyProviderType getForConfigValue(final int value) {
        return Stream.of(ProxyProviderType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .filter(ProxyProviderType::isSupported)
                .orElse(null);
    }
}
