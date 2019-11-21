package net.adoptopenjdk.icedteaweb.ie;

import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.KEY_READ;
import static net.adoptopenjdk.icedteaweb.ie.WinRegistryConstants.REG_SUCCESS;

public class WinRegistry {

    private WinRegistry() {

    }

    public static String readStringFromRegistry(final RegistryScope scope, final String key, final String value) throws Exception {
        return readStringFromRegistry(scope, key, value, 0);
    }

    public static String readStringFromRegistry(final RegistryScope scope, final String key, final String value, final int wow64) throws Exception {
        int[] handles = WindowsRegistryInternals.invokeOpenKey(scope.getPreferences(), scope.getRawValue(), WindowsRegistryInternals.toCstr(key), new Integer(KEY_READ | wow64));
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        byte[] valb = WindowsRegistryInternals.invokeQueryValueEx(scope.getPreferences(), handles[0], WindowsRegistryInternals.toCstr(value));
        WindowsRegistryInternals.invokeCloseKey(scope.getPreferences(), handles[0]);
        return (valb != null ? new String(valb).trim() : null);
    }

}
