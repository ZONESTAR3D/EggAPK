package com.example.mediaconvert

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.FFprobeKit
import java.io.File

object VideoConverter {
    var onDebug: ((String) -> Unit)? = null

    fun convert(input: File, model: String = "EGG70"): File? {
        return try {
            onDebug?.invoke("VideoConverter: 开始视频转换")
            onDebug?.invoke("VideoConverter: 输入文件 - ${input.name}, 大小: ${input.length()} bytes")

            val isEgg50 = model == "EGG50"
            val out = File(input.parent,
                "${input.nameWithoutExtension}" + (if (isEgg50) "_240_mjpeg.mjpeg" else "_360_mjpeg.avi"))
            onDebug?.invoke("VideoConverter: 输出文件 - ${out.name}")

            val dimensions = VideoInfo.getVideoDimensions(input)
            if (dimensions == null) {
                onDebug?.invoke("VideoConverter: 无法获取视频分辨率，取消转换")
                return null
            }
            val (width, height) = dimensions
            val isPortrait = height > width
            val targetSize = if (isEgg50) 240 else 360
            val crop = if (isPortrait) {
                // 竖屏：高度是短板，先缩高，再居中裁宽
                "crop=w='min(in_w,$targetSize)':h='min(in_h,$targetSize)':x='(in_w-min(in_w,$targetSize))/2':y='(in_h-min(in_h,$targetSize))/2'"
            } else {
                // 横屏：宽度是短板，直接居中裁正方形
                "crop=$targetSize:$targetSize:(in_w-$targetSize)/2:(in_h-$targetSize)/2"
            }
            val scale = if (isPortrait) "scale=-2:$targetSize:flags=lanczos"
            else "scale=-2:$targetSize:flags=lanczos"

            val vf = "fps=15,$scale,$crop"

            val cmd = if (isEgg50) {
                listOf(
                    "-i", input.absolutePath,
                    "-t", "60",
                    "-c:v", "mjpeg",
                    "-vtag", "MJPG",
                    "-pix_fmt", "yuvj420p",
                    "-b:v", "400K",
                    "-q:v", "10",
                    "-vf", vf,
                    "-an",
                    "-y", out.absolutePath
                )
            } else {
                listOf(
                    "-i", input.absolutePath,
                    "-t", "60",
                    "-c:v", "mjpeg",
                    "-vtag", "MJPG",
                    "-pix_fmt", "yuvj420p",
                    "-b:v", "600K",
                    "-q:v", "10",
                    "-vf", vf,
                    "-c:a", "libmp3lame",
                    "-write_xing", "0",
                    //"-id3v2_version", "0",
                    "-ac", "2",
                    "-ar", "44100",
                    "-af", "loudnorm",
                    "-b:a", "128k",
                    "-y", out.absolutePath
                )
            }.toTypedArray()

            onDebug?.invoke("VideoConverter: FFmpeg命令: ${cmd.joinToString(" ")}")
            onDebug?.invoke("VideoConverter: 开始执行FFmpeg...")

            val startTime = System.currentTimeMillis()
            val session  = FFmpegKit.execute(cmd.joinToString(" "))
            val endTime  = System.currentTimeMillis()
            val duration = (endTime - startTime) / 1000.0
            val rc       = session.returnCode

            onDebug?.invoke("VideoConverter: FFmpeg执行完成, 返回码: $rc, 耗时: ${"%.2f".format(duration)}秒")

            if (ReturnCode.isSuccess(rc)) {
                if (out.exists()) {
                    onDebug?.invoke("VideoConverter: ✓ 输出文件存在, 大小: ${out.length()} bytes")
                    out
                } else {
                    onDebug?.invoke("VideoConverter: ✗ 输出文件不存在")
                    null
                }
            } else {
                onDebug?.invoke("VideoConverter: ✗ FFmpeg转换失败, 返回码: $rc")
                onDebug?.invoke("VideoConverter: 可能原因: 输入文件格式不支持/编码器错误")
                null
            }
        } catch (e: Exception) {
            onDebug?.invoke("VideoConverter: 转换异常 - ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

object VideoInfo {
    var onDebug: ((String) -> Unit)? = null

    fun getDurationSeconds(input: File): Long {
        return try {
            val session = FFprobeKit.getMediaInformation(input.absolutePath)
            val durStr = session.mediaInformation.duration ?: return -1
            val seconds = durStr.toDouble().toLong()
            onDebug?.invoke("VideoInfo: ✓ 时长=$seconds 秒")
            seconds
        } catch (e: Exception) {
            onDebug?.invoke("VideoInfo: 异常 - ${e.message}")
            -1
        }
    }

    fun getVideoDimensions(input: File): Pair<Int, Int>? {
        return try {
            val session = FFprobeKit.getMediaInformation(input.absolutePath)
            val stream = session.mediaInformation.streams.firstOrNull { it.width > 0 && it.height > 0 }
            val width = stream?.width?.toInt() ?: return null
            val height = stream?.height?.toInt() ?: return null
            onDebug?.invoke("VideoInfo: 原始分辨率 = ${width}x${height}")
            Pair(width, height)
        } catch (e: Exception) {
            onDebug?.invoke("VideoInfo: 获取分辨率失败 - ${e.message}")
            null
        }
    }
}