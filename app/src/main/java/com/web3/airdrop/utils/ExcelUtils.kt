package com.web3.airdrop.utils

import com.blankj.utilcode.util.LogUtils
import com.web3.airdrop.data.Wallet
import jxl.Workbook
import jxl.read.biff.BiffException
import java.io.IOException
import java.io.InputStream

object ExcelUtils {

    fun parseExcel(inputStream: InputStream): List<Wallet> {
        val walletInfos = mutableListOf<Wallet>()
        try {
            val workbook = Workbook.getWorkbook(inputStream)
            val sheet = workbook.getSheet(0) // 获取第一个工作表

            // 假设第一行是表头，从第二行开始读取
            for (row in 1 until sheet.rows) {
                val address = sheet.getCell(0, row).contents
                val privateKey = sheet.getCell(1, row).contents
                val proxy = sheet.getCell(3, row).contents
                val mailTm = sheet.getCell(7, row).contents
                LogUtils.d("address=$address  privateKey=$privateKey proxy=$proxy")
                walletInfos.add(Wallet(0,"ETH",address,privateKey,"P$row",proxy))
            }
            inputStream.close()
            workbook.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: BiffException) {
            e.printStackTrace()
        }

        return walletInfos
    }
}
