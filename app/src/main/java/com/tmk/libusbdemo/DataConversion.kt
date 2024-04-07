package com.tmk.libserialhelper.serialport

import java.util.Locale

/**
 * 数据转换工具类
 *
 */
object DataConversion {
    /**
     * 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
     *
     * @param num
     * @return
     */
    fun isOdd(num: Int): Int {
        return num and 0x1
    }

    /**
     * 将int转成byte
     *
     * @param number
     * @return
     */
    fun intToByte(number: Int): Byte {
        return hexToByte(intToHex(number))
    }

    /**
     * 将int转成hex字符串
     *
     * @param number
     * @return
     */
    fun intToHex(number: Int): String {
        val st = Integer.toHexString(number).uppercase(Locale.getDefault())
        return String.format("%2s", st).replace(" ".toRegex(), "0")
    }

    /**
     * 字节转十进制
     *
     * @param b
     * @return
     */
    fun byteToDec(b: Byte): Int {
        val s = byteToHex(b)
        return hexToDec(s).toInt()
    }

    /**
     * 字节数组转十进制
     *
     * @param bytes
     * @return
     */
    fun bytesToDec(bytes: ByteArray): Int {
        val s = encodeHexString(bytes)
        return hexToDec(s).toInt()
    }

    /**
     * 字节转十六进制字符串
     *
     * @param num
     * @return
     */
    fun byteToHex(num: Byte): String {
        val hexDigits = CharArray(2)
        hexDigits[0] = Character.forDigit((num.toInt() shr 4) and 0xF, 16)
        hexDigits[1] = Character.forDigit(num.toInt() and 0xF, 16)
        return String(hexDigits).uppercase()
    }


    /**
     * 十六进制转byte字节
     *
     * @param hexString
     * @return
     */
    fun hexToByte(hexString: String): Byte {
        return hexString.toInt(16).toByte()
    }

    private fun toDigit(hexChar: Char): Int {
        val digit = hexChar.digitToIntOrNull(16) ?: -1
        require(digit != -1) { "Invalid Hexadecimal Character: $hexChar" }
        return digit
    }

    /**
     * 字节数组转十六进制
     *
     * @param byteArray
     * @return
     */
    fun encodeHexString(byteArray: ByteArray): String {
        val hexStringBuffer = StringBuffer()
        for (i in byteArray.indices) {
            hexStringBuffer.append(byteToHex(byteArray[i]))
        }
        return hexStringBuffer.toString().uppercase(Locale.getDefault())
    }

    /**
     * 十六进制转字节数组
     *
     * @param hexString
     * @return
     */
    fun decodeHexString(hexString: String): ByteArray {
        require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied.-->$hexString" }
        val bytes = ByteArray(hexString.length / 2)
        var i = 0
        while (i < hexString.length) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2))
            i += 2
        }
        return bytes
    }

    /**
     * 十进制转十六进制
     *
     * @param dec
     * @return
     */
    fun decToHex(dec: Int): String {
        var hex = Integer.toHexString(dec)
        if (hex.length == 1) {
            hex = "0$hex"
        }
        return hex.lowercase(Locale.getDefault())
    }

    /**
     * 十六进制转十进制Long
     *
     * @param hex
     * @return
     */
    fun hexToDec(hex: String): Long {
        return hex.toLong(16)
    }

    /**
     * 十六进制转十进制Int
     *
     * @param hex
     * @return
     */
    fun hexToInt(hex: String): Int {
        return hexToDec(hex).toInt()
    }

    /**
     * 十六进制转十进制Int(大端模式)
     *
     * @param hex
     * @return
     */
    fun hexToIntBigEnd(hex: String): Int {
        // 去除十六进制字符串中的空格，并将其转换为大写
        var hex = hex
        hex = hex.replace("\\s".toRegex(), "").uppercase(Locale.getDefault())
        // 将十六进制字符串转换为字节数组
        val bytes = decodeHexString(hex)
        // 计算字节数组的长度
        val length = bytes.size
        // 初始化结果为0
        var result = 0
        // 遍历字节数组
        for (i in 0 until length) {
            // 将字节转换为无符号整数
            var value = bytes[i].toInt() and 0xFF
            // 将无符号整数左移对应的位数
            val shift = (length - 1 - i) * 8
            value = value shl shift
            // 将左移后的值累加到结果中
            result += value
        }
        return result
    }
}


// 拓展函数
fun Byte.toHex(): String = "%02x".format(this).uppercase()
fun Short.toHex(): String = "%04x".format(this).uppercase()
fun UByte.toHex(): String = "%02x".format(toInt()).uppercase()
fun UInt.toHex(): String = "%08x".format(toInt()).uppercase()
fun UShort.toHex(): String = this.toUInt().toHex()
