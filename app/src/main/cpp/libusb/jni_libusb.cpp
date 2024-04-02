

#include <jni.h>
#include "unrooted_android.h"
#include "libusb.h"
#include <android/log.h>
#include <jni.h>
#include <string>

#define  LOG_TAG    "LibUsb"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

int verbose = 0;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_tmkusbdemo_LibUsb_sayHello(JNIEnv *env, jobject ) {
    std::string hello = "I am libusb, hello! ";
    return env->NewStringUTF(hello.c_str());
}