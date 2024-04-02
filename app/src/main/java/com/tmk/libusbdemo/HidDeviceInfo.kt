package com.tmk.libusbdemo
data class HidDeviceInfo(
    /** 平台特定的设备路径 */
    var path: String? = null,

    /** 设备厂商ID */
    var vendorId: UShort = 0u,

    /** 设备产品ID */
    var productId: UShort = 0u,

    /** 序列号 */
    var serialNumber: String? = null,

    /** 设备发布号，以二进制编码的十进制表示，也被称为设备版本号 */
    var releaseNumber: UShort = 0u,

    /** 制造商字符串 */
    var manufacturerString: String? = null,

    /** 产品字符串 */
    var productString: String? = null,

    /** 此逻辑设备代表的USB接口。仅在设备是USB HID设备时有效。在所有其他情况下设置为-1。 */
    var interfaceNumber: Int = -1,

    /** 指向下一个设备的指针 */
    var next: HidDeviceInfo? = null,

    /** 底层的总线类型。从版本0.13.0开始，@ref HID_API_VERSION >= HID_API_MAKE_VERSION(0, 13, 0) */
//    var busType: HidBusType = HidBusType.HID_API_BUS_UNKNOWN
)


enum class HidBusType(val value: Int) {
    /** Unknown bus type */
    HID_API_BUS_UNKNOWN(0x00),

    HID_API_BUS_USB(0x01),

    HID_API_BUS_BLUETOOTH(0x02),

    HID_API_BUS_I2C(0x03),

    HID_API_BUS_SPI(0x04),
}
