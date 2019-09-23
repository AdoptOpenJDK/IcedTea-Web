package net.adoptopenjdk.icedteaweb;

public class JavaSystemProperties {

    public static String getJavaVersion() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_VERSION);
    }

    public static String getJavaVendor() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_VENDOR);
    }

    public static String getJavaVendorUrl() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_VENDOR_URL);
    }

    public static String getJavaClassVersion() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_CLASS_VERSION);
    }

    public static String getJavaClassPath() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_CLASS_PATH);
    }

    public static String getOsName() {
        return System.getProperty(JavaSystemPropertiesConstants.OS_NAME);
    }

    public static String getOsVersion() {
        return System.getProperty(JavaSystemPropertiesConstants.OS_VERSION);
    }

    public static String getOsArch() {
        return System.getProperty(JavaSystemPropertiesConstants.OS_ARCH);
    }

    public static String getFileSeparator() {
        return System.getProperty(JavaSystemPropertiesConstants.FILE_SEPARATOR);
    }

    public static String getPathSeparator() {
        return System.getProperty(JavaSystemPropertiesConstants.PATH_SEPARATOR);
    }

    public static String getLineSeparator() {
        return System.getProperty(JavaSystemPropertiesConstants.LINE_SEPARATOR);
    }

    public static String getJavaSpecVersion() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_SPEC_VERSION);
    }

    public static String getJavaSpecVendor() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_SPEC_VENDOR);
    }

    public static String getJavaSpecName() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_SPEC_NAME);
    }

    public static String getVmSpecVendor() {
        return System.getProperty(JavaSystemPropertiesConstants.VM_SPEC_VENDOR);
    }

    public static String getVmSpecName() {
        return System.getProperty(JavaSystemPropertiesConstants.VM_SPEC_NAME);
    }

    public static String getVmVersion() {
        return System.getProperty(JavaSystemPropertiesConstants.VM_VERSION);
    }

    public static String getVmVendor() {
        return System.getProperty(JavaSystemPropertiesConstants.VM_VENDOR);
    }

    public static String getVmName() {
        return System.getProperty(JavaSystemPropertiesConstants.VM_NAME);
    }

    public static String getUserLanguage() {
        return System.getProperty(JavaSystemPropertiesConstants.USER_LANGUAGE);
    }

    public static String getUserHome() {
        return System.getProperty(JavaSystemPropertiesConstants.USER_HOME);
    }

    public static String getJavaTempDir() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_IO_TMPDIR);
    }

    public static String getUserDir() {
        return System.getProperty(JavaSystemPropertiesConstants.USER_DIR);
    }

    public static String getUserName() {
        return System.getProperty(JavaSystemPropertiesConstants.USER_NAME);
    }

    public static String getJavaHome() {
        return System.getProperty(JavaSystemPropertiesConstants.JAVA_HOME);
    }

    public static String getAwtHeadless() {
        return System.getProperty(JavaSystemPropertiesConstants.AWT_HEADLESS);
    }

    public static String getAwtUseSystemAAFontSettings() {
        return System.getProperty(JavaSystemPropertiesConstants.AWT_AA_FONT_SETTINGS);
    }
}
