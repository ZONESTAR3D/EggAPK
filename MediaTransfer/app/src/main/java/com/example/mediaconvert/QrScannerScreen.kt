package com.example.mediaconvert

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment  // 添加这个导入
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.core.content.ContextCompat
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Card  // 添加这个导入
import androidx.compose.material3.CardDefaults  // 添加这个导入
import androidx.compose.material3.Text  // 添加这个导入

@Composable
fun QrScannerScreen(onQrCode: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val scannerWidthDp = (screenWidthDp * 0.8f).toInt()
    val scannerHeightDp = scannerWidthDp

    val (scannerLeftPx, scannerTopPx, scannerWidthPx, scannerHeightPx) = remember {
        val screenWidthPx = (screenWidthDp * density.density).toInt()
        val scannerWidthPx = (scannerWidthDp * density.density).toInt()
        val scannerHeightPx = (scannerHeightDp * density.density).toInt()
        val leftPx = (screenWidthPx - scannerWidthPx) / 2
        val topPx = (120 * density.density).toInt()

        Quad(leftPx, topPx, scannerWidthPx, scannerHeightPx)
    }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var scanStatus by remember { mutableStateOf("请对准二维码") }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PreviewView(it).apply { previewView = this } },
            modifier = Modifier.fillMaxSize()
        )

        // 画半透明扫描框
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(scannerLeftPx.toFloat(), scannerTopPx.toFloat()),
                size = androidx.compose.ui.geometry.Size(scannerWidthPx.toFloat(), scannerHeightPx.toFloat()),
                blendMode = BlendMode.Clear
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(scannerLeftPx.toFloat(), scannerTopPx.toFloat()),
                size = androidx.compose.ui.geometry.Size(scannerWidthPx.toFloat(), scannerHeightPx.toFloat()),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 扫描状态显示
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
            ) {
                Text(
                    text = scanStatus,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
        }
    }

    LaunchedEffect(previewView) {
        previewView ?: return@LaunchedEffect
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView?.surfaceProvider)
        }
        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(android.util.Size(scannerWidthPx, scannerHeightPx))
            .build()

        analyzer.setAnalyzer(ContextCompat.getMainExecutor(context),
            object : ImageAnalysis.Analyzer {
                private val scanner = BarcodeScanning.getClient()

                override fun analyze(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image ?: return
                    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { ip ->
                                if (isValidIpAddress(ip)) {
                                    scanStatus = "✓ 扫描成功: $ip"
                                    onQrCode(ip)
                                } else {
                                    scanStatus = "✗ 无效IP格式: $ip"
                                }
                            }
                            if (barcodes.isEmpty()) {
                                scanStatus = "请对准二维码"
                            }
                        }
                        .addOnFailureListener {
                            scanStatus = "扫描失败，请重试"
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }

                private fun isValidIpAddress(ip: String): Boolean {
                    return ip.matches(Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$"))
                }
            }
        )

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer
        )
    }
}

// 简单的数据类来存储矩形信息
private data class Quad(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)