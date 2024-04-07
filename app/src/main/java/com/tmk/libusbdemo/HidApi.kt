package com.tmk.libusbdemo

class HidApi {
    external fun sayHello(): String
    external fun getHidApiVersion(): String
    external fun hidInitNativeDevice(fileDescriptor: Int, interfaceNumber: Int): String
    external fun hidReadData(length: Int): ByteArray

    companion object {
        init {
            System.loadLibrary("hidapi")
        }
    }
}