package com.tmk.libusbdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.blankj.utilcode.util.SPUtils
import com.tmk.libusbdemo.MyUtil.showToast
import com.tmk.libusbdemo.databinding.ActivityMainBinding
import kotlin.math.PI

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var hidApi = HidApi()
    lateinit var usb: USBHIDReader
    private var VID = 0
    private var PID = 0

    companion object {
        val TAG = "Hid测试"
        val KEY_PID = "KEY_PID"
        val KEY_VID = "KEY_VID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usb = USBHIDReader(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sampleText.text = LibUsb().sayHello()
        binding.sampleText.text = hidApi.getHidApiVersion()
        initListener()
        VID = SPUtils.getInstance().getString(KEY_VID,"0").toInt()
        PID = SPUtils.getInstance().getString(KEY_PID,"0").toInt()
        binding.etPid.setText(PID.toString())
        binding.etVid.setText(VID.toString())
    }

    private fun initListener() {
        binding.btnGetDeviceList.setOnClickListener { getDeviceList() }
        binding.btnPermission.setOnClickListener { reqPermission() }
        binding.etPid.doOnTextChanged { text, _, _, _ ->
            SPUtils.getInstance().put(KEY_PID, text.toString().trim())
        }
        binding.etVid.doOnTextChanged { text, _, _, _ ->
            SPUtils.getInstance().put(KEY_VID, text.toString().trim())
        }
    }

    private fun getDeviceList() {
//        val list = hidApi.getHidDeviceList()
//        list.forEach {
//            Log.d(TAG, "Hid设备：$it")
//        }
        VID = binding.etVid.text.toString().trim().toInt()
        PID = binding.etPid.text.toString().trim().toInt()
        val hidDeviceInfo = hidApi.enumerateDevices(VID, PID)
        Log.d(TAG, "Hid设备：$hidDeviceInfo")
    }



    private fun reqPermission(){
        val device = usb.usbManager.deviceList.values.find {
            it.vendorId == VID && it.productId == PID
        }
        if (device == null) {
            showToast("没找到指定设备")
            return
        }
        usb.requestPermission(device) {
            showToast("这个设备已经有权限了")
        }
    }


}