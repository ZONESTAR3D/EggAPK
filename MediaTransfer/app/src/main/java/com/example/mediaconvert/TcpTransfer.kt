package com.example.mediaconvert

import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.Socket

object TcpTransfer {
    fun send(file: File, host: String, port: Int = 80, customFileName: String? = null, onProgress: ((String) -> Unit)? = null): Boolean {
        return try {
            val fileNameToSend = customFileName ?: file.name
            
            onProgress?.invoke("TCP传输: 检查文件 ${file.name}")
            onProgress?.invoke("文件存在: ${file.exists()}, 大小: ${file.length()} bytes")
            
            if (!file.exists()) {
                onProgress?.invoke("错误: 文件不存在")
                return false
            }
            
            Socket(host, port).use { socket ->
                val output: OutputStream = socket.getOutputStream()
                val fileInput = FileInputStream(file)

                onProgress?.invoke("连接成功，开始传输文件")
                onProgress?.invoke("发送文件名: $fileNameToSend")
                onProgress?.invoke("文件大小: ${file.length()} bytes")

                // 发送文件名（固定64字节）
                val fileNameBytes = fileNameToSend.toByteArray(Charsets.UTF_8)
                val fileNamePadded = ByteArray(64)
                System.arraycopy(fileNameBytes, 0, fileNamePadded, 0, fileNameBytes.size.coerceAtMost(63))
                output.write(fileNamePadded)

                // 发送文件大小（4字节）
                val fileSize = file.length().toInt()
                output.write((fileSize shr 24) and 0xFF)
                output.write((fileSize shr 16) and 0xFF)
                output.write((fileSize shr 8) and 0xFF)
                output.write(fileSize and 0xFF)

                // 发送文件内容
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalSent = 0

                while (fileInput.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalSent += bytesRead
                    val progress = (totalSent * 100 / fileSize)
                    onProgress?.invoke("进度: $progress% ($totalSent/$fileSize bytes)")
                }

                fileInput.close()
                output.flush()
                
                onProgress?.invoke("✓ 所有数据已发送完成")
                
                // 读取服务器响应
                try {
                    socket.soTimeout = 5000
                    val response = socket.getInputStream().bufferedReader().readLine()
                    onProgress?.invoke("服务器响应: $response")
                } catch (e: Exception) {
                    onProgress?.invoke("未收到服务器响应")
                }
                
                true
            }
        } catch (e: Exception) {
            onProgress?.invoke("传输错误: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}