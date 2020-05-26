package net.adoptopenjdk.icedteaweb.proxy;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.proxy.direct.DirectProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.firefox.FirefoxProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.mac.MacProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.manual.ManualConfigBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.manual.ManualPacFileProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.windows.WindowsProxyProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

public enum ProxyProviderType {

    NONE(0) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) {
            LOG.debug("Direct proxy created");
            return DirectProxyProvider.getInstance();
        }
    },

    MANUAL_HOSTS(1) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) {
            LOG.debug("Manual proxy created");
            return new ManualConfigBasedProxyProvider(config);
        }
    },

    MANUAL_PAC_URL(2) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {
            LOG.debug("Pac based proxy created");
            return new ManualPacFileProxyProvider(config);
        }
    },

    FIREFOX(3) {
        @Override
        public void checkSupported() {
            if (!isSupported()) {
                // Not implemented: https://support.mozilla.org/en-US/questions/1152265
                throw new IllegalStateException("Firefox proxy is not supported on " + OperationSystem.getLocalSystem());
            }
        }

        @Override
        public boolean isSupported() {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            return !OperationSystem.getLocalSystem().isMac();
        }

        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {
            LOG.debug("Firefox based proxy created");
            return new FirefoxProxyProvider(config, ioExecutor);
        }
    },

    OPERATION_SYSTEM(4) {
        @Override
        public void checkSupported() {
            if (!isSupported()) {
                throw new IllegalStateException("System proxy is not supported for " + OperationSystem.getLocalSystem());
            }
        }

        @Override
        public boolean isSupported() {
            final OperationSystem localSystem = OperationSystem.getLocalSystem();
            return localSystem.isWindows() || localSystem.isMac() || localSystem.isLinux();
        }

        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {
            final OperationSystem localSystem = OperationSystem.getLocalSystem();
            if (localSystem.isWindows()) {
                LOG.debug("Windows based proxy created");
                return new WindowsProxyProvider(config, ioExecutor);
            } else if (localSystem.isMac()) {
                LOG.debug("Mac based proxy created");
                return new MacProxyProvider(config, ioExecutor);
            } else if (localSystem.isLinux()) {
                LOG.debug("Linux based proxy created");
                return new LinuxProxyProvider(config, ioExecutor);
            }
            throw new IllegalStateException("System proxy is not supported for " + OperationSystem.getLocalSystem());
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(ProxyProviderType.class);

    private final int configValue;

    ProxyProviderType(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public ProxyProvider createProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {
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
                .orElse(NONE);
    }
}
