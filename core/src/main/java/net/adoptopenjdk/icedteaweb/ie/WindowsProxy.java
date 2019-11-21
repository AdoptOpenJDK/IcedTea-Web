package net.adoptopenjdk.icedteaweb.ie;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;

import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_REGISTRY_KEY;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_OVERRIDE_VAL;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTracker.class);

    private static String readProxyStringFromRegistry() throws Exception {
        return WinRegistry.readStringFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_REGISTRY_VAL);
    }

    private static String readOverrideHostsFromRegistry() throws Exception {
        return WinRegistry.readStringFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_OVERRIDE_VAL);
    }


    public static void main(String[] args) throws Exception {

        final String proxyServer = readProxyStringFromRegistry();
        final String overrideHosts = readOverrideHostsFromRegistry();

        LOG.info("Windows Proxy Server setting: '" + proxyServer + "'");
        LOG.info("Windows Proxy Server override hosts setting: '" + overrideHosts + "'");
    }
}
