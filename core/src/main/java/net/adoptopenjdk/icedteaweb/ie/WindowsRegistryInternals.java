package net.adoptopenjdk.icedteaweb.ie;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

public class WindowsRegistryInternals {


    //See https://docs.microsoft.com/en-us/windows/win32/api/winreg/nf-winreg-regopenkeya
    public static int[] invokeOpenKey(final Preferences preferences, int hKey, byte[] lpSubKey, int phkResult) throws InvocationTargetException, IllegalAccessException {
        winCheck();
        Assert.requireNonNull(preferences, "preferences");
        return (int[]) getMethod(preferences.getClass(), "WindowsRegOpenKey", new Class[]{int.class, byte[].class, int.class}).invoke(preferences, hKey, lpSubKey, phkResult);
    }

    //See https://docs.microsoft.com/en-us/windows/win32/api/winreg/nf-winreg-regclosekey
    public static void invokeCloseKey(final Preferences preferences, final int hKey) throws InvocationTargetException, IllegalAccessException {
        winCheck();
        Assert.requireNonNull(preferences, "preferences");
        getMethod(preferences.getClass(), "WindowsRegCloseKey", new Class[]{int.class}).invoke(preferences, hKey);
    }

    //See https://docs.microsoft.com/en-us/windows/win32/api/winreg/nf-winreg-regqueryvalueexa
    public static byte[] invokeQueryValueEx(final Preferences preferences, final int hKey, final byte[] lpValueName) throws InvocationTargetException, IllegalAccessException {
        winCheck();
        Assert.requireNonNull(preferences, "preferences");
        return (byte[]) getMethod(preferences.getClass(), "WindowsRegQueryValueEx", new Class[]{int.class, byte[].class}).invoke(preferences, hKey, lpValueName);
    }

    private static void winCheck() {
        if(!OsUtil.isWindows()) {
            throw new IllegalStateException("Registry functionality can not be used outside of windows");
        }
    }

    public static byte[] toCstr(String str) throws IOException {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.writeUtf8Content(outputStream, str);
            outputStream.write(0);
            return outputStream.toByteArray();
        }
    }

    private static Method getMethod(final Class<? extends Preferences> cls, final String methodName, Class[] params) {
        try {
            Assert.requireNonNull(cls, "cls");
            Assert.requireNonBlank(methodName, "methodName");
            final Method method = cls.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Looks like the given preferences type is no 'native' windows registry preferences type since method '" + methodName + "' can not be found", e);
        }
    }
}
