package cn.mtjsoft.bean

data class VersionUpdateInfoBean(
    var vesionCode: Int = 0,
    var vesionName: String = "",
    var md5: String = "",
    var update_message: String = "",
    var project: String = "",
    var down_url: String = "",
    var foreceUpgrade: String = "N",
    var apkSize: String = "",
    var updateType: String = "D",
    var diffPatchs: MutableList<DiffPatch> = mutableListOf(),
)

data class DiffPatch(
    var patchName: String = "",
    var md5: String = "",
    var down_url: String = "",
    var patchSize: String = ""
)