package net.adoptopenjdk.icedteaweb.ie;

public interface WinRegistryConstants {

    int REG_SUCCESS = 0;

    int KEY_READ = 0x20019;

    String PROXY_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    String PROXY_SERVER_REGISTRY_VAL = "ProxyServer";
    String PROXY_SERVER_OVERRIDE_VAL = "ProxyOverride";
}
