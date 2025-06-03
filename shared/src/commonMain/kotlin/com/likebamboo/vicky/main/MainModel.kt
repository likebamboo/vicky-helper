package com.likebamboo.vicky.main

import java.io.File

internal const val KEY_1: String = "店铺名称"
internal const val KEY_2: String = "广告活动"
internal const val KEY_3: String = "关键词"
internal const val KEY_4: String = "匹配方式"
internal const val KEY_5: String = "有效状态"
internal const val KEY_6: String = "竞价-本币"
// internal const val KEY_7: String = "输出结果"
internal const val KEY_8: String = "默认竞价-本币"
internal const val KEY_9: String = "曝光量"
internal const val KEY_10: String = "搜索结果首页首位IS"
internal const val KEY_11: String = "点击"
internal const val KEY_12: String = "CTR"
internal const val KEY_13: String = "CPC-本币"
internal const val KEY_14: String = "花费-本币"
internal const val KEY_15: String = "销售额-本币"
internal const val KEY_16: String = "直接成交销售额-本币"
internal const val KEY_17: String = "间接成交销售额-本币"
internal const val KEY_18: String = "ACoS"
internal const val KEY_19: String = "ROAS"
internal const val KEY_20: String = "广告订单"
internal const val KEY_21: String = "直接成交订单"
internal const val KEY_22: String = "间接成交订单"
internal const val KEY_23: String = "间接订单占比"
internal const val KEY_24: String = "CPA-本币"
internal const val KEY_25: String = "CVR"
internal const val KEY_26: String = "广告销量"


internal const val KEY_100: String = "目标竞价"
internal const val KEY_101: String = "真实ACos"
internal const val KEY_102: String = "基础点击"
internal const val KEY_103: String = "平均CPA"
internal const val KEY_104: String = "平均CPC"
internal const val KEY_105: String = "平均ACoS"
internal const val KEY_106: String = "基础竞价"
internal const val KEY_107: String = "产品单价"

/**
 * 主界面ui触发的事件
 */
sealed class MainEvent {
    data class LoadPropertyFile(val file: File) : MainEvent()
    data class LoadDataFile(val file: File) : MainEvent()
    data class Submit(val str: String?) : MainEvent()
}

/**
 * 主界面状态
 */
data class MainUi(
    val propertyFile: File? = null,
    val dataFile: File? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val log: StringBuilder = StringBuilder(),
)

data class Property(
    // 活动名称
    val activity: String = "",
    // 基础竞价
    val baseBid: Float = 0f,
    // 单价
    val price: Float = 0f,
)

data class RowData(
    val cells: MutableMap<String, String> = mutableMapOf(),
)

