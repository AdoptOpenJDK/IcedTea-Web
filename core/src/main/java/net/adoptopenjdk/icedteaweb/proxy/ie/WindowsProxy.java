package net.adoptopenjdk.icedteaweb.proxy.ie;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static net.adoptopenjdk.icedteaweb.proxy.ie.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WindowsProxyConstants.PROXY_REGISTRY_KEY;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WindowsProxyConstants.PROXY_SERVER_OVERRIDE_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.ie.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    private static void printProxySettings() throws InterruptedException, ExecutionException, IOException {
        final Map<String, RegistryValue> proxyRegistryEntries = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
        final RegistryValue proxyEnabledValue = proxyRegistryEntries.get(PROXY_ENABLED_VAL);
        if (proxyEnabledValue != null && proxyEnabledValue.getValueAsBoolean()) {
            System.out.println("Windows Proxy Server is enabled");

            final RegistryValue autoConfigUrlValue = proxyRegistryEntries.get(AUTO_CONFIG_URL_VAL);
            if (autoConfigUrlValue != null) {
                System.out.println("Windows Proxy Server use PAC with URL: " + autoConfigUrlValue.getValue());
            } else {
                final RegistryValue proxyServerValue = proxyRegistryEntries.get(PROXY_SERVER_REGISTRY_VAL);
                if (proxyServerValue != null) {
                    Arrays.asList(proxyServerValue.getValue().split(Pattern.quote(";"))).forEach(p -> System.out.println("Proxy: " + p));
                } else {
                    System.out.println("No specific Proxy server defined. DIRECT will be used");
                }
                final RegistryValue overrideHostsValue = proxyRegistryEntries.get(PROXY_SERVER_OVERRIDE_VAL);
                if (overrideHostsValue != null) {
                    Arrays.asList(overrideHostsValue.getValue().split(Pattern.quote(";"))).forEach(p -> System.out.println("Exclusion: " + p));
                } else {
                    System.out.println("No exclusion defined");
                }
            }
        } else {
            System.out.println("Windows Proxy Server is not enabled");
        }
    }

    public static void main(String[] args) throws Exception {
        printProxySettings();
    }
}
