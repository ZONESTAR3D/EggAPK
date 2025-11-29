package com.example.mediaconvert

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun UploadScreen(
    onVideoClick: () -> Unit,
    onImageClick: () -> Unit,
    onAudioClick: () -> Unit,
    onQrScanClick: () -> Unit,
    ip: String,
    onIpChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. IP 输入 + 相机图标
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ip,
                onValueChange = onIpChange,
                label = { Text("ESP32 IP") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onQrScanClick) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "扫码")
            }
        }
        Spacer(Modifier.height(32.dp))
        // 2. 三个大按钮
        Button(onClick = onVideoClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.VideoFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("上传视频")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onImageClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("上传图片")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAudioClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AudioFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("上传音乐")
        }
    }
}