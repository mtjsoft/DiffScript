# DiffScript
APK增量更新时，批量进行差量包打包的脚本工具


### 1、下载运行工具

#### ①已安装java运行环境的，可以下载tools目录
双击 startUp.bat 运行。</br>
<img src="https://raw.githubusercontent.com/mtjsoft/DiffScript/master/images/tool.png" alt="Use" />

#### ②未安装java运行环境的，可以下载tools2exe目录
双击 DiffScript.exe 运行。</br>
<img src="https://raw.githubusercontent.com/mtjsoft/DiffScript/master/images/tool_exe.jpg" alt="Use" />

### 2、如何使用

<img src="https://raw.githubusercontent.com/mtjsoft/DiffScript/master/images/diffscript.png" alt="Use" />
</br>
1、【差分工具选择】，已经默认选择了，不需要动。</br>
2、【旧版本安装包】，选择旧版本APK安装包所在的文件夹（可以有多个，脚本会扫描文件夹下所有的APK，依次进行差量打包）</br>
3、【新版本安装包】，选择最新版本的安装包APK文件。（可以放到上一步的同一个文件夹里，脚本扫描时，会自动根据版本名称进行区分）</br>

文件夹示例：
</br>
当有多个旧版本APK时，脚本会根据版本号，生成多个对应的差量包文件，命名规则（旧版本名_新版本名.patch）。差量包会保存在新版本APK所在的文件夹中。
</br>
<img src="https://raw.githubusercontent.com/mtjsoft/DiffScript/master/images/version.png" alt="Use" />
</br>
