package ai.openclaw.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.openclaw.android.OpenClawApplication
import ai.openclaw.android.MainActivity
import ai.openclaw.android.R
import ai.openclaw.android.server.GatewayServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * OpenClaw Gateway를 실행하는 Foreground Service
 *
 * Ktor 기반 순수 Kotlin 서버를 사용하여 Node.js 의존성 없이 동작합니다.
 */
class OpenClawGatewayService : Service() {

    companion object {
        const val TAG = "OpenClawGatewayService"
        const val NOTIFICATION_ID = 1001
        const val DEFAULT_PORT = 18789

        // Actions
        const val ACTION_START = "ai.openclaw.android.action.START"
        const val ACTION_STOP = "ai.openclaw.android.action.STOP"
        const val ACTION_RESTART = "ai.openclaw.android.action.RESTART"

        // 상태 Flow
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        private val _statusMessage = MutableStateFlow("중지됨")
        val statusMessage: StateFlow<String> = _statusMessage

        private val _statusDetail = MutableStateFlow("")
        val statusDetail: StateFlow<String> = _statusDetail

        private val _gatewayUrl = MutableStateFlow("")
        val gatewayUrl: StateFlow<String> = _gatewayUrl

        private val _installProgress = MutableStateFlow(0)
        val installProgress: StateFlow<Int> = _installProgress

        private val _connectedClients = MutableStateFlow(0)
        val connectedClients: StateFlow<Int> = _connectedClients
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var gatewayServer: GatewayServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        startForeground(NOTIFICATION_ID, createNotification("OpenClaw Gateway", "초기화 중..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startGateway()
            ACTION_STOP -> stopGateway()
            ACTION_RESTART -> {
                stopGateway()
                serviceScope.launch {
                    delay(500)
                    startGateway()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        stopGatewayInternal()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startGateway() {
        if (_isRunning.value) {
            Log.w(TAG, "Gateway already running")
            return
        }

        serviceScope.launch {
            try {
                _statusMessage.value = "시작 중..."
                _statusDetail.value = "서버 초기화 중..."
                _installProgress.value = 50
                updateNotification("서버 시작 중...", "")

                // Gateway 서버 생성 및 시작
                gatewayServer = GatewayServer(DEFAULT_PORT)

                if (gatewayServer?.start() == true) {
                    val ip = getLocalIpAddress()
                    _gatewayUrl.value = "ws://$ip:$DEFAULT_PORT"
                    _isRunning.value = true
                    _statusMessage.value = "실행 중"
                    _statusDetail.value = "Ktor Server v1.0.0"
                    _installProgress.value = 100

                    updateNotification("실행 중", "$ip:$DEFAULT_PORT")
                    Log.i(TAG, "Gateway started at ${_gatewayUrl.value}")

                    // 클라이언트 연결 상태 모니터링
                    monitorConnections()
                } else {
                    throw Exception("서버 시작 실패")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start gateway", e)
                _isRunning.value = false
                _statusMessage.value = "오류"
                _statusDetail.value = e.message ?: "알 수 없는 오류"
                _installProgress.value = 0
                updateNotification("오류", e.message ?: "알 수 없는 오류")
            }
        }
    }

    private fun stopGateway() {
        Log.d(TAG, "Stopping gateway (external request)")
        stopGatewayInternal()
    }

    private fun stopGatewayInternal() {
        gatewayServer?.stop()
        gatewayServer = null
        _isRunning.value = false
        _statusMessage.value = "중지됨"
        _statusDetail.value = ""
        _gatewayUrl.value = ""
        _installProgress.value = 0
        _connectedClients.value = 0
        updateNotification("중지됨", "")
    }

    private fun monitorConnections() {
        serviceScope.launch {
            while (_isRunning.value) {
                GatewayServer.connectedClients.collect { count ->
                    _connectedClients.value = count
                }
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.isSiteLocalAddress) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get IP address", e)
        }
        return "127.0.0.1"
    }

    private fun createNotification(title: String, message: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OpenClawApplication.CHANNEL_ID_GATEWAY)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, message))
    }

    private suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}
