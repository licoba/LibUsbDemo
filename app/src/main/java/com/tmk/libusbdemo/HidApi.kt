package com.tmk.libusbdemo

class HidApi {
    external fun sayHello(): String
    external fun getHidApiVersion(): String
    external fun getHidDeviceList(): Array<HidDeviceInfo>
    external fun enumerateDevices(vendorId: Int, productId: Int): HidDeviceInfo

    companion object {
        init {
            System.loadLibrary("hidapi")
        }
    }
}