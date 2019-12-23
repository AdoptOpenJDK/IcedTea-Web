package net.adoptopenjdk.integration;

public class ClassWithNativeCall {

    static {
        System.loadLibrary("nativeLib");
    }

    public native String callNative();
}
