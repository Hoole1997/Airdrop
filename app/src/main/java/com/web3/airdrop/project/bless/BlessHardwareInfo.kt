package com.web3.airdrop.project.bless

import kotlin.random.Random
import java.security.MessageDigest
import java.util.*

object BlessHardwareInfo {

    data class Node(val nodeId: String, val hardwareId: String, val proxy: String?)
    data class User(val usertoken: String, val nodes: List<Node>)
    data class HardwareInfo(
        val cpuArchitecture: String,
        val cpuModel: String,
        val cpuFeatures: List<String>,
        val numOfProcessors: Int,
        val totalMemory: Long
    )

    fun generateRandomHardwareInfo(): HardwareInfo {
        val cpuModels = BlessCpuData.cpuModels()
        val cpuFeatures = listOf("mmx", "sse", "sse2", "sse3", "ssse3", "sse4_1", "sse4_2", "avx")
        return HardwareInfo(
            cpuArchitecture = "x86_64",
            cpuModel = cpuModels.random(),
            cpuFeatures = cpuFeatures.shuffled().take(Random.nextInt(1, cpuFeatures.size + 1)),
            numOfProcessors = Random.nextInt(6, 18),
            totalMemory = Random.nextLong(8L * 1024 * 1024 * 1024, 129L * 1024 * 1024 * 1024)
        )
    }

    fun getRandomHardwareIdentifier(): String {
        return Random.nextBytes(32).joinToString("") { "%02x".format(it) }
    }

    fun getHardwareIdentifierFromNodeId(): String {
        val cpuArchitecture = "x64"
        val cpuModel = getRandomCpuModel()
        val numOfProcessors = 4
        val totalMemory = 8L * 1024 * 1024 * 1024

        val cpuInfo = mapOf(
            "cpuArchitecture" to cpuArchitecture,
            "cpuModel" to cpuModel,
            "numOfProcessors" to numOfProcessors,
            "totalMemory" to totalMemory
        )

        return Base64.getEncoder().encodeToString(cpuInfo.toString().toByteArray())
    }

    fun generateDeviceIdentifier(hardwareIdentifier: String): String {
        val deviceInfo = """{"hardware":"$hardwareIdentifier"}"""
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(deviceInfo.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun generatePubKey(length: Int = 52): String {
        val prefix = "12D3KooW"
        val remainingLength = length - prefix.length
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val remainingChars = (1..remainingLength)
            .map { characters.random() }
            .joinToString("")

        return prefix + remainingChars
    }

    fun getRandomCpuModel(): String {
        val models = listOf("Intel(R) Core(TM) i7-10700K CPU @ 3.80GHz", "AMD Ryzen 9 5950X 16-Core Processor", "Apple M1")
        return models.random()
    }

}