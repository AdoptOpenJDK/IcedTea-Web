#include <jni.h>
#include <stdio.h>
#include "ClassWithNativeCall.h"

JNIEXPORT jstring JNICALL Java_net_adoptopenjdk_integration_ClassWithNativeCall_callNative(JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello from native world!");
}