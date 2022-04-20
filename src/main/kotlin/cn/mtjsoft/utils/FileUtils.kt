package cn.mtjsoft.utils

import cn.mtjsoft.bean.TaskDiffBean
import java.io.*
import java.util.*
import java.io.File

import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import java.text.DecimalFormat
import java.nio.channels.FileChannel
import java.nio.file.Path

import java.nio.file.Paths








/**
 * 文件工具
 *
 * @author mtj
 * @date 2021-11-18 16:28:14
 */
object FileUtils {
    fun copy(source: String, dest: String) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            inputStream = FileInputStream(source)
            out = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > 0) {
                out.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun writeStringToFile(filePath: String, saveString: String) {
        try {
            val fw = FileWriter(filePath, true)
            val bw = BufferedWriter(fw)
            bw.write(saveString)
            bw.close()
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun findAllFileList(path: String): List<TaskDiffBean> {
        val list: MutableList<TaskDiffBean> = LinkedList()
        try {
            findFileList(File(path), list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun findFileList(dir: File, fileList: MutableList<TaskDiffBean>) {
        if (dir.isDirectory) {
            dir.list()?.map { s ->
                val file = File(dir, s)
                if (file.isDirectory) {
                    findFileList(file, fileList)
                } else {
                    if (file.name.toLowerCase().endsWith(".apk")) {
                        fileList.add(apkToBean(file))
                    }
                }
            }
        } else {
            if (dir.name.toLowerCase().endsWith(".apk")) {
                fileList.add(apkToBean(dir))
            }
        }
    }

    fun apkToBean(file: File): TaskDiffBean {
        val versionName = try {
            val apkFile = ApkFile(file)
            val apkMeta = apkFile.apkMeta
            apkMeta.versionName
        } catch (e: Exception) {
            ""
        }
        return TaskDiffBean(versionName, file)
    }

    fun getFileSize(file: File): String {
        return  try {
            val filePath: Path = Paths.get(file.absolutePath)
            val fileChannel = FileChannel.open(filePath)
            formatFileSize(fileChannel.size())
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatFileSize(size: Long): String {
        val wrongSize = "0B"
        if (size == 0L) {
            return wrongSize
        }
        val df = DecimalFormat("#.00")
        return when {
            size < 1024 -> {
                df.format(size.toDouble()) + "B"
            }
            size < 1048576 -> {
                df.format(size.toDouble() / 1024) + "KB"
            }
            size < 1073741824 -> {
                df.format(size.toDouble() / 1048576) + "MB"
            }
            size < 1099511627776L -> {
                df.format(size.toDouble() / 1073741824) + "GB"
            }
            size < 1125899906842624L -> {
                df.format(size.toDouble() / 1099511627776L) + "TB"
            }
            else -> {
                df.format(size.toDouble() / 1125899906842624L) + "PB"
            }
        }
    }
}