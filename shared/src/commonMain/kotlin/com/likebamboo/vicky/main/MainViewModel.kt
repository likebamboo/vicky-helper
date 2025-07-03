package com.likebamboo.vicky.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likebamboo.vicky.utils.ExcelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToInt

/**
 * 没有基础竞价
 */
private const val NO_BASE_BID = -9999f

/**
 * 没有对应的活动
 */
private const val NO_AD_ACTIVITY = -99999f

class MainViewModel : ViewModel() {
    companion object {
        private val CONFIG_FILE_PATH: String
            get() {
                // 获取用户主目录下的默认配置文件目录
                val userHome = System.getProperty("user.home")
                val configDir = Paths.get(userHome, ".vicky_helper") // ".myAppConfig" 是自定义的目录名

                try {
                    // 创建配置目录（如果不存在）
                    if (!Files.exists(configDir)) {
                        Files.createDirectories(configDir)
                    }
                } catch (e: Exception) {
                    // System.err.println("保存配置文件时出错: " + e.message)
                }
                // 定义配置文件路径
                return configDir.resolve("config.txt").toFile().absolutePath
            }
    }

    private val _uiState = MutableStateFlow(MainUi())
    val uiState: StateFlow<MainUi> = _uiState.asStateFlow()

    init {
        readLastPaths()
    }

    /**
     * 处理事件
     */
    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.LoadPropertyFile -> {
                _uiState.update {
                    it.copy(
                        propertyFile = event.file,
                        loading = false,
                        error = null
                    )
                }
                saveLastPaths()
            }

            is MainEvent.LoadDataFile -> {
                _uiState.update {
                    it.copy(
                        dataFile = event.file,
                        loading = false,
                        error = null
                    )
                }
                saveLastPaths()
            }

            is MainEvent.Submit -> {
                process()
            }
        }
    }

    private fun process() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch(Dispatchers.Default) {
            // 数据处理
            try {
                // 假设这里是处理数据的逻辑
                val properties = readProperties()
                val groups = doProcessData(properties)
                // 写数据
                val outputFile = File(
                    _uiState.value.dataFile?.parentFile,
                    _uiState.value.dataFile?.nameWithoutExtension + "_output.xlsx"
                )
                writeExcelFile(outputFile.absolutePath, groups)
                _uiState.update {
                    it.copy(
                        log = it.log.append("\n 处理完成：${outputFile.absolutePath}"),
                        loading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, loading = false) }
            }
        }
    }


    // 保存文件路径
    private fun saveLastPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pathsFile = File(CONFIG_FILE_PATH)
                pathsFile.writeText("")
                pathsFile.appendText(_uiState.value.propertyFile?.absolutePath ?: "")
                pathsFile.appendText("\n")
                pathsFile.appendText(_uiState.value.dataFile?.absolutePath ?: "")
            } catch (e: Exception) {
                // 保存失败时不影响主流程
            }
        }
    }

    // 保存文件路径
    private fun readLastPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pathsFile = File(CONFIG_FILE_PATH)
                if (pathsFile.exists() && pathsFile.isFile) {
                    val lines = pathsFile.readLines()
                    if (lines.size >= 2) {
                        _uiState.update {
                            it.copy(
                                propertyFile = File(lines[0]),
                                dataFile = File(lines[1])
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // 保存失败时不影响主流程
            }
        }
    }

    private suspend fun writeExcelFile(filePath: String, groups: MutableMap<String, List<RowData>>) {
        withContext(Dispatchers.IO) {
            // 表头
            val headers = listOf(
                KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_8,
                KEY_9, KEY_10, KEY_11, KEY_12, KEY_13, KEY_14, KEY_15,
                KEY_16, KEY_17, KEY_18, KEY_19, KEY_20, KEY_21, KEY_22,
                KEY_23, KEY_24, KEY_25, KEY_26,
                // 新增的列
                KEY_100, KEY_101, KEY_102, KEY_103, KEY_104, KEY_105,
                KEY_106, KEY_107
            )
            // 用数字表示的列（包括用数字表示的列）
            val numberCols = listOf(
                KEY_6, KEY_8,
                KEY_9, KEY_10, KEY_11, KEY_12, KEY_13, KEY_14, KEY_15,
                KEY_16, KEY_17, KEY_18, KEY_19, KEY_20, KEY_21, KEY_22,
                KEY_23, KEY_24, KEY_25, KEY_26,
                // 新增的列
                KEY_100, KEY_101, KEY_102, KEY_103, KEY_104, KEY_105,
                KEY_106, KEY_107
            )
            // 用百分比表示的列
            val percentColsIdx = mutableListOf<Int>()
            headers.forEachIndexed { index, key ->
                if (key == KEY_10 || key == KEY_12 || key == KEY_18 || key == KEY_23 || key == KEY_101 || key == KEY_105) {
                    percentColsIdx.add(index)
                }
            }
            val workbook = ExcelUtils.createSingleSheetExcel(headers.toTypedArray())
            val sheet = workbook.getSheetAt(0)

            // 创建数字样式
            val decimalStyle = workbook.createCellStyle()
            // 设置为保留两位小数
            decimalStyle.dataFormat = workbook.createDataFormat().getFormat("0.00")

            // 创建一个数字样式 + 红色背景
            val decimalRedStyle = workbook.createCellStyle()
            decimalRedStyle.dataFormat = workbook.createDataFormat().getFormat("0.00")
            // 设置红色背景
            decimalRedStyle.setFillForegroundColor(IndexedColors.RED.getIndex())
            decimalRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            // 创建一个数字样式 + 红色背景
            val decimalDarkRedStyle = workbook.createCellStyle()
            decimalDarkRedStyle.dataFormat = workbook.createDataFormat().getFormat("0.00")
            // 设置红色背景
            decimalDarkRedStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex())
            decimalDarkRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            // 创建百分比样式，保留两位小数
            val percentStyle = workbook.createCellStyle()
            percentStyle.dataFormat = workbook.createDataFormat().getFormat("0.00%")

            groups.forEach { (activity, datas) ->
                // 写入数据
                datas.forEachIndexed { rowIndex, data ->
                    val item = mutableListOf<Any>()
                    headers.forEach { key ->
                        // 数字类型的列
                        val str = data.cells[key]
                        if (numberCols.contains(key)) {
                            val number = if (str?.endsWith("%") == true) {
                                (str.removeSuffix("%").toFloatOrNull() ?: 0f) / 100f
                            } else {
                                str?.toFloatOrNull() ?: 0f
                            }
                            item.add(number)
                        } else {
                            item.add(str ?: "")
                        }
                    }

                    if (item.first().toString().startsWith("汇总")) {
                        ExcelUtils.insertSingleRow(workbook, item.first().toString(), item.size)
                    } else {
                        ExcelUtils.insertRow(
                            workbook, item.toTypedArray(), sheet.sheetName,
                            styleProvider = { idx, cell, value ->
                                if (percentColsIdx.contains(idx)) {
                                    percentStyle
                                } else {
                                    when (value.toFloat()) {
                                        NO_BASE_BID -> {
                                            cell.setBlank()
                                            decimalDarkRedStyle
                                        }
                                        NO_AD_ACTIVITY -> {
                                            cell.setBlank()
                                            decimalRedStyle
                                        }
                                        else -> decimalStyle
                                    }
                                }
                            }
                        )
                    }
                }
            }
            // 保存工作簿到文件
            ExcelUtils.saveExcelToFile(workbook, filePath)
        }
    }

    private suspend fun readProperties(): List<Property> {
        return withContext(Dispatchers.Default) {
            val workbook = ExcelUtils.readExcelFile(_uiState.value.propertyFile?.absolutePath)
            if (workbook != null) {
                // 处理工作簿数据
                val sheet = workbook.getSheetAt(0)
                // 数据行
                val properties = mutableListOf<Property>()
                sheet.forEachIndexed { rowIndex, row ->
                    if (rowIndex != 0) {
                        row.forEachIndexed { index, cell ->
                            val item = Property(
                                activity = row.getCell(0)?.toString() ?: "",
                                baseBid = if (row.getCell(1)?.numericCellValue?.toFloat() == 0f) {
                                    NO_BASE_BID
                                } else row.getCell(1)?.numericCellValue?.toFloat() ?: NO_BASE_BID,
                                price = row.getCell(2)?.numericCellValue?.toFloat() ?: 0f
                            )
                            properties.add(item)
                        }
                    }
                }
                return@withContext properties
            }
            throw Exception("无法读取配置文件: ${_uiState.value.propertyFile?.absolutePath}")
        }
    }

    private suspend fun doProcessData(properties: List<Property> = emptyList()): MutableMap<String, List<RowData>> {
        return withContext(Dispatchers.Default) {
            val workbook = ExcelUtils.readExcelFile(_uiState.value.dataFile?.absolutePath)
            if (workbook != null) {
                // 处理工作簿数据
                val sheet = workbook.getSheetAt(0)
                // 表头
                val header = mutableListOf<String>()
                // 数据行
                val rowData = mutableListOf<RowData>()
                val num = sheet.lastRowNum
                for (i in 0..num) {
                    val row = sheet.getRow(i)
                    if (row == null) {
                        continue
                    }

                    val cellNum = row.lastCellNum
                    if (cellNum <= 0) {
                        continue
                    }

                    // 如果是第一行，读取表头
                    if (header.isEmpty()) {
                        // 读取表头
                        for (j in 0 until cellNum) {
                            val cell = row.getCell(j)
                            if (cell != null) {
                                header.add(cell.toString())
                            }
                        }
                        continue
                    }

                    rowData.add(RowData().apply {
                        // 读取数据行
                        for (j in 0 until cellNum) {
                            val cell = row.getCell(j)
                            if (cell != null) {
                                cells[header.getOrNull(j) ?: ""] = cell.toString()
                            }
                        }
                    })
                }
                // 分组
                val groups = rowData.groupBy { it.cells[KEY_2] ?: "" }
                val outputGroups = groups.toMutableMap()
                // 处理分组数据
                groups.forEach { activity, datas ->
                    // 总花费
                    var sumCost = 0.0f
                    // 总订单
                    var sumOrders = 0
                    // 总点击
                    var sumClick = 0
                    // 总销售额
                    var sumSaleMoneys = 0.0f
                    // 总销量
                    var sumSaleCounts = 0

                    // 输出数据
                    val outputDatas = mutableListOf<RowData>()

                    // 该活动产品基础竞价
                    val property = properties.find {
                        it.activity == activity
                    }
                    val bid = property?.baseBid ?: NO_AD_ACTIVITY
                    // 真实acos
                    datas.forEach { d ->
                        // 当前关键词的广告花费
                        val keywordCost = d.cells[KEY_14]?.toFloatOrNull() ?: 0.0f
                        sumCost += keywordCost
                        // 当前关键词的订单数
                        val keywordOrder = d.cells[KEY_20]?.toFloatOrNull()?.toInt() ?: 0
                        sumOrders += keywordOrder
                        // 当前关键词的点击数
                        val keywordClick = d.cells[KEY_11]?.toFloatOrNull()?.toInt() ?: 0
                        sumClick += keywordClick

                        val saleMoney = d.cells[KEY_15]?.toFloatOrNull() ?: 0.0f
                        sumSaleMoneys += saleMoney
                        val saleCount = d.cells[KEY_26]?.toFloatOrNull()?.toInt() ?: 0
                        sumSaleCounts += saleCount
                    }

                    // 价格
                    val price = if (sumSaleCounts > 0) {
                        sumSaleMoneys / sumSaleCounts
                    } else {
                        0.0f
                    }
                    // 真实acos
                    datas.forEach { d ->
                        // 当前关键词的广告花费
                        val keywordCost = d.cells[KEY_14]?.toFloatOrNull() ?: 0.0f
                        // 当前关键词的订单数
                        val keywordOrder = d.cells[KEY_20]?.toFloatOrNull()?.toInt() ?: 0
                        val cells = d.cells

                        // 计算真实acos
                        val realAcos = if (price > 0 && keywordOrder > 0) {
                            (keywordCost / (keywordOrder * price))
                        } else {
                            0.0f
                        }
                        cells[KEY_101] = String.format("%.2f", realAcos * 100) + "%"
                        cells[KEY_106] = when (bid) {
                            NO_BASE_BID -> NO_BASE_BID.toString()
                            NO_AD_ACTIVITY -> NO_AD_ACTIVITY.toString()
                            else -> String.format("%.2f", bid)
                        }
                        cells[KEY_107] = String.format("%.2f", price)
                        // 添加到输出数据
                        outputDatas.add(RowData(cells))
                    }

                    // 计算平均CPA
                    val cpa = if (sumOrders > 0) {
                        sumCost / sumOrders
                    } else {
                        0.0f
                    }

                    // 计算平均CPC
                    val cpc = if (sumClick > 0) {
                        sumCost / sumClick
                    } else {
                        0f
                    }

                    // 计算平均acos
                    val averageAcos = if (sumOrders > 0 && price > 0) {
                        sumCost / (sumOrders * price)
                    } else {
                        0f
                    }

                    // 基础点击量
                    val baseClick = if (cpa > 0) {
                        (cpa / cpc).roundToInt().toFloat()
                    } else {
                        0f
                    }
                    outputDatas.forEach {
                        it.cells[KEY_103] = String.format("%.2f", cpa)
                        it.cells[KEY_104] = String.format("%.2f", cpc)
                        it.cells[KEY_102] = String.format("%.2f", baseClick)
                        it.cells[KEY_105] = String.format("%.2f", averageAcos * 100) + "%"
                    }

                    // 加一行
                    outputDatas.add(RowData().apply {
                        val cpcStr = String.format("%.2f", cpc)
                        val cpaStr = String.format("%.2f", cpa)
                        val acosStr = String.format("%.2f", averageAcos * 100) + "%"
                        val clickStr = String.format("%.2f", baseClick)
                        cells[KEY_1] =
                            "汇总: 平均CPC: $cpcStr, 平均CPA: $cpaStr, 平均ACoS: $acosStr, 基础点击: $clickStr"
                    })

                    // 添加到输出数据
                    outputGroups[activity] = outputDatas
                }

                // 处理分组数据
                outputGroups.forEach { activity, datas ->
                    val outdatas = datas.filter {
                        // 过滤掉花费等于0的数据，汇总数据要保留
                        val notEmpty =
                            !it.cells[KEY_14].isNullOrEmpty() && (it.cells[KEY_14]?.toFloatOrNull() ?: 0f) > 0f
                        notEmpty || it.cells[KEY_1]?.startsWith("汇总") == true
                    }.sortedByDescending {
                        if (it.cells[KEY_1]?.startsWith("汇总") == true) {
                            // 汇总行放在最后
                            -1f
                        } else {
                            // 按花费排序
                            it.cells[KEY_14]?.toFloatOrNull() ?: 0f
                        }
                    }
                    outdatas.forEachIndexed { index, data ->
                        if (data.cells[KEY_1]?.startsWith("汇总") == true) {
                            return@forEachIndexed
                        }
                        val baseBid = when {
                            data.cells[KEY_106] == NO_AD_ACTIVITY.toString() -> NO_AD_ACTIVITY
                            data.cells[KEY_106] == NO_BASE_BID.toString() -> NO_BASE_BID
                            else -> data.cells[KEY_106]?.toFloatOrNull() ?: 0f
                        }
                        val click = data.cells[KEY_11]?.toFloatOrNull()?.toInt() ?: 0
                        if (click >= 20) {
                            // 1，以ACOS作为计算依据，先算出某一个广告活动的平均ACOS（广告费/广告的订单销售额）=总广告费/(总广告订单*产品单价）
                            // 2，算出每个关键词的真实ACOS:关键词的广告花费/广告订单*产品单价
                            // 3，用每个关键词的真实ACOS和平均ACOS做对比
                            // 4，如果小于平均ACOS，则每小3%算一个阶梯，在基础竞价上加价（当基础竞价小于等于0.5时，加0.01；0.6-1.2是0.02；1.2-1.4是0.03；1.5及以上是0.05）
                            // 5，如果大于平均ACOS，则每大3%算一个阶梯，在基础竞价上减价
                            val realAcos = data.cells[KEY_101]?.removeSuffix("%")?.toFloatOrNull() ?: 0f
                            val avgAcos = data.cells[KEY_105]?.removeSuffix("%")?.toFloatOrNull() ?: 0f

                            val diff = realAcos - avgAcos
                            // 有多少个 3% 的
                            val steps = (diff / 3f).roundToInt()
                            when {
                                baseBid == NO_AD_ACTIVITY -> data.cells[KEY_100] = NO_AD_ACTIVITY.toString()
                                baseBid == NO_BASE_BID -> data.cells[KEY_100] = NO_BASE_BID.toString()
                                baseBid <= 0.5f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.01f)
                                }

                                baseBid > 0.5f && baseBid <= 1.2f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.02f)
                                }

                                baseBid > 1.2f && baseBid <= 1.4f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.03f)
                                }

                                baseBid > 1.4f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.05f)
                                }
                            }
                        } else {
                            // 当点击量小于20时，按以下逻辑
                            // 1，用CPA/CPC算出基础点击量-即出一单需要几次点击
                            // 2，用每个关键词的实际点击和基础点击量做对比
                            // 3，用订单量*基础点击量，当大于实际点击时，在基础竞价上加价。[（订单量*基础点击量-实际点击）/3]*(范围 0.01-0.05)
                            // 4，用订单量*基础点击量，当小于实际点击时，在基础竞价上减价。[（订单量*基础点击量-实际点击）/3]*(范围0.01-0.05)
                            val realClick = data.cells[KEY_11]?.toFloatOrNull()?.toInt() ?: 0
                            val baseClick = data.cells[KEY_102]?.toFloatOrNull() ?: 0f
                            val targetClick = (data.cells[KEY_20]?.toFloatOrNull()?.toInt() ?: 0) * baseClick

                            // 有多少个 3% 的
                            val steps = ((realClick - targetClick) / 3f).roundToInt()
                            when {
                                baseBid == NO_AD_ACTIVITY -> data.cells[KEY_100] = NO_AD_ACTIVITY.toString()
                                baseBid == NO_BASE_BID -> data.cells[KEY_100] = NO_BASE_BID.toString()
                                baseBid <= 0.5f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.01f)
                                }

                                baseBid > 0.5f && baseBid <= 1.2f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.02f)
                                }

                                baseBid > 1.2f && baseBid <= 1.4f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.03f)
                                }

                                baseBid > 1.4f -> {
                                    data.cells[KEY_100] = String.format("%.2f", baseBid - steps * 0.05f)
                                }
                            }
                        }
                    }
                    outputGroups[activity] = outdatas
                }

                return@withContext outputGroups
            }
            throw Exception("无法读取数据文件: ${_uiState.value.dataFile?.absolutePath}")
        }
    }

}
