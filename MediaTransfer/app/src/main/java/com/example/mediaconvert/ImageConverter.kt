package com.example.mediaconvert
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageConverter {
    var onDebug: ((String) -> Unit)? = null

    fun convert(input: File): File? {
        return try {
            addDebugMessage("ImageConverter: 开始转换图片")
            addDebugMessage("ImageConverter: 输入文件 - ${input.name}, 大小: ${input.length()} bytes")

            val bmp = BitmapFactory.decodeFile(input.absolutePath)
            if (bmp == null) {
                addDebugMessage("ImageConverter: ✗ 无法解码位图")
                return null
            }

            addDebugMessage("ImageConverter: 原始图片尺寸 - ${bmp.width}x${bmp.height}")

            val out = File(input.parent, "${input.nameWithoutExtension}_360.png")
            addDebugMessage("ImageConverter: 输出文件路径 - ${out.absolutePath}")

            FileOutputStream(out).use { outputStream ->
                val scaledBitmap = Bitmap.createScaledBitmap(bmp, 360, 360, true)
                addDebugMessage("ImageConverter: 缩放后尺寸 - ${scaledBitmap.width}x${scaledBitmap.height}")

                val success = scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                addDebugMessage("ImageConverter: 压缩结果 - $success")

                if (success) {
                    addDebugMessage("ImageConverter: ✓ PNG文件创建成功")
                } else {
                    addDebugMessage("ImageConverter: ✗ PNG文件创建失败")
                }
            }

            // 检查输出文件
            if (out.exists()) {
                addDebugMessage("ImageConverter: 输出文件存在, 大小: ${out.length()} bytes")
            } else {
                addDebugMessage("ImageConverter: ✗ 输出文件不存在")
            }

            out
        } catch (e: Exception) {
            addDebugMessage("ImageConverter: 转换异常 - ${e.message}")
            null
        }
    }

    // 需要在 MainActivity 中添加这个静态方法
    private fun addDebugMessage(message: String) {
        // 这里需要从 MainActivity 调用，或者通过其他方式传递
    }
}
