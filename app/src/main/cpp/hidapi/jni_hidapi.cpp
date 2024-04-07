

#include <jni.h>
#include <locale>
#include <codecvt>
#include <android/log.h>
#include <string>
#include "hidapi.h"
#include "libusb.h"
#include "hidapi_libusb.h"
#include "../libusb_utils.h"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, "LibUsb", __VA_ARGS__)

// 全局变量，用来保存 mHidDevice 对象
hid_device *mHidDevice = nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_sayHello(JNIEnv *env, jobject) {
    std::string hello = "I am hidapi, hello! ";
    return env->NewStringUTF(hello.c_str());
}


//初始化Hid设备，根据文件描述符和接口号
extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_hidInitNativeDevice(JNIEnv *env, jobject, jint fileDescriptor,
                                                   jint interface_number) {
    libusb_set_option(nullptr, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);
    if (mHidDevice == nullptr)
        mHidDevice = hid_libusb_wrap_sys_device((intptr_t) fileDescriptor, interface_number);
    if (mHidDevice == nullptr) {
        LOGD("wrap device failed, mHidDevice is null");
        return env->NewStringUTF("null");
    }
    hid_device_info *hidDeviceInfo = hid_get_device_info(mHidDevice);
    wchar_t *product_string = hidDeviceInfo->product_string;
    std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;
    std::string deviceName = converter.to_bytes(product_string);
    LOGD("deviceName = %s", deviceName.c_str());
    return env->NewStringUTF(deviceName.c_str());
}


// 获取Hid的版本
extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_getHidApiVersion(JNIEnv *env, jobject) {
    const struct hid_api_version *version = hid_version();
    std::string versionStr = std::to_string(version->major) + "." +
                             std::to_string(version->minor) + "." +
                             std::to_string(version->patch);
    return env->NewStringUTF(versionStr.c_str());
}



// 读取Hid设备数据
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_tmk_libusbdemo_HidApi_hidReadData(JNIEnv *env, jobject obj, jint length) {
    // Get the hid_device object from the pointer

    // Allocate a buffer to store the data
    unsigned char *data = new unsigned char[length];

    // Read data from the device
    int bytesRead = hid_read(mHidDevice, data, length);

    // Create a Java byte array to store the data
    jbyteArray dataArray = env->NewByteArray(bytesRead);

    // Copy the data to the Java byte array
    env->SetByteArrayRegion(dataArray, 0, bytesRead, (jbyte *) data);

    // Free the buffer
    delete[] data;

    return dataArray;
}