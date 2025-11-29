package com.example.mediaconvert

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onModelChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val prefs = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var selectedModel by remember {
        mutableStateOf(
            prefs.getString("product_model", "EGG50") ?: "EGG50"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                /* 1. 产品型号下拉菜单 */
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("产品型号") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("EGG50", "EGG60", "EGG70").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    selectedModel = it
                                    expanded = false
                                    prefs.edit().putString("product_model", it).apply()
                                    onModelChange(it)
                                }
                            )
                        }
                    }
                }

                /* 2. 语言切换（从主界面搬过来） */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onLanguageChange("zh") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text("简体中文")
                    }
                    Button(
                        onClick = { onLanguageChange("en") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text("English")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}