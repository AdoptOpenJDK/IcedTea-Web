package net.adoptopenjdk.icedteaweb.ie;

public interface WinRegistryConstants {

    int HKEY_CURRENT_USER = 0x80000001;
    int HKEY_LOCAL_MACHINE = 0x80000002;
    int REG_SUCCESS = 0;
    int REG_NOTFOUND = 2;
    int REG_ACCESSDENIED = 5;

    int KEY_WOW64_32KEY = 0x0200;
    int KEY_WOW64_64KEY = 0x0100;

    int KEY_ALL_ACCESS = 0xf003f;
    int KEY_READ = 0x20019;

    String PROXY_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
    String PROXY_DWORD_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    String PROXY_SERVER_REGISTRY_VAL = "ProxyServer";
    String PROXY_SERVER_OVERRIDE_VAL = "ProxyOverride";
    String PROXY_AUTOCONFIG_URL_VAL = "AutoConfigURL";
}
