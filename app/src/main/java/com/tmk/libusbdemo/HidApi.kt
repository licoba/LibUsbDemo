package com.tmk.libusbdemo

class HidApi {
    external fun sayHello(): String

    companion object {
        init {
            System.loadLibrary("hidapi")
        }
    }
}