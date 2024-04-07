package com.tmk.libusbdemo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import java.util.Locale

class USBHIDReader(var context: Context) {
    var TAG = "USBHIDReader"
    lateinit var usbManager: UsbManager
    var usbDevice: UsbDevice? = null
    var usbHidInterface: UsbInterface? = null
    var usbHidRead: UsbEndpoint? = null
    var usbHidWrite: UsbEndpoint? = null
    var usbDeviceConnection: UsbDeviceConnection? = null
    var permissionIntent: PendingIntent? = null
    private var packetSize = 0
    private var iRlistener: IReceiveDataListener? = null


    fun open(device: UsbDevice, type: USBCommType): Boolean {
        if (device.vendorId == VID && device.productId == PID) {
            usbDevice = device
            if (usbManager.hasPermission(device)) {
                usbDevice = device
                if (findInterface(type)) {
                    if (assignEndpoint(type)) {
                        return openDevice()
                    } else {
                        Log.e(TAG, "没有找到端点")
                    }
                } else {
                    Log.e(TAG, "没有找到Hid接口")
                }
            } else {
                Log.d(TAG, "没权限打开")
            }
        } else {
            Log.d(TAG, "ID不一样")
        }
        return false
    }


    private fun findInterface(type: USBCommType): Boolean {
        if (usbDevice == null) {
            return false
        }
        Log.d(TAG, "寻找接口 $type")
        Log.d(TAG, "USB接口数量 : " + usbDevice!!.interfaceCount)
        for (i in 0 until usbDevice!!.interfaceCount) {
            val intf = usbDevice!!.getInterface(i)
            Log.d(TAG, "USB接口 : $intf")
            if (type == USBCommType.HID) {
                if (intf.interfaceClass == UsbConstants.USB_CLASS_HID) {
                    Log.d(TAG, "找到了HID接口(class=3)")
                    usbHidInterface = intf
                    return true
                }
            } else if (type == USBCommType.STORAGE) {
                if (intf.interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE) {
                    Log.d(TAG, "找到了升级Storage接口！(class=8)")
                    usbHidInterface = intf
                    return true
                }
            }
        }
        return false
    }


    // 指定端点
    private fun assignEndpoint(type: USBCommType): Boolean {
        Log.d(TAG, "指定分配端点")
        if (usbHidInterface == null) {
            return false
        }
        val endpointType = when (type) {
            USBCommType.HID -> UsbConstants.USB_ENDPOINT_XFER_INT
            USBCommType.STORAGE -> UsbConstants.USB_ENDPOINT_XFER_BULK
        }
        for (i in 0 until usbHidInterface!!.endpointCount) {
            val ep = usbHidInterface!!.getEndpoint(i)
            if (ep.type == endpointType) {
                if (ep.direction == UsbConstants.USB_DIR_OUT) {
                    Log.d(TAG, "找到了Out端点")
                    usbHidWrite = ep
                } else {
                    usbHidRead = ep
                    packetSize = usbHidRead!!.maxPacketSize
                    Log.d(TAG, "找到了In端点,packetSize: $packetSize")
                }
            }
        }
        return true
    }


    fun openDevice(): Boolean {
        if (usbHidInterface == null) {
            Log.e(TAG, "Usb接口为空，打开设备失败")
            return false
        }
        var conn: UsbDeviceConnection? = null
        if (usbManager.hasPermission(usbDevice)) {
            conn = usbManager.openDevice(usbDevice)
        }
        if (conn == null) {
            Log.e(TAG, "连接为空，打开设备失败")
            return false
        }
        Log.e(TAG, "开始声明设备访问权..")
        // 声明访问权限，参数force在这里表示如果该接口已经被其他应用程序声明，那么是否强制夺取该接口的控制权
        if (conn.claimInterface(usbHidInterface, true)) {
            usbDeviceConnection = conn
            Log.e(TAG, "打开设备成功")
        } else {
            Log.e(TAG, "声明访问权限失败，关闭连接")
            conn.close()
        }
        return true
    }




    fun close() {
        if (null == usbManager) {
            return
        }
        if (usbManager.deviceList.isEmpty()) {
            return
        }
        if (usbDevice != null) {
            return
        }

        usbDeviceConnection!!.releaseInterface(usbHidInterface)
        usbDeviceConnection!!.close()
        usbDeviceConnection = null
        usbHidInterface = null
        usbHidRead = null
        usbHidWrite = null
        usbDevice = null
        context.unregisterReceiver(usbReceiver)
        Log.e(TAG, "USB connection closed")
    }


    fun sendBytes(data: ByteArray): Int {
        if (usbDevice != null && usbHidWrite != null && usbManager.hasPermission(usbDevice)) {
            Log.e(TAG, "发送[${data.size}]-> " + bytes2HexString(data))
            val ret = usbDeviceConnection!!.bulkTransfer(usbHidWrite, data, data.size, 500)
            Log.e(TAG, "发送结果: $ret")
            return ret
        } else {
            Log.e(TAG, "无法发送，检查权限和端口")
            return -1
        }
    }





    fun UsbSCSIWrite(
        pCDB: ByteArray?,
        nCDBLen: Int,
        pData: ByteArray?,
        nDataLen: Int,
        nTimeOut: Int
    ): Boolean {
        val w_abyTmp = ByteArray(31)
        val w_abyCSW = ByteArray(13)
        var w_bRet: Boolean

        //Arrays.fill(w_abyTmp, (byte)0);
        w_abyTmp[0] = 0x55
        w_abyTmp[1] = 0x53
        w_abyTmp[2] = 0x42
        w_abyTmp[3] = 0x43
        w_abyTmp[4] = 0x28
        w_abyTmp[5] = 0x2b
        w_abyTmp[6] = 0x18
        w_abyTmp[7] = 0x89.toByte()
        w_abyTmp[8] = 0x00
        w_abyTmp[9] = 0x00
        w_abyTmp[10] = 0x00
        w_abyTmp[11] = 0x00
        w_abyTmp[12] = 0x00 //cCBWFlags
        w_abyTmp[13] = 0x00 //cCBWlun
        w_abyTmp[14] = 0x0a //cCBWCBLength
        System.arraycopy(pCDB, 0, w_abyTmp, 15, nCDBLen)
        //System.arraycopy(pData, 0, w_abyTmp, 31, nDataLen);
        sendBytes(w_abyTmp)
        sendBytes(pData!!)
        return true
    }


    interface IReceiveDataListener {
        fun onReceiveData(byteArray: ByteArray)
    }

    fun getiRlistener(): IReceiveDataListener? {
        return iRlistener
    }

    fun setiRlistener(iRlistener: IReceiveDataListener?) {
        this.iRlistener = iRlistener
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.e(TAG, "广播接收器收到回调")
            if (ACTION_USB_PERMISSION == action) {
                Log.e(TAG, "USB权限")
                synchronized(this) {
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false
                        )
                    ) {
                        Log.e(TAG, "权限已经获取")
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (device == null) {
                            Log.d(TAG, "device is null")
                            return
                        }
                        if (device.vendorId == VID && device.productId == PID) {
                            if (usbManager.hasPermission(device)) {
                                Log.e(TAG, "usb设备有权限了")
                                usbDevice = device
                                if (findInterface(USBCommType.HID)) {
                                    if (assignEndpoint(USBCommType.HID)) {
                                        openDevice()
                                    } else {

                                    }
                                } else {
                                    Log.e(TAG, "没有找到接口")
                                }
                            } else {

                            }
                        } else {

                        }
                    } else {
                        Log.d(TAG, "Permission denied for USB device")
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent
                    .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (null != device && usbDevice != null && device.vendorId == VID && device.productId == PID) {
                    usbDeviceConnection!!.releaseInterface(usbHidInterface)
                    usbDeviceConnection!!.close()
                    usbDeviceConnection = null
                    usbDevice = null
                    Log.d(TAG, "USB connection closed")
                }
            }
        }
    }

    init {
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        registerReceiver()
    }


    fun requestPermission(device: UsbDevice, alreadyHas: ((Boolean) -> Unit)? = null) {
        Log.d(TAG, "请求权限...")
        if (usbManager.hasPermission(device)) {
            Log.e(TAG, "设备已经有权限了，不需要再次请求")
            alreadyHas?.invoke(true)
            return
        }
        permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }


    private fun registerReceiver() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "注册广播监听...")
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
    }

    fun listUsbDevices(): String {
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            return "no usb devices found"
        }
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        var returnValue = ""
        var usbInterface: UsbInterface
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            returnValue += "\n\uD83D\uDFE2Name: " + device.deviceName
            returnValue += """
                
                ID: ${device.deviceId}
                """.trimIndent()
            returnValue += """
                
                Protocol: ${device.deviceProtocol}
                """.trimIndent()
            returnValue += """
                
                Class: ${device.deviceClass}
                """.trimIndent()
            returnValue += """
                
                Subclass: ${device.deviceSubclass}
                """.trimIndent()
            returnValue += """
                
                Product ID: ${device.productId}
                """.trimIndent()
            returnValue += """
                
                Vendor ID: ${device.vendorId}
                """.trimIndent()
            returnValue += """
                
                Interface count: ${device.interfaceCount}
                """.trimIndent()
            returnValue += "\n—————————————————————"
            for (i in 0 until device.interfaceCount) {
                usbInterface = device.getInterface(i)
                returnValue += "\n  接口 ${i + 1}"
                returnValue += "\n\tInterface ID: " + usbInterface.id
                returnValue += "\n\tClass: " + usbInterface.interfaceClass
                returnValue += "\n\tProtocol: " + usbInterface.interfaceProtocol
                returnValue += "\n\tSubclass: " + usbInterface.interfaceSubclass
                returnValue += "\n\tEndpoint count: " + usbInterface.endpointCount
                for (j in 0 until usbInterface.endpointCount) {
                    returnValue += "\n\t  Endpoint $j"
                    returnValue += "\n\t\t\tAddress: " + usbInterface.getEndpoint(j).address
                    returnValue += "\n\t\t\tAttributes: " + usbInterface.getEndpoint(j).attributes
                    returnValue += "\n\t\t\tDirection: " + usbInterface.getEndpoint(j).direction
                    returnValue += "\n\t\t\tNumber: " + usbInterface.getEndpoint(j).endpointNumber
                    returnValue += "\n\t\t\tInterval: " + usbInterface.getEndpoint(j).interval
                    returnValue += "\n\t\t\tType: " + usbInterface.getEndpoint(j).type
                    returnValue += "\n\t\t\tMax packet size: " + usbInterface.getEndpoint(j).maxPacketSize
                }
                returnValue += "\n  ———————————————————"

            }
        }
        return returnValue
    }

    companion object {
//        const val VID = 32903
//        const val PID = 4132
        const val VID = 3853
        const val PID = 674
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        fun toInt(b: Byte): Int {
            return b.toInt() and 0xFF
        }

        fun toByte(c: Int): Byte {
            return (if (c <= 0x7f) c else c % 0x80 - 0x80).toByte()
        }

        fun bytes2HexString(b: ByteArray): String {
            var stmp = ""
            val sb = StringBuilder("")
            for (n in b.indices) {
                stmp = Integer.toHexString(b[n].toInt() and 0xFF)
                sb.append(if (stmp.length == 1) "0$stmp" else stmp)
                sb.append(" ")
            }
            return sb.toString().uppercase(Locale.getDefault()).trim { it <= ' ' }
        }
    }


    enum class USBCommType {
        HID, STORAGE
    }

}
