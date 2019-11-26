package net.adoptopenjdk.icedteaweb.proxy.ie;

import static net.adoptopenjdk.icedteaweb.proxy.ie.WinRegistryConstants.PROXY_ENABLED_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WinRegistryConstants.PROXY_REGISTRY_KEY;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WinRegistryConstants.PROXY_SERVER_OVERRIDE_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WinRegistryConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    public static void main(String[] args) throws Exception {

        final RegistryValue proxyServerValue = RegistryQuery.getRegistryValue(PROXY_REGISTRY_KEY, PROXY_SERVER_REGISTRY_VAL).orElse(null);
        if(proxyServerValue != null) {
            System.out.println("Windows Proxy Server setting: '" + proxyServerValue.getValue() + "'");
        } else {
            System.out.println("Windows Proxy Server setting not defined");
        }

        final RegistryValue overrideHostsValue = RegistryQuery.getRegistryValue(PROXY_REGISTRY_KEY, PROXY_SERVER_OVERRIDE_VAL).orElse(null);
        if(overrideHostsValue != null) {
            System.out.println("Windows Proxy Server override hosts setting: '" + overrideHostsValue.getValue() + "'");
        } else {
            System.out.println("Windows Proxy Server override hosts setting not defined");
        }

        final RegistryValue proxyEnabledValue = RegistryQuery.getRegistryValue(PROXY_REGISTRY_KEY, PROXY_ENABLED_VAL).orElse(null);
        if(proxyEnabledValue != null) {
            System.out.println("Windows Proxy Server enabled: '" + proxyEnabledValue.getValueAsBoolean() + "'");
        } else {
            System.out.println("Windows Proxy Server enabled not defined");
        }
    }
}
