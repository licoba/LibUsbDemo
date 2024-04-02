package com.tmk.libusbdemo

class LibUsb {
    external fun sayHello(): String

    companion object {
        // Used to load the 'libusbdemo' library on application startup.
        init {
            System.loadLibrary("libusb")
        }
    }
}