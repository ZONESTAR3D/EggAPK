package com.example.mediaconvert

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object TcpDetect {
    suspend fun probe(host: String, port: Int = 80, timeout: Int = 3000): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeout)
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
}