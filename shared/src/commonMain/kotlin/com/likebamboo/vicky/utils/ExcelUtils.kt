package com.likebamboo.vicky.utils

import org.apache.commons.io.FileUtils
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.*
import java.io.*


object ExcelUtils {

    /**
     * 超链接的样式
     */
    var linkstyles = mutableMapOf<XSSFSheet?, XSSFCellStyle?>()

    fun createSingleSheetExcel(title: Array<String>?): XSSFWorkbook {
        // 创建excel工作簿
        val workbook = XSSFWorkbook()
        // 创建工作表sheet
        val sheet = workbook.createSheet()
        if (title != null && title.isNotEmpty()) {
            // 创建第一行
            val row = sheet.createRow(0)
            // 插入第一行数据的表头
            for (i in title.indices) {
                val cell = row.createCell(i)
                cell.setCellValue(title[i])
            }
        }
        return workbook
    }

    fun createExcelOnly(): XSSFWorkbook {
        // 创建excel工作簿
        return XSSFWorkbook()
    }

    fun createSheet(workbook: XSSFWorkbook, sheetName: String, title: Array<String>?): XSSFSheet {
        // 创建工作表sheet
        val sheet = workbook.createSheet(sheetName)
        if (title != null && title.isNotEmpty()) {
            // 创建第一行
            val row = sheet.createRow(0)
            // 插入第一行数据的表头
            for (i in title.indices) {
                val cell = row.createCell(i)
                cell.setCellValue(title[i])
            }
        }
        return sheet
    }


    /**
     * 插入一条数据
     *
     * @param workbook workbook
     * @param data     data
     */
    fun insertRow(
        workbook: XSSFWorkbook,
        data: Array<Any?>,
        sheetName: String? = null,
        styleProvider: ((Int, XSSFCell, Number)-> XSSFCellStyle?)? = null
    ) {
        val sheetCount = workbook.numberOfSheets
        if (sheetCount <= 0) {
            return
        }

        val sheet = workbook.getSheetAt(
            if (!sheetName.isNullOrEmpty()) {
                workbook.getSheetIndex(sheetName)
            } else {
                0
            }
        )
        val linkStyle = getLinkStyle(workbook, sheet)
        if (data.isNotEmpty()) {
            val num = sheet.lastRowNum + 1
            // 创建一行
            val row = sheet.createRow(num)
            // 插入数据
            for (i in data.indices) {
                if (data[i] == null) {
                    continue
                }

                val cell = row.createCell(i)
                if (data[i] is Number) {
                    cell.cellType = CellType.NUMERIC
                    val value = (data[i] as Number).toDouble()
                    cell.setCellValue(value)
                    val style = styleProvider?.invoke(i, cell, value)
                    if (style != null) {
                        cell.cellStyle = style
                    }
                } else {
                    val str = data[i].toString()
                    cell.setCellValue(str)
                    if (str.startsWith("http://") || str.startsWith("https://")) {
                        val link = workbook.creationHelper.createHyperlink(HyperlinkType.URL)
                        link.address = str
                        cell.hyperlink = link as XSSFHyperlink
                        cell.cellStyle = linkStyle
                    }
                }
            }
        }
    }

    /**
     * 插入一条数据
     *
     * @param workbook workbook
     * @param msg      msg
     */
    fun insertSingleRow(workbook: XSSFWorkbook, msg: String, range: Int, sheetName: String? = null) {
        val sheetCount = workbook.numberOfSheets
        if (sheetCount <= 0) {
            return
        }

        val sheet = workbook.getSheetAt(
            if (!sheetName.isNullOrEmpty()) {
                workbook.getSheetIndex(sheetName)
            } else {
                0
            }
        )
        val linkStyle = getLinkStyle(workbook, sheet)
        val num = sheet.lastRowNum + 1
        // 创建一行
        val row = sheet.createRow(num)
        val cell = row.createCell(0)
        cell.setCellValue(msg)
        if (msg.startsWith("http://") || msg.startsWith("https://")) {
            val link = workbook.creationHelper.createHyperlink(HyperlinkType.URL)
            link.address = msg
            cell.hyperlink = link as XSSFHyperlink
            cell.cellStyle = linkStyle
        }
        // 合并单元格,合并后的内容取决于合并区域的左上角单元格的值
        val region = CellRangeAddress(num, num + 1, 0, range)
        sheet.addMergedRegion(region)
        cell.cellStyle.alignment = HorizontalAlignment.CENTER
        cell.cellStyle.verticalAlignment = VerticalAlignment.CENTER
        cell.cellStyle.fillBackgroundColor = HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.index
        // 为合并后的单元格添加边框线
        addMergeCellBorder(region, sheet)
    }

    /**
     * 插入一条数据
     *
     * @param workbook workbook
     * @param msg      msg
     */
    fun insertMergeCell(
        workbook: XSSFWorkbook, msg: String, rowIdx: Int, cellIndex: Int, range: Int, sheetName: String? = null,
        autoWrap: Boolean = false
    ) {
        val sheetCount = workbook.numberOfSheets
        if (sheetCount <= 0) {
            return
        }

        val sheet = workbook.getSheetAt(
            if (!sheetName.isNullOrEmpty()) {
                workbook.getSheetIndex(sheetName)
            } else {
                0
            }
        )

        val linkStyle = getLinkStyle(workbook, sheet)
        val row = if (sheet.getRow(rowIdx) == null) {
            sheet.createRow(rowIdx)
        } else {
            sheet.getRow(rowIdx)
        }
        val cell = if (row.getCell(cellIndex) == null) {
            row.createCell(cellIndex)
        } else {
            row.getCell(cellIndex)
        }
        cell.setCellValue(msg)
        if (msg.startsWith("http://") || msg.startsWith("https://")) {
            val link = workbook.creationHelper.createHyperlink(HyperlinkType.URL)
            link.address = msg
            cell.hyperlink = link as XSSFHyperlink
            cell.cellStyle = linkStyle
        }
        if (autoWrap) {
            cell.cellStyle.wrapText = true
        }
        if (range == 0) {
            return
        }
        // 合并单元格,合并后的内容取决于合并区域的左上角单元格的值
        val region = CellRangeAddress(rowIdx, rowIdx, cellIndex, cellIndex + range)
        sheet.addMergedRegion(region)
        cell.cellStyle.alignment = HorizontalAlignment.CENTER
        cell.cellStyle.verticalAlignment = VerticalAlignment.CENTER
        // 为合并后的单元格添加边框线
        addMergeCellBorder(region, sheet)
    }

    /**
     * 为合并后的单元格添加边框线
     * @param cra 合并区域
     * @param sheet sheet
     */
    private fun addMergeCellBorder(cra: CellRangeAddress, sheet: XSSFSheet) {
        RegionUtil.setBorderTop(BorderStyle.THIN, cra, sheet)
        RegionUtil.setBorderBottom(BorderStyle.THIN, cra, sheet)
        RegionUtil.setBorderLeft(BorderStyle.THIN, cra, sheet)
        RegionUtil.setBorderRight(BorderStyle.THIN, cra, sheet)
    }


    /**
     * 链接类型的样式
     */
    private fun getLinkStyle(workbook: XSSFWorkbook, sheet: XSSFSheet?): XSSFCellStyle {
        var style = linkstyles[sheet]
        if (style != null) {
            return style
        }
        style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.underline = XSSFFont.U_SINGLE
        font.color = HSSFColor.HSSFColorPredefined.BLUE.index
        style.setFont(font)
        linkstyles[sheet] = style
        return style
    }

    /**
     * 删除一条数据
     *
     * @param workbook workbook
     * @param data     data
     */
    fun deleteRow(workbook: XSSFWorkbook, data: Array<Any>?, sheetName: String? = null) {
        val sheetCount = workbook.numberOfSheets
        if (sheetCount <= 0) {
            return
        }
        val sheet = workbook.getSheetAt(
            if (!sheetName.isNullOrEmpty()) {
                workbook.getSheetIndex(sheetName)
            } else {
                0
            }
        )
        if (data != null && data.isNotEmpty()) {
            val num = sheet.lastRowNum
            for (i in 0..num) {
                val row = sheet.getRow(i) ?: continue
                val cellNum = row.lastCellNum.toInt()
                var j = 0
                while (j <= cellNum) {
                    if (j >= data.size) {
                        // 清空该行数据
                        sheet.removeRow(row)
                        break
                    }
                    val cell = row.getCell(j)
                    if (cell == null) {
                        j++
                        continue
                    }
                    val cv = cell.stringCellValue
                    if (cv == null || cv != data[j].toString()) {
                        break
                    }
                    j++
                }
            }
        }
    }

    /**
     * 保存文件
     *
     * @param workbook workbook
     * @param fileName 文件名称
     * @return
     */
    fun saveExcelToFile(workbook: XSSFWorkbook, fileName: String?): File {
        // 移除空行
        deleteEmptyRow(workbook)
        // 创建excel文件
        val file = File(fileName)
        try {
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            // 将excel写入
            val stream: FileOutputStream = FileUtils.openOutputStream(file)
            workbook.write(stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 读取excel文件
     *
     * @param filePath 文件路径
     * @return
     */
    fun readExcelFile(filePath: String?): XSSFWorkbook? {
        var fis: InputStream? = null
        try {
            val input = File(filePath)
            fis = FileInputStream(input)
            return XSSFWorkbook(fis)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * 删除空行
     *
     * @param workbook book
     */
    private fun deleteEmptyRow(workbook: XSSFWorkbook) {
        val sheetCount = workbook.numberOfSheets
        if (sheetCount <= 0) {
            return
        }
        for (index in 0 until sheetCount) {
            val sheet = workbook.getSheetAt(index)
            var i = sheet.lastRowNum
            var tempRow: XSSFRow?
            while (i > 0) {
                i--
                tempRow = sheet.getRow(i)
                if (tempRow == null) {
                    sheet.shiftRows(i + 1, sheet.lastRowNum, -1)
                }
            }
        }
    }

}