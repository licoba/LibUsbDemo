package com.tmk.libusbdemo

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Toast
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ToastUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.nio.charset.StandardCharsets

object MyUtil {
    /**
     * 一个 Context 对象，
     * assets 文件夹中要读取的文件名，
     * 要读取的起始下标 startOffset
     * 要读取的数据长度 length
     */
    fun readBytesFromAsset(
        context: Context,
        fileName: String,
        start: Int,
        length: Int
    ): ByteArray? {
        return try {
            val inputStream = context.assets.open(fileName)
            inputStream.skip(start.toLong())
            val buffer = ByteArray(length)
            inputStream.read(buffer)
            inputStream.close()
            buffer
        } catch (e: Exception) {
            null
        }
    }


    /**
     * 读取文件内的全部数据
     */
    fun readBytesFromAssets(context: Context, fileName: String): ByteArray {
        val inputStream = context.assets.open(fileName)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        return buffer
    }


    /**
     * 列出assests根文件夹下的所有文件
     */
    fun listFilesInAssetsRoot(context: Context): List<String> {
        val assetManager = context.assets
        val fileList = mutableListOf<String>()

        try {
            val files = assetManager.list("")
            if (files != null) {
                for (file in files) {
                    fileList.add(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fileList
    }


    /**
     * 显示toast
     * @param text String
     */
    fun showToast(
        text: String, gravity: Int = Gravity.BOTTOM,
        color: Int = Color.WHITE
    ) {
        ToastUtils.make()
            .setDurationIsLong(false)
            .setBgColor(Color.parseColor("#FF0A85FF"))
            .setGravity(gravity, 0, ConvertUtils.dp2px(20f))
            .setTextColor(color)
            .setTextSize(13)
            .show(text)
    }
    fun readAssetsFileToList(context: Context, fileName: String): List<String> {
        val resultList = mutableListOf<String>()
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                resultList.add(line)
                line = reader.readLine()
                val nextLine = reader.readLine()
                if (line != null && nextLine != null) {
                    resultList.add(line + nextLine)
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }
}