package cn.mtjsoft.view

import cn.mtjsoft.AutoScriptWindow
import cn.mtjsoft.utils.FileUtils
import java.awt.Desktop
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextField
import kotlin.system.exitProcess


class AutoWindow : JFrame() {

    private lateinit var autoScriptWindow: AutoScriptWindow

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    private val icon: Image

    private var diffInputPath = ""
    private var oldApkPath = ""
    private var newApkPath = ""

    private val timeTipStr = "差分包计算中..."

    @Volatile
    private var startTime = System.currentTimeMillis()

    init {
        title = "APK差量包拆分工具v1.0.0"
        icon = Toolkit.getDefaultToolkit().createImage(this.javaClass.getResource("/image/icon72.png"))
        iconImage = icon
    }

    fun showWindow(
        diffInputPath: String?, oldApkPath: String?, newApkPath: String?
    ) {
        isResizable = true
        contentPane.add(AutoScriptWindow().apply {
            autoScriptWindow = this
            diffBtn.addActionListener {
                chooseFile(diffInput, JFileChooser.FILES_ONLY, diffInput.text)
            }
            oldVersionBtn.addActionListener {
                chooseFile(oldVersionInput, JFileChooser.DIRECTORIES_ONLY, oldVersionInput.text)
            }
            newVersionBtn.addActionListener {
                chooseFile(newVersionInput, JFileChooser.FILES_ONLY, oldVersionInput.text)
            }
            ok.addActionListener {
                okClick()
            }
            progressBar.isVisible = false
            autoOpenCheck.isSelected = true
        }.root)
        setSize(500, 600)
        setLocationRelativeTo(null)
        isVisible = true
        addWindowCloseListener(this)
        autoScriptWindow.apply {
            if (!diffInputPath.isNullOrEmpty()) {
                diffInput.text = diffInputPath
            }
            val list: List<String> = readPathData()
            if (oldApkPath.isNullOrEmpty()) {
                oldVersionInput.text = if (list.size == 2) list[0] else ""
            } else {
                oldVersionInput.text = oldApkPath
            }
            if (newApkPath.isNullOrEmpty()) {
                newVersionInput.text = if (list.size == 2) list[1] else ""
            } else {
                newVersionInput.text = newApkPath
            }
            diffInput.isEnabled = false
        }
    }

    /**
     * 点击确认
     */
    private fun okClick() {
        autoScriptWindow.apply {
            diffInputPath = diffInput.text
            val diffFile = File(diffInputPath)
            if (diffInputPath.isEmpty() || !diffFile.exists() || !diffFile.isFile) {
                JOptionPane.showMessageDialog(null, "请选择差分工具bsdiff.exe.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            oldApkPath = oldVersionInput.text
            if (oldApkPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请选择旧版本安装包文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            // 循环查找旧版本个数
            val oldApkFileList = FileUtils.findAllFileList(oldApkPath)
            if (oldApkFileList.isEmpty()) {
                JOptionPane.showMessageDialog(null, "旧版本安装包不存在.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            newApkPath = newVersionInput.text
            val newApkFile = File(newApkPath)
            if (newApkPath.isEmpty() || !newApkFile.exists() || !newApkFile.isFile) {
                JOptionPane.showMessageDialog(null, "请选择新版本安装包.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            // 进度条未走完
            if (mTimer?.isRunning == true || progressBar.isVisible && progressBar.maximum > 0 && progressBar.value > 0 && progressBar.value < progressBar.maximum) {
                JOptionPane.showMessageDialog(null, "当前任务还未结束，请稍后.", "提示", JOptionPane.WARNING_MESSAGE)
                return
            }
            // 缓存当前选择的目录数据到文件
            cachePathData()
            // 过滤掉新版本
            val maxSize = oldApkFileList.filter {
                it.versionName != newApkFile.parentFile.name
            }.size
            setProgressBar(maxSize)
            result.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date())
            printResult("\n========完成差量任务数：0/${maxSize}")
            printResult("=====================================")
            // 自动差分
            oldApkFileList.map { task ->
                val oldApk = task.file.absolutePath
                val newApk = newApkPath
                if (oldApk != newApkPath) {
                    val patchName = "V${task.versionName}_V${newApkFile.parentFile.name}.patch"
                    val patch = "${newApkFile.parentFile.absoluteFile}/${patchName}"
                    cmd(
                        "cd /d ${diffFile.parentFile.absoluteFile} && ${diffFile.name} $oldApk $newApk $patch",
                        patch,
                        patchName
                    )
                }
            }
        }
    }

    /**
     * 写入路径缓存
     */
    private fun cachePathData() {
        autoScriptWindow.apply {
            val data = "${oldVersionInput.text}\n${newVersionInput.text}"
            val writeFile = File(File(diffInput.text).parentFile.absolutePath + File.separator + "cache.txt")
            if (writeFile.exists()) {
                writeFile.delete()
            }
            FileUtils.writeStringToFile(
                writeFile.absolutePath,
                data
            )
        }
    }

    /**
     * 读取缓存的数据
     */
    private fun readPathData() = try {
        val file = File(File(autoScriptWindow.diffInput.text).parentFile.absolutePath + File.separator + "cache.txt")
        if (file.exists()) {
            file.readText()
                .split("\n")
        } else {
            mutableListOf()
        }
    } catch (e: Exception) {
        mutableListOf()
    }


    /**
     * 初始化进度条
     */
    private fun setProgressBar(size: Int) {
        autoScriptWindow.apply {
            progressBar.isVisible = size > 0
            progressBar.value = 0
            progressBar.minimum = 0
            progressBar.maximum = size
        }
    }

    /**
     * 每循环一个文件，进度 + 1
     */
    private fun updateProgressBar(addNUm: Int = 1, isOver: Boolean = false) {
        var progress = autoScriptWindow.progressBar.value + addNUm
        if (progress > autoScriptWindow.progressBar.maximum || isOver) {
            progress = autoScriptWindow.progressBar.maximum
        }
        autoScriptWindow.progressBar.value = progress
        printResult("========完成差量任务数：${progress}/${autoScriptWindow.progressBar.maximum}")
        if (progress >= autoScriptWindow.progressBar.maximum) {
            // 完成时，将完整日志保存至，资源文件目录
            printResult("=====================================")
            printResult("===========  全部差分完毕  =============")
            // 差分完毕，打开文件夹
            try {
                if (autoScriptWindow.autoOpenCheck.isSelected) {
                    Desktop.getDesktop().open(File(autoScriptWindow.newVersionInput.text).parentFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 选择文件
     */
    private fun chooseFile(
        jTextField: JTextField,
        model: Int = JFileChooser.DIRECTORIES_ONLY,
        currentDirectoryPath: String = ""
    ) {
        val jFrame = JFrame()
        jFrame.iconImage = icon
        val jfc = if (currentDirectoryPath.isEmpty()) {
            JFileChooser()
        } else {
            JFileChooser(currentDirectoryPath)
        }
        jfc.dialogTitle = if (model == JFileChooser.DIRECTORIES_ONLY) "选择文件夹" else "选择文件"
        jfc.fileSelectionMode = model
        val returnVal = jfc.showOpenDialog(jFrame)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = jfc.selectedFile
            file?.let {
                jTextField.text = it.absolutePath
            }
        }
    }


    private fun cmd(stam: String, diffFilePath: String, s: String) {
        singleThreadExecutor.execute {
            startTime = System.currentTimeMillis()
            startTime()
            printResult(">>> $stam")
            try {
                val diffFile = File(diffFilePath)
                if (diffFile.exists()) {
                    diffFile.delete()
                }
                val processBuilder = ProcessBuilder().command("cmd.exe", "/c", stam)
                val process: Process = processBuilder.start()
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
                //循环等待进程输出，判断进程存活则循环获取输出流数据
                while (process.isAlive) {
                    while (bufferedReader.ready()) {
                        printResult(bufferedReader.readLine())
                    }
                }
                //获取执行结果
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
                printResult(">>> " + e.message)
                printResult("自动差分错误 >>> $s")
            } finally {
                stopTime()
                printResult("自动差分结束 >>> $s 耗时：${(System.currentTimeMillis() - startTime) / 1000}秒")
                updateProgressBar()
            }
        }
    }

    /**
     * 启动计时
     */
    private var mTimer: javax.swing.Timer? = null

    /**
     * 开始计时
     */
    @Synchronized
    private fun startTime() {
        if (mTimer == null) {
            javax.swing.Timer(1000, taskPerformer).also {
                mTimer = it
            }
        }
        if (mTimer?.isRunning == true) {
            mTimer?.stop()
        }
        mTimer?.start()
    }

    /**
     * 结束计时
     */
    @Synchronized
    private fun stopTime() {
        if (mTimer?.isRunning == true) {
            mTimer?.stop()
        }
    }

    /**
     * 计时事件，每秒一次
     */
    private val taskPerformer = ActionListener {
        var lineStartOffset = -1
        var lineEndOffset = -1
        try {
            lineEndOffset = autoScriptWindow.result.text.length
            if (lineEndOffset > 0) {
                if (autoScriptWindow.result.text.endsWith("秒\n")) { // 最后是时间
                    lineStartOffset = autoScriptWindow.result.text.lastIndexOf(timeTipStr)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val result = "$timeTipStr${(System.currentTimeMillis() - startTime) / 1000}秒"
            if (lineStartOffset > -1 && lineEndOffset > lineStartOffset) {
                // 删除掉最后一行
                autoScriptWindow.result.replaceRange(result + "\n", lineStartOffset, lineEndOffset)
            } else {
                printResult(result)
            }
        }
    }

    /**
     * 输出结果
     */
    private fun printResult(s: String, isLine: Boolean = true) {
        autoScriptWindow.result.apply {
            append(s)
            if (isLine) {
                append("\n")
            }
        }
        // 处理自动滚动到最底部
        autoScriptWindow.result.caretPosition = autoScriptWindow.result.text.length
    }

    /**
     * 处理关闭
     */
    private fun addWindowCloseListener(jFrame: JFrame) {
        jFrame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        jFrame.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent?) {
            }

            override fun windowClosing(e: WindowEvent) {
                val option = JOptionPane.showConfirmDialog(
                    jFrame, "确定退出吗?", "提示", JOptionPane.YES_NO_OPTION
                )
                if (option == JOptionPane.YES_OPTION) {
                    if (e.window === jFrame) {
                        jFrame.dispose()
                        exitProcess(0)
                    } else {
                        return
                    }
                } else if (option == JOptionPane.NO_OPTION) {
                    if (e.window === jFrame) {
                        return
                    }
                }
            }

            override fun windowClosed(e: WindowEvent?) {
            }

            override fun windowIconified(e: WindowEvent?) {
            }

            override fun windowDeiconified(e: WindowEvent?) {
            }

            override fun windowActivated(e: WindowEvent?) {
            }

            override fun windowDeactivated(e: WindowEvent?) {
            }
        })
    }

}