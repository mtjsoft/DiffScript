package cn.mtjsoft.utils

import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.math.BigInteger
import java.lang.Exception
import java.io.IOException

object MD5Util {
    //计算文件的MD5
    fun getMd5ByFile(file: File): String {
        var value: String
        var `in`: FileInputStream? = null
        try {
            `in` = FileInputStream(file)
            val byteBuffer = `in`.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(byteBuffer)
            val bi = BigInteger(1, md5.digest())
            value = bi.toString(16)
        } catch (e: Exception) {
            value = ""
        } finally {
            if (null != `in`) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return value
    }
}