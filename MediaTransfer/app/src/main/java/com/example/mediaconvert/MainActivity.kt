package com.example.mediaconvert

import android.content.Context
import androidx.core.app.ActivityOptionsCompat
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {
    enum class UiState { IDLE, CONNECTING, CONNECT_OK, CONNECT_FAIL, CONVERTING, CONVERT_OK, CONVERT_FAIL,
        TRANSFERRING, TRANSFER_OK, TRANSFER_FAIL, UNKNOW_FILE, VIDEO_TOOLONG, EXCEPTION_ERROR}

    private val uiState = mutableStateOf(UiState.IDLE)
    private val productModel = mutableStateOf("EGG50")

    private lateinit var cacheDirPath: String
    private val debugMessages = mutableStateListOf<String>() // 改为列表存储多行信息

    private val isUploading = mutableStateOf(false)

    private val transferProgress = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedLanguage()
        super.onCreate(savedInstanceState)
        cacheDirPath = cacheDir.absolutePath
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        productModel.value = prefs.getString("product_model", "EGG50") ?: "EGG50"

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
            var espIp by remember { mutableStateOf("192.168.1.100") }
            val uploading by remember { isUploading }
            Box(Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.Main -> MainContent(
                        ip = espIp,
                        onIpChange = { espIp = it },
                        onQrScanClick = {
                            val hasCamera =
                                packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                            if (!hasCamera) {
                                addDebugMessage("本设备无摄像头，请手动输入 IP")
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.CameraNoFind),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@MainContent
                            }
                            currentScreen = Screen.QrScanner
                        }
                    )

                    Screen.QrScanner -> QrScannerScreen(
                        onQrCode = { ip ->
                            espIp = ip
                            currentScreen = Screen.Main
                            addDebugMessage("扫描到IP: $ip")
                            addDebugMessage("正在测试连接...")

                            lifecycleScope.launch {
                                uiState.value = UiState.CONNECTING
                                val success = TcpDetect.probe(ip)
                                if (success) {
                                    uiState.value = UiState.CONNECT_OK
                                    addDebugMessage("连接测试: ✓ 成功")
                                } else {
                                    uiState.value = UiState.CONNECT_FAIL
                                    addDebugMessage("连接测试: ✗ 失败")
                                    addDebugMessage("请检查设备服务是否运行")
                                }
                            }
                        },
                        onBack = {
                            currentScreen = Screen.Main
                            addDebugMessage("已返回主界面")
                        }
                    )
                }

                if (uploading) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    private fun applySavedLanguage() {
        try {
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val savedLanguage = prefs.getString("app_language", "system") ?: "system"

            if (savedLanguage != "system") {
                setAppLanguage(savedLanguage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAppLanguage(languageCode: String) {
        try {
            val resources = resources
            val configuration = resources.configuration
            val locale = when (languageCode) {
                "zh" -> Locale("zh", "CN")
                "en" -> Locale.ENGLISH
                else -> Locale.getDefault()
            }

            Locale.setDefault(locale)
            configuration.setLocale(locale)

            // 更新资源配置
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeAppLanguage(languageCode: String) {
        try {
            // 保存语言设置
            getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putString("app_language", languageCode)
                .apply()

            // 立即应用新语言
            setAppLanguage(languageCode)

            // 重启 Activity
            recreate()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "语言切换失败", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun stateText(state: UiState): String = when (state) {
        UiState.IDLE            -> stringResource(R.string.status_idle)
        UiState.CONNECTING      -> stringResource(R.string.status_connecting)
        UiState.CONNECT_OK      -> stringResource(R.string.status_connect_ok)
        UiState.CONNECT_FAIL    -> stringResource(R.string.status_connect_fail)
        UiState.CONVERTING      -> stringResource(R.string.status_converting)
        UiState.CONVERT_OK      -> stringResource(R.string.status_convert_ok)
        UiState.CONVERT_FAIL    -> stringResource(R.string.status_convert_fail)
        UiState.TRANSFERRING    -> stringResource(R.string.status_transferring) + " ${transferProgress.value}%"
        UiState.TRANSFER_OK     -> stringResource(R.string.status_transfer_ok)
        UiState.TRANSFER_FAIL   -> stringResource(R.string.status_transfer_fail)
        UiState.UNKNOW_FILE     -> stringResource(R.string.status_unkwon_file)
        UiState.VIDEO_TOOLONG   -> stringResource(R.string.status_video_toolong)
        UiState.EXCEPTION_ERROR -> stringResource(R.string.status_exception_error)
        else                    -> stringResource(R.string.status_unkwon_state)
    }

    private fun addDebugMessage(message: String) {
        lifecycleScope.launch {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val fullMessage = "[$timestamp] $message"
            
            debugMessages.add(fullMessage)
            
            // 限制最多显示10行
            if (debugMessages.size > 10) {
                debugMessages.removeAt(0)
            }
        }
    }

    // 清除调试信息的方法
    private fun clearDebugMessages() {
        lifecycleScope.launch {
            debugMessages.clear()
            addDebugMessage("调试信息已清除")
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun MainContent(
        ip: String,
        onIpChange: (String) -> Unit,
        onQrScanClick: () -> Unit
    ) {
        val context = LocalContext.current
        val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
        val storagePermission = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        var isDetecting by remember { mutableStateOf(false) }
        var detectResult by remember { mutableStateOf<Boolean?>(null) }
        var showSettings by remember { mutableStateOf(false) }

        // 权限申请
        LaunchedEffect(Unit) {
            cameraPermission.launchPermissionRequest()
            storagePermission.launchPermissionRequest()
        }

        // launcher
        val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                clearDebugMessages()
                addDebugMessage("已选择视频文件，开始处理...")
                lifecycleScope.launch {
                    processFileWithProgress(it, "video", ip)
                }
            }
        }
        val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                clearDebugMessages()
                addDebugMessage("已选择图片文件，开始处理...")
                lifecycleScope.launch {
                    processFileWithProgress(it, "image", ip)
                }
            }
        }
        val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                clearDebugMessages()
                addDebugMessage("已选择音频文件，开始处理...")
                lifecycleScope.launch {
                    processFileWithProgress(it, "audio", ip)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和清除按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.title), style = MaterialTheme.typography.headlineSmall)
                Row {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.btn_settings))
                    }
                }
            }

            // IP输入行
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = {
                        onIpChange(it)
                        addDebugMessage("IP已更新: $it")
                    },
                    label = { Text(stringResource(R.string.IP_ADDRESS)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onQrScanClick) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "二维码")
                }
                IconButton(
                    onClick = {
                        clearDebugMessages()
                        isDetecting = true
                        addDebugMessage("正在测试连接: $ip")
                        lifecycleScope.launch {
                            uiState.value = UiState.CONNECTING
                            detectResult = TcpDetect.probe(ip)
                            isDetecting = false
                            if (detectResult == true) {
                                uiState.value = UiState.CONNECT_OK
                                addDebugMessage("连接成功: $ip")
                            } else {
                                uiState.value = UiState.CONNECT_FAIL
                                addDebugMessage("连接失败: $ip")
                                addDebugMessage("可能原因: 服务未运行/端口错误/网络不通")
                            }
                            Toast.makeText(
                                context,
                                if (detectResult == true) "连接成功" else "连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = !isDetecting,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = "检测连接")
                    }
                }
            }

            // 按钮区域
            Button( onClick = {
                clearDebugMessages()
                videoLauncher.launch("video/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)) {
                Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(18.dp))
                Text(stringResource(R.string.btn_video), style = MaterialTheme.typography.titleMedium)
            }
            Button( onClick = {
                    clearDebugMessages()
                    imageLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(18.dp))
                        Text(stringResource(R.string.btn_image), style = MaterialTheme.typography.titleMedium)
                }
            val isEgg50 = productModel.value == "EGG50"
            Button( onClick = { clearDebugMessages()
                audioLauncher.launch("audio/*") },
                enabled = !isEgg50,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEgg50) Color.Gray else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)) {
                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(18.dp))
                Text(stringResource(R.string.btn_audio), style = MaterialTheme.typography.titleMedium)
            }

            // 状态显示
            Text(stringResource(R.string.status_label) + "${stateText(uiState.value)}", style = MaterialTheme.typography.bodyMedium)

            // 调试信息显示区域 - 固定高度，可滚动
            if (BuildConfig.ENABLE_DEBUG_PANEL) {
                if (debugMessages.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Text(
                                "调试信息:",
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 可滚动的调试信息列表 - 修复这里
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                items(debugMessages.size) { index ->
                                    // 反转显示，最新的在最上面
                                    val messageIndex = debugMessages.size - 1 - index
                                    Text(
                                        text = debugMessages[messageIndex],
                                        style = MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.ui.graphics.Color.DarkGray,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
//                IconButton(
//                    onClick = { clearDebugMessages() },
//                    modifier = Modifier.size(48.dp)
//                ) {
//                    Text(
//                        text = "clean",
//                        style  = MaterialTheme.typography.labelMedium,
//                        color  = MaterialTheme.colorScheme.onSurface
//                    )
//                }
                TextButton(onClick = { clearDebugMessages() }) {
                    Text("clean")
                }
            }
        }
        if (showSettings) {
            SettingsDialog(
                onDismiss = { showSettings = false },
                onLanguageChange = { (context as? MainActivity)?.changeAppLanguage(it) },
                onModelChange = { newModel ->
                    (context as? MainActivity)?.productModel?.value = newModel
                }
            )
        }
    }

    private suspend fun processFileWithProgress(
        uri: Uri,
        type: String,
        espIp: String
    ) {
        isUploading.value = true
        try {
            withContext(Dispatchers.IO) {
                ImageConverter.onDebug = { message: String -> addDebugMessage(message) }
                VideoConverter.onDebug = { message: String -> addDebugMessage(message) }
                VideoInfo.onDebug = { message: String -> addDebugMessage(message) }


                addDebugMessage("步骤1: 缓存文件...")
                val cached = cacheUriToFile(uri) ?: run {
                    addDebugMessage("✗ 缓存失败")
                    return@withContext
                }
                addDebugMessage("✓ 缓存成功: ${cached.name}")
                addDebugMessage("文件路径: ${cached.absolutePath}")
                addDebugMessage("文件大小: ${cached.length()} bytes")

                addDebugMessage("步骤2: 正在处理文件...")
                val fileToSend = when (type) {
                    "image" -> {
                        uiState.value = UiState.CONVERTING
                        addDebugMessage("开始图片转换...")
                        addDebugMessage("输入文件: ${cached.name}")

                        val convertedFile = ImageConverter.convert(cached)
                        if (convertedFile != null) {
                            addDebugMessage("✓ 图片转换完成")
                            addDebugMessage("输出文件: ${convertedFile.name}")
                            addDebugMessage("输出大小: ${convertedFile.length()} bytes")
                            addDebugMessage("文件存在: ${convertedFile.exists()}")
                        } else {
                            addDebugMessage("✗ 图片转换失败")
                        }
                        convertedFile
                    }
                    "video" -> {
                        uiState.value = UiState.CONVERTING
                        addDebugMessage("开始视频转换...")
                        addDebugMessage("输入文件: ${cached.name}")
                        addDebugMessage("输入大小: ${cached.length()} bytes")

                        // 检查视频时长
                        addDebugMessage("检查视频时长...")
                        val durationSeconds = VideoInfo.getDurationSeconds(cached)
                        if (durationSeconds < 0) {
                            uiState.value = UiState.UNKNOW_FILE
                            addDebugMessage("✗ 无法获取视频时长")
                            null
                        } else if (durationSeconds > 60) {
                            uiState.value = UiState.VIDEO_TOOLONG
                            addDebugMessage("✗ 视频时长 ${durationSeconds}秒 超过60秒限制")
                            null
                        } else {
                            addDebugMessage("✓ 视频时长: ${durationSeconds}秒")

                            // 开始视频转换
                            addDebugMessage("启动FFmpeg视频转换...")
                            addDebugMessage("目标格式: MJPEG AVI, 360x360, 15fps")

                            val convertedFile = VideoConverter.convert(cached, productModel.value)
                            if (convertedFile != null) {
                                addDebugMessage("✓ 视频转换完成")
                                addDebugMessage("输出文件: ${convertedFile.name}")
                                addDebugMessage("输出大小: ${convertedFile.length()} bytes")
                                addDebugMessage("文件存在: ${convertedFile.exists()}")

                                // 检查转换后的文件是否有效
                                if (convertedFile.length() == 0L) {
                                    uiState.value = UiState.EXCEPTION_ERROR
                                    addDebugMessage("⚠ 警告: 转换后文件大小为0")
                                    return@withContext
                                }
                            } else {
                                uiState.value = UiState.CONVERT_FAIL
                                addDebugMessage("✗ 视频转换失败")
                                addDebugMessage("可能原因: FFmpeg转换错误/格式不支持")
                            }
                            convertedFile
                        }
                    }
                    "audio" -> {
                        addDebugMessage("准备音频文件...")
                        addDebugMessage("输入文件: ${cached.name}")
                        addDebugMessage("输入大小: ${cached.length()} bytes")

                        // 检查音频文件
                        if (cached.length() == 0L) {
                            uiState.value = UiState.EXCEPTION_ERROR
                            addDebugMessage("✗ 音频文件大小为0")
                            null
                        } else {
                            addDebugMessage("✓ 音频文件准备就绪")
                            cached
                        }
                    }
                    else -> {
                        uiState.value = UiState.UNKNOW_FILE
                        addDebugMessage("未知文件类型: $type")
                        null
                    }
                }

                if (fileToSend == null) {
                    delay(1200)
                    uiState.value = UiState.CONVERT_FAIL
                    addDebugMessage("✗ 文件处理失败 - fileToSend is null")
                    return@withContext
                }

                if (!fileToSend.exists()) {
                    delay(1200)
                    uiState.value = UiState.CONVERT_FAIL
                    addDebugMessage("✗ 文件不存在: ${fileToSend.absolutePath}")
                    return@withContext
                }

                if (fileToSend.length() == 0L) {
                    delay(1200)
                    uiState.value = UiState.CONVERT_FAIL
                    addDebugMessage("✗ 文件大小为0")
                    return@withContext
                }

                uiState.value = UiState.TRANSFERRING
                addDebugMessage("步骤3: 正在传输文件到 $espIp...")

                // 生成简单的文件名：类型 + 时间戳
                val timestamp = System.currentTimeMillis()
                val simpleName = when (type) {
                    "image" -> "image_${timestamp}.png"
                    "video" -> "video_${timestamp}.avi"
                    "audio" -> "audio_${timestamp}.mp3"
                    else -> "file_${timestamp}.dat"
                }

                addDebugMessage("传输文件名: $simpleName")
                addDebugMessage("传输文件大小: ${fileToSend.length()} bytes")

                val success = TcpTransfer.send(fileToSend, espIp, 80, simpleName) { progress ->
                    addDebugMessage(progress)
                    val p = progress.substringAfter("进度: ").substringBefore("%").toIntOrNull() ?: 0
                    transferProgress.value = p
                }

                if (success) {
                    uiState.value = UiState.TRANSFER_OK
                    addDebugMessage("✓ 文件传输成功!")
                    addDebugMessage("文件已保存为数字编号格式")

                    // 清理临时文件
                    try {
                        if (cached.exists() && cached != fileToSend) {
                            cached.delete()
                            addDebugMessage("临时文件已清理")
                        }
                        if (fileToSend.exists() && fileToSend != cached) {
                            fileToSend.delete()
                            addDebugMessage("转换文件已清理")
                        }
                    } catch (e: Exception) {
                        addDebugMessage("清理临时文件时出错: ${e.message}")
                    }
                } else {
                    uiState.value = UiState.TRANSFER_FAIL
                    addDebugMessage("✗ 文件传输失败")
                    addDebugMessage("可能原因: 网络连接问题/ESP32服务未运行")
                }
            }
        } catch (e: Exception) {
            addDebugMessage("处理文件时发生错误: ${e.message}")
            e.printStackTrace()
        } finally {
            isUploading.value = false
        }
    }

    private fun cacheUriToFile(uri: Uri): File? {
        return try {
            val input = contentResolver.openInputStream(uri) ?: return null
            
            // 根据文件类型确定扩展名
            val mimeType = contentResolver.getType(uri)
            val extension = when {
                mimeType?.startsWith("video/") == true -> ".mp4"
                mimeType?.startsWith("image/") == true -> ".jpg"
                mimeType?.startsWith("audio/") == true -> ".mp3"
                else -> ".tmp"
            }
            
            val file = File(cacheDirPath, "input_${System.currentTimeMillis()}$extension")
            input.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

sealed class Screen {
    object Main : Screen()
    object QrScanner : Screen()
}