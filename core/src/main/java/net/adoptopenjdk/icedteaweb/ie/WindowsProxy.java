package net.adoptopenjdk.icedteaweb.ie;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.HKEY_CURRENT_USER;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_REGISTRY_KEY;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_OVERRIDE_VAL;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTracker.class);

    private final WinRegistry winRegistry;

    public WindowsProxy(final WinRegistry winRegistry) {
        this.winRegistry = winRegistry;
    }

    private String readProxyStringFromRegistry() throws InvocationTargetException, IllegalAccessException {
        return winRegistry.readString(
                HKEY_CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_REGISTRY_VAL, 0);
    }

    private String readOverrideHostsFromRegistry() throws InvocationTargetException, IllegalAccessException {
        return winRegistry.readString(
                HKEY_CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_OVERRIDE_VAL, 0);
    }

    public void getProxySettingsFromWindowsRegistry(final URI uri) throws Exception {
        final WinRegistry winRegistry = new WinRegistry();

        final String proxyServer = readProxyStringFromRegistry();
        final String overrideHosts = readOverrideHostsFromRegistry();

        LOG.info("Windows Proxy Server setting: '" + proxyServer + "'");
        LOG.info("Windows Proxy Server override hosts setting: '" + overrideHosts + "'");
    }
}
