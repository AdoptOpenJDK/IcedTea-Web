package net.adoptopenjdk.icedteaweb.ie;

import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_ENABLED_VAL;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_REGISTRY_KEY;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_OVERRIDE_VAL;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    private static String getProxyString() throws Exception {
        return WinRegistry.readStringFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_REGISTRY_VAL);
    }

    private static String getOverrideHosts() throws Exception {
        return WinRegistry.readStringFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_OVERRIDE_VAL);
    }

    private static String getAutoConfigUrl() throws Exception {
        return WinRegistry.readStringFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_SERVER_OVERRIDE_VAL);
    }

    private static boolean isProxyEnabled() throws Exception {
        return WinRegistry.readBooleanFromRegistry(
                RegistryScope.CURRENT_USER,
                PROXY_REGISTRY_KEY,
                PROXY_ENABLED_VAL);
    }


    public static void main(String[] args) throws Exception {

        final String proxyServer = getProxyString();
        final String overrideHosts = getOverrideHosts();
        final boolean proxyEnabled = isProxyEnabled();
        final String autoConfigUrl = getAutoConfigUrl();

        System.out.println("Windows Proxy Server setting: '" + proxyServer + "'");
        System.out.println("Windows Proxy Server override hosts setting: '" + overrideHosts + "'");
        System.out.println("Windows Proxy enabled: '" + proxyEnabled + "'");
        System.out.println("Windows Proxy Auto Config Url: '" + autoConfigUrl + "'");
    }
}
