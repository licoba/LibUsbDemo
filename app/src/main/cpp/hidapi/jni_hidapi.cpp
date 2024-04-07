

#include <jni.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include "hidapi.h"
#include "libusb.h"
#include "../libusb_utils.h"

#define  LOG_TAG    "LibUsb"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_sayHello(JNIEnv *env, jobject ) {
    std::string hello = "I am hidapi, hello! ";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tmk_libusbdemo_HidApi_hidInit(JNIEnv *env, jobject ) {
    libusb_set_option(nullptr, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);        //
    return hid_init();
}



extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_hidInitNativeDevice(JNIEnv *env, jobject, jint fileDescriptor ) {
    libusb_context *ctx;
    libusb_device_handle *devh;
    int r = 0;

    libusb_set_option(nullptr, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);        //
    libusb_init(nullptr);
    libusb_wrap_sys_device(nullptr, (intptr_t)fileDescriptor, &devh);

    auto device = libusb_get_device(devh);
    print_device(device, devh);
    std::string deviceName = get_device_name(device, devh);
    return env->NewStringUTF(deviceName.c_str());
}


// 获取Hid的版本
extern "C"
JNIEXPORT jstring JNICALL
Java_com_tmk_libusbdemo_HidApi_getHidApiVersion(JNIEnv *env, jobject ) {
    // 获取HIDAPI版本
    const struct hid_api_version* version = hid_version();
    // 将版本信息转换为字符串
    std::string versionStr = std::to_string(version->major) + "." +
                             std::to_string(version->minor) + "." +
                             std::to_string(version->patch);
    // 将C++字符串转换为Java字符串
    return env->NewStringUTF(versionStr.c_str());
}

// 获取Hid设备列表
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_tmk_libusbdemo_HidApi_getHidDeviceList(JNIEnv *env, jobject ) {
    // 获取HID设备列表
//    struct hid_device_info* devs = hid_enumerate(0, 0);
    struct hid_device_info* devs = hid_enumerate(0, 0);
    struct hid_device_info* cur_dev = devs;

    // 计算设备数量
    int deviceCount = 0;
    while (cur_dev) {
        deviceCount++;
        cur_dev = cur_dev->next;
    }

    // 获取Java中的HidDeviceInfo类和它的构造方法
    jclass hidDeviceInfoClass = env->FindClass("com/tmk/libusbdemo/HidDeviceInfo");
    jmethodID constructor = env->GetMethodID(hidDeviceInfoClass, "<init>", "()V");

    // 创建一个HidDeviceInfo对象数组
    jobjectArray deviceArray = env->NewObjectArray(deviceCount, hidDeviceInfoClass, NULL);

    // 填充数组
    cur_dev = devs;
    for (int i = 0; i < deviceCount; i++) {
        jobject deviceInfo = env->NewObject(hidDeviceInfoClass, constructor);

        // 设置HidDeviceInfo的字段
        jfieldID field = env->GetFieldID(hidDeviceInfoClass, "path", "Ljava/lang/String;");
        env->SetObjectField(deviceInfo, field, env->NewStringUTF(cur_dev->path));

        // 设置其他字段，按照实际情况进行
        // ...

        // 将HidDeviceInfo对象添加到数组中
        env->SetObjectArrayElement(deviceArray, i, deviceInfo);

        cur_dev = cur_dev->next;
    }

    // 释放设备列表
    hid_free_enumeration(devs);

    return deviceArray;
}




// 根据VID和PID枚举设备
extern "C"
JNIEXPORT jobject JNICALL
Java_com_tmk_libusbdemo_HidApi_enumerateDevices(JNIEnv *env,
                                                jobject obj,
                                                jint vendorId,
                                                jint productId)  {

    LOGD("Vendor ID: %d, Product ID: %d", vendorId, productId);
    if ((vendorId < 0) || (vendorId > 65535) || (productId < 0) || (productId > 65535)) {
        // Print the vendor ID and product ID
        LOGD("VID/PID Error! Vendor ID: %d, Product ID: %d", vendorId, productId);
        return nullptr;
    }
    // Call the hidapi function to enumerate devices
    struct hid_device_info *devs = hid_enumerate(vendorId, productId);
    struct hid_device_info *cur_dev = devs;

    // Find the HidDeviceInfo class
    jclass cls = env->FindClass("com/tmk/libusbdemo/HidDeviceInfo");

    // Find the constructor of HidDeviceInfo class
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;SSLjava/lang/String;SLjava/lang/String;Ljava/lang/String;ILcom/tmk/libusbdemo/HidDeviceInfo;)V");

    // Create a new HidDeviceInfo object

    jobject deviceInfo = nullptr;
    jobject previousDeviceInfo = nullptr;
    // Enumerate all devices
    while (cur_dev) {
        // Convert C strings and wide strings to Java strings
        jstring path = env->NewStringUTF(cur_dev->path);
        jstring serialNumber = (cur_dev->serial_number == nullptr) ? nullptr : env->NewString((jchar*)cur_dev->serial_number, wcslen(cur_dev->serial_number));
        jstring manufacturerString = (cur_dev->manufacturer_string == nullptr) ? nullptr : env->NewString((jchar*)cur_dev->manufacturer_string, wcslen(cur_dev->manufacturer_string));
        jstring productString = (cur_dev->product_string == nullptr) ? nullptr : env->NewString((jchar*)cur_dev->product_string, wcslen(cur_dev->product_string));

        // Create a new HidDeviceInfo object
        deviceInfo = env->NewObject(cls, constructor, path, cur_dev->vendor_id, cur_dev->product_id, serialNumber, cur_dev->release_number, manufacturerString, productString, cur_dev->interface_number, previousDeviceInfo);

        // Release local references
        env->DeleteLocalRef(path);
        if(serialNumber) env->DeleteLocalRef(serialNumber);
        if(manufacturerString) env->DeleteLocalRef(manufacturerString);
        if(productString) env->DeleteLocalRef(productString);

        // Move to next device
        previousDeviceInfo = deviceInfo;
        cur_dev = cur_dev->next;
    }


    // Free the enumeration
    hid_free_enumeration(devs);

    return deviceInfo;
}