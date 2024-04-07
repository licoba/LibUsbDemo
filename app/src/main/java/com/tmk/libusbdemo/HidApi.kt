package com.tmk.libusbdemo

class HidApi {
    external fun sayHello(): String
    external fun getHidApiVersion(): String
    external fun getHidDeviceList(): Array<HidDeviceInfo>
    external fun enumerateDevices(vendorId: Int, productId: Int): HidDeviceInfo
    external fun hidInit(): Int
    external fun hidInitNativeDevice( fd: Int): String

    companion object {
        init {
            System.loadLibrary("hidapi")
        }
    }
}