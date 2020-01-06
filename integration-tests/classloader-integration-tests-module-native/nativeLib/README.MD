This folder contains everything to create a native lib that is called in the Java class of this module.

# How to create the native Library

The header file (`ClassWithNativeCall.h`) is created based on this tutorial: https://www.java-tips.org/other-api-tips-100035/148-jni/1378-simple-example-of-using-the-java-native-interface.html
The c file was written by hand. Once this is done it can be compiled.

## Compile with GCC on MAC

Based on the Java `System.mapLibraryName(libname)` call the native file must be called `libnativeLib.dylib`

`gcc -I/Library/Java/JavaVirtualMachines/adoptopenjdk8u212-b03/Contents/Home/include -I/Library/Java/JavaVirtualMachines/adoptopenjdk8u212-b03/Contents/Home/include/darwin -dynamiclib ClassWithNativeCall.c -o libnativeLib.dylib`

# How to add the native lib

The Maven build of this project adds all native libraries to the JAR. By calling `new ClassWithNativeCall().callNative()` you can check if the native lib is called.